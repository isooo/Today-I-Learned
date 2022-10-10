package item2

import item2.Color.*

enum class Color {
    BLUE, YELLOW, RED
}

// 나쁜 예
fun updateWeather1(degrees: Int) {
    val description: String
    val color: Color
    if (degrees < 5) {
        description = "cold"
        color = BLUE
    } else if (degrees < 23) {
        description = "mild"
        color = YELLOW
    } else {
        description = "hot"
        color = RED
    }
}

// 좋은 예
fun updateWeather2(degrees: Int) {
    val (description, color) = when {
        degrees < 5 -> "cold" to BLUE
        degrees < 23 -> "mild" to YELLOW
        else -> "hot" to RED
    }
}