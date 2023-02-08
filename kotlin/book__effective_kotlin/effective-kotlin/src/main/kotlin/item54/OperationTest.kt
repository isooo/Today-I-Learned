package item54

class Student(val name: String?)

// Works
fun List<Student>.getNames1(): List<String> = this.map { it.name }
    .filter { it != null }
    .map { it!! }

// Better
fun List<Student>.getNames2(): List<String> = this.map { it.name }
    .filterNotNull()

// Best
fun List<Student>.getNames3(): List<String> = this.mapNotNull { it.name }
