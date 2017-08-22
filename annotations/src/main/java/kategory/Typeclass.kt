package kategory


import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap

/**
 * Marker trait that all Functional typeclasses such as Monad, Functor, etc... must implement to be considered
 * candidates to pair with global instances
 */
interface Typeclass

/**
 * A parametrized type calculated from walking up the interface chain in a Typeclass and finding other relevant
 * typeclasses that should be registered
 */
data class InstanceParametrizedType(val raw: Type, val typeArgs: List<Type>) : ParameterizedType {
    override fun getRawType(): Type = raw

    override fun getOwnerType(): Type? = null

    override fun getActualTypeArguments(): Array<Type> = typeArgs.toTypedArray()
}

/**
 * Auto registers subtypes as a global instance for all the functional typeclass interfaces they implement
 */
open class GlobalInstance<T : Typeclass>(recurseInterfaces: Boolean = true) : TypeLiteral<T>() {

    init {
        if (recurseInterfaces) recurseInterfaces(javaClass)
        else registerSingleTypeClass(javaClass)
    }

    fun registerSingleTypeClass(c: Class<*>) {
        val found = c.interfaces.filter {
            it != Typeclass::class.java && Typeclass::class.java.isAssignableFrom(it)
        }
        found.forEach {
            val instanceType = InstanceParametrizedType(it, listOf(type.actualTypeArguments[0]))
            if (!GlobalInstances.containsKey(instanceType)) {
                val registered = GlobalInstances.putIfAbsent(instanceType, this)
                if (registered == null) {
                    println("registered single typeclass: $instanceType -> $this")
                }
            }
        }
    }

    /**
     * Recursively scan all implemented interfaces and add as global instances all the ones that match a Typeclass
     */
    fun recurseInterfaces(c: Class<*>): Unit = when {
        c.interfaces.isEmpty() -> Unit
        else -> {
            c.interfaces.filter {
                it != Typeclass::class.java && Typeclass::class.java.isAssignableFrom(it)
            }.forEach { i ->
                val instanceType = InstanceParametrizedType(i, listOf(type.actualTypeArguments[0]))
                GlobalInstances.putIfAbsent(instanceType, this)
                recurseInterfaces(i)
            }
        }
    }

}

/**
 * Instrospects the generic type arguments to locate generic interfaces and their type args
 */
open class TypeLiteral<T> {
    val type: ParameterizedType
        get() {
            val t = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
            //force class initialization if it hasn't already happened
            try {
                if (t is ParameterizedType) {
                    val fBound = t.actualTypeArguments[0].typeName
                    if (fBound.contains('$')) {
                        val typeName = t.actualTypeArguments[0].typeName.substringBeforeLast('$')
                        Class.forName(typeName, true, javaClass.classLoader)
                    }
                }
            } finally {
                return t as ParameterizedType
            }
        }
}

inline fun <reified T> typeLiteral(): Type = object : TypeLiteral<T>() {}.type

/**
 * A concurrent hash map of local global instances that may be invoked at runtime as if they were implicitly summoned
 */
object GlobalInstances : ConcurrentHashMap<Type, GlobalInstance<*>>()

/**
 * Obtains a global registered typeclass instance when fast unsafe runtime lookups are desired over passing instances
 * as args to functions. Use with care. This lookup will throw an exception if a typeclass is summoned and can't be found
 * in the GlobalInstances map
 */

data class TypeClassInstanceNotFound(val type: Type)
    : RuntimeException("Thread: ${Thread.currentThread().name} Time: ${System.nanoTime()} : \n$type not found in Global Typeclass Instances registry. " +
        "\nPlease ensure your instances implement `GlobalInstance<$type>` for automatic registration." +
        "\nAlternatively invoke `GlobalInstances.put(typeLiteral<$type>(), instance)` if you wish to register " +
        "\nor override a typeclass manually" +
        "\n Current global instances are : \n\n" +
        GlobalInstances.map { "${it.key} -> ${it.value}" }.joinToString("\n") +
        "\n"
)

@Suppress("UNCHECKED_CAST")
fun <T : Typeclass> instance(t: Type): T {
    if (GlobalInstances.containsKey(t))
        return GlobalInstances.getValue(t) as T
    else {
        val e = TypeClassInstanceNotFound(t)
        println(e.message)
        throw e
    }
}
