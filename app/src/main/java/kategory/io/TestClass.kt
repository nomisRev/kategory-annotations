package kategory.io

class Whatever {

    fun test() {

        val target = TargetClass()

        target.x("") // normal
        target.x()   // provided

        target.y(1.0, 0) // normal
        target.y(1.0)    // provided
    }
}
