package item43

class FullName2(
    var name: String,
    var surname: String
) {
    override fun equals(other: Any?): Boolean =
        other is FullName2
                && other.name == name
                && other.surname == surname
}

fun main() {
    val fullName2Set = mutableSetOf<FullName2>()
    fullName2Set.add(FullName2("ISOO", "CHO"))

    val y = FullName2("ISOO", "CHO")
    println(y in fullName2Set) // false  (FullName2가 hashCode도 재정의하면, true 나옴)
    println(y == fullName2Set.first()) // true
}
