package kategory.derive

import kategory.*

interface Functor<F> : Typeclass {

    fun <A, B> map(fa: HK<F, A>, f: (A) -> B): HK<F, B>

    fun <A, B> lift(f: (A) -> B): (HK<F, A>) -> HK<F, B> =
            { fa: HK<F, A> ->
                map(fa, f)
            }
}

interface Applicative<F> : Functor<F>, Typeclass {

    fun <A> pure(a: A): HK<F, A>

    fun <A, B> ap(fa: HK<F, A>, ff: HK<F, (A) -> B>): HK<F, B>

    fun <A, B> product(fa: HK<F, A>, fb: HK<F, B>): HK<F, Pair<A, B>> = ap(fb, map(fa) { a: A -> { b: B -> Pair(a, b) } })

    override fun <A, B> map(fa: HK<F, A>, f: (A) -> B): HK<F, B> = ap(fa, pure(f))

    fun <A, B, Z> map2(fa: HK<F, A>, fb: HK<F, B>, f: (Pair<A, B>) -> Z): HK<F, Z> = map(product(fa, fb), f)

}

interface Monad<F> : Applicative<F>, Typeclass {

    fun <A, B> flatMap(fa: HK<F, A>, f: (A) -> HK<F, B>): HK<F, B>

    override fun <A, B> ap(fa: HK<F, A>, ff: HK<F, (A) -> B>): HK<F, B> = flatMap(ff, { f -> map(fa, f) })

    fun <A> flatten(ffa: HK<F, HK<F, A>>): HK<F, A> = flatMap(ffa, { it })

}

@higherkind
@deriving(Functor::class, Applicative::class, Monad::class)
sealed class Option<out A> : OptionKind<A> {

    fun <B> map(f: (A) -> B): Option<B> = TODO()

    fun <B> flatMap(f: (A) -> OptionKind<B>): Option<B> = TODO()

    companion object {
        fun <A> pure(a: A): Option<A> = TODO()
        fun <A, B> lift(f: (A) -> B): (OptionKind<A>) -> Option<B> = TODO()
    }
}


