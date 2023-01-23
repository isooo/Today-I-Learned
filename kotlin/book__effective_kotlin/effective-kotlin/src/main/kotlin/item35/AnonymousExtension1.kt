package item35

fun main() {
    val x = 3

    val y1 = x.myPlus1("a")
    val y2 = x.myPlus2("a")
    val y3 = x.myPlus3("a")
    val y4 = x.myPlus4("a")

    // 위와 같이 일반적인 extension function 사용하듯이 사용할 수도 있고,
    // invoke 메서드를 호출해서 사용할 수도 있고
    val z1 = myPlus2.invoke(x, "a")
    // non-extension function 처럼 사용할 수도 있다
    val r1 = myPlus2(x, "a")
}

fun Int.myPlus1(other: String) = this.toString() + other
val myPlus2 = fun Int.(other: String) = this.toString() + other
val myPlus3: Int.(String) -> String = fun Int.(other: String) = this.toString() + other
val myPlus4: Int.(String) -> String = { this.toString() + it }

