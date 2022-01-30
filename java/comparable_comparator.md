# `Comparable`과 `Comparator`
자바에서 오브젝트를 정렬할 때 `Comparable`과 `Comparator`를 구현하여 정렬할 수 있다.
  
- `Comparable`는 **클래스 내에** 정렬 메서드를 구현할 때 사용한다  
- `Comparator`는 **클래스를 호출하는 쪽에서** 정렬 메서드를 사용할 때 사용한다    

각각 코드를 통해 알아보자 :sunglasses:

<br/>

## :small_blue_diamond: `Comparable`
아래 `Student`클래스가 있다.  
```java
public class Student {
    private int id;
    private String name;

    public Student(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
```  
  
<br/>
  
이 `Student`클래스의 인스턴스를 **id를 기준으로 오름차순으로 정렬**하는 일이 많다면,    
아래와 같이 `Comparable`인터페이스를 구현하여 `compareTo()`를 오버라이드 해주면 된다.    
*==> 특정 기준으로 정렬해야 하는 경우가 많다면, 아예 클래스 내부에다 메서드를 구현해놓고 편리하게 사용할 수 있다*  

```java
// Comparable 인터페이스 구현
public class Student implements Comparable {
    ...
    @Override
    public int compareTo(final Student s) {
        return this.id - s.id;
    }
    ...
}
```

<br/>

이제 `Student`클래스를 요소로 가진 리스트를 생성 후, `Collections.sort()`를 이용하여 정렬시켜 보자.   
```java
public static void main(String[] args) {
    final List<Student> list = new ArrayList<>();
    list.add(new Student(3, "banana"));
    list.add(new Student(2, "dragonFruit"));
    list.add(new Student(4, "cherry"));
    list.add(new Student(1, "apple"));

    Collections.sort(list);     
    System.out.println(list);
}
```

<br/>

결과:   
```console
[{id=1, name='apple'}, {id=2, name='dragonFruit'}, {id=3, name='banana'}, {id=4, name='cherry'}]
```

<br/>

## :small_blue_diamond: `Comparator`
위 `Comparable` 예시에서는 `Student`클래스를 정렬할 때 <U>id 오름차순</U>으로 정렬했었다.      
만약 위 정렬 순서가 아니라, 추가적으로 `name`에 대한 오름차순이나 `id` 내림차순, `name` 내림차순 등 다른 방법으로 정렬을 하고자 한다면 `Comparator`를 이용하여 해결할 수 있다.     
```java
public static void main(String[] args) {
    // name을 기준으로 오름차순 정렬하기
    final Comparator comparator = new Comparator<Student>() { // Comparator를 익명 클래스로 구현
        @Override
        public int compare(Student s1, Student s2) {
            return s1.getName().compareTo(s2.getName());
        }
    };

    Collections.sort(list, comparator);  // 파라미터로 정렬하고자 하는 리스트와 Comparator인터페이스를 넘기면 된다
    System.out.println(list);

    System.out.println("==============================================");

    // id를 기준으로 내림차순 정렬하기
    Collections.sort(list, (s1, s2) -> s2.getId() - s1.getId()); // Comparator를 익명 클래스가 아닌 람다로 구현
    System.out.println(list);
}
```

<br/>

결과:   
```java
[{id=1, name='apple'}, {id=3, name='banana'}, {id=4, name='cherry'}, {id=2, name='dragonFruit'}]
==============================================
[{id=4, name='cherry'}, {id=3, name='banana'}, {id=2, name='dragonFruit'}, {id=1, name='apple'}]
```

<br/><br/>

## 정리
클래스를 정렬하고자 할 때,  
- `Arrays.sort(T[] a)`, `Collections.sort(List<T> list)`를 쓰고 싶다면 
    - **클래스가 `Comparable` 인터페이스를 implements 해야** 한다.
- 그렇지 않으면 `Arrays.sort(T[] a, Comparator<? super T> c)`, `Collections.sort(List<T> list, Comparator<? super T> c)`를 쓰되
    - **`Comparator` 인터페이스를 파라미터로 전달**하면 된다.

<br/><br/>

## 참고
- [카이 호스트만의 코어 자바 8](http://www.yes24.co.kr/24/goods/23449538)
