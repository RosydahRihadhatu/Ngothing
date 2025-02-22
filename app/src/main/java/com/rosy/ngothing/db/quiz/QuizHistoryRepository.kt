package com.rosy.ngothing.db.quiz

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizHistoryRepository(private val userId: String) {
    private val db: FirebaseFirestore = Firebase.firestore

    // Mendapatkan referensi ke koleksi quiz history pengguna
    private fun getQuizHistoryCollection() = db.collection("users")
        .document(userId)
        .collection("quiz_history")

    // Menyimpan dokumen QuizHistory baru ke Firestore
    suspend fun saveQuizHistory(quizHistory: QuizHistory): Boolean {
        return try {
            val documentRef = getQuizHistoryCollection().add(quizHistory).await()
            quizHistory.id = documentRef.id
            Log.d("QuizHistoryRepository", "Quiz history saved with ID: ${documentRef.id}")
            true
        } catch (e: Exception) {
            Log.e("QuizHistoryRepository", "Failed to save quiz history", e)
            false
        }
    }

    // Mengambil daftar QuizHistory dari Firestore, diurutkan berdasarkan tanggal
    suspend fun getQuizHistoryList(): List<QuizHistory> {
        return try {
            val result = getQuizHistoryCollection()
                .orderBy("date")
                .get()
                .await()

            result.map { document ->
                document.toObject(QuizHistory::class.java).apply { id = document.id }
            }.also {
                Log.d("QuizHistoryRepository", "Fetched ${it.size} quiz histories")
            }
        } catch (e: Exception) {
            Log.e("QuizHistoryRepository", "Failed to fetch quiz histories", e)
            emptyList() // Mengembalikan daftar kosong jika gagal
        }
    }

    // Memperbarui dokumen QuizHistory yang sudah ada di Firestore berdasarkan ID
    suspend fun updateQuizHistory(quizHistoryId: String, updatedData: Map<String, Any>, onlyForTingkatanKuis: Boolean = true): Boolean {
        return try {
            // Dapatkan referensi dokumen dari Firestore
            val documentRef = getQuizHistoryCollection().document(quizHistoryId)

            // Cek apakah dokumen tersebut ada
            val documentSnapshot = documentRef.get().await()
            if (documentSnapshot.exists()) {
                // Hanya perbarui jika panggilan berasal dari halaman "Tingkatan Kuis"
                if (onlyForTingkatanKuis) {
                    documentRef.update(updatedData).await()
                    Log.d("QuizHistoryRepository", "Quiz history updated for ID: $quizHistoryId")
                } else {
                    Log.d("QuizHistoryRepository", "Update skipped for Riwayat Kuis for ID: $quizHistoryId")
                }
                true
            } else {
                Log.e("QuizHistoryRepository", "Quiz history not found for ID: $quizHistoryId")
                false
            }
        } catch (e: Exception) {
            Log.e("QuizHistoryRepository", "Failed to update quiz history", e)
            false
        }
    }

    // Menghapus dokumen QuizHistory di Firestore berdasarkan ID
    suspend fun deleteQuizHistory(quizHistoryId: String): Boolean {
        return try {
            getQuizHistoryCollection().document(quizHistoryId).delete().await()
            Log.d("QuizHistoryRepository", "Quiz history deleted for ID: $quizHistoryId")
            true
        } catch (e: Exception) {
            Log.e("QuizHistoryRepository", "Failed to delete quiz history", e)
            false
        }
    }

    // Fungsi utilitas untuk mendapatkan tanggal saat ini dalam format string
    fun getCurrentFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
