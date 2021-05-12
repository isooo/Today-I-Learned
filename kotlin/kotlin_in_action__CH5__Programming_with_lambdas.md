# **CH5. 람다로 프로그래밍**

<br/>

## 목차
- [5.1 람다 식과 멤버 참조](#51-람다-식과-멤버-참조)
- [5.2 컬렉션 함수형 API](#52-컬렉션-함수형-api): 함수형 스타일로 컬렉션 다루기
- [5.3 지연 계산 lazy 컬렉션 연산](#53-지연-계산-lazy-컬렉션-연산): 시퀀스로 컬렉션 연산을 lazy하게 하기
- [5.4 자바 함수형 인터페이스 활용](#54-자바-함수형-인터페이스-활용)
- [5.5 수신 객체 지정 람다: with와 apply](#55-수신-객체-지정-람다-with와-apply)

<br/><br/>

---

<br/><br/>

# 5.1 람다 식과 멤버 참조
**람다 식**(lambda expression)은 값처럼 여기저기 전달할 수 있는 동작의 모음이다.  

<br/>

## 5.1.1 람다 소개: 코드 블록을 함수 인자로 넘기기
람다는 '메소드가 하나뿐인 익명 클래스(무명 객체)' 대신 사용할 수 있다.

<br/>

## 5.1.2 람다와 컬렉션
```kotlin
// 반복문 사용
fun findTheOldest(people: List<Person>) {
    var maxAge = 0;
    var oldestPeople: Person? = null

    for (person in people) {
        if (person.age >= maxAge) {
            maxAge = person.age
            oldestPeople = person
        }
    }

    println(oldestPeople)
}

// 함수 사용 및 람다로 멤버 참조
fun findTheOldestLambda(people: List<Person>) {
    println(people.maxBy(Person::age))
}

// 함수 사용
fun findTheOldestLambda2(people: List<Person>) {
    println(people.maxBy { it.age })  
}
```

<br/>

## 5.1.3 람다 식의 문법
```kotlin
// 코틀린에서 람다는 중괄호 사이에 위치한다
people.maxBy({ p: Person -> p.age })
people.maxBy() { p: Person -> p.age }
people.maxBy { p: Person -> p.age }
people.maxBy { p -> p.age }
people.maxBy(Person::age)
```
   
- 람다의 파라미터가 하나뿐이고, 그 타입을 컴파일러가 추론 가능한 경우에 `it`을 사용할 수 있다.      
- 람다를 변수에 저장했을 경우, 파라미터 타입을 추론할 수 없으므로 이럴 땐 파라미터 타입을 명시해야 한다.   
    ```kotlin
    val getAge = {p:Person -> p.age}    
    people.maxBy(getAge)
    ```
- 이름 붙인 인자를 사용해 람다를 전달할 수 있다.  
    ```kotlin
    val name = people.joinToString(separator = " ", transform = { it.name })
    // people.joinToString(separator = " ") { it.name } // 마지막 인자인 람다를 괄호 밖으로 빼낼 수도 있음
    ```
- 본문이 여러 줄로 이루어진 람다의 경우, 맨 마지막에 있는 식이 람다의 결과가 된다.  
    ```kotlin
    val sum = { x: Int, y: Int -> 
        ... 
        println("Computing the sum of $x and $y")
        ...
        x + y   // 람다의 결과값
    }
    ```

<br/>

## 5.1.4 현재 영역에 있는 변수에 접근
- 람다를 함수 안에 정의하면, 함수의 파라미터와 (람다 정의보다 앞에 선언된)로컬 변수를 람다에서 사용할 수 있다.  
    - 람다 안에서 사용되는 외부 변수(함수의 파라미터나 함수의 로컬 변수)를 '람다가 표획(==**capture**)한 변수'라 한다.  
    ```kotlin
    fun printMessageWithPrefix(messages: Collection<String>, prefix: String) {
        messages.forEach {
            println("$prefix $it") 
        }
    }
    ```
- 람다 안에서 `final`이 아닌 변수에도 접근할 수 있다
    - 그 변수의 값을 변경할 수도 있다.  
    - `final`변수를 포획한 경우엔 람다 코드를 변수 값과 함께 저장한다.   
    `final`이 아닌 변수를 포획한 경우엔 특별한 래퍼로 감싸서 나중에 변경하거나 읽을 수 있게 한 다음, 래퍼에 대한 참조를 람다 코드와 함께 저장한다.  
    ```kotlin
    fun printProblemCount(responses: Collection<String>) {
        var clientErrors = 0
        var serverErrors = 0
        
        responses.forEach {
            if (it.startsWith("4")) {
                clientErrors++
            } else if (it.startsWith("5")) {
                serverErrors++
            }
        }
        
        println("client error: $clientErrors ||| server error: $serverErrors")
    }
    ```

<br/>

> [ ] 람다를 실행 시점에 표현하는 데이터 구조는 람다에서 시작하는 모든 참조가 포함된 닫힌(`closed`) 객체 그래프를 람다 코드와 함께 저장해야 한다?  

<br/>

## 5.1.5 멤버 참조
- 함수를 값으로 바꿀 수 있는데, 이 때 `::`(이중 콜론)을 사용한다.
    - `::`를 사용하는 식을 **멤버 참조**(member reference)라 한다. 
    ```kotlin
    val getAge = Person::age  // { p: Person -> p.age }
    ```
  
- 최상위에 선언된(다른 클래스의 멤버가 아닌) 함수나 프로퍼티를 참조할 수도 있다.  
    ```kotlin
    fun salute() = println("Salute!")

    fun main() {
        run(::salute)
    }
    ```

- 람다가 '인자가 여럿인 다른 함수'한테 작업을 위임하는 경우, 람다를 정의하지 않고 직접 위임 함수에 대한 참조를 제공하여 편리하게 사용할 수 있다.
    ```kotlin
    fun sendEmail(person: Person, message: String) = println("hello")

    fun main() {
        val action = { person: Person, message: String -> sendEmail(person, message) }
        val nextAction = ::sendEmail    
    }
    ```

- `::` 뒤에 클래스 이름을 넣으면 생성자 참조(constructor reference)를 만들 수 있다.  
    ```kotlin
    val createPerson = ::Person
    val p = createPerson("isoo", 1)
    ```

- 확장 함수도 멤버 함수와 동일한 방식으로 참조할 수 있다.  
    ```kotlin
    fun Person.isAdult() = age >= 21

    fun main() {
        val predicate = Person::isAdult     
    }
    ```

- 코틀린 1.1부터는 바운드 멤버 참조(bound member reference)를 지원한다.  
    ```kotlin
    fun main() {
        val p = Person("isoo", 1)
        val getAge2 = p::age    // 바운드 멤버 참조 이용
        println(getAge2())
    }
    ```

<br/><br/>

---

<br/><br/>

# 5.2 컬렉션 함수형 API
컬렉션을 다루는 코틀린 표준 라이브러리 몇 가지를 알아보자.  

<br/>

## 5.2.1 필수적인 함수: `filter`와 `map`
- `filter` 함수는 컬렉션을 이터레이션하면서, 주어진 람다에 각 원소를 넘겨 람다가 `true`를 반환하는 원소만 모은다.    
- `map` 함수는 주어진 람다를 컬렉션의 각 원소에 적용한 결과를 모아 새 컬렉션을 만든다.  

```kotlin
list.filter { person -> person.age == list.maxBy { it.age }!!.age }

// 위 코드는 아래와 같이 리펙토링가능  
val maxAge = list.maxBy { it.age }!!.age
list.filter { person -> person.age == maxAge }
```

> `!!`는 절대 null이 아니다는 뜻이다.  

- `Map`컬렉션은 key와 value를 처리하는 함수가 따로 존재한다.  
    - `filterKeys`와 `mapKeys`는 key를 걸러 내거나 변환하고,  
    - `filterValues`와 `mapValues`는 value를 걸러내거나 반환한다.  
    ```kotlin
    val map = mapOf(0 to "zero", 1 to "one")
    val upperValues = map.mapValues { it -> it.value.toUpperCase() } 
    // {0=ZERO, 1=ONE}
    ```

<br/>

## 5.2.2 `all`, `any`, `count`, `find`: 컬렉션에 술어 적용
- `all`
    - 컬렉션의 모든 원소가 특정 조건을 만족하는가
- `any`
    - 컬렉션에서 특정 조건을 만족하는 하나 이상의 원소가 있는지
- `count`
    - 조건을 만족하는 원소의 개수
- `find`
    - 조건을 만족하는 첫 번째 원소 반환
    - `findOrNull`과 같다. 만약 원소가 없다면 `null`이 반환된다는 걸 명확히 하고 싶으면, 이걸 쓰면 됨.  

```kotlin
val list = listOf(Person("c", 10), Person("a", 15), Person("B", 12))
val canBeUnder13 = { p: Person -> p.age < 13 }
list.any(canBeUnder13)
```

<br/>

## `groupBy`: 리스트를 여러 그룹으로 이뤄진 맵으로 변경
```kotlin
val list = listOf(
    Person("Alice", 31),
    Person("Bob", 29),
    Person("Carol", 31)
)
val groupByAge = list.groupBy { it.age }
// {31=[Person(name=Alice, age=31), Person(name=Carol, age=31)], 29=[Person(name=Bob, age=29)]}
```

위 `groupBy`의 결과 타입은 `Map<Int, List<Person>>`이다.  

<br/>

## 5.2.4 `flatMap`과 `flatten`: 중첩된 컬렉션 안의 원소 처리
- `flatMap` 함수는 인자로 주어진 람다를 먼저 컬렉션의 모든 객체에 적용하고(혹은 매핑하고), 그 결과 얻어지는 리스트들을 하나의 리스트로 모은다.      
    ```kotlin
    val list = listOf("abc", "def")
    val flatMap = list.flatMap { it.toList() } 
    ```
- `flatten` 함수는 중첩된 리스트에 별다른 연산없이 모으기만 할 때 사용하면 된다.
    ```kotlin
    val list = listOf(listOf("abc", "def"), listOf("123", "456"))
    val flattened = list.flatten() // [abc, def, 123, 456]
    ```

<br/><br/>

---

<br/><br/>

# 5.3 지연 계산 lazy 컬렉션 연산
- `map`이나 `filter`는 결과 컬렉션을 즉시(eagerly) 생성한다. 
    - 컬렉션 함수를 연쇄하면 매 단계마다 계산 중간 결과를 새로운 컬렉션에 임시로 담는다는 뜻.   
      
**시퀀스**(sequence)를 사용하면 중간 임시 컬렉션을 사용하지 않고도 컬렉션 연산을 연쇄할 수 있다.  
```kotlin
people.asSequence()     // 원본 컬렉션을 시퀀스로 변환
    .map(Person::name)  // 시퀀스도 컬렉션과 동일한 API를 제공
    .filter { it.startsWith("A") } // 시퀀스도 컬렉션과 동일한 API를 제공
    .toList()   // 결과 시퀀스를 리스트로 변환
```

> 코틀린 지연 계산 시퀀스는 `Sequence`인터페이스에서 시작한다.  
> `Sequence`안에는 `iterator`라는 메서드 하나만 있다. 이 메서드를 통해 시퀀스로부터 원소 값을 얻을 수 있다.  
> `Sequence`인터페이스의 강점은 그 인터페이스 위에 구현된 연산이 계산을 수행하는 방법에 있다. 시퀀스의 원소는 필요할 때 비로소 계산된다.  
> 시퀀스를 리스트로 만들려면 `toList()`를 사용하면 된다.  
  
<br/>

## 5.3.1 시퀀스 연산 실행: 중간 연산과 최종 연산
- 시퀀스는 중간(intermediate) 연산, 최종(terminal) 연산으로 나뉜다.  
- 중간 연산은 항상 지연 계산된다. 
- 최종 연산이 호출될 때 (연기됐던)모든 계산이 수행된다.  

<br/>

## 5.3.2 시퀀스 만들기
앞서 살펴본 것처럼 `asSequence()`로 만드는 것 외에,  
`generateSequence()`로도 시퀀스를 만들 수 있다. 

```kotlin
generateSequence(0) { it + 1 }
    .takeWhile { it <= 100 }
    .sum()
```

<br/><br/>

---

<br/><br/>

# 5.4 자바 함수형 인터페이스 활용
인터페이스 내에 하나의 추상 메서드만 존재한다면, 이런 인터페이스를 **함수형 인터페이스**(functional interface) 또는 **SAM**(single abstract method; 단일 추상 메서드) 인터페이스라 한다.    
함수형 인터페이스를 인자로 받을 경우, 람다를 넘길 수 있다.    
  
<br/>
  
> 람다와 무명 객체 사이엔 차이가 있다.  
> 객체를 명시적으로 선언한 경우 메서드를 호출할 때마다 새로운 객체가 생성된다.  
> 람다는 다르다.   
> [ ] 람다의 경우, (정의가 들어있는 함수의 변수에 접근하지 않는)람다에 대응하는 무명 객체를 메서드를 호출할 때마다 반복 사용한다?  
> 람다가 주변 영역의 변수를 포획(capture)한다면, 매 호출마다 같은 인스턴스를 사용할 수 없다. 그런 경우 컴파일러는 매번 주변 영역의 변수를 포획한 새로운 인스턴스를 생성해준다.      
  
<br/>

## 5.4.2 SAM 생성자: 람다를 함수형 인터페이스로 명시적으로 변경
- SAM 생성자는 람다를 함수형 인터페이스의 인스턴스로 변환할 수 있게, 컴파일러가 자동으로 생성한 함수다.  
- 컴파일러가 자동으로 람다를 함수형 인터페이스 무명 클래스로 바꾸지 못하는 경우, SAM 생성자를 사용할 수 있다.  
- [ ] SAM 생성자의 이름은, 사용하려는 함수형 인터페이스의 이름과 같다.   
  
```kotlin
fun createAllDoneRunnable(): Runnable {
    return Runnable { println("All done!") }
}

fun main() {
    createAllDoneRunnable().run()
}
```
  
<br/><br/>

---

<br/><br/>

# 5.5 수신 객체 지정 람다: with와 apply
**수신 객체 지정 람다**(lambda with reveiver)는 수신 객체를 명시하지 않고 람다 본문 안에서 다른 객체의 메서드를 호출할 수 있는 기능이다.  

<br/>

## `with` 함수
`with`는 객체의 이름을 반복하지 않고도 그 객체에 대한 다양한 연산을 수행할 수 있는 기능을 제공한다.  

```kotlin
fun alphabet(): String {
    val result = StringBuilder()

    for (letter in 'A'..'Z') {
        result.append(letter)
    }

    result.append("\nNow I know the alphabet!")
    return result.toString()
}

// 위 alphabet() 함수에서 result라는 객체가 여러번 호출됨을 알 수 있다. 이를 with를 사용한 코드로 바꿔보자.

fun alphabetUsingWith(): String {
    val sb = StringBuilder()

    return with(sb) {   // 수신 객체를 지정
        for (letter in 'A'..'Z') {
            this.append(letter) 
        }

        append("\nNow I know the alphabet!") // this를 생략해도 메서드 호출 가능
        this.toString() // with 가 반환하는 값
    }
}

// 식을 본문으로 하는 함수로 표현하여, 위 함수를 아래와 같이 리펙터링할 수 있다. 
fun alphabetUsingWithRefactoring() =
    with(StringBuilder()) {   // 수신 객체를 직접 선언
        for (letter in 'A'..'Z') {
            this.append(letter)
        }

        append("\nNow I know the alphabet!") 
        this.toString() 
    }

fun main() {
    println(alphabet())
    println(alphabetUsingWith())
    println(alphabetUsingWithRefactoring())
}
```
  
- `with`는 파라미터가 2개인 함수다.   
    - (위 예시 기준으로) 첫 번째는 `sb`, 두 번째는 람다다.  
- `with`함수는 첫 번째 인자로 받은 객체를, 두 번째 인자로 받은 람다의 수신 객체로 만든다.  
- 인자로 받은 람다 본문에서는 `this`를 사용해 그 수신 객체에 접근할 수도 있고, `this`를 사용하지 않고도 수신 객체의 멤버에 접근할 수 있다.    
  
<br/>

## `apply` 함수
`with`처럼 람다의 결과가 아닌, 수신 객체가 필요한 경우엔 `apply`함수를 사용하면 된다.     
`apply`함수는 항상 자신에게 전달된 객체(== 수신 객체)를 반환한다.       
`apply`함수는 객체의 인스턴스를 만들면서 즉시 프로퍼티 중 일부를 초기화해야 하는 경우에 유용하다.   
    
```kotlin
fun alphabetUsingApply() = StringBuilder().apply {
    for (letter in 'A'..'Z') {
        this.append(letter)
    }

    append("\nNow I know the alphabet!")
}.toString()
```
위 예시에서 `apply`를 실행한 결과는 `StringBuilder` 객체가 된다.       

<br/>
  
> `with`와 `apply` 모두 확장 함수로 정의돼 있다.
  
<br/>

> 표준 라이브러리 중 `buildString`이라는 함수를 사용하면 위 예시 코드를 더 단순화할 수 있다.   
> - `buildString`는 `StringBuilder`를 만드는 일, 마지막에 `toString()`하는 일을 알아서 해준다!    
> - `buildString`의 인자는 수신 객체 지정 람다이며, 수신 객체는 항상 `StringBuilder`이다. 
> 
> ```kotlin
> fun alphabetUsingBulidString() = buildString {
>     for (letter in 'A'..'Z') {
>         this.append(letter)
>     }
> 
>     append("\nNow I know the alphabet!")
> }
> ```

