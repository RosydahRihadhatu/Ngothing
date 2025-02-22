package com.rosy.ngothing.db.quiz

import com.google.firebase.Timestamp

// Data class untuk menyimpan informasi quiz history
data class QuizHistory(
    var id: String = "", // ID dokumen di Firestore
    val title: String = "",
    val language: String = "",
    val level: String = "",
    val date: Timestamp = Timestamp.now(),
    val score: Int = 0,
    val total: Int = 0,
    val category: String = ""
) {
    // Fungsi untuk mendapatkan tanggal dalam milidetik
    fun getDateInMillis(): Long {
        return date.toDate().time
    }
}
