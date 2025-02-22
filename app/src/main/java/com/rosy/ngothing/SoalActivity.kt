package com.rosy.ngothing

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rosy.ngothing.db.quiz.SoalJawaban
import com.rosy.ngothing.db.quiz.QuizHistory
import com.rosy.ngothing.db.quiz.QuizHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import kotlinx.coroutines.withContext

class SoalActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var currentQuestionTextView: TextView
    private lateinit var scriptTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var ansA: Button
    private lateinit var ansB: Button
    private lateinit var ansC: Button
    private lateinit var ansD: Button
    private lateinit var submitBtn: ImageButton

    private var score = 0
    private lateinit var questions: List<String>
    private lateinit var choices: List<List<String>>
    private lateinit var correctAnswers: List<String>
    private lateinit var scripts: List<String>
    private var totalQuestion = 0
    private var currentQuestionIndex = 0
    private var selectedAnswer: String? = null

    private lateinit var quizLanguage: String
    private lateinit var quizLevel: String
    private lateinit var quizTopic: String

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var quizHistoryRepository: QuizHistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soal)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        quizHistoryRepository = QuizHistoryRepository(userId)

        currentQuestionTextView = findViewById(R.id.current_question)
        questionTextView = findViewById(R.id.question)
        scriptTextView = findViewById(R.id.script)
        ansA = findViewById(R.id.ans_A)
        ansB = findViewById(R.id.ans_B)
        ansC = findViewById(R.id.ans_C)
        ansD = findViewById(R.id.ans_D)
        submitBtn = findViewById(R.id.submit_btn)

        ansA.setOnClickListener(this)
        ansB.setOnClickListener(this)
        ansC.setOnClickListener(this)
        ansD.setOnClickListener(this)
        submitBtn.setOnClickListener(this)

        findViewById<ImageButton>(R.id.btn_keluar).setOnClickListener {
            showExitDialog()
        }

        // Mengambil bahasa, level, dan topik dari Intent
        quizLanguage = intent.getStringExtra("QUIZ_LANGUAGE") ?: "Kotlin"
        quizLevel = intent.getStringExtra("QUIZ_LEVEL") ?: "Intermediate"
        quizTopic = intent.getStringExtra("QUIZ_TOPIC") ?: "Unknown Topic"

        loadQuestions(quizLanguage, quizLevel, quizTopic)

        if (questions.isEmpty()) {
            Toast.makeText(this, "Tidak ada soal untuk topik ini.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        totalQuestion = questions.size
        updateCurrentQuestion()
        loadNewQuestion()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ans_A, R.id.ans_B, R.id.ans_C, R.id.ans_D -> {
                resetButtonColors()

                val clickedButton = view as Button
                selectedAnswer = clickedButton.text.toString()
                clickedButton.setBackgroundColor(Color.WHITE)
                clickedButton.setTextColor(Color.BLUE)
            }
            R.id.submit_btn -> {
                if (selectedAnswer == null) {
                    showUnansweredDialog()
                } else {
                    if (selectedAnswer == correctAnswers[currentQuestionIndex]) {
                        score++
                    }

                    selectedAnswer = null
                    currentQuestionIndex++
                    if (currentQuestionIndex < totalQuestion) {
                        updateCurrentQuestion()
                        loadNewQuestion()
                        resetButtonColors()
                    } else {
                        finishQuiz()
                    }
                }
            }
        }
    }

    private fun resetButtonColors() {
        ansA.setBackgroundResource(R.drawable.trans_button)
        ansB.setBackgroundResource(R.drawable.trans_button)
        ansC.setBackgroundResource(R.drawable.trans_button)
        ansD.setBackgroundResource(R.drawable.trans_button)

        val purpleDarkColor = ContextCompat.getColor(this, R.color.ungutua)
        ansA.setTextColor(purpleDarkColor)
        ansB.setTextColor(purpleDarkColor)
        ansC.setTextColor(purpleDarkColor)
        ansD.setTextColor(purpleDarkColor)
    }

    private fun loadNewQuestion() {
        // Menampilkan soal baru
        questionTextView.text = questions[currentQuestionIndex]
        ansA.text = choices[currentQuestionIndex][0]
        ansB.text = choices[currentQuestionIndex][1]
        ansC.text = choices[currentQuestionIndex][2]
        ansD.text = choices[currentQuestionIndex][3]

        // Menampilkan script jika ada
        val script = scripts.getOrNull(currentQuestionIndex)
        if (script != null) {
            scriptTextView.visibility = View.VISIBLE
            scriptTextView.text = script
        } else {
            scriptTextView.visibility = View.GONE
        }
    }

    private fun updateCurrentQuestion() {
        currentQuestionTextView.text = "${currentQuestionIndex + 1}/$totalQuestion"
    }

    private fun showUnansweredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Soal Tidak Terjawab")
            .setMessage("Anda belum memilih jawaban. Silakan pilih jawaban sebelum melanjutkan.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showExitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = dialogBuilder.create()
        dialogView.findViewById<Button>(R.id.btn_tidak).setOnClickListener {
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_ya).setOnClickListener {
            alertDialog.dismiss()
            finish()
        }

        alertDialog.show()
    }

    private fun finishQuiz() {
        CoroutineScope(Dispatchers.IO).launch {
            // Cari quizHistory berdasarkan title dan level
            val quizHistoryList = quizHistoryRepository.getQuizHistoryList()
            val quiz = quizHistoryList.find { it.title == quizTopic && it.language == quizLanguage && it.level == quizLevel }

            val success = if (quiz != null) {
                // Jika quiz ditemukan, perbarui menggunakan ID-nya
                quizHistoryRepository.updateQuizHistory(
                    quizHistoryId = quiz.id,
                    updatedData = mapOf("score" to score)
                )
            } else {
                // Jika tidak ditemukan, simpan quiz baru
                quizHistoryRepository.saveQuizHistory(
                    QuizHistory(
                        title = quizTopic,
                        language = quizLanguage,
                        level = quizLevel,
                        date = Timestamp.now(),
                        score = score,
                        total = totalQuestion,
                        category = "$quizLanguage > $quizLevel"
                    )
                )
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    val intent = if (score >= 10) {
                        Intent(this@SoalActivity, HasilJawabanBerhasilActivity::class.java)
                    } else {
                        Intent(this@SoalActivity, HasilJawabanGagalActivity::class.java)
                    }
                    intent.putExtra("SCORE", score)
                    intent.putExtra("TOTAL_QUESTIONS", totalQuestion)
                    setResult(RESULT_OK, intent)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SoalActivity, "Gagal memperbarui hasil kuis.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun loadQuestions(language: String, level: String, title: String) {
        questions = SoalJawaban.getQuestions(language, level, title)
        choices = SoalJawaban.getChoices(language, level, title)
        correctAnswers = SoalJawaban.getCorrectAnswers(language, level, title)
        scripts = SoalJawaban.getScripts(language, level, title)
    }
}
