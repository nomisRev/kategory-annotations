package kategory.higherkinds

import kategory.common.Package
import kategory.common.utils.knownError
import org.jetbrains.kotlin.serialization.ProtoBuf
import org.jetbrains.kotlin.serialization.ProtoBuf.TypeParameter.Variance
import java.io.File
import javax.lang.model.element.Name

typealias HigherKindsExtensionFunction = String

data class HigherKind(
        val `package`: Package,
        val target: AnnotatedHigherKind
) {
    val tparams: List<ProtoBuf.TypeParameter> = target.classOrPackageProto.typeParameters
    val kindName: Name = target.classElement.simpleName
    val alias: String = if (tparams.size == 1) "kategory.HK" else "kategory.HK${tparams.size}"
    val aliasJ: String = if (tparams.size == 1) "kategory.HK_J" else "kategory.HK${tparams.size}_J"
    val typeArgs: List<String> = target.classOrPackageProto.typeParameters.map { target.classOrPackageProto.nameResolver.getString(it.name) }
    val expandedTypeArgs: String = target.classOrPackageProto.typeParameters.joinToString(
            separator = ", ", transform = { target.classOrPackageProto.nameResolver.getString(it.name) })
    val name: String = "${kindName}Kind"
    val markerName = "${kindName}HK"
    val variance: Variance = target.classOrPackageProto.typeParameters.map { it.variance }.reduce { acc: Variance?, value: Variance? -> addVariance(acc, value) }

    private fun addVariance(acc: Variance?, value: Variance?): Variance =
            when (acc) {
                null -> knownError("In class $kindName: Variance not found for some generic parameter")
                Variance.INV -> acc
                Variance.IN -> when (value) {
                    null -> knownError("In class $kindName: Variance not found for some generic parameter")
                    Variance.INV -> value
                    Variance.IN -> Variance.IN
                    Variance.OUT -> Variance.INV
                }
                Variance.OUT -> when (value) {
                    null -> knownError("In class $kindName: Variance not found for some generic parameter")
                    Variance.INV -> value
                    Variance.IN -> Variance.INV
                    Variance.OUT -> Variance.OUT
                }
            }
}

class HigherKindsFileGenerator(
        private val generatedDir: File,
        annotatedList: List<AnnotatedHigherKind>
) {

    private val higherKinds: List<HigherKind> = annotatedList.map { HigherKind(it.classOrPackageProto.`package`, it) }

    /**
     * Main entry point for higher kinds extension generation
     */
    fun generate() {
        higherKinds.forEachIndexed { counter, hk ->
            val elementsToGenerate = listOf(genKindMarker(hk), genKindTypeAliases(hk), genEvidence(hk))
            val source: String = elementsToGenerate.joinToString(prefix = "package ${hk.`package`}\n\n", separator = "\n", postfix = "\n")
            val file = File(generatedDir, higherKindsAnnotationClass.simpleName + "Extensions$counter.kt")
            file.writeText(source)
        }
    }

    private fun genKindMarker(hk: HigherKind): String =
            "class ${hk.markerName} private constructor()"

    private fun genKindTypeAliases(hk: HigherKind): String {
        return if (hk.tparams.isEmpty()) {
            knownError("Class must have at least one type param to derive HigherKinds")
        } else if (hk.tparams.size <= 5) {
            val kindAlias = "typealias ${hk.name}<${hk.expandedTypeArgs}> = ${hk.alias}<${hk.markerName}, ${hk.expandedTypeArgs}>"
            val kindJAlias = if (hk.variance != Variance.INV) kindAlias else kindAlias + "\ntypealias ${hk.name}J<${hk.expandedTypeArgs}> = ${hk.aliasJ}<${hk.markerName}, ${hk.expandedTypeArgs}>"
            val acc = if (hk.tparams.size == 1) kindJAlias else kindJAlias + "\n" + genPartiallyAppliedKinds(hk)
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

    private fun genEvidence(hk: HigherKind): String =
            "@Suppress(\"UNCHECKED_CAST\") inline fun <${hk.expandedTypeArgs}> ${hk.name}<${hk.expandedTypeArgs}>.ev(): ${hk.kindName}<${hk.expandedTypeArgs}> = this as ${hk.kindName}<${hk.expandedTypeArgs}>"

}
