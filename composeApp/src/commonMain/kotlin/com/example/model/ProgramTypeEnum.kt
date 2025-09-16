package com.example.model

enum class ProgramTypeEnum(val value: Int){
    PreEvents(0),
    Live(1),
    LongTerm(2);

    companion object {
        fun getFromValue(value: Int): ProgramTypeEnum {
            return values()
                .find { it.value == value } ?: ProgramTypeEnum.PreEvents
        }
    }
}