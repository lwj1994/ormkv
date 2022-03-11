package com.lwjlol.ormkv.annotation.compiler

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.lwjlol.ormkv.OrmKvHandler
import com.lwjlol.ormkv.annotation.ColumnInfo
import com.lwjlol.ormkv.annotation.Entity
import com.lwjlol.ormkv.annotation.Ignore
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException
import java.io.OutputStream

/**
 * @author luwenjie on 2022/2/21 20:21:50
 */
fun OutputStream.appendText(str: String) {
    try {
        this.write(str.toByteArray())
    } catch (e: IOException) {

    }
}

class KspOrmkvProcessor(
    val codeGenerator: CodeGenerator,
    val options: Map<String, String>,
    val logger: KSPLogger
) : SymbolProcessor {
    var invoked = false
    lateinit var file: OutputStream
    fun emit(s: String, indent: String) {
        if (!LOG) return
        file.appendText("$indent$s\n")
    }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        if (LOG && !invoked) {
            file = codeGenerator.createNewFile(Dependencies(false), "", TAG, "log")
        }
        emit("$invoked", "invoked $resolver  ")
        emit("$TAG: init($options)", "")

        val entityClassName = Entity::class.qualifiedName ?: ""
        emit("$TAG: $entityClassName", "entityClassName:")

        val entitySymbols = resolver.getSymbolsWithAnnotation(entityClassName)
        val ret = entitySymbols.filter { !it.validate() }.toList()

        emit("$TAG: process() ${entitySymbols.count()} entitySymbols", "")
        emit("$TAG: process() ${resolver.getAllFiles().count()} AllFiles", "")
        val ormkvVisitor = OrmkvVisitor()
        entitySymbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                emit("$TAG: processing ${it.containingFile?.filePath}", "\n")
                it.accept(ormkvVisitor, Unit)
            }
        invoked = true
        return ret
    }

    private fun generateClass(
        className: String,
        packageName: String,
        parameters: List<KSValueParameter>,
        entityArgs: Map<String, Any?>,
        source: KSFile
    ) {
        val classNameArg = entityArgs["className"] as? String ?: ""
        val prefixKeyArg = entityArgs["prefixKeyWithClassName"] as? Boolean ?: false
        val handlerCodeReferenceArg = entityArgs["handlerCodeReference"] as? String ?: ""
        var name = classNameArg
        val generatePackageName = if (classNameArg.contains('.')) {
            name = classNameArg.substringAfterLast('.')
            classNameArg.substringBeforeLast('.')
        } else {
            packageName
        }
        if (name.isEmpty()) {
            name = className
        }
        val fileName = if (classNameArg.isNotEmpty()) name else "${name}${END_FIX}"
        val typeSpec = TypeSpec.classBuilder(fileName)
            .addKdoc("this class is generated by https://github.com/lwj1994/ormkv for [${packageName}.${className}], Please don't modify it!")

        if (handlerCodeReferenceArg.isEmpty()) {
            return
        }
        typeSpec.addProperty(
            PropertySpec.builder(
                HANDLER,
                OrmKvHandler::class,
                KModifier.PRIVATE
            ).initializer(handlerCodeReferenceArg).build()
        )
        // clear code
        val clearCode = StringBuilder()
        val toStringCode = StringBuilder()
        val toModelCode = StringBuilder()
        val updateCode = StringBuilder()

        var toModelError = false
        parameters.forEachIndexed { _, member ->
            emit(
                "${member.type.resolve().declaration.qualifiedName?.asString() ?: ""}:${member.name?.asString() ?: ""}",
                "visit constructor parameters: "
            )
            val ignore =
                member.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == Ignore::class.qualifiedName } != null
            val columnInfo =
                member.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == ColumnInfo::class.qualifiedName }
            var defaultValue: String = ""
            var columnName = member.name?.asString() ?: ""
            var enableReset = true
            columnInfo?.arguments?.forEach {
                if (it.name?.asString() == DEFAULT_VALUE) {
                    defaultValue = it.value?.toString() ?: ""
                }
                if (it.name?.asString() == NAME) {
                    it.value?.toString()?.apply {
                        if (isNotEmpty()) columnName = this
                    }
                }
                if (it.name?.asString() == ENABLE_RESET) {
                    enableReset = it.value as Boolean
                }
            }
            val memberTypeName = member.type.resolve().declaration.qualifiedName?.asString() ?: ""
            val valueName = "_${member.name?.asString()}"
            val propertyName = member.name?.asString() ?: ""
            val keyUnitName = columnName.ifEmpty { member.name?.asString() ?: "" }
            var getName = ""

            val prefix = "$packageName.${className}"
            val keyName = if (prefixKeyArg) "${prefix}_$keyUnitName" else keyUnitName
            when (memberTypeName) {
                "kotlin.String" -> {
                    getName = "get(\"$keyName\", \"\"\"$defaultValue\"\"\") as String"
                }
                "kotlin.Float" -> {
                    defaultValue = defaultValue.ifEmpty { "0F" }
                    if (!defaultValue.endsWith('F')) {
                        defaultValue = "${defaultValue}F"
                    }
                    getName = "get(\"$keyName\", $defaultValue) as Float"
                }
                "kotlin.Int" -> {
                    defaultValue = defaultValue.ifEmpty { "0" }
                    getName = "get(\"$keyName\", $defaultValue) as Int"
                }
                "kotlin.Long" -> {
                    defaultValue = defaultValue.ifEmpty { "0L" }
                    if (!defaultValue.endsWith('L')) {
                        defaultValue = "${defaultValue}L"
                    }
                    getName = "get(\"$keyName\", $defaultValue) as Long"
                }
                "kotlin.Boolean" -> {
                    defaultValue = defaultValue.ifEmpty { "false" }
                    getName = "get(\"$keyName\", $defaultValue) as Boolean"
                }
                "kotlin.ByteArray" -> {
                    defaultValue = defaultValue.ifEmpty { "ByteArray(0)" }
                    getName = "get(\"$keyName\", ByteArray(0)) as ByteArray"
                }
                else -> {
                    defaultValue = "error"
                }
            }
            // ignore
            if (ignore && !toModelError) {
                if (defaultValue == "error") {
                    toModelError = true
                    return@forEachIndexed
                }
                if (memberTypeName.contains("String")) {
                    toModelCode.append("|$propertyName = \"$defaultValue\", \n")
                } else {
                    toModelCode.append("|$propertyName = $defaultValue, \n")
                }
                return@forEachIndexed
            }
            val typeName = ClassName.bestGuess(memberTypeName)
            typeSpec.addProperty(
                PropertySpec.builder(valueName, typeName.copy(nullable = true))
                    .initializer("null")
                    .addModifiers(KModifier.PRIVATE)
                    .mutable(true)
                    .build()
            )

            val setName = """put("$keyName", value)"""
            typeSpec.addProperty(
                PropertySpec.builder(propertyName, typeName)
                    .mutable(true)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                """
                                    |if ($valueName == null) {
                                    |   $valueName = ${HANDLER}.$getName
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
                            |${HANDLER}.$setName
                            |""".trimMargin()
                        ).build()
                    )
                    .build()
            )
            toStringCode.append("|$propertyName = $$propertyName\n")
            toModelCode.append("|$propertyName = $propertyName, \n")
            updateCode.append("|$propertyName = model.$propertyName\n")
            if (enableReset) {
                if (memberTypeName.contains("String")) {
                    clearCode.append("$propertyName = \"\"\"$defaultValue\"\"\" \n")
                } else {
                    clearCode.append("$propertyName = $defaultValue \n")
                }
            }
        }

        // add reset()
        typeSpec.addFunction(
            FunSpec.builder("reset")
                .addCode(
                    """
              |$clearCode
            """.trimMargin()
                )
                .build()
        ).addToString("return \"\"\"$toStringCode\"\"\".trimMargin()")
            .addUpdate(updateCode.toString().trimMargin(), ClassName(packageName, className))
        if (!toModelError) {
            typeSpec.addToModel(toModelCode.toString(), "$packageName.$className")
        }

        // write file
        val file = FileSpec.builder(generatePackageName, fileName).addType(typeSpec.build()).build()
        file.writeTo(codeGenerator = codeGenerator, dependencies = Dependencies(false, source))
    }


    private fun TypeSpec.Builder.addToString(code: String): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("toString")
                .returns(String::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(code)
                .build()
        )
    }

    private fun TypeSpec.Builder.addToModel(code: String, className: String): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("toModel")
                .returns(ClassName.bestGuess(className))
                .addCode(
                    """
                    |return $className(
                    ${code.trim('\n').trim(' ').trim(',')})             
                """.trimMargin()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.addUpdate(code: String, typeName: TypeName): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("update")
                .addParameter(ParameterSpec.builder("model", typeName).build())
                .addCode(code)
                .build()
        )
    }

    inner class OrmkvVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            emit(classDeclaration.classKind.type, "visitClassDeclaration class type: ")
            classDeclaration.annotations.forEach {
                it.accept(this, Unit)
                emit(
                    "${it.annotationType.resolve().declaration.qualifiedName?.asString()}",
                    "visitClassDeclaration:"
                )
            }
            val className = classDeclaration.simpleName.getShortName()
            val parameters = classDeclaration.getConstructors().maxByOrNull {
                it.parameters.size
            }?.parameters ?: return

            classDeclaration.annotations.filter {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Entity::class.qualifiedName
            }.forEach { annotation ->
                val argsMap = mutableMapOf<String, Any?>()
                annotation.arguments.forEach {
                    argsMap[it.name?.asString() ?: ""] = it.value
                    emit(
                        "name:${it.name?.asString()},value:${it.value?.toString()},spread:${it.isSpread}",
                        "$className:${annotation.shortName.asString()}  "
                    )
                }


                generateClass(
                    className = className,
                    packageName = classDeclaration.packageName.asString(),
                    parameters = parameters,
                    entityArgs = argsMap,
                    source = classDeclaration.containingFile ?: return@forEach
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        if (LOG) {
            file.close()
        }
    }
}


class KspOrmkvProvider : SymbolProcessorProvider {


    companion object {
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KspOrmkvProcessor(environment.codeGenerator, environment.options, environment.logger)
    }
}

private const val END_FIX = "Registry"
private const val LOG = false
private const val HANDLER = "kvHandler"
private const val TAG = "KspOrmkv"
private const val DEFAULT_VALUE = "defaultValue"
private const val NAME = "name"
private const val ENABLE_RESET = "enableReset"