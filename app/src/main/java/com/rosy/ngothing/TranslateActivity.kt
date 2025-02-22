package com.rosy.ngothing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rosy.ngothing.api.ApiClient
import com.rosy.ngothing.api.GPTChatRequest
import com.rosy.ngothing.api.GPTChatResponse
import com.rosy.ngothing.api.Message
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslateActivity : AppCompatActivity() {

    private lateinit var textInputEditTextSource: TextInputEditText
    private lateinit var textInputEditTextTarget: TextInputEditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        textInputEditTextSource = findViewById(R.id.textInputEditTextSource)
        textInputEditTextTarget = findViewById(R.id.textInputEditTextTarget)

        val btnSalin: ImageButton = findViewById(R.id.btn_salin)
        btnSalin.setOnClickListener {
            copyToClipboard("Source Text", textInputEditTextSource.text.toString())
        }

        val btnSalin2: ImageButton = findViewById(R.id.btn_salin2)
        btnSalin2.setOnClickListener {
            copyToClipboard("Translated Text", textInputEditTextTarget.text.toString())
        }

        // Setup navigation buttons
        setupNavigationButtons()

        val codeInput = intent.getStringExtra("CODE_INPUT")
        val sourceLanguage = intent.getStringExtra("SOURCE_LANGUAGE_NAME") ?: "Unknown Language"
        val targetLanguage = intent.getStringExtra("TARGET_LANGUAGE_NAME") ?: "Unknown Language"
        val sourceLanguageIcon = intent.getIntExtra("SOURCE_LANGUAGE_ICON", R.drawable.indonesia)
        val targetLanguageIcon = intent.getIntExtra("TARGET_LANGUAGE_ICON", R.drawable.python)

        textInputEditTextSource.setText(codeInput)

        val iconSourceLanguage = findViewById<ImageView>(R.id.icon_source_language)
        val textSourceLanguage = findViewById<TextView>(R.id.text_source_language)
        val iconTargetLanguage = findViewById<ImageView>(R.id.icon_target_language)
        val textTargetLanguage = findViewById<TextView>(R.id.text_target_language)

        iconSourceLanguage.setImageResource(sourceLanguageIcon)
        textSourceLanguage.text = sourceLanguage
        iconTargetLanguage.setImageResource(targetLanguageIcon)
        textTargetLanguage.text = targetLanguage

        if (isProgrammingLanguage(sourceLanguage) && !isProgrammingLanguage(targetLanguage)) {
            translateCodeToExplanation(codeInput ?: "", sourceLanguage, targetLanguage)
        } else if (!isProgrammingLanguage(sourceLanguage) && isProgrammingLanguage(targetLanguage)) {
            translateExplanationToCode(codeInput ?: "", sourceLanguage, targetLanguage)
        } else {
            Toast.makeText(this, "Translation type not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Teks berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Tidak ada teks untuk disalin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isProgrammingLanguage(language: String): Boolean {
        val programmingLanguages = listOf("Python", "Kotlin", "CSS", "Dart", "HTML", "JavaScript", "Java")
        return programmingLanguages.contains(language)
    }

    private fun translateCodeToExplanation(code: String, sourceLanguage: String, targetLanguage: String) {
        val prompt = """
            Briefly explain the purpose of the following $sourceLanguage code in one concise sentence in $targetLanguage:
            ```
            $code
            ```
        """.trimIndent()

        val messages = listOf(
            Message(role = "system", content = "You are a helpful assistant that provides concise explanations of programming code."),
            Message(role = "user", content = prompt)
        )

        val request = GPTChatRequest(
            model = "gpt-4",
            messages = messages,
            max_tokens = 500,
            temperature = 0.7
        )

        ApiClient.apiService.translateCode(request).enqueue(object : Callback<GPTChatResponse> {
            override fun onResponse(call: Call<GPTChatResponse>, response: Response<GPTChatResponse>) {
                if (response.isSuccessful) {
                    val translation = response.body()?.choices?.firstOrNull()?.message?.content ?: "No explanation found"
                    textInputEditTextTarget.setText(translation.trim())
                    saveTranslationHistoryToFirestore(code, translation.trim(), sourceLanguage, targetLanguage)
                    Toast.makeText(this@TranslateActivity, "Explanation successful", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@TranslateActivity, "Failed to get explanation", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GPTChatResponse>, t: Throwable) {
                Toast.makeText(this@TranslateActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun translateExplanationToCode(explanation: String, sourceLanguage: String, targetLanguage: String) {
        val prompt = """
            Please generate only the $targetLanguage code based on the following explanation in $sourceLanguage, without any additional commentary or explanation:
            ```
            $explanation
            ```
        """.trimIndent()

        val messages = listOf(
            Message(role = "system", content = "You are a helpful assistant that translates natural language descriptions into programming code."),
            Message(role = "user", content = prompt)
        )

        val request = GPTChatRequest(
            model = "gpt-4",
            messages = messages,
            max_tokens = 500,
            temperature = 0.7
        )

        ApiClient.apiService.translateCode(request).enqueue(object : Callback<GPTChatResponse> {
            override fun onResponse(call: Call<GPTChatResponse>, response: Response<GPTChatResponse>) {
                if (response.isSuccessful) {
                    val translation = response.body()?.choices?.firstOrNull()?.message?.content ?: "No code found"
                    textInputEditTextTarget.setText(translation.trim())
                    saveTranslationHistoryToFirestore(explanation, translation.trim(), sourceLanguage, targetLanguage)
                    Toast.makeText(this@TranslateActivity, "Translation successful", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@TranslateActivity, "Failed to translate", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GPTChatResponse>, t: Throwable) {
                Toast.makeText(this@TranslateActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTranslationHistoryToFirestore(inputText: String, translatedText: String, sourceLanguage: String, targetLanguage: String) {
        val user = auth.currentUser
        if (user != null) {
            val history = hashMapOf(
                "inputText" to inputText,
                "translatedText" to translatedText,
                "sourceLanguage" to sourceLanguage,
                "targetLanguage" to targetLanguage,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).collection("translations").add(history)
                .addOnSuccessListener {
                    Toast.makeText(this, "Translation history saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save translation history", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupNavigationButtons() {
        val buttonHomeScreenActivity = findViewById<ImageButton>(R.id.btn_home_on)
        buttonHomeScreenActivity.setOnClickListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
        val buttonKuisActivity = findViewById<ImageButton>(R.id.btn_kuis)
        buttonKuisActivity.setOnClickListener {
            startActivity(Intent(this, KuisActivity::class.java))
        }
        val buttonRiwayatActivity = findViewById<ImageButton>(R.id.btn_riwayat)
        buttonRiwayatActivity.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
        val buttonProfilActivity = findViewById<ImageButton>(R.id.btn_profil)
        buttonProfilActivity.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }
        val buttonKembaliActivity = findViewById<ImageButton>(R.id.btn_kembali)
        buttonKembaliActivity.setOnClickListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
    }
}
