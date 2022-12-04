package item24

fun main() {
    val list = mutableListOf("a", "b", "c")
//    append(list) // compile error: type mismatch

    val a = listOf(1, 2, 3)
    execute(a)
}

fun append(list: MutableList<Any>) {
    list.add(42)
}

fun execute(list: List<Any?>) {
    // do somthing
}
