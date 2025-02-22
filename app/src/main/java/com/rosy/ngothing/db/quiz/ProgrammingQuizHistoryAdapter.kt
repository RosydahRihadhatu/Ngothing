package com.rosy.ngothing.db.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.R


class ProgrammingQuizHistoryAdapter(
    private val quizHistoryItems: List<ProgrammingQuizHistoryItem>
) : RecyclerView.Adapter<ProgrammingQuizHistoryAdapter.QuizHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_programming_quiz_history, parent, false)
        return QuizHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizHistoryViewHolder, position: Int) {
        val quizHistoryItem = quizHistoryItems[position] as? ProgrammingQuizHistoryItem.QuizHistory
        quizHistoryItem?.let {
            holder.bind(it.copy()) // Bind dengan salinan untuk memastikan independensi data
        }
    }

    override fun getItemCount(): Int = quizHistoryItems.size

    inner class QuizHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextViewDay: TextView = itemView.findViewById(R.id.dateTextViewDay)
        private val dateTextViewMonth: TextView = itemView.findViewById(R.id.dateTextViewMonth)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val score: TextView = itemView.findViewById(R.id.score)

        fun bind(quizHistoryItem: ProgrammingQuizHistoryItem.QuizHistory) {
            val quizHistory = quizHistoryItem.history
            dateTextViewDay.text = quizHistoryItem.day
            dateTextViewMonth.text = quizHistoryItem.month
            title.text = quizHistory.title
            category.text = quizHistory.category
            score.text = itemView.context.getString(R.string.score_format, quizHistory.score, quizHistory.total)

            // Set text color based on score
            val scoreColor = if (quizHistory.score < 10) {
                ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
            }
            score.setTextColor(scoreColor)
        }
    }
}
