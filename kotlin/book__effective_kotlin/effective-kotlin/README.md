# [이펙티브 코틀린](https://product.kyobobook.co.kr/detail/S000001033129)

<br/>

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

<br/>

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

<br/>

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

<br/>

## :small_blue_diamond: 아이템 4: inferred 타입으로 리턴하지 말라
> 코틀린의 타입 추론<sub>type inference</sub>은 유용한 기능이지만...  

- 타입을 확실하게 지정해야 하는 경우엔 명시적으로 타입을 지정하자
- 특별한 이유나 확실한 확인 없이는 타입을 제거하지 말자

<br/>

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

<br/>

## :small_blue_diamond: Item 6: Prefer standard errors to custom ones
커스텀한 예외 대신 표준 예외를 사용하자.  

- 이미 잘 알려진 표준 라이브러리의 예외를 사용하면, 사용자들이 API를 더 쉽게 이해할 수 있다. 

<br/>

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

<br/>

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

<br/>

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

<br/>

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

<br/>

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

<br/>

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

<br/>

## :small_blue_diamond: Item 14: Specify the variable type when it is not clear
코틀린이 제공해 주는 타입 추론 시스템 덕분에, 타입을 생략할 수 있다. 하지만 반환값이 명확하지 않은 경우 등에서 타입을 생략하면 오히려 가독성을 떨어트린다

```kotlin
val data = getSomeData() // data의 타입이 뭔지 알려면 함수를 확인해 봐야 함.. 
```

> 경우에 따라 어떤 구현 방법이 가독성을 떨어트리는지 잘 생각하고 구현하자.  

<br/>

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

<br/>

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

<br/>

## :small_blue_diamond: Item 17: Consider naming arguments
인수 이름을 잘 지어주자
- 인수 이름을 활용하면 휴먼 에러를 방지할 수 있다
    - 인수가 n개 이상일 경우, 잘못된 위치에 인수를 전달할 확률이 낮아짐 
    - 동일한 타입의 인수가 여러 개 있을 때, 인수 이름을 이용해 헷갈림 방지 가능

<br/>

## :small_blue_diamond: Item 18: Respect coding conventions
컨벤션 잘 지키자. 코틀린 뿐만 아니라!
- 프로젝트의 코드는 여러 사람이 아닌 한 사람이 작성한 것처럼 작성되어야 한다

<br/>

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

<br/>

## :small_blue_diamond: Item 20: Do not repeat common algorithms
비즈니스 로직을 포함하지 않고 별도 모듈/라이브러리로 추출할 수 있는 패턴은 일반적인 동작을 한다. 이들을 반복적으로 구현하진 말자
- 어디선가 한 번 사용되면 최적화되어, 다른 사용처에서도 이 이득을 누릴 수 있음
- 명명해두면 나중에 이름만 보고도 기능을 이해할 수 있어 시간 절약됨  

stdlib<sub>standard library</sub>를 항상 살펴보자!  
- 만약 stdlib에 없는 알고리즘이 필요하다면, 프로젝트에서 정의하되 extension function을 활용하자 
    - 구체적인 타입이 있는 개체에서만 사용하도록 제한할 수 있어 좋음
    - 수정할 객체를 인수로 받는 것보다, 확장 리시버로 사용하는게 더 가독성에 좋음
    - 일반 함수보다 찾기 쉬움(자ㅏ동 완성을 빨리 할 수 있)

<br/>

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

<br/>

## :small_blue_diamond: Item 22: Use generics when implementing common algorithms
타입 파라미터는 컴파일 타이에 타입을 확인하고 올바르게 추론할 수 있어 더욱 안전한 프로그래밍을 할 수 있다  

> - 제네릭은 일반적으로 JVM 바이트 코드 제한으로 인해, 컴파일 중에 지워지기 때문에 런타임에 그다지 유용하지 않음
>   - `reified` 타입만 안 지워짐

타입 파라미터는 구체적인<sub>concrete</sub> 타입의 서브타입만 사용하도록 타입을 제한할 수 있다
- 이렇게 하면 이 concrete 타입이 제공하는 함수를 안전하게 사용할 수 있다

<br/>

## :small_blue_diamond: Item 23: Avoid shadowing type parameters
동일한 이름으로 클래스 프로퍼티와 함수 파라미터를 정의할 경우, 함수 파라미터가 외부 스코프인 프로퍼티를 가리는 상황이 발생한다. 이를 섀도잉<sub>shadowing</sub> 이라 한다.  
```kotlin
class Forest(val name: String) {
    fun addTree(name: String) { ... }
}
```

섀도잉이 발생할 수 있는 상황을 만들지 말자.  

<br/>

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
  
```kotlin
var list1 = listOf<Any>()
val list2 = listOf<Int>()
list1 = list2

var list3 = arrayOf<Any>()
val list4 = arrayOf<Int>()
list3 = list4 // compile error: Type mismatch.
```
- `List`, `Set`, `Map`의 타입 파라미터는 covariant
- `Array`, `MutableList`, `MutableSet`, `MutableMap`의 타입 파라미터는 invariant

> `out`은 only read(함수에서 반환), `in`은 only modify(함수에서 인자)  

<br/>

## :small_blue_diamond: Item 25: Reuse between different platforms by extracting common modules

<br/>

## :small_blue_diamond: Item 26: Each function should be written in terms of a single level of abstraction
> - 추상화는 복잡성을 숨기기 위해 사용되는 단순화된 형태
> - 모든 인스턴스가 하나의 추상화만을 가지는 건 아니다. 관점에 따라 다양한 인터페이스로 표현할 수 있다

- 기능은 작고 최소한의 책임을 가지도록 한다
- 적절한 추상화를 통해 읽기 쉬운 코드를 만들면, 함수를 재사용하기 용이하고 테스트하기 쉽다

Abstraction levels in program architecture
- 추상화를 이용해 하위 시스템의 세부 사항을 숨기고 관심사를 분리하여 상호 운영성<sub>interoperability</sub>과 플랫폼 독립성<sub>platform independence</sub>을 가능케 할 수 있다
    > - Code Complete by Steve McConnell, 2nd Edition, Section 34.6

<br/>

## :small_blue_diamond: Item 27: Use abstraction to protect code against changes
- 추상화를 이용한다면 코드 변경/변화로부터 자유로울 수 있다
- 추상화로 가는 길
    - Extracting constant
    - Wrapping behavior into a function
    - Wrapping function into a class
    - Hiding a class behind an interface
    - Wrapping universal objects into specialistic
    - Using generic type parameters
    - Extracting inner classes
    - Restricting creation, for instance by forcing object creation via factory method
- 추상화의 문제점
    - 너무 많은 추상화는 행동 결과를 예측하기 어렵게 할 수도 있다
    - 코드 이해력을 떨어트릴 수 있다 
- 팀 사이즈, 프로젝트 사이즈, 도메인 지식 등 조건에 따라 적절한 추상화를 하자 

<br/>

## :small_blue_diamond: Item 28: Specify API stability
- 표준 API를 선호하는 이유
    - 익숙하지 않은 API 업데이트에 대응하기 어려운 문제
    - 새 API를 학습하는데 필요한 러닝 커브
- 도입 단계라면
    ```kotlin
    @Experimental(level = Experimental.Level.WARNING)
    annotation class ExperimentalNewApi

    @ExperimentalNewApi
    suspend fun getUsers(): List<User> { ... }
    ```
- 전환하고자 한다면
    ```
    @Deprecated("Use suspending getUsers instead")
    fun getUsers(callback: (List<User>)->Unit) { ... }

    // 대안이 있는 경우 
    @Deprecated("Use suspending getUsers instead", ReplaceWith("getUsers()"))
    fun getUsers(callback: (List<User>)->Unit) { ... }
    ```
  
- API를 갑자기 변경하여 사용자에게 고통을 주지 말자..  
- 사용자가 API를 안정적으로 쓸 수 있도록 잘 가이드하자
    - 모듈/라이브러리에 description을 잘 작성하자
    - 버전 이름, 도큐먼트, 주석을 잘 업데이트하자 

<br/>

## :small_blue_diamond: Item 29: Consider wrapping external API
- 사용자의 프로젝트에 맞게 API를 래핑해서 쓰자 
    - 사용자 입맞에 맞게 바꿔가며 사용할 수 있다
- 문제점도 있음
    - 래핑된 API에 대해 알아야 함. 그리고 이 래핑된 API에서 문제가 발생하면 이에 대한 해결 방식 또한 내부에서 가지고 있어야 함

<br/>

## :small_blue_diamond: Item 30: Minimize elements visibility
- 내부 요소를 변경할 수 있는 set은 외부로 드러나지 않게 하자 
    ```kotlin
    class CounterSet<T>(
        private val innerSet: MutableSet<T> = mutableSetOf()
    ) : MutableSet<T> by innerSet {
        var elementsAdded: Int = 0
            private set

        override fun add(element: T): Boolean {
            elementsAdded++
            return innerSet.add(element)
        }

        override fun addAll(elements: Collection<T>): Boolean {
            elementsAdded += elements.size
            return innerSet.addAll(elements)
        }
    }
    ```
