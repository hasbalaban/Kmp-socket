package com.example.model

/**
 *   Created by Emre Ergün
 *   19.11.2021 - 12:48
 *
 *   Computer Engineer & Software Developer
 *   All Rights Reserved
 */

enum class SportTypeEnum(val sportName: String,val sportId: Int,val sportNameTurkish:String) {
    SOCCER("SOCCER",1,"FUTBOL"),
    BASKETBALL("BASKETBALL",2,"BASKETBOL"),
    BASEBALL("BASEBALL",3,"BEYZBOL"),
    ICE_HOCKEY("ICE HOCKEY",4,"BUZ HOKEYİ"),
    TENNIS("TENNIS",5,"TENİS"),
    HANDBALL("HANDBALL",6,"HENTBOL"),
    FLOOR_BALL("FLOOR BALL",7,"FLORBOL"),
    GOLF("GOLF",9,"GOLF"),
    MOTO_GP("MOTOGP",11,"MOTOGP"),
    RUGBY("RUGBY",12,"RAGBİ"),
    AUSSIE_RULES("AUSSIE RULES",13,"AVUSTRALYA FUTBOLU"),
    WINTER_SPORTS("WINTER SPORTS",14,"KIŞ SPORLARI"),
    BANDY("BANDY",15,"BANDY"),
    SNOOKER("Snooker",19,"SNOOKER"),
    TABLE_TENNIS("TABLE TENNIS",20,"MASA TENİSİ"),
    DARTS("DARTS",22,"DART"),
    VOLLEYBALL("VOLLEYBALL",23,"VOLEYBOL"),
    FIELD_HOCKEY("FIELD HOCKEY",24,"ÇİM HOKEYİ"),
    WATER_POLO("WATER_POLO",26,"SU_POLO"),
    CURLING("CURLING",28,"CURLING"),
    FUTSAL("FUTSAL",29,"FUTSAL"),
    OLYMPICS("OLYMPICS",30,"OLİMPİYATLAR"),
    BADMINTON("BADMINTON",31,"BADMİNTON"),
    BEACH_VOLLEY("BEACH_VOLLEY",34,"PLAJ VOLEYBOLU"),
    FORMULA_1("FORMULA_1",40,"FORMULA_1"),
    BEACH_SOCCER("BEACH_SOCCER",60,"PLAJ_FUTBOLU"),
    PESAPOLLO("PESAPOLLO",61,"PESAPALLO"),
    CS_GO("Cs Go",109,"Cs Go"),
    LOL("Lol",110,"LOL"),
    DOTA("DOTA",111,"DOTA"),
    STAR_CRAFT("Star Craft",112,"Star Craft"),
    HEARTH_STONE("Heart Stone",113,"Heart Stone"),
    MMA("MMA",117,"MMA"),
    CALL_OF_DUTY("Call Of Duty",118,"Call Of Duty"),
    OVERWATCH("Over Watch",121,"Over Watch"),
    ESOCCER("E-SOCCER", 137, "E-FUTBOL"),
    EBASKETBALL("E-BASKETBALL", 153, "E-BASKETBOL"),
    DUEL("DUEL", 998,"DÜELLO"),
    LIVE("Live", -1,"Canlı"),
    SPECIAL_EVENTS("SpecialEvents", -2,"Özel Etkinlikler");

    companion object {
        val ESPORT_TYPES = listOf(ESOCCER, EBASKETBALL)

        fun isEsportType(value: Int?): Boolean {
            return getFromValue(value ?: -1) in ESPORT_TYPES
        }

        fun getFromValue(value: Int): SportTypeEnum {
            return values()
                .find { it.sportId == value } ?: SOCCER
        }
        // TODO sportsbook confige git
        fun getFromSportNameTurkish(value: String): SportTypeEnum {
            return values()
                .find { it.sportNameTurkish.replace('İ', 'I').replace(" ", "-") == value.uppercase() } ?: SOCCER
        }
    }
}

