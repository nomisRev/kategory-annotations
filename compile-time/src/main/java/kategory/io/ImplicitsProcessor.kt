package kategory.io

import kategory.io.generation.FileGenerator
import kategory.io.messager.logMW
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class ImplicitsProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes() = setOf(implicit::class.java.canonicalName)

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        processingEnv.messager.logMW("Implicits processor running...")
        val fileGenerator = FileGenerator()
        roundEnv.getElementsAnnotatedWith(implicit::class.java)
                .forEach {
                    processingEnv.messager.logMW("Implicit annotated class found: " + it.simpleName)

                    val kaptGeneratedDir = File(processingEnv.options["kapt.kotlin.generated"])
                    if (!kaptGeneratedDir.parentFile.exists()) {
                        kaptGeneratedDir.parentFile.mkdirs()
                    }

                    val elementPackage = processingEnv.elementUtils.getPackageOf(it).qualifiedName
                    val kotlinFile = fileGenerator.createKotlinFile(elementPackage)
                    kotlinFile.writeTo(kaptGeneratedDir)
                }
        return false
    }
}