- 가시성이 제한적일 때, 클래스가 어떻게 변경되는지 추척하기 쉽다
- 요소가 외부로 드러나야 할 이유가 없다면 숨기도록 하자 
    - 단, 이 규칙에서 data를 가지고 있는 클래스는 제외.
        - e.g. data model class, DTO... 

> #### 클래스 멤버의 가시성 변경자
> - private 
>     - visible inside this class only.
> - protected 
>     - visible inside this class and in subclasses.
> - internal 
>     - visible inside this module, for clients who see the declaring class.
> - public (default) 
>     - visible everywhere, for clients who see the declaring class.
>
> #### Top-level 요소의 가시성 변경자
> - private 
>     - visible inside the same file only. 
> - internal 
>     - visible inside this module.
> - public (default) 
>     - visible everywhere.
> 
> 모듈과 패키지는 다르다. 코틀린에서 'same module'은 '함께 컴파일된 코틀린 소스의 세트'로 정의된다.  
> - e.g. Gradle source set, Maven project, IntelliJ IDEA module, a set of files compiled with one invocation of the Ant task.

<br/>

## :small_blue_diamond: Item 31: Define contract with documentation
- 주석을 적절하게 이용하면 해당 기능에 대해 더욱 빠르게 파악할 수 있다 
- 적절한 contract를 정의하여, 사용자가 세부 구현에 의존하지 않도록 하자
    - names
    - comments and documentation    
        - 기능을 설명해 줄 가장 강력한 방법
    - types
        - 인자 타입, 반환 타입
- 주석이 항상 좋은 것은 아니다
    - 함수 이름과 매개 변수로 명확하게 기능을 인지할 수 있다면, 굳이 주석을 달아서 과대 정보를 불필요하게 전달하지 말도록..
- 사용자에게 API의 contract를 통해 현재 어떻게 동작하고 앞으로 어떻게 동작할 것인지 확신을 주자 

KDoc
- 주석을 사용해 기능을 문서화할 때, 해당 주석을 제시하는 공식 형식을 `KDoc`이라 한다
- 형식
    - `/** ... */`
    - 마크다운으로 작성
    - 첫째 단락은 요약 설명, 둘째 단락은 자세한 설명, 그다음부턴 태그
- 태그
    - `@param`, `@return`, `@constructor`, `@author`...

<br/>

## :small_blue_diamond: Item 32: Respect abstraction contracts
- 프로그램을 안정적으로 유지하려면 contract를 잘 지키자
    - 가시성을 private으로 해두었다면 그럴 만한 이유가 있었을 것이다...
    - 만약 이를 부숴야 한다면, 이 사실을 잘 문서화해두자 

<br/>

## :small_blue_diamond: Item 33: Consider factory functions instead of secondary constructors
클래스를 객체화할 때 기본 생성자 외 다른 방식으로 생성하고자 한다면, 생성자 대신 함수로 만들어보자
- 함수는 이름이 있기 때문에, 어떻게 생성하는지 그리고 인자가 무엇인지 드러낼 수 있다. 좀 더 알기 쉽다! 
    - e.g. `ArrayList(3)` vs `ArrayList.withSize(3)`
        - 전자는 `3`이 사이즈인지 요소인지 알 수 없다. 후자는 직관적으로 알 수 있다 
- 동일한 인자 타입을 가진 생성자들끼리 충돌을 미연에 방지할 수 있다
- 함수는 생성자와는 달리 반환 타입의 하위 타입으로 반환할 수도 있다
    - 자유도가 증가함
    - e.g. `listOf` -> 여러 타입으로 리스트를 만들어낼 수 있음
- 생성자와 달리 함수는 호출될 때마다 새 객체를 생성하지 않을 수 있다
    - 캐싱 등을 이용해 객체 생성을 최적화하거나(e.g. like singleton pattern), 객체 재사용, 혹은 객체를 생성할 수 없을 때 null을 리턴하는 정적 팩토리 함수(e.g. `Connections.createOrNull()`) 등..
- 생성자는 슈퍼 클래스의 생성자나 주 생성자를 즉시 호출하지만, 함수를 이용하면 생성자 호출을 연기할 수도 있다 

#### :heavy_check_mark: factory function의 종류와 규칙
**Companion Object Factory Functions**
- 자바에서 대부분의 factory function은 static으로 선언되어 있다. 코틀린 커뮤니티는 자바에 기원을 두고 있어, factory function를 companion obejct에 정의하여 이 관행을 모방하고 있다  
    ```kotlin
    class LinkedList<T>(
        val head: T,
        val tail: LinkedList<T>?
    ) {
        companion object {
            fun <T> of(vararg elements: T): LinkedList<T> { ... }
        }
    }

    // usage
    val list = LinkedList.of(1, 2)
    ```
- companion object factory function은 companion object에 정의되어야 하며, top-level 함수는 어디서든 정의할 수 있다. companion object factory function는 companion object에 대한 확장으로 정의할 수 있다!  
    ```kotlin
    interface Tool {
        companion object { ... }
    }

    fun Tool.Companion.createBigTool(/*...*/): Tool { ... }

    val tool = Tool.createBigTool()
    ```
- companion object factory function에는 몇 가지 네이밍 규칙이 존재한다
    - e.g.
        - `from`
            - e.g. `val date: Date = Date.from(instant)`
        - `of`
            - e.g. `val faceCards: Set<Rank> = EnumSet.of(JACK, QUEEN, KING)`
        - `valueOf`
            - e.g. `val prime: BigInteger = BigInteger.valueOf(Integer.MAX_VALUE)`
        - `instance` or `getInstance`
            - e.g. `val luke: StackWalker = StackWalker.getInstance(options)`
        - `createInstance` or `newInstance`
            - e.g. `val newArray = Array.newInstance(classObject, arrayLen)`
        - `get{Type}`
            - e.g. `val fs: FileStore = Files.getFileStore(path)`
        - `new{Type}`
            - e.g. `val br: BufferedReader = Files.newBufferedReader(path)`
- companion object는 static 요소의 대체제 뿐 아니라, 인터페이스 구현이나 클래스 상속도 가능하다
    ```kotlin
    abstract class ActivityFactory {
        abstract fun getIntent(context: Context): Intent
        ...
    }

    class MainActivity : AppCompatActivity() {
        ...
        companion object : ActivityFactory() {
            override fun getIntent(context: Context): Intent {}
        }
    }
    ```
- abstract companion object factory는 값을 가질 수 있어, 캐싱을 구현하거나 테스트를 위한 fake 생성도 지원할 수 있다

**Top-level factory functions**
- 일반적으로 top-level factory function을 이용해 개체를 생성할 수 있다
    - 대표적인 예
        - `listOf`, `setOf`, `mapOf`...
- companion object보다 읽기 쉬울 수도?
    - `List.of(1, 2, 3)` vs `listOf(1, 2, 3)`
- top-level factory function은 모든 곳에서 사용할 수 있으므로 <sub>혼란을 야기하지 않도록</sub> 네이밍 등에 신경써서  신중히 만들도록 하자

**Builders**
- 코틀린에서 빌더를 구현하는 일반적인 방법은 top-level function과 DSL 패턴을 이용하는 것이다
    ```kotlin
    val s = sequence {
        yield("A")
        yield("B")
    }

    println(s.toList())
    ```
- kotlin 코루틴에서 코루틴을 시작하거나 흐름을 정의할 때 빌더 패턴을 일반적으로 사용하고 있다 
    ```kotlin
    scope.launch { ... }
    ```

**Conversion methods**
- A 타입에서 B 타입으로 convert할 때, conversion method를 사용할 수 있다
    ```kotlin
    val sequence: Sequence = list.asSequence() 
    val double: Double = i.toDouble()
    val flow: Flow = observable.asFlow()
    ```

**Fake constructors**
- 생성자는 top-level function과 동일하게 동작한다
    ```kotiln
    class A
    fun b() = A()

    val x = A()
    val y = b()
    ```
- '생성자 참조' 역시 top-level function과 동일하게 동작한다
    ```kotlin
    val reference: () -> A = ::A
    ```
    - '생성자 참조'는 함수 타입<sub>function type</sub>을 구현한다
   
- 위 예시를 보면 생성자와 함수 사용 차이는 대/소문자인 것을 알 수 있다
    - 생성자는 대문자, 함수는 소문자로 시작함
    - but, kotlin standard library에서는 함수 중 이름이 대문자로 시작하는 경우가 있음
        - e.g. `List`, `MutableList`
            ```kotlin
            @SinceKotlin("1.1")
            @kotlin.internal.InlineOnly
            public inline fun <T> List(size: Int, init: (index: Int) -> T): List<T> = MutableList(size, init)

            @SinceKotlin("1.1")
            @kotlin.internal.InlineOnly
            public inline fun <T> MutableList(size: Int, init: (index: Int) -> T): MutableList<T> {
                val list = ArrayList<T>(size)
                repeat(size) { index -> list.add(init(index)) }
                return list
            }
            ```
            ```kotlin
            List(4) { "User$it" } // [User0, User1, User2, User3]
            ```
        - 이 top-level 함수는 생성자처럼 보이고 동작하지만, factory function의 이점을 가졌다
            - 사용하는 쪽에선 이것이 top-level 함수라는걸 알 필요 없이 생성자처럼 쓰게 된다. 그래서 이러한 함수를 facke constructor라고 부른다
