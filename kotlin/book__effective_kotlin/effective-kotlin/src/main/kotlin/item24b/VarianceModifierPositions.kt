package item24b

interface Dog
interface Cutie
data class Puppy(val name: String) : Dog, Cutie
data class Hound(val name: String) : Dog
data class Cat(val name: String) : Cutie

fun fillWithPuppies(list: MutableList<in Puppy>) { // Dog, Cutie 타입이 허용됨
    list.add(Puppy("Jim"))
    list.add(Puppy("Beam"))
}

fun main() {
    val dogs = mutableListOf<Dog>(Hound("Pluto"))
    fillWithPuppies(dogs)
    println(dogs) // [Hound(name=Pluto), Puppy(name=Jim), Puppy(name=Beam)]

    val animals = mutableListOf<Cutie>(Cat("Felix"))
    fillWithPuppies(animals) // [Cat(name=Felix), Puppy(name=Jim), Puppy(name=Beam)]
    println(animals)
}
