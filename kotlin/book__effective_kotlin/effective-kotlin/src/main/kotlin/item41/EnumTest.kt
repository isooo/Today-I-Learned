package item41

import java.math.BigDecimal

enum class PaymentOption(val commission: BigDecimal) {
    CASH(BigDecimal.ONE) {
        override fun printAccount(description: String) {/* ... */
        }
    },
    CARD(BigDecimal.TEN) {
        override fun printAccount(description: String) {/* ... */
        }
    },
    TRANSFER(BigDecimal.ZERO) {
        override fun printAccount(description: String) {/* ... */
        }
    };

    abstract fun printAccount(description: String)
}

enum class PaymentOption2(val commission: BigDecimal, val printAccount: (String) -> Unit) {
    CASH(BigDecimal.ONE, ::println),
    CARD(BigDecimal.TEN, ::println),
    TRANSFER(BigDecimal.ZERO, ::println)
}

enum class PaymentOption3(val commission: BigDecimal) {
    CASH(BigDecimal.ONE),
    CARD(BigDecimal.TEN),
    TRANSFER(BigDecimal.ZERO)
}

fun PaymentOption3.printAccount(description: String) {
    when (this) {
        PaymentOption3.CASH -> println(description)
        PaymentOption3.CARD -> println(description)
        PaymentOption3.TRANSFER -> println(description)
    }
}
