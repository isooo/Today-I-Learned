# **CH6. 코틀린 타입 시스템**
코틀린은 널이 될 수 있는 타입(nullable type)과 읽기 전용 컬렉션을 제공하여 코드의 가독성을 향상시킨다.    
  
<br/>

## 목차
- [6.1 널 가능성](#61-널-가능성): 널이 될 수 있는 타입과 널을 처리하는 구문의 문법
- [6.2 코틀린의 기본 타입](#62-코틀린의-기본-타입): 코틀린 원시 타입 소개와 자바 타입과 코틀린 원시 타입의 관계
- [6.3 컬렉션과 배열](#63-컬렉션과-배열): 코틀린 컬렉션 소개와 자바 컬렉션과 코틀린 컬렉션의 관계
  
<br/><br/>

---

<br/><br/>

# 6.1 널 가능성
널 가능성(nullablility)은 NPE(NullPointerException)를 피할 수 있게 도와주는 코틀린 타입 시스템 특성이다.  
null이 될 수 있는지를 타입 시스템에 추가해 null 여부를 컴파일러가 감지할 수 있도록 하여, 실행 시점에 발생할 수 있는 예외 가능성을 줄일 수 있다.    

<br/>

## 6.1.1 널이 될 수 있는 타입
코틀린 타입 시스템은 널이 될 수 있는 타입을 명시적으로 지원한다.  

- 만약 함수가 파라미터로 nullable한 값을 받게 하려면, 타입 뒤에 `?`를 명시하면 된다.   
    - e.g. `String?`, `CustomType?`
        ```kotlin
        fun strLen(s: String) = s.length

        fun main() {
            strLen(null)    // 컴파일 에러 발생: Null can not be a value of a non-null type String
        }
        ```


- null이 될 수 있는 타입의 값이 있다면, 그 값으로 수행할 수 있는 연산 종류가 제한된다.
    - nullable한 값이 있는 메서드를 직접 호출할 수 **없다**.
        ```kotlin
        fun strLenSafe(s: String?) = s.length 
                                    // 컴파일 에러 발생 (Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type String?)
        ```        
    - nullable한 값을 nullable하지 않는 타입의 변수에 대입할 수 없다.
        ```kotlin
        val x: String? = null
        val y: String = x  // 컴파일 에러 발생 (Type mismatch)
        ```        
    - nullable 타입의 값을 nullable하지 않는 타입의 변수에 대입할 수 없다.
        ```kotlin
        fun strLen(s: String) = s.length

        val x: String? = "text" 
        strLen(x)   // 컴파일 에러 발생 (Type mismatch)
        ```    
    - 만약 인자가 nullable할 때, null 검사를 추가해주면 null이 될 수 없는 타입의 값처럼 사용할 수 있지만... 이는 코드를 번잡스럽게 만든다.  
  
<br/>

## 6.1.2 타입의 의미
위키피디아에 정의된 타입(data type)
- 분류(classification).
- 어떤 값들이 가능한지 그리고 그 타입에 대해 수행할 수 있는 연산의 종류룰 결정
- 자바의 경우 `String`은 문자열 뿐만아니라 null도 담을 수 있다. 이는 자바의 타입 시스템이 널을 제대로 다루지 못한다는 뜻.  

<br/>

## 6.1.3 안전한 호출 연산자: `?.`
- `?.`는 null검사와 메서드 호출을 한 번의 연산으로 수행한다.  
- `s?.uppercase()` == `if (s != null) s.uppercase() else null`
- `?.`는 null이 아닌 값이면 일반 메서드 호출처럼 작동하고, null이면 이 호출은 무시되고 null이 결과값이 된다.  
- nullable한 타입에 함수 적용 시, 해당 함수의 결과 타입역시 nullable한 타입이다.
    ```kotlin
    fun strLenSafe(s: String?) {
        val returnValue: String? = s?.uppercase()
        println(returnValue == null)
    }

    fun main() {
        strLenSafe("test")  // false
        strLenSafe(null)    // true
    }
    ``` 

- 객체 그래프에서 nullable한 중간 객체가 여럿 있다면, 하나의 식에서 연쇄 호출 시 `?.`를 이용해 편리하고 안전하게 호출할 수 있다.
    ```kotlin
    class Address(val streetAddress: String, val zipCode: Int, val city: String, val country: String)
    class Company(val name: String, val address: Address?)
    class Person(val name: String, val company: Company?)

    fun Person.countryName(): String {
        val country = this.company?.address?.country    // 이 객체 그래프에서 어떤 객체가 null이어도 NPE는 발생하지 않는다. null이면 그때 "Unknown"이 리턴됨.  
        return if (country == null) "Unknown" else country  // country ?: "Unknown" 
    }
    ```

<br/>

## 6.1.4 엘비스 연산자: `?:`
- 엘비스(elvis)연산자는 null 대신 사용할 디폴트 값을 지정할 때 사용할 수 있다.  
- 이 연산자는 **좌항**을 계산한 값이 널이 아니면 **그 계산한 값을 결과 값**으로 하고, **널이라면 우항 값을 결과로** 한다.  
    ```kotlin
    fun foo (s: String?): Int {
        return s?.length ?: 0  // ==> if (s == null) 0 else s.length 
    }
    ```

- 코틀린에선 `return`이나 `throw` 연산은 식이다. 따라서 엘비스 연산자 우항에 이 연산들을 넣어 (예외를 던지는 등으로) 편리하게 사용할 수 있다.  
    ```kotlin
    fun printShippingLabel(person: Person) {
        val address = person.company?.address ?: throw IllegalArgumentException("No address")
        with(address) { 
            println(this.streetAddress) 
            println("$city, $zipCode")
        }
    }

    fun main() {
        val add = Address("street", 0, "city", "c")
        val company1 = Company("company1", add)
        val person1 = Person("abc", company1)
        printShippingLabel(person1)

        println("---")

        val company = Company("company1", null) 
        val person = Person("abc", company)
        printShippingLabel(person)
    }    
    ```
    ```
    street
    city, 0
    ---
    Exception in thread "main" java.lang.IllegalArgumentException: No address
    ```
   
<br/>

## 6.1.5 안전한 캐스트: `as?`
- `as?`는 지정한 타입으로 캐스팅하되, 지정 타입으로 변환할 수 없으면 null을 반환한다.  
    ```kotlin
    class Person(val firstName: String, val lastName: String) {

        override fun equals(other: Any?): Boolean {
            val otherPerson = other as? Person ?: return false // ==> if (other !is Person) return false
            return otherPerson.firstName == firstName && otherPerson.lastName == lastName
        }

        override fun hashCode(): Int = firstName.hashCode() * 37 + lastName.hashCode()
    }

    fun main() {
        val personA = Person("ISOO", "CHO")
        val personB = Person("ISOO", "CHO")

        // "== 연산자"는 "equals()"를 호출함
        println(personA == null)    // false
        println(personA == personB) // true
    }
    ```

<br/>

## 6.1.6 널 아님 단언: `!!`
- 널 아님 단언(not-null assertion)은 어떤 값이든 널이 될 수 없는 타입으로 (강제로) 바꿀 수 있으며, `!!`로 표현한다.   
- 만약 해당 값이 null이라면 NPE가 발생한다.  
    ```kotlin
    fun ignoreNull(s: String?) {
        val notNullString = s!! // 이 지점에서 NullPointerException이 발생함
        println(notNullString.length)
    }

    fun main() {
        ignoreNull(null)
    }
    ```
- 여러 `!!`단언문을 한 줄에 쓰는건 지양하자. 어떤 값이 널이었는지 알기 어려움.
    ```kotlin
    person.company!!.address!!.country // 이렇게 쓰지 마라
    ``` 

<br/>

## 6.1.7 `let` 함수 
- `let` 함수는 `?.`연산자와 함께 사용하여, 원하는 식을 평가해 결과가 null인지 검사한 다음에야 그 결과를 변수에 대입한다.  
- `let` 함수는 자신의 수신 객체를, 인자로 전달받은 람다에게 넘기는데, 이때 람다식은, 전달받은 객체가 널이 아닐 경우에만 수행된다.        
    ```kotlin
    val listWithNulls: List<String?> = listOf("Kotlin", null, "Java")
    for (element in listWithNulls) {
        element?.let { 
            println(">> ${it.uppercase()}") // ele -> println(">> ${ele.uppercase()}")
        } 
    }
    ```
    ```
    >> Kotlin
    >> Java
    ```

<br/>
 
## 6.1.8 나중에 초기화할 프로퍼티
- `lateinit` 변경자를 이용해 **나중에 초기화**(late-initialized)할 수 있다. 
    - 단, 나중에 초기화하는 프로퍼티는 `var` 여야 한다. (`val`는 **final 필드로 컴파일**되며 **생성자 안에서 반드시 초기화해야**함)
- e.g. JUnit 테스트 시 `@Before` 등으로 초기화 값을 대입해주는 경우 등
    ```kotlin
    internal class MyServiceTest {
        // lateinit 변경자를 붙이지 않는다면, 컴파일 에러 발생
        private lateinit var myService: MyService

        @BeforeEach fun setup() {
            myService = MyService()  
        }

        @Test fun test() {
            myService.performAction() 
            // 만약 위에서 초기화 안 해주면, 런타임 에러 발생 
        }
    }
    ```

<br/>

## 6.1.9 널이 될 수 있는 타입 확장
- null이 될 수 있는 수신 객체에 대해 확장 함수를 호출할 수 있다.  
    ```kotlin
    fun verifyUserInput(input: String?) {   // nullable 타입을 인자로 받았지만, 
        if (input.isNullOrBlank()) {        // 이 확장 함수를 호출할 때 null 체크를 해주지 않아도 된다!
            println("Please fill in the required fields")
        }
    }
    ```

<br/>

## 6.1.10 타입 파라미터의 널 가능성
- 함수나 클래스의 모든 '타입 파라미터'는 기본적으로 nullable하다.  
    ```kotlin
    fun <T> printHashCode(t: T) {
        println(t?.hashCode())  // T가 nullable하므로 안전한 호출 연산자 ?. 를 사용헸다.
    }

    fun main() {
        printHashCode(null) // null
    }
    ```

- 타입 파라미터가 null이 아님을 확실히 하려면, null이 될 수 없는 타입으로 지정해주면 된다.  
아래 예시에선 `upper bound`를 지정해주었다.   
    ```kotlin
    fun <T:Any> printHashCode(t: T) {   // T의 타입을 지정해줌.
        println(t.hashCode())
    }

    fun main() {
        printHashCode(null) // 컴파일 에러 발생
    }
    ```

<br/>

## 6.1.11 널 가능성과 자바
- 자바 소스코드에 널 가능성 애너테이션이 없는 경우, 이 (자바) 타입은 코틀린에선 플랫폼 타입(platform type)이 된다.  
  
### **플랫폼 타입**
- 플랫폼 타입은 **코틀린이 널 관련 정보를 알 수 없는 타입**을 뜻한다.  
- 이 타입은 "널이 될 수 있는/업는 타입" 무엇으로도 처리가능하다. (그에 대한 책임은 개발자의 몫)    
- 자바의 [타입] = 코틀린의 ([특정 타입`?`] OR [특정 타입])
  
아래와 같이 `Person` 자바 클래스가 있다. 
```java
public class Person {
    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```
위 클래스의 인스턴스를 만든 후 `getName`했을 때, 이는 null인지 알 수 없다.  
아래와 같이 null에 대한 검사를 하지 않는 `yellAt` 함수가 있고, null검사를 하는 `yellAtSafe`를 만들 수 있다.  
```kotlin
fun yellAtSafe(person: Person) {
    println((person.name ?: "Anyone").uppercase() + "!!!")
}

fun yellAt(person: Person) {
    println(person.name.uppercase() + "!!!")
}

fun main() {
    yellAtSafe(Person(null)) // ANYONE!!!
    yellAt(Person(null)) // java.lang.NullPointerException: person.name must not be null
}
```

<br/>

- 플랫폼 타입을 직접 선언할 수 없다. 자바 코드에서 가져온 타입만 플랫폼 타입이 된다.  

<br/>

### **상속**
- 코틀린에서 자바 메서드를 오버라이드할 때 그 메서드의 파라미터와 반환 타입을 널이 될 수 있는/없는 타입으로 선언할 지 결정해야 한다.  

    ```java
    public interface StringProcessor {
        void process(String value);
    }
    ```
    코틀린 컴파일러는 아래 2가지 구현을 모두 허용한다.  
    ```kotlin
    class StringPrinter: StringProcessor {
        override fun process(value: String) {
            println(value)
        }
    }

    class NullableStringPrinter: StringProcessor {
        override fun process(value: String?) {
            if (value != null) {
                println(value)
            }
        }
    }
    ```


<br/><br/>

---

<br/><br/>

# 6.2 코틀린의 기본 타입
코틀린은 원시 타입과 래퍼 타입을 구분하지 않는다.   
  
<br/>

## 6.2.1 기본 타입: `Int`, `Boolean` 등
- 코틀린은 원시타입과 래퍼 타입을 구분하지 않고, 항상 같은 타입을 사용햔다.  
    ```kotlin
    val i: Int = 1
    val listOf: List<Int> = listOf(1, 2, 3)
    ```

- 코틀린은 숫자 타입 등 원시 타입의 값에 대해 메서드를 호출할 수 있다.
    ```kotlin
    fun showProgress(progress: Int) {
        val percent = progress.coerceIn(0, 100) // 표준라이브러리 함수 coerceIn 함수 사용. min값과 max값을 인자로 받음.
        println("We're ${percent}% done!")
    }

    fun main() {
        showProgress(34)   // We're 34% done!
        showProgress(134)   // We're 100% done!
    }
    ```

<br/>

- 코틀린은 숫자 타입의 경우, 실행 시점에 가장 효율적이라 판단되는 방식으로 표현한다.  
    - `Int`를 사용하였을 때, 이게 컬렉션과 같은 제네릭 클래스를 사용하는 경우를 제외하곤 자바의 `int`타입으로 컴파일 된다.

<br/>

> ### 자바 원시 타입에 해당하는 타입
> - 정수 타입
>     - `Byte`, `Short`, `Int`, `Long`
> - 부동 소수점 수 타입
>     - `Float`, `Double`
> - 문자 타입
>     - `Char`
> - 불리언 타입
>     - `Boolean`

<br/>

## 6.2.2 널이 될 수 있는 기본 타입: `Int?`, `Boolean?` 등
- 코틀린에선 nullable한 원시 타입을 사용하면,  자바의 래퍼 타입으로 컴파일된다.  
    ```kotlin
    val i: Int? = null
    val i2: Int = 0
    val i3: Int = 100
    val i4: Int = 1000
    ```
    위 코드를 컴파일 한 후, 그 `.class`를 `.java`로 디컴파일해보면 아래와 같다.      
    ```java
    Integer i = null;
    int i2 = 0;
    int i3 = 100;
    int i4 = 1000;
    ```

<br/>

- 아래 코드는 null 값이나 null이 될 수 있는 타입을 전혀 사용하지 않았지만, 저 list는 wrapper인 `Integer` 타입으로 저장된다.  
    ```kotlin
    val listOf = listOf(1, 2, 3)
    ```

<br/>

## 6.2.3 숫자 변환
- 코틀린은 한 타입의 숫자를 다른 타입의 숫자로 자동 변환하지 않는다. 
    ```kotlin
    val i = 1
    val j:Long = i  // 컴파일 에러 발생(type mismatch)
    ```

- 모든 원시 타입(`Boolean`은 제외)에 대해 변환 함수를 제공한다. 
    - `toByte()`, `toChar()`, `toShort()` 등
    ```kotlin
    val x = 1
    val list = listOf(1L, 2L)
    println(x in list)  // 컴파일 에러 발생 (Type inference failed)
    println(x.toLong() in list) // true
    ```

<br/>

- 아래와 같이 산술 연산자는 별도의 변환 없이도 코드가 잘 동작하도록 오버로드돼 있다.  
    ```kotlin
    val a: Long = 1 
    val b: Byte = 2
    println(a + b)
    ```
    ```java
    // 디컴파일 코드
    long a = 1L;
    byte b = 2;
    long var3 = a + (long)b;
    System.out.println(var3);
    ```

<br/>

## 6.2.4 `Any`, `Any?`: 최상위 타입
- 코틀린에서 `Any`는 `모든 널이 될 수 없는 타입의 조상 타입`이다.    
    - 원시 타입을 모두 포함한 타입의 조상 타입이다.    
- `Any`는 null이 될 수 없는 타입이다. 만약 널을 포함하는 모든 값을 대입하는 변수를 선언하려면 `Any?` 타입을 사용해야 한다.  
- `Any`는 `java.lang.Object`에 대응한다.  
    - 자바 메서드에서 `Object`를 인자로 받거나 반환할 경우, 코틀린에선 `Any`로 그 타입을 취급한다
        - 더 정확히 말하면 널이 될 수 있는지 여부를 알 수 없으므로 플랫폼 타입인 `Any!`로 취급한다.  
- 모든 코틀린 클래스는 `toString()`, `equals()`, `hashCode()` 이 3가지 메서드가 들어있는데, 이 메서드들은 `Any`에 정의된 메서드를 상속한 것이다.  
- `java.lang.Object`에 있는 다른 메서드(`wait()`, `notify()`)는 `Any`에선 사용할 수 없으며, 그런 메서드를 호출하고 싶다면 `java.lang.Object`로 값을 캐스팅해야한다.  
  
<br/>

## 6.2.5 `Unit` 타입: 코틀린의 `void`
- 코틀린의 `Unit` 타입은 자바의 `void`와 같은 기능을 한다.  
    ```kotlin
    fun printHello(name: String?): Unit {
        if (name != null)
            println("Hello $name")
        else
            println("Hi there!")
        // `return Unit` or `return` is optional
    }
    ```
    위 함수는 아래와 같이 표현해도 무방하다.  
    ```kotlin
    fun printHello(name: String?) { ... }
    ```

<br/>

## 6.2.6 `Nothing` 타입: “이 함수는 결코 정상적으로 끝나지 않는다.”
- 코틀린에는 '반환 값'이라는 개념 자체가 의미 없는 함수가 일부 존재하는데, 이들은 반환 타입으로 `Nothing`을 사용한다.    
    ```kotlin
    val s = person.name ?: throw IllegalArgumentException("Name required")
    ```
    이 예시에서 `throw` 표현식의 타입은 `Nothing`이다.    
   
- `Nothing` 타입은 아무 값도 포함하지 않는다. 따라서 `Nothing`은 함수의 반환 타입이나, 반환 타입으로 쓰일 타입 파라미터로만 쓸 수 있다.
    - 그 외 용도로 `Nothing` 타입의 변수를 선언하더라도, 그 변수는 아무 값도 저장할 수 없으므로 아무 의미가 없음..
    ```kotlin
    val x = null           // 'x' has type `Nothing?`
    val l = listOf(null)   // 'l' has type `List<Nothing?>
    ```
  
<br/><br/>

---

<br/><br/>

# 6.3 컬렉션과 배열

<br/>

## 6.3.1 널 가능성과 컬렉션
```kotlin
fun addValidNumbers(numbers: List<Int?>) {
    val validNumbers = numbers.filterNotNull() // validNumbers는 List<Int> 타입이 된다
    println(validNumbers.sum())
}

fun main() {
    addValidNumbers(listOf(1, null, 2, 3)) 
}
```

<br/>

## 6.3.2 읽기 전용과 변경 가능한 컬렉션
코틀린은 컬렉션 안의 데이터에 접근하는 인터페이스와 컬렉션 안의 데이터를 변경하는 인터페이스를 분리했다.  
- `kotlin.collections.Collection`
    - 컬렉션 안의 원소에 대해 iteration하고, 컬렉션의 크기를 얻고, 컬렉션안에 어떤 값이 들어있는지 검사하고, 컬렉션의 데이터를 읽는 다른 연산을 수행할 수 있다.
    - read only access to the collection
- `kotlin.collections.MutableCollection`
    - 컬렉션의 데이터를 수정할 수 있다. 위 `Collection`인터페이스를 확장한 인터페이스
    - supports adding and removing elements

<br/>

## 6.3.3 코틀린 컬렉션과 자바
- 모든 코틀린 컬렉션은 그에 상응하는 자바 컬렉션 인터페이스의 인스턴스다.  
- 코틀린의 읽기 전용과 변경 가능 인터페이스의 기본 구조는 `java.util` 패키지에 있는 자바 컬렉션 인터페이스의 구조를 그대로 옮겨 놓았다. 
    - 변경 가능한 각 인터페이스는 자신과 대응하는 읽기 전용 인터페이스를 확장(상속)한다. 

<br/>

### 컬렉션 생성 함수
| 컬렉션 타입 | 읽기 전용 타입 | 변경 가능 타입 |
|--|--|--|
| List | listOf | mutableListOf, arrayListOf |
| Set | setOf | mutableSetOf, hashSetOf, linkedSetOf, sortedSetOf |
| Map | mapOf | mutableMapOf, hashMapOf, linkedMapOf, sortedMapOf |

<br/>

> `setOf()`와 `mapOf()`는 원소가 하나면 `java.util.Collections.singleton(element)`, 원소가 둘 이상이면 `LinkedHashSet` 또는 `LinkedHashMap`을 반환한다.  

<br/>

## 6.3.5 객체의 배열과 기본 타입의 배열
- 배열 만들기
    - `arrayOf()`함수에 원소를 넘기면 된다
        - e.g. `val arrayOf = arrayOf(1, 2, 3)`
    - `arrayOfNulls()`함수에 정수 값을 인자로 넘기면, 모든 원소가 null이고 인자값을 크기로 가진 배열을 만들 수 있다. 
        - e.g. `val arrayOfNulls = arrayOfNulls<String>(3)`
    - `Array` 생성자를 이용할 수 있다. 배열의 크기와 람다를 인자로 받고, 람다를 호출하여 각 배열 원소를 초기화해준다. 
        - `arrayOf`를 쓰지 않고, 각 원소가 null이 아닌 배열을 만들어야 하는 경우, 이 생성자를 사용한다. 
        - e.g. `val array = Array(5) { i -> "hello $i" }`
        - e.g. `val array = Array(5) { "hello" }`

- `toTypedArray`를 사용하면 컬렉션을 배열로 바꿀 수 있다. 
    ```kotlin
    val numbers = listOf(1, 2, 3) // List<Int>
    val numbersArray = numbers.toTypedArray() // Array<Int>
    ```

- 코틀린은 원시 타입의 배열을 표현하는 별도 클래스를, 각 원시 타입마다 하나씩 제공한다.  
    - 예를 들어 `Int` 타입의 배열은 `IntArray`이다. 이 타입은 자바 원시 타입 배열인 `int[]`로 컴파일된다.  

원시 타입 배열 만들기
- 각 배열 타입 생성자로 만들기
    - 인자로 size를 넘기면, 해당 원시 타입이면서 디폴트 값(보통은 0)으로 으로 초기화된 배열을 반환한다.
        - e.g. `val intArray = IntArray(3)` ==> [0, 0, 0] 
    - size와 람다를 인자로 받는 생성자도 있다.
        - e.g. `val intArray1 = IntArray(5) { i -> i * 2 }` ==> [0, 2, 4, 6, 8]   
- 팩토리 함수 이용
    - 여러 값을 가변 인자로 받아, 그 값이 들어간 배열을 반환. 
    - e.g. `val intArrayOf = intArrayOf(1, 2, 3)`
