# **CH10. 애노테이션과 리플렉션**
애노테이션과 리플렉션을 사용하면 이름을 알지 못하는 상황에서 임의의 클래스를 다룰 수 있다.  
애노테이션을 사용하면 라이브러리가 요구하는 의미를 클래스에 부여할 수 있고, 리플렉션을 사용하면 실행 시점에 컴파일러 내부 구조를 분석할 수 있다.  

<br/>

## 목차
- [10.1 애노테이션 선언과 적용](#101-애노테이션-선언과-적용)
- [10.2 리플렉션: 실행 시점에 코틀린 객체 내부 관찰](#102-리플렉션-실행-시점에-코틀린-객체-내부-관찰)

<br/><br/>

---

<br/><br/>

# 10.1 애노테이션 선언과 적용

<br/>

## 10.1 애노테이션 적용
- 자바와 동일한 방식으로 사용한다.  
- 코틀린에선 `@Deprecated` 사용 시, 파라미터로 대체 버전을 제시할 수 있다.
    ```kotlin
    @Deprecated("Use removeAt(index) instead.", ReplaceWith("removeAt(index)"))
    fun remove(index: Int) {...}
    ```
- 애너테이션 인자로 사용 가능한 타입은 아래와 같다.
    - 원시 타입의 값
    - 문자열
    - enum
    - 클래스 참조
    - 다른 애너테이션 클래스
    - 앞서 나열한 타입의 요소들로 이뤄진 배열
    - 임의의 식
        - 안전하지 못한 캐스팅에 대한 경고를 무시할 수 있는 `@Suppress`를 아래와 같이 사용 가능
            ```kotlin
            inline fun <reified T> List<*>.asListOfType(): List<T>? =
                if (all { it is T })
                    @Suppress("UNCHECKED_CAST")
                    this as List<T> else
                    null
            ```

- 애너테이션 인자를 지정하는 특정 문법이 있다.
    - 클래스를 애너테이션 인자로 지정할 땐, 해당 클래스 이름 뒤에 `::class`를 넣어야 한다.
        - e.g. `@MyAnnotation(MyClass::class)`
    - 다른 애너테이션을 인자로 지정할 땐 인자로 들어가는 애너테이션의 이름 앞에 `@`를 넣지 않아야 한다. 
        - e.g. `@ReplaceWith`는 애너테이션이다. 하지만 `@Deprecated` 애너테이션의 인자로 들어가므로 `ReplaceWith` 앞에 `@`를 사용하지 않는다.
            - `@Deprecated("Use removeAt(index) instead.", ReplaceWith("removeAt(index)"))`
    - 배열을 인자로 지정하려면 `arrayOf` 함수를 사용한다. 
        - e.g. `@RequestMapping(path = arrayOf("/foo", "/bar"))`
        - 만약 자바에서 선언한 애너테이션 클래스를 사용한다면 `value`라는 이름의 파라미터가 필요에 따라 자동으로 가변 길이 인자로 변환되므로, `arrayOf` 함수를 쓰지 않아도 된다.            
            - e.g. `@JavaAnnotationWithArrayValue("abc", "foo", "bar")`
    - 애너테이션 인자는 컴파일 시점에 알 수 있어야 한다. 따라서 임의의 프로퍼티를 인자로 지정할 수는 없다. 프로퍼티를 애너테이션 인자로 사용하려면 그 앞에 `const` 변경자를 붙여야 한다.
        - e.g. 
            ```kotlin
            const val TEST_TIMEOUT = 100L

            @Test(timeout = TEST_TIMEOUT) fun testMethod() { ... }
            ```

> *`const`가 붙은 프로퍼티는 파일의 맨 위나 `object` 안에 선언해야 하며, 원시 타입이나 `String`으로 초기화해야 한다.*   
  
<br/>

## 10.1.2 애노테이션 대상
사용 지점 대상<sup>*use-site target*</sup> 선언으로 애너테이션을 붙일 요소를 정할 수 있다.
- 문법
    - `@사용지점대상:애너테이션 이름`
    - e.g. `@get:Rule` ==> 프로퍼티의 `get`함수에 `@Rule` 애너테이션을 적용하겠다.
- 사용 지점 대상에 사용할 수 있는 대상 목록
    | 대상 | 설명 |
    |---|---|
    | `property` | 프로퍼티 전체. 자바에서 선언된 애너테이션에는 이 사용 지점 대상을 사용할 수 없다. |
    | `field` | 프로퍼티에 의해 생성되는 (뒷받침하는) 필드 |
    | `get` | 프로퍼티 게터 |
    | `set` | 프로퍼티 세터 |
    | `receiver` | 확장 함수나 프로퍼티의 수신 객체 파라미터 |
    | `param` | 생성자 파라미터 |
    | `setparam` | 세터 파라미터 |
    | `delegate` | 위임 프로퍼티의 위임 인스턴스를 담아둔 필드 |
    | `file` | 파일 안에 선언된 최상위 함수와 프로퍼티를 담아두는 클래스. <br/> `package` 선언 앞, 파일 최상위 수준에만 적용 가능. <br/> - e.g. `@file:JvmName("StringFunctions)` ==> 파일의 최상위 선언을 담는 클래스 이름을 바꿔주는 `@JvmName` 애너테이션을 `file`에 사용 |  

<br/>

> #### 자바 API를 애너테이션으로 제어하기
> - `@JvmName`
>     - 코틀린 선언이 만들어내는 자바 필드나 메서드 이름 변경
> - `@JvmStatic`
>     - 메서드, 객체 선언, 동반 객체에 적용 시 그 요소가 자바 static 으로 노출됨
> - `@JvmOverloads`
>     - 디폴트 파라미터 값이 있는 함수에 대해 컴파일러가 자동으로 오버로딩한 함수를 생성해줌
> - `@JvmField`
>     - 프로퍼티에 사용 시 getter/setter가 없는 public 자바 필드로 프로퍼티를 노출시킴

<br/>

## 10.1.3 애노테이션을 활용한 JSON 직렬화 제어
> 직렬화<sup>*serialization*</sup>
> - 객체를 저장장치에 저장하거나 네트워크를 통해 전송하기 위해 텍스트나 이진 형식으로 변환하는 것   
> 
> 역직렬화<sup>*deserialization*</sup>
> - 텍스트나 이진 형식으로 저장된 데이터로부터 원래의 객체를 만들어 내는 것

> 자바와 JSON을 변환할 때 주로 사용되는 라이브러리는 [잭슨<sup>Jackson</sup>](https://github.com/FasterXML/jackson)과 [지슨<sup>GSON</sup>](https://github.com/google/gson)이 있다.  
> 이 라이브러리들은 코틀린과도 완전히 호환된다.  
> 그리고 코틀린에는 JSON 직렬화를 위한 [제이키드<sup>jkid</sup>](https://github.com/yole/jkid)라는 순수 코틀린 라이브러리도 있다.  

이번 장에서는 jkid를 사용하여 직렬화를 제어해보자.  
- `@JsonExclude`
    - 해당 애노테이션을 사용하면 직렬화나 역직렬화 시 그 프로퍼티를 무시할 수 있다.
- `@JsonName`
    - 해당 애노테이션을 사용하면 프로퍼티를 표현하는 키/값 쌍의 키로 프로퍼티 이름 대신 애노테이션이 지정한 이름을 쓰게 할 수 있다.

<br/>

## 10.1.4 애노테이션 선언

<br/>

## 10.1.5 메타애노테이션: 애노테이션을 처리하는 방법 제어
- 애너테이션 클래스에 적용할 수 있는 애너테이션을 메타애노테이션<sup>*meta-annotation*</sup>이라고 부른다.
- 표준 라이브러리에서 가장 흔히 쓰이는 메타애노테이션은 `@Target`이다.  
    - `@Target`은 애너테이션을 적용할 수 있는 요소의 유형을 지정한다. 애노테이션 클래스에 대해 구체적인 `@Target`을 지정하지 않으면, 모든 선언에 적용할 수 있는 애노테이션이 된다.

<br/>

## 10.1.6 애노테이션 파라미터로 클래스 사용
- 코틀린에선 클래스에 대한 참조를 저장할 때 `KClass` 타입을 사용한다.  

<br/><br/>

---

<br/><br/>

# 10.2 리플렉션: 실행 시점에 코틀린 객체 내부 관찰
- 리플렉션<sup>*reflection*</sup>을 사용하여 애너테이션에 저장된 데이터에 접근할 수 있다.   
- 리플렉션은 실행 시점에 (<sub>*동적으로*</sub>) 객체의 프로퍼티와 메서드에 접근할 수 있게 해준다.  
- e.g. `JSON` 직렬화 라이브러리는, 어떤 객체든 `JSON`으로 변환할 수 있어야 하고, 실행 시점이 되기 전까진 라이브러리가 직렬화할 프로퍼티나 클래스에 대한 정보를 알 수 없다. 이런 경우 리플렉션을 사용한다.  
- 코틀린에서 리플렉션을 사용하려면 두 가지 서로 다른 리플렉션 API를 다뤄야 한다. 
    1. 자바가 `java.lang.reflect` 패키지를 통해 제공하는 표준 리플렉션
        - 코틀린 클래스는 일반 자바 바이트코드로 컴파일되므로 자바 리플렉션 API도 코틀린 클래스를 컴파일한 바이트코드를 완벽히 지원한다.
    2. 코틀린이 `kotlin.reflect` 패키지를 통해 제공하는 코틀린 리플렉션 API
        - 이 API는 자바에는 없는 프로퍼티나 널이 될 수 있는 타입과 같은 코틀린 고유 개념에 대한 리플렉션을 제공한다. 
        - 하지만 현재 코틀린 리플렉션 API는 자바 리플렉션 API를 완전히 대체할 수 있는 복잡한 기능을 제공하지는 않는다.
  
<br/>

## 10.2.1 코틀린 리플렉션 API: `KClass`, `KCallable`, `KFunction`, `KProperty`

### `KClass`
- `java.lang.Class`에 해당하는 `KClass`를 사용하면 클래스 안에 있는 모든 선언을 열거하고 각 선언에 접근하거나 클래스의 상위 클래스를 얻는 등의 작업이 가능하다.
- `MyClass:class`라는 식을 쓰면 `KClass`의 인스턴스를 얻을 수 있다.
- 실행 시점에서 객체의 클래스를 얻으려면, 
    1. 먼저 객체의 `javaClass` 프로퍼티를 사용해 객체의 자바 클래스를 얻어야 한다. 
        - `javaClass`는 자바의 `java.lang.Object.getClass()` 와 같다.
    2. 자바 클래스를 얻었으면, `.kotlin` 확장 프로퍼티를 이용해 자바에서 코틀린 리플렉션 API로 옮겨올 수 있다.  

```kotlin
import kotlin.reflect.full.memberProperties

class Person(val name: String, val age: Int)

fun main() {
    val person = Person("Alice", 10)
    val kClass = person.javaClass.kotlin    // javaClass를 이용해 Person의 자바 클래스를 얻은 후, .kotlin을 이용해 KClass<Person> 인스턴스를 얻음.  
    println(kClass.simpleName)

    kClass.memberProperties.forEach { println("  ${it.name}") }
}
```
```
Person
  age
  name
```

- `KClass`를 통해 사용할 수 있는 다양한 기능은, 실제로는 `kotlin-reflect`라이브러리를 통해 제공되는 확장 함수들이다.  
    - 이 확장 함수를 사용하려면 `kotlin.reflect.full` 이하 패키지를 import하여야 한다. 
        - 위 예시 참고

### `KCallable`
- 클래스 내 모든 멤버 목록은 `KCallable` 인스턴스의 컬렉션에 해당한다.  
    - 위 예시 `kClass.memberProperties.forEach { println("  ${it.name}") }` 에서 `it`은 `memberProperties`의 요소를 뜻하며, 이 `memberProperties`의 타입이 `Collection<KProperty1<T, *>>`이다. 
- `KCallable`은 함수와 프로퍼티를 아우르는 공통 상위 인터페이스다.
- `KCallable`에는 `call` 메서드가 있으며, 이를 사용하면 함수나 프로퍼티의 getter를 호출할 수 있다.  
```kotlin
fun foo(x: Int) = println(x)

fun main() {
    val kFunctionTest = ::foo   
    kFunctionTest.call(100)     // call을 사용해 함수를 호출할 수 있다 
}
```
```
100
```

### `KFunction` 
- `KFunction`은 위 `KCallable#call`보다 함수를 호출할 때 더 구체적으로 호출할 수 있는 메서드를 제공한다. 
    - `KFunction#invoke`를 이용해 함수를 호출할 수 있다. 
    - `invoke`메서드는 호출할 때 인자 개수나 타입이 정확히 일치하지 않으면 컴파일이 되지 않는다.  
        - 만약 인자 타입과 반환 타입을 모두 알고 있는 경우라면 `invoke`메서드를 호출하자.  
        `call` 메서드는 모든 타입의 함수에 적용할 수 있지만, 타입 안전성을 보장해주진 않는다.  
            ```kotlin
            @Suppress("UNCHECKED_CAST")
            override fun call(vararg args: Any?): R = reflectionCall {
                return caller.call(args) as R
            }
            ```

- 아래와 같이 `KFunction`을 직접 호출할 수 있다.  
    ```kotlin
    import kotlin.reflect.KFunction2

    fun sum(x: Int, y: Int) = x + y

    fun main() {
        val kFunctionTest: KFunction2<Int, Int, Int> = ::sum
        println(kFunctionTest.invoke(1, 2) + kFunctionTest.invoke(3, 4))
    }
    ```
    ```
    10
    ```

### `KProperty`
- `KProperty`의 `call`메서드를 이용해 프로퍼티의 getter를 호출할 수 있다. 
  
<br/>

## 10.2.2 리플렉션을 사용한 객체 직렬화 구현
jkid의 직렬화 함수 선언은 다음과 같다.  
```kotlin
fun serialize(obj: Any): String = buildString { serializeObject(obj) }
```
객체를 받아서 그 객체에 대한 `JSON` 표현을 문자열로 돌려준다.   

> 코드 참고: https://github.com/yole/jkid/blob/master/src/main/kotlin/serialization/Serializer.kt
