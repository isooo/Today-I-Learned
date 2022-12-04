package item24

fun printProcessedNumber(transition: (Int) -> Any) {
    println(transition(42))
}

fun main() {
    val intToDouble: (Int) -> Number = { it.toDouble() }
    val numberAsText: (Number) -> Any = { it.toShort() }
    val identity: (Number) -> Number = { it.toInt() }
    val numberToInt: (Number) -> Int = { it.toInt() }
    val numberHash: (Any) -> Number = { it.hashCode() }

    printProcessedNumber(intToDouble)
    printProcessedNumber(numberAsText)
    printProcessedNumber(identity)
    printProcessedNumber(numberToInt)
    printProcessedNumber(numberHash)
}
