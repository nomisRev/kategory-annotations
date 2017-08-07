# Kategory annotations and processors

Kategory annotations for easier typed FP

### Current status

* @higherkind

When annotating a class as a @higherkind Kategory will generate all the HK machinery needed to emulate HigherKinds in Kotlin.

```kotlin
@higherkind sealed class Option<A> : OptionKind<A>
```

`OptionKind` is a Higher Kind representation of the `Option` type constructor that can be used as target in typeclasses such as 
Functor, Applicative, Monad etc.

We currently support up to 5 type args in derivation. 
In the case above the following is generated:

```kotlin
class OptionHK private constructor()
typealias OptionKind<A> = kategory.HK<OptionHK, A>
fun <A> OptionKind<A>.ev(): Option<A> = this as Option<A>
```

### In progress

@implicit allows implicit value lookup in a global scope currently only for monomorfic non recursive declarations:

```kotlin
@implicit fun provideString() = "1"

fun x(@implicit a: String): String = a
```

Here `x` receives `provideString()` as default Value for `a` by generating an extension function.
Users may call `x()` implicitly requiring the implicit inyected or explicitly `x("yourvalue")`