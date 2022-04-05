# JUnit5
- 참고: [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

## :large_orange_diamond: `@ParameterizedTest`
[`ArgumentsProvider`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.params/org/junit/jupiter/params/provider/ArgumentsProvider.html)와 함께 사용하며, 하나의 테스트 메서드에 여러 파라미터를 전달하여 테스트하고자 할 때 사용된다.  

> #### `ArgumentsProvider`
> - `@ParameterizedTest`가 붙은 메서드에 파라미터 스트림을 전달하는 역할
> - `ArgumentsProvider` 인터페이스를 구현하면 커스텀한 provider를 만들 수 있다
>   - 커스텀 provider를 애너테이션 기반으로 사용하고 싶다면, [`@ArgumentsSource`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.params/org/junit/jupiter/params/provider/ArgumentsSource.html)을 이용해 해당 provider를 등록하면 된다
> - e.g. `@ValueSource`, `@NullSource`, `@CsvSource`, `@EnumSource`, `@MethodSource`, `@EmptySource`

<br/>

## :large_orange_diamond: `@CsvSource`
`value()`속성이나 `textBlock()`속성에 지정된, comma로 구분된 값<sub>comma-separated values; CSV</sub>을 읽어들여 `@ParameterizedTest`가 붙은 메서드의 파라미터로 제공하는 역할

- delimiter
    - defaults delimiter는 comma(`,`)
    - `delimiter()`, `delimiterString()` 속성을 이용해 커스터마이징 가능

### :small_blue_diamond: 빈 값(empty value) 전달하기 
single quotation(`''`)을 이용해 ~<sub>null이 아닌!!</sub>~ empty value를 전달할 수 있다.
```kotlin
class Test {
    fun print(name: String, greeting: String): String {
        return "[$name $greeting]"
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "isoo:hello",
            "isoo:''",
            "'isoo':''",
            "'':''",
        ],
        delimiter = ':'
    )
    fun test(name: String, greeting: String) {
        val result = print(name, greeting)
        println("result:: $result")
        assertThat(result).isEqualTo("[$name $greeting]")
    }
}
```
- 결과
    ```
    result:: [isoo hello]
    result:: [isoo ]
    result:: [isoo ]
    result:: [ ]
    ```

> *테스트 메서드의 인자가 **1개**일 때 empty value를 전달하고 싶다면, [`@EmptySource`](https://junit.org/junit5/docs/5.5.0/api/org/junit/jupiter/params/provider/EmptySource.html)를 사용하면 된다*

<br/>

## :large_orange_diamond: `@EmptySource`
`@ParameterizedTest`가 붙은 메서드의 파라미터가 1개 일 때, 이 파라미터의 값으로 empty value를 제공하는 역할

- 지원하는 타입
    - `String`
    - `java.util.List`
    - `java.util.Set`
    - `java.util.Map`
    - primitive arrays  
        - e.g. `int[]`, `char[][]`, etc.
    - object arrays 
        - e.g. `String[]`, `Integer[][]`, etc.

> :warning: 위 타입들의 subtype은 지원하지 **않음**
> - e.g. `HashSet`, `SortedSet`, `TreeSet`, `HashMap`, `LinkedHashMap` 등은 지원하지 않음. 
>   - 사용 시, `org.junit.platform.commons.PreconditionViolationException .... [xxx] is not a supported type` 예외 발생  

<br/>

### :small_blue_diamond: 사용 예시
```kotlin
@ParameterizedTest
@EmptySource
fun emptyStringTest(emptyString: String) {
    assertThat(emptyString).isEmpty()
}

@ParameterizedTest
@EmptySource
fun emptySetTest(emptySet: Set<String>) {
    assertThat(emptySet).isEmpty()
    assertThat(emptySet.size).isZero
}
```
