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
