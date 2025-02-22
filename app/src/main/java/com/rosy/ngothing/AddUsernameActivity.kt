package com.rosy.ngothing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddUsernameActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_username)

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Referensi elemen UI
        val usernameField: EditText = findViewById(R.id.edit_username)
        val btnSubmit: Button = findViewById(R.id.btn_submit)

        // Tombol untuk menyimpan username
        btnSubmit.setOnClickListener {
            val username = usernameField.text.toString().trim()

            // Validasi input username
            if (username.isEmpty()) {
                Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (username.length < 3) {
                Toast.makeText(
                    this,
                    "Username harus memiliki minimal 3 karakter",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                saveUsernameToFirestore(username)
            }
        }
    }

    private fun saveUsernameToFirestore(username: String) {
        val userId = auth.currentUser?.uid ?: return

        // Data pengguna yang akan disimpan
        val userMap = hashMapOf(
            "username" to username,
            "email" to auth.currentUser?.email
        )

        // Simpan ke Firestore
        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Username berhasil disimpan", Toast.LENGTH_SHORT).show()
                saveUsernameLocally(username) // Simpan username secara lokal
                navigateToHomeScreen()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan username.", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menyimpan username ke SharedPreferences
    private fun saveUsernameLocally(username: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            apply()
        }
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
        finish()
    }
}
