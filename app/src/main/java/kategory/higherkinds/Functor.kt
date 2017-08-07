package kategory.higherkinds

import kategory.HK

interface Functor<F> {

    fun <A, B> map(fa: HK<F, A>, f: (A) -> B): HK<F, B>

}