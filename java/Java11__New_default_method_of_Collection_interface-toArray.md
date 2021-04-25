# Java11에 추가된 기능: New default method of `Collection` interface: `toArray(java.util.function.IntFunction)`
`Collection` 인터페이스에 `toArray(java.util.function.IntFunction)`가 추가되었다.  
이 메서드는 기존에 제공되었던 `<T> T[] toArray(T[] a)`를 오버로딩한 메서드이며 내부는 아래와 같이 생겼다. 
```java
default <T> T[] toArray(IntFunction<T[]> generator) {
    return toArray(generator.apply(0));
}
``` 

## Java 11 전, List를 array로
```java
final List<Integer> integerList = List.of(1, 2, 3);
final int[] integerArray = integerList.stream()
        .mapToInt(Integer::intValue)    // 또는 (i -> i) 
        .toArray();

final List<String> stringList = List.of("a", "b", "c");
final String[] stringArray = stringList.toArray(new String[0]); 
```

> `List.of`는 Java9에 추가된 메서드. unmodifiable list를 반환함.  

<br/>

## Java 11 이후, List를 array로
```java
final var stringList = List.of("a", "b", "c");
final String[] stringArray = stringList.toArray(String[]::new);
```

<br/>

# 참고
- [ORACLE: Java Bug Database: JDK-8060192 : Add default method A[] Collection.toArray(IntFunction generator)](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8060192)