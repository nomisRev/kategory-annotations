package kategory.optics

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KotlinFile
import java.io.File

class LensesFileGenerator(
        private val annotatedList: Collection<AnnotatedLens.Element>,
        private val generatedDir: File
) {

    fun generate() = buildLenses(annotatedList).forEach {
        it.writeTo(generatedDir)
    }

    private fun buildLenses(elements: Collection<AnnotatedLens.Element>): List<KotlinFile> = elements.map(this::processElement)
            .map { (name, funs) ->
                funs.fold(KotlinFile.builder("kategory.optics", "optics.kategory.lens.$name").skipJavaLangImports(true), { builder, lensSpec ->
                    builder.addFun(lensSpec)
                }).build()
            }

    private fun processElement(annotatedLens: AnnotatedLens.Element): Pair<String, List<FunSpec>> =
            annotatedLens.type.simpleName.toString().toLowerCase() to annotatedLens.properties.map { variable ->
                val className = annotatedLens.type.simpleName.toString().toLowerCase()
                val variableName = variable.simpleName

                FunSpec.builder("$className${variableName.toString().capitalize()}")
                        .addStatement(
                                """return Lens(
                                   |        get = { $className: %T -> $className.$variableName },
                                   |        set = { $variableName: %T ->
                                   |            { $className: %T ->
                                   |                $className.copy($variableName = $variableName)
                                   |            }
                                   |        }
                                   |)""".trimMargin(), annotatedLens.type, variable, annotatedLens.type)
                        .build()
            }

}
