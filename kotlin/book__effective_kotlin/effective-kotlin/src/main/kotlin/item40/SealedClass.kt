package item40

sealed class ValueMatcher2<T> {
    abstract fun match(value: T): Boolean

    class Equal<T>(val value: T) : ValueMatcher2<T>() {
        override fun match(value: T): Boolean =
            value == this.value
    }

    class NotEqual<T>(val value: T) : ValueMatcher2<T>() {
        override fun match(value: T): Boolean =
            value != this.value
    }

    class EmptyList<T>() : ValueMatcher2<T>() {
        override fun match(value: T) =
            value is List<*> && value.isEmpty()
    }

    class NotEmptyList<T>() : ValueMatcher2<T>() {
        override fun match(value: T) =
            value is List<*> && value.isNotEmpty()
    }
}

// sealed class를 써서, 아래와 같이 when을 활용한 extension function을 만들 수도 있음
fun <T> ValueMatcher2<T>.reversed(): ValueMatcher2<T> =
    when (this) {
        is ValueMatcher2.EmptyList -> ValueMatcher2.NotEmptyList<T>()
        is ValueMatcher2.NotEmptyList -> ValueMatcher2.EmptyList<T>()
        is ValueMatcher2.Equal -> ValueMatcher2.NotEqual(value)
        is ValueMatcher2.NotEqual -> ValueMatcher2.Equal(value)
    }
