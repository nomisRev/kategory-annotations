package kategory.derive

import kategory.common.utils.ClassOrPackageDataWrapper
import kategory.common.utils.extractFullName
import org.jetbrains.kotlin.serialization.ProtoBuf
import java.io.File

sealed class HKArgs {
    object None : HKArgs()
    object First : HKArgs()
    object Unknown : HKArgs()
}

data class FunctionSignature(
        val typeParams: List<String>,
        val name: String,
        val args: List<Pair<String, String>>,
        val retType: String,
        val hkArgs: HKArgs,
        val receiverType: String

) {

    override fun toString(): String {
        val typeParamsS = typeParams.joinToString(prefix = "<`", separator = "`, `", postfix = "`>")
        val argsS = args.map { "${it.first}: ${it.second}" }.joinToString(prefix = "(`", separator = "`, `", postfix = "`)")
        return "override fun $typeParamsS `$name`$argsS: $retType =\n\t${implBody()}"
    }

    fun implBody(): String =
            when(hkArgs) {
                is HKArgs.None -> "${receiverType}.${name}()"
                is HKArgs.First -> "${args[0].first}.ev().${name}(${args.drop(1).map { it.first }.joinToString(", ")})"
                is HKArgs.Unknown -> "${receiverType}.${name}(${args.map { it.first }.joinToString(", ")})"
            }

    companion object {
        fun from(r: AnnotatedDeriving, c: ClassOrPackageDataWrapper, f: ProtoBuf.Function): FunctionSignature =
                FunctionSignature(
                        f.typeParameterList.map { c.nameResolver.getString(it.name) },
                        c.nameResolver.getString(f.name),
                        f.valueParameterList.map { c.nameResolver.getString(it.name) to it.type.extractFullName(c, failOnGeneric = false) },
                        f.returnType.extractFullName(c, failOnGeneric = false),
                        if (f.valueParameterList.isEmpty()) HKArgs.None
                        else if (c.nameResolver.getString(f.getValueParameter(0).type.className).startsWith("kategory/HK")) HKArgs.First
                        else HKArgs.Unknown,
                        r.classElement.qualifiedName.toString()
                )
    }
}

class DerivingFileGenerator(
        private val generatedDir: File,
        private val annotatedList: List<AnnotatedDeriving>
) {

    /**
     * Main entry point for deriving extension generation
     */
    fun generate() {
        annotatedList.forEach { c ->
            c.derivingTypeclasses.forEach { tc ->
                tc.functionList.forEach { func ->
                    val expectedFunction = FunctionSignature.from(c, tc, func)
                    println(expectedFunction)
                }
            }
        }
    }

}