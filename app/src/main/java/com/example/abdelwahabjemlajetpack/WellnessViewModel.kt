package com.example.abdelwahabjemlajetpack

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class WellnessViewModel : ViewModel() {
    private val database = Firebase.database
    private val refFirebase = database.getReference("tasks")

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

    fun importFromFirebase() {
        refFirebase.get().addOnSuccessListener { dataSnapshot ->
            val tasksFromFirebase = dataSnapshot.children.mapNotNull { it.getValue(WellnessTask::class.java) }
            _tasks.clear()
            _tasks.addAll(tasksFromFirebase)
        }
    }

    private fun syncWithFirebase(task: WellnessTask, remove: Boolean = false) {
        val taskRef = refFirebase.child(task.id.toString())
        if (remove) {
            taskRef.removeValue()
        } else {
            taskRef.setValue(task)
        }
    }

    private fun getWellnessTasks() = List(30) { i -> WellnessTask(i + 1, "Task #${i + 1}") }

    fun syncInitialTasksWithFirebase(tasks: List<WellnessTask>) {
        tasks.forEach { syncWithFirebase(it) }
    }
}
