package kategory.higherkinds

interface OptionInstances : Functor<OptionHK> {

    override fun <A, B> map(fa: OptionKind<A>, f: (A) -> B): Option<B> =
            TODO()

}
