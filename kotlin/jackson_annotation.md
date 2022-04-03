# Jackson annotation
- 참고
    - [Jackson Project Home @github](https://github.com/FasterXML/jackson)
    - [Jackson Core Annotations](https://github.com/FasterXML/jackson-annotations/wiki)

## :large_orange_diamond:  `@JsonIgnoreProperties`
직렬화<sub>serialization</sub>시 제외할 속성을 정의하거나, JSON을 역직렬화<sub>deserialization</sub>할 때 무시할 속성을 정의할 때 사용  

- `value()`
    - 직렬화/역직렬화 시 ignore하고자 하는 속성 이름을 넣음 
- `ignoreUnknown()`
    - default는 `false`
        - 이 경우, 객체에 정의되어 있지 않은 속성이 json object에 있다면 `com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException`이 발생한다. 
    - 역직렬화 중 인식되지 않는 속성(객체에 정의되지 않은 json object)을 무시할 것인지 정의하는 boolean 속성
        - `ignoreUnknown = true`로 정의할 경우, 객체에 정의되지 않은 json object는 무시

```kotlin
class Test {
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    internal fun setUp() {
        objectMapper = ObjectMapper()
    }

    @Test
    fun serializationTest() {
        val member = Member("isoo", "이수", "Seoul", 100)
        val result = objectMapper.writeValueAsString(member)
        assertThat(result).isEqualTo("{\"id\":\"isoo\",\"name\":\"이수\",\"address\":\"Seoul\"}")
        // age 항목은 제외됨을 확인할 수 있다
    }

    @Test
    fun deserializationTest() {
        val jsonString =
            "{\"id\":\"isoo\",\"name\":\"이수\",\"address\":\"Seoul\",\"age\":100,\"specialty\":\"space out\"}"
        val result = objectMapper.readValue(jsonString, Member::class.java)
        assertThat(result).isEqualTo(Member("isoo", "이수", "Seoul", 0))
        // Member에 정의되어 있지 않은 specialty와 
        // 정의는 되어있으나 제외하겠다고 선언된 age는
        // 역직렬화 시 제외됨을 확인할 수 있다. 
        // (그래서 age에는 default 값인 0이 할당되어 있음)
    }
}

// age 속성은 직렬화/역직렬화에서 제외
// json을 Member로 역직렬화할 때, Member에 정의되어 있지 않은 json object는 무시
@JsonIgnoreProperties(value = ["age"], ignoreUnknown = true)
data class Member(
    @get:JsonProperty("id") var id: String = "",
    @get:JsonProperty("name") var name: String = "",
    @get:JsonProperty("address") var address: String = "",
    @get:JsonProperty("age") var age: Int = 0,
)
```
