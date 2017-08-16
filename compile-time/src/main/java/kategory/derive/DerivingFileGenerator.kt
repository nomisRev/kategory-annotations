package kategory.derive

import kategory.deriving
import java.io.File
import javax.lang.model.element.AnnotationMirror


class DerivingFileGenerator(
        private val generatedDir: File,
        private val annotatedList: List<AnnotatedDeriving>
)  {

    /**
     * Main entry point for deriving extension generation
     */
    fun generate() {
//        annotatedList.forEachIndexed { index, annotatedDeriving ->
//            val annMirrors: MutableList<out AnnotationMirror> = annotatedDeriving.classOrPackageProto.
//            annMirrors.forEachIndexed { typeclassIndex, typeclassType ->
//                println(typeclassType)
//            }
//        }
    }

}