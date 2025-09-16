package com.example.myapplication.moneyformatter

import com.example.model.ignoreNull
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter

actual fun Double?.formatMoney(pattern: String): String {
    if (this == null) return ""

    return try {
        val number = NSNumber(double = this)
        val formatter = NSNumberFormatter()

        // Pattern'i ayarlama
        formatter.positiveFormat = pattern

        // Türkiye yerel ayarlarını (locale) ayarlama
        formatter.setLocale(NSLocale("tr_TR"))

        formatter.stringFromNumber(number).ignoreNull()
    } catch (e: Exception) {
        // Hata durumunda null dön
        ""
    }
}