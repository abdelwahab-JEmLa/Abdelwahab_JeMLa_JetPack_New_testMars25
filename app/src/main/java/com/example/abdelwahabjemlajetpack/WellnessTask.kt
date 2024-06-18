package com.example.abdelwahabjemlajetpack

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class WellnessTask(
    val id: Int = 0,
    val label: String = "",
    var initialChecked: Boolean = false
) {
    var checked: Boolean by mutableStateOf(initialChecked)

    // No-argument constructor required for Firebase
    constructor() : this(0, "", false)
}
