package com.example.myapplication

enum class MbsImage(private val value: Int?) {
    MBS_1(1),
    MBS_2(2),
    MBS_3(3);

    companion object {
        fun valueOf(mbc: Int?): MbsImage? {
            return entries.firstOrNull { mbsImage ->
                mbsImage.value == mbc
            }
        }
    }
}

fun getMbsImage(mbs: Int): MbsImage? {
    return when (mbs) {
        1 -> MbsImage.MBS_1
        2 -> MbsImage.MBS_2
        3 -> MbsImage.MBS_3
        else -> null
    }
}