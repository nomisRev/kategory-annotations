package kategory

import io.kindedj.HK as HK1_J

interface HK<out F, out A>

typealias HK2<F, A, B> = HK<HK<F, A>, B>

typealias HK3<F, A, B, C> = HK<HK2<F, A, B>, C>

typealias HK4<F, A, B, C, D> = HK<HK3<F, A, B, C>, D>

typealias HK5<F, A, B, C, D, E> = HK<HK4<F, A, B, C, D>, E>

typealias HK_J<F, A> = HK1_J<F, A>

typealias HK2_J<F, A, B> = HK1_J<HK_J<F, A>, B>

typealias HK3_J<F, A, B, C> = HK1_J<HK2<F, A, B>, C>

typealias HK4_J<F, A, B, C, D> = HK1_J<HK3<F, A, B, C>, D>

typealias HK5_J<F, A, B, C, D, E> = HK1_J<HK4<F, A, B, C, D>, E>
