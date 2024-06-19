package com.example.abdelwahabjemlajetpack

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class WellnessTask(
    val id: Int = 0,
    val label: String = "",
    var checked: Boolean = false
) {
    constructor() : this(0, "", false)
}
