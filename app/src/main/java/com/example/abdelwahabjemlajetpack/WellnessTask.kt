package com.example.abdelwahabjemlajetpack

data class WellnessTask(
    val id: Int = 0,
    val label: String = "",
    var bigCardView: Boolean = false
) {
    constructor() : this(0, "", false)
}
