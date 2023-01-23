package item35

// builder를 반환
fun table(init: TableBuilder.() -> Unit): TableBuilder {
    val tableBuilder = TableBuilder()
    init.invoke(tableBuilder)
    return tableBuilder
}

// 위 table 함수를, apply사용해서 더욱 짧게 만들 수 있다
fun table2(init: TableBuilder.() -> Unit) = TableBuilder().apply(init)

class TableBuilder {
    fun tr(init: TrBuilder.() -> Unit) { /* ... */
    }
}

class TrBuilder {
    fun td(init: TdBuilder.() -> Unit) { /* ... */
    }
}

class TdBuilder {
    var text = ""

    operator fun String.unaryPlus() {
        text += this
    }
}

fun createTable() = table {
    tr {
        for (i in 1..2) {
            td {
                +"This is solumn $1"
            }
        }
    }
}
