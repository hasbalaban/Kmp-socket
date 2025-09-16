package com.example.myapplication.moneyformatter

import platform.Foundation.NSDecimalNumber

actual class Decimal actual constructor(value: String) {
    private val nativeDecimal = NSDecimalNumber(string = value)
    actual override fun toString(): String = nativeDecimal.stringValue

    actual companion object {
        actual fun fromString(value: Double): Decimal {
            return Decimal(value.toString())
        }
    }
}