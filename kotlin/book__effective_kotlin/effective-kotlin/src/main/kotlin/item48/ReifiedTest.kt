package item48

/**
fun <T> printTypeName() {
    print(T::class.simpleName) // ERROR: Cannot use 'T' as reified type parameter. Use a class instead.
}
 */

inline fun <reified T> printTypeName() {
    print(T::class.simpleName)
}

inline fun repeat(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}

fun repeatNoinline(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}

fun main() {
    printTypeName<Int>()
    printTypeName<String>()

    var a = 1L
    repeatNoinline(100_000_000) { a += it }
}
