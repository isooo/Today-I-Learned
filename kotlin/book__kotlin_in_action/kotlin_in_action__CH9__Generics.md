# **CH9. 제네릭스**

<br/>

## 목차
- [9.1 제네릭 타입 파라미터](#91-제네릭-타입-파라미터)
- [9.2 실행 시 제네릭스의 동작: 소거된 타입 파라미터와 실체화된 타입 파라미터](#92-실행-시-제네릭스의-동작-소거된-타입-파라미터와-실체화된-타입-파라미터)
- [9.3 변성: 제네릭과 하위 타입](#93-변성-제네릭과-하위-타입)
  
<br/><br/>

---

<br/><br/>

# 9.1 제네릭 타입 파라미터
제네릭스를 사용하면 타입 파라미터<sup>*type parameter*</sup>를 받는 타입을 정의할 수 있다. 제네릭 타입의 인스턴스를 만들려면 타입 파라미터를 구체적인 타입 인자<sup>*type argument*</sup>로 치환해야 한다.

- 코틀린 컴파일러는 보통 타입과 마찬가지로 타입 인자도 추론할 수 있다.
    ```kotlin
    val authors = listOf("Dmitry", "Svetlana")
    ```
- 빈 리스트를 만들어야 한다면 타입 인자를 직접 명시해야 한다.
    ```kotlin
    // 아래 두 선언은 동등하다
    val readers: MutableList<String> = mutableListOf()  // 변수 타입을 지정 
    val readers = mutableListOf<String>()               // 변수를 만드는 함수의 타입 인자를 지정
    ```

<br/>

## 9.1.1 제네릭 함수와 프로퍼티
리스트를 다루는 함수를 작성할 때, 어떤 특정 타입을 저장하는 리스트가 아닌 불특정 타입의 리스트를 다룰 수 있는 함수인 제네릭 함수를 만들 수 있다.   
  
아래 제네릭 함수인 `slice`는 `T`를 타입 파라미터로 받는다.   
```kotlin
fun <T> List<T>.slice(indices: IntRange): List<T> {...}
```
첫번째 `<T>`는 타입 파라미터로 선언하기 위함이다.    
두번째와 세번째 `<T>`는 수신 객체와 반환 타입에 사용되었다.   
  
```kotlin
val letters = ('a'..'z').toList()
println(letters.slice<Char>(0..2))  // 타입 인자를 명시적으로 지정
println(letters.slice(0..2))        // 컴파일러가 T타입을 Char로 추론
```
```
[a, b, c]
[a, b, c]
```

<br/>

- 클래스나 인터페이스 안에 정의된 메소드, 확장 함수 또는 최상위 함수에서 타입 파라미터를 선언 할 수 있다.  
- 제네릭 함수를 정의할 때와 마찬가지 방법으로 제네릭 확장 프로퍼티를 선언할 수 있다.
    ```kotlin
    val <T> List<T>.penultimate: T  // 모든 리스트 타입에 제네릭 확장 프로퍼티를 사용할 수 있다.
        get() = this[size - 2]

    fun main() {
        println(listOf(1, 2, 3, 4).penultimate) // 이때 타입 파라미터 T는 Int로 추론된다.  
    }
    ```
    ```
    3
    ```
    - 확장 프로퍼티만 제네릭하게 만들 수 있다. 일반 프로퍼티는 타입 파라미터를 가질 수 없다. 

<br/>

## 9.1.2 제네릭 클래스 선언
자바와 마찬가지로 코틀린에서도 타입 파라미터를 넣은 꺽쇠 기호(`<>`)를 클래스 이름 뒤에 붙이면 클래스를 제네릭하게 만들 수 있다.  
타입 파라미터를 이름 뒤에 붙이고 나면 클래스 본문 안에서 타입 파라미터를 다른 일반 타입처럼 사용할 수 있다.

```kotlin
interface List<T> { // List 인터페이스에 T라는 타입 파라미터를 정의한다.
    operator fun get(index: Int): T // 인터페이스 안에서 T를 일반 타입처럼 사용할 수 있다.
    ...
}
```

<br/>

## 9.1.3 타입 파라미터 제약
타입 파라미터 제약<sup>*type parameter constraint*</sup>은 클래스나 함수에 사용할 수 있는 타입 인자를 제한하는 기능이다.
(e.g. `sum`이라는 함수를 `List<Int>`나 `List<Double>`은 적용할 수 있지만 `List<String>`에는 적용되지 않도록, `sum` 함수의 타입 파라미터로는 숫자 타입만 허용하게 정의)   
어떤 타입을 제네릭 타입의 타입 파라미터에 대한 상한<sup>*upper bound*</sup>으로 지정하면 그 제네릭 타입을 인스턴스화할 때 사용하는 타입 인자는 반드시 그 상한 타입이거나 그 상한 타입의 하위 타입이어야 한다.  

```kotlin
// 예시
fun <T : Number> List<T>.sum(): T  // fun <타입 파라미터: 상한타입> List<T>.sum(): T
```

- 타입 파라미터 `T`에 대한 상한을 정하고나면 `T` 타입의 값을 그 상한 타입의 값으로 취급할 수 있다.  
- 타입 파라미터에 대해 둘 이상의 제약을 가해야 하는 경우, 아래와 같이 구현하면 된다.  
    ```kotlin
    fun <T> ensureTrailingPeriod(seq: T) where T : CharSequence, T : Appendable {
        if (!seq.endsWith('.')) {
            seq.append('.')
        }
    }

    fun main() {
        val helloWorld = StringBuilder("Hello World")
        ensureTrailingPeriod(helloWorld)
        println(helloWorld)
    }    
    ```
    ```
    Hello World.
    ```

<br/>

## 9.1.4 타입 파라미터를 널이 될 수 없는 타입으로 한정
제네릭 클래스나 함수를 정의하고 그 타입을 인스턴스화할 때는 nullable한 타입을 포함하는 어떤 타입으로 타입 인자를 지정해도 타입 파라미터를 치환할 수 있다. 아무런 상한을 정하지 않은 타입 파라미터는 결과적으로 `Any?`를 상한으로 정한 파라미터와 같다.  
```kotlin
class Processor<T> {
    fun process(value: T) {
        value?.hashCode() // "value"는 널이 될 수 있으므로 안전한 호출을 사용해야 한다. 
    }
}
```

만약 nullable하지 않은 타입만 인자로 받으려면, 타입 파라미터에 제약을 가해야 한다.  
```kotlin
class Processor<T : Any> {
    fun process(value: T) {
        value.hashCode()
    }
}
```

<br/><br/>

---

<br/><br/>

# 9.2 실행 시 제네릭스의 동작: 소거된 타입 파라미터와 실체화된 타입 파라미터
JVM의 제네릭스는 보통 타입 소거<sup>*type erasure*</sup>를 사용해 구현된다. 이는 **실행 시점**에 **제네릭 클래스의 인스턴스에 타입 인자 정보가 들어있지 않다**는 뜻이다.    
  
함수를 inline으로 만들면 타입 인자가 지워지지 않게 할 수 있는데, 이를 코틀린에선 실체화<sup>*reify*</sup>라 부른다.    

<br/>

## 9.2.1 실행 시점의 제네릭: 타입 검사와 캐스트
자바와 마찬가지로 코틀린 제네릭 타입 인자 정보는 **런타임에 지워진다**.  
이는 **제네릭 클래스 인스턴스가** 그 인스턴스를 생성할 때 쓰인 **타입 인자에 대한 정보를 유지하지 않는다**는 뜻이다.  
   
아래 예시 코드의 두 리스트를 컴파일러는 서로 다른 타입으로 인식하지만, 실행 시점에 그 둘은 완전 같은 타입의 객체다.   
```kotlin
val list1: List<String> = listOf("a", "b")
val list2: List<Int> = listOf(1, 2, 3)
```

이런 타입 소거의 단점은 타입 인자를 따로 저장하지 않기 때문에 실행 시점에 타입 인자를 검사할 수 없다는 것이다.  

<br/>

만약 타입 인자를 명시하지 않고 제네릭 타입을 사용하려면, 스타 프로젝션(star projection; `*`)을 사용하면 된다.  
타입 파라미터가 2개 이상이라면 모든 타입 파라미터에 `*`를 포함시켜야 한다.

```kotlin
fun printSum(c: Collection<*>) {
    val intList = c as? List<Int>
        ?: throw IllegalArgumentException("List is expected")
    println(intList.sum())
}

fun main() {
    printSum(listOf(1, 2, 3))   // 정상동작함
//    printSum(setOf(1, 2, 3))  // IllegalArgumentException: List is expected  ==> List가 아니므로 지정한 예외가 발생
    printSum(listOf("a", "c"))  // ClassCastException   ==> as? 캐스팅에는 성공하지만, String 타입이 아니어서 캐스트 예외 발생
}
```
  
만약 `fun printSum(c: Collection<Int>)` 이렇게 컴파일 시점에 타입 정보를 주고 `listOf("a", "c")`로 테스트하면  
코틀린 컴파일러는 type mismatch라는 컴파일 에러를 발생시켜준다.   

<br/>

## 9.2.2 실체화한 타입 파라미터를 사용한 함수 선언
코틀린의 제네릭 타입의 타입 인자 정보는 실행 시점에 지워진다. 따라서 제네릭 클래스의 인스턴스가 있어도 그 인스턴스를 만들 때 사용한 타입 인자를 알아낼 수 없다.  
```kotlin
fun <T> isA(value: Any) = value is T // 컴파일 에러 발생 --> Cannot check for instance of erased type: T
```
하지만 함수에 inline 키워드를 붙이면, 컴파일러가 그 함수를 호출한 식을 모두 함수 본문으로 바꿔주기 때문에, inline 함수의 타입 파라미터는 실체화되므로 실행 시점에 inline 함수의 타입 인자를 알 수 있다.  
```kotlin
inline fun <reified T> isA(value: Any) = value is T

fun main() {
    println(isA<String>("abc"))
    println(isA<Int>(1))
    println(isA<List<String>>(1))
    println(isA<List<String>>("a"))
    println(isA<List<String>>(listOf("a", "c")))
}
```
```
true
true
false
false
true
```
    
<br/>

## 9.2.3 실체화한 타입 파라미터로 클래스 참조 대신
> `java.lang.Class` 타입 인자를 파라미터로 받는 API에 대한 코틀린 어댑터(adapter)를 구축하는 경우 실체화한 타입 파라미터를 자주 사용한다.  

```kotlin
val serviceImpl = ServiceLoader.load(Service::class.java)
```
위 코드는 아래와 같이 구체화한 타입 파라미터를 이용해 짧게 구현할 수 있다. 
```kotlin
// T::class로 타입 파라미터의 클래스를 가져올 수 있다.
// reifed 로 타입 파라미터를 명시한다
inline fun <reified T> loadService() {
    return ServiceLoader.load(T::class.java)
}

fun main() {
    val serviceImpl = loadService<Service>()
}
```

<br/>

## 9.2.4 실체화한 타입 파라미터의 제약
- 실체화한 타입 파라미터를 사용할 수 있는 경우
    - 타입 검사와 캐스팅(`is`, `!is`, `as`, `as?`)
    - 10장에서 설명할 코틀린 리플렉션 API(`::class`)
    - 코틀린 타입에 대응하는 `java.lang.Class`를 얻기(`::class.java`)
    - 다른 함수를 호출할 때 타입 인자로 사용

- [ ] 실체화한 타입 파라미터가 하지 못하는 일
    - 타입 파라미터 클래스의 인스턴스 생성하기
    - 타입 파라미터 클래스의 동반 객체 메소드 호출하기
    - 실체화한 타입 파라미터를 요구하는 함수를 호출하면서 실체화하지 않은 타입 파라미터로 받은 타입을 타입 인자로 넘기기
    - 클래스, 프로퍼티, 인라인 함수가 아닌 함수의 타입 파라미터를 `reified`로 지정하기

<br/><br/>

---

<br/><br/>

# 9.3 변성: 제네릭과 하위 타입
변성<sup>*variance*</sup> 개념은 `ist<String>`와 `List<Any>`와 같이 기저 타입이 같고 타입 인자가 다른 여러 타입이 서로 어떤 관계가 있는지 설명하는 개념이다.   
변성을 잘 활용하면 사용에 불편하지 않으면서 타입 안전성을 보장하는 API를 만들 수 있다.  
  
<br/>

## 9.3.1 변성이 있는 이유: 인자를 함수에 넘기기
```kotlin
fun printContents(list: List<Any>) {
    println(list.joinToString())
}

fun main() {
    printContents(listOf("a", "cb"))
}
```
```
a, cb
```
위 코드는 잘 동작한다. 
`printContents`함수는 각 원소를 `Any`로 취급하고, `String`은 `Any`타입이기도 하므로 안전하게 수행된다.  
  
하지만 아래의 경우는 다르다.  
```kotlin
fun addAnswer(list: MutableList<Any>) {
    list.add(42)
}

fun main() {
    val strings = mutableListOf("a", "cb")
    addAnswer(strings)  // 컴파일 에러 발생 --> Type mismatch.
}
```

원소에 추가나 변경이 없는 경우엔 `List<String>`을 `List<Any>` 대신 넘겨도 안전하지만,  
리스트의 원소에 변동이 일어날 함수에는 `List<String>`을 `List<Any>` 대신 넘길 수 없다.    

<br/>

## 9.3.2 클래스, 타입, 하위 타입
- 제네릭 클래스가 아닌 클래스에서는 클래스 이름을 바로 타입으로 쓸 수 있다.
    - e.g. `var x: String`
- 제네릭 클래스에서는 올바른 타입을 얻으려면 제네릭 타입의 타입 파라미터를 구체적인 타입 인자로 바꿔줘야 한다.
    - e.g. `List`는 타입이 아니다(클래스다). 타입 인자를 치환한 `List<Int>`, `List<String>` 등은 제대로 된 타입이다.  
- 어떤 타입 `A`의 값이 필요한 모든 장소에 어떤 타입 `B`의 값을 넣어도 아무 문제가 없다면 `B`는 타입 `A`의 하위 타입<sup>*subtype*</sup>이다. 
    - ==> `B`가 `A`보다 구체적
- 상위 타입<sup>*supertype*</sup>은 하위 타입의 반대다.
- 하위 타입은 하위 클래스<sup>*subclass*</sup>와 근본적으로 같다.
    - e.g. `Int`클래스는 `Number`의 하위 클래스이므로 `Int`는 `Number`의 하위 타입이다. 
    - 단, 널이 될 수 있는 타입은 하위 타입과 하위 클래스가 같지 않음.  
- 어떤 인터페이스를 구현하는 클래스의 타입은 그 인터페이스 타입의 하위 타입이다.
    - e.g.`String`은 `CharSequence`의 하위 타입이다.  
- 제네릭 타입을 인스턴스화 할 때 타입 인자로 서로 다른 타입이 들어가면 인스턴스 타입 사이의 하위 타입 관계가 성립하지 않으면 그 제네릭 타입을 **무공변**<sup>*invariant*</sup>이라고 한다.
- 코틀린의 `List` 인터페이스는 읽기 전용 컬렉션을 표현한다. `A`가 `B`의 하위 타입이면 `List<A>`는 `List<B>`의 하위 타입이다. 그런 클래스나 인터페이스를 **공변적**<sup>*covariant*</sup>이라고 한다.

<br/>

## 9.3.3 공변성: 하위 타입 관계를 유지
`Producer<T>`라는 클래스가 있다고 가정하자.  
`A`가 `B`의 하위 타입일 때 `Producer<A>`가 `Producer<B>`의 하위 타입이면 `Producer`는 **공변적**이다. 이를 `하위 타입 관계가 유지된다`고 말한다.   
- e.g. `Cat`은 `Animal`의 하위 타입이기 때문에 `Producer<Cat>`은 `Producer<Animal>`의 하위 타입이다.  

코틀린에서 제네릭 클래스가 타입 파라미터에 대해 공변적임을 표시하려면 타입 파라미터 이름 앞에 `out`을 넣어야 한다  
```kotlin
interface Producer<out T> { // 클래스가 T에 대해 공변적이라고 선언
    fun produce(): T
}
```

<br/>

```kotlin
open class Animal {
    fun feed() {...}
}

class Herd<T : Animal> {  // 이 타입 파라미터를 무공변성으로 지정한다. 
    val size: Int get() = ...
    operator fun get(i: Int): T {...}
}

fun feedAll(animals: Herd<Animal>) {
    for (i in 0 until animals.size) {
        animals[i].feed()
    }
}

// 사용자 코드가 고양이 무리를 만들어서 관리한다. 
class Cat : Animal() {  // Cat은 Animal이다
    fun cleanLitter() {...}
}

fun takeCareOfCats(cats: Herd<Cat>) {
    for (i in 0 until cats.size) {
        cats[i].cleanLitter()
        // feedAll(cats)  // ==> Type mismatch 컴파일 에러 발생. Required:Herd<Animal> , Found:Herd<Cat>
    }
}
```

`feedAll`함수가 컴파일 에러 나는 이유는  
`Herd`클래스에서 `T`타입 파라미터에 대해 아무 변성도 지정하지 않았기 때문이다. 그래서 Cat은 Animal의 하위 클래스가 아니다!  
아래와 같이 `Herd`를 공변적인 클래스로 만들고, 호출하는 코드를 변경하면 오류를 해결할 수 있다.  
```kotlin
class Herd<out T : Animal> {    // T는 이제 공변적이다
   ...
}

fun takeCareOfCats(cats: Herd<Cat>) {
    for (i in 0 until cats.size) {
        cats[i].cleanLitter()
    }
    feedAll(cats)  
}
```

<br/>

모든 클래스를 공변적으로 만들 수는 없다. 공변적으로 만들면 안전하지 못한 클래스도 있다.  
타입 파라미터를 공변적으로 지정하면 클래스 내부에서 그 파라미터를 사용하는 방법을 제한한다. 타입 안정성을 보장하기 위해 공변적 파라미터는 항상 아웃<sup>*out*</sup>위치에 있어야 한다. 이는 클래스가 `T` 타입의 값을 생산할 수는 있지만 `T`타입의 값을 소비할 수는 없다는 뜻이다.  
클래스 멤버를 선언할 때 타입 파라미터를 사용할 수 있는 지점은 모두 인<sup>*in*</sup>과 아웃<sup>*out*</sup>위치로 나뉜다
- e.g. `T`라는 타입 파라미터를 선언하고 `T`를 사용하는 함수가 멤버로 있는 클래스 
    - `T`가 함수 반환 타입에 쓰인다면 `T`는 아웃<sub>*out*</sub> 위치에 있다. 그 함수는 `T` 타입의 값을 생산<sub>*produce*</sub>한다. 
    - `T`가 함수 파라미터 타입에 쓰인다면 `T`는 인<sub>*in*</sub> 위치에 있다. 그 함수는 `T` 타입의 값을 소비<sub>*consume*</sub>된다.

> 타입 파라미터 `T`에 붙은 `out` 키워드의 의미
> - **공변성**: 하위 타입 관계가 유지된다.
>     - e.g. `Producer<Cat>`은 `Producer<Animal>`의 하위 타입이다.  
> - **사용 제한**: `T`를 `out` 위치에서만 사용할 수 있다.

<br/>

> `List<T>` 인터페이스는 읽기 전용이다. 따라서 여기엔 `T` 타입의 원소를 **반환**하는 `get` 메서드는 있지만, 인(in) 위치에 `T`를 쓰며 리스트에 있는 기존 값을 변경하거나 리스트에 값을 추가하는 메서드는 없다.  
> 따라서 `List`는 `T`에 대해 **공번젹이다**.

<br/>

## 9.3.4 반공변성: 뒤집힌 하위 타입 관계
반공변성<sub>*contravariance*</sub>은 공변성의 반대다. 반공변 클래스의 하위 타입 관계는 공변 클래스의 경우와 반대다.  
  
`Comparator<T>`는 `T` 타입을 소비하기만 한다. 
```kotlin
interface Comparator<in T> {
    fun compare(e1: T, e2: T): Int {...}    // T를 in 위치에 사용
}
```

<br/>

`Consumer<T>`를 예로 들어 설명하자. 
타입 `B`가 타입 `A`의 하위 타입인 경우 `Consumer<A>`가 `Consumer<B>`의 하위 타입인 관계가 성립하면 제네릭 클래스 `Consumer<T>`는 타입 인자 `T`에 대해 반공변이다.
- e.g. `Consumer<Animal>`은 `Consumer<Cat>`의 하위 타입이다.  

<br/>

| 공변성 | 반공변성 | 무공변성 |
| - | - | - |
| `Producer<out T>` | `Consumer<in T>` | `MutableList<T>` |
| 타입 인자의 하위 타입 관계가 제네릭 타입에서도 유지된다. | 타입 인자의 하위 타입 관계가 제네릭 타입에서 뒤집힌다. | 하위 타입 관계가 성립하지 않는다. |
| `Producer<Cat>`은 `Producer<Animal>`의 하위 타입이다. | `Producer<Animal>`은 `Producer<Cat>`의 하위 타입이다. |  |
| `T`를 아웃 위치에서만 사용할 수 있다. | `T`를 인 위치에서만 사용할 수 있다. | `T`를 아무 위치에서나 사용할 수 있다. |

<br/>

## 9.3.5 사용 지점 변성: 타입이 언급되는 지점에서 변성 지정
클래스를 선언하면서 변성을 지정하는 방식을 선언 지점 변성<sub>*declaration site variance*</sub>이라고 한다.  
   
<br/>

## 9.3.6 스타 프로젝션: 타입 인자 대신 `*` 사용
제네릭 타입 인자 정보가 없음을 표현하기 위해 스타 프로젝션(star projection; `*`)을 사용한다.   
- e.g. 원소 타입이 알려지지 않은 리스트는 `List<*>`라는 구문으로 표현할 수 있다.

<br/>

-  `MutableList<*>`는 `MutableList<Any?>`와 같지 않다.
    - `MutableList<*>`는 어떤 정해진 구체적인 타입의 원소만을 담는다.
        - 컴파일러는 `MutableList<*>`를 **아웃 프로젝션 타입**으로 인식한다.    
            - e.g. `MutableList<*>`는 `MutableList<out Any?>`처럼 동작한다. 어떤 리스트의 원소 타입을 모르더라도 그 리스트에서 안전하게 `Any?`타입의 원소를 꺼내올 순 있지만(`Any?`는 모든 코틀린의 상위 타입이므로), 타입을 모르는 리스트에 원소를 마음대로 넣을 순 없다.  
    - `MutableList<Any?>`는 모든 타입의 원소를 담을 수 있다.
- 타입 파라미터를 시그니처에서 전혀 언급하지 않거나 데이터를 읽기는 하지만 그 타입에는 관심이 없는 경우와 같이 타입 인자 정보가 중요하지 않을 때도 스타 프로젝션 구문을 사용할 수 있다.
