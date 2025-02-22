package com.rosy.ngothing.db.translation

data class TranslationHistory(
    val id: String = "", // Menggunakan String untuk ID agar sesuai dengan Firestore
    val inputText: String,
    val translatedText: String,
    val timestamp: Long,
    val userId: String,
    val sequenceNumber: Int
)
