# 얕은 복사(Shallow Copy)와 깊은 복사(Deep Copy)

> :warning: *이 개념은 java에만 존재하는 것은 아니며, 설명을 java로 했기 때문에 java 디렉터리 하위로 넣어둠*  

<br/>

## 0. 준비

<details>
<summary><italic> 예시 코드</italic></summary>
<div markdown="1">

```java
public class Fruit {
    private String name;
    private int count;

    public Fruit(final String name, final int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Fruit{" +
                "name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}
```

```java
public class CopyTest {
    static List<Fruit> origin = new ArrayList<>();
    static List<Fruit> copy = new ArrayList<>();

    private static void shallowCopy() {
        copy = origin;
        copy.add(new Fruit("cherry", 3));
        final Fruit f = copy.get(1);
        f.setCount(100);
    }

    private static void fakeDeepCopy() {
        copy = new ArrayList<>(origin);
        copy.add(new Fruit("cherry", 3));
        final Fruit f = copy.get(1);
        f.setCount(100);
    }

    public static void printArray() {
        System.out.println("============= origin ==============");
        for(int i = 0 ; i < origin.size() ; i++) {
            System.out.println("origin [" + i + "] : " + origin.get(i));
        }

        System.out.println("\n============= copy ==============");
        for(int i = 0 ; i < copy.size() ; i++) {
            System.out.println("copy [" + i + "] : " + copy.get(i));
        }
    }

    public static void main(String[] args) {
        origin.add(new Fruit("apple", 1));
        origin.add(new Fruit("banana", 2));

        // operation
    }
}
```

---

</div>
</details>

<br/>

요소 2개를 동일하게 갖고 있는 array `origin`과 `copy`를 `shallowCopy()`, `fakeDeepCopy()`했을 때 각각 요소가 어떻게 변하는지 확인해 보자.   

<br/>

## 1. 얕은 복사

```java
shallowCopy();
printArray();
```

결과 :
```
============= origin ==============
origin [0] : Fruit{name='apple', count=1}
origin [1] : Fruit{name='banana', count=100}
origin [2] : Fruit{name='cherry', count=3}

============= copy ==============
copy [0] : Fruit{name='apple', count=1}
copy [1] : Fruit{name='banana', count=100}
copy [2] : Fruit{name='cherry', count=3}
```  

`origin`과 `copy` 둘 다 첫 번째 요소의 `count`가 `100`으로 바뀌었고, 두 번째 요소에 `cherry, 3`이 추가된 것을 확인할 수 있다.      
  
- 요소 추가/변경 모두 영향을 끼침  
  
<br/>

## 2. 깊은 복사 <sub>*가 사실 아님! 진정한 깊은 복사는 3번에서 확인!*</sub>

```java
fakeDeepCopy();
printArray();
```  

결과 :
```
============= origin ==============
origin [0] : Fruit{name='apple', count=1}
origin [1] : Fruit{name='banana', count=100}

============= copy ==============
copy [0] : Fruit{name='apple', count=1}
copy [1] : Fruit{name='banana', count=100}
copy [2] : Fruit{name='cherry', count=3}
```
1번 얕은 복사와 달라진 점은 `copy`리스트에만 두 번째 요소가 추가된 것이다.  
  
하지만 이번 복사에서 추가로 기대했던 부분은    
`fakeDeepCopy()`에서 `copy`리스트의 첫 번째 인덱스의 `count`를 `100`으로 변경했을 때(`f.setCount(100)`),  
`origin`리스트의 첫 번째 인덱스의 `count`는 영향을 받지 않고, `copy`의 첫 번째 인덱스만 영향을 받는 것이었다.  
하지만 위 결과에서는 `origin`의 값도 (2 -> 100)으로 변경되어 버림 ==> 이것은 진정한 깊은 복사라 할 수 없다.  
    
- 요소 추가는 영향을 끼치지 않음
- 요소 변경은 영향을 끼침
  
<br/>

## 3. **진정한** 깊은 복사  
먼저 `Fruit`클래스에서 `clone()`을 오버라이드 해주었다.  
```java
public class Fruit {
...
    @Override
    public Fruit clone() {
        return new Fruit(this.name, this.count);
    }
...
}
```  

이제 `deepCopy()` 메서드를 추가해줬다.  
```java
public class CopyTest {
    ...
    private static void deepCopy() {
        copy = origin.stream()
                .map(Fruit::clone)
                .collect(Collectors.toList());

        // 아래는 기존 메서드(shallowCopy(), fakeDeepCopy())들과 동일
        copy.add(new Fruit("cherry", 3));
        final Fruit f = copy.get(1);
        f.setCount(100);
    }
    ...
}
```

메서드 실행!  
```
deepCopy();
printArray();
```  

결과 :
```
============= origin ==============
origin [0] : Fruit{name='apple', count=1}
origin [1] : Fruit{name='banana', count=2}

============= deepCopy ==============
deepCopy [0] : Fruit{name='apple', count=1}
deepCopy [1] : Fruit{name='banana', count=100}
deepCopy [2] : Fruit{name='cherry', count=3}
```  
이제 `deepCopy`에서 첫 번째 인덱스에 대한 `count`를 변경하여도  
`origin`리스트는 영향을 받지 않는다!     
  
- 요소 추가/변경 모두 영향을 끼치지 않음  
  
<br/>

## 참고
- [Oh! My Library: ArrayList 깊은 복사(deep copy) vs 얕은 복사(shallow copy)](http://library1008.tistory.com/47)
