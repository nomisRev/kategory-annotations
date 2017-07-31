package kategory.io

import com.google.auto.service.AutoService
import kategory.io.generation.FileGenerator
import kategory.io.utils.AbsImplicitsProcessor
import kategory.io.utils.ClassOrPackageDataWrapper
import kategory.io.utils.getParameter
import kategory.io.utils.getPropertyOrNull
import kategory.io.utils.isCompanionOrObject
import kategory.io.utils.knownError
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import me.eugeniomarletti.kotlin.metadata.kaptGeneratedOption
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

//TODO support constructors
//TODO check that providers are accessible (public file/object)
//TODO check that consumers are accessible
//TODO better errors, plus log the fully qualified name/signature of the kotlin element
//TODO support non-object classes with empty constructors
//TODO maybe support enums?
//TODO generate overloads when consumer have parameters with default values
//TODO support implicit parameters in provider
//TODO check that generated functions don't conflict with existing functions

//TODO support generic types

//TODO open issue about nested functions not having stubs, resulting in annotations not going through processing

@AutoService(Processor::class)
class ImplicitsProcessor : AbsImplicitsProcessor() {

    companion object {
        const val useTypeAliasOption = "kategory.io.useTypeAlias"
    }

    private val useTypeAlias by lazy { options[useTypeAliasOption] == "true" }

    private val annotatedList = mutableListOf<Annotated>()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(implicitAnnotationClass.canonicalName)

    override fun getSupportedOptions(): Set<String> = setOf(useTypeAliasOption)

    override fun onProcess(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
        annotatedList += roundEnv
            .getElementsAnnotatedWith(implicitAnnotationClass)
            .map { element ->
                when (element.kind) {
                    ElementKind.PARAMETER -> processParameter(element as VariableElement)
                    ElementKind.METHOD -> processMethod(element as ExecutableElement)
                    else -> knownError("$implicitAnnotationName can only be used on function/constructor arguments, properties or functions")
                }
            }

        if (roundEnv.processingOver()) {
            val generatedDir = File(options[kaptGeneratedOption], implicitAnnotationClass.simpleName).also { it.mkdirs() }
            FileGenerator(generatedDir, annotatedList, useTypeAlias).generate()
        }
    }

    private fun processParameter(parameterElement: VariableElement): Annotated {
        val methodElement = parameterElement.enclosingElement as ExecutableElement
        if (methodElement.kind == CONSTRUCTOR) knownError("$implicitAnnotationName constructor parameters are not yet supported")

        val classElement = methodElement.enclosingElement as TypeElement
        val proto = getClassOrPackageDataWrapper(classElement)
        val function = proto.getFunction(methodElement)
        val parameter = proto.getParameter(function, parameterElement)
        if (parameter.declaresDefaultValue) knownError("Parameters annotated with $implicitAnnotationName can't have default values")

        return Annotated.Consumer.ValueParameter(classElement, proto, function, parameter)
    }

    private fun processMethod(methodElement: ExecutableElement): Annotated {
        val classElement = methodElement.enclosingElement as TypeElement
        val proto = getClassOrPackageDataWrapper(classElement)
        if (proto is ClassOrPackageDataWrapper.Class && !proto.classProto.classKind.isCompanionOrObject)
            knownError("Only properties/functions that are top level or inside a companion/object can be $implicitAnnotationName providers")

        val property = proto.getPropertyOrNull(methodElement)
        if (property != null) return Annotated.Provider.Property(classElement, proto, property)

        val function = proto.getFunction(methodElement)
        if (function.valueParameterList.any { !it.declaresDefaultValue })
            knownError("$implicitAnnotationName functions must have no parameters or those parameters must have default values")

        return Annotated.Provider.Function(classElement, proto, function)
    }
}
