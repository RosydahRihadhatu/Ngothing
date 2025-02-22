package com.rosy.ngothing.db

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication : Application() {

    companion object {
        private lateinit var firestore: FirebaseFirestore
        private lateinit var instance: MyApplication

        fun getFirestore(): FirebaseFirestore {
            return firestore
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()
    }
}
