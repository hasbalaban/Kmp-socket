package com.example.myapplication

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.byUnicodePattern

val dateFormat = LocalDate.Format {
    byUnicodePattern("yyyy-MM-dd")
}