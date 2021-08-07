# **CH8. 고차 함수: 파라미터와 반환 값으로 람다 사용**
- 람다를 인자로 받거나 반환하는 함수인 **고차 함수**(high order function)를 만드는 방법을 알아보자.   
- 람다를 사용함에 따라 발생할 수 있는 성능상 부가 비용을 없애고, 람다 안에서 더 유연하게 흐름을 제어할 수 있는 **인라인**(inline) 함수에 대해 알아보자. 

<br/>

## 목차
- [8.1 고차 함수 정의](#81-고차-함수-정의)
- [8.2 인라인 함수: 람다의 부가 비용 없애기](#82-인라인-함수-람다의-부가-비용-없애기)
- [8.3 고차 함수 안에서 흐름 제어](#83-고차-함수-안에서-흐름-제어): 비로컬 return과 레이블, 무명 함수
  
<br/><br/>

---

<br/><br/>

# 8.1 고차 함수 정의
고차 함수는 다른 함수를 인자로 받거나 함수를 반환하는 함수다. 
고차 함수로 람다나 함수 참조를 인자로 넘길 수 있고, 람다나 함수 참조를 반환할 수 있다.     

<br/>

## 8.1.1 함수 타입
람다를 로컬 변수에 대입해보자.    
```kotlin
val sum: (Int, Int) -> Int = { x: Int, y: Int -> x + y }
```
  
- `Unit` 타입은 의미 있는 값을 반환하지 않을 때 사용되는 함수 반환 타입이다. 
    - 만약 함수 정의 시, 타입을 선언한다면 반환 타입을 반드시 명시해야 하므로 위 예시에서도 `Unit`을 명시한 것이다.   
    ```kotlin
    val printSomething: (s: String) -> Unit = {s ->  println(s) }
    ```
- 반환 타입이 nullable한 경우도 지정할 수 있다.
    ```kotlin
    val canReturnNull: (Int, Int) -> Int? = { x, y -> null }
    ```
- 함수의 반환 타입이 아닌, 함수 타입 전체가 nullable한 경우, 함수 타입을 괄호로 감싸고 그 위에 `?`를 붙여야 한다. 
    ```kotlin
    val funOrNull: ((Int, Int) -> Int)? = null
    ```

<br/>

## 8.1.2 인자로 받은 함수 호출
고차 함수에 인자로 함수를 넘길 수 있으며, 이때 인자로 넘기는 함수에 이름을 붙일 수 있다.  
```kotlin
fun String.filter(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()
    for (index in 0 until length) {
        val element = get(index)
        if (predicate(element)) sb.append(element)  // 인자로 받은 함수를 이름으로 호출하여 사용함
    }

    return sb.toString()
}

fun main() {
    println("hello world".filter { i -> i == 'o' })
    println("say something".filter { it in 'a'..'g' })
}
```
```
oo
aeg
```
   
<br/>

## 8.1.3 자바에서 코틀린 함수 타입 사용
함수 타입을 사용하는 코틀린 함수를 자바에서도 쉽게 호출할 수 있다.  
코틀린에서 사용된 함수 타입은, 컴파일 시 `FunctionN` 인터페이스로 바뀐다. 
```kotlin
// twoAndThree(3, 4) { x, y -> x + y }
twoAndThree(3, 4, (Function2)null.INSTANCE);
```
```kotlin
// fun twoAndThree(x: Int, y: Int, operation: (Int, Int) -> Int) {
public static final void twoAndThree(int x, int y, @NotNull Function2 operation) {    
```
  
`FunctionN` 인터페이스는 `invoke` 추상 메서드가 정의되어 있다. 이 `invoke`를 호출하여 함수를 실행하는 구조다.  
  
<br/>

## 8.1.4 디폴트 값을 지정한 함수 타입 파라미터나 널이 될 수 있는 함수 타입 파라미터
파라미터를 함수 타입으로 선언할 때도 디폴트 값을 정할 수 있다.    
```kotlin
fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    suffix: String = "",
    transform: (T) -> String = { it.toString() }    // 함수를 파라미터로 받되, 디폴트 값을 지정함
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(transform(element))
    }

    result.append(suffix)
    return result.toString()
}

fun main() {
    val list = listOf("one", "TWo", "tHree")
    println(list.joinToString(" : ", "(", ")"))     // 디폴트 변환 함수 사용.
    println(list.joinToString { it.lowercase() })   // 람다만 인자로 전달. 나머진 디폴트 값 사용
}
```
```
(one : TWo : tHree)
one, two, three
```

<br/>

함수가 nullable한 경우, 아래와 같이 안전한 호출을 사용하면 된다.  
```kotlin
fun <T> Collection<T>.joinToString(
    ...
    transform: ((T) -> String)? = { it.toString() }
): String {
    ...
    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(transform?.invoke(element) ?: element.toString())
    }
    ...
}
```

<br/>

## 8.1.5 함수를 함수에서 반환
함수를 반환값으로 사용할 수도 있다.  

아래 예시는 사용자가 선택한 배송 수단에 따라 배송비를 계산하는 함수를 반환한다.  
```kotlin
enum class Delivery { STANDARD, EXPEDITED }

class Order(val itemCount: Int)

fun getShippingCostCalculator(delivery: Delivery): (Order) -> Double {
    if (delivery == Delivery.EXPEDITED) {
        return { order -> 6 + 2.1 * order.itemCount }   
    }

    return { order -> 1.2 * order.itemCount }
}

fun main() {
    val costCalculator = getShippingCostCalculator(Delivery.EXPEDITED)
    println("Shipping costs ${costCalculator(Order(3))}")
}
```
```
Shipping costs 12.3
```

<br/>

## 8.1.6 람다를 활용한 중복 제거
함수 타입과 람다 식을 이용해 중복을 제거할 수 있는 재활용 코드를 만들 수 있다.  

아래 예시 코드에선 `averageWindowsDuration` 변수에 윈도우 사용에 대한 사용시간의 평균을 구하고 있다.  
```kotlin
data class SiteVisit(
    val path: String,
    val duration: Double,
    val os: OS
)

enum class OS { WINDOWS, LINUX, MAC, IOS, ANDROID }

fun main() {
    val log = listOf(
        SiteVisit("/", 34.0, OS.WINDOWS),
        SiteVisit("/", 22.0, OS.MAC),
        SiteVisit("/login", 12.0, OS.WINDOWS),
        SiteVisit("/signup", 8.0, OS.IOS),
        SiteVisit("/", 16.3, OS.ANDROID)
    )

    val averageWindowsDuration = log.filter { it.os == OS.WINDOWS }
        .map(SiteVisit::duration)
        .average()
}
```

만약 윈도우 뿐만 아니라 다른 os의 평균 사용시간을 측정하고 싶다면? 아래와 같이 해당 로직 자체를 함수로 정의하여 쓸 수 있다!    
 
```kotlin
fun List<SiteVisit>.averageDurationFor(os: OS) =
    filter { it.os == os }
        .map(SiteVisit::duration)
        .average()

fun main() {
    val log = listOf(
        ...
    )

    val averageIOSDuration = log.averageDurationFor(OS.IOS)
}
```

<br/><br/>

---

<br/><br/>

# 8.2 인라인 함수: 람다의 부가 비용 없애기
람다 식을 사용할 때마다 새로운 클래스가 만들어지는 것은 아니다.    
하지만 람다가 변수를 포획하면, 람다가 생성되는 시점마다 새로운 무명 클래스 객체가 생긴다. 이 경우엔 실행 시점에 무명 클래스 생성에 따른 부가 비용이 발생한다. 그래서 이럴 땐 똑같은 작업을 수행하는 일반 함수를 사용한 구현보다 람다 구현이 덜 효율적이게 된다.  

반복되는 코드를 별도의 라이브러리 함수로 빼내되, 컴파일러가 (~~*자바의 일반 명령문만큼*~~) 효율적인 코드를 생성하게 만들 수도 있다.  
`inline` 변경자를 함수에 붙이면, 컴파일러는 그 함수를 호출하는 모든 문장을 함수 본문에 해당하는 바이트코드로 바꿔치기 해준다. (메모리 할당 면에서 유리함)   
      
<br/>

## 8.2.1 인라이닝이 작동하는 방식
함수를 `inline`으로 선언하면, 그 함수의 본문이 인라인(inline)된다. 함수를 호출하는 코드를, 함수를 호출하는 바이트 코드 대신 함수 본문을 번역한 바이트 코드로 컴파일한다.  
람다의 본문에 의해 만들어지는 바이트 코드는, 그 람다를 호출하는 코드의 일부분으로 간주되어 코틀린 컴파일러는 그 람다를 (함수 인터페이스를 구현하는) 무명 클래스로 감싸지 않는다.  

<br/>

## 8.2.3 컬렉션 연산 인라이닝
코틀린 표준 라이브러리의 컬렉션 함수는 대부분 람다를 인자로 받는다. `filter`와 `map`은 인라인 함수다. 따라서 그 두 함수의 본문은 인라이닝되며, 추가 객체나 클래스 생성은 없다.  
하지만 이 코드는 리스트를 걸러낸 결과를 ***저장하는 중간 리스트***를 만든다. 처리할 원소가 많아지면 중간 리스트를 사용하는 부가 비용도 걱정할 만큼 커진다. `asSequence`를 통해 리스트 대신 시퀀스를 사용하면 중간 리스트로 인한 부가 비용은 줄어든다. 이때 각 중간 시퀀스는 람다를 필드에 저장하는 객체로 표현되며, 최종 연산은 중간 시퀀스에 있는 여러 람다를 연쇄 호출한다.  

<br/>

## 8.2.4 함수를 인라인으로 선언해야 하는 경우
inline 키워드의 이점을 배우고 나면 코드를 더 빠르게 만들기 위해 코드 여기저기에서 inline을 사용하고 싶어질 것이다. 하지만 사실 이는 좋은 생각이 아니다.   
inline 키워드를 사용해도 람다를 인자로 받는 함수만 성능이 좋아질 가능성이 높다.  
  
일반 함수 호출의 경우 JVM은 이미 강력하게 인라이닝을 지원한다. JVM은 코드 실행을 분석해서 가장 이익이 되는 방향으로 호출을 인라이닝한다. 이런 과정은 바이트코드를 실제 기계어 코드로 번역하는 과정(JIT)에서 일어난다. 이런 JVM의 최적화를 활용한다면 바이트코드에서는 각 함수 구현이 정확히 한 번만 있으면 되고, 그 함수를 호출하는 부분에서 따로 함수 코드를 중복할 필요가 없다. 반면 코틀린 인라인 함수는 바이트 코드에서 각 함수 호출 지점을 함수 본문으로 대치하기 때문에 **코드 중복**이 생긴다. 게다가 함수를 직접 호출하면 스택 트레이스가 더 깔끔해진다.
inline 변경자를 함수에 붙일 때는 코드 크기에 주의를 기울여야 한다. 인라이닝하는 함수가 큰 경우 함수의 본문에 해당하는 바이트코드를 모든 호출 지점에 복사해 넣고 나면 바이트코드가 **전체적으로 아주 커질 수 있다**.

<br/>

## 8.2.5 자원 관리를 위해 인라인된 람다 사용
자바 7부터는 자원을 관리하기 위한 특별한 구문인 `try-with-resource`문이 생겼다.  
```java
static String readFirstLineFromFile(String path) throws IOException {
		try (BufferedReader br = 
						new BufferedReader(new FileReader(path))) {
				return br.readLine();
		}
}
```
  
코틀린 언어는 이와 같은 기능을 언어 구성 요소로 제공하지는 않는다. 대신 자바 `try-with-resource`와 같은 기능을 제공하는 `use`라는 함수가 코틀린 표준 라이브러리 안에 들어있다.
```kotlin
fun readFirstLineFromFile(path: String): String {
    BufferedReader(FileReader(path)).use { br ->
        return br.readLine()
    }
}
```

`use` 함수는 닫을 수 있는(closeable) 자원에 대한 확장 함수며, 람다를 인자로 받는다. `use`는 람다를 호출한 다음에 자원을 닫아준다.  
`use`는 인라인 함수이기 때문에 이를 사용해도 성능에는 영향이 없다.  

<br/><br/>

---

<br/><br/>

# 8.3 고차 함수 안에서 흐름 제어

<br/>

## 8.3.1 람다 안의 return문: 람다를 둘러싼 함수로부터 반환
```kotlin
data class Person(val name: String, val age: Int)

fun lookForAlice(people: List<Person>) {
    for (person in people) {
        if (person.name == "Alice") {
            println("Found!")
            return
        }
    }
    println("Alice is not found")
}

fun main() {
    val people = listOf(Person("Alice", 29), Person("Bob", 31))
    lookForAlice(people)
}
```
```
Found!
```

이 코드를 `forEach`로 바꿔쓴다면?
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") {
            println("Found!")
            return
        }
    }
    println("Alice is not found")
}
```

람다 안에서 return을 사용하면 람다로부터만 반환되는 게 아니라 그 람다를 호출하는 함수가 실행을 끝내고 반환된다.  
그렇게 자신을 둘러싸고 있는 블록보다 더 바깥에 있는 다른 블록을 반환하게 만드는 return 문을 `넌로컬(non-local) return`이라 부른다.  
    
이렇게 return이 바깥쪽 함수를 반환시킬 수 있는 때는 람다를 인자로 받는 함수가 인라인 함수인 경우뿐이다. 예제에서 `forEach`는 인라인 함수이므로 람다 본문과 함께 인라이닝된다. 따라서 return 식이 바깥쪽 함수(여기서는 lookForAlice)를 반환시키도록 쉽게 컴파일할 수 있다.  
    
<br/>

## 8.3.2 람다로부터 반환: 레이블을 사용한 return
람다 식에서도 로컬 return을 사용할 수 있다. 람다 안에서 로컬 return은 for루프의 break와 비슷한 역할을 한다.  
로컬 return은 람다의 실행을 끝내고 람다를 호출했던 코드의 실행을 계속 이어간다.  
로컬 return과 넌로컬 return을 구분하기 위해선 레이블(label)을 사용해야 한다.  
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach test@{                      // 람다 식 앞에 label을 붙인다
        if (it.name == "Alice") return@test    // return@test 는 앞서 정의한 label인 test@를 참조한다
    }
    println("Alice might be somewhere")         // 이 줄은 항상 출력된다. 
}
```

