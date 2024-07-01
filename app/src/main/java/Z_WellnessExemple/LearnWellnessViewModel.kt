package Z_WellnessExemple

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LearnWellnessViewModel : ViewModel() {
    private val database = Firebase.database
    private val refFirebase = database.getReference("tasks")

    private val _tasks = mutableListOf<WellnessTask>().toMutableStateList()
    val tasks: List<WellnessTask>
        get() = _tasks

    init {
        importFromFirebase()
    }

    fun remove(item: WellnessTask) {
        _tasks.remove(item)
        syncWithFirebase(item, true)
    }

    fun changeTaskChecked(item: WellnessTask, checked: Boolean) {
        _tasks.find { it.id == item.id }?.let { task ->
            task.bigCardView = checked
            syncWithFirebase(task)
        }
    }

    fun importFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            val dataSnapshot = refFirebase.get().await()
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

    fun syncInitialTasksWithFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            val initialTasks = List(30) { i -> WellnessTask(i + 1, "Task #${i + 1}") }
            initialTasks.forEach { syncWithFirebase(it) }
            _tasks.clear()
            _tasks.addAll(initialTasks)
        }
    }
}
