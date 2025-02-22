package com.rosy.ngothing.db.quiz

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.R

data class Quiz(
    val title: String,
    val topicCount: Int,
    val progress: Int,
    val total: Int
)

class QuizAdapter(private var quizList: List<Quiz>, private val listener: OnItemClickListener) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(quiz: Quiz)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz, parent, false)
        return QuizViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.bind(quiz, listener)
    }

    override fun getItemCount() = quizList.size

    // Menggunakan DiffUtil untuk update data secara efisien
    fun updateData(newQuizList: List<Quiz>) {
        val diffCallback = QuizDiffCallback(quizList, newQuizList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        quizList = newQuizList
        diffResult.dispatchUpdatesTo(this)
    }

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuizTitle: TextView = itemView.findViewById(R.id.tvQuizTitle)
        private val tvTopicCount: TextView = itemView.findViewById(R.id.tvTopicCount)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        @SuppressLint("SetTextI18n")
        fun bind(quiz: Quiz, listener: OnItemClickListener) {
            tvQuizTitle.text = quiz.title
            tvTopicCount.text = "${quiz.topicCount} Topik"
            tvProgress.text = "${quiz.progress}/${quiz.total}"
            progressBar.max = quiz.total
            progressBar.progress = quiz.progress

            itemView.setOnClickListener {
                listener.onItemClick(quiz)
            }
        }
    }

    // DiffUtil.Callback untuk membandingkan perubahan pada daftar kuis
    class QuizDiffCallback(
        private val oldList: List<Quiz>,
        private val newList: List<Quiz>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Bandingkan berdasarkan judul sebagai identitas unik kuis, bisa disesuaikan
            return oldList[oldItemPosition].title == newList[newItemPosition].title
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Bandingkan seluruh isi item
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