- fake constructor를 만드는 이유
    - interface에 대한 생성자가 필요해서
    - reified type parameter가 필요해서  
- fake constructor를 만드는 또다른 방법
    - `invoke` operator를 가진 companion object
        ```kotlin
        class Tree<T> {
            companion object {
                operator fun <T> invoke(size: Int, generator: (Int) -> T): Tree<T> { ... }
            }
        }
        ```    
        ```kotlin
        Tree(10) { "$it" }
        ```    
        - 이 방법은 권장되진 않음. `item 12`에 어긋남. 
            - companion object를 호출한다면, 연산자 대신 이름을 사용해라 
                ```kotlin
                Tree.invoke(10) { "$it" }
                ```    
            - but, 이렇게 써버리면 이 호출이 어떤걸 의미하는지 파악하기 더 어려워짐. top-level function으로 만드는게 더 나음
- 정리하자면, fake constructor가 꼭 필요하다면, 이는 standard top-level function으로 만들자

**Methods on factory classes**
- factory class와 관련된 패턴들
    - abstract factory, prototype...
- factoruy class의 장점
    - 클래스가 상태를 가질 수 있음
        ```kotlin
        data class Student(
            val id: Int,
            val name: String, val surname: String
        )

        class StudentsFactory {
            var nextId = 0
            fun next(name: String, surname: String) = Student(nextId++, name, surname)
        }
        ```    
        ```kotlin
        val factory = StudentsFactory()
        val s1 = factory.next("Marcin", "Moskala")
        println(s1) // Student(id=0, name=Marcin, Surname=Moskala)
        val s2 = factory.next("Igor", "Wojda")
        println(s2) // Student(id=1, name=Igor, Surname=Wojda)
        ```    
        - 상태를 가지기 때문에 객체 생성을 최적화할 수도 있다. 
            - 캐싱하거나, 객체를 복사하여 생성 속도를 높이거나, 
            - 논리적으로 분리하여 코드 구성을 잘 하거나
                - 객체 생성 시 여러 service나 repository가 필요하다면, 이를 팩토리 클래스로 추출할 수도 있고!
                    ```kotlin
                    class UserFactory(
                        private val uuidProvider: UuidProvider,
                        private val timeProvider: TimeProvider,
                        private val tokenService: TokenService,
                    ) {
                        fun create(newUserData: NewUserData): User {
                            val id = uuidProvider.next()
                            return User(
                                id = id,
                                creationTime = timeProvider.now(), 
                                token = tokenService.generateToken(id), 
                                name = newUserData.name,
                                ...
                            )
                        }
                    }
                    ```                

<br/>

## :small_blue_diamond: Item 34: Consider a primary constructor with named optional arguments
- 주 생성자를 이용하여 객체를 생성할 때, 인자에 이름을 붙이면 어떤 인자인지 좀 더 명확하게 알 수 있다

<br/>

## :small_blue_diamond: Item 35: Consider defining a DSL for complex object creation
- DSL<sub>Domain Specific Language</sub>을 이용해 복잡한 객체나 객체 계층 구조를 정의할 수 있다. 정의하기 쉽지는 않지만, 이를 이용한다면 복잡한 코드를 숨길 수 있다는 장점이 있다

Defining your own DSL
- DSL을 이해하려면 리시버<sub>receiver</sub>가 있는 function type을 이해해야 한다
    - few examples of function types
        - `() -> Unit` 
            - Function with no arguments that returns Unit
        - `(Int) -> Unit`
        - `(Int) -> Int`
        - `(Int, Int) -> Int`
            - two arguments of type Int and returns Int
        - `(Int) -> () -> Unit` 
            - Function that takes Int and returns an other function. This other function has no arguments and returns Unit
        - `(() -> Unit) -> Unit` 
            - Function that takes another function and returns Unit. This other function has no arguments and returns Unit
    - function type 의 인스턴스를 생성하는 방범
        - lambda expressions
        - anonymous functions
        - function references
- function type은 '함수를 나타내는 객체'를 표현하는 수단이다        
    - 프로퍼티의 타입이 명확한 경우, 람다식과 익명 함수의 argument type을 유추할 수 있다 <sub>익명 함수는 이름 없는 함수, 람다식은 익명 함수를 짧게 표기한 것</sub>
        ```kotlin
        fun plus(a: Int, b: Int) = a + b

        val plus1: (Int, Int) -> Int = { a, b -> a + b}
        val plus2: (Int, Int) -> Int = fun(a, b) = a + b
        val plus3: (Int, Int) -> Int = ::plus
        ```
    - argument type이 명확한 경우, 함수 타입을 정의할 수 있다
        ```kotlin
        val plus4 = { a: Int, b: Int -> a + b }
        val plus5 = fun(a: Int, b: Int) = a + b
        // val plus5 = fun(a: Int, b: Int): Int = a + b
        ```
    - anonymous extension function
        - `val myPlus = fun Int.(other: String) = this.toString() + other`
            - `myPlus`의 타입은 '리시버가 있는 함수' 타입<sub>function type with a receiver</sub>이다. 
                - 일반 함수와 비슷하게 생겼지만, '인자 앞에 `.`으로 구분된' 리시버 타입(`Int`)이 선언돼있다
        - 이 타입은 람다식<sub>lambda expression with receiver</sub>으로 표현할 수도 있다
            - `val myPlus: Int.(String) -> String = { this.toString() + it }`
        - 사용 예
            ```kotlin
            fun main() {
                val x = 3

                val y1 = x.myPlus1("a")
                val y2 = x.myPlus2("a")
                val y3 = x.myPlus3("a")
                val y4 = x.myPlus4("a")

                // 위와 같이 일반적인 extension function 사용하듯이 사용할 수도 있고, 
                // invoke 메서드를 호출해서 사용할 수도 있고
                val z1 = myPlus2.invoke(x, "a")
                // non-extension function 처럼 사용할 수도 있다
                val r1 = myPlus2(x, "a")
            }

            fun Int.myPlus1(other: String) = this.toString() + other
            val myPlus2 = fun Int.(other: String) = this.toString() + other
            val myPlus3: Int.(String) -> String = fun Int.(other: String) = this.toString() + other
            val myPlus4: Int.(String) -> String = { this.toString() + it }        
            ```
        - 참고
            - `item35/AnonymousExtension2.kt`
            - `item35/DslEx.kt`

When should we use DSLs?
- DSL은 명확하고 구조화된 방식으로, 정보를 빠르게 전달할 때 사용할 수 있다 
- DSL은 보일러플레이트<sub>boilerplate</sub> 제거에 적합하다. 만약 반복적인 보일러 플레이트 코드가 있고 이를 해결해 줄 코틀린 기능이 없다면, 그땐 DSL 사용을 고려해 보자
- 하지만 익숙하지 않은 이에겐 복잡도를 증가시키므로 잘 판단해서 써라~ 

> boilerplate code
> - Repeatable code that does not contain any important information for a reader.

<br/>

## :small_blue_diamond: Item 36: Prefer composition over inheritance
- 상속<sub>inheritance</sub>은 `is a` 관계인 객체의 계층 구조를 생성하도록 설계되었다
    - 슈퍼 클래스용으로 작성된 TC는, 해당 서브 클래스들로도 모두 통과해야 한다 == LSP<sub>Liskov Substitution Principle</sub> 
- 만약 단순히 코드 추출이나 재사용이 필요하다면, 상속보단 합성<sub>composition</sub>을 사용하자
    - Composition is more secure
        - 클래스 구현 방식까지 알 필요 없고, 외부에 보이는 행위에만 의존하면 된다
    - Composition is more flexible
        - 상속은 1개밖에 못하지만, composition은 1개 이상도 가능하다
        - 상속을 한다면 우리는 슈퍼 클래스의 모든 것을 가지게 되지만, composition은 내가 필요한 것만 취하면 된다
            - 상속의 경우, 슈퍼 클래스가 변동되면 이의 모든 서브 클래스들이 영향을 받게 된다 
                - 특정 서브 클래스만 영향받도록 하기 어려운 구조!
    - Composition is more explicit
        - 상속을 이용한다면 슈퍼 클래스의 메서드를 내 클래스 내 정의된 메서드처럼 사용할 수 있지만, 누군가 봤을 때 이 메서드 출처가 내껀지 슈퍼껀지 extension인지 알기 어려움
        - composition은 메서드 출처를 알기 쉬움
    - Composition is more demanding
        - 상속은 슈퍼 클래스에 기능을 추가하면 하위 클래스에서도 쓸 수 있음. 하지만 composition은 직접 조정해 줘야 한다 .. ?
    - Inheritance gives us strong polymorphic behavior
        - 슈퍼클래스는 계약을 정의하고, 서브클래스는 이를 respect 한다
            - e.g. Dog을 Animal로 treat할 수 있는 건 편리하지만, 그만큼 행동에 제약을 가져다준다. Animal의 모든 서브 클래스들은 Animal의 모든 행동과 일치해야 하기 때문이다
