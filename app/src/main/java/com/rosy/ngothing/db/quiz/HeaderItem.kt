package com.rosy.ngothing.db.quiz

data class HeaderItem(
    val title: String,
    val childItems: List<ChildItem>,
    var isExpanded: Boolean = false
)
