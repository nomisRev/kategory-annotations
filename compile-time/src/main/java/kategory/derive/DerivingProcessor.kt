package kategory.derive

import com.google.auto.service.AutoService
import kategory.common.utils.AbstractProcessor
import kategory.common.utils.ClassOrPackageDataWrapper
import kategory.common.utils.knownError
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.ElementFilter


@AutoService(Processor::class)
class DerivingProcessor : AbstractProcessor() {

    private val annotatedList: MutableList<AnnotatedDeriving> = mutableListOf()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(derivingAnnotationClass.canonicalName)

    /**
     * Processor entry point
     */
    override fun onProcess(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
        annotatedList += roundEnv
                .getElementsAnnotatedWith(derivingAnnotationClass)
                .map { element ->
                    when (element.kind) {
                        ElementKind.CLASS -> processClass(element as TypeElement)
                        else -> knownError("${derivingAnnotationName} can only be used on classes")
                    }
                }

        if (roundEnv.processingOver()) {
            val generatedDir = File(this.generatedDir!!, derivingAnnotationClass.simpleName).also { it.mkdirs() }
            DerivingFileGenerator(generatedDir, annotatedList).generate()
        }
    }

    fun getExecutableElement(typeElement: TypeElement, name: Name): ExecutableElement? {
        val te = processingEnv.typeUtils.asElement(typeElement.superclass) as TypeElement
        return ElementFilter.methodsIn(te.enclosedElements).find {
            name == it.simpleName && it.parameters.isEmpty()
        }
    }

    fun findEnclosingTypeElement(e: Element): TypeElement {
        val e = if (e !is TypeElement) e.enclosingElement else e
        return TypeElement::class.java.cast(e)
    }


    private fun processClass(element: TypeElement): AnnotatedDeriving {
        val proto: ClassOrPackageDataWrapper = getClassOrPackageDataWrapper(element)
        val typeClasses: List<ClassOrPackageDataWrapper> = element.annotationMirrors.flatMap { am ->
            am.elementValues.entries.filter {
                "typeclasses" == it.key.simpleName.toString()
            }.flatMap {
                val l = it.value.value as List<*>
                l.map {
                    val typeclassName = it.toString().replace(".class", "")
                    val typeClassElement = elementUtils.getTypeElement(typeclassName)
                    getClassOrPackageDataWrapper(typeClassElement)
                }
            }
        }
        return AnnotatedDeriving(element, proto, typeClasses)
    }

}