- e.g. 상속을 이용한 방법
    ```kotlin
    class ProfileLoader {
        fun load () {
            // show progress bar
            // load profile
            // hide progress bar
        }
    }

    class ImageLoader {
        fun load () {
            // show progress bar
            // load image
            // hide progress bar
        }
    }
    ```
    - 위 코드를 리팩터링한다면, 아래와 같이 추상 클래스에 공통 로직을 모아두는 방식으로 해결할 수도 있다
    ```kotlin
    abstract class LoaderWithProgressBar {
        fun load () {
            // show progress bar
            action()
            // hide progress bar
        }

        abstract fun action()
    }

    class ProfileLoader2 : LoaderWithProgressBar() {
        override fun action() {
            // load profile
        }
    }
    class ImageLoader2 : LoaderWithProgressBar() {
        override fun action() {
            // load profile
        }
    }
    ```
    - 하지만 이 접근 방식은 몇 가지 단점이 있다
        - 오직 하나의 클래스만 확장 가능
            - 상속을 사용하여 기능을 추출하면, 계층이 지나치게 복잡해지거나 많은 기능이 축적된 거대한 BaseXXX 클래스가 탄생할 수도 있다
        - 확장 시, 슈퍼 클래스의 모든 것을 가져와야 함
            - 필요치 않는 '정보와 기능'을 가진 클래스가 돼버릴 수 있다(ISP<sub>interface segregation principle</sub> 위반)
                - e.g. 
                    ```kotlin
                    // Dog라는 추상 클래스가 있다
                    abstract class Dog {
                        open fun bark() {/* ... */ }
                        open fun sniff() {/* ... */ }
                    }
                    ```
                    ```kotlin
                    // RobotDog은 sniff기능이 필요없어서 아래와 같이 구현했다면?
                    class RobotDog : Dog() {
                        override fun sniff() {
                            throw Error("Operation not supported")
                        }
                    }
                    ```                
                    - `RobotDog`은 필요하지 않은 메서드를 가지고 있으므로 ISP를 위반하고,  
                    상위 슈퍼 클래스의 동작을 지원하지 않게 구현했기 때문에 LSP도 위반했다
                    > - 상속 대신 composition을 이용하면, 우리가 원하는 재사용 항목만 선택할 수 있다     
        - 슈퍼 클래스의 기능을 확장하는 것은 덜 명시적임 
            - 메서드 동작을 알기 위해 슈퍼 클래스까지 따라가야하는 불필요한 일은 리소스 낭비,,,
- e.g. composition을 이용한 방법
    ```kotlin
    class ProgressBar {
        fun show() { /* show progress bar */ }
        fun hide() { /* hide progress bar */ }
    }

    class ProfileLoader3 {
        val progressBar = ProgressBar()
        fun load() {
            progressBar.show()
            // load profile
            progressBar.hide()
        }
    }
    class ImageLoader3 {
        val progressBar = ProgressBar()
        fun load() {
            progressBar.show()
            // load image
            progressBar.hide()
        }
    }
    ```
    - 상속보단 좀 더 복잡하긴 하지만, 
    - 이 코드는 progressbar라는게 **사용되고** 있고, **어떻게 사용되고** 있는지 알 수 있다
    - 그리고 progressbar 동작 방식을 수정할 수도 있다

Inheritance breaks encapsulation
- 상속 시, 외부에 보여지는 부분 외 내부 구현도 신경써야 한다. 이는 곧 캡슐화를 깨트리는 상황
- e.g.
    ```kotlin
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
    ```
    - `allAll`에서 3을 기대했지만 실제론 6이 찍혔다. 그 이유는 `HashSet#addAll`은 내부적으로 `HashSet#add`를 호출하고 있기 때문에, `elementsAdded += elements.size`에서 3이 증가됐고 이후 `addAll`에서 요소의 수 만큼 `add`가 호출되어 `elementsAdded++`가 총 3번 호출됐기 때문에, 결과적으로 `elementsAdded`는 6이 된 것이다  
    - 이를 해결하기 위해 delegation pattern을 적용해보자
        - 위임 패턴은 대상 클래스가 인터페이스를 implements하고, 동일한 인터페이스의 객체를 멤버로 구성하고, 인터페이스에 정의된 메서드를 이 멤버 객체로 전달한다는 것
            - 이 방법을 forwarding method라고도 부름
        ```kotlin
        class CounterSet2<T> : MutableSet<T> {
            private val innerSet = HashSet<T>()
            var elementsAdded: Int = 0
                private set

            override fun add(element: T): Boolean {
                elementsAdded++
                return innerSet.add(element)
            }

            override fun addAll(elements: Collection<T>): Boolean {
                elementsAdded += elements.size
                return innerSet.addAll(elements)
            }

            override val size: Int
                get() = TODO("Not yet implemented")

            // 이하 override해야하는 8개 메서드들
        }        
        ```
        - 위 코드는 `MutableSet`의 메서드를 전부 override해줘야 하는 문제점이 있다. 
        - 하지만 kotlin은 인터페이스 delegation을 지원하는 기능이 있다!!
            ```kotlin
            class CounterSet3<T>(
                private val innerSet: MutableSet<T> = mutableSetOf()
            ) : MutableSet<T> by innerSet {
                var elementsAdded: Int = 0
                    private set

                override fun add(element: T): Boolean {
                    elementsAdded++
                    return innerSet.add(element)
                }

                override fun addAll(elements: Collection<T>): Boolean {
                    elementsAdded += elements.size
                    return innerSet.addAll(elements)
                }
            }
            ```
            - 이 위임 방식 역시 이게 완전 좋다기보단, 사용되기 적절하다고 판단될 때 사용하자
                - 대부분 다형성이 필요 없거나, 위임 없이 composition만으로도 해결 가능할 테니,,
- composition pattern으로 해결하면 재사용하기 쉽고, 유연성을 높일 수 있다 

Restricting overriding
- 기본적으로 클래스나 메서드는 final 상태이기 때문에, 상속으로 허용하고 싶다면 `open` modifier를 붙여줘야 한다 
- 상속이 필요할 경우에만 `open`하자 

<br/>

> OOP에선 상속보다 composition을 권장하고,   
> 코틀린은 이에 대한 지원을 더욱 강력하게 한다. 예를 들면 클래스나 메서드의 기본이 final인 것, interface delegation을 first-class citizen으로 만들었다는 것 등.  

<br/>

## :small_blue_diamond: Item 37: Use the data modifier to represent a bundle of data
- data class는 bundle of data를 전달할 때 사용한다
- class에 `data` modifier를 추가하면 몇 가지 기능이 추가된다
    - `toString`
    - `equals` and `hashCode`
        - `equals`는 주 생성자의 프로퍼티 값이 모두 일치하는지 체크 (동등성 체크)
        - `hashCode`는 동일성 체크
    - `copy`
        - 주 생성자의 프로퍼티와 동일한 값을 가지는 객체를 만들어냄. 단 이름있는 인수를 사용하여 다른 값을 입력할 수도 있음
        - 주 생성자에 없는 데이터는 복사 안됨! 
    - `componentN` <sub>`component1`, `component2`, etc.</sub>
        - `N` 함수로 프로퍼티에 값을 넣을 수 있음
            - e.g. `val (id, name, pts) = player`
                ```kotlin
                // After compilation
                val id: Int = player.component1()
                val name: String = player.component2()
                val pts: Int = player.component3()                
                ```
        - 이 position-based destructuring은 아래와 같이 활용할 수 있다
            ```kotlin
            val visited = listOf("China", "Russia", "India")
            val (fist, second, third) = visited
            println("$first, $second, $third") // China, Russia, India
            ```
            ```kotlin
            val trip = mapOf(
                "China" to "Tianjin", "Russia" to "Petersburg", "India" to "Rishikesh"
            )
            for ((country, city) in trip) {
                println("We loved $city in $country")
                // We loved Tianjin in China
                // We loved Petersburg in Russia
                // We loved Rishikesh in India
            }
            ```
            ```kotlin
            val (odd, even) = numbers.partition { it % 2 == 1 }
            val map = mapOf(1 to "San Francisco", 2 to "Amsterdam")
            ```
            - 이 표현식은 유용해보이기도 하지만 헷갈리게도 하니까 적절하게 써라 
- tuple 대신 data class을 더 활용해보자            
- `Pair`나 `Triple`도 data class로 구현되어 있다
- data class를 destructure할 땐, 변수와 인자 이름을 일치시켜 헷갈리지 않도록 하자 

<br/>

## :small_blue_diamond: Item 38: Use function types or functional interfaces to pass operations and actions
- SAM<sub>single abstract method</sub>은 인터페이스가 하나의 메서드를 갖고 있는 형태
    - e.g.
        ```kotlin
        // SAM
        interface OnClick {
            fun onClick(view: View)
        }
        ```
        ```kotlin
        // OnClick을 인자로 받는 메서드
        fun setOnClickListener(listener: OnClick) {/* ... */}
        ```
        ```kotlin
        // (실제 사용 시)
        // 인터페이스를 구현한 오브젝트를 인자로 넘겨줘야함
        setOnClickListener(object : OnClick {
            override fun onClick(view: View) {/* ... */}
        })
        ```
- 코틀린은 SAM보다 유용한 아래 매커니즘을 지원한다
    - function type
        - e.g. `fun setOnClickListener(listener: (View) -> Unit) {/* ... */}`
    - functional interface
