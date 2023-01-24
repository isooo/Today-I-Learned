package item42

class NameA(val name: String)
data class NameB(val name: String)

fun main() {
    val name1 = NameA("isoo")
    val name2 = NameA("isoo")
    val name1Ref = name1

    println(name1 == name1) // true
    println(name1 == name2) // false
    println(name1 == name1Ref) //true

    println(name1 === name1) // true
    println(name1 === name2) // false
    println(name1 === name1Ref) // true

    val name3 = NameB("isoo")
    val name4 = NameB("isoo")
    val name3Ref = name3

    println(name3 == name3) // true
    println(name3 == name4) // true
    println(name3 == name3Ref) // true

    println(name3 === name3) // true
    println(name3 === name4) // false
    println(name3 === name3Ref) // true

    name3.copy()
}
