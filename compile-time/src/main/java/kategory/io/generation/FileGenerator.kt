package kategory.io.generation

import kategory.io.Annotated
import kategory.io.Annotated.Consumer
import kategory.io.Annotated.Consumer.ValueParameter
import kategory.io.Annotated.Provider
import kategory.io.Annotated.Provider.Function
import kategory.io.Annotated.Provider.Property
import kategory.io.utils.ClassOrPackageDataWrapper
import kategory.io.implicitAnnotationClass
import kategory.io.implicitAnnotationName
import kategory.io.utils.escapedClassName
import kategory.io.utils.extractFullName
import kategory.io.utils.knownError
import kategory.io.utils.plusIfNotBlank
import me.eugeniomarletti.kotlin.metadata.getJvmMethodSignature
import org.jetbrains.kotlin.serialization.ProtoBuf
import java.io.File

typealias Type = String
typealias Package = String
typealias ProviderInvocation = String
typealias FunctionToGenerate = String

class FileGenerator(
    private val generatedDir: File,
    private val annotatedList: List<Annotated>,
    private val useTypeAlias: Boolean
) {

    fun generate() {
        val consumers = annotatedList.filterIsInstance<Consumer>()
        val providers = annotatedList.filterIsInstance<Provider>()
        if (providers.isEmpty() && consumers.isEmpty()) return

        val providersByType = getProvidersByTypes(providers)
        checkMissingProvidedTypes(consumers, providersByType)

        val providerInvocationsByType = getProviderInvocationsByType(providersByType)
        val consumerFunctionGroupsByPackage = getConsumerFunctionGroupsByPackage(consumers)
        val functionsToGenerateByPackage = getFunctionsToGenerateByPackage(consumerFunctionGroupsByPackage, providerInvocationsByType)

        functionsToGenerateByPackage.entries.forEachIndexed { counter, (`package`, functionsToGenerate) ->
            val source = functionsToGenerate.joinToString(prefix = "package $`package`\n\n", separator = "\n")
            val file = File(generatedDir, implicitAnnotationClass.simpleName + "Extensions$counter.kt")
            file.writeText(source)
        }
    }

    private fun getProvidersByTypes(providers: List<Provider>): Map<Type, Provider> {
        val providersByType = providers.groupBy { provider ->
            when (provider) {
                is Provider.Function -> provider.functionProto.returnType
                is Provider.Property -> provider.propertyProto.returnType
            }.extractFullName(provider.classOrPackageProto, useTypeAlias)
        }

        val duplicatedProviders = providersByType.filter { (_, providers) -> providers.size > 1 }
        if (duplicatedProviders.isNotEmpty())
            knownError("These $implicitAnnotationName types are provided more than once: $duplicatedProviders")

        return providersByType.mapValues { (_, providers) -> providers[0] }
    }

    private fun checkMissingProvidedTypes(
        consumers: List<Consumer>,
        providersByType: Map<Type, Provider>
    ) {
        val consumersByType = consumers.groupBy { consumer ->
            when (consumer) {
                is Consumer.ValueParameter -> consumer.valueParameterProto.type
            }.extractFullName(consumer.classOrPackageProto, useTypeAlias)
        }

        val missingProvidedTypes = consumersByType.keys - providersByType.keys
        if (missingProvidedTypes.isNotEmpty())
            knownError("These $implicitAnnotationName types are requested but not provided: $missingProvidedTypes")
    }

    private fun getProviderInvocationsByType(providersByType: Map<Type, Provider>): Map<Type, ProviderInvocation> =
        providersByType.mapValues { (_, provider) ->
            val proto = provider.classOrPackageProto
            val nameResolver = proto.nameResolver
            val prefix = when (proto) {
                is ClassOrPackageDataWrapper.Package -> proto.`package`
                is ClassOrPackageDataWrapper.Class -> nameResolver.getString(proto.classProto.fqName)
            }.escapedClassName.plusIfNotBlank(".")

            when (provider) {
                is Function -> {
                    val name = nameResolver.getString(provider.functionProto.name)
                    "$prefix`$name`()"
                }
                is Property -> {
                    val name = nameResolver.getString(provider.propertyProto.name)
                    "$prefix`$name`"
                }
            }
        }

    private fun getConsumerFunctionGroupsByPackage(consumers: List<Consumer>): Map<Package, List<List<Consumer>>> =
        consumers
            .groupBy { consumer ->
                when (consumer) {
                    is ValueParameter -> {
                        val proto = consumer.classOrPackageProto
                        val nameResolver = proto.nameResolver
                        val function = consumer.functionProto
                        val signature = function.getJvmMethodSignature(nameResolver)
                        val fqFunctionSignature = when (proto) {
                            is ClassOrPackageDataWrapper.Package -> proto.`package`
                            is ClassOrPackageDataWrapper.Class -> nameResolver.getString(proto.classProto.fqName).replace('/', '.')
                        }.plusIfNotBlank(".") + signature
                        fqFunctionSignature
                    }
                }
            }
            .values
            .groupBy { consumersInFunction -> consumersInFunction[0].classOrPackageProto.`package` }

    private fun getFunctionsToGenerateByPackage(
        consumerFunctionGroupsByPackage: Map<Package, List<List<Consumer>>>,
        providerInvocationsByType: Map<Type, ProviderInvocation>
    ): Map<Package, List<FunctionToGenerate>> =
        consumerFunctionGroupsByPackage.mapValues { (`package`, consumerFunctionGroup) ->
            consumerFunctionGroup.map { consumersInFunction ->
                val first = consumersInFunction[0]
                val function = when (first) {
                    is Consumer.ValueParameter -> first.functionProto
                }
                val proto = first.classOrPackageProto
                val nameResolver = proto.nameResolver
                val escapedPackage = `package`.escapedClassName
                val prefix = when (proto) {
                    is ClassOrPackageDataWrapper.Package -> ""
                    is ClassOrPackageDataWrapper.Class ->
                        nameResolver.getString(proto.classProto.fqName).escapedClassName.removePrefix(escapedPackage + ".") + "."
                }
                val escapedFunctionName = "`" + nameResolver.getString(function.name) + "`"

                val argsIn = function.valueParameterList.mapNotNull { valueParameter ->
                    extractConsumerValueParameter(valueParameter, consumersInFunction) { parameterName, consumer ->
                        valueParameter.takeIf { consumer == null }?.let {
                            "$parameterName: " + it.type.extractFullName(proto, failOnGeneric = false)
                        }
                    }
                }.joinToString()

                val argsOut = function.valueParameterList.map { valueParameter ->
                    extractConsumerValueParameter(valueParameter, consumersInFunction) { parameterName, consumer ->
                        if (consumer == null) parameterName
                        else {
                            val type = when (consumer) {
                                is ValueParameter -> consumer.valueParameterProto.type
                            }.extractFullName(consumer.classOrPackageProto, useTypeAlias, failOnGeneric = false)
                            providerInvocationsByType[type]!!
                        }
                    }
                }.joinToString()

                "fun $prefix$escapedFunctionName($argsIn) = $escapedFunctionName($argsOut)"
            }
        }

    private inline fun <T> extractConsumerValueParameter(
        valueParameter: ProtoBuf.ValueParameter,
        consumersInFunction: List<Consumer>,
        action: (parameterName: String, consumer: Consumer?) -> T
    ): T {
        val nameResolver = consumersInFunction[0].classOrPackageProto.nameResolver
        val paramName = nameResolver.getString(valueParameter.name)
        val consumer = consumersInFunction.firstOrNull {
            val consumerParameterName = when (it) {
                is Consumer.ValueParameter -> nameResolver.getString(it.valueParameterProto.name)
            }
            paramName == consumerParameterName
        }
        return action(paramName, consumer)
    }
}
