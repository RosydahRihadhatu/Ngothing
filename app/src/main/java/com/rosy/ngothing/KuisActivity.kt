package com.rosy.ngothing

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.db.quiz.Quiz
import com.rosy.ngothing.db.quiz.QuizAdapter
import com.rosy.ngothing.db.quiz.QuizHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KuisActivity : AppCompatActivity(), QuizAdapter.OnItemClickListener {

    private lateinit var quizAdapter: QuizAdapter
    private val quizList = listOf(
        Quiz("Python", 13, 0, 13),
        Quiz("Kotlin", 10, 0, 10),
        Quiz("Dart", 7, 0, 7),
        Quiz("Java", 11, 0, 11),
        Quiz("HTML", 9, 0, 9),
        Quiz("CSS", 9, 0, 9),
        Quiz("JavaScript", 9, 0, 9)
    )

    private lateinit var quizHistoryRepository: QuizHistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuis)

        // Mengambil ID pengguna dari FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user_id"
        quizHistoryRepository = QuizHistoryRepository(userId)

        val backButton = findViewById<ImageButton>(R.id.btn_kembali)
        backButton.setOnClickListener { finish() }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.kuisrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        quizAdapter = QuizAdapter(quizList, this)
        recyclerView.adapter = quizAdapter

        loadQuizData()

        // Setup tombol navigasi
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        findViewById<ImageButton>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btn_profil).setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btn_riwayat).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
    }

    private fun loadQuizData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val quizHistories = quizHistoryRepository.getQuizHistoryList()

                // Menghitung topik yang sudah diselesaikan tanpa mengubah data di Firestore
                val updatedQuizList = quizList.map { quiz ->
                    val completedTopics = quizHistories.count {
                        it.language == quiz.title && it.score >= 10
                    }
                    quiz.copy(progress = completedTopics)
                }

                withContext(Dispatchers.Main) {
                    quizAdapter.updateData(updatedQuizList)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    override fun onItemClick(quiz: Quiz) {
        val intent = Intent(this, TingkatanKuisActivity::class.java).apply {
            putExtra("QUIZ_TITLE", quiz.title)
        }
        startActivity(intent)
    }
}
