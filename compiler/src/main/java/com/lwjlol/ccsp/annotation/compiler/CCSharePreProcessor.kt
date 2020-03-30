package com.lwjlol.ccsp.annotation.compiler

import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Entity
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
@SupportedAnnotationTypes(value = ["com.lwjlol.ccsp.annotation.Entity", "com.lwjlol.ccsp.annotation.ColumnInfo"])
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
                    val allMembers = element.enclosedElements

                    val className = element.simpleName.toString()
                    val packageName = elementUtils.getPackageOf(element).toString()

                    generateClass(className, packageName, allMembers, entity)
                }
            }
        return true
    }

    private fun generateClass(
        className: String,
        packageName: String,
        allMembers: List<Element>,
        entity: Entity
    ) {

        val fileName = if (entity.name.isNotEmpty()) entity.name else "${className}_${PRE_FIX}"
        val typeSpec = TypeSpec.objectBuilder(fileName)
            .addProperty(
                PropertySpec.builder(
                    "sp",
                    Class.forName("android.content.SharedPreferences"), KModifier.PRIVATE
                ).initializer(entity.getSpCode)
                    .build()
            )

        val clearCode = StringBuilder()
        allMembers.forEach { member ->
            if (member.kind.isField && !member.modifiers.contains(Modifier.STATIC)) {
                val name = member.asType().asTypeName()

                val valueName = "_${member.simpleName}"
                val propertyName = member.simpleName.toString()
                val typeName =
                    if (name.toString().contains("String")) ClassName("kotlin", "String") else name

                typeSpec.addProperty(
                    PropertySpec.builder(valueName, typeName.copy(true))
                        .initializer("null")
                        .addModifiers(KModifier.PRIVATE)
                        .mutable(true)
                        .build()

                )
                val spColumnInfo = member.getAnnotation(ColumnInfo::class.java)
                val s = typeName.toString()
                val defValue = when {
                    s.contains("String") -> if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue else ""
                    s.contains("Boolean") -> "${if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue.toBoolean() else false}"
                    s.contains("Int") -> "${if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue.toInt() else 0}"
                    s.contains("Long") -> "${if (spColumnInfo.defValue.isNotEmpty()) spColumnInfo.defValue.toLong() else 0}"
                    s.contains("Float") -> {
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
                    s.contains("String") -> "getString(\"$propertyName\", \"$defValue\")"
                    s.contains("Boolean") -> "getBoolean(\"$propertyName\", $defValue)"
                    s.contains("Int") -> "getInt(\"$propertyName\", $defValue)"
                    s.contains("Long") -> "getLong(\"$propertyName\", $defValue)"
                    s.contains("Float") -> "getFloat(\"$propertyName\", $defValue)"

                    else -> "unsupport"
                }

                val setName = when {
                    s.contains("String") -> "putString(\"$propertyName\", value)"
                    s.contains("Boolean") -> "putBoolean(\"$propertyName\", value)"
                    s.contains("Int") -> "putInt(\"$propertyName\", value)"
                    s.contains("Long") -> "putLong(\"$propertyName\", value)"
                    s.contains("Float") -> "putFloat(\"$propertyName\", value)"
                    else -> "unsupport"
                }


                typeSpec.addProperty(
                    PropertySpec.builder(propertyName, typeName)
                        .mutable(true)
                        .getter(
                            FunSpec.getterBuilder()
                                .addCode(
                                    """
                                    |if ($valueName == null) {
                                    |   $valueName = sp.$getName
                                    |}
                                    |return $valueName!!
                                    |""".trimMargin()
                                )
                                .build()
                        )
                        .setter(
                            FunSpec.setterBuilder().addParameter("value", typeName).addCode(
                                """
                            |if ($valueName == value) return
                            |$valueName = value
                            |sp.edit().$setName.apply()
                            |""".trimMargin()
                            ).build()
                        )
                        .build()
                )

                if (typeName.toString().contains("String")) {
                    clearCode.append("$propertyName = \"$defValue\" \n")
                } else {
                    clearCode.append("$propertyName = $defValue \n")
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
    }
}