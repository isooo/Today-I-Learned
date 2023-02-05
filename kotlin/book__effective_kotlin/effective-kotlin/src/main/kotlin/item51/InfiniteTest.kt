package item51

fun main() {
    generateSequence(1) { it + 1 }
        .map { it * 2 }
        .take(10)
        .forEach { print("$it ") } // 2 4 6 8 10 12 14 16 18 20
}
