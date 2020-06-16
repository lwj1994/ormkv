package com.lwjlol.ccsp.annotation.compiler

import com.lwjlol.ccsp.CcspEncrypt
import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Encrypt
import com.lwjlol.ccsp.annotation.Entity
import com.lwjlol.ccsp.annotation.Skip
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * @author luwenjie on 2019-08-11 19:17:41
 */

//@AutoService(value = [Process::class])
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(value = ["com.lwjlol.ccsp.annotation.Entity", "com.lwjlol.ccsp.annotation.ColumnInfo", "com.lwjlol.ccsp.annotation.Skip", "com.lwjlol.ccsp.annotation.Encrypt"])
class CCSharePreProcessor : AbstractProcessor() {
    private var messager: Messager? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        messager = processingEnv?.messager
    }

    private fun print(text: String) {
        if (!LOG) return
        messager?.printMessage(Diagnostic.Kind.NOTE, text)
        println("CCSharePreProcessor--- $text")
    }

    private fun encodeCode(value: String, secret: String, isValueString: Boolean = true) =
        if (isValueString) {
            """${ENCRYPTUTIL}.encode("$value","$secret")"""
        } else {
            """${ENCRYPTUTIL}.encode($value,"$secret")"""
        }


    /**
     * @param isValueString value 是否是一个字符串， false：变量
     */
    private fun decodeCode(value: String, secret: String, isValueString: Boolean = true) =
        if (isValueString) {
            """${ENCRYPTUTIL}.decode("$value","$secret")"""
        } else {
            """${ENCRYPTUTIL}.decode($value,"$secret")"""
        }

    private fun setValueCode(value: String, encrypt: Encrypt?, isValueString: Boolean = true) =
        if (encrypt != null) encodeCode(value, encrypt.secret, isValueString) else value


    private fun getValueCode(
        value: String,
        encrypt: Encrypt?,
        isValueString: Boolean = true
    ) =
        if (encrypt != null) decodeCode(value, encrypt.secret, isValueString) else value


    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elementUtils = processingEnv.elementUtils
        print("process")
        annotations.forEach {
            print(it::class.java.name)
        }

        roundEnv.getElementsAnnotatedWith(Entity::class.java)
            .forEach { element ->
                if (element.kind.isClass) {
                    val entity = element.getAnnotation(Entity::class.java)
                    val encrypt = element.getAnnotation(Encrypt::class.java)
                    val allMembers = element.enclosedElements

                    val className = element.simpleName.toString()
                    val packageName = elementUtils.getPackageOf(element).toString()

                    generateClass(className, packageName, allMembers, entity, encrypt)
                }
            }
        return true
    }


    private fun appendd(s:String): String {
        return "\"\"\"$s\"\"\""
    }

    private fun generateClass(
        className: String,
        packageName: String,
        allMembers: List<Element>,
        entity: Entity,
        encrypt: Encrypt?
    ) {
        val fileName = if (entity.name.isNotEmpty()) entity.name else "${className}_${PRE_FIX}"
        val typeSpec = TypeSpec.objectBuilder(fileName)
        if (encrypt != null) {
            typeSpec.addProperty(
                PropertySpec.builder(
                    ENCRYPTUTIL,
                    Class.forName(CcspEncrypt::class.java.canonicalName),
                    KModifier.PRIVATE
                ).initializer(encrypt.getEncryptCode).build()
            )
        }
        typeSpec.addProperty(
            PropertySpec.builder(
                SP,
                Class.forName("android.content.SharedPreferences"),
                KModifier.PRIVATE
            ).initializer(entity.getSpCode).build()
        )


        val clearCode = StringBuilder()
        allMembers.forEach { member ->
            if (member.kind.isField && !member.modifiers.contains(Modifier.STATIC)) {
                val spSkip = member.getAnnotation(Skip::class.java)
                if (spSkip != null) return@forEach
                val spColumnInfo = member.getAnnotation(ColumnInfo::class.java)
                val defInitValue = spColumnInfo?.defValue ?: ""
                val clear = spColumnInfo?.clear ?: true

                val name = member.asType().asTypeName()
                val valueName = "_${member.simpleName}"
                val propertyName = member.simpleName.toString()
                print("propertyName = $propertyName")
                val typeName =
                    if (name.toString().contains("String")) ClassName("kotlin", "String") else name

                typeSpec.addProperty(
                    PropertySpec.builder(valueName, typeName.copy(true))
                        .initializer("null")
                        .addModifiers(KModifier.PRIVATE)
                        .mutable(true)
                        .build()

                )


                val paramType = typeName.toString()
                val defValue = when {
                    paramType.contains("String") -> if (defInitValue.isNotEmpty()) defInitValue else ""
                    paramType.contains("Boolean") -> "${if (defInitValue.isNotEmpty()) defInitValue.toBoolean() else false}"
                    paramType.contains("Int") -> "${if (defInitValue.isNotEmpty()) defInitValue.toInt() else 0}"
                    paramType.contains("Long") -> "${if (defInitValue.isNotEmpty()) defInitValue.toLong() else 0}"
                    paramType.contains("Float") -> {
                        val res =
                            (if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue.toFloat() else 0F).toString()
                        if (!res.contains("F")) {
                            "${res}F"
                        } else {
                            res
                        }
                    }

                    else -> "unsupport"
                }

                val getName = when {
                    paramType.contains("String") -> "getString(\"$propertyName\", \"\"\"$defValue\"\"\")"
                    paramType.contains("Boolean") -> "getBoolean(\"$propertyName\", $defValue)"
                    paramType.contains("Int") -> "getInt(\"$propertyName\", $defValue)"
                    paramType.contains("Long") -> "getLong(\"$propertyName\", $defValue)"
                    paramType.contains("Float") -> "getFloat(\"$propertyName\", $defValue)"

                    else -> "unsupport"
                }

                val valueStringCode =
                    if (encrypt != null) """${ENCRYPTUTIL}.encode(value,"${encrypt.secret}")""" else "value"
                val setName = when {
                    paramType.contains("String") -> "putString(\"$propertyName\", $valueStringCode)"
                    paramType.contains("Boolean") -> "putBoolean(\"$propertyName\", value)"
                    paramType.contains("Int") -> "putInt(\"$propertyName\", value)"
                    paramType.contains("Long") -> "putLong(\"$propertyName\", value)"
                    paramType.contains("Float") -> "putFloat(\"$propertyName\", value)"
                    else -> "unsupport"
                }


                typeSpec.addProperty(
                    PropertySpec.builder(propertyName, typeName)
                        .mutable(true)
                        .getter(
                            FunSpec.getterBuilder()
                                .addCode(
                                    if (paramType.contains("String")) {
                                        """
                                    |if ($valueName == null) {
                                    |   val $SP_ORIGIN_VALUE = $SP.$getName ?:""
                                    |   if($SP_ORIGIN_VALUE == ${appendd(defValue)}){
                                    |     $valueName = $SP_ORIGIN_VALUE     
                                    |   }else{
                                    |     $valueName = ${getValueCode(
                                            SP_ORIGIN_VALUE,
                                            encrypt,
                                            false
                                        )}
                                    |   } 
                                    |}
                                    |return $valueName!!
                                    |""".trimMargin()
                                    } else {
                                        """
                                    |if ($valueName == null) {
                                    |   $valueName = $SP.$getName
                                    |}
                                    |return $valueName!!
                                    |""".trimMargin()
                                    }
                                )
                                .build()
                        )
                        .setter(
                            FunSpec.setterBuilder().addParameter("value", typeName).addCode(
                                """
                            |if ($valueName == value) return
                            |$valueName = value
                            |$SP.edit().$setName.apply()
                            |""".trimMargin()
                            ).build()
                        )
                        .build()
                )
                if (clear) {
                    if (typeName.toString().contains("String")) {
                        clearCode.append("$propertyName = \"\"\"$defValue\"\"\" \n")
                    } else {
                        clearCode.append("$propertyName = $defValue \n")
                    }
                }
            }
        }


        typeSpec.addFunction(
            FunSpec.builder("clear")
                .addCode(
                    """
              |$clearCode
            """.trimMargin()
                )
                .build()
        )
        val file = FileSpec.builder(packageName, fileName).addType(typeSpec.build()).build()


        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "ccsp"))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private const val TAG = "CCSharePreProcessor"
        private const val PRE_FIX = "CCSP"
        private const val LOG = false
        private const val SP = "sp"
        private const val ENCRYPTUTIL = "encryptUtil"
        private const val SP_ORIGIN_VALUE = "spOriginValue"

    }
}