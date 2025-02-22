package com.rosy.ngothing.db

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.rosy.ngothing.R

class CustomAdapter(
    context: Context,
    private val languages: List<String>,
    private val icons: List<Int>
) : ArrayAdapter<String>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val safePosition = position % languages.size // Indeks modular untuk menghindari out of bounds

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.custom_spinner_item, parent, false)

        val language = languages[safePosition]
        val iconRes = icons[safePosition]

        val icon = view.findViewById<ImageView>(R.id.icon)
        val textView = view.findViewById<TextView>(R.id.language_name)

        icon.setImageResource(iconRes)
        textView.text = language

        return view
    }
}
