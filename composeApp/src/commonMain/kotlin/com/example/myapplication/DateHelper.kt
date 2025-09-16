package com.example.myapplication

import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlinx.datetime.format.char
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Türkiye için sabit bir TimeZone tanımlayalım
private val istanbulTimeZone = TimeZone.of("Europe/Istanbul")
private val utcTimeZone = TimeZone.UTC

object DateHelper {
    @OptIn(ExperimentalTime::class)
    fun getStartOfDay(timestamp: Long): LocalDate {
        val instant =  kotlin.time.Instant.fromEpochSeconds(timestamp)
        // Kullanıcının saat dilimine göre tarihi alır.
        // Eğer her zaman UTC veya İstanbul gibi sabit bir time zone isterseniz
        // TimeZone.UTC veya TimeZone.of("Europe/Istanbul") kullanabilirsiniz.
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    /**
     * Mevcut tarihten belirtilen gün kadar öncesini hesaplar.
     */
    @OptIn(ExperimentalTime::class)
    fun getCalculatedDate(dateFormat: String = "yyyy-MM-dd", days: Int): String {
        val today = kotlin.time.Clock.System.now().toLocalDateTime(istanbulTimeZone).date
        val calculatedDate = today.minus(days, DateTimeUnit.DAY)
        val format = LocalDate.Format { byUnicodePattern(dateFormat) }
        return calculatedDate.format(format)
    }

    /**
     * Unix timestamp'i "Bugün", "Yarın", "Pazartesi" gibi göreceli bir formata çevirir.
     */
    @OptIn(ExperimentalTime::class)
    fun timestampToTimeBulletinScreen(timestamp: Long, formatPattern: String = "dd-MM-yyyy HH:mm"): String {
        val instant = kotlin.time.Instant.fromEpochSeconds(timestamp)
        val targetDateTime = instant.toLocalDateTime(istanbulTimeZone)
        val now = kotlin.time.Clock.System.now().toLocalDateTime(istanbulTimeZone)

        val targetDate = targetDateTime.date
        val today = now.date
        val tomorrow = today.plus(1, DateTimeUnit.DAY)

        val timeFormat = LocalTime.Format { byUnicodePattern("HH:mm") }
        val fullFormat = LocalDateTime.Format { byUnicodePattern(formatPattern) }

        return when (targetDate) {
            today -> "Bugün ${targetDateTime.time.format(timeFormat)}"
            tomorrow -> "Yarın ${targetDateTime.time.format(timeFormat)}"
            else -> {
                // Bugün ile hedef gün arasındaki farkı hesapla
                val daysUntil = today.daysUntil(targetDate)
                if (daysUntil in 2..6) {
                    // Türkçe gün isimleri için bir yardımcı fonksiyon
                    "${getDayName(targetDate.dayOfWeek)} ${targetDateTime.time.format(timeFormat)}"
                } else {
                    targetDateTime.format(fullFormat)
                }
            }
        }
    }

    // `timestampToDayOrDate` fonksiyonu yukarıdakinin çok benzeri, onu basitleştirebiliriz.
    @OptIn(ExperimentalTime::class)
    fun timestampToDayOrDate(timestamp: Long, formatPattern: String = "dd-MM-yyyy"): String {
        // Yukarıdaki mantığı kullanarak basitleştirilebilir, ama isteğinize göre aynısını yeniden yazıyorum
        val instant = kotlin.time.Instant.fromEpochSeconds(timestamp)
        val targetDate = instant.toLocalDateTime(istanbulTimeZone).date
        val today = Clock.System.now().toLocalDateTime(istanbulTimeZone).date
        val tomorrow = today.plus(1, DateTimeUnit.DAY)

        return when (targetDate) {
            today -> "Bugün"
            tomorrow -> "Yarın"
            else -> {
                val daysUntil = today.daysUntil(targetDate)
                if (daysUntil in 2..6) {
                    getDayName(targetDate.dayOfWeek)
                } else {
                    val format = LocalDate.Format { byUnicodePattern(formatPattern) }
                    targetDate.format(format)
                }
            }
        }
    }

    /**
     * Unix timestamp'i sadece saat formatına çevirir (HH:mm).
     */
    @OptIn(ExperimentalTime::class)
    fun timestampToHour(timestamp: Long): String {
        val instant = kotlin.time.Instant.fromEpochSeconds(timestamp)
        val time = instant.toLocalDateTime(istanbulTimeZone).time
        val format = LocalTime.Format { byUnicodePattern("HH:mm") }
        return time.format(format)
    }

    /**
     * Türkçe gün ismi döndürür.
     */
    private fun getDayName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Pazartesi"
            DayOfWeek.TUESDAY -> "Salı"
            DayOfWeek.WEDNESDAY -> "Çarşamba"
            DayOfWeek.THURSDAY -> "Perşembe"
            DayOfWeek.FRIDAY -> "Cuma"
            DayOfWeek.SATURDAY -> "Cumartesi"
            DayOfWeek.SUNDAY -> "Pazar"
        }
    }

    /**
     * İki tarihin aynı günde olup olmadığını kontrol eder.
     */
    @OptIn(ExperimentalTime::class)
    fun areDatesOnSameDay(instant1: kotlin.time.Instant, instant2: kotlin.time.Instant): Boolean {
        val date1 = instant1.toLocalDateTime(istanbulTimeZone).date
        val date2 = instant2.toLocalDateTime(istanbulTimeZone).date
        return date1 == date2
    }

    /**
     * Verilen string'i ISO 8601 formatından "dd.MM.yyyy - HH:mm" formatına çevirir.
     */
    @OptIn(ExperimentalTime::class)
    fun formatDateTime(inputDate: String?): String {
        if (inputDate.isNullOrBlank()) return ""
        return try {
            val instant = kotlin.time.Instant.parse(inputDate)
            val dateTime = instant.toLocalDateTime(istanbulTimeZone)
            val format = LocalDateTime.Format { byUnicodePattern("dd.MM.yyyy - HH:mm") }
            dateTime.format(format)
        } catch (e: Exception) {
            // Hatalı format gelirse boş dönsün veya hata yönetimi yapılsın
            ""
        }
    }

    /**
     * Milisaniyeyi "dd/MM/yyyy" formatına çevirir.
     */
    @OptIn(ExperimentalTime::class)
    fun formatDate(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val instant = kotlin.time.Instant.fromEpochMilliseconds(milliseconds)
        val date = instant.toLocalDateTime(istanbulTimeZone).date
        val format = LocalDate.Format { byUnicodePattern(DAYMONTHYEARFORMAT) }
        return date.format(format)
    }

    /**
     * Milisaniyeyi "yyyy-MM-dd" (ISO 8601) formatına çevirir.
     */
    @OptIn(ExperimentalTime::class)
    fun convertMillisToIso8601Date(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val instant = kotlin.time.Instant.fromEpochMilliseconds(milliseconds)
        val date = instant.toLocalDateTime(istanbulTimeZone).date
        return date.toString() // LocalDate'in varsayılan toString() metodu zaten "yyyy-MM-dd" formatındadır.
    }

    const val DAYMONTHYEARFORMAT = "dd/MM/yyyy"
    const val YEARMONTHDAYFORMAT = "yyyy-MM-dd"
}