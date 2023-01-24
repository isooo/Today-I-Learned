package item43

data class FullName(
    var name: String,
    var surname: String
)

fun main() {
    val fullNameSet = mutableSetOf<FullName>()

    val person = FullName("ISOO", "CHO")
    fullNameSet.add(person)

    person.name = "AGNES"
    println("person: $person") // person: FullName(name=AGNES, surname=CHO)
    println(fullNameSet.contains(person)) // false
    println(fullNameSet.first() == person) // true
}
