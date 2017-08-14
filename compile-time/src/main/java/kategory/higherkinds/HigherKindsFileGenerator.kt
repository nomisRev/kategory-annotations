package kategory.higherkinds

import java.io.File
import kategory.common.*
import kategory.common.utils.knownError
import org.jetbrains.kotlin.serialization.ProtoBuf
import javax.lang.model.element.Name

typealias HigherKindsExtensionFunction = String

data class HigherKind(
        val `package`: Package,
        val target: AnnotatedHigherKind
        ) {
    val tparams: List<ProtoBuf.TypeParameter> = target.classOrPackageProto.typeParameters
    val kindName: Name = target.classElement.simpleName
    val alias: String = if (tparams.size == 1) "kategory.HK" else "kategory.HK${tparams.size}"
    val typeArgs: List<String> = target.classOrPackageProto.typeParameters.map { target.classOrPackageProto.nameResolver.getString(it.name) }
    val expandedTypeArgs: String = target.classOrPackageProto.typeParameters.joinToString(
            separator = ", ", transform = { target.classOrPackageProto.nameResolver.getString(it.name) })
    val name: String = "${kindName}Kind"
    val markerName = "${kindName}HK"
}

class HigherKindsFileGenerator(
        private val generatedDir: File,
        private val annotatedList: List<AnnotatedHigherKind>
) {

    private val higherKinds: List<HigherKind> = annotatedList.map { HigherKind(it.classOrPackageProto.`package`, it) }

    /**
     * Main entry point for higher kinds extension generation
     */
    fun generate() {
        higherKinds.forEachIndexed { counter, hk ->
            val elementsToGenerate = listOf(genKindMarker(hk), genKindTypeAliases(hk), genEv(hk))
            val source: String = elementsToGenerate.joinToString(prefix = "package ${hk.`package`}\n\n", separator = "\n", suffix = "\n")
            val file = File(generatedDir, higherKindsAnnotationClass.simpleName + "Extensions$counter.kt")
            file.writeText(source)
        }
    }

    private fun genKindTypeAliases(hk: HigherKind): String {
        return if (hk.tparams.isEmpty()) {
            knownError("Class must have at least one type param to derive HigherKinds")
        } else if (hk.tparams.size <= 5) {
            val kindAlias = "typealias ${hk.name}<${hk.expandedTypeArgs}> = ${hk.alias}<${hk.markerName}, ${hk.expandedTypeArgs}>"
            val acc = if (hk.tparams.size == 1) kindAlias else kindAlias + "\n" + genPartiallyAppliedKinds(hk)
            acc
        } else {
            knownError("HigherKinds are currently only supported up to a max of 5 type args")
        }
    }

    private fun genPartiallyAppliedKinds(hk: HigherKind): String {
        val appliedTypeArgs = hk.typeArgs.dropLast(1)
        val expandedAppliedTypeArgs = appliedTypeArgs.joinToString(", ")
        val hkimpl = if (appliedTypeArgs.size == 1) "kategory.HK" else "kategory.HK${appliedTypeArgs.size}"
        return "typealias ${hk.name}Partial<$expandedAppliedTypeArgs> = $hkimpl<${hk.markerName}, $expandedAppliedTypeArgs>"
    }

    private fun genEv(hk: HigherKind): String =
            "@Suppress(\"UNCHECKED_CAST\") inline fun <${hk.expandedTypeArgs}> ${hk.name}<${hk.expandedTypeArgs}>.ev(): ${hk.kindName}<${hk.expandedTypeArgs}> = this as ${hk.kindName}<${hk.expandedTypeArgs}>"

    private fun genKindMarker(hk: HigherKind): String =
            "class ${hk.markerName} private constructor()"

}
