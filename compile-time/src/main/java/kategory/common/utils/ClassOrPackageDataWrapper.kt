package kategory.common.utils

import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.hasAnnotations
import org.jetbrains.kotlin.serialization.ClassData
import org.jetbrains.kotlin.serialization.PackageData
import org.jetbrains.kotlin.serialization.ProtoBuf.Constructor
import org.jetbrains.kotlin.serialization.ProtoBuf.Function
import org.jetbrains.kotlin.serialization.ProtoBuf.Property
import org.jetbrains.kotlin.serialization.ProtoBuf.TypeParameter
import org.jetbrains.kotlin.serialization.deserialization.NameResolver

sealed class ClassOrPackageDataWrapper {
    abstract val `package`: String
    abstract val nameResolver: NameResolver
    abstract val constructorList: List<Constructor>
    abstract val functionList: List<Function>
    abstract val propertyList: List<Property>
    abstract val typeParameters: List<TypeParameter>
    abstract fun getTypeParameter(typeParameterIndex: Int): TypeParameter?

    class Package(
        override val nameResolver: NameResolver,
        val packageProto: org.jetbrains.kotlin.serialization.ProtoBuf.Package,
        override val `package`: String
    ) : ClassOrPackageDataWrapper() {
        override val constructorList: List<Constructor> get() = emptyList()
        override val functionList: List<Function> get() = packageProto.functionList
        override val propertyList: List<Property> get() = packageProto.propertyList
        override val typeParameters: List<TypeParameter> = emptyList()
        override fun getTypeParameter(typeParameterIndex: Int): TypeParameter? = null
    }

    class Class(
        override val nameResolver: NameResolver,
        val classProto: org.jetbrains.kotlin.serialization.ProtoBuf.Class,
        override val `package`: String
    ) : ClassOrPackageDataWrapper() {
        override val constructorList: List<Constructor> get() = classProto.constructorList
        override val functionList: List<Function> get() = classProto.functionList
        override val propertyList: List<Property> get() = classProto.propertyList
        override val typeParameters: List<TypeParameter> = classProto.typeParameterList
        override fun getTypeParameter(typeParameterIndex: Int): TypeParameter? = classProto.getTypeParameter(typeParameterIndex)
    }
}

fun ClassData.asClassOrPackageDataWrapper(`package`: String) =
    ClassOrPackageDataWrapper.Class(nameResolver, classProto, `package`)

fun PackageData.asClassOrPackageDataWrapper(`package`: String) =
    ClassOrPackageDataWrapper.Package(nameResolver, packageProto, `package`)
