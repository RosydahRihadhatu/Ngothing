package com.rosy.ngothing.db.quiz

sealed class QuizHistoryItem {
    data class DateHeader(val date: String) : QuizHistoryItem()
    data class QuizHistory(val history: com.rosy.ngothing.db.quiz.QuizHistory) : QuizHistoryItem()
}
