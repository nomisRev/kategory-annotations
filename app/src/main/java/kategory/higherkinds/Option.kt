package kategory.higherkinds

import kategory.higherkind

@higherkind sealed class Option<out A> : OptionKind<A>

@higherkind sealed class Either<L, R> : EitherKind<L, R>