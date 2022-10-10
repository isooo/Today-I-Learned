package item1

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

suspend fun main() {
    useCoroutine()
    useSynchronized()
}

private suspend fun useCoroutine() {
    var num = 0

    coroutineScope {
        for (i in 1..1000) {
            launch {
                delay(10)
                num++
            }
        }
    }

    println("num:: $num") // 실행할 때마다 다른 숫자가 나옴
}

private fun useSynchronized() {
    val lock = Any()
    var num = 0
    for (i in 1..1000) {
        thread {
            Thread.sleep(10)
            synchronized(lock) {
                num++
            }
        }
    }
    Thread.sleep(1000)
    println("num:: $num") // 항상 1000이 나옴
}