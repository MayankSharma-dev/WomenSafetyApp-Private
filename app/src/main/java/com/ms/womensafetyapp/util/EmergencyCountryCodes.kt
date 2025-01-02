package com.ms.womensafetyapp.util

import androidx.annotation.Keep

//@Keep
val emergencyCodes = mapOf(
    "911" to listOf("US", "CA", "AS", "AI", "AR", "BS", "BB", "BM", "VG", "KY", "CR", "DO", "SV", "GU", "MH", "MX", "FM", "MS", "MP", "PW", "PA", "PR", "KN", "LC", "VC", "SX", "TT", "TC", "VI"),
    "112" to listOf("EU", "AL", "AD", "AM", /*"AU",*/ "AT", "BA", "BR", "CL", "CO", "HR", "FO", "GE", "GL", "IS", "IN", "ID", "IL", "KZ", "XK", "KG", "LI", "LT", "MK", "MD", "MC", "ME", "NL", "NZ", "NO", "PY", "PE", "PH", "RU", "SM", "RS", "KR", "CH", "TW", "TR", "UA", "GB", "VA"),
    "999" to listOf("BD", "BW", "GH", "HK", "KE", "MY", "MU", "MM", "NP", "NG", "QA", "SA", "SG", "LK", "TZ", "UG", "AE", "ZM", "ZW"),
    "000" to listOf("AU"),
    "111" to listOf("NZ"),
    "110" to listOf("IR", "JP","CN"),
    "119" to listOf("KR"),
    //"100" to listOf("IN"),
    "101" to listOf("IL"),
    "102" to listOf("BY"),
    "104" to listOf("UA"),
    //"108" to listOf("IN"),
    "190" to listOf("BR"),
    //"7017417217" to listOf("SS"),  // Checking Purpose TEMP
    "0000000000" to listOf("SS") // Checking Purpose TEMP
)

//@Keep
fun getCountryEmergencyCode(countryCode: String): String {
    val entry = emergencyCodes.entries.find { entryItems ->
        entryItems.value.any{
            it.lowercase().contentEquals(countryCode.lowercase())
        }
    }
    return entry?.key ?: ""
}