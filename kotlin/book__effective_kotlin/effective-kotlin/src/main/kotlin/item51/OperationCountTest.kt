package item51

fun main() {
    val iterable = (1..10)
        .filter { print("F$it "); it % 2 == 1 } // F1 F2 F3 F4 F5 F6 F7 F8 F9 F10
        .map { print("M$it "); it * 2 }            // M1 M3 M5 M7 M9
        .find { print("D$it "); it > 5 }          // D2 D6

    println("\niterable:: $iterable") // 6

    val sequence = (1..10).asSequence()
        .filter { print("F$it "); it % 2 == 1 }
        .map { print("M$it "); it * 2 }
        .find { print("D$it "); it > 5 }          // F1 M1 D2 F2 F3 M3 D6
    println("\nsequence:: $sequence") // 6
}
