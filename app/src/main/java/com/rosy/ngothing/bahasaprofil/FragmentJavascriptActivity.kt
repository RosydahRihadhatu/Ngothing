package com.rosy.ngothing.bahasaprofil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.R
import com.rosy.ngothing.db.quiz.ProgrammingQuizHistoryAdapter
import com.rosy.ngothing.db.quiz.ProgrammingQuizHistoryItem
import com.rosy.ngothing.db.quiz.QuizHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentJavascriptActivity : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: ProgrammingQuizHistoryAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_fragment_javascript, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadPassedQuizzes("JavaScript")
    }

    private fun loadPassedQuizzes(language: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val quizHistoryList = getQuizHistoryFromFirestore(language)
            val groupedQuizHistory = groupQuizHistoryByDate(quizHistoryList)

            withContext(Dispatchers.Main) {
                if (groupedQuizHistory.isEmpty()) {
                    emptyTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter = ProgrammingQuizHistoryAdapter(groupedQuizHistory)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    private suspend fun getQuizHistoryFromFirestore(language: String): List<QuizHistory> {
        val quizHistoryList = mutableListOf<QuizHistory>()
        val userId = auth.currentUser?.uid ?: return emptyList() // Get dynamic userId

        val quizHistoryCollection = firestore.collection("users")
            .document(userId) // Use dynamic userId
            .collection("quiz_history")

        val snapshot = quizHistoryCollection
            .whereEqualTo("language", language)
            .whereGreaterThanOrEqualTo("score", 10)
            .get()
            .await()

        for (document in snapshot.documents) {
            val quizHistory = document.toObject(QuizHistory::class.java)
            if (quizHistory != null) {
                quizHistoryList.add(quizHistory)
            }
        }

        return quizHistoryList
    }

    private fun groupQuizHistoryByDate(quizHistoryList: List<QuizHistory>): List<ProgrammingQuizHistoryItem> {
        val groupedItems = mutableListOf<ProgrammingQuizHistoryItem>()
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val groupedByDate = quizHistoryList.groupBy {
            it.date?.let { timestamp ->
                try {
                    val date = timestamp.toDate() // Ubah Timestamp ke Date
                    outputDateFormat.format(date) // Format Date ke String
                } catch (e: Exception) {
                    null // Jika terjadi kesalahan parsing tanggal
                }
            } ?: "Unknown Date"
        }


        for ((date, histories) in groupedByDate) {
            val dateParts = date.split(" ")
            val day = dateParts.getOrElse(0) { "Unknown" }
            val month = dateParts.getOrElse(1) { "Unknown" }

            for (history in histories) {
                groupedItems.add(ProgrammingQuizHistoryItem.QuizHistory(history, day, month))
            }
        }

        return groupedItems
    }
}