- 위 2가지 매커니즘을 사용한다면, 인자는 아래 형태로 정의할 수 있다
    - lambda expression
    - anonymous function
    - function reference or bounded function reference
    - function type을 구현한 object
    - functional interface 
- function type이 복잡하다면, alias를 붙일 수도 있다
    - e.g. `typealias OnClick = (View) -> Unit`
    - 타입에 제네릭도 사용 가능
- functional interface는 자바<sub>또는 다른 언어</sub> 상호 운용성과 좀 복잡한 기능 구현 시 사용하면 좋다?

<br/>

## :small_blue_diamond: Item 39: Use sealed classes and interfaces to express restricted hierarchies
- 구체적인 계층 구조에선 `sealed class`나 `sealed interface`를 쓰자
    - 미래에 변경될 수 있다고 하더라도, 현재 계층 구조가 고정되어 있다면!
- e.g. 
    ```kotlin
    sealed class ValueChange<out T>
    object Keep : ValueChange<Nothing>() // 클래스가 상태값을 갖고 있지 않다면, object로 선언하자! 싱글턴임을 나타내는 modifier
    object SetDefault : ValueChange<Nothing>()
    object SetEmpty : ValueChange<Nothing>()
    class Set<out T>(val value: T) : ValueChange<T>()
    ```
- sealed class는 abstract class이고, 아래와 같은 제약 사항을 갖고 있다
    - sealed class들은 동일한 패키지나 모듈에 선언돼있어야 한다
        - sealed 라는 modifer를 붙인 녀석들을 제어할 수 있어야 하기 때문. 라이브러리에 있는 sealed를 클라이언트맘대로 제어해버릴 수 없도록,,
    - local이나 anonymous object로 선언할 수 없다
        - sealed 클래스에 속한 계층 구조는 제한적이기 때문. 밑에서 사부작사부작 자식을 만들어놓으면 제어하기 어려워서
- 상속이 필요하다면 `abstract`를, 하위 클래스에 대한 제어가 필요하다면 `sealed`를 쓰자 
- `when`표현식과 조합이 좋음 

<br/>

## :small_blue_diamond: Item 40: Prefer class hierarchies instead of tagged classes
- tagged class보다는 `sealed class`등을 사용해 계층 구조로 풀어내자 
    - 사용되지 않는 변수까지 떠맡아야 할 필요가 없음
    - 책임이 얽혀있지 않아 깔끔

> 한 클래스 내에서 분기문 같은 걸로 여러 서브 클래스를 생성하거나 관리하는 형태를 tagged라 표현하는 듯
> - 참고 `item40.TaggedClass.kt`, `item40.SealedClass.kt`

<br/>

## :small_blue_diamond: Item 41: Use enum to represent a list of values
- 종류가 구체적이거나, 상수 셋을 표현하고자 할 땐 enum을 쓰자
    - 구체적인 상수값 집합을 나타내는데 최적

상태값
- 각 enum은 enum class의 인스턴스이므로, 상태값을 가질 수 있다 
    ```kotlin
    enum class PaymentOption {
        CASH,
        CARD,
        TRANSFER;

        // This is a global mutable state,
        // so generally not the best practice (see Item 1)
        var commission: BigDecimal = BigDecimal.ZERO
    }

    fun main() {
        val c1 = PaymentOption.CARD
        val c2 = PaymentOption.CARD
        print(c1 == c2) // true,
        // because it is the same object

        c1.commission = BigDecimal.TEN
        print(c2.commission) // 10
        // because c1 and c2 point to the same item
        
        val t = PaymentOption.TRANSFER
        print(t.commission) // 0,
        // because commission is per-item
    }
    ```
    - 위 예시처럼 var 대신, 아래와 같이 주 생성자를 이용하자
        ```kotlin
        enum class PaymentOption(val commission: BigDecimal) {
            CASH(BigDecimal.ONE),
            CARD(BigDecimal.TEN),
            TRANSFER(BigDecimal.ZERO)
        }
        ```
        ```kotlin
        PaymentOption.CARD.commission
        val paymentOption: PaymentOption = PaymentOption.values().random()
        ```

메서드
- enum은 추상 메서드를 선언할 수도 있는데, 그럼 각 인스턴스에선 이를 override 해줘야함
    ```kotlin
    enum class PaymentOption(val commission: BigDecimal) {
        CASH(BigDecimal.ONE) {
            override fun printAccount(description: String) {/* ... */}
        },
        ...;

        abstract fun printAccount(description: String)
    }
    ```
    - 참고 `item41.PaymentOption`
- 위 방식보단, 아래처럼 주 생성자에 function type을 받도록 하는게 더 유용함
    ```kotlin
    enum class PaymentOption2(val commission: BigDecimal, val printAccount: (String) -> Unit) {
        CASH(BigDecimal.ONE, ::println),
        ...
    }
    ```
    - 참고 `item41.PaymentOption2`
- 아니면 아래처럼 extension function을 이용하는 방법도 있음 
    ```kotlin
    fun PaymentOption3.printAccount(description: String) {
        when (this) {
            PaymentOption3.CASH -> println(description)
            PaymentOption3.CARD -> println(description)
            PaymentOption3.TRANSFER -> println(description)
        }
    }
    ```
    - 참고 `item41.PaymentOption3`, `item41.EnumTestKt#printAccount` 

지원되는 함수
- `values()`나 `enumValueOf()`를 이용해 enum 항목을 가져올 수 있음

장점
- `Comparable`를 구현하고 있음
- `toString`, `hashCode`, `equals`를 구현함
- 직렬화/역직렬화 쉬움

<br/>

> Enum or a sealed class?
> - enum class는 구체적인 값 집합을, sealed class는 구체적인 클래스의 집합을 나타낸다
> - 클래스는 object로 선언할 수 있기 때문에, enum 대신 sealed를 쓸 순 있지만, sealed 대신 enum을 쓸 순 없다

<br/>

## :small_blue_diamond: Item 42:  Respect the contract of `equals`
equality
- 2가지 종류의 equality가 있다
    - structural equality
        - 동등성
        - `equals`나 `==`<sub>`!=`</sub>으로 체크 가능
            - e.g. `a == b`, `a.equals(b)`, 만약 a가 nullable하다면 `a?.equals(b) ?: (b === null)`
    - referential equality
        - 동일성
        - `===`<sub>`!==`</sub>로 체크 가능
    - e.g. 
        ```kotlin
        class NameA(val name: String)
        data class NameB(val name: String)

        fun main() {
            val name1 = NameA("isoo")
            val name2 = NameA("isoo")
            val name1Ref = name1

            println(name1 == name1) // true
            println(name1 == name2) // false
            println(name1 == name1Ref) //true

            println(name1 === name1) // true
            println(name1 === name2) // false
            println(name1 === name1Ref) // true

            val name3 = NameB("isoo")
            val name4 = NameB("isoo")
            val name3Ref = name3

            println(name3 == name3) // true
            println(name3 == name4) // true
            println(name3 == name3Ref) // true

            println(name3 === name3) // true
            println(name3 === name4) // false
            println(name3 === name3Ref) // true
        }
        ```
- `data class`는 equals를 재정의하고 있는데, 필요에 따라 커스텀하게 쓸 수도 있다 
    - 커스텀하게 사용할 '동등성' 기준이 있다면, 그에 맞춰 equals를 override 하면됨
        - 주 생성자에 없는 데이터롤 비교해야한다거나,,

The contract of equals
- `equal to`가 성립된다는 건, 아래 상황을 만족한다는 뜻 
    - non-null인 `x`에 대해 `x.equals(x) == true`
        - `x.equals(null) == false`
    - non-null인 `x`, `y`에 대해 `x.equals(y) == y.equals(x) == true`
        - 여러 번 호출해도 동일한 결과여야 함
    - non-null인 `x`, `y`, `z`에 대해 `x.equals(y) == y.equals(z) == true`이면 `x.equals(z) == true`

<br/>

> 커스텀하게 equals를 만들어내는 건 쉬운 일이 아니니. 웬만하면 주어진 data class를 쓰도록 하자    

<br/>

## :small_blue_diamond: Item 43: Respect the contract of `hashCode`
- `hashCode`함수는 `Hashtable` 자료구조를 사용하고 있다 

Hashtable
- 동일한 입력값은 동일한 결과값을 반환해주는 hash function을 이용해 빠르게 요소를 찾을 수 있다
- 해시 결과는 배열 형태에 인덱스로 저장해둔다
- `LinkedHashSet`이나 `LinkedHashMap`에도 사용되고 있음

The problem with mutability
- 해싱을 하기 때문에, 요소 값에 변경이 있어도 해시테이블 내에선 그 사실을 알 수 없음. 즉 요소에 변경이 있다면 `LinkedHashSet`나 `LinkedHashMap`은 기대했던대로 동작하지 않는다
- e.g.
    ```kotlin
    data class FullName(
        var name: String,
        var surname: String
    )

    fun main() {
        val fullNameSet = mutableSetOf<FullName>()

        val person = FullName("ISOO", "CHO")
        fullNameSet.add(person)

        person.name = "AGNES"
        println("person: $person") // person: FullName(name=AGNES, surname=CHO)
        println(fullNameSet.contains(person)) // false
        println(fullNameSet.first() == person) // true
    }
    ```
    -  mutable한 객체는 '해시 기반의 데이터 구조'나 'mutable한 속성을 기반으로 요소를 구성하는 다른 데이터 구조'에 사용되어서는 안 되는 이유! `Set`이나 `Map`의 key로 mutable 요소를 사용해서는 안 되며, 이 자료 구조에 속한 요소를 변형해서는 안 된다.

