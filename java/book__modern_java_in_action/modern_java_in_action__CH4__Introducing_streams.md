# **CH4. 스트림 소개**

## 목차
- [4.1 스트림이란 무엇인가?](#41-스트림이란-무엇인가)
- [4.2 스트림 시작하기](#42-스트림-시작하기)
- [4.3 스트림과 컬렉션](#43-스트림과-컬렉션)
- [4.4 스트림 연산](#44-스트림-연산)

<br/><br/>

---

<br/><br/>

# 4.1 스트림이란 무엇인가?
**스트림**<sub>Stream</sub>
- 자바 8 API에서 추가된 기능
- 컬렉션과 같은 대량의 데이터를 **선언형**으로 처리할 수 있도록 도와줌
    - 질의 형태로 데이터 처리 가능
        - sql처럼 직접적으로 내가 원하는 것을 표현하는 방식. (What에 대한 걸 서술. How는 기술하지 않아도 됨)  
        - e.g. 수학 점수가 100점인 학생 필터링, 칼로리가 100 이상인 메뉴의 담당 요리사 이름을 알파벳 순으로 조회 등 
- **데이터 처리 연산을 지원**하도록, **소스에서 추출된 연속된 요소**<sub>Sequence of elements</sub>
    > - ***데이터 처리 연산***
    >   - 스트림은 '함수형 프로그래밍 언어에서 일반적으로 지원하는 연산', 'DB와 비슷한 연산'을 지원한다. 
    >   - e.g. `filter`, `map`, `sort` 등으로 데이터를 조작할 수 있다. 
    >   - 스트림 연산은 순차적으로 또는 병렬로 실행할 수 있다.     
    > - ***소스***
    >   - 스트림은 데이터를 제공하는 소스로부터 데이터를 소비한다.   
    > - ***연속된 요소***
    >   - 스트림은 특정 요소 형식으로 이루어진 연속된 값 집합의 인터페이스를 제공한다. 
    >         - `컬렉션`은 **자료구조**이므로 **데이터 자체**가 주제이고, 요소 저장 및 접근 연산이 주를 이룬다. 
    >       - `스트림`은 **표현 계산**이 주를 이루며, 계산이 주제이다.      
  
<br/>

예를 들어, `저칼로리인 요리명을 반환하되, 칼로리를 기준으로 정렬`하는 기능을 구현하고자 한다.  
먼저 자바 7로 구현해보자.  
```java
final List<Dish> lowCaloricDishes = new ArrayList<>();

for (Dish d : menu) {
    if (d.getCalories() < 400) {
        lowCaloricDishes.add(d);
    }
}

Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
    @Override
    public int compare(final Dish d1, final Dish d2) {
        return Integer.compare(d1.getCalories(), d2.getCalories());
    }
});

final List<String> lowCaloricDishesName = new ArrayList<>();

for (Dish d : lowCaloricDishes) {
    lowCaloricDishesName.add(d.getName());
}
```

위 코드에서는 `lowCaloricDishes` 라는 *가비지 변수*가 사용되었다. 연산 처리 시 컨테이너 역할만 하는 변수를 뜻한다.   
자바 8은 이러한 세부 구현을 라이브러리 내에서 처리하도록 한다.  
    
이제 자바 8로 구현해보자.     
```java
final List<String> lowCaloricDishesName = menu.stream()
        .filter(d -> d.getCalories() < 400)
        .sorted(Comparator.comparing(Dish::getCalories))
        .map(Dish::getName)
        .collect(Collectors.toList());
```

코드의 길이가 짧아져 가독성이 늘었으며, 의미도 더 명확하게 드러난다! :+1:  
  
여기서 `stream()`을 `parallelStream()`으로 바꾸면 이 코드를 멀티코어 아키텍처에서 병렬로 실행할 수 있다.  
```java
final List<String> lowCaloricDishesName = menu.parallelStream()
        .filter(d -> d.getCalories() < 400)
        .sorted(Comparator.comparing(Dish::getCalories))
        .map(Dish::getName)
        .collect(Collectors.toList());
```
    
> *병렬처리에 대한 내용(`parallelStream()`)은 추후 [책 CH7](./modern_java_in_action__CH7__Parallel_data_processing_and_performance.md#71-병렬-스트림)에서 다룰 예정*    
    
<br/>  
  
위 코드처럼 `filter`, `sorted`, `map`, `collect` 같은 여러 연산을 연결하여 복잡한 데이터를 처리하는 파이프라인을 만들 수 있으며, 읽기 쉽고 의미도 명확히 전달된다.      
`filter` 등의 연산은 고수준 빌딩 블록<sub>high-level building block</sub>으로 이루어져 있어, 특정 스레딩 모델에 제한되지 않고, 어떤 상황에서든 자유롭게 사용할 수 있다. 또한 이들은 내부적으로 단일 스레드 모델에 사용할 수 있지만 멀티코어 아키텍처를 최대한 투명하게 활용할 수 있게 구현되어 있다. 결과적으로 우리는 데이터 처리 과정을 **병렬화**하면서 **스레드와 락을 걱정할 필요가 없**다.      
  
<br/><br/>

---

<br/>

<details>
<summary>참고: 앞으로 사용될 예제 코드</summary>
<div markdown="1">

```java
public class Dish {
    private final String name;
    private final boolean vegetarian;
    private final int calories;
    private final Type type;

    public Dish(final String name, final boolean vegetarian, final int calories, final Type type) {
        this.name = name;
        this.vegetarian = vegetarian;
        this.calories = calories;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public int getCalories() {
        return calories;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "name='" + name + '\'' +
                ", vegetarian=" + vegetarian +
                ", calories=" + calories +
                ", type=" + type +
                '}';
    }
    public enum Type {
        MEAT, FISH, OTHER
    }
}
```

```java
final List<Dish> menu = Arrays.asList(
        new Dish("season fruit", true, 120, Dish.Type.OTHER),
        new Dish("pork", false, 800, Dish.Type.MEAT),
        new Dish("salmon", false, 450, Dish.Type.FISH),
        new Dish("prawns", false, 280, Dish.Type.FISH),
        new Dish("chicken", false, 400, Dish.Type.MEAT),
        new Dish("french fries", true, 530, Dish.Type.OTHER),
        new Dish("rice", true, 350, Dish.Type.OTHER),
        new Dish("pizza", true, 550, Dish.Type.OTHER),
        new Dish("beef", false, 700, Dish.Type.MEAT)
);
```

---

</div>
</details>

<br/><br/>

# 4.2 스트림 시작하기
스트림 특징 
- **파이프라이닝**<sub>Pipelining</sub>
    - 대부분의 스트림 연산은 스트림 연산끼리 연결하여 하나의 파이프 라인을 구성할 수 있도록 **스트림 자신을 반환**한다. 그 덕분에 laziness, short-circuiting(쇼트서킷) 같은 최적화도 얻을 수 있다(*==> 이건 CH5에서 추가 설명함*). 
- **내부 반복**
    - 스트림은 내부 반복을 지원한다,
        - 컬렉션은 `iterator().next()` 처럼 반복자를 이용하여 명시적으로 반복해 줘야 함

<br>

아래 코드를 통해 스트림에 대해 좀 더 알아보자. 구하고자 하는 것은 `300 칼로리를 초과한 요리 3개의 이름`이다.    
```java
// pork, salmon, chicken
final List<String> threeHighCaloricDishNames = menu.stream()
        .filter(d -> d.getCalories() > 300)
        .map(Dish::getName)
        .limit(3)
        .collect(Collectors.toList());
```

1. `menu`라는 (요리 리스트) 콜렉션의 `stream()`을 호출하여 스트림을 얻어냈다.
    - 이때 **데이터 소스**는 `menu`(요리 리스트)이다.
    - 이제 `menu`는 내부적으로 가진 **연속된 요소**를 스트림에 제공한다. 
2. 스트림에 `filter`, `map`, `limit`, `collect`로 연속된 **데이터 처리 연산**이 적용된다.
    - `collect`를 제외한 나머지 연산은 파이프라인을 형성할 수 있도록 스트림 자신을 반환함. 
3. `collect`연산을 통해 파이프라인의 처리 결과를 반환한다. 
    - `collect`가 호출되기 전까지는 `menu`에서 무엇도 선택되지 않으며, 출력 결과도 없다. 
    
스트림을 이용해 구하고자 하는 바를 선언형으로 데이터로 처리할 수 있었다. 그리고 스트림 라이브러리에서 `filter`,이름으로 `mapping`, 3개로 제한한 `limit` 기능 등을 제공해주어 구현을 편리하게 할 수 있었다.    
결과적으로 스트림 API는 파이프라인을 더 최적화할 수 있는 유연성을 제공해주더라!    

<br/><br/>

---

<br/><br/>

# 4.3 스트림과 컬렉션

## **공통점**

### 특정 요소로 이루어진 **연속된 값 집합**
- 이때 `연속된`에는 아무 값에나 접근하는 것이 아닌 **순차적으로 값에 접근한다**는 뜻이 포함됨    

<br/>

## **차이점**  

### 데이터를 **언제** 계산하느냐  
- 컬렉션은 **현재 자료구조가 포함하는 모든 값을 메모리에 저장하는** 자료구조다. 즉, 컬렉션의 모든 요소는 컬렉션에 추가하기 **전에** 계산되어야 한다. (컬렉션에는 요소를 추가하거나 요소를 삭제할 수 **있다**. 그리고 이런 연산을 수행할 때마다 컬렉션의 모든 요소를 메모리에 저장해야 하며, 컬렉션에 추가하려는 요소는 미리 계산되어야 한다.)  
    > 생산자 중심<sub>supplier-driven</sub>: 팔기도 전에 창고를 가득 채움  
- 스트림은 이론적으로 **요청할 때만 요소를 계산**하는 고정된 자료구조다. (스트림에는 요소를 추가하거나 요소를 제거할 수 **없다**) 스트림은 게으르게 만들어지는 컬렉션과 같다. 즉, 사용자가 데이터를 **요청할 때만 값을 계산**한다. 
    > 요청 중심 제조<sub>demand-driven manufacturing</sub>, 즉석 제조<sub>just-in-time manufacturing</sub>  

> e.g. DVD에 저장된 영화 vs 스트리밍 서비스를 이용
> - DVD에는 영화를 추가로 더 저장하거나, 기존 영화를 삭제할 수도 있다. DVD에는 전체 자료구조가 저장되어 있으므로 **컬렉션**으로 볼 수 있다.   
> - 구글 등에서 검색어를 입력했다고 가정하자. 해당 웹 서비스가 가지고 있는 모든 검색 결과를 내려받을 때까지 기다리지 않아도, 상위 10개 혹은 20개의 검색 결과를 포함하는 **스트림**을 얻을 수 있다. 그리고 다음 페이지 버튼을 누르면 그다음 검색 결과를 일정 개수만큼 내려받을 수 있다. 

<br/>

### 스트림은 딱 한 번만 탐색할 수 있다
- 스트림은 `iterator`와 같은 반복자처럼 **한 번만 탐색할 수 있**다. 
    - 탐색된 스트림 요소는 **소비**되며, 만약 한 번 탐색한 요소를 다시 탐색하려면 초기 데이터 소스에서 새로운 스트림을 생성해야 한다. 

<br/>

### 데이터 반복 처리 방법  
- 컬렉션 인터페이스를 사용하려면 사용자가 `for-each`등을 이용해 **직접 요소를 반복**해야 한다. 
    - 이를 **외부 반복**<sub>external iteration</sub>이라 한다.  
    - 명시적으로 컬렉션에서 요소를 하나씩 꺼내와서 처리하는 방식
    - 외부 반복은 병렬성을 스스로 관리해야 한다
        - `synchronized`를 이용하여 직접 구현하는 귀찮고 귀찮은 일을 해야 한.. :expressionless:  
- 스트림 라이브러리는 반복을 **알아서 처리**하고 결과 스트림 값을 어딘가에 저장해 준다. 
    - 이를 **내부 반복**<sub>internal iteration</sub>이라 한다. 
    - 어떤 작업을 수행할지 함수만 지정해 주면, 모든 것이 알아서 처리된다!  
    - 내부 반복을 이용하면 나는 결과적으로 원하는 것을 지시하고, 그 해결은 스트림 내부에서 최적화된 순서로 처리되는 게 장점!
        > *스트림 라이브러리의 내부 반복은 데이터 표현과 하드웨어를 활용한 병렬성 구현을 자동으로 선택한다*
    - 단, 내부 반복을 사용하려면 이 반복되는 연산이 미리 정의되어 있어야 한다. 
        - 내가 직접 구현해야하는 경우도 있는데, 이럴 땐 자바에서 제공해 주는 대부분의 연산이 람다 표현식을 인수로 받기 때문에 동적 파라미터화를 이용해 해결할 수 있다. (*이에 대한 자세한 내용은 CH5에서 계속* :sunglasses: )
```java
// 컬렉션의 for-each 루프를 이용하는 외부 반복
final List<String> namesByExternal = new ArrayList<>();
for (Dish dish : menu) {    // 명시적으로 menu에서 Dish를 하나씩 꺼내어 반복
    namesByExternal.add(dish.getName());
}

// 스트림의 내부 반복
final List<String> namesByInternal = menu.stream()
        .map(Dish::getName)
        .collect(Collectors.toList());
```

<br/><br/>

---

<br/><br/>

# 4.4 스트림 연산
스트림 인터페이스의 연산은 크게 두 가지로 구분할 수 있다.
- **중간 연산**
    - 서로 연결할 수 있어 파이프라인을 형성할 수 있는 스트림 연산
- **최종 연산**
    - 파이프라인을 실행한 다음 스트림을 닫는 연산

```java
final List<String> names = menu.stream()            // menu로부터 스트림 얻기 
        .filter(d -> d.getCalories() > 300)         // 중간 연산
        .map(Dish::getName)                         // 중간 연산
        .limit(3)                                   // 중간 연산
        .collect(Collectors.toList());              // 최종 연산
```

<br/>

## **중간 연산**
- 중간 연산은 다른 스트림을 반환한다.  
- 중간 연산의 중요 특징은 최종 연산을 스트림 파이프라인에 실행하기 전까지는 **아무 연산도 수행하지 않는다**는 것이다. 
    - ==> ***lazy***
- 중간 연산들은 **최종 연산에서 한 번에 처리**한다.  

```java
final List<String> names = menu.stream()
        .filter(d -> {
            System.out.println("filtering: " + d.getName()); 
            return d.getCalories() > 300;
        })
        .map(d -> {
            System.out.println("map: " + d.getName());
            return d.getName();
        })
        .limit(3)
        .collect(Collectors.toList());
```

위 코드를 실행하면 아래의 결과를 받는다. 
```bash
filtering: season fruit
filtering: pork
map: pork
filtering: salmon
map: salmon
filtering: prawns
filtering: chicken
map: chicken
```

<br/>

스트림의 lazy한 특성 덕분에 몇 가지 최적화 효과를 얻을 수 있다.
- `menu`에는 300칼로리가 넘는 음식이 여러 개 있지만 `limit(3)`의 연산 덕분에 오직 처음 3개만 선택되었다. 이는 `쇼트서킷`이라 불리는 기법으로 CH5에서 자세히 설명한다.
- `filter`와 `map`은 서로 다른 연산이지만 한 과정으로 병합되었다. 이는 루프 퓨전<sub>loop fusion</sub>이라고 한다.

<br/>

### **중간 연산 종류**
| 연산 | 반환 타입 | 파라미터 타입 | 함수 디스크립터 |
| - | - | - | - |
| `filter` | `Stream<T>` | `Predicate<T>` | `T -> boolean` |
| `map` | `Stream<T>` | `Function<T, R>` | `T -> R` |
| `limit` | `Stream<T>` |  |  |
| `sorted` | `Stream<T>` | `Comparator<T>` | `(T, T) -> int` |
| `distinct` | `Stream<T>` |  |  |

<br/>

## **최종 연산**
- 최종 연산은 스트림 파이프라인에서 결과를 도출한다.  
    - `Stream`이 아닌 `List`, `Integer`, `void` 등의 결과를 반환한다.  

<br/>

### **최종 연산 종류**
| 연산 | 반환 타입 | 목적 |
| - | - | - |
| `forEach` | `void` | 스트림의 각 요소를 소비하면서 람다를 적용한다. |
| `count` | `long` <br/> (generic) | 스트림의 요소 개수를 반환한다. |
| `collect` |  | 스트림을 reduce해서 `List`, `Map` 등의 컬렉션을 만든다. <br/> (자세한 설명은 CH6에서 계속) |  

<br/>

## **스트림 연산 과정 요약**
1. 질의를 수행할 (컬렉션 같은) 데이터 소스
2. 스트림 파이프라인을 구성할 중간 연산 연결
3. 스트림 파이프라인을 실행하고 결과를 만들 최종 연산
