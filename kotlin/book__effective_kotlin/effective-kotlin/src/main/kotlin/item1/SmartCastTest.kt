package item1

fun main() {
    if (fullName != null) {
//        println(sct.fullName.length) // 컴파일 에러(Smart cast to 'String' is impossible, because 'sct.fullName' is a property that has open or custom getter)
    }
    if (fullName2 != null) {
        println(fullName2.length) // 정상 동작
    }
}

val name: String? = "ISOO"
val surname: String = "CHO"

val fullName: String?
    get() = name?.let { "$it $surname" }

val fullName2: String? = name?.let { "$it $surname" }