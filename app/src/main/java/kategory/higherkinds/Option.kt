package kategory.higherkinds

import kategory.higherkind

@higherkind sealed class Option<out A> : OptionKind<A>

@higherkind sealed class Either<L, R> : EitherKind<L, R>

@higherkind sealed class StateT<F, S, A> : StateTKind<F, S, A>

typealias X<L> = EitherKindPartiallyApplied<L>

typealias Z<F, S> = StateTKindPartiallyApplied<F, S>

