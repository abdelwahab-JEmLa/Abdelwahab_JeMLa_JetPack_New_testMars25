package com.example.abdelwahabjemlajetpack

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class WellnessViewModel : ViewModel() {

    private val _tasks = getWellnessTasks().toMutableStateList()
    val tasks: List<WellnessTask>
        get() = _tasks

    init {
        syncInitialTasksWithFirebase(_tasks)
    }

    fun remove(item: WellnessTask) {
        _tasks.remove(item)
        syncWithFirebase(item, true)
    }

    fun changeTaskChecked(item: WellnessTask, checked: Boolean) {
        _tasks.find { it.id == item.id }?.let { task ->
            task.checked = checked
            syncWithFirebase(task)
        }
    }

    private fun syncWithFirebase(task: WellnessTask, remove: Boolean = false) {
        val refFirebase = Firebase.database.getReference("tasks").child(task.id.toString())

        if (remove) {
            refFirebase.removeValue()
        } else {
            refFirebase.setValue(task)
        }
    }

    private fun syncInitialTasksWithFirebase(tasks: List<WellnessTask>) {
        tasks.forEach { task ->
            syncWithFirebase(task)
        }
    }

    private fun getWellnessTasks() = List(30) { i -> WellnessTask(i, "Task #$i") }
}
