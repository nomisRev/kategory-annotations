package kategory.io

@implicit fun provideString() = "hello"

object Wrapper {
    @implicit val providedBar: Bar = 123
}