package com.rosy.ngothing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.db.quiz.ChildItem
import com.rosy.ngothing.db.quiz.HeaderItem
import com.rosy.ngothing.db.MyAdapter
import com.rosy.ngothing.db.quiz.QuizHistory
import com.rosy.ngothing.db.quiz.QuizHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp

class TingkatanKuisActivity : AppCompatActivity() {

    private lateinit var headers: List<HeaderItem>
    private lateinit var quizTitle: String
    private var quizHistory: List<QuizHistory> = listOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var quizHistoryRepository: QuizHistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tingkatan_kuis)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        quizHistoryRepository = QuizHistoryRepository(userId)

        val backButton = findViewById<ImageButton>(R.id.btn_kembali)
        backButton.setOnClickListener { finish() }

        quizTitle = intent.getStringExtra("QUIZ_TITLE") ?: "Unknown Quiz"
        findViewById<TextView>(R.id.tvTitle).text = quizTitle

        headers = loadHeaders(quizTitle)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(headers, quizTitle, quizHistory) { childItem ->
            if (canAttemptChildItem(childItem)) {
                val intent = Intent(this, SoalActivity::class.java).apply {
                    putExtra("QUIZ_LANGUAGE", quizTitle)
                    putExtra("QUIZ_LEVEL", childItem.level)
                    putExtra("QUIZ_TOPIC", childItem.title)
                }
                startActivityForResult(intent, 1)
            } else {
                Toast.makeText(this, "Kerjakan Soal Sebelumnya!", Toast.LENGTH_LONG).show()
            }
        }

        loadPassedQuizzes(quizTitle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Panggil fungsi loadPassedQuizzes untuk memuat ulang data quizHistory
            loadPassedQuizzes(quizTitle)
        }
    }


    private fun loadHeaders(quizTitle: String): List<HeaderItem> {
        return when (quizTitle) {
            "Python" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Tipe Data dan Variabel", "basic"),
                        ChildItem("Tipe Data List", "basic"),
                        ChildItem("Tipe Data Tupple", "basic"),
                        ChildItem("Input dan Output", "basic"),
                        ChildItem("Percabangan", "basic"),
                        ChildItem("Perulangan", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Fungsi", "intermediate"),
                        ChildItem("Recursive Function", "intermediate"),
                        ChildItem("Lambda Function", "intermediate"),
                        ChildItem("Recursive Function", "intermediate")
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Object Oriented Programming:", "advanced"),
                        ChildItem("Generator", "advanced"),
                        ChildItem("Modul Library", "advanced")
                    )
                )
            )

            "Kotlin" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Activity", "basic"),
                        ChildItem("Intent", "basic"),
                        ChildItem("View dan ViewGroup", "basic"),
                        ChildItem("Style dan Theme", "basic"),
                        ChildItem("Recycler View", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Android Widget", "intermediate"),
                        ChildItem("Motion Layout", "intermediate"),
                        ChildItem("Adaptive Layout", "intermediate"),
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Database", "advanced"),
                        ChildItem("Firebase dan Reflection", "advanced")
                    )
                )
            )

            "Dart" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Fungsi Input dan Output", "basic"),
                        ChildItem("Operator", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Control Flow (Percangan)", "intermediate"),
                        ChildItem("Control Flow (Perulangan)", "intermediate"),
                        ChildItem("Struktur data List", "intermediate")
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Generics", "advanced"),
                        ChildItem("Advanced Asynchronous Programming", "advanced")
                    )
                )
            )

            "Java" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Variabel dan Tipe Data", "basic"),
                        ChildItem("Operator", "basic"),
                        ChildItem("Percabangan", "basic"),
                        ChildItem("Perulangan", "basic"),
                        ChildItem("Struktur Data Array", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Datastructures", "intermediate"),
                        ChildItem("OOP, Interfaces, Classes", "intermediate"),
                        ChildItem("Package", "intermediate")
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Memory Management", "advanced"),
                        ChildItem("Collection Framework", "advanced"),
                        ChildItem("Serialization", "advanced")
                    )
                )
            )

            "HTML" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Elemen dan Tag", "basic"),
                        ChildItem("Atribut", "basic"),
                        ChildItem("Link dan Gambar", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Audio dan Video", "intermediate"),
                        ChildItem("Layout", "intermediate"),
                        ChildItem("Tabel", "intermediate")
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Canvas dan SVG", "advanced"),
                        ChildItem("Collecting Information", "advanced"),
                        ChildItem("Microdata dan Aksesibilitas", "advanced")
                    )
                )
            )

            "CSS" -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Selektor CSS", "basic"),
                        ChildItem("Margin dan Padding", "basic"),
                        ChildItem("Position CSS", "basic"),
                        ChildItem("Border CSS", "basic"),
                        ChildItem("Properti Font dan Warna", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Grid Layout", "intermediate"),
                        ChildItem("Flexbox", "intermediate")
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("CSS Frameworks", "advanced"),
                        ChildItem("Performance Optimization", "advanced")
                    )
                )
            )

            else -> listOf(
                HeaderItem(
                    "Basic", listOf(
                        ChildItem("Dasar-dasar JavaScript", "basic"),
                        ChildItem("Tipe Operator", "basic"),
                        ChildItem("Deklarasi Variabel", "basic"),
                        ChildItem("JSON", "basic")
                    )
                ),
                HeaderItem(
                    "Intermediate", listOf(
                        ChildItem("Argumen Objek", "intermediate"),
                        ChildItem("Rekursi", "intermediate"),
                        ChildItem("Asynchronous JavaScript", "intermediate"),
                    )
                ),
                HeaderItem(
                    "Advanced", listOf(
                        ChildItem("Memori manajemen", "advanced"),
                        ChildItem("Debugging", "advanced")
                    )
                )
            )
        }
    }

    private fun canAttemptChildItem(childItem: ChildItem): Boolean {
        val flatChildItems = headers.flatMap { it.childItems }
        val currentIndex = flatChildItems.indexOf(childItem)

        val currentQuiz = quizHistory.find { it.title == childItem.title && it.level == childItem.level }
        if (currentQuiz != null) return true
        if (currentIndex == 0) return true

        val previousItem = flatChildItems.getOrNull(currentIndex - 1)
        val previousQuiz = quizHistory.find { it.title == previousItem?.title && it.level == previousItem?.level }
        return previousQuiz?.score ?: 0 >= 10
    }

    private suspend fun updateRecyclerViewWithData() {
        // Memetakan header untuk menyertakan data terbaru dari quizHistory
        val updatedHeaders = headers.map { header ->
            val updatedChildItems = header.childItems.map { child ->
                val quiz = quizHistory.find { it.title == child.title && it.level == child.level }

                // Periksa apakah ada data kuis yang cocok, dan salin nilai yang diperlukan
                if (quiz != null) {
                    child.copy(isDone = true, score = quiz.score, totalScore = quiz.total)
                } else {
                    child
                }
            }
            header.copy(childItems = updatedChildItems)
        }

        // Pindah ke Main Thread untuk memperbarui UI
        withContext(Dispatchers.Main) {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

            // Buat adapter baru dengan data yang diperbarui
            val adapter = MyAdapter(updatedHeaders, quizTitle, quizHistory) { childItem ->
                if (canAttemptChildItem(childItem)) {
                    val intent = Intent(this@TingkatanKuisActivity, SoalActivity::class.java).apply {
                        putExtra("QUIZ_LANGUAGE", quizTitle)
                        putExtra("QUIZ_LEVEL", childItem.level)
                        putExtra("QUIZ_TOPIC", childItem.title)
                    }
                    startActivityForResult(intent, 1)
                } else {
                    Toast.makeText(this@TingkatanKuisActivity, "Kerjakan Soal Sebelumnya!", Toast.LENGTH_LONG).show()
                }
            }

            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged() // Memastikan adapter diperbarui
        }
    }

    private fun loadPassedQuizzes(language: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                quizHistory = quizHistoryRepository.getQuizHistoryList()
                withContext(Dispatchers.Main) {
                    updateRecyclerViewWithData() // Memperbarui UI dengan data terbaru
                }
            } catch (exception: Exception) {
                Log.e("TingkatanKuisActivity", "Gagal mengambil riwayat kuis", exception)
            }
        }
    }

    private fun updateQuizHistory(completedItem: String, score: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val quiz = quizHistory.find { it.title == completedItem }
            val quizId = quiz?.id

            if (quizId != null) {
                val success = quizHistoryRepository.updateQuizHistory(
                    quizHistoryId = quizId,
                    updatedData = mapOf("score" to score)
                )

                withContext(Dispatchers.Main) {
                    if (success) {
                        Log.d("UpdateQuizHistory", "Score updated for: $completedItem with score: $score")
                        loadPassedQuizzes(quizTitle) // Reload to update UI
                    } else {
                        Log.e("UpdateQuizHistory", "Failed to update quiz history")
                    }
                }
            } else {
                saveNewQuizHistory(completedItem, score)
            }
        }
    }

    private suspend fun saveNewQuizHistory(completedItem: String, score: Int) {
        val newQuiz = QuizHistory(
            title = completedItem,
            language = quizTitle,
            level = "Unknown Level",
            date = Timestamp.now(),
            score = score,
            total = 15,  // Sesuaikan total skor sesuai kebutuhan
            category = "$quizTitle > Unknown Level"
        )

        val success = quizHistoryRepository.saveQuizHistory(newQuiz)

        withContext(Dispatchers.Main) {
            if (success) {
                Log.d("UpdateQuizHistory", "New quiz history added for: $completedItem with score: $score")
                loadPassedQuizzes(quizTitle) // Reload to update UI
            } else {
                Log.e("UpdateQuizHistory", "Failed to save new quiz history")
            }
        }
    }
}
