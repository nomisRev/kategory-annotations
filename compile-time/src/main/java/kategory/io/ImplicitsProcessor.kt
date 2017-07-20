package kategory.io

import com.google.auto.service.AutoService
import kategory.io.messager.log
import kategory.io.messager.logW
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(Processor::class)
class ImplicitsProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements
    private lateinit var typeUtils: Types
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private var isFirstProcessingRound: Boolean = false

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
        isFirstProcessingRound = true
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes() = setOf(implicit::class.java.canonicalName)

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val rootElements = roundEnv.rootElements
        val implicitElements = roundEnv.getElementsAnnotatedWith(implicit::class.java)


        messager.log("TEST NOTES!!! ********************************************************")
        messager.logW("TEST WARNINGS!!! ********************************************************")
        messager.log("Implicit classes found: " + implicitElements.size)
        implicitElements.forEach {
            messager.log("ClassName: " + it.simpleName)
        }

        return false
    }
}
