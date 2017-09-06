package kategory.implicits

import kategory.implicit

typealias Foo = Double
typealias Bar = Int

class TargetClass {

    fun x(@implicit a: String): String = a
    fun y(f: Foo, @implicit b: Bar): Double = f + b
    fun z(@implicit b: Bar, f: Foo): Double = f + b
    fun l(@implicit l: List<Int>): List<Int> = l
    fun z(@implicit z: List<String>): List<String> = z

    //inline fun <reified A> la(@implicit l: List<A>): List<A> = l

}
