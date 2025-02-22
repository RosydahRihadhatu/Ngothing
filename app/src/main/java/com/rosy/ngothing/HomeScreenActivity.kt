package com.rosy.ngothing

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.db.CustomAdapter
import com.rosy.ngothing.db.translation.TranslationHistory
import com.rosy.ngothing.db.translation.TranslationHistoryAdapter
import com.rosy.ngothing.ocr.OcrManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeScreenActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_IMAGE_PICK = 102
        private const val REQUEST_CROP = 103
        private const val TAG = "HomeScreenActivity"
    }

    private var isSwapped = false
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var ocrManager: OcrManager
    private lateinit var translationHistoryAdapter: TranslationHistoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameTextView: TextView
    private lateinit var spinnerSourceLanguage: Spinner
    private lateinit var spinnerTargetLanguage: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ocrManager = OcrManager(this)

        usernameTextView = findViewById(R.id.txt_username)
        loadUsername() // Load username when the activity starts

        setupNavigationButtons()

        val programmingLanguages = listOf("Python", "Kotlin", "CSS", "Dart", "HTML", "JavaScript", "Java")
        val programmingIcons = listOf(
            R.drawable.python, R.drawable.kotlin, R.drawable.css, R.drawable.dart,
            R.drawable.html, R.drawable.js, R.drawable.java
        )

        val targetLanguages = listOf("Indonesia", "English(US)")
        val targetIcons = listOf(R.drawable.indonesia, R.drawable.english)

        spinnerSourceLanguage = findViewById(R.id.spinner_source_language)
        spinnerTargetLanguage = findViewById(R.id.spinner_target_language)
        val switchButton: ImageButton = findViewById(R.id.btn_switch)

        val customAdapterProgramming = CustomAdapter(this, programmingLanguages, programmingIcons)
        val customAdapterTarget = CustomAdapter(this, targetLanguages, targetIcons)

        spinnerSourceLanguage.adapter = customAdapterProgramming
        spinnerTargetLanguage.adapter = customAdapterTarget

        switchButton.setOnClickListener {
            swapViews()
        }

        val btnCamera: ImageView = findViewById(R.id.btn_kamera)
        val btnGallery: ImageView = findViewById(R.id.btn_galeri)

        btnCamera.setOnClickListener {
            if (checkPermission()) {
                openCamera()
            } else {
                requestPermission()
            }
        }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
        btnGallery.setOnClickListener {
            openGallery()
        }

        textInputEditText = findViewById(R.id.textinputedittext)

        val buttonSubmit: ImageButton = findViewById(R.id.btn_kaca)
        buttonSubmit.setOnClickListener {
            val codeInput = textInputEditText.text.toString()
            val sourceLanguageIndex = if (isSwapped) spinnerTargetLanguage.selectedItemPosition else spinnerSourceLanguage.selectedItemPosition
            val targetLanguageIndex = if (isSwapped) spinnerSourceLanguage.selectedItemPosition else spinnerTargetLanguage.selectedItemPosition

            val selectedSourceLanguageName = (if (isSwapped) targetLanguages else programmingLanguages)[sourceLanguageIndex]
            val selectedTargetLanguageName = (if (isSwapped) programmingLanguages else targetLanguages)[targetLanguageIndex]
            val selectedSourceLanguageIcon = (if (isSwapped) targetIcons else programmingIcons)[sourceLanguageIndex]
            val selectedTargetLanguageIcon = (if (isSwapped) programmingIcons else targetIcons)[targetLanguageIndex]

            val intent = Intent(this, TranslateActivity::class.java).apply {
                putExtra("CODE_INPUT", codeInput)
                putExtra("SOURCE_LANGUAGE_NAME", selectedSourceLanguageName)
                putExtra("TARGET_LANGUAGE_NAME", selectedTargetLanguageName)
                putExtra("SOURCE_LANGUAGE_ICON", selectedSourceLanguageIcon)
                putExtra("TARGET_LANGUAGE_ICON", selectedTargetLanguageIcon)
            }
            startActivity(intent)
        }

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_historynew)
        recyclerView.layoutManager = LinearLayoutManager(this)
        translationHistoryAdapter = TranslationHistoryAdapter(emptyList())
        recyclerView.adapter = translationHistoryAdapter

        // Load translation history from Firestore
        loadTranslationHistoryFromFirestore()
    }

    override fun onResume() {
        super.onResume()
        loadUsername() // Pastikan memuat ulang username saat aktivitas kembali aktif
    }

    private fun loadUsername() {
        val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        if (savedUsername != null) {
            usernameTextView.text = "Halo, $savedUsername"
        } else {
            val userId = auth.currentUser?.uid ?: return
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "User"
                        usernameTextView.text = "Halo, $username"
                        saveUsernameLocally(username)
                    } else {
                        usernameTextView.text = "Halo, User"
                    }
                }
                .addOnFailureListener {
                    usernameTextView.text = "Halo, User"
                    Toast.makeText(this, "Gagal memuat username.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUsernameLocally(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            apply()
        }
    }

    private fun loadTranslationHistoryFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("translations")
            .get()
            .addOnSuccessListener { documents ->
                val historyList = documents.map { document ->
                    TranslationHistory(
                        inputText = document.getString("inputText") ?: "",
                        translatedText = document.getString("translatedText") ?: "",
                        timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                        userId = userId,
                        sequenceNumber = document.getLong("sequenceNumber")?.toInt() ?: 0
                    )
                }
                translationHistoryAdapter.updateData(historyList)
                Log.d(TAG, "Loaded history from Firestore: ${historyList.size} items")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading history from Firestore", e)
                Toast.makeText(this, "Failed to load translation history", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = File(cacheDir, "captured_image.jpg")
        val imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        val options = UCrop.Options()
        options.setCompressionQuality(100)
        options.setMaxBitmapSize(8000)
        options.setHideBottomControls(true)
        options.setFreeStyleCropEnabled(true)
        UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(4096, 4096)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageFile = File(cacheDir, "captured_image.jpg")
                    val imageUri = Uri.fromFile(imageFile)
                    startCrop(imageUri)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        startCrop(selectedImageUri)
                    }
                }
                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(data!!)
                    if (resultUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                        processOCR(bitmap)
                    } else {
                        Log.e(TAG, "Crop failed, resultUri is null")
                    }
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(data!!)
                    Log.e(TAG, "Crop error: ", cropError)
                }
            }
        }
    }

    private fun processOCR(bitmap: Bitmap) {
        lifecycleScope.launch {
            val extractedText = withContext(Dispatchers.Default) {
                ocrManager.performOCR(bitmap)
            }
            Log.d(TAG, "Extracted Text: $extractedText")
            textInputEditText.setText(extractedText)
        }
    }

    private fun swapViews() {
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraint_layout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        if (isSwapped) {
            // Set constraints to original positions
            constraintSet.clear(R.id.spinner_source_language, ConstraintSet.START)
            constraintSet.clear(R.id.spinner_source_language, ConstraintSet.END)
            constraintSet.clear(R.id.spinner_target_language, ConstraintSet.START)
            constraintSet.clear(R.id.spinner_target_language, ConstraintSet.END)

            constraintSet.connect(R.id.spinner_source_language, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
            constraintSet.connect(R.id.spinner_source_language, ConstraintSet.END, R.id.btn_switch, ConstraintSet.START, 8)
            constraintSet.connect(R.id.spinner_target_language, ConstraintSet.START, R.id.btn_switch, ConstraintSet.END, 8)
            constraintSet.connect(R.id.spinner_target_language, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
        } else {
            // Swap constraints
            constraintSet.clear(R.id.spinner_source_language, ConstraintSet.START)
            constraintSet.clear(R.id.spinner_source_language, ConstraintSet.END)
            constraintSet.clear(R.id.spinner_target_language, ConstraintSet.START)
            constraintSet.clear(R.id.spinner_target_language, ConstraintSet.END)

            constraintSet.connect(R.id.spinner_source_language, ConstraintSet.START, R.id.btn_switch, ConstraintSet.END, 8)
            constraintSet.connect(R.id.spinner_source_language, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
            constraintSet.connect(R.id.spinner_target_language, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
            constraintSet.connect(R.id.spinner_target_language, ConstraintSet.END, R.id.btn_switch, ConstraintSet.START, 8)
        }

        constraintSet.applyTo(constraintLayout)

        // Swap selections
        val sourceSelection = spinnerSourceLanguage.selectedItemPosition
        val targetSelection = spinnerTargetLanguage.selectedItemPosition

        spinnerSourceLanguage.setSelection(targetSelection)
        spinnerTargetLanguage.setSelection(sourceSelection)

        isSwapped = !isSwapped
    }

    private fun setupNavigationButtons() {
        findViewById<ImageButton>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btn_kuis).setOnClickListener {
            startActivity(Intent(this, KuisActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_riwayat).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_profil).setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_user).setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }

        findViewById<TextView>(R.id.btn_riwayatbaru).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
