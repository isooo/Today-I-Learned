# **CH8 컬렉션 API 개선**
리스트, 집합, 맵을 쉽게 만들 수 있도록 자바 9에 추가된 컬렉션 팩토리를 살펴보자.   
자바 8의 개선 사항으로 리스트와 집합에서 요소를 삭제하거나 바꾸는 관용패턴을 적용하는 방법을 알아보자.   
맵 작업과 관련해 추가된 새로운 기능을 살펴보자.  


- 자바 9는 적의 원소를 포함하며 바꿀 수 없는 리스트, 집합, 맵을 쉽게 만들 수 있도록 `List.of`, `Set.of`, `Map.of`, `Map.ofEntries` 등의 컬렉션 팩토리를 지원한다.
- 이들 컬렉션 팩토리가 반환한 객체는 immutable하다(**바꿀 수 없다**)
- `List` 인터페이스는 `removeIf`, `replaceAll`, `sort` 등 3가지 티폴트 메서드를 지원한다.
- `Set` 인터페이스는 `removeIf` 디폴드 메서드를 지원한다.
- `Map` 인터페이스는 자주 사용하는 패턴과 버그를 방지할 수 있도록 다양한 디폴트 메서드를 지원한다.
- `ConcurrentHashMap`은 `Map`에서 상속받은 새 디폴트 메서드를 지원함과 동시에 스레드 안전성도 제공한다. 

--- 

