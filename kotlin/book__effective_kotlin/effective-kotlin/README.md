# [이펙티브 코틀린](https://product.kyobobook.co.kr/detail/S000001033129)

## :small_blue_diamond: 아이템 1: 가변성을 제한하라
- `val`는 읽기 전용 프로퍼티지만, 변경할 수 없음<sub>immutable</sub>을 의미하는 것은 아니다
- `val`는 `custom getter`나 델리게이트<sub>delegate</sub>로 정의할 수 있다
    - e.g. `val fullName`프로퍼티에게 `custom getter`를 정의한다면 스마트 캐스트<sub>smart cast</sub> 기능을 이용할 수 없다. getter를 이용해 값이 사용되는 시점의 `name` 프로퍼티의 값에 따라 다른 결과가 나올 수 있기 때문이다
    - e.g. `fullName2`처럼 '지역 변수가 아닌 프로퍼티'<sub>non-local property</sub>가 final이고, `custom getter`를 정의하지 않았다면 스마트 캐스트를 이용할 수 있다
- 읽기 전용 컬렉션 일부는 내부적으로 mutable한 컬렉션을 쓰고 있지만, 외부적으론 immutable하게 보이게 만들어 안정적인 형태로 제공하고자 한다
    - e.g. 
        ```kotlin
        val a = listOf(1, 2, 3) // a는 읽기 전용 컬렉션
        ```
        이때 `listOf(1, 2, 3).map { }`을 했을 때 이 `map`은 내부적으로 `ArrayList`를 사용하고 있다. `ArrayList`는 mutable한 컬렉션! 
    - 내부까지 immutable하게 만들지 않은 건 좀 더 자유로운 설계를 허용하기 위함인 듯..?
- 일부로 컬렉션을 다운캐스팅해서 안전하지 못하고 예측하기 어려운 코드를 만들진 말자
    ```kotlin
    val a = listOf(1, 2)
    if (a is MutableList) { // Do not use like this
        a.add(3)
    }
    ```
    - 만약 위와 같이 읽기 전용을 mutable 컬렉션으로 변경해야 한다면,   
    `val c = a.toMutableList()`이렇게 copy를 통해 새로운 컬렉션을 만드는 방법을 사용하자

### immutable 객체를 사용했을 때 장점
- immutable 객체를 공유해도 충돌이 발생하지 않으므로, 병렬 처리를 안전하게 할 수 있다
- immutable 객체에 대한 참조는 변경되지 않으므로, 쉽게 캐시할 수 있다
- immutable 객체는 방어적 복사본<sub>defensive copy</sub>을 만들지 않아도 된다
    - 깊은 복사가 필요없음
    - immutable 객체는 쉽게 다른 객체를 만들 수 있다
- immutable 객체는 `Set`이나 `Map`의 key로 사용할 수 있다
    - `item1.ImmutableCollectionTest.kt` 참고

### 다른 종류의 변경 가능 지점
- 아래 `a`와 `b`는 둘 다 변경 가능 지점<sub>mutating point</sub>이 있지만, 위치가 다르다
    ```kotlin
    val a = mutableListOf<Int>()
    var b = listOf<Int>()

    a += 1 // a.add(1)
    b += 1 // b.plus(1)
    ```
    - `a`는 리스트 구현 내부에 변경 가능 지점이 있고, `b`는 프로퍼티 자체가 변경 가능하다
        - 굳이 따지자면 멀티스레드에선 `b`가 안정적이라 볼 수 있다(`a`는 내부적으로 동기화가 잘 되어있는지 확실치 않으므로)
- 변이 지점을 잘 파악해두고, 불필요한 변이 지점은 만들지 말자

### 변경 가능 지점 노출하지 말기
- mutable 객체는 외부에 노출하지 말자
    - `item1.MutatingPoint.kt` 참고

## :small_blue_diamond: 아이템 2: 변수의 스코프를 최소화하라
- 프로퍼티보단 지역 변수를 사용하자
- 최대한 좁은 스코프를 갖게 변수를 사용하자
    - 특정 블록 내에서만 사용된다면, 그 블록 내에서 변수를 선언하자
    - 좁은 스코프에 걸쳐 있을수록 값 변경 등을 추적하기가 쉽기 때문
