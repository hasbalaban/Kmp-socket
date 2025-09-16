package com.example.myapplication.moneyformatter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object DecimalAsStringSerializer : KSerializer<Decimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Decimal", PrimitiveKind.STRING)

    // Decimal nesnesini JSON'a yazarken (String'e çevir)
    override fun serialize(encoder: Encoder, value: Decimal) {
        encoder.encodeString(value.toString())
    }

    // JSON'dan okurken (String'den Decimal'e çevir)
    override fun deserialize(decoder: Decoder): Decimal {
        // expect/actual ile tanımladığınız Decimal'in String alan kurucusunu kullanıyoruz
        return Decimal(decoder.decodeString())
    }
}

expect class Decimal(value: String) {
    override fun toString(): String

    companion object {
        fun fromString(value: Double): Decimal
    }
}

fun Double?.toKmpBigDecimal(): Decimal? {
    return this?.let {
        try {
            Decimal.fromString(it)
        } catch (e: Exception) {
            // Parse hatası olursa null dönmek güvenlidir
            null
        }
    }
}