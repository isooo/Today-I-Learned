package item51

fun main() {
    listOf(1, 2, 3)
        .filter { print("F$it "); it % 2 == 1 }    // F1 F2 F3
        .map { print("M$it "); it * 2 }               // M1 M3
        .forEach { print("E$it ") }                        // E2 E6

    sequenceOf(1, 2, 3)
        .filter { print("F$it "); it % 2 == 1 }
        .map { print("M$it "); it * 2 }
        .forEach { print("E$it ") }                        // F1 M1 E2 F2 F3 M3 E6
}
