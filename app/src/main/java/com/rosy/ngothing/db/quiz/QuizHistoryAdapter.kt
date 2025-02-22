package com.rosy.ngothing.db.quiz

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.R
import com.rosy.ngothing.databinding.ItemDateHeaderBinding
import com.rosy.ngothing.databinding.ItemQuizHistoryBinding


class QuizHistoryAdapter(private val quizHistoryItems: List<QuizHistoryItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_QUIZ_HISTORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (quizHistoryItems[position]) {
            is QuizHistoryItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is QuizHistoryItem.QuizHistory -> VIEW_TYPE_QUIZ_HISTORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateHeaderViewHolder(binding)
            }
            VIEW_TYPE_QUIZ_HISTORY -> {
                val binding = ItemQuizHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                QuizHistoryViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> holder.bind(quizHistoryItems[position] as QuizHistoryItem.DateHeader)
            is QuizHistoryViewHolder -> {
                val quizHistoryItem = (quizHistoryItems[position] as QuizHistoryItem.QuizHistory).copy() // Buat salinan untuk independensi
                holder.bind(quizHistoryItem)
            }
        }
    }

    override fun getItemCount(): Int = quizHistoryItems.size

    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dateHeader: QuizHistoryItem.DateHeader) {
            binding.dateTextView.text = dateHeader.date
        }
    }

    inner class QuizHistoryViewHolder(private val binding: ItemQuizHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(quizHistoryItem: QuizHistoryItem.QuizHistory) {
            val quizHistory = quizHistoryItem.history
            binding.title.text = quizHistory.title
            binding.category.text = quizHistory.category
            binding.score.text = "${quizHistory.score}/${quizHistory.total}"

            Log.d("QuizHistoryAdapter", "Binding quiz: ${quizHistory.title} - Score: ${quizHistory.score}")

            binding.icon.setImageResource(
                when {
                    quizHistory.category.contains("Python") -> R.drawable.python
                    quizHistory.category.contains("Kotlin") -> R.drawable.kotlin
                    quizHistory.category.contains("Dart") -> R.drawable.dart
                    quizHistory.category.contains("Java") -> R.drawable.java
                    quizHistory.category.contains("HTML") -> R.drawable.html
                    quizHistory.category.contains("CSS") -> R.drawable.css
                    quizHistory.category.contains("JavaScript") -> R.drawable.js
                    else -> R.drawable.ic_launcher_background
                }
            )

            binding.score.setTextColor(
                if (quizHistory.score < 10) {
                    itemView.context.getColor(android.R.color.holo_red_dark)
                } else {
                    itemView.context.getColor(android.R.color.holo_green_dark)
                }
            )
        }
    }
}
