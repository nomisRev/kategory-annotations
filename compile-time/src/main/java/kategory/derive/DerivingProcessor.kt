package kategory.derive

import com.google.auto.service.AutoService
import kategory.Typeclass
import kategory.common.utils.AbstractProcessor
import kategory.common.utils.knownError
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.ElementFilter
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror


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

    private fun processClass(element: TypeElement): AnnotatedDeriving {
        val proto = getClassOrPackageDataWrapper(element)
        val derivingTypeclasses =
                element.annotationMirrors.flatMap { m ->
                    m.elementValues.entries.filter {
                        it.key.simpleName.toString() == "typeclasses"
                    }.flatMap {
                        it.value ???
                    }
                }


        return AnnotatedDeriving(element, proto, derivingTypeclasses)
    }

}