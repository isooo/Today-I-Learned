package item48

fun main() {
//    repeatNoinline(10) {
//        println(it)
//        return // ERROR: 'return' is not allowed here
//    }

    repeat(10) {
        println(it)
        return
    }
}
