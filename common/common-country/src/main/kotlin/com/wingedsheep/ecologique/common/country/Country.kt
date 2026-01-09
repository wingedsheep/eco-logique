package com.wingedsheep.ecologique.common.country

/**
 * ISO 3166-1 alpha-2 country codes.
 */
enum class Country(val displayName: String) {
    NL("Netherlands"),
    DE("Germany"),
    BE("Belgium"),
    FR("France");

    companion object {
        fun fromCode(code: String): Country? =
            entries.find { it.name.equals(code, ignoreCase = true) }
    }
}
