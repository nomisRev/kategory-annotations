package kategory.implicits

import kategory.implicit

@implicit fun provideString() = "1"

@implicit fun provideIntList(): List<Int> = listOf(1,2,3)

object Wrapper {
    @implicit val providedBar: Bar = 123
}