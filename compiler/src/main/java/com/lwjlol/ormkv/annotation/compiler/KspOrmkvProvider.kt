package com.lwjlol.ormkv.annotation.compiler

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.lwjlol.ormkv.OrmKvHandler
import com.lwjlol.ormkv.annotation.ColumnInfo
import com.lwjlol.ormkv.annotation.Entity
import com.lwjlol.ormkv.annotation.Ignore
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.OutputStream

/**
 * @author luwenjie on 2022/2/21 20:21:50
 */
fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
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
        if (invoked) {
            return emptyList()
        }
        if (LOG) {
            file = codeGenerator.createNewFile(Dependencies(false), "", TAG, "log")
        }
        emit("$TAG: init($options)", "")


        val files = resolver.getAllFiles()
        emit("$TAG: process()", "")
        val visitor = OrmkvVisitor()
        for (file in files) {
            emit("$TAG: processing ${file.fileName}", "\n")
            file.accept(visitor, Unit)
        }
        invoked = true
        return emptyList()
    }

    private fun generateClass(
        className: String,
        packageName: String,
        parameters: List<KSValueParameter>,
        entityArgs: Map<String, Any?>
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
        val typeSpec = TypeSpec.objectBuilder(fileName)
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
            val ignore =
                member.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == Ignore::class.qualifiedName } != null
            val columnInfo =
                member.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == ColumnInfo::class.qualifiedName }
            var defValue: String = ""
            var enableReset = true
            columnInfo?.arguments?.forEach {
                if (it.name?.asString() == "defValue") {
                    defValue = it.value?.toString() ?: ""
                }
                if (it.name?.asString() == "enableReset") {
                    enableReset = it.value as Boolean
                }
            }
            val memberTypeName = member.type.resolve().declaration.qualifiedName?.asString() ?: ""
            val valueName = "_${member.name?.asString()}"
            val propertyName = member.name?.asString() ?: ""
            var getName = ""

            val prefix = "$packageName.${className}"
            val keyName = if (prefixKeyArg) "${prefix}_$propertyName" else propertyName
            when (memberTypeName) {
                "kotlin.String" -> {
                    getName = "get(\"$keyName\", \"\"\"$defValue\"\"\") as String"
                }
                "kotlin.Float" -> {
                    defValue = defValue.ifEmpty { "0F" }
                    getName = "get(\"$keyName\", $defValue) as Float"
                }
                "kotlin.Int" -> {
                    defValue = defValue.ifEmpty { "0" }
                    getName = "get(\"$keyName\", $defValue) as Int"
                }
                "kotlin.Long" -> {
                    defValue = defValue.ifEmpty { "0L" }
                    getName = "get(\"$keyName\", $defValue) as Long"
                }
                "kotlin.Boolean" -> {
                    defValue = defValue.ifEmpty { "false" }
                    getName = "get(\"$keyName\", $defValue) as Boolean"
                }
                "kotlin.ByteArray" -> {
                    defValue = defValue.ifEmpty { "ByteArray(0)" }
                    getName = "get(\"$keyName\", ByteArray(0)) as ByteArray"
                }
                else -> {
                    defValue = "error"
                }
            }
            // ignore
            if (ignore && !toModelError) {
                if (defValue == "error") {
                    toModelError = true
                    return@forEachIndexed
                }
                if (memberTypeName.contains("String")) {
                    toModelCode.append("|$propertyName = \"$defValue\", \n")
                } else {
                    toModelCode.append("|$propertyName = $defValue, \n")
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
                    clearCode.append("$propertyName = \"\"\"$defValue\"\"\" \n")
                } else {
                    clearCode.append("$propertyName = $defValue \n")
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
        file.writeTo(codeGenerator = codeGenerator, dependencies = Dependencies(false))
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
        override fun visitFile(file: KSFile, data: Unit) {
            if (checkVisited(file)) return
            file.annotations.forEach { it.accept(this, data) }
            emit(file.filePath, "visit file: ")
            for (declaration in file.declarations) {
                declaration.accept(this, data)
            }
        }

        override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
            if (checkVisited(annotation)) return
            emit(
                annotation.annotationType.resolve().declaration.qualifiedName?.asString() ?: "",
                "visit annotation:  ",
            )
            annotation.annotationType.accept(this, data)
            annotation.arguments.forEach { it.accept(this, data) }
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (checkVisited(classDeclaration)) return
            emit(classDeclaration.classKind.type, "class type: ")
            classDeclaration.annotations.forEach { it.accept(this, Unit) }
            classDeclaration.annotations.forEach {
                emit(
                    "${it.annotationType.resolve().declaration.qualifiedName?.asString()}",
                    "visitClassDeclaration:"
                )
            }
            val className = classDeclaration.simpleName.getShortName() ?: ""


            classDeclaration.getConstructors().maxByOrNull {
                it.parameters.size
            }?.parameters?.forEach {
                emit(
                    "${it.type.resolve().declaration.qualifiedName?.asString() ?: ""}:${it.name?.asString() ?: ""}",
                    "visit constructor parameters: "
                )
            }

            val parameters = classDeclaration.getConstructors().maxByOrNull {
                it.parameters.size
            }?.parameters ?: return


            classDeclaration.getDeclaredProperties().forEach {
                emit(it.qualifiedName?.asString() ?: "", "visit getDeclaredProperties : ")
            }
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
                    entityArgs = argsMap
                )
            }


        }


        private val visited = HashSet<Any>()

        private fun checkVisited(symbol: Any): Boolean {
            return if (visited.contains(symbol)) {
                true
            } else {
                visited.add(symbol)
                false
            }
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