package com.rosy.ngothing.riwayat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.rosy.ngothing.R
import com.rosy.ngothing.db.translation.TranslationHistory
import com.rosy.ngothing.db.translation.TranslationHistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentRiwayatTerjemahanActivity : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TranslationHistoryAdapter
    private var historyList: List<TranslationHistory> = listOf()
    private var filteredHistoryList: List<TranslationHistory> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_riwayat_terjemahan_fragment, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TranslationHistoryAdapter(historyList)
        recyclerView.adapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            loadHistoryFromFirestore(userId)
        } else {
            Log.e("FragmentRiwayat", "User is not logged in.")
        }
    }

    private fun loadHistoryFromFirestore(userId: String) {
        db.collection("users").document(userId).collection("translations")
            .get()
            .addOnSuccessListener { documents ->
                historyList = documents.map { document ->
                    TranslationHistory(
                        inputText = document.getString("inputText") ?: "",
                        translatedText = document.getString("translatedText") ?: "",
                        timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                        userId = userId,
                        sequenceNumber = document.getLong("sequenceNumber")?.toInt() ?: 0
                    )
                }
                filteredHistoryList = historyList
                adapter.updateData(filteredHistoryList)
                Log.d("FragmentRiwayat", "Loaded history from Firestore: ${historyList.size} items")
            }
            .addOnFailureListener { e ->
                Log.e("FragmentRiwayat", "Error loading history from Firestore", e)
            }
    }

    private fun filterHistory(query: String?) {
        Log.d("FragmentRiwayat", "Filtering history with query: $query")
        val filteredList = if (query.isNullOrEmpty()) {
            historyList
        } else {
            historyList.filter {
                val timestampFormatted = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date(it.timestamp))
                it.inputText.contains(query, ignoreCase = true) ||
                        it.translatedText.contains(query, ignoreCase = true) ||
                        timestampFormatted.contains(query, ignoreCase = true)
            }
        }
        Log.d("FragmentRiwayat", "Filtered history: ${filteredList.size} items")
        filteredHistoryList = filteredList
        adapter.updateData(filteredHistoryList)
    }
}