## **목차**
- [8.1 컬렉션 팩토리](#81-컬렉션-팩토리)
- [8.2 리스트와 집합 처리](#82-리스트와-집합-처리)
- [8.3 맵 처리](#83-맵-처리)
- [8.4 개선된 ConcurrentHashMap](#84-개선된-concurrenthashmap)

---

# 8.1 컬렉션 팩토리
자바 8에선 `List`를 아래와 같은 방법으로 만들 수 있다.
```java
final List<String> friendsA = new ArrayList<>();
friendsA.add("Mike");
friendsA.add("Alice");
friendsA.add("Tom");

final List<String> friendsB = Arrays.asList("Mike", "Alice", "Tom");
```

하지만 `friendsB`에는 새 요소를 추가하거나 요소를 삭제할 수 없다. (갱신은 가능)  
```java
friendsB.set(0, "Jane1");
// friendsB.add("Jane2");      // UnsupportedOperationException 발생
// friendsB.add(0, "Jane3");   // UnsupportedOperationException 발생
// friendsB.remove(0);         // UnsupportedOperationException 발생 
```

### `UnsupportedOperationException` 예외 
이 예외가 발생하는 이유는 `friendsB`가 **내부적으로 고정된 크기의 변환할 수 있는 배열**로 구현되었기 때문이다.     
> 자세한 설명은 [isooo 블로그: [Java] ArrayList vs ArrayList](https://isooo.github.io/etc/2019/01/13/Arrays-asList.html) 참고 :sunglasses:   
  
<br/>  

집합<sub>`Set`</sub>은 아래와 같은 방식으로 만들 순 있다.  
```java
final Set<String> friendsC = new HashSet<>(Arrays.asList("Mike", "Alice", "Tom"));

final Set<String> friendsD = Stream.of("Mike", "Alice", "Tom").collect(Collectors.toSet());
```
하지만 이 방식은 내부적으로 불필요한 객체 할당이 이뤄지며, mutable한 상태다.  

`Map`은 자바 9부터 팩토리 메서드가 제공되어 쉽게 만들 수 있다.    
이제 자바 9에서 `List`, `Set`, `Map`을 쉽게 만들수 있도록 제공해주는 팩토리 메서드를 차례로 알아보자.     
       
<br/>  
  
## **`List` 팩토리**
- `List.of` 팩토리 메서드를 이용해 간단하게 `List`를 만들 수 있다.  
    ```java
    final List<String> friends = List.of("Mike", "Alice", "Tom");
    ```
  
- `List.of`로 생성된 `List`는 `ImmutableLis`로 만들어지기 때문에 요소를 변경할 수 없다.  
    ```java
    // friends.set(0, "Jane1");   // UnsupportedOperationException 발생
    // friends.add("Jane2");      // UnsupportedOperationException 발생
    // friends.add(0, "Jane3");   // UnsupportedOperationException 발생
    // friends.remove(0);         // UnsupportedOperationException 발생
    ```

- `List.of`의 요소에 `null`을 넣으면 NPE가 발생한다.
  
<br/>
  
> ### 오버로딩 vs 가변 인수
> `List.of`는 다양한 버전으로 오버로딩된 메서드들이 있다.  
> ```java
> static <E> List<E> of()
> static <E> List<E> of(E e1) 
> static <E> List<E> of(E e1, E e2)
> static <E> List<E> of(E e1, E e2, E e3)
> // ... (인자 10개를 가진 메소드까지 존재함)
> ```
> 이쯤되면 `저렇게 오버로딩 여러 개를 만들지 않고, 가변 인수를 이용해 다중 요소를 받으면 될텐데?` 하는 의문이 들 수 있는데 :eyes:  ==> 물론 존재한다!    
> ```java
> static <E> List<E> of(E... elements)
> ```
> 이 가변 인수 버전은 내부적으로 추가 배열을 선언하여 `ListN`으로 감싼다. 따라서 배열을 할당하고 초기화하며 나중에는 가비지 컬렉션하는 비용까지 지불해야 한다.   
> 그래서 고정된 숫자의 요소(최대 10개)로 `List`를 만들 땐 API를 제공하여 이러한 비용을 줄이는 것이다. (`List.of`로 10개 이상의 요소를 가진 리스트를 만든다면 [가변 인수를 이용하는 메서드]가 사용되는 것이고..)    
> 뒤이어 살펴볼 `Set.of`나 `Map.of`도 동일한 패턴으로 되어있다.  

<br/>

## **`Set` 팩토리**
- `Set.of` 팩토리 메서드를 이용해 간단하게 `Set`을 만들 수 있다.  
    ```java
    final Set<String> friends = Set.of("Mike", "Alice", "Tom");
    ```
- 중복된 요소로 `Set`을 만들려고 하면 `IllegalArgumentException`이 발생한다. 오직 **고유한 요소로만 생성**할 수 있다.
    ```java
    Set.of("Mike", "Alice", "Tom", "Tom"); // IllegalArgumentException: duplicate element: Tom
    ```

<br/>

## **`Map` 팩토리**
- 10개 이하의 요소를 가진 `Map`이라면 아래와 같이 key와 value를 번갈아 선언하는 방식으로 만드는게 유용하다.  
    ```java
    final Map<String, Integer> numberOfFriends
            = Map.of("Mike", 30, "Alice", 31, "Tom", 32);
    ```

- 만약 10개를 초과한다면 `Map.Entry<K,V>`를 인수로 받으며 가변 인수로 구현된 `Map.ofEntries`라는 팩토리 메서드를 이용하는 것이 좋다.  
    ```java
    final Map<String, Integer> numberOfFriends = Map.ofEntries(
            Map.entry("Mike", 30),
            Map.entry("Alice", 31),
            Map.entry("Tom", 32)
    );
    ```
> `Map.entry(K k, V v)`는 `Map.Entry<K, V>`를 리턴하는 팩토리 메서드다. (`Entry`는 인터페이스다)  
  
<br/><br/>

---

<br/><br/>

# 8.2 리스트와 집합 처리
자바 8에서는 `List`와 `Set`인터페이스에 다음과 같은 메서드가 추가되었다. 
- `removeIf`
    - `List`, `Set` 모두 제공
    - `Predicate`를 만족하는 요소를 제거한다 (e.g. `friends.removeIf(s -> s.length() > 3)`)
    - 요소 중 하나라도 제거된다면 true를, 제거된 요소가 없다면 false를 리턴한다
- `replaceAll`
    - `List`에서 제공
    - `UnaryOperator`함수를 이용해 요소를 바꾼다 (e.g. `friends.replaceAll(s -> s.concat("plus"))`)
- `sort`
    - `List`에서 제공
  
<sub>(스트림은 새로운 결과를 만들어 내지만)</sub> 이들 메서드는 호출한 컬렉션 자체를 바꾼다.    
컬렉션을 바꾸는 동작은 사실 에러를 유발하기 쉽고 복잡함을 증가시키기 때문에, 자바 8에서는 그 불편함을 감소시키고자 이 메서드를 제공한다.     

<br/>

<details>
<summary><italic>예시로 사용될 코드: 거래자 리스트와 트랜잭션 리스트</italic></summary>
<div markdown="1">

```java
class Trader {
    private final String name;
    private final String city;

    public Trader(final String name, final String city) {
        this.name = name;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return "Trader{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}

class Transaction {
    private final Trader trader;
    private final int year;
    private final int value;

    public Transaction(final Trader trader, final int year, final int value) {
        this.trader = trader;
        this.year = year;
        this.value = value;
    }

    public Trader getTrader() {
        return trader;
    }

    public int getYear() {
        return year;
    }

    public int getValue() {
        return value;
    }

    // replaceAll 테스트를 위해 추가한 코드 
    public Transaction changeValue(final int value) {
        return new Transaction(this.trader, this.year, this.value + value);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "trader=" + trader +
                ", year=" + year +
                ", value=" + value +
                '}';
    }
}
```

```java
final Trader raoul = new Trader("Raoul", "Cambridge");
final Trader mario = new Trader("Mario", "Milan");
final Trader alan = new Trader("Alan", "Cambridge");
final Trader brian = new Trader("Brian", "Cambridge");

final List<Transaction> transactions = new ArrayList();
transactions.add(new Transaction(brian, 2011, 300));
transactions.add(new Transaction(raoul, 2012, 1000));
transactions.add(new Transaction(brian, 2011, 400));
transactions.add(new Transaction(mario, 2012, 710));
transactions.add(new Transaction(brian, 2012, 700));
transactions.add(new Transaction(alan, 2012, 950));
```

---
  
</div>
</details>

<br/>

## **`removeIf`**
`transactions` 중에서 `value > 500`인 요소는 삭제해보자.   
```java
for (Transaction t : transactions) {
    if (t.getValue() > 500) {
        transactions.remove(t);
    }
}
```
이 코드는 `ConcurrentModificationException`가 발생시킨다.  
  
`for-each`루프는 내부적으로 `Iterator`객체를 사용한다. 그래서 아래와 같이 해석된다.  
```java
for (Iterator<Transaction> iterator = transactions.iterator();
        iterator.hasNext();
) {
    final Transaction t = iterator.next();
    if (t.getValue() > 500) {
        transactions.remove(t);
    }
}
```
여기서 2개의 개별된 객체가 컬렉션에 접근한다는 사실을 주목하자.
- `Iterator`
    - `hasNext()`와 `next()`를 통해 소스에 질의
- `Collection`
    - `remove()`를 호출해 요소를 삭제

결과적으로 `Iterator`의 상태는 `Collection`의 상태와 서로 동기화 되지 않는다!   
이 문제는 `Iterator`의 `remove()`를 호출하여 해결할 수 있다.
```java
for (
        Iterator<Transaction> iterator = transactions.iterator();
        iterator.hasNext();
) {
    final Transaction t = iterator.next();
    if (t.getValue() > 500) {
        iterator.remove();  
    }
}
```

하지만 이렇게 해결하는 방법보다 더 간단한 방법이 자바 8에서 제공하는 `removeIf`를 이용하는 것이다.
```java
transactions.removeIf(t -> t.getValue() > 500);
```
  
<br/>

## **`replaceAll`**
이번엔 요소를 바꾸어보자.    

`transactions` 내 모든 요소의 `value`에 1000을 추가해보자.     
```java
final List<Transaction> collect = transactions.stream()
        .map(t -> t.changeValue(1000))
        .collect(Collectors.toList());
```
이렇게 만들면 `collect`라는 새로운 `Transaction` 컬렉션이 만들어진다.  
원래 요구 사항은 기존 `transactions`의 `value`를 변경하는 것이었다.
```java
for (ListIterator<Transaction> iterator = transactions.listIterator();   // ListIterator 객체는 요소를 바꾸는 set() 메서드를 지원한다.
        iterator.hasNext();
) {
    final Transaction t = iterator.next();
    iterator.set(t.changeValue(1000));
}
```
이렇게 하면 기존 `transactions`의 요소를 바꿀 수 있지만..   
   
이 역시 자바 8에서 제공하는 기능을 이용하면 간단하게 구현할 수 있다.   
```java
transactions.replaceAll(t -> t.changeValue(1000));
``` 
      
<br/><br/>

---

<br/><br/>

# 8.3 맵 처리
자바 8에서는 `Map`인터페이스에 몇 가지 디폴트<sub>default</sub> 메서드가 추가됐다.(default method는 추후 CH13에서 자세히 다룰 예정)  

<br/>

## **`forEach` 메서드**
`Map.Entry<K, V>`의 반복자를 이용해 `Map`의 항목 집합을 반복할 수 있다.  
```java
final Map<String, Integer> numberOfFriends = Map.ofEntries(
        Map.entry("Mike", 33),
        Map.entry("Alice", 31),
        Map.entry("Tom", 32)
);

for (Map.Entry<String, Integer> entry : numberOfFriends.entrySet()) {
    final String friend = entry.getKey();
    final Integer age = entry.getValue();
    System.out.println(friend + " is " + age + " years old");
}
```
    
자바 8부터는 `Map`인터페이스에서 `BiConsumer<? super K, ? super V> action`를 인수로 받는 `forEach`메서드를 지원한다.  
이 `forEach`를 사용하여 위 구현을 아래와 같이 간단히 나타낼 수 있다.   
```java
numberOfFriends.forEach((friend, age) -> System.out.println(friend + " is " + age + " years old"));
```
  
<br/>
  
## **정렬 메서드**
아래 2개의 유틸리티가 지원되어 `Map`의 항목을 key 또는 value 기준으로 손쉽게 정렬할 수 있다.  
- `Entry.comparingByKey`
- `Entry.comparingByValue`
  
```java
numberOfFriends.entrySet().stream()
        .sorted(Map.Entry.comparingByKey()) // numberOfFriends 컬렉션 자체를 변경하는 것이 아님
        .forEachOrdered(System.out::println);
```
```
Alice=31
Mike=30
Tom=32
```

> `forEachOrdered`는 스트림의 순서를 그대로 유지하여 처리하는 메서드다. 병렬성을 포기하고 순서대로 값을 처리함.

<br/>

> ### **`HashMap` 성능**
> 자바 8에서는 `HashMap`의 내부 구조를 바꿔 성능을 개선했다.  
> - 기존
>   - key로 생성한 해시코드로 접근할 수 있는 버켓에 `Map` 요소를 저장했다. 
>   - 동일한 해시코드를 반환하는 key가 많아질 경우, `O(n)`만큼의 시간이 걸리는 `LinkedList`로 버킷을 반환해야 하므로 성능이 저하된다.  
> - 자바 8
>   - 버킷이 너무 커질 경우 이를 `O(log(n))`의 시간이 소요되는 정렬된 트리를 이용하여, 동적으로 치환해 충돌이 일어나는 요소 반환 성능을 개선했다. 
>       - 이는 key가 `String`, `Number` 클래스처럼 `Comparable`한 형태여야만 정렬된 트리로 지원된다.  
  
<br/>
  
## **`getOrDefault` 메서드**
이전에는 찾으려는 key가 존재하지 않으면 null이 반환되어, NPE에 대한 방어 로직을 추가해 주었어야 했다.  
하지만 자바 8부터는 `getOrDefault` 메서드를 이용하면 요청한 key가 `Map`에 존재하지 않을 때 동작할 로직을 지정할 수 있다.      
> 물론 key에 해당하는 value가 null이라면 `getOrDefault`가 null을 반환할 수 있다.   

```java
numberOfFriends.getOrDefault("Olivia", 20)  // 20
numberOfFriends.getOrDefault("Alice", 20)  // 31
```

<br/>

## **계산 패턴**
자바 8에서는 key값이 존재하는지 여부에 따라 실행할 동작을 지정할 수 있는 패턴을 몇 가지 제공한다.    
- `computeIfAbsent`
    - 제공된 key값이 존재하지 않는다면(또는 null이라면), key를 이용해 새 값을 계산하고 map에 추가한다
- `computeIfPresent`
    - 제공된 key값이 존재하면 새 값을 계산하고 map에 추가한다
    - 단 key에 대한 value가 null이 아닐 때만 새 값을 계산한다. 
- `compute`
    - 제공된 key값으로 새 값을 계산하고 map에 저장한다

```java
final Map<String, Integer> numberOfFriends = new HashMap<>();
numberOfFriends.put("Mike", 33);
numberOfFriends.put("Alice", 31);
numberOfFriends.put("Tom", 32);
numberOfFriends.put("nullnull", null);

numberOfFriends.computeIfAbsent("Olivia", k -> calculateAge(k)); 
// "Olivia" 라는 key가 존재하지 않으면, ["Olivia", k -> calculateAge(k)]라는 요소를 numberOfFriends에 추가

numberOfFriends.computeIfPresent("Alice", (k, v) -> v + 10);
// "Alice" 라는 key가 존재한다면, "Alice"의 value를 (value + 10)으로 업데이트
numberOfFriends.computeIfPresent("nullnull", (k, v) -> 10);
// "nullnull"에 대한 value가 null이므로, BiFunction 로직이 null로 리턴됨.

// numberOfFriends.compute("kate", (k, v) -> v + 10); // NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "v" is null
numberOfFriends.compute("Tom", (k, v) -> v + 10); // "Tom"이라는 key가 존재하므로, "Tom"의 value를 (value + 10)으로 업데이트
```
  
<br/>

`computeIfPresent`메서드는 `값을 만드는 함수`가 null을 반환하면, 현재 map에서 해당 요소를 제거해버린다.
```java
final Map<String, String> map = new HashMap<>();
map.put("A", "non null");
map.put("B", "non null");

System.out.println(map);

map.computeIfPresent("A", (key, value) -> "hello");
map.computeIfPresent("B", (key, value) -> null);    // 값을 만드는 함수가 null을 리턴

System.out.println(map);
```
```
{A=non null, B=non null}
{A=hello}       // "B"가 없어졌음
```

> 이러한 방법으로 map에서 요소를 제거할 수도 있긴 하지만, 권장되는 방법은 `remove` 메서드를 오버라이드하여 사용하는 것이다. 
   
<br/>
   
## **삭제 패턴**
`Map#remove`는 제공된 key에 해당하는 항목을 제거하는 메서드다.  
자바 8에서는 key가 특정한 값과 연관되었을 때만 항목을 제거하는 오버로드 버전 메서드를 제공한다.   

```java
final Map<String, String> favouriteMovies = new HashMap<>();
favouriteMovies.put("Raphael", "Jack Reacher 2");
favouriteMovies.put("Thibaut", "Matrix");

final String key = "Raphael";
final String value = "Jack Reacher 2";

if (favouriteMovies.containsKey(key) && Objects.equals(favouriteMovies.get(key), value)) {
    favouriteMovies.remove(key);
    System.out.println(true);       // true 출력됨
} else {
    System.out.println(false);
}
```
  
이걸 다음과 같이 간결하게 나타낼 수 있다.
```java
System.out.println(favouriteMovies.remove("Raphael", "Jack Reacher 2"));    // true 출력됨
```

<br/>

## **교체 패턴**
`Map`의 항목을 바꾸는 데 사용할 수 있는 2개의 메서드가 추가되었다.  
- `replaceAll`
    - `BiFunction`을 적용한 결과로 각 항목의 value를 교체한다
    - 앞서 살펴보았던 `List#replaceAll`과 비슷한 동작을 수행한다
- `Replace`
    - key가 존재하면 value를 바꾼다
    - key가 특정 value로 매핑되었을 때만 value를 교체하는 버전도 있다

```java
final Map<String, String> favouriteMovies = new HashMap<>();
favouriteMovies.put("Raphael", "Star wars");
favouriteMovies.put("Olivia", "james bond");
// Map.Entry가 아닌 put을 이용하여 map을 만드는 이유는, replaceAll을 사용할 것이기 때문에 mutable한 map을 생성하기 위함

favouriteMovies.replaceAll((friend, movie) -> movie.toUpperCase());
System.out.println(favouriteMovies);
```

<br/>

## **합침**
지금까지 배운 `replace` 패턴은 하나의 `Map`에만 적용할 수 있다.  
만약 2개의 `Map`에서 값을 합치거나 바꿔야 한다면? `merge`메서드를 이용하면 된다!  
   
우선 2개의 맵을 합친다고 가정하자. `putAll`을 사용할 수 있다.
```java
final Map<String, String> family = Map.ofEntries(
        Map.entry("Teo", "Star Wars"), Map.entry("Cristina", "James Bond")
);
final Map<String, String> friends = Map.ofEntries(
        Map.entry("Raphael", "Star Wars")
);

final HashMap<String, String> everyone = new HashMap<>(family);
everyone.putAll(friends);

System.out.println(everyone);
// {Cristina=James Bond, Raphael=Star Wars, Teo=Star Wars}
```
  
위 코드는 중복된 key가 없었기 때문에 잘 동작했다.  
좀 더 유연하게 합쳐야 한다면 `merge`메서드를 이용할 수 있다.  
이 메서드는 중복된 key를 어떻게 합칠 지 결정하는 `BiFunction`을 인수로 받는다.  
  
`family`와 `friends` 두 map에 모두 다른 value를 가진 `Cristina`가 존재한다고 가정하자.  
```java
final HashMap<String, String> everyone = new HashMap<>(family);
friends.forEach((k, v) ->
        everyone.merge(
                k,
                v,
                (movie1, movie2) -> movie1 + " & " + movie2
        )
);

System.out.println(everyone);
// {Raphael=Star Wars, Cristina=James Bond & Matrix, Teo=Star Wars}
```
  
`merge`메서드는 value가 null인 경우 등 복잡한 상황도 처리할 수 있다.
> *지정된 key와 연관된 value가 없거나 null이면, `merge`는 key를 null이 아닌 **value와 연결**한다. 아니면 `merge`는 연결된 value를 주어진 매핑 함수의 결과값으로 대치하거나 결과가 null이면 해당 요소를 제거한다* .    
> - javadoc
  
<br/>  

`merge`를 이용해 초기화 검사를 구현할 수도 있다.   
영화 카운트를 증가시키기 전에 영화가 map에 존재하는지 체크하는 로직이 필요하다고 가정하자.  
```java
final HashMap<String, Long> moviesToCount = new HashMap<>();
final String movieName = "James Bond";
final Long count = moviesToCount.get(movieName);
if (count == null) {
    moviesToCount.put(movieName, 1);
} else {
    moviesToCount.put(movieName, count + 1);
}
```

위 식은 아래와 같이 간단하게 구현할 수 있다.  
```java
moviesToCount.merge(
        movieName, 1L, (k, v) -> count + 1L
);
```
movieName이라는 key에 value가 존재하지 않을 경우, 1L을 value로 매핑시키고,  
존재할 경우 기존 count에 1L을 더한다.  

<br/>

`Map`에도 `removeIf`를 사용해볼 수 있다.  
```java
final HashMap<String, Integer> movies = new HashMap<>();
        movies.put("JamesBond", 20);
        movies.put("Matrix", 15);
        movies.put("Harry Potter", 5);

// value가 10 미만인 요소는 제거
movies.entrySet().removeIf(key -> key.getValue() < 10);
// {Matrix=15, JamesBond=20}
```
  
<br/><br/>

---

<br/><br/>

# 8.4 개선된 ConcurrentHashMap
`ConcurrentHashMap` 클래스는 동시성 친화적인 `HashMap` 최신 버전이다. 내부 자료구조의 특정 부분만 잠궈 동시 추가/갱신 작업을 허용한다. 따라서 동기화된 `HashTable`버전에 비해 읽기/쓰기 연산 성능이 월등하다. (참고로 표준 `HashMap`은 비동기로 동작한다. )

<br/>

## **리듀스와 검색**
`ConcurrentHashMap`은 아래와 같은 새로운 연산을 지원한다.
- `forEach`
    - 각 KV 쌍에게 주어진 동작을 실행
- `reduce`
    - 모든 KV 쌍을 제공된 reduce함수를 이용해 결과로 합침
- `search`
    - null이 아닌 값을 반환할 때까지 각 KV 쌍에 함수를 적용
  
> `ConcurrentHashMap`에서 지원되는 연산 형태
> - key, value로 연산
>     - `forEach`, `reduce`, `search`
> - key로 연산
>     - `forEachKey`, `reduceKeys`, `searchKeys`
> - value로 연산
>     - `forEachValue`, `reduceValues`, `searchValues`
> - `Map.Entry` 객체로 연산
>     - `forEachEntry`, `reduceEntries`, `searchEntries`
  
이들 연산은 `ConcurrentHashMap`의 상태를 잠그지 않고 연산하기 때문에, 이들 연산에 제공된 함수는 계산이 진행되는 동안 바뀔 수 있는 객체, 값, 순서 등에 의존하지 않아야 한다.  
  
또한 이들 연산에 병렬성 기준값<sub>thredhold</sub>을 지정해야 한다. 맵의 크기가 주어진 기준값보다 작으면 순차적으로 연산을 실행한다. 고로 기준값을 1로 지정하면(엄청 작게 지정) 공통 스레드 풀을 이용해 병렬성을 극대화한다. 반대로 `Long.MAX_VALUE`를 기준값으로 설정하면 1개의 스레드로 연산을 실핸한다. 
> 내 컴퓨터의 소프트웨어 아키텍쳐가 고급 수준의 자원 활용 최적화를 사용하고 있지 않다면, 기준값 규칙을 따르는 것이 좋다   
아래 예제에서는 `reduceValues` 메서드를 이용해 map의 최댓값을 찾는다.  
```java
final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
final long parallelismThreshold = 1;
final Optional<Long> maxValue = Optional.ofNullable(map.reduceValues(parallelismThreshold, Long::max));
```

<br/>

## **계수**
`ConcurrentHashMap`은 해당 map의 매핑 개수를 반환하는 `mappingCount`메서드를 제공한다. 반환 타입은 int이며, 매핑 개수가 int의 범위를 넘어서는 이후의 상황을 대처할 수 있다.  

<br/>

## **집합뷰**
`ConcurrentHashMap`를 집합 뷰로 반환하는 `keySet`이라는 새로운 메서드를 제공한다.  
map을 바꾸면 set도 바뀌고, 반대로 set을 바꾸면 map도 영향을 받는다.  
`newKeySet`이라는 메서드를 이용해 `ConcurrentHashMap`으로 유지되는 `Set`을 만들 수도 있다. 
