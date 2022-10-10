package item1

fun main() {
    val names = mutableSetOf<FullName>()
    val person = FullName("ISOO", "CHO")
    names.add(person)
    names.add(FullName("Josh", "Long"))

    println(names) // [FullName(name=ISOO, surname=CHO), FullName(name=Josh, surname=Long)]
    println(person in names) // true

    person.name = "Agnes"
    println(names) // [FullName(name=Agnes, surname=CHO), FullName(name=Josh, surname=Long)]
    println(person in names) // false. 객체를 변경했기 때문에 찾을 수 없음
}

data class FullName(
    var name: String,
    var surname: String
)