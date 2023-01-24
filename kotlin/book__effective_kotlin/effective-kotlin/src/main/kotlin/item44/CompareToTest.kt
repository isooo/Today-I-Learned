package item44

class User(val name: String, val surname: String)

class User2(val name: String, val surname: String) {
    companion object {
        val DISPLAY_ORDER = compareBy(User2::surname, User2::name)
    }
}

class User3(val name: String, val surname: String) : Comparable<User3> {
    override fun compareTo(other: User3): Int = compareValues(surname, other.surname)
}

fun main() {
    val names = listOf(
        User("GA", "KIM"),
        User("SEA", "PARK"),
    )
    val sorted = names.sortedBy { it.surname }
    val sorted2 = names.sortedWith(compareBy({ it.surname }, { it.name }))

    val names2 = listOf(
        User2("GA", "KIM"),
        User2("SEA", "PARK"),
    )

    val sorted3 = names2.sortedWith(User2.DISPLAY_ORDER)
}
