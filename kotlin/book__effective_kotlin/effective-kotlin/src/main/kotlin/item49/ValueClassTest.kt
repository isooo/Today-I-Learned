package item49

@JvmInline
value class Name(private val value: String) {
    init {
        require(value.isNotEmpty()) { }
    }

    val length: Int
        get() = value.length

    fun greet() {
        println("Hello, $value")
    }
}

fun acceptString(s: String) {}
fun acceptNameInlineClass(n: Name) {}

fun main() {
    val name = Name("ISOO")
    name.greet()
    println(name.greet())
    acceptString("string")
    acceptNameInlineClass(Name("inline"))
    acceptNameInlineClass(name)
}

@JvmInline value class Minutes(val minutes: Int) {}
@JvmInline value class Millis(val milliseconds: Int) {}
interface Timer {
    fun callAfter(timeMillis: Millis, callback: () -> Unit)
}

@JvmInline value class Minutes2(val minutes: Int){
    val millis: Long get() = 1L
}
