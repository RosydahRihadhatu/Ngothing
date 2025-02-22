package com.rosy.ngothing.db.quiz

sealed class ProgrammingQuizHistoryItem {
    data class QuizHistory(val history: com.rosy.ngothing.db.quiz.QuizHistory, val day: String, val month: String) : ProgrammingQuizHistoryItem()
}
