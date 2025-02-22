package com.rosy.ngothing

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Konfigurasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Tambahkan default_web_client_id ke strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Tombol untuk memulai Google Sign-In
        val btnLogin: ImageView = findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            signOutPreviousUser() // Pastikan sesi sebelumnya dihapus
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            user?.let { firebaseUser -> checkUserInFirestore(firebaseUser) }
                        }
                    }
                } else {
                    Toast.makeText(this, "Autentikasi Google gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserInFirestore(user: FirebaseUser) {
        val userId = user.uid
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Jika data pengguna ditemukan, langsung ke dashboard
                    val username = document.getString("username") ?: "Guest"

                    // Simpan username ke SharedPreferences
                    val sharedPreferences = getSharedPreferences("USER_PREF", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("USERNAME", username)
                        apply()
                    }

                    navigateToDashboard(username)
                } else {
                    // Jika data pengguna tidak ditemukan, arahkan ke AddUsernameActivity
                    navigateToAddUsername(user.displayName ?: "Guest", userId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Terjadi kesalahan saat memeriksa data pengguna.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDashboard(username: String) {
        Toast.makeText(this, "Selamat datang, $username!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeScreenActivity::class.java)
        intent.putExtra("USERNAME", username)
        startActivity(intent)
        finishAffinity()
    }

    private fun navigateToAddUsername(displayName: String, userId: String) {
        val intent = Intent(this, AddUsernameActivity::class.java)
        intent.putExtra("DISPLAY_NAME", displayName)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finishAffinity()
    }

    private fun signOutPreviousUser() {
        auth.signOut() // Logout dari Firebase
        googleSignInClient.signOut() // Logout dari Google Sign-In

        // Hapus data lokal
        val sharedPreferences = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