람다에 label을 붙여서 사용하는 대신, 람다를 인자로 받는 인라인 함수의 이름을 return 뒤에 label로 사용해도 된다.
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") return@forEach  // return@forEach 는 람다 식으로부터 반환시킨다
    }
    println("Alice might be somewhere")
}
```

<br/>
  
## 8.3.3 무명 함수: 기본적으로 로컬 return
코틀린은 넌로컬 반환문을 여럿 사용해야 할 경우, 무명 함수를 이용해 코드 블록을 쉽게 작성할 수 있도록 제공하고 있다.    
무명 함수는 코드 블록을 함수에 넘길 때 사용할 수 있는 다른 방법이다.     
  
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach(fun(person) {
        if (person.name == "Alice") return
        println("${person.name} is not Alice")
    })
}
```

무명 함수는 일반 함수와 비슷해 보인다. 차이는 함수 이름이나 파라미터 타입을 생략할 수 있다는 점뿐이다. 무명 함수 안에서 레이블이 붙지 않은 return 식은 무명 함수 자체를 반환시킬 뿐 무명 함수를 둘러싼 다른 함수를 반환시키지 않는다.  
사실 return에 적용되는 규칙은 단순히 return은 fun 키워드를 사용해 정의된 **가장 안쪽 함수를 반환**시킨다는 점이다. 
그래서 람다 식은 fun을 이용해 정의되지 않으므로 람다 본문의 return은 람다 밖의 함수를 반환시키지만,  
무명 함수는 fun을 사용해 정의되므로 무명 함수 본문의 return은 그 무명 함수를 반환 시키는 것이다.  
