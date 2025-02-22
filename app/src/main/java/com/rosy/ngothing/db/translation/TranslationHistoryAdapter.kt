package com.rosy.ngothing.db.translation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rosy.ngothing.R
import java.text.SimpleDateFormat
import java.util.*

class TranslationHistoryAdapter(private var historyList: List<TranslationHistory>) :
    RecyclerView.Adapter<TranslationHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagePreview: ImageView = view.findViewById(R.id.image_preview)
        val textTimestamp: TextView = view.findViewById(R.id.text_timestamp)
        val textDate: TextView = view.findViewById(R.id.text_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_translation_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        // Set default image or specific preview image if applicable
        holder.imagePreview.setImageResource(R.drawable.script)

        // Format timestamp for date and time components
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault())
        holder.textTimestamp.text = dateFormat.format(Date(history.timestamp))

        // Format date for display
        val dateFormatDisplay = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        holder.textDate.text = dateFormatDisplay.format(Date(history.timestamp))
    }

    override fun getItemCount() = historyList.size

    fun updateData(newHistoryList: List<TranslationHistory>) {

        historyList = newHistoryList
        notifyDataSetChanged()
    }
}
