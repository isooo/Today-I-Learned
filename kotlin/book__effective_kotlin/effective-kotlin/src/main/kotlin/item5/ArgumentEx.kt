package item5

fun factorial(n: Int): Long {
    require(n >= 0) // IllegalArgumentException을 던짐
    return if (n <= 1) 1 else factorial(n - 1) * n
}

fun factorial2(n: Int): Long {
    require(n >= 0) {"Cannot calculate factorial of $n"} // exception 메시지도 함께 정의할 수 있음
    return if (n <= 1) 1 else factorial(n - 1) * n
}

fun sendEmail(user: User, message: String) {
    requireNotNull(user.email)
//    require(isValidEmail(user.email))

    // ...
}

data class User(
    val name: String,
    val email: String
)