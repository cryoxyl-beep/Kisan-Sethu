package com.kisansethu.app.data

enum class Language(
    val code: String,
    val nativeName: String
) {
    ENGLISH("en", "English"),
    TELUGU("te", "తెలుగు"),
    TAMIL("ta", "தமிழ்"),
    MALAYALAM("ml", "മലയാളം"),
    HINDI("hi", "हिंदी"),
    MARATHI("mr", "मराठी"),
    BENGALI("bn", "বাংলা"),
    KANNADA("kn", "ಕನ್ನಡ");

    companion object {
        fun fromCode(code: String?): Language? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}
