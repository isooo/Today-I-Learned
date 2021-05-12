# **CH4. 클래스, 객체, 인터페이스**

<br/>

## 목차
- [4.1 클래스 계층 정의](#41-클래스-계층-정의): 클래스와 인터페이스
- [4.2 뻔하지 않은 생성자와 프로퍼티를 갖는 클래스 선언](#42-뻔하지-않은-생성자와-프로퍼티를-갖는-클래스-선언)
- [4.3 컴파일러가 생성한 메소드: 데이터 클래스와 클래스 위임](#43-컴파일러가-생성한-메소드-데이터-클래스와-클래스-위임)
- [4.4 object 키워드: 클래스 선언과 인스턴스 생성](#44-object-키워드-클래스-선언과-인스턴스-생성)

<br/><br/>

---

<br/><br/>

# 4.1 클래스 계층 정의
클래스와 인터페이스에 대해 알아보자.     
코틀린의 가시성과 접근 변경자에 대해 알아보자. 코틀린의 가시성/접근 변경자는 자바와 비슷하지만, default 가시성이 다르다!    
코틀린에서 새로 도입된 `sealed` 변경자에 대해 알아보자. 이는 클래스 상속을 제한하는 변경자다.  

<br/>

## 4.1.1 코틀린 인터페이스
- 코틀린 인터페이스 안에는 추상메서드와 구현이 있는 메서드를 정의할 수 있다. 상태(필드)는 들어갈 수 잆다.        
- 클래스 이름 뒤에 `:`(콜론)을 붙이고 이름을 적으면 인터페이스는 구현을, 클래스는 확장을 처리할 수 있다.   
    - 인터페이스는 제한없이 구현할 수 있고, 클래스는 오직 하나만 확장할 수 있다.    
- `override` 변경자는 오버라이드한다는 표시인데, 코틀린에선 이 변경자를 **반드시** 사용해야 한다. 
    - 상위 클래스와 시그니처가 동일한 메서드를 하위 클래스에 선언하는 실수를 방지해준다.  
- 인터페이스에 디폴트 메서드를 선언할 수 있는데, (자바였다면 `default`라는 키워드를 붙여줬어야 하지만) 코틀린에선 키워드를 붙이지 않는다.    
      
아래는 예시코드다.    
```kotlin
interface Clickable {
    // 추상 메서드
    fun click()  // 이 인터페이스를 구현한 비추상 클래스(또는 구체적 클래스)는 이 메서드에 대한 구현을 제공해야 한다

    // 디폴트 메서드
    fun showOff() = println("I'm clickable!")   
}

// Clickable을 구현한 Button 클래스
class Button : Clickable {
    override fun click() = println("I was clicked!")
}

fun main() {
    Button().click()   
    Button().showOff()  // Button에서 새로 정의하지 않았으므로, 인터페이스의 디폴트 메서드에 대한 구현을 그대로 사용함 
}
```
```
I was clicked!
I was clicked!
```
  
만약 이름과 시그니처가 같은 멤버 메서드에 대해 둘 이상의 디폴트 구현이 있는 경우, 인터페이스를 구현하는 하위 클래스에서는 명시적으로 새로운 구현을 제공해야 한다.    
그렇지 않으면 컴파일 에러 발생함. 클래스가 구현하는 두 상위 인터페이스에 정의된 `showOff()` 구현을 대체할 오버라이딩 메서드를 제공하지 않았기 때문이다.    
```kotlin
interface Focusable {
    fun setFocus(b: Boolean) = println("I ${if (b) "got" else "lost"} focus.")

    fun showOff() = println("I'm focusable!")
}

class Button : Clickable, Focusable {
    override fun click() = println("I was clicked!")
    
    override fun showOff() {
        // 상위 타입의 이름을 꺾쇠 괄호 사이에 넣고 super를 지정하면, 어느 상위 타입의 멤버 메서드를 호출할 지 지정할 수 있다. 
        super<Clickable>.showOff()  
        super<Focusable>.showOff()
    }

    // 만약 super의 둘 중 하나만 호출한다면 이렇게도 가능함
    // override fun showOff() = super<Focusable>.showOff()
}
```

<br/>

## 4.1.2 `open`, `final`, `abstract` 변경자: 기본적으로 `final`
- 코틀린에선 클래스와 메서드는 기본적으로 `final`이다.  
- 클래스의 상속을 허용하고자 한다면 클래스 앞에 `open`를,  
또 메서드나 프로퍼티를 오버라이드할 수 있게 허용하려면 메서드나 프로퍼티 앞에 `open`변경자를 붙여주면 된다.    
- 상위 클래스나 인터페이스의 멤버를 **오버라이드한 경우**, 그 함수나 프로퍼티는 **기본적으로 `open`상태**다.   
    - 만약 하위에서 오버라이드하지 못하게 하려면 `final`을 붙여주면 된다.  
- `abstract` 변경자로 선언한 추상 클래스는 인스턴스화할 수 없다.  
    - 추상 클래스에 속했더라도, 비 추상 함수는 기본적으로 final이다. 
        - 오버라이드를 허용하려면 `open`을 붙여줘야한다.
    - 추상 멤버는 `open`이 기본이라 굳이 명시하지 않아도 하위 클래스에서 오버라이드 할 수 있다.  
  
```kotlin
open class RichButton: Clickable {
    // 이 함수는 final이다. 하위 클래스는 이 메서드를 오버라이드할 수 없다
    fun disable() {}    

    // 이 함수는 오버라이드 할 수 있다
    open fun animate() {}

    // 이 함수는 상위인 인터페이스에 정의된 함수를 오버라이드한 것으로, 
    // 오버라이드한 함수는 기본적으로 open 상태이다 ==> 하위에서 오버라이드 가능
    override fun click() {}
}
```
  
```kotlin
// 추상 클래스
abstract class Animated {
    abstract fun animate()
    
    // 오버라이드 가능
    open fun stopAnimating() {}

    // 오버라이드 불가능
    fun animatedTwice () = println("22")
}

class Test: Animated() {    
    override fun animate() = println("animating~~")     
    override fun stopAnimating() = println("stop!") 
}
```

<br/>

| 변경자 | 이 변경자가 붙은 멤버는 | 설명 |
|--|--|--|
| `final` | 오버라이드할 수 없음 | 클래스 멤버의 기본 변경자! |
| `open` | 오버라이드할 수 있음 | 반드시 `open`을 명시해야 오버라이드할 수 있음 |
| `abstract` | 반드시 오버라이드해줘야 함 | 추상 클래스의 멤버에만 이 변경자를 붙일 수 있음 |
| `override` | 상위 '클래스\|\|인스턴스의 멤버' 를 오버라이드하는 중 | 오버라이드를 하는 멤버는 기본적으로 변경자가 `open`. 그래서 하위 클래스에서 이 멤버의 오버라이드를 금지하려면 `final`을 명시해야 한다. |  
  
<br/>

## 4.1.3 가시성 변경자: 기본적으로 공개
- 코틀린의 기본 가시성 변경자는 `public`이다.   
- 자바의 기본 가시성인 `default` (패키지 전용; package-private)은 **코틀린에는 없다**. 
- 코틀린에서 패키지는 코드의 네임스페이스를 관리하기 위한 용도로만 사용되기 때문에, 패키지를 가시성 제어에 사용하진 않는다.  

| 변경자 | 클래스 멤버 | 최상위 선언 |
|--|--|--|
| `public`(기본 가시성) | 모든 곳에서 볼 수 있음 | 모든 곳에서 볼 수 있음 |
| `internal` | 같은 모듈 안에서만 볼 수 있음 | 같은 모듈 안에서만 볼 수 있음 |
| `protected` | 같은 클래스 또는 하위 클래스 안에서만 볼 수 있음 | (*최상의 선언에 적용할 수 없음*) |
| `private` | 같은 클래스 안에서만 볼 수 있음 | 같은 파일 안에서만 볼 수 있음 |
    
<br/>

## 4.1.4 내부 클래스와 중첩된 클래스: 기본적으로 중첩 클래스
- 클래스 안에 다른 클래스를 선언할 수 있다. 이를 중첩 클래스라 한다.    
- 중첩 클래스(nested class)는 명시적으로 요청하지 않는 한, 바깥쪽 클래스의 인스턴스에 대한 **접근 권한은 없다**.  
    - 중첩 클래스에서 바깥쪽 클래스에 대한 참조를 가능케하고 싶다면, `inner` 변경자를 붙이면 된다.    
        - '바깥쪽 클래스(`Outer`)의 인스턴스를 가리키는 참조'를 표기하는 방법 
            ```kotlin
            class Outer {
                inner class Inner {
                    fun getOuterReference(): Outer = this@Outer
                }
            }
            ```

<br/>

## 4.1.5 봉인된 클래스: 클래스 계층 정의 시 계층 확장 제한
- 상위 클래스에 `sealed` 변경자를 붙이면, 그 상위 클래스를 상속한 하위 클래스 정의를 제한할 수 있다. 
- `sealed` 클래스의 하위 클래스를 정의할 땐 반드시 상위 클래스 안에 중첩시켜야 한다.  
- `sealed` 클래스는 자동으로 `open` 클래스가 된다.   
- `sealed`클래스는 내부적으로 `private` 생성자를 가진다. 
    - 이 생성자는 클래스 내부에서만 호출할 수 있다.
  
```kotlin
// 기반 클래스인 Expression를 sealed로 봉인함
sealed class Expression {
    // 기반 클래스 내 모든 하위 클래스를 중첩 클래스로 나열함
    class Num(val value: Int) : Expression()
    class Sum(val left: Expression, val right: Expression) : Expression()
}

fun eval(e: Expression): Int =
        when (e) {
            is Expression.Num -> e.value
            is Expression.Sum -> eval(e.left) + eval(e.right)
        } // 별도의 else 분기가 필요없음!
```
  
> 코틀린 1.1부터는 `sealed`클래스와 동일한 파일 내에만 이를 상속할 하위 클래스를 만들면 되고, 데이터 클래스로도 하위 클래스를 정의할 수 있다. 
   
<br/><br/>

---

<br/><br/>

# 4.2 뻔하지 않은 생성자와 프로퍼티를 갖는 클래스 선언
코틀린은 주 생성자와 부 생성자를 가진다.  
- 주 생성자
    - primary constructor
    - 클래스를 초기화할 때 주로 사용하는 간략한 생성자
    - 클래스 본문 밖에서 정의함
- 부 생성자
    - secondary constructor
    - 클래스 본문 안에서 정의함

<br/>

## 4.2.1 클래스 초기화: 주 생성자와 초기화 블록
```kotlin
class User(val nickname: String)
```
`(val nickname: String)`)가 **주 생성자**이다.   
  
위 코드와 동일한 목적을 달성할 수 있는, 가장 명시적으로 풀어쓴 코드는 아래와 같다.  
```kotlin
class User constructor(_nickname: String) {
    val nickname: String

    init {
        nickname = _nickname
    }
}
```
- `constructor`키워드는 주 생성자나 부 생성자 정의를 시작할 때 사용한다.  
    - 생성자 앞에 별다른 애너테이션이나 가시성 변경자가 없다면 `constructor`는 생략해도 된다. 
- `init`키워드는 초기화 블록  
    - 생성 시 필요한 코드가 있다면 초기화 블록을 통해 선언하면 된다.    
    - 클래스 안에 여러 초기화 블록을 선언할 수 있다.  
- 생성자 파라미터 `_nickname`에서 맨 앞의 밑줄(`_`)은 프로퍼티와 생성자 파라미터를 구분하기 위해 사용되는 관례다.  
    
> 위 예제에선 프로퍼티 초기화 코드를 프로퍼티 선언에 포함시킬 수 있다.
> ```kotlin
> class User (_nickname: String) {   
>     val nickname: String = _nickname    // 프로퍼티를 주 생성자의 파라미터로 초기화한다
> }
> ```
> 
> 위 코드에서, 주 생성자의 파라미터로 프로퍼티를 초기화한다면, 아래와 같이 더 간략하게 표현할 수 있다.  
> ```kotlin
> class User(val nickname: String)    // 주 생성자 파라미터 이름 앞에 val을 추가하여, 프로퍼티 정의와 초기화를 한 번에 함
> ``` 

<br/>

- 함수 파라미터와 마찬가지로 생성자 파라미터에도 디폴트 값을 정의할 수 있다.  
    ```kotlin
    class User(val nickname: String, val isSubscribed: Boolean = true)
    ```
- 클래스를 정의할 때 별도로 생성자를 정의하지 않으면, 컴파일러가 자동으로 인자 없는 디폴트 생성자를 만들어준다.  
    ```kotlin
    open class Button // 인자 없는 디폴트 생성자가 만들어짐
    ```
- 만약 어떤 클래스를 외부에서 인스턴스화 하지 못하게 막고 싶다면, 모든 생성자를 `private`으로 만들어주면 된다.   
    ```kotlin
    open class Member private constructor(val id: String, val name: String)
    ```
  
<br/>

## 4.2.2 부 생성자: 상위 클래스를 다른 방식으로 초기화
- 클래스에 주 생성자가 없다면, 모든 부 생성자는 반드시 상위 클래스를 초기화하거나 다른 생성자에게 생성을 위임해야 한다.  
```kotlin
open class View {
    constructor(ctx: Context) {...}
    constructor(ctx: Context, attr: AttributeSet) {...}
}

class MyButton : View {
    // this를 이용해 이 클래스 내 다른 생성자를 호출하여, 생성을 위임할 수도 있다.  
    constructor(ctx: Context) : this(ctx, MY_STYLE)

    // 상위 클래스의 부 생성자를 새로 정의함
    constructor(ctx: Context, attr: AttributeSet) : super(ctx, attr) {...}
}
```  

<br/>

## 4.2.3 인터페이스에 선언된 프로퍼티 구현
```kotlin
interface User {
    val name: String
}

class PrivateUser(override val name: String) : User

class SubscribingUser(val email: String) : User {
    override val name: String
        get() = email.substringBefore('@')
}
```
  
인터페이스는 위와 같이 추상 프로퍼티 뿐 아니라 getter/setter가 있는 프로퍼티를 선언할 수도 있다.  
```kotlin
interface User {
    val email: String   
    val name: String    
        get() = email.substringBefore('@')
}

class TestUser(override val email: String) : User {...} 
// TestUser클래스는 인터페이스의 추상 프로퍼티인 email은 반드시 오버라이드해야 한다.  
```

<br/>

## 4.2.4 게터와 세터에서 뒷받침하는 필드에 접근
- `field`식별자를 통해 뒷받침하는 필드에 접근할 수 있다.  
```kotlin
class User(val name: String) {
    // 뒷받침 필드인 address
    var address: String = "unspecified"
        set(value: String) {
            println("""
                Address was changed for $name:
                "$field"  -> "$value"
                """.trimIndent())
            field = value   // 뒷받침하는 필드 값 변경하기
        }
}

fun main() {
    val user = User("isoo")
    user.address = "서울"   // 뒷받침 필드의 값을 get할 수 있음
}
```

<br/>

## 4.2.5 접근자의 가시성 변경
- 접근자의 가시성은 기본적으로 프로퍼티의 가시성과 같다.   
- 원한다면 getter/setter 앞에 가시성 변경자를 추가하여 접근자의 가시성을 변경할 수 있다.  

```kotlin
class LengthCounter {
    var counter: Int = 0
        private set     // 외부에서 counter 프로퍼티의 값을 변경할 수 없도록 setter를 private으로 지정함

    fun addWord(word: String) {
        counter += word.length
    }
}
```

<br/><br/>

---

<br/><br/>

# 4.3 컴파일러가 생성한 메소드: 데이터 클래스와 클래스 위임
코틀린 컴파일러는 `equals`, `toString`, `hashCode` 등의 메서드를 구현해주어, 이런 필수 메서드를 구현해야하는 번잡함을 줄여준다.  

<br/>
  
## 4.3.2 데이터 클래스: 모든 클래스가 정의해야 하는 메서드 자동 생성
- `data`라는 변경자를 클래스 앞에 붙여주면, 필요한 메서드를 컴파일러가 자동으로 만들어 준다.  
    - 이 클래스를 **데이터 클래스**라 부른다.  
- 데이터 클래스는 아래 메서드들을 포함한다
    - 인스턴스 간 비교를 위한 `equals()`
    - `HashMap`과 같은 해시 기반 컨테이너에서 키로 사용할 수 있는 `hashCode()`
    - 클래스의 각 필드를 선언 순서대로 표시하는 문자열 표현을 만들어주는 `toString()`
    - 객체를 복사할 수 있는 `copy()`

```kotlin
data class Client(val name: String, val postalCode: Int) 
```

### **데이터 클래스와 불변성: `copy()`**
`copy()`를 이용해 객체를 복사할 수 있다.  
복사본은 원본과 다른 생명주기를 가지며, 복사를 하면서 일부 프로퍼티 값을 바꾸거나 복사본을 제거하여도, 원본을 참조하는 다른 부분에는 전혀 영향을 끼치지 않으므로, 불변으로 만든 원본 클래스를 이용하여 다른 작업을 하고자 할 때 유용하게 사용할 수 있다.  
```kotlin
data class Client(val name: String, val postalCode: Int) 

fun main() {
    val origin = Client("isoo", 1)
    val copy = origin.copy(postalCode = 2)  
}
```

<br/>

## 4.3.3 클래스 위임: `by` 키워드 사용
- 인터페이스를 구현할 때 `by` 키워드를 사용해 그 인터페이스에 대한 구현을 다른 객체에 위임 중이라는 사실을 명시할 수 있다.  
    - == ~~귀찮은~~ 구현을 떠맡길 수 있다.  
    - 컴파일러가 메서드들을 자동으로 생성해준다!  
- 메서드 중 동작을 변경하고 싶은 메서드가 있다면 오버라이드하면 된다.    
```kotlin
class CountingSet<T>(val innerSet: MutableCollection<T> = HashSet() 
) : MutableCollection<T> by innerSet {  // MutableCollection에 대한 구현을 innerSet에게 위임함 
    var objectAdded = 0

    // 기존 (위임된)메서드를 오버라이드함
    override fun add(element: T): Boolean {
        objectAdded++
        return innerSet.add(element)
    }

    // 기존 (위임된)메서드를 오버라이드함
    override fun addAll(elements: Collection<T>): Boolean {
        objectAdded += elements.size
        return innerSet.addAll(elements)
    }
}
```
`CountingSet`의 코드는 위임 대상 내부 클래스인 `MutableCollection`의 문서화된 API를 활용하기 때문에 `MutableCollection`의 API가 변경되지 않는 한 계속 잘 작동할 것이다.    

<br/><br/>

---

<br/><br/>

# 4.4 object 키워드: 클래스 선언과 인스턴스 생성
- `object`키워드는 클래스를 정의하면서 동시에 인스턴스(객체)를 생성한다.
-  `object`키워드를 사용하는 케이스  
    - 객체 선언(object declaration)은 **싱글턴을 정의**하는 방법 중 하나다. 
        - 클래스 안에서 (중첩으로) '객체 선언'을 할 수도 있다.  
    - 동반 객체(companion object)는 인스턴스 메서드는 아니지만, 어떤 클래스와 관련있는 메서드와 팩토리 메서드를 담을 때 쓰인다. 
        - 동반 객체 메서드에 접근할 때는 동반 객체가 포함된 클래스의 이름을 사용할 수 있다.  
    - 객체 식은 자바의 무명 내부 클래스(anonymous inner class) 대신 쓰인다.    
  
<br/>

## 4.4.1 객체 선언: 싱글턴을 쉽게 만들기
아래와 같이 유틸리티 역할인 `Comparator`를 싱글턴으로 만들고자 할 때, 아래와 같이 `object` 키워드를 사용할 수 있다.
```kotlin
object CaseInsensitiveFileComparator : Comparator<File> {
    override fun compare(f1: File, f2: File): Int {
        return f1.path.compareTo(f2.path, ignoreCase = true)
    }
}

fun main() {
    val files = listOf(File("/z"), File("/a"))
    files.sortedWith(CaseInsensitiveFileComparator)
}
```

<br/>

## 4.4.2 동반 객체: 팩토리 메서드와 정적 멤버가 들어갈 장소
- 클래스 안에 정의된 객체 중 하나에 `companion`을 붙이면 그 클래스의 동반 객체로 만들 수 있다. 
- 동반 객체의 프로퍼티나 메소드에 접근하려면 그 동반 객체가 정의된 클래스의 이름을 사용하면 된다.  

```kotlin
class A {
    companion object {
        fun bar() {
            println("Companion object called.")
        }
    }
}

fun main() {
    A.bar()
}
```

- 동반 객체는 자신을 둘러싼 클래스의 모든 `private` 멤버에 접근할 수 있다. 
    - 바깥쪽 클래스의 `private` 생성자도 호출할 수 있다.  
    - ==> 동반 객체는 팩토리 패턴을 구현하기 좋다!    

```kotlin
// 주 생성자를 private으로 만들어버림
class User private constructor(val nickname: String) {

    // 동반 객체 선언
    companion object {
        fun newSubscribingUser(email: String) = User(email.substringBefore('@'))
    }
}

fun main() {
    User.newSubscribingUser("isoo@mail.com")    // 팩토리 메서드를 통해서만 User를 만들 수 있다
}
```
  
<br/>

## 4.4.3 동반 객체를 일반 객체처럼 사용
- 동반 객체는 클래스 안에 정의된 일반 객체다. 
    - 동반 객체에 이름을 붙이거나, 동반 객체가 인터페이스를 구현하거나, 동반 객체 안에 확장 함수와 프로퍼티를 정의할 수도 있다.   
    - 만약 특별히 이름을 지정하지 않으면, 동반 객체의 이름은 자동으로 `Companion`이 된다.  
- 클래스에 동반 객체가 있으면, 그 객체 안에 함수를 정의하여 클래스에 대해 호출할 수 있는 확장 함수를 만들 수 있다.  

<br/>

## 4.4.4 객체 식: 무명 내부 클래스를 다른 방식으로 작성
- `object` 키워드는 무멍 객체(anonymous object)를 정의할 때도 쓰인다.   
- 무명 객체는 클래스를 정의하고 그 클래스에 속한 인스턴스를 생성하지만, 그 클래스나 인스턴스에 이름을 붙이진 않는다.   
    - 만약 이름을 붙이고 싶다면, 무명 객체를 변수에 대입하면 된다. ==> `val listener = object : MouserAdaptor() { ...`    
- 코틀린의 무명 클래스는 여러 인터페이스를 구현하거나, 클래스를 확장하면서 인터페이스를 구현할 수 있다. 
- '객체 선언'과는 달리 무명 객체는 **싱글턴이 아니다**. '객체 식'이 쓰일 때마다 새로운 인스턴스가 생성됨!  
- 무명 객체는 그 객체가 속한 함수의 변수에 접근할 수 있다. 그리고 `final`이 아닌 변수에도 접근할 수 있고, 그 변수의 값을 변경할 수도 있다.  
