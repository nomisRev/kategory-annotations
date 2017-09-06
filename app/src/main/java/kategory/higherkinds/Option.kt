package kategory.higherkinds

import kategory.higherkind

@higherkind sealed class Either<L, R> : EitherKind<L, R>

@higherkind sealed class StateT<F, S, A> : StateTKind<F, S, A>

typealias X<L> = EitherKindPartial<L>

typealias Z<F, S> = StateTKindPartial<F, S>

