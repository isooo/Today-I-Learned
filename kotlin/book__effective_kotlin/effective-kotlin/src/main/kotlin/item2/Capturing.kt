package item2

fun getPrimeNumber() {
    var numbers = (2..100).toList()
    val primes = mutableListOf<Int>()
    while (numbers.isNotEmpty()) {
        val prime = numbers.first()
        primes.add(prime)
        numbers = numbers.filter { it % prime != 0 }
    }

    println("prime list:: $primes")
}

fun getPrimeNumberUsingSequence() {
    fun getPrimes(): Sequence<Int> {
        return sequence {
            var numbers = generateSequence(2) { it + 1 }
            while (true) {
                val prime = numbers.first()
                yield(prime)
                numbers = numbers.drop(1)
                    .filter { it % prime != 0 }
            }
        }
    }

    val primes = getPrimes()
    println("prime list:: ${primes.take(10).toList()}") // 2, 3, 5, 7, 11, 13, 17, 19, 23, 29
}

fun getPrimeNumberUsingSequence2() {
    fun getPrimes(): Sequence<Int> {
        return sequence {
            var numbers = generateSequence(2) { it + 1 }
            var prime: Int // do not use like this

            while (true) {
                prime = numbers.first()
                yield(prime)
                numbers = numbers.drop(1)
                    .filter { it % prime != 0 }
            }
        }
    }

    val primes = getPrimes()
    println("prime list:: ${primes.take(10).toList()}") // 2, 3, 5, 6, 7, 8, 9, 10, 11, 12
}