- 여러 프로퍼티를 한꺼번에 설정해야 하는 경우엔 [구조분해 선언<sub>destructuring declaration</sub>](https://github.com/isooo/Today-I-Learned/blob/564dd09dd47852b5c2998cfbf5c76dd69555af34/kotlin/book__kotlin_in_action/kotlin_in_action__CH7__Operator_overloading_and_other_conventions.md#74-%EA%B5%AC%EC%A1%B0-%EB%B6%84%ED%95%B4-%EC%84%A0%EC%96%B8%EA%B3%BC-component-%ED%95%A8%EC%88%98)을 활용하자
    - `item2.DestructuringDeclarationEx.kt` 참고 

### 캡쳐링
- 람다에선 변수를 캡처한다는 것을 기억하자
    - `item2.Capturing.kt` 참고

## :small_blue_diamond: 아이템 3: 최대한 플랫폼 타입을 사용하지 말라
> - 플랫폼 타입<sub>platform type</sub>
>     - 코틀린이 아닌 다른 프로그래밍 언어에서 전달되어 null 여부를 알 수 없는 타입

- 플랫폼 타입은 안전하지 않으니 최대한 빠르게 제거하자 
    - e.g. 아래와 같은 java코드가 있을 때
        ```java
        // java
        public class JavaClass {
            public String getValue() {
                return null;
            }
        }
        ```
    - 위 자바 코드를 코틀린에서 사용하고자 한다면, 아래와 같은 상황이 발생할 수 있다
        ```kotlin
        fun statedType() {
            val value: String = JavaClass().value // NPE
            println(value.length)
        }

        fun platformType() {
            val value = JavaClass().value
            println(value.length) // NPE
        }
        ```
        - 문제는 NPE 발생 위치. `statedType()`의 경우가 훨씬 에러를 파악하기 쉽다 

## :small_blue_diamond: 아이템 4: inferred 타입으로 리턴하지 말라
> 코틀린의 타입 추론<sub>type inference</sub>은 유용한 기능이지만...  

- 타입을 확실하게 지정해야 하는 경우엔 명시적으로 타입을 지정하자
- 특별한 이유나 확실한 확인 없이는 타입을 제거하지 말자

## :small_blue_diamond: 아이템 5: 예외를 활용해 코드에 제한을 걸어라
- 코틀린에는 코드의 동작에 제한을 거는 기능이 제공된다
    - 예상치 못한 동작을 방지할 수 있음
    - 코드를 어느 정도 자체적으로 검사할 수 있어서, 관련된 단위 테스트를 줄일 수 있다는 장점
    - 스마트 캐스트를 활용하여, 캐스트를 적게할 수 있음

### `require` 블록
> argument를 제한할 수 있다

- 함수를 정의할 때 타입 시스템을 활용해 argument에 제한을 걸 수 있다
- 제한을 만족하지 못할 경우, 예외를 throw한다
    - `throw IllegalArgumentException`
- `item5.ArgumentEx.kt` 참고

### `check` 블록
> 상태과 관련된 동작을 제한할 수 있다

- 구체적 조건을 만족할 때만 함수를 사용할 수 있음
    - e.g. 객체가 초기화되어 있을 때만 처리하고자 할 때, 사용자가 로그인했을 때만 처리하고자 할 때 등등
- `require`와 비슷하지만, 지정된 예측을 만족하지 못하면 `throw IllegalStateException`한다는 점이 다름
- `item5.StatusEx.kt` 참고

### `assert` 블록
> 어떤 것이 true인지 확인할 수 있다. (테스트 모드에서만 작동함)

- 구현한 내용이 확실한 지 확인하고자 할 때 사용
- 주로 단위 테스트 구현의 정확성을 확인할 때 사용됨
    - e.g. `assertEquals()`...
- 만족하지 않는다고 해서 예외를 던지지는 않음

### `return` 또는 `throw`와 함께 활용하는 elvis 연산자
- throw나 return을 엘비스 연산자 오른쪽에 두는 방식으로 구현하면, 읽기 쉽고 유연한 코드를 짤 수 있다
- `item5.SmartCastingEx.kt` 참고

## :small_blue_diamond: Item 6: Prefer standard errors to custom ones
커스텀한 예외 대신 표준 예외를 사용하자.  

- 이미 잘 알려진 표준 라이브러리의 예외를 사용하면, 사용자들이 API를 더 쉽게 이해할 수 있다. 

## :small_blue_diamond: Item 7: Prefer a nullable or Result result type when the lack of a result is possible
결과가 없는 경우, 반환 타입을 nullable하게 하거나 `Result` 타입을 사용하자.    

- 함수 내에서 특정 상황으로 인해 원하는 반환을 생성하지 못할 수 있다. 이 상황에서는 null이나 `Result.failure`를 리턴하거나, 예외를 던지는 방식으로 처리시킬 수 있다
    - 예외 상황 예
        - 인터넷 연결에 문제가 있어 데이터를 가져오지 못할 때
        - 배열에서 요소를 가져오려고 하는데 그런 요소가 존재하지 않을 때
        - 특정 타입으로 변환하려고 하는데, 예측한 형식이 아니어서 변환하지 못할 때
    - 던지는 방식의 차이
        - 예외
            - 예외는 표준적인 방법이 아니므로 권장하지 않는다. 이 함수가 던질 예외를 다른 개발자가 캐치하지 못할 수 있다<sub>코틀린은 checked exception을 지원하지 않으니</sub>
        - null이나 `Result.failure`
            - 예외가 발생할 가능성이 있는 곳에 위치하는데 적합한 형태다. 명확하며 효율적이다
                - 만약 예외가 예측되지 않을 때는 예외를 throw하도록 하자

- 예시
    ```kotlin
    inline fun <reified T> String.readObjectOrNull(): T? {
        if (incorrectoSign) {
            return null
        }

        return result
    }

    inline fun <reified T> String.readObject(): Result<T> {
        if (incorrectoSign) {
            return Result.failure(JsonParsingException())
        }

        return Result.success(result)
    }

    class JsonParsingException : Exception()
    ```
    - 위와 같이 구현하면 에러를 핸들링을 쉽게 할 수 있다. 
        - `readObjectOrNull`를 이용한다면, 다음과 같이 엘비스 연산자를 이용해 null-safety하게 결과값을 받을 수 있다
            ```kotlin
            val age = userText.readObjectOrNull<Person>()?.age ?: -1
            ```
        - `readObject`를 이용한다면, `Result`클래스를 사용해, 경우에 따른 결과값을 핸들링할 수 있다
            ```kotlin
            userText.readObject<Person>()
                .onSuccess { showPersonAge(it) }
                .onFailure { showError(it) }
            ```
        - ==> 이런 식으로 <sub>try-catch보다</sub> 효율적이고 쉽게 오류를 처리할 수 있다  
            - 개발하면서 예외 처리를 빠트리거나, 예상 못 한 상황에서 발생하여 프로그램 흐름에 방해를 끼칠 수도 있다. 하지만 이렇게 명시적으로 처리하면 흐름을 방해하지 않을 수 있다!
- nullable 방식과 `Result` 방식의 차이는, 후자는 fail일 때 추가 정보를 전달해야 한다는 것 <sub>fail에 대한 처리가 필요하다면 `Result`를, 그렇지 않다면 nullable 방식을 선택하면 된다</sub>
    - `Result`는 결과 처리에 대한 여러 함수를 제공하고 있다 
        - `exceptionOrNull`, `getOrThrow`, `getOrElse`, `fold`, `map`, `mapCatching`...

> 함수는 2가지 유형으로 정의할 수 있다. `List`는 2가지 유형의 api를 모두 제공하고 있다.  
> - 오류가 발생할 수 있다고 예상하거나
>   - e.g. `get`
>       - 특정 요소가 특정 위치에 존재할 것이라 예상하고 `get`을 사용하는 경우. 만약 해당 위치에 없다면 그 함수는 `IndexOutOfBoundException`을 던진다
> - 오류가 예기치 못한 상황으로 간주하거나
>   - e.g. `getOrNull`
>       - 범위를 벗어난 요소를 요청할 수 있다고 가정했을 때, null을 반환받을 수 있다 
>       - `getOrDefault`로도 처리할 수 있지만, `getOrNull`와 엘비스 연산자로 대체할 수도 있다
> 
> 개발자가 특정 요소가 특정 위치에 있다는 걸 확신한다면 굳이 nullable에 대한 처리를 강제할 필요는 없지만,     
> 조금이라도 의심된다면 `getOrNull`을 이용해 만약의 사태에 대비하는 걸 권장한다  

## :small_blue_diamond: Item 8: Handle nulls properly
null을 적절하게 처리하자.  

- null은 값이 없다는 것을 의미한다. 
- 만약 함수가 null을 반환하면, 함수의 역할에 따라 그 의미는 달라질 수 있다
    - e.g. `String.toIntOrNull()`와 `Iterable<T>.firstOrNull(() -> Boolean)`, 두 함수가 null을 반환했을 때 그 의미는 다르게 해석할 수 있다
- 어떤 경우라도 null을 처리하긴 해야하므로, 어떻게 하면 안전하게 처리할 수 있는지 알아보자

#### :heavy_check_mark: Handling nulls safely
- 안전하게 호출하거나, 스마트 캐스팅<sub>smart casting</sub>하거나
    - e.g.
        - `printer?.print()`
        - `if (printer != null) printer.print()`
    - 여기에 덧붙여 엘비스 연산자를 이용해 null일때 상황을 처리할 수 있다
        - e.g. 
            ```kotlin
            val printerName1 = printer?.name ?: "Unnamed" 
            val printerName2 = printer?.name ?: return 
            val printerName3 = printer?.name ?: throw Error("Printer must be named")
            ```            
            > - 우측에 throw를 넣을 수 있는 이유는 `Nothing`이 모든 타입의 하위 타입이기에 가능! 
- `List`나 `String`등 많은 오브젝트가 null 상황을 체크할 수 있는 함수를 제공하니 적절하게 이용하자
    - e.g. `isNullOrBlank`

> 참고: [Marcin Moskala: The beauty of Kotlin typing system](https://blog.kotlin-academy.com/the-beauty-of-kotlin-typing-system-7a2804fe6cf0)

#### :heavy_check_mark: Defensive and offensive programming
- 방어적 프로그래밍
    - 앞서 살펴본 '`printer`가 null일땐 이후 로직을 실행하지 않음' 방식은 방어적 프로그래밍을 한 것이다
    - 이는 코드의 안정성을 높이는 방식 중 하나
    - 모든 가능한 상황을 올바르게 처리할 수 있을 때 사용할 수 있는 베스트 프렉티스
        - 만약 상황을 안전하게 처리하는게 불가능하다면, 공격적 프로그래밍을 해야한다
- 공격적 프로그래밍
    - 예상치 못한 상황이 발생했을 땐, 개발자가 인지할 수 있도록 알리고 수정하도록 해야 한다
        - `item 5`에서 언급한 것처럼 구현하는 것도 하나의 방법!

#### :heavy_check_mark: Throw an error
- `printer`가 null이 아닐 땐 `print`를 수행하겠지만, null이라면 개발자는 눈치채지 못한 채 흘러가버릴 수도 있다. 이런 케이스는 찾기 힘든 오류로 번질 수도 있으니, 충족되지 못한 상황이라면 예외를 던지는 게 더 나을 수도 있다.    
    - e.g. `throw`하거나, non-null assertion<sub>`!!`</sub>을 쓰거나, `requireNotNull`나 `checkNotNull` 등 다른 error-throwing functions를 사용하거나...  
    ```kotlin
    requireNotNull(user.name)
    val context = checkNotNull(context)
    val networkService = getNetworkService(context) ?: throw NoInternetConnection()
    networkService.getData { data, userData -> show(data!!, userData!!) }
    ```
  
#### :heavy_check_mark: The problems with the non-null assertion(`!!`)
- e.g.
    ```kotlin
    fun largestOf(a: Int, b: Int, c: Int, d: Int): Int = listOf(a, b, c, d).maxOrNull()!!
    ```
    - 이 함수의 문제점은 `largestOf`의 인자로 주어지는 값들이 empty하지 않기 때문에 `maxOrNull`의 결과값이 `!!`라는 걸 알고 있어야 한다는 것이다. 만약 누군가 이 함수를 이용해 아래와 같은 함수를 만들 경우
        ```kotlin
        fun largestOf(vararg nums: Int): Int = nums.maxOrNull()!!
        ``` 
        이를 `largestOf()`로 호출할 수 있게 되고, 이 코드는 NPE를 발생시킨다.  
- e.g. 
    ```kotlin
    class UserControllerTest {
        private var dao: UserDao? = null
        private var controller: UserController? = null

        @BeforeEach
        fun init() {
            dao = mockk()
            controller = UserController(dao!!)
        }

        @Test
        fun test() {
            controller!!.doSomething()
        }
    }
    ```
    - 매번 `!!`를 선언해 줘야 하는 것도 귀찮고, 이렇게 막 쓰다가 정작 가능성 있는 nullable을 놓치게 될 위험도 있다
        - 이런 경우엔 `lateinit`이나 `Delegates.notNull`을 사용하자 
- 제일 권장하는 건 `!!`를 쓰는 상황을 만들지 않는 것... 
  
#### :heavy_check_mark: Avoiding meaningless nullability
- 특별한 이유 없이 nullable하게 만들면, `!!`가 남발하는 상황이 발생할 수 있다. 그러니 의미 없는 nullable은 피하도록 하자
- 결과 타입이 예상되지만, 값이 없을<sub>nullable하거나 `Result`가 반환될만한</sub> 상황이 조금이라도 있다면, `List<T>`가 제공해 준 것처럼 `get`/`getOrNull`과 같은 여러 케이스의 함수를 제공하도록 하자
- 클래스 필드가 사용 전이나 혹은 클래스 생성 이후 반드시 값이 할당된다면, `lateinit`이나 `notNull`위임자를 사용하자
    - e.g. `private var doctorId: Int by Delegates.notNull()`
- null인 상황과 empty인 상황을 구분하자
- nullable enum 과 'a None enum value'를 구분하자
  
#### :heavy_check_mark: The `lateinit` property and the `notNull` delegate
- 앞서 `UserControllerTest` 예시를 `lateinit` modifier를 이용해 의미 있게 해결할 수 있다
    ```kotlin
    private lateinit var dao: UserDao
    private lateinit var controller: UserController

    @BeforeEach
    fun init() {
        dao = mockk()
        controller = UserController(dao) // !!를 제거할 수 있음
    }

    @Test
    fun test() {
        controller.doSomething()  // !!를 제거할 수 있음
    }
    ```
- 초기화되기 전에 `lateinit`으로 선언한 변수가 사용된다면 예외가 throw될 것이다
    - 이 상황에서 예외가 던져지는 건 바람직한 일이다. 사용 전에 초기화가 되도록 수정해 주면 됨.
    - `lateinit`를 사용하면 `!!`로 매번 unpack하지 않아도 된다는 것도 장점

> - property delegation pattern은 `item 21`에서 더 다룰 예정
>   - property delegate의 장점은 개발자가 nullable한 상황을 안전하게 피할 수 있게 도와주는 것

## :small_blue_diamond: Item 9: Close resources with `use`
`use`를 사용해 리소스를 close하자

- `InputStream`, `OutputStream`, `Connection`, 각종 `Reader`, `Socket`, `Scanner` 등은 `Closeable`인터페이스를 implements하고 있고, `Closeable`은 `AutoCloseable`을 extends하고 있다.   
이들을 이용해 자원을 사용하고 나서 사용이 끝나면 <sub>리소스는 비용이 많이 들기 때문에</sub> `close`를 해줘야 하는데<sub>자체적으로 곧바로 close가 실행되지 않기 때문. 물론 자원에 대한 참조가 발생하지 않으면 GC에 의해 처리되기도 하지만 이렇게 처리되기 까지는 오래 걸리니</sub>, 아래와 같이 `try-finally`블록을 이용해 구현할 수 있다.  
    ```kotlin
    fun countCharactersInFile(path: String): Int {
        val reader = BufferedReader(FileReader(path))
        try {
            return reader.lineSequence().sumOf { it.length }
        } finally {
            reader.close()
        }
    }
    ```
    - 이 코드의 문제는, `try`와 `finally`블록 모두 오류가 발생할 경우 하나의 오류만 전파되어 상황을 파악하기 어렵다는 것이다
- 이때 `use`를 사용한다면 자원을 적절하게 close하고 오류를 핸들링할 수 있다. 
    ```kotlin
    reader.use { reader ->
        return reader.lineSequence().sumOf { it.length }
    }
    ```
    - `use`는 `Closeable`을 구현한 객체에서 사용할 수 있다 
- 리시버<sub>receiver</sub>는 람다로 전달할 수도 있다
    ```kotlin
    fun countCharactersInFile(path: String): Int {
        BufferedReader(FileReader(path)).use { reader ->
            return reader.lineSequence().sumOf { it.length }
        }
    }
    ```
- 코틀린에선 위와 같은 경우에 사용할 수 있는 `useLines`라는 함수를 제공한다
    ```kotlin
    fun countCharactersInFile(path: String): Int =
        File(path).useLines { lines ->
            lines.sumOf { it.length }
        }
    ```
    - 파일을 읽어올 때 여러 줄을 반복적으로 읽어와야 한다면 `useLines`를 써보자  
    - `useLines`는 내부적으로 `use`를 구현하고 있어 더 편리하고 쓸 수 있음
    - 이 시퀀스는 요청 시 줄을 읽고 메모리에 한 번에 한 줄만 보유하기 때문에 대용량 파일도 처리하는 데 적합한 방법이다
        > - 참고로 `item 51`에서 대규모 컬렉션 처리에 대한 방법을 더 알아본다

## :small_blue_diamond: Item 10: Write unit tests
단위 테스트를 작성하자
> 이는 코틀린에만 국한된 것은 아니다!  

- 애플리케이션 응답 값이 올바른지 테스트를 하는 건, 외부 요구사항에 맞춰진 테스트다. 이 테스트는 프로그래머에게 유용하긴 하지만 충분하지는 않다
- 개발자에게 더 유용하고 개발자에게 유의미한 테스트의 단위 테스트를 작성하자
- 단위 테스트는 구현 방식에 대해 빠른 피드백을 주기 때문에 개발하는 동안 매우 유용하다. 
    - 잘 테스트된 요소는 더욱 신뢰할 수 있다
        - 심리적 안정감도 준다
    - 요소를 적절하게 테스트했을 경우, 이후 리펙터링이 두렵지 않다
        - 레거시 코드 만지는 것을 두려워하지 않아도 된다
    - 수동으로 확인하는 것보다, 자동화된 테스트를 이용하면, 수정에 대한 비용을 줄일 수 있다
- 단위 테스트를 작성하는데 시간이 소요될 수 있지만
    - 장기적으로는 단위 테스트로 인해 버그를 찾는데 더 적은 시간을 할애하여 더 이득
- 테스트가 불가능한 코드들이 있을 수 있지만 
    - 이 코드를 테스트 가능하게 변경함으로써 더 나은 아키텍쳐 설립으로 이어질 수 있다
- 올바른 단위 테스트 작성을 위해선 스스로 학습할 필요가 있다
    - 형편없이 작성된 단위 테스트는 득보다 실이 클 수도 있다!
- 테스트는 장기적 유지보수와 신뢰할 수 있는 애플리케이션에 대한 투자다

<br/>

---

<br/>

> Any fool can write code that a computer can understand. Good programmers write code that humans can understand.   
> – Martin Fowler, Refactoring: Improving the Design of Existing Code, p. 15
  
> 코틀린은 무조건 간결하게 만들려는 것이 목표가 아니라, 읽기 쉽게<sub>readable</sub> 상용구나 반복된 구조 등의 노이즈를 제거하여 개발자가 더 중요한 것에 집중할 수 있도록 만들어졌다.  


<br/>

---

<br/>

## :small_blue_diamond: Item 11: Design for readability
프로그래밍은 쓰기보단 읽기에 더 많은 시간을 할애하게 된다. 때문에 항상 가독성을 염두하면서 코딩해야 한다    
  
#### :heavy_check_mark: 인지 부하 감소
아래 두 구조를 비교해보자.
```kotlin
// Implementation A
if (person != null && person.isAdult) { 
    view.showPerson(person)
} else { 
    view.showError()
}

// Implementation B
person?.takeIf { it.isAdult }
  ?.let(view::showPerson)
  ?: view.showError()
```
A와 B 중에 뭐가 더 낫다고 할 수 있을까? 사람마다 다를 것이다.  
코틀린에 익숙하고 전형적인 관용구를 많이 사용해 본 사람과, 코틀린을 처음 접하는 사람 각각은 다르게 선택할 것이다.    
어떤 구조로 구현을 하건, 읽는 사람들이 이해할 수 있는 구조로 작성해야 한다.  

#### :heavy_check_mark: 극단적이지 않게
아래 코드가 읽기 쉬운가?  
```kotlin
var obj = FileInputStream("/file.gz") 
    .let(::BufferedInputStream) 
    .let(::ZipInputStream) 
    .let(::ObjectInputStream) 
    .readObject() as SomeObject
```
복잡한 코드지만 간결하다.  
간결성 때문에 복잡하게 코드를 작성할 필요는 없다.  
코드는 명확하게 인식 가능하도록 작성하자.  

#### :heavy_check_mark: 컨벤션
프로그래밍은 표현의 예술이다. 하지만 이해하고 지켜야 할 규칙이 존재한다.   
이에 대해선 다음 아이템에서 알아보자.  

## :small_blue_diamond: Item 12: An operator’s meaning should be consistent with its function name
연산자 오버로딩을 이용할 경우, 함수의 이름과 연산자의 의미가 일치하도록 만들자.  

- 예를 들어, factorial 함수를 구현해 이를 연산자 오버로딩으로 만들고 싶다고 하면. `6!` 이런 식으로 선언하고 싶겠지만 이는 코틀린에서 지원하지 않는 연산자다. 여기서 굴하지 않고 `operator fun Int.not() = factorial()`이렇게 구현하여 `!6`으로 쓸 수 있게 만들었다고 가정하자
    - 이게 좋은 코드일까? Nope!
    - 문자열 앞에 느낌표를 붙이는 건 `not`이라는 논리 연산으로 사용한다고 우리는 이해하고 있는데, 갑자기 factorial 결과값이 나온다면, 이는 혼란과 오해를 불러일으키게 된다!
- 연산자의 의미가 명확하지 않을 경우, 연산자 오버로딩이 아닌 특정 이름을 가진 함수로 구현하자
    - 아니면 infix 확장 함수나 top-level 함수로 구현해보자 


#### :heavy_check_mark: 연산자 오버로딩을 이상한 방식으로 사용해도 되는 경우: DSL<sub>Domain Specific Language</sub> 설계시!    
```
body {
    div {
        +"Some text"
    }
}
```
위와 같이 고전적인 HTML DSL에서, 요소에 텍스트를 추가하기 위해 `String.unaryPlus`를 사용했다.   

## :small_blue_diamond: Item 13: Use operators to increase readability
앞선 `itme 12`에선 연산자 오버로딩의 오용에 대해 알아보았다. 이번 장에선 가독성을 높이기 위해 연산자를 사용하는 것에 대해 알아보자.  

```kotlin
val netPrice = BigDecimal("10")
val tax = BigDecimal("0.23")
val currentBalance = BigDecimal("20")

// A
val newBalance = currentBalance.minus(netPrice.times(tax))

// B
val newBalance = currentBalance - netPrice * tax
```
```kotlin
val SUPPORTED_TAGS = setOf("ADMIN", "TRAINER", "ATTENDEE")
val tag = "ATTENDEE"

// A
println(SUPPORTED_TAGS.contains(tag))
// B
println(tag in SUPPORTED_TAGS)
```
```kotlin
val ADMIN_TAG = "ADMIN"

// A
val admins = users.map { user.tags.contains(ADMIN_TAG) }
// B
val admins = users.map { ADMIN_TAG in user.tags }
```
- 위 케이스들에서 A와 B 중 어떤 것이 더 읽기 쉬운가?

## :small_blue_diamond: Item 14: Specify the variable type when it is not clear
코틀린이 제공해 주는 타입 추론 시스템 덕분에, 타입을 생략할 수 있다. 하지만 반환값이 명확하지 않은 경우 등에서 타입을 생략하면 오히려 가독성을 떨어트린다

```kotlin
val data = getSomeData() // data의 타입이 뭔지 알려면 함수를 확인해 봐야 함.. 
```

> 경우에 따라 어떤 구현 방법이 가독성을 떨어트리는지 잘 생각하고 구현하자.  

## :small_blue_diamond: Item 15: Consider referencing receivers explicitly
리시버를 명시적으로 사용하자

- 둘 이상의 리시버가 사용되는 범위 안에서, 명시적 리시버를 사용하면 가독성이 증가한다
    - 명시적 리시버를 사용하지 않은 경우
        ```kotlin
        class Node(val name: String) {
            fun makeChild(childName: String) =
                create("$name.$childName").apply {
                    println("Created $name")
                }

            fun create(name: String): Node? = Node(name)
        }

        fun main() {
            val node = Node("parent")
            node.makeChild("child") 
            // Created parent.child in parent
        }
        ```
    - 명시적 리시버를 사용한 경우
        ```kotlin
        class Node(val name: String) {
            fun makeChild(childName: String) = 
                create("$name.$childName").apply {
                    println("Created ${this?.name} in ${this@Node.name}")
                }

            fun create(name: String): Node? = Node(name)
        }
        ```
- 리시버를 변경해야 할 경우, 명시적 리시버를 사용하면 함수의 출처를 명확히 알 수 있기 때문에 가독성을 증가시킬 수 있다

## :small_blue_diamond: Item 16: Properties should represent a state, not a behavior
- 코틀린의 프로퍼티는 자바의 필드와 비슷하게 생겼지만, 개념이 다르다
    - 둘 다 데이터를 보관할 수 있지만
    - 프로퍼티에는 더 많은 기능이 있다
        - 커스텀 setter/getter
            ```kotlin
            var name: String? = null
                get() = field?.uppercase()
                set(value) {
                    if (!value.isNullOrBlank()) {
                        field = value
                    }
                }
            ```        
            - 이 예시에서 보면 `field`라는 백킹 필드<sub>backing field</sub>를 이용하고 있다. 이는 `name`을 지칭한다
- 프로퍼티는 필드보단 접근자의 개념으로 보자
    - 위임 속성을 사용할 수 있다 ( `item 21`에서 더 다룰 예정)
- 하지만 과도한 동작을 포함시키진 말자
    - 무거운 동작이 반복적으로 실행되기 때문에, 이 경우엔 함수로 추출되어야 한다

## :small_blue_diamond: Item 17: Consider naming arguments
인수 이름을 잘 지어주자
- 인수 이름을 활용하면 휴먼 에러를 방지할 수 있다
    - 인수가 n개 이상일 경우, 잘못된 위치에 인수를 전달할 확률이 낮아짐 
    - 동일한 타입의 인수가 여러 개 있을 때, 인수 이름을 이용해 헷갈림 방지 가능

## :small_blue_diamond: Item 18: Respect coding conventions
컨벤션 잘 지키자. 코틀린 뿐만 아니라!
- 프로젝트의 코드는 여러 사람이 아닌 한 사람이 작성한 것처럼 작성되어야 한다

## :small_blue_diamond: Item 19: Do not repeat knowledge
재사용성의 중요성
- 비슷한 로직으로 구현되어 있다고 하더라도, 서로 다른 곳(actor)에서 사용된다면 하나의 로직으로 처리하지 말라
    - 그들은 서로 독립적으로 변경할 가능성이 크기 때문
- 재사용되어선 안되는 부분을 재사용하려는 유혹을 참아내자...
- 물론 DRY<sub>don't repeat yourself</sub>이긴 하지만, 균형을 잘 지켜서 개발하자 

프로그램의 주요 지식 2가지
- logic, common algorithem

Single responsibility principle<sub>srp</sub>
- solid에서 단일 책임 원칙
- 클래스가 변경되는 이유는 하나여야 한다
    - 둘 이상의 이유로 하나의 클래스가 변경되선 안된다 

## :small_blue_diamond: Item 20: Do not repeat common algorithms
비즈니스 로직을 포함하지 않고 별도 모듈/라이브러리로 추출할 수 있는 패턴은 일반적인 동작을 한다. 이들을 반복적으로 구현하진 말자
- 어디선가 한 번 사용되면 최적화되어, 다른 사용처에서도 이 이득을 누릴 수 있음
- 명명해두면 나중에 이름만 보고도 기능을 이해할 수 있어 시간 절약됨  

stdlib<sub>standard library</sub>를 항상 살펴보자!  
- 만약 stdlib에 없는 알고리즘이 필요하다면, 프로젝트에서 정의하되 extension function을 활용하자 
    - 구체적인 타입이 있는 개체에서만 사용하도록 제한할 수 있어 좋음
    - 수정할 객체를 인수로 받는 것보다, 확장 리시버로 사용하는게 더 가독성에 좋음
    - 일반 함수보다 찾기 쉬움(자ㅏ동 완성을 빨리 할 수 있)

## :small_blue_diamond: Item 21: Use property delegation to extract common property patterns
- 프로퍼티 패턴을 추출하는 방법에 프로퍼티 위임이 사용된다 
- 프로퍼티 위임은 프로퍼티의 일반적 행위를 추출해 재사용할 수 있게 한다
    - 프로퍼티와 관련된 다양한 조작 가능 
- 위임은 단지 반복 패턴을 줄이기 위해서가 아니라 observable 속성도 갖고 있다 
- lazy, observable 용도 외에도 다른 패턴도 존재한다 
    - lazy
        - 첫 사용 요청이 들어왔을 때 초기화되는 패턴
    - observable
        - 프로퍼티 값에 변경이 있을 때마다 이를 감지하는 패턴
    - vetoable
    - notNull
    - 이 외에도 직접 프로퍼티 위임을 만들어 사용할 수도 있다 

> *책엔 안드로이드 예시가 많이 나옴...* 

## :small_blue_diamond: Item 22: Use generics when implementing common algorithms
타입 파라미터는 컴파일 타이에 타입을 확인하고 올바르게 추론할 수 있어 더욱 안전한 프로그래밍을 할 수 있다  

> - 제네릭은 일반적으로 JVM 바이트 코드 제한으로 인해, 컴파일 중에 지워지기 때문에 런타임에 그다지 유용하지 않음
>   - `reified` 타입만 안 지워짐

타입 파라미터는 구체적인<sub>concrete</sub> 타입의 서브타입만 사용하도록 타입을 제한할 수 있다
- 이렇게 하면 이 concrete 타입이 제공하는 함수를 안전하게 사용할 수 있다

## :small_blue_diamond: Item 23: Avoid shadowing type parameters
동일한 이름으로 클래스 프로퍼티와 함수 파라미터를 정의할 경우, 함수 파라미터가 외부 스코프인 프로퍼티를 가리는 상황이 발생한다. 이를 섀도잉<sub>shadowing</sub> 이라 한다.  
```kotlin
class Forest(val name: String) {
    fun addTree(name: String) { ... }
}
```

섀도잉이 발생할 수 있는 상황을 만들지 말자.  

## :small_blue_diamond: Item 24: Consider variance for generic types
예시
- `class Cup<T>` 
    - 타입 파라미터 `T`에 variance modifier<sub>`in`, `out`</sub>가 안 붙어 있다면, 기본적으로 이는 invariant이라는 뜻
    - `T` 타입 간에 어떤 관계가 있다 하더라도, 이 제네릭 클래스에서는 아무 관계가 없다는 뜻
    - e.g. 
        ```kotlin
        // 아래 코드 전부 컴파일 에러 발생: Type mismatch

        val anys: Cup<Any> = Cup<Int>()
        val numbers: Cup<Number> = Cup<Int>()
        val nothings: Cup<Any> = Cup<Nothing>()
        val ints: Cup<Nothing> = Cup<Int>()
        ```            
        - `Cup<Int>`, `Cup<Number>`
        - `Cup<Any>`, `Cup<Nothing>`

variance modifiers            
- `out`, `in` variance modifiers를 사용하면 관계를 만들 수 있다
- `out`
    - 해당 타입 파라미터를 covariant<sub>공변</sub>로 만든다
    - `A is B` 관계에서 `Cup`의 타입 파라미터에 `out` 을 붙여 `Cup`이 covariant로 만든다면
        - `Cup<A> is a subtype of Cup<B>`가 되는 것이다
        ```kotlin
        class Cup<out T>

        fun main() {
            val anys: Cup<Any> = Cup<Int>() // 정상 동작
            val numbers: Cup<Number> = Cup<Int>() // 정상 동작
            val nothings: Cup<Any> = Cup<Nothing>() // 정상 동작
            // val ints: Cup<Nothing> = Cup<Int>() // 컴파일 에러 발생

            val b: Cup<Dog> = Cup<Puppy>() // 정상 동작
            // val a: Cup<Puppy> = Cup<Dog>() // 컴파일 에러 발생
        }

        open class Dog
        class Puppy: Dog()
        ```
- `in`
    - 해당 타입 파라미터를 contravariant<sub>반공변</sub>로 만든다
    - `A is B` 관계에서 `Cup`의 타입 파라미터에 `in` 을 붙여 `Cup`이 contravariant로 만든다면
        - `Cup<A> is a supertype of Cup<B>`가 되는 것이다
        ```kotlin
        class Cup<in T>

        fun main() {
            // val anys: Cup<Any> = Cup<Int>() // 컴파일 에러 발생
            // val numbers: Cup<Number> = Cup<Int>() // 컴파일 에러 발생
            // val nothings: Cup<Any> = Cup<Nothing>() // 컴파일 에러 발생
            val ints: Cup<Nothing> = Cup<Int>() // 정상 동작

            // val b: Cup<Dog> = Cup<Puppy>() // 컴파일 에러 발생
            val a: Cup<Puppy> = Cup<Dog>() // 정상 동작
        }

        open class Dog
        class Puppy: Dog()
        ```
        
> | variance<br/>modifier | example |
> |--|--|
> | | Int -> Number|
> | invariant<br/>`class Box<T>` | `Box<Int>` --  `Box<Number>` |
> | covariant<br/>`class Box<out T>` | `Box<Int>` -> `Box<Number>` |
> | contravariant<br/>`class Box<in T>` | `Box<Int>` <- `Box<Number>` |

Function types
- 아래 고차 함수의 인자로 `(Int) -> Number`, `(Number) -> Int`, `(Any) -> Number` 등의 타입이 가능하다
    ```kotlin
    fun printProcessedNumber(transition: (Int) -> Any) {
        println(transition(42))
    }
    ```
    > - 자세한 예시는 `item24.FunctionType.kt` 참고
    - 이게 가능한 이유는 다음과 같은 관계가 있기 때문
        - `(Int) -> Any` <-- `(Int) -> Number`, `(Number) -> Any` <-- `(Number) -> Number` <-- `(Any) -> Number`, `(Number) -> Int`
            - 계층 구조에서 아래로 내려갈수록 파라미터 타입은 더 상위 타입으로, 리턴 타입은 더 하위 타입으로 흘러간다
        - 이러한 계층 구조를 가지게 된 것은
            - 코틀린의 함수 타입에서   
            모든 파라미터 타입은 `in` variance modifier인 contravariant이고,  
            모든 반환 타입은 `out` variance modifier인 covariant이기 때문  
                - e.g. `(T1, T2) -> T3`
                    - `T1`, `T2`는 contravariant
                    - `T3`은 covariant
  
The safety of variance modifiers
- java에서 `array`는 covariant다. <sub>이렇게 된 이유는 모든 타입의 배열에서 일반적인 operation이 동작하게 하기 위함이었다고 추측,,</sub>
    ```java
    Integer[] numbers = {1, 4, 2, 1};
    Object[] objects = numbers;
    objects[2] = "B"; // Runtime error: ArrayStoreException (not compile time error)
    ```
    - `numbers`가 `Object[]`타입 변수에 담겼지만, 내부 구조는 여전히 `Integer` 배열인 상태다. 그래서 `String`타입의 값을 `objects`의 요소로 넣으려고 하면 에러가 발생하는 것이다.   
        - 빌드된 클래스 파일 열어보면   
            ```java
            Integer[] numbers = new Integer[]{1, 4, 2, 1};
            numbers[2] = "B";        
            ```
    - 이러한 <sub>자바의 결함</sub> 상황을 방지하기 위해 코틀린에선 `Array`<sub>`IntArray`, `CharArray` etc.</sub>를 invariant로 만든 것이다. 
        - `Array<Int>`를 `Array<Any>`로 업캐스팅할 수 없음! 
        ```kotlin
        val intArray = arrayOf<Int>()
        val anyArray: Array<Any> = intArray // compile error: type mismatch
        ```
- public in-position에 covariant 타입 파라미터<sub>`out` modifier</sub>는 금지 (안전하지 못하기 때문)
    ```kotlin
    fun main() {
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

    class Box<out T> {
        private var value: T? = null

        // illegal in kotlin (예시를 위함... ㅠㅠ)
        fun set(value: @UnsafeVariance T) {
            this.value = value
        }

        fun get(): T = value ?: error("Value not set")
    }
    ```
    - 위 예시는 `Box<out T>`라 선언했고, `puppyBox`에 `Hound`를 담거나 `dogBox2`에 `Int` 등 다른 타입을 담을 수 있게 되는 상황이다. 예기치 못한 에러가 발생할 수 있는 상황!  
    - 그렇기 때문에 kotlin은 public in position에서, `out` modifier를 사용한 covariant를 금지한다
        - 위 경우에선 `Box#set()`을 private으로 선언하여 오브젝트를 업캐스트하는데 covariance를 쓸 수 없도록 만드는 방식으로 해결할 수도 있다  
    - `MutableList`는 invariant다. 그래서 `T`를 in position에 사용해도 안전하다
        ```kotlin
        fun main() {
            val list = mutableListOf("a", "b", "c")
            append(list) // compile error: type mismatch
        }

        fun append(list: MutableList<Any>) {
            list.add(42)
        }
        ```
        - invariant이기 때문에 `append` 함수에는 `MutableList<Any>` 타입만 들어올 수 있음!  
- public out-position에 covariant 타입 파라미터<sub>`out` modifier</sub>는 충분히 가능
    - e.g. 생상자나 immutable data holders에 주로 사용됨 
    - `List`는 covariant다. 그래서 `List<Any?>`를 기대하는 곳에 `List<Int>`를 넣을 수 있다. 
        ```kotlin
        fun main() {
            val list = listOf(1, 2, 3)
            append(list)
        }

        fun execute(list: List<Any?>) {
            // do somthing
        }
        ```
        - 이때 `List`는 immutable하기 때문에 covariant여도 앞서 'in position에 out modifier를 사용하게 될 경우 발생할 수 있는 에러' 상황이 발생하지 않을 뿐더러, 아래 예시처럼 `execute`내에서 <sub>파라미터로 들어온 어떤 타입이든</sub> `List`내 함수를 자유롭게 적용할 수 있는 것이다
- public out-position에 contravariant 타입 파라미터는 금지
    - 단, 아래와 같이 private으로 선언하는건 가능
        ```kotlin
        class Box<in T> {
            private var value: T? = null

            fun set(value: T) {
                this.value = value
            }

            private fun get(): T = value ?: error("Value not set") 
        }
        ```
        - `get`이 private이 아니라면 compile error 발생
            - `Type parameter T is declared as 'in' but occurs in 'out' position in type T`
                - `T`는 `in`으로 선언되있는데, 공개적이면 `out`처럼 사용되는거니, 리턴 타입에 `@UnsafeVariance`를 붙여 명시적으로 표기하거나 아니면 `in`을 제거하라고 뜸
    - `in` modifier를 public in-position에 알맞게 쓴 예시: `kotlin.coroutines.Continuation`과 `resumeWith()`

Variance modifier positions
- variance modifier 사용 위치는 2가지가 있다
    - 선언자<sub>declaration-side</sub> 위치
        ```kotlin
        class Box<out T>(val value: T)

        fun main() {
            val boxStr: Box<String> = Box("Str")
            val boxAny: Box<Any> = boxStr
        }
        ```
    - 사용<sub>use-site</sub> 위치
        ```kotlin
        class Box<T>(val value: T)

        fun main() {
            val boxStr: Box<String> = Box("Str")
            val boxAny: Box<out Any> = boxStr
        }        
        ```
        - 이 방법을 적용할 수 없는 인스턴스도 존재한다
        - 주로 하나의 변수에 대해 적용하고자 할 때 사용한다
            ```kotlin
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
                val dogs = mutableListOf<Dog>(Hound("Pluto")) // Dog로 채우길 원함
                fillWithPuppies(dogs)
                println(dogs) // [Hound(name=Pluto), Puppy(name=Jim), Puppy(name=Beam)]

                val animals = mutableListOf<Cutie>(Cat("Felix")) // Cutie로 채우기 위함 
                fillWithPuppies(animals) // [Cat(name=Felix), Puppy(name=Jim), Puppy(name=Beam)]
                println(animals)
            }
            ```        

#### :heavy_check_mark: 정리
- `Cup<T>`에서 타입 파라미터 `T`는 invariant
    - `A is a subtype of B`일 때, `Cup<A>`와 `Cup<B>`는 아무 관계도 없다
- `Cup<out T>`에서 타입 파라미터 `T`는 covariant
    - `A is a subtype of B`일 때, `Cup<A> is a subtype of Cup<B>`가 된다
    - covariant type은 out-position에 쓸 수 있다 <sub>e.g. `get()`</sub>
- `Cup<in T>`에서 타입 파라미터 `T`는 contravariant
    - `A is a subtype of B`일 때, `Cup<B> is a subtype of Cup<A>`가 된다
    - contravariant type은 in-position에 쓸 수 있다 <sub>e.g. `set()`</sub>
  
> `out`은 only read, `in`은 only modify  
