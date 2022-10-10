package item5

/*
fun changeDress(person: Person) {
    require(person.outfit is Dress)
    val dress: Dress = person.outfit // require로 검사 후, person.outfit이 final이라면 스마트 캐스트가 적용됨
}
*/

fun sendEmail(person: Person, text: String) {
    val email: String = person.email ?: return
}

fun sendEmail2(person: Person, text: String) {
    val email: String = person.email ?: run {
        println("email not sent, no email address")
        return
    }
}

data class Person(
    val name: String,
    val email: String
)