The contract of hashCode
- 동일한 객체를 한 번 이상 호출해도, `equals`에 사용되는 정보가 수정되지 않는 한 `hashCode()`는 동일한 integer를 반환해야 한다
    - `hashCode`의 일관성이 유지되어야 한다는 뜻
- 두 객체가 `equals == true`일 때, 각각의 `hashCode()` 결과값은 동일한 integer 값이어야 한다
    - `hashCode`의 결과는 항상 `equal`의 결과와 일치해야 하며<sub>hashCode같으면 equals도 같아야 함</sub>, `equal`와 `hashCode`는 동일한 요소로 비교해야 한다
- e.g. 
    ```kotlin
    class FullName2(
        var name: String,
        var surname: String
    ) {
        override fun equals(other: Any?): Boolean =
            other is FullName2
                    && other.name == name
                    && other.surname == surname
    }

    fun main() {
        val fullName2Set = mutableSetOf<FullName2>()
        fullName2Set.add(FullName2("ISOO", "CHO"))

        val y = FullName2("ISOO", "CHO")
        println(y in fullName2Set) // false  (FullName2가 hashCode도 재정의하면, true 나옴)(equals만 재정의했기 때문에, set의 key에선 y의 hashCode가 다르다고 판단하고 false를 리턴한 것임)
        println(y == fullName2Set.first()) // true
    }
    ```

Implementing hashCode
- 웬만하면 스스로 재정의하지 말고 data class를 사용하도록 하고, 재정의가 꼭 필요하다면 IDE가 만들어주는 걸로 쓰자
    - 괜히 잘못만들면 충돌이 발생해, hash를 쓰는 이점이 없어짐

<br/>

## :small_blue_diamond: Item 44: Respect the contract of `compareTo`
- `compareTo`는 비교를 위한 operator이다.
    ```kotlin
    obj1 > obj2 // translates to obj1.compareTo(obj2) > 0
    obj1 < obj2 // translates to obj1.compareTo(obj2) < 0
    obj1 >= obj2 // translates to obj1.compareTo(obj2) >= 0
    obj1 <= obj2 // translates to obj1.compareTo(obj2) <= 0
    ```
- <sub>`Any`가 제공해주는 메서드는 아니고</sub> `Comparable<T>` 인터페이스에 위치해있다 
- 객체가 이 인터페이스를 구현하고 있거나, `compareTo` 라는 이름을 가진 인자 하나를 받는 메서드를 갖고 있다면, 해당 객체는 정렬이 가능하다는 뜻이다 
    - `a >= b` and `b >= a`이면, `a == b` 
    - `a >= b` and `b >= c`이면, `a >= c`
        - `a > b` and `b > c`, then `a > c`
    - 두 요소를 비교할 땐 `a >= b` or `b >= a`의 관계성을 가지고 있어야 한다 
- e.g.
    ```kotlin
    class User(val name: String, val surname: String)

    fun main() {
        val names = listOf(
            User("GA", "KIM"),
            User("SEA", "PARK"),
        )
        val sorted = names.sortedBy { it.surname }
        val sorted2 = names.sortedWith(compareBy({ it.surname }, { it.name }))
    }
    ```
    - 이렇게 구현할 수도 있음
        ```kotlin
        class User2(val name: String, val surname: String) {
            companion object {
                val DISPLAY_ORDER = compareBy(User2::surname, User2::name)
            }
        }
        ```
        - 참고 `item44.User2`

Implementing `compareTo`
- `compareValues`
    - 2개의 값을 비교할 때
    - e.g. `compareValues(surname, other.surname)` 
- `compareValuesBy`
    - 2개 이상의 값을 비교할 때
    - e.g. `compareValuesBy(this, other, { it.surname }, { it.name })`
- 반환값은 아래와 같이 해석하면 된다
    - `0`
        - receiver와 다른 receiver가 동일한 경우
    - positive number
        - receiver가 다른 receiver보다 클 경우
        - e.g. `override fun compareTo(other: T)` 에서 this가 other보다 클 때
    - negative number
        - receiver가 다른 receiver보다 작으면

<br/>

## :small_blue_diamond: Item 45: Consider extracting non-essential parts of your API into extensions
- memeber로 선언할 것인지, extension으로 선언할 것인가
    ```kotlin
    // Defining methods as members
    class Workshop {
        fun makeEvent(date: LocalDate) {/* ... */}

        val permalink
            get() = "/workshop"
    }
    ```
    ```kotlin
    // Defining methods as extensions
    class Workshop {/* ... */}
    fun Workshop.makeEvent(date: LocalDate) {/* ... */}

    val Workshop.permalink
        get() = "/workshop"

    ```
- member와 extensioin 차이
    - extension은 import 해야 한다
    - member의 우선 순위가 높다
    - extension은 클래스에 선언한 것이 아닌, 타입에 해당한다
    - extension은 클래스 참조에 나열되지 않는다
- extension은 유연함을 제공하지만, 상속이나 annotation processing을 지원하지 않는다. 그래서 호출 시 혼란스러울 수도 있음
- API에서 필수 요소는 member로 선언하되, 필수 요소가 아닌건 extension으로 추출해보자

<br/>

## :small_blue_diamond: Item 46: Avoid member extensions
- 가시성을 제한하기 위해 member extension을 하지 말자
    - 실제론 가시성을 제한하지 않는다!
        - 클래스 내부에 위치하게 한다고 해서 가시성이 제한되는게 아니라는 뜻 
    - 필요하다면 멤버로 설정하지 말고, extension 자체의 가시성 제한자를 수정해라 

Why to avoid extension functions
- <sub>function</sub> reference를 지원하지 않음
- receiver들의 암묵적인 접근으로 인해, 어떤 게 어떤 값인지 몰라 혼동할 수 있다
- 직관적이지 않음

Avoid, not prohibit
- DSL 등에선 member extension이 필요하다. 그러니 무조건 금하지 말고, 꼭 필요한 상황이 아니라면 피하자 

<br/>

## :small_blue_diamond: Item 47: Avoid unnecessary object creation
- JVM에서는 동일한 문자열을 은 동일 가상 머신 내에선 재사용된다
    ```kotlin
    val str1 = "Lorem ipsum dolor sit amet"
    val str2 = "Lorem ipsum dolor sit amet"
    print(str1 == str2) // true
    print(str1 === str2) // true
    ```
- Boxed primitives<sub>Integer, Long</sub>도 특정 범위는 재사용된다
    - e.g. Integer Cache는 -128 에서 127까진 캐싱하고 있다
        ```kotlin
        val i1: Int? = 1
        val i2: Int? = 1
        println(i1 == i2) // true
        println(i1 === i2) // true, because i2 was taken from cache

        val j1: Int? = 1234
        val j2: Int? = 1234
        println(j1 == j2) // true
        println(j1 === j2) // false
        ```
        - 위 예시는 `Integer`로 컴파일되게 하려고 일부러 nullable한 객체로 만든 것임. nonnull한 Int로 선언하면 `int`로 컴파일됨(primitive `int`는 null일 수 없기 때문)

Is object creation expensive?
- 객체로 래핑하는데 들어가는 리소스
    - 객체는 추가적인 고간이 필요하다
    - 요소가 캡슐화될 때 액세스에는 추가 함수 호출이 필요하다
    - 객체는 메모리에서 생성 및 할당되어야 하며, 참조도 생성되어야 한다

객체 생성 비용을 줄여보자
- Object declaration
    - 객체를 매번 생성하지말고 재활용하는 좋은 예는, singleton으로 사용하는 것이다 
- Factory function with a cache
    - 생성자가는 호출될 때마다 새 객체를 생성한다. 팩토리 함수를 이용해 동일한 객체<sub>EmptyList같은..</sub>를 반환하도록 구현해보자 
- Lazy initialization
    - 헤비한 클래스를 만들 땐 지연 초기화를 이용해보자 
- Using primitives

<br/>

## :small_blue_diamond: Item 48: Use the inline modifier for functions with parameters of functional types
- inline modifier를 쓰면, 컴파일러가 컴파일하면서 이 함수를 본문에 넣어준다 
    - e.g. `repeat(10) { print(it) }` --> `for (index in 0 until 1-) { print(it) }`
    - 일반 함수는 함수가 호출되면 해당 함수로 이동해서 로직 수행하는 식이지만, 위 방법은 이와 다르다
        - 이러한 동작의 장점
            - A type argument can be reified
            - Functions with functional parameters are faster when they are inlined
            - Non-local return is allowed

A type argument can be reified
- 함수를 inline으로 만들면 함수가 <sub>호출된 부분에서</sub>본문으로 대체되기 때문에, type parameter가 사용된 부분을 reified modifier를 이용해 대체할 수 있다
- e.g.
    ```kotlin
    fun <T> printTypeName() {
        print(T::class.simpleName) // ERROR: Cannot use 'T' as reified type parameter. Use a class instead.
    }

    inline fun <reified T> printTypeName() {
        print(T::class.simpleName)
    }
    ```
    ```kotlin
    // usage
    printTypeName<Int>()
    printTypeName<String>()
    ```
    - 컴파일된 코드를 보면, 함수가 호출된 부분이 '함수 본문'으로 대체됐고, type parameter는 인자값으로 대체됨
    ```java
    String var1 = Reflection.getOrCreateKotlinClass(Integer.class).getSimpleName();
    System.out.print(var1);

    var1 = Reflection.getOrCreateKotlinClass(String.class).getSimpleName();
    System.out.print(var1);
    ```

Functions with functional parameters are faster when they are inlined
- function은 inline 될 때 약간 더 빠르다. execution 되고 실행 완료 후 backstack할 때 추적할 필요가 없기 때문
    - stdlib에 자주 사용되는 작은 함수들이 종종 인라인화되는 이유
- e.g.
    ```kotlin
    // repeat 함수가 repeatNoinline 보다 미세하지만 조금 더 빠름
    inline fun repeat(times: Int, action: (Int) -> Unit) {
        for (index in 0 until times) {
            action(index)
        }
    }

    fun repeatNoinline(times: Int, action: (Int) -> Unit) {
        for (index in 0 until times) {
            action(index)
        }
    }
    ```
    - `item47.Avoid unnecessary object creation`를 기억하자. 함수 본문을 객체로 wrapping하면 코드 속도가 더 느려진다
- 지역 변수는 non-inline lambda에서 직접 사용할 수 없다. 그래서 아래와 같이 사용하면, 컴파일됐을 때 지역 변수`a`가 reference object로 매핑된다
    ```kotlin
    var a = 1L
    repeatNoinline(100_000_000) { a += it }
    ```
    ```java
    final Ref.LongRef a = new Ref.LongRef();
    a.element = 1L;
    repeatNoinline(100000000, (Function1)(new Function1() {
        // $FF: synthetic method
        // $FF: bridge method
        public Object invoke(Object var1) {
        this.invoke(((Number)var1).intValue());
        return Unit.INSTANCE;
        }

        public final void invoke(int it) {
        Ref.LongRef var10000 = a;
        var10000.element += (long)it;
        }
    }));
    ```

Non-local return is allowed
```kotlin
fun main() {
    repeatNoinline(10) {
        println(it)
//        return // ERROR: 'return' is not allowed here
    }

    repeat(10) {
        println(it)
        return
    }
}
```
- `repeatNoinline`는 코드가 다른 클래스에 있어 `main` 함수 내에선 리턴할 수 없다. 하지만 `repeat`는 가능하다. 컴파일될 때 함수가 `main`에 인라이닝 되기 때문
    ```java
    ReifiedTestKt.repeatNoinline(10, (Function1)null.INSTANCE);

    int times$iv = true;
    int $i$f$repeat = false;
    byte index$iv = 0;
    int var5 = false;
    System.out.println(index$iv);
    ```

Costs of inline modifiers
- 인라인 함수는 재귀가 불가능
- 인라인 함수엔 가시성이 제한된 요소를 사용할 수 없음
    - private이나 internal 함수/프로퍼티를 public inline 함수에 쓸 수 없음
- 코드를 거대화시킬수도 있음
    - 인라인 함수를 호출할 때마다 해당 부분에 본문으로 함수가 인라이닝되므로, 코드가 누적될 수 있음!

Crossinline and noinline
- 함수를 인라이닝하고 싶지만, 제약사항이 필요할 땐 아래와 같은 modifier를 써보자
    - `crossinline` 
        - 함수는 인라이닝되어야 하지만, non-local return을 허용하지 않을 때
        - 이 함수가 non-local return이 허용되지 않는 다른 스코프<sub>e.g. 인라이닝되지 않는 또다른 lambda</sub>에서 사용될 때 주로 사용
            - non-local return이 허용되지 않는 컨텍스트에서 인라인 기능을 수행하려고 할 떄,,
        - e.g.
            ```kotlin
            public inline fun <T, R : Comparable<R>> Sequence<T>.sortedBy(crossinline selector: (T) -> R?): Sequence<T> {
                return sortedWith(compareBy(selector))
            }
            ```
    - `noinline` 
        - 이 인자는 인라이닝되지 않아야 할 때
        - 인라이닝되지 않는 다른 함수에 인수로, 이 함수를 사용할 때 주로 사용
        - e.g.
            ```kotlin
            public inline fun CharSequence.replace(regex: Regex, noinline transform: (MatchResult) -> CharSequence): String =
                regex.replace(this, transform)
            ```

<br/>

## :small_blue_diamond: Item 49: Consider using inline value classes
- 함수 뿐만 아니라 '하나의 프로퍼티만을 가진 클래스'도 inline으로 만들 수 있다. 이를 사용하려면 아래와 같이!
    ```kotlin
    @JvmInline // 코틀린의 다른 버전(Kotlin/JS, Kotlin/Native)과의 호환성을 위해 선언하는 애너테이션
    value class Name(private val value: String) { // value modifier를 이용
        fun greet() {
            println("Hello, I'm $value")
        }
    }
    ```
    - 사용하려면
        ```kotlin
        val name = Name("ISOO")
        name.greet()
        ```
        - 컴파일된 모습은
            ```kotlin
            String name = Name.constructor-impl("ISOO");
            Name.greet-impl(name);
            ```
            - 정적 메서드처럼 표현됨 
            - 인라인된 `value class`는 성능 오버헤드없이 wrapper 로 만들 수 있다
                - 위 예시를 보면 `Name.greet-impl()`가 인자로 String을 받는 메서드로 컴파일됨
                - `value class`를 인자로 받는 곳은, 컴파일될 때 클래스를 받는게 아니라 '`value class`의 하나뿐인 프로퍼티' 타입으로 받게 된다는 것
                - e.g. `fun acceptNameInlineClass(n: Name) {}`  
                --(컴파일)--> `public static final void acceptNameInlineClass_(@NotNull String n) {}`
- `value class` 사용처
    - To indicate a unit of measure
        - e.g. `time: Int` 이런 식으로 파라미터를 받으면, 이게 second인지 ms인지 알기 어려움. 물론 파라미터 이름을 명확하게 하는 방법으로 해결할 수도 있지만, 더 좋은 해결 방법은 `value class`를 이용해 효율적이며 좀 더 엄격한 형태로 만드는 것
            ```kotlin
            @JvmInline value class Minutes(val minutes: Int) {}
            @JvmInline value class Millis(val milliseconds: Int) {}
            interface Timer {
                fun callAfter(timeMillis: Millis, callback: () -> Unit) 
                    // 이 함수를 호출할 때, timeMillis에 Millis타입이 들어오지 않으면 Type mismatch Error가 발생하게 만듦
            }
            ```            
    - To use types to protect users from value misuse
        - e.g.
            ```kotlin
            @Entity(name = "grades")
            class Grades(
                @Column(name = "studentId")
                val studentId: Int,
                @Column(name = "teacherId")
                val teacherId: Int,
                ...
            )
            ```
            - 위 유형에서 Int를 더욱 안전한 형태로 쓸려면 아래와 같이
                ```kotlin
                @JvmInline
                value class StudentId(val studentId: Int)

                @JvmInline
                value class TeacherId(val teacherId: Int)

                @Entity(name = "grades")
                class Grades(
                    @Column(name = "studentId")
                    val studentId: StudentId,
                    @Column(name = "teacherId")
                    val teacherId: TeacherId,
                    ...
                )
                ```
                - 어차피 컴파일될 때 `Int`로 대체되니까,, 
- 인라이닝되지 않는 상황
    - `value class`는 implememts interface가 가능하지만, 그럼 인라이닝되는 이점을 누릴 수 없음. 오브젝트가 인터페이스로 사용된다면 인터페이스로 해당 타입을 나타내야 해서 래핑된 객체를 만들기 때문에 인라이닝될 수 없음.         
    - `value class`를 파라미터로 받을 때, 해당 파라미터가 nullable하다면.. 

> - `value class`는, 타입을 좀 더 엄격하게 관리하여 오남용을 막기 위해 사용되는 듯      
    
<br/>

> - type alias를 쓰면 긴 타입을 간결하게 나타낼 수 있지만, 이것이 실제 코드에서 문제점을 더 빨리 찾아낼 수 있는지는 잘 생각해 보고 쓰자..       

<br/>

## :small_blue_diamond: Item 50: Eliminate obsolete object references
- 더 이상 사용되지 않거나 필요하지 않는 자원은 해제하여 누수를 막자
- 읽기 힘든 코드는 메모리 누수를 찾기 어렵게 만든다 
- 캐시를 붙인다면 소프트 참조<sub>soft reference</sub>를 사용하자 
- 메모리 누수를 막으려면 변수를 로컬 범위<sub>item2: Minimize the scope of variables</sub>에 정의하고, 최상위 수준 속성이나 객체 선언(동반 객체 포함)에 무거운 데이터를 저장하지 않는 것

<br/>

