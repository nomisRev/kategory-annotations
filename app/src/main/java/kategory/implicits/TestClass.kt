package kategory.implicits

class Whatever {

    fun test() {

        val target = TargetClass()

        target.x("") // normal
        target.x()   // provided

        target.y(1.0, 0) // normal
        target.y(1.0)    // provided

        target.z(f = 1.0)    // provided

        target.l(listOf(999)) // normal
        target.l() // provided

       // val lint: List<Int> = target.la<Int>()

    }
}
