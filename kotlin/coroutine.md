# 코루틴<sub>coroutine</sub>
코루틴은
- an instance of suspendable computation
- thread와 비슷한 개념이지만, 코루틴은 특정 스레드에서만 실행되는 것은 아니다. 한 스레드에서 suspend됐을 때 다른 스레드에서 resume될 수 있다. 
- light-weight

## 간단한 예시  
```kotlin
fun main() = runBlocking { // this: CoroutineScope
    launch { // launch a new coroutine and continue
        delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
        println("World!") // print after delay
    }
    println("Hello") // main coroutine continues while a previous one is delayed
}
```
```
Hello
World!
```
- `launch`
    - 코루틴 빌더
    - 현재 스레드를 blocking하지 않고, `Job`을 return함
    - 블럭 이후 코드를 독립적으로 수행하면서 동시에 새로운 코루틴을 시작한다
- `delay`
    - suspending function
    - 특정 시간동안 코루틴을 suspend한다
        - 코루틴이 suspend됐다는건 **코루틴을 실행하던 스레드가 block됐다는 의미가 아니다. 다른 코루틴이 해당 스레드에서 코드를 실행될 수 있다는 것**!
- `runBlocking`
    - 코루틴 빌더
    - <sub>위 코드에서 `main` 함수 내</sub> '코루틴이 아닌 코드'와 '`runBlocking { ... }` 블럭 내의 <sub>코루틴</sub> 코드'를 연결하는 빌더
    - 위 코드에서 `runBlocking`을 제거하면 `launch`에서 `Unresolved reference: launch` 컴파일 에러가 발생한다. `launch`는 `CoroutineScope`에서만 선언할 수 있기 때문이다
    - `runBlocking`은 `runBlocking { ... }` 블럭 내 모든 코루틴이 실행 완료될 때까지 현재 코루틴이 실행되고 있는 스레드를 점유한다. block되어버림  
        - 위 예시에선 메인 스레드가 block됨
        - 이런 식으로 스레드를 점유하는 건 매우 비효율적이기 때문에 실제 서비스에서 이런 식으로 사용하지 말어라..  

## Structured concurrency
코루틴은 Structured concurrency 원칙을 따른다 
- 새로운 코루틴은 오직 <sub>코루틴의 라이프타임 범위를 지정하는</sub> CoroutineScope에서만 시작할 수 있다
    - 앞선 예시 기준으론 `runBlocking { ... }`를 의미
- outer scope는 이 하위 코루틴들이 실행 완료되어야 완료할 수 있다
- 실행하면서 발생한 error는 유실되지 않고 report된다

## Scope builder
[`coroutineScope`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/coroutine-scope.html) 빌더를 이용해 코루틴 스코프를 선언할 수 있다
- 코루틴 스코프는 자식 코드들이 완료되기 전까진 완료되지 않는다
- `runBlocking`과 `coroutineScope`은 둘 다 자신의 body 내 코드와 자식이 완료될 때까지 기다린다는 공통점이 있지만, 이들의 차이점은
    - `runBlocking`은
        - 현재 스레드를 점유하며 block하고 있음
    - `coroutineScope`은
        - 기반 스레드가 다른 작업을 할 수 있게 suspend/release 된다
    - ==> 이 차이점때문에 `runBlocking`는 regular function<sub>일반 다른 함수처럼 block하면서 동작한다는 의미</sub>, `coroutineScope`은 suspending function이 된다

## Scope builder and concurrency
`coroutineScope` 빌더는 suspend function 내에서 여러 동시 작업을 하도록 사용될 수 있다
- 예시
    ```
    // Sequentially executes doWorld followed by "Done"
    fun main() = runBlocking {
        doWorld()
        println("Done")
    }

    // Concurrently executes both sections
    suspend fun doWorld() = coroutineScope { // this: CoroutineScope
        launch {
            delay(2000L)
            println("World 2")
        }
        launch {
            delay(1000L)
            println("World 1")
        }
        println("Hello")
    }
    ```
    ```
    Hello
    World 1
    World 2
    Done
    ```
    - 두 `launch` 블럭은 동시에 실행된다

## An explicit job
- `launch` 코루틴 빌더는 `Job`을 리턴한다. 이 `Job`은 코루틴 실행을 핸들링하거나 완료를 명시적으로 기다리는데 사용할 수 있다
    ```kotlin
    fun main() = runBlocking {
        val job = launch { // launch a new coroutine and keep a reference to its Job
            delay(1000L)
            println("World!")
        }
        println("Hello(${job.isActive})")
        job.join() // wait until child coroutine completes
        println("Done(${job.isActive})")
    }
    ```
    ```
    Hello(true)
    World!
    Done(false)
    ```

## 참고
- [kotlinlang - Coroutines](https://kotlinlang.org/docs/coroutines-overview.html#tutorials)