## :small_blue_diamond: Item 51: Prefer Sequences for big collections with more than one processing step
- `Iterable`과 `Sequence`의 차이
    ```kotlin
    // iterable
    public inline fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T> {
        return filterTo(ArrayList<T>(), predicate)
    }

    // sequence
    public fun <T> Sequence<T>.filter(predicate: (T) -> Boolean): Sequence<T> {
        return FilteringSequence(this, true, predicate)
    }
    ```
    - Iterable은 매 단계에서 <sub>`List`같은</sub> 컬렉션을 반환한다
    - Sequence는 지연 처리<sub>lazy</sub>하기 때문에 중간 연산을 별도로 계산해두지 않는다. 대신 연산이 끝날 때마다 새로운 시퀀스를 반환한다.  
        - lazy 이점
            - natural order를 유지한다
            - 최소한의 operation을 수행
            - infinite 가능
            - 연산마다 컬렉션 생성 안 함

Order is important
- 작업 순서
    ```kotlin
    listOf(1, 2, 3)
        .filter { print("F$it "); it % 2 == 1 }    // F1 F2 F3 
        .map { print("M$it "); it * 2 }            // M1 M3 
        .forEach { print("E$it ") }                // E2 E6 

    sequenceOf(1, 2, 3)
        .filter { print("F$it "); it % 2 == 1 }    
        .map { print("M$it "); it * 2 }               
        .forEach { print("E$it ") }                // F1 M1 E2 F2 F3 M3 E6     
    ```
    - Iterable 
        - 모든 요소를 스텝바이스텝으로 연산에 적용한다<sub>eager order</sub>
        - e.g. 첫 번째 연산에 모든 요소를 적용하고, 그 다음 연산에 모든 요소를 적용하고...
    - Sequence
        - 한 요소를 전체 연산에 적용한다<sub>lazy order</sub>
        - e.g. 한 요소를 전체 연산에 적용하고, 그 다음 연산을 전체 연산에 적용하고...
- 위 예시를 만약 classic loop로 나타냈다면, 그 연산 결과는 Sequence와 동일하게 나온다. ~~그러니 시퀀스의 처리 결과가 natural order라고 볼 수 있다~~

Sequences do the minimal number of operations
- 중간 연산이 있고, find와 같이 먼저 발견되면 연산이 종료되는 경우, 시퀀스를 사용하는 것이 더 효율적일 수 있다. 
    - 이에 해당하는 종단 연산의 예로는 `first`, `find`, `take`, `any`, `all`, `none`, `indexOf` 등이 있다
- e.g. 
    ```kotlin
    val iterable = (1..10)
        .filter { print("F$it "); it % 2 == 1 } // F1 F2 F3 F4 F5 F6 F7 F8 F9 F10
        .map { print("M$it "); it * 2 }            // M1 M3 M5 M7 M9
        .find { print("D$it "); it > 5 }          // D2 D6

    val sequence = (1..10).asSequence()
        .filter { print("F$it "); it % 2 == 1 }
        .map { print("M$it "); it * 2 }
        .find { print("D$it "); it > 5 }          // F1 M1 D2 F2 F3 M3 D6
    ```
    - 종단 연산이 `5 이상인 요소 하나를 찾아라 == find {it > 5}`이므로, lazy로 요소 하나하나를 루프 돌리는게 이득 

Sequences can be infinite
- `generateSequence`나 `sequence` 등을 이용해 무한 시퀀스를 생성할 수 있다 
    ```kotlin
    generateSequence(1) { it + 1 }
            .map { it * 2 }
            .take(10)
            .forEach { print("$it ") } // 2 4 6 8 10 12 14 16 18 20     
    ```
- 단, 요소 수를 제한하는 함수를 꼭 넣어줄 것! 안 그럼 무한 반복...

Sequences do not create collections at every processing step
- 일반적인 컬렉션 처리 기능은, 연산 시 새 컬렉션을 반환한다. 이는 이 단계 이후 곧바로 컬렉션을 사용할 수 있다는 이점은 있지만, 그만큼 비용이 발생함 
- 컬렉션이 많은 요소를 담고 있다면(혹은 요소들을 담고 있는 컬렉션이 무겁다면) 매 연산마다 새 컬렉션을 생성하지 않도록 시퀀스를 이용하는걸 권장함

When aren’t sequences faster?
- 컬렉션의 연산에 `sorted`가 포함된 경우, <sub>드물게</sub> 컬렉션보다 시퀀스 처리가 느릴 수도 있음

<br/>

> Java Stream API가 Sequence와 유사하게 lazy하게 동작함. 
> - 차이점
>   - Java에선 병렬로 Stream 처리 가능
> 
> 병렬처리가 꼭 필요한 경우<sub>병렬 모드에서 연산 수행 시 이득을 얻을 수 있는 처리일 경우</sub>를 제외하곤, Kotlin Stdlib 함수를 이용하자! 

## :small_blue_diamond: Item 52: Consider associating elements to a map
- 고유한 key를 가지는 데이터 요소들은 list 대신 map 형태로 저장하는 게 효율적이다
- 데이터를 수정하거나 반복<sub>iterating</sub>하거나, `filter`, `map`, `flatMap`, `sorted`, `sum` 등등은 map과 list에 큰 차이가 없지만,  
**find**할 때는 key를 기반으로 찾는 게 훨씬 빠르다
- `associateBy`를 이용해 식별자 요소를 지정하여 map을 편리하게 만들 수 있다 
- map의 key가 중복되지 않도록 주의하자
    - 아래 예시에서 사용된 `associateBy`는, 'If any two elements would have the same key returned by keySelector the last one gets added to the map.'
    ```kotlin
    fun main() {
        val users = listOf(User(1, "Michal"), User(2, "Michal"))

        val byName = users.associateBy { it.name }
        println(byName) // {Michal=User(id=2, name=Michal)}

        val group: Map<String, List<User>> = users.groupBy { it.name }
        println(group) // {Michal=[User(id=1, name=Michal), User(id=2, name=Michal)]}
    }

    data class User(val id: Int, val name: String)
    ```
- map에서 list로 변환하려면 `values` 함수를 이용하면 됨
    - `users.associateBy { it.id }.values`
- map은, 해당 컬렉션에 자주 access할 때 '성능상 유의미함'을 보여줌. 만약 list에서 map으로 변환해야 하는 작업이 잦다면 되려 시간이 오래 소요될 수 있으니 잘 판단해서 사용할 것! 

<br/>

## :small_blue_diamond: Item 53: Consider using `groupingBy` instead of `groupBy`
- 성능이 중요할 땐 `groupBy`보다 `groupingBy`
- `groupBy`
    - returns `Map<K, List<V>>`
    - 편의성과 가독성있는 부분은 장점이지만, 카테고리 별 Collection을 만들어 내는데 시간이 다소 소요됨
- `groupingBy`
    - 추가 작업 없이, 지정된 key와 함께 iterable을 wrapping함
    - returns `Grouping<T, K>`
    - `groupBy`보다 사용하기 어렵지만, 성능은 좋음
- e.g. 
    ```kotlin
    // 'user 리스트' 기준으로 각 도시의 사용자 수 count
    val usersCount1: Map<City, Int> = users.groupBy { it.city }
        .mapValues { (_, users) -> users.size }

    val usersCount2 = users.groupingBy { it.city }
        .eachCount()


    // 'player 리스트' 기준으로 각 팀의 득점 합
    val pointsPerTeam1: Map<Team, Int> = players.groupBy { it.team }
        .mapValues { (_, players) ->
            players.sumOf { it.points }
        }

    val pointsPerTeam2 = players.groupingBy { it.team }
        .fold(0) { acc, elem -> acc + elem.points }


    // 'format 리스트'을 기준으로 각 퀄리티 별로 가장 적합한 format 찾기
    val bestFormatPerQuality1: Map<Quality, Resolution> = formats.groupBy { it.quality }
        .mapValues { (_, formats) ->
            formats.maxByOrNull { it.resolution }!!
            // it is fine to use !! here, because
            // this collection cannot be empty
        }

    val bestFormatPerQuality2 = formats.groupingBy { it.quality }.reduce { _, acc, elem ->
        if (acc.resolution > elem.resolution) acc else elem
    }
    ```
    - 중간에 `pointsPerTeam2` 예제는 아래처럼 확장함수로 분리해서 구현할 수도 있음
        ```kotlin
        fun <T, K> Grouping<T, K>.eachSumBy(
            selector: (T) -> Int
        ): Map<K, Int> = fold(0) { acc, elem -> acc + selector(elem) }
        ```
        ```kotlin
        val pointsPerTeam = players
            .groupingBy { it.team }
            .eachSumBy { it.points }
        ```
    - 마지막 `bestFormatPerQuality2` 예제도 아래처럼 확장함수로 분리해서 구현할 수도 있음
        ```kotlin
        inline fun <T, K> Grouping<T, K>.eachMaxBy(
            selector: (T) -> Int
        ): Map<K, T> =
            reduce { _, acc, elem ->
                if (selector(acc) > selector(elem)) acc else elem
            }
        ```
        ```kotlin
        val bestFormatPerQuality = formats.groupingBy { it.quality }
            .eachMaxBy { it.resolution }
        ```
