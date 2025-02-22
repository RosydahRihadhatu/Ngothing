package com.rosy.ngothing.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rosy.ngothing.databinding.ActivityRiwayatKuisFragmentBinding
import com.rosy.ngothing.db.quiz.QuizHistory
import com.rosy.ngothing.db.quiz.QuizHistoryAdapter
import com.rosy.ngothing.db.quiz.QuizHistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentRiwayatKuisActivity : Fragment() {

    private var _binding: ActivityRiwayatKuisFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: QuizHistoryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityRiwayatKuisFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quizHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadQuizHistory()
    }

    private fun loadQuizHistory() {
        val userId = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val quizHistoryList = db.collection("users")
                    .document(userId)
                    .collection("quiz_history")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { document ->
                        document.toObject(QuizHistory::class.java)?.copy() // Membuat salinan setiap objek
                    }

                val groupedQuizHistory = groupQuizHistoryByDate(quizHistoryList)

                withContext(Dispatchers.Main) {
                    adapter = QuizHistoryAdapter(groupedQuizHistory)
                    binding.quizHistoryRecyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal memuat riwayat kuis", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun groupQuizHistoryByDate(quizHistoryList: List<QuizHistory>): List<QuizHistoryItem> {
        val groupedItems = mutableListOf<QuizHistoryItem>()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val currentDate = Calendar.getInstance()
        var currentDateString: String? = null

        quizHistoryList.sortedByDescending { it.getDateInMillis() }.forEach { history ->
            val historyDate = Calendar.getInstance().apply { timeInMillis = history.getDateInMillis() }
            val date = when {
                isSameDay(currentDate, historyDate) -> "Terbaru"
                isYesterday(currentDate, historyDate) -> "Kemarin"
                else -> dateFormat.format(historyDate.time)
            }
            if (date != currentDateString) {
                groupedItems.add(QuizHistoryItem.DateHeader(date))
                currentDateString = date
            }
            groupedItems.add(QuizHistoryItem.QuizHistory(history.copy())) // Salinan baru
        }

        return groupedItems
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yesterday, cal2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
