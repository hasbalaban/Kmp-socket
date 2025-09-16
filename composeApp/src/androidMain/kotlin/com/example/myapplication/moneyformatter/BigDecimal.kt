package com.example.myapplication.moneyformatter

import java.math.BigDecimal as JavaBigDecimal

actual class Decimal actual constructor(value: String) {
    private val nativeDecimal = JavaBigDecimal(value)
    actual override fun toString(): String = nativeDecimal.toPlainString()

    actual companion object {
        actual fun fromString(value: Double): Decimal {
            return Decimal(value.toString()) // Sadece constructor'ı çağırır
        }
    }
}