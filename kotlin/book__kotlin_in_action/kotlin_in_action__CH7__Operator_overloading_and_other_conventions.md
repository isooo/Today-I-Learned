# **CH7. 연산자 오버로딩과 기타 관례**
코틀린에서는 (언어 차원의 기능)과 (미리 정해진 이름의 함수)를 연결해주는 기법을 **관례**(convention)라 부른다. 
- e.g. 어떤 클래스 안에 `plus`라는 함수를 정의해두면, 그 클래스의 인스턴스에 대해 `+` 연산자를 사용할 수 있다.

이런 관례를 채택한 이유는 기존 자바 클래스를 코틀린 언어에 적용하기 위함이다.  
기존 자바 클래스에 대해 확장함수를 구현하면, 기존 자바 코드를 바꾸지 않아도 새로운 기능을 쉽게 부여할 수 있기 때문이다. 

<br/>

> 이번 7장에선 아래 `Point` 클래스를 주 예제로 사용한다.  
> `data class Point(val x: Int, val y: Int)`
    
<br/>

## 목차
- [7.1 산술 연산자 오버로드](#71-산술-연산자-오버로드)
- [7.2 비교 연산자 오버로딩](#72-비교-연산자-오버로딩)
- [7.3 컬렉션과 범위에 대해 쓸 수 있는 관례](#73-컬렉션과-범위에-대해-쓸-수-있는-관례)
- [7.4 구조 분해 선언과 component 함수](#74-구조-분해-선언과-component-함수)
- [7.5 프로퍼티 접근자 로직 재활용: 위임 프로퍼티](#75-프로퍼티-접근자-로직-재활용-위임-프로퍼티)
  
<br/><br/>

---

<br/><br/>

# 7.1 산술 연산자 오버로드
코틀린에서 관례를 사용하는 가장 단순한 예가 산술 연산자다.  
자바에서는 원시 타입에 대해서만 산술 연산자를 사용할 수 있고, 추가로 `String`에 대해 `+`연산자를 사용할 수 있다.  
  
> 코틀린에선 `BigInteger`를 `.add()`말고 `+`연산자를 이용해 덧셈할 수 있다 ㅎ__ㅎ 

<br/>

## 7.1.1 이항 산술 연산 오버로딩
`Point`에서 두 점을 더하는 연산을 `+` 연산자를 통해 사용할 수 있도록 구현해보자.  
```kotlin
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {    // operator를 붙여줘야 + 연산자로 이를 호출할 수 있다
        return Point(x + other.x, y + other.y)
    }
}

fun main() {
    val pointA = Point(1, 4)
    val pointB = Point(2, 10)
    println(pointA + pointB)    // 컴파일 된 소스 ==> pointA.plus(pointB) 
}
```

<br/>

연산자를 위 예시처럼 멤버 함수로 만들지 않고, 확장 함수로 정의할 수도 있다. 
```kotlin
data class Point(val x: Int, val y: Int) {
}

operator fun Point.plus(other: Point): Point {
    return Point(x + other.x, y + other.y)
}
```

<br/>

외부 함수의 클래스에 대한 연산자를 정의할 땐 관례를 따르는 이름의 확장 함수로 구현하는게 일반적인 패턴이다.  
코틀린에선 미리 정해둔 연산자만 오버로딩할 수 있으며, 아래 표처럼 정해진 이름을 사용해야 한다.  

### 오버로딩 가능한 이항 산술 연산자
| Expression | 함수 이름 | Translated to |
|--|--|--|
| a * b | times | a.times(b) |
| a / b | div | a.div(b) |
| a % b | rem | a.rem(b) |
| a + b | plus | a.plus(b) |
| a - b | minus | a.minus(b) |
| a..b | rangeTo | a.rangeTo(b) |

<br/>

연산자를 정의할 때 두 피연산자(연산자 함수의 두 파라미터)가 같은 타입일 필요는 없다.  
```kotlin
data class Point(val x: Int, val y: Int) {
...
    operator fun times(scale: Double): Point {
        return Point((x * scale).toInt(), (y * scale).toInt())
    }
}

fun main() {
    val pointA = Point(1, 4)
    println(pointA * 5.2) // Point(x=5, y=20)
}
```

<br/>

코틀린 연산자는 교환 법칙(commutativity; `a op b == b op a`인 성질)을 지원하지 **않는다**.  
즉 위 예시 코드의 경우 `pointA * 5.2`의 결과와 `5.2 * pointA`의 결과가 동일하게 하려면, `times` 함수에 추가적으로 정의줘야 한다.  

<br/>

연산자 함수의 반환 타입이 두 피연산자 중 하나와 일치하지 않아도 된다.  
```kotlin
// Char를 좌항으로 받고, Int를 우항으로 받아 String을 리턴하는 연산
operator fun Char.times(count: Int): String {
    return toString().repeat(count)
}

fun main() {
    println('c' * 3)    // ccc
}
```

<br/>

## 7.1.2 복합 대입 연산자 오버로딩
`plus` 같은 연산자를 오버로딩하면 코틀린은 `+` 뿐만 아니라 그와 관련 있는 연산자인 `+=`도 자동으로 함께 지원한다.  
`+=`, `-=` 등의 연산자는 복합 대입(compound assignment)연산자라 부른다.  
```kotlin
var pointC = Point(1, 4)
pointC += pointB
println(pointC)
```

<br/>

`+=` 연산이 "객체에 대한 참조를 다른 참조로 바꾸기"가 아닌, 원래 객체의 내부 상태를 변경하는 경우도 있다.  
예를 들면 `MutableCollection`에 원소를 추가할 때, 아래와 같이 쓸 수 있다.
```kotlin
val numbers = ArrayList<Int>()
numbers += 42
println(numbers[0]) // 42
```

<br/>

> `plus`와 `plusAssign`연산을 동시에 정의하진 말자. 만약 이 두 함수를 모두 정의하고 둘 다 `+=`를 사용 가능한 경우에 컴파일러는 오류를 보고한다. (일반 연산자 `+`만 사용하면 해결 가능).  
> 만약 변경 불가능한 클래스라면 `plus`를 사용해 새로운 값을 반환하도록 하고,  
> 빌더와 같이 변경 가능한 클래스를 설계한다면 `plusAssign`이나 그와 비슷한 연산만을 제공하자.  

<br/>
    
코틀린 표준 라이브러리는 `MutableCollection`에 대해 아래와 같이 정의해두었다. 그래서 앞선 예시가 가능했던 것.   
```kotlin
@kotlin.internal.InlineOnly
public inline operator fun <T> MutableCollection<in T>.plusAssign(element: T) {
    this.add(element)
}
```
     
<br/>

## 7.1.3 단항 연산자 오버로딩
코틀린은 `-a`처럼 한 값에만 작용하는 단항(unary) 연산자도 제공한다.  
```kotlin
operator fun Point.unaryMinus(): Point {
    return Point(-x, -y)
}

fun main() {
    val pointA = Point(1, 4)
    println(pointA.unaryMinus())    // Point(x=-1, y=-4)
    println(-pointA)                // Point(x=-1, y=-4)
}
```

<br/>

### 오버로딩 가능한 단항 산술 연산자
| Expression | 함수 이름 |
|--|--|
| +a | unaryPlus |
| -a | unaryMinus |
| !a | not |
| ++a, a++ | inc |
| --a, a-- | dec |

<br/><br/>

---

<br/><br/>

# 7.2 비교 연산자 오버로딩
코틀린에선 모든 객체에 대해 `==` 비교 연산자를 직접 사용해 비교 연산을 수행할 수 있다.

<br/>

## 7.2.1 동등성 연산자: `equals`
코틀린은 `==` 연산자 호출을 `equals` 메서드 호출로 컴파일 한다.    
```kotlin
a == b
a?.equals(b) ?: (b == null)
```

<br/>

식별자 비교(identity equals) 연산자 `===` 는 두 피연산자가 서로 같은 객체를 가리키는지(원시 타입의 경우엔 두 값이 같은지) 비교한다.    

<br/>

## 7.2.2 순서 연산자: `compareTo`
코틀린은 `Comparable` 인터페이스 안에 있는 `compareTo` 메서드를 호출하는 관례를 제공한다. 따라서 비교 연산자 (`<`, `>`, `<=`, `>=`)는 `compareTo`호출로 컴파일된다. `compareTo`의 return 타입은 `Int`다. 
  
`a >= b` ==> `a.compareTo(b) >= 0`

```kotlin
class Person(
    val name: String
) : Comparable<Person> {
    override fun compareTo(other: Person) = compareValuesBy(this, other, Person::name)
}

fun main() {
    println(Person("d") > Person("a")) // true
    println(Person("d") < Person("a")) // false
    println(Person("a") <= Person("a")) // true
    println(Person("a") >= Person("a")) // true
}
```

<br/><br/>

---

<br/><br/>

# 7.3 컬렉션과 범위에 대해 쓸 수 있는 관례
인덱스를 사용해 원소를 설정하거나 가져오고 싶을 때는 `a[b]`라는 식을 사용하고, 이를 **인덱스 연산자**라 부른다.  

<br/>

## 7.3.1 인덱스로 원소에 접근: `get`과 `set`
코틀린에선 각괄호(`[]`)를 이용해 배열 원소에 접근한다.   
이 연산자를 이용해 (변경 가능한 컬렉션의) key/value 를 넣거나 연관 관계를 변경할 수 있다.  
`mutableMap[key] = newValue`

<br/>

이 인덱스 연산자들도 관례를 따르는데  
원소를 읽는 연산은 `get`, 원소를 쓰는 연산은 `set` 연산자 메서드로 변환된다.  
```kotlin
operator fun Point.get(index: Int): Int {
    return when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Invalid coordinate $index")
    }
}

fun main() {
    val point = Point(3, 7)
    println(point[0])   // 3
    println(point[1])   // 7
}
```
  
<br/>

## 7.3.2 `in` 관례
`in`은 객체가 컬렉션에 들어있는지 검사(멤버십 검사; membership test)한다. 이때 `in` 연산자와 대응하는 함수는 `contains`다.    
```kotlin
data class Rectangle(val upperLeft: Point, val lowerRight: Point)

operator fun Rectangle.contains(p: Point) = p.x in upperLeft.x until lowerRight.x &&
        p.y in upperLeft.y until lowerRight.y

fun main() {
    val rectangle = Rectangle(Point(10, 20), Point(50, 50))
    println(Point(20, 30) in rectangle) // true
}
```

<br/>

## 7.3.3 `rangeTo` 관례
`..` 연산자는 `rangeTo` 함수를 간략히 표현한 것이다.  
```kotlin
val now = LocalDate.now()
val someDay = now..now.plusDays(3)
println(now.plusDays(2) in someDay) // true
```

<br/>

> `rangeTo` 연산자는 다른 산술 연산자보다 우선순위가 낮다.  

<br/>

## 7.3.4 for 루프를 위한 `iterator` 관례
코틀린은 for 루프에서 `in` 연산자를 사용한다. 이는 루프할 대상의 `iterator()`를 호출해 이터레이터를 얻은 다음, 이 이터레이터에 대해 `hasNext()`와 `next()`를 반복 호출하는 방식으로 변환된다.  
  
```kotlin
for (i in 1..3) {
    println(i)
}

for (i in array.indices) {
    println(array[i])
}
```
  
<br/><br/>

---

<br/><br/>

# 7.4 구조 분해 선언과 component 함수
구조 분해 선언(destructuring declaration)은 관례를 사용한 특성이다. 이를 사용하면 복합적인 값을 분해해 여러 다른 변수를 한꺼번에 초기화할 수 있다.  
```kotlin
val point = Point(1, 2)
val (x, y) = point
println("$x : $y")  // 1 : 2
```
  
구조 분해 선언은 내부에서 다시 관례를 사용한다. 각 변수를 초기화하기 위해 `componentN`이라는 함수를 호출한다. 여기서 `N`은 구조 분해 선언에 있는 변수 위치에 따라 붙는 번호다.   
앞서 살펴본 예시는 아래롸 같이 컴파일 된다.  
```java
val x = p.component1()
val y = p.component2()
```
  
`data class`의 주 생성자에 들어있는 프로퍼티에 대해선 컴파일러가 자동으로 `componentN` 함수를 만들어 준다. 만약 직접 구현하려면 `operator fun component1() = x` 이런 식으로 구현하면 된다.    

코틀린 표준 라이브러리에서는 맨 앞 다섯 원소에 대한 `componentN`을 제공한다. 그 크기를 초과하는 원소에 대해 구조 분해 선언을 사용하면 컴파일 에러가 발생한다.  
  
<br/>
  
## 7.4.1 구조 분해 선언과 루프
구조 분해 선언은 변수 선언이 위치할 수 있는 장소에서 사용 가능하다.  

아래와 같이 루프 안에서도 사용할 수 있다.  
```kotlin
fun printEntries(map: Map<String, String>) {
    for ((key, value) in map) {
        println("$key -> $value")
    }
}
```
위 for문은 아래와 같다.   
```kotlin
for (entry in map.entries) {
    val key = entry.component1()
    val value = entry.component2()
    ...
}
```
  
<br/><br/>

---

<br/><br/>

# 7.5 프로퍼티 접근자 로직 재활용: 위임 프로퍼티
위임 프로퍼티(delegated property)는 코틀린이 제공하는 관례에 의존하는 특성 중 하나로,  
이를 사용하면 값을 뒷받침하는 필드에 단순히 저장하는 것보다 더 복잡한 방식으로 작동하는 프로퍼티를 쉽게 구현할 수 있다.  

> delegate는 객체가 직접 작업을 수행하지 않고 다른 도우미 객체가 그 작업을 처리하게 맡기는 디자인 패턴을 뜻한다. 이때 작업을 처리하는 도우미 객체를 *위임 객체*라 한다.    
  
<br/>

## 7.5.1 위임 프로퍼티 소개
위임 프로퍼티의 일반적인 문법은 다음과 같다.  

```kotlin
class Example {
    var p: String by Delegate()
}
```
프로퍼티`p` 는 접근자 로직을 다른 객체에 위임한다. `by` 뒤에 있는 식을 계산해서 위임에 쓰일 객체를 얻는데, 여기선 위임 객체로 `Delegate`클래스의 인스턴스를 사용했다.  
프로퍼티 위임 관례를 따르는 `Delegate` 클래스는 `getValue()`와 `setValue()`를 제공해야 한다.(물론 `setValue`는 변경 가능한 프로퍼티에만!)   
위 예시에서 사용된 `Delegate` 클래스를 단순화하면 다음과 같다.  
```kotlin
class Delegate {
    operator fun getValue(...): String {...}    // getter를 구현하는 로직을 담는다

    operator fun setValue(... s: String) {...}  // setter를 구현하는 로직을 담는다
}
```
```kotlin
val ex = Example()  
val oldValue = ex.p     // 이는 내부적으로 delegate.getValue(...)를 호출한다
ex.p = "new value~~"    // 이는 내부적으로 delegate.setValue(..., newValue)를 호출한다
```
  
<br/>

## 7.5.2 위임 프로퍼티 사용: `by lazy()`를 사용한 프로퍼티 초기화 지연
지연 초기화(lazy initialization)는 객체 일부분을 초기화하지 않고 놔뒀다가, 실제 그 부분의 값이 필요한 경우에 초기화하는 패턴이다.  
초기화 과정에서 자원을 많이 사용하거나, 객체를 사용할 때마다 꼭 초기화하지 않아도 되는 프로퍼티에 대해 지연 초기화 패턴을 사용한다.   

코틀린 표준 라이브러리에서 제공하는 `lazy` 함수는 위임 객체를 반환한다. 
`lazy` 함수의 인자는 값을 초기화할 때 호출할 람다이다.  

아래 예시를 살펴보면   
첫 번째 호출은 `lazy()`로 전달된 람다를 실행한 뒤 그 결과를 기억해뒀다가, 이후 `get()`를 호출할 경우엔 기억된 결과를 반환한다.  
```kotlin
val lazyValue: String by lazy {
    println("computed!")
    "Hello"
}

fun main() {
    println(lazyValue)
    println(lazyValue)
    println(lazyValue)
}
```
```
computed!
Hello
Hello
Hello
```
  
<br/>

## 7.5.3 위임 프로퍼티 구현
`Delegates.observable()`는 2가지 인자(프로퍼티의 초기 값, 변경을 처리할 핸들러)를 받는다. 
핸들러는 프로퍼티에 변경사항이 발생할 때마다 콜백 함수를 호출한다. 이때 콜백함수는 3가지 인자(`KProperty`타입의 프로퍼티, 기존 값, 변경된 값)를 받는다. 그리고 이 콜백이 호출될 때 이미 프로퍼티의 값은 변경된 상태이다.  

```kotlin
class User {
    var name: String by Delegates.observable("<no name>") { prop, old, new ->
        println("$old -> $new")
    }
}

fun main() {
    val user = User()
    user.name = "first"
    user.name = "second"
}
```
```
<no name> -> first // User.name의 초기값 <no name>에서 first로 값이 변경되어 콜백함수가 호출됨.
first -> second
```

<br/>

<details>
<summary><italic>참고: 책 예시</italic></summary>
<div markdown="1">

### 1. 위임 프로퍼티 없이, 나이나 급여가 변경되면 해당 정보를 리스너에게 통지해주는 코드 

```kotlin
// PropertyChangeSupport를 사용하기 위한 도우미 클래스
open class PropertyChangeAware {
    protected val changeSupport = PropertyChangeSupport(this)

    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }
}

// 나이나 급여가 바뀌면 그 사실을 리스너에게 통지
class Person(
    val name: String, age: Int, salary: Int
) : PropertyChangeAware() {
    var age: Int = age
        set(newValue) {
            val oldValue = field    // 뒷받침 필드에 접근할 때 field 식별자 사용
            field = newValue

            // 프로퍼티 변경을 리스너에게 통지
            changeSupport.firePropertyChange("age", oldValue, newValue)
        }

    var salary: Int = salary
        set(newValue) {
            val oldValue = field
            field = newValue
            changeSupport.firePropertyChange(
                "salary", oldValue, newValue
            )
        }
}

fun main() {
    val p = Person("Amy", 34, 2000)
    p.addPropertyChangeListener( // 프로퍼티 변경 리스너 추가
        PropertyChangeListener { event ->
            println(
                "Property ${event.propertyName} changed " +
                        "from ${event.oldValue} to ${event.newValue}"
            )
        }
    )
    p.age = 35
    p.salary = 2100
}
```
```
Property age changed from 34 to 35
Property salary changed from 2000 to 2100
```

<br/>

### 2. `Person`에서 set해주는 부분을 따로 빼내어, 프로퍼티의 값을 저장하고 값의 변경이 일어나면 통지를 보내주는 클래스를 추가해주자. 

```kotlin
class ObservableProperty(
    val propName: String, var propValue: Int,
    val changeSupport: PropertyChangeSupport
) {
    fun getValue(): Int = propValue
    fun setValue(newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(propName, oldValue, newValue)
    }
}

class Person(
    val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

    val _age = ObservableProperty("age", age, changeSupport)
    var age: Int
        get() = _age.getValue()
        set(value) {
            _age.setValue(value)
        }

    val _salary = ObservableProperty("salary", salary, changeSupport)
    var salary: Int
        get() = _salary.getValue()
        set(value) {
            _salary.setValue(value)
        }
}

// PropertyChangeAware 클래스와 main 메서드의 소스는 동일
```

<br/>

### 3. 코틀린의 위임 프로퍼티를 이용해, `Person`에 존재하는 `ObservableProperty` 로직 거둬내기

```kotlin
class ObservableProperty(
    var propValue: Int, val changeSupport: PropertyChangeSupport
) {
    operator fun getValue(p: Person, prop: KProperty<*>): Int = propValue

    operator fun setValue(p: Person, prop: KProperty<*>, newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }
}

class Person(
    val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

    var age: Int by ObservableProperty(age, changeSupport)
    var salary: Int by ObservableProperty(salary, changeSupport)
}

// PropertyChangeAware 클래스와 main 메서드의 소스는 동일
```

- 코틀린 관례에 사용하는 다른 함수들처럼 `getValue`와 `setValue` 함수에 `operator` 변경자가 붙는다
- `getValue`와 `setValue`는 프로퍼티가 포함된 객체(`p`)와 프로퍼티를 표현하는 객체(`prop`)을 파라미터로 받는다. 
    - `KProperty` 타입의 객체를 사용해 프로퍼티를 표현한다
    - `KProperty.name`을 통해 메서드가 처리할 프로퍼티의 이름을 알 수 있다 (자세한건 10.2절에서)
- `KProperty`를 통해 프로퍼티 이름을 전달받으므로, `Person`에서 `ObservableProperty`를 생성할 때의 주생성자에선 `name` 프로퍼티(`"age"`, `"salary"`)를 없앤다.  

<br/>

### 4. `Delegates.observable`를 사용해 프로퍼티 변경 통지 구현하기
3번 과정에서 살펴본 `ObservableProperty`와 비슷한 클래스가 이미 코틀린 표준 라이브러리에 있다.  
프로퍼티 값의 변경을 통지할 때, 이 표준 라이브러리 클래스가 `PropertyChangeAware`를 사용할 수 있도록 알려주는 코드만 추가하면 된다.  
```kotlin
class Person(
    val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

    private val observer = { prop: KProperty<*>, oldValue: Int, newValue: Int ->
        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }

    var age: Int by Delegates.observable(age, observer)
    var salary: Int by Delegates.observable(salary, observer)
}

// PropertyChangeAware 클래스와 main 메서드의 소스는 동일
```

---

</div>
</details>

<br/>

> 참고: [kotlinlang: Delegated properties: Observable properties](https://kotlinlang.org/docs/delegated-properties.html#observable-properties)

<br/>

## 7.5.4 위임 프로퍼티 컴파일 규칙
위임 프로퍼티의 동작 방식을 정리해보자.    

아래와 같은 위임 프로퍼티를 가진 클래스가 있다고 가정하자.  
```kotlin
class C {
    var prop: Type by MyDelegate()
}

val c = C()
```
컴파일러는 `MyDelegate` 클래스의 인스턴스를 감춰진 프로퍼티에 저장하며, 그 감춰진 프로퍼티를 `<delegate>`라 부른다. 
컴파일러는 이 프로퍼티를 표현하기 위해 `KProperty`타입의 객체를 사용한다. 이 객체는 `<property>`라 부른다.   
컴파일러는 아래 코드를 생성한다.  
```kotlin
class C {
    private val <delegate> = MyDelegate()

    var prop: Type
        get() = <delegate>.getValue(this, <property>)
    set(value : Type) = <delegate>.setValue(this, <property>, value )
}
```
  
프로퍼티를 사용하면, `<property>`에 있는 `getValue`나 `setValue`함수가 호출된다.  

<br/>

## 7.5.5 프로퍼티 값을 맵에 저장
자신의 프로퍼티를 동적으로 정의할 수 있는 객체를 만들 때 위임 프로퍼티를 활용하기도 한다. 그런 객체를 확장 가능한 객체(expando object)라 부른다.   
   
`Map`에 속성 값을 저장해두고, `Map` 인스턴스 자체를 위임자로 사용할 수 있다.  
```kotlin
class Member(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
}

fun main() {
    val member = Member(
        mapOf(
            "name" to "John Doe",
            "age" to 25
        )
    )

    println(member.name)    // John Doe
    println(member.age)     // 25
}
```