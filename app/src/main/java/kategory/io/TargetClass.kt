package kategory.io

typealias Foo = Double
typealias Bar = Int

class TargetClass {

    fun x(@implicit a: String) = a
    fun y(f: Foo, @implicit b: Bar) = f + b
    fun z(@implicit b: Bar, f: Foo) = f + b
}
