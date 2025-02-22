package com.rosy.ngothing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rosy.ngothing.bahasaprofil.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup tombol navigasi dan data pengguna
        setupNavigationButtons()
        displayUsername() // Load and display the username

        // Set fragment default ke FragmentPythonActivity
        replaceFragment(FragmentPythonActivity())

        // Setup listener untuk tiap TextView yang mengganti fragmen
        setupFragmentListeners()

        // Setup tombol logout
        findViewById<ImageView>(R.id.btn_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun displayUsername() {
        val tvUsername = findViewById<TextView>(R.id.txt_username)

        // Ambil username dari SharedPreferences terlebih dahulu
        val sharedPreferences = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("USERNAME", null)

        if (savedUsername != null) {
            // Tampilkan username yang sudah tersimpan secara lokal
            tvUsername.text = "$savedUsername"
        } else {
            // Ambil dari Firestore jika tidak ditemukan di SharedPreferences
            val userId = auth.currentUser?.uid ?: return
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Guest"
                        tvUsername.text = "$username"
                        saveUsernameLocally(username) // Simpan username ke SharedPreferences
                    } else {
                        tvUsername.text = "Guest"
                    }
                }
                .addOnFailureListener {
                    tvUsername.text = "Halo, Guest"
                    Toast.makeText(this, "Gagal memuat nama pengguna", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUsernameLocally(username: String) {
        val sharedPreferences = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("USERNAME", username)
            apply()
        }
    }

    private fun setupFragmentListeners() {
        findViewById<TextView>(R.id.txt_python_profil).setOnClickListener {
            replaceFragment(FragmentPythonActivity())
        }
        findViewById<TextView>(R.id.txt_kotlin_profil).setOnClickListener {
            replaceFragment(FragmentKotlinActivity())
        }
        findViewById<TextView>(R.id.txt_dart_profil).setOnClickListener {
            replaceFragment(FragmentDartActivity())
        }
        findViewById<TextView>(R.id.txt_java_profil).setOnClickListener {
            replaceFragment(FragmentJavaActivity())
        }
        findViewById<TextView>(R.id.txt_html_profil).setOnClickListener {
            replaceFragment(FragmentHtmlActivity())
        }
        findViewById<TextView>(R.id.txt_css_profil).setOnClickListener {
            replaceFragment(FragmentCssActivity())
        }
        findViewById<TextView>(R.id.txt_javascript_profil).setOnClickListener {
            replaceFragment(FragmentJavascriptActivity())
        }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.btn_ya).setOnClickListener {
            logout()
            alertDialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.btn_tidak).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun logout() {
        // Clear user session
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        Toast.makeText(this, "Anda berhasil keluar", Toast.LENGTH_SHORT).show()
    }


    private fun setupNavigationButtons() {
        findViewById<ImageButton>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btn_riwayat).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btn_kuis).setOnClickListener {
            startActivity(Intent(this, KuisActivity::class.java))
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_profil, fragment)
            .commit()
    }
}   
