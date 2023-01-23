package item36

class CounterSet<T> : HashSet<T>() {
    var elementsAdded: Int = 0
        private set

    override fun add(element: T): Boolean {
        elementsAdded++
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elementsAdded += elements.size
        return super.addAll(elements)
    }
}

fun main() {
    val counterSet = CounterSet<String>()
    counterSet.addAll(listOf("a", "b", "c"))
    println(counterSet.elementsAdded) // 6 (not 3)
}
