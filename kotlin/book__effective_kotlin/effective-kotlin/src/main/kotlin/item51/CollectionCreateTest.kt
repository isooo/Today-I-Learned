package item51

fun main() {
    (1..10)
        .filter { it % 10 == 0 } // new collection
        .map { it * 2 } // new collection
        .sum()
// .sumOf { it * 2 } 로 표현할 수도 있지만, 예시를 위해 위와 같이 분리함

    (1..10).asSequence()
        .filter { it % 10 == 0 }
        .sumOf { it * 2 }
    // no collection created
}
