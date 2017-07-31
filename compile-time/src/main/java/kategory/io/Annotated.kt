package kategory.io

import kategory.io.utils.ClassOrPackageDataWrapper
import org.jetbrains.kotlin.serialization.ProtoBuf
import javax.lang.model.element.TypeElement

sealed class Annotated {
    abstract val classElement: TypeElement
    abstract val classOrPackageProto: ClassOrPackageDataWrapper

    sealed class Consumer : Annotated() {
        data class ValueParameter(
            override val classElement: TypeElement,
            override val classOrPackageProto: ClassOrPackageDataWrapper,
            val functionProto: ProtoBuf.Function,
            val valueParameterProto: ProtoBuf.ValueParameter
        ) : Consumer()
    }

    sealed class Provider : Annotated() {
        data class Function(
            override val classElement: TypeElement,
            override val classOrPackageProto: ClassOrPackageDataWrapper,
            val functionProto: ProtoBuf.Function
        ) : Provider()

        data class Property(
            override val classElement: TypeElement,
            override val classOrPackageProto: ClassOrPackageDataWrapper,
            val propertyProto: ProtoBuf.Property
        ) : Provider()
    }
}
