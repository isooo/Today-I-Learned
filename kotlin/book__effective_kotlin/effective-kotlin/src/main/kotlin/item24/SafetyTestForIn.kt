package item24

fun main() {
    takeDog(Dog())
    takeDog(Puppy())
    takeDog(Hound())



    val puppyBox = Box<Puppy>()
    val dogBox1: Box<Dog> = puppyBox
    dogBox1.set(Hound()) // But I have a place for a Puppy

    val dogBox2 = Box<Dog>()
    val anyBox: Box<Any> = dogBox2
    anyBox.set("Some string") // But I have a place for a Dog
    anyBox.set(42) // But I have a place for a Dog
}

open class Dog
class Puppy: Dog()
class Hound: Dog()

fun takeDog(dog: Dog) {}



class Box<out T> {
    private var value: T? = null

    // illegal in kotlin (예시를 위함... ㅠㅠ)
    fun set(value: @UnsafeVariance T) {
        this.value = value
    }

    fun get(): T = value ?: error("Value not set")
}
