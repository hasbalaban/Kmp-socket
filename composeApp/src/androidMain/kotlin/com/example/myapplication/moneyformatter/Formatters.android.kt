package com.example.myapplication.moneyformatter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

actual fun Double?.formatMoney(pattern: String): String {
    if (this == null) return ""

    return try {
        val df = DecimalFormat(pattern)
        // Türkiye'ye özel sembolleri (virgül, nokta) ayarlayalım
        df.decimalFormatSymbols = DecimalFormatSymbols(Locale("tr", "TR"))
        df.format(this)
    } catch (e: Exception) {
        // Geçersiz bir pattern gelirse null dönmek daha güvenli olabilir
        ""
    }
}