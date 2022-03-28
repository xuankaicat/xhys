package com.github.xuankaicat.xhys.ksp

import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.Import

@AutoService(SymbolProcessorProvider::class)
class BehaviourProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = BehaviourProcessor(
        options = environment.options,
        codeGenerator = environment.codeGenerator,
        logger = environment.logger
    )
}

class BehaviourProcessor(
    val options: Map<String, String>,
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {

    var invoked = false

    val fileSpec = FileSpec.builder(
        packageName =  "com.github.xuankaicat.xhys.ksp.generated",//BuildConfig.PACKAGE_NAME
        fileName = "Behaviour"
    )

    val funSpec = FunSpec.builder("initBehaviours")
        .receiver(IXhysBot::class)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(Behaviour::class.qualifiedName!!, true)
        val ret = symbols.filter { !it.validate() }

        symbols.filter { it is KSFunctionDeclaration && it.validate() }
            .forEach {
                it.accept(BehaviourVisitor(), Unit)
            }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "com.github.xuankaicat.xhys.ksp.generated",
            fileName = "Behaviour"
        ).use { outputStream ->
            outputStream.writer()
                .use {
                    fileSpec
                        .addFunction(funSpec.build())
                        .build()
                        .writeTo(it)
                }
        }

        invoked = true

        return ret.toList()
    }

    inner class BehaviourVisitor : KSVisitorVoid() {
        private lateinit var functionName: String
        private lateinit var packageName: String

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            functionName = function.simpleName.asString()
            packageName = function.packageName.asString()

            function.parentDeclaration?.simpleName?.let {
                packageName += ".${it.getShortName()}"
            }

            fileSpec.addImport(packageName, functionName)
            funSpec.addStatement("${functionName}()")
        }
    }
}