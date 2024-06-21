package A_Learn.LazyC

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class WellnessTask(
    val id: Int,
    val label: String,
    var bigCardView: Boolean = false
)

@Composable
fun WellnessTaskGrid() {
    var tasks by rememberSaveable { mutableStateOf(generateTasks()) }
    var expandedItemIndex by rememberSaveable { mutableIntStateOf(-1) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        val numColumns = 3
        val items = getItemsForDisplay(tasks, expandedItemIndex, numColumns)

        itemsIndexed(items) { _, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                item.forEach { (task, itemIndex) ->
                    if (itemIndex != -1) {
                        TaskCard(
                            task = task,
                            isExpanded = (itemIndex == expandedItemIndex),
                            onTaskClick = {
                                expandedItemIndex = if (expandedItemIndex == itemIndex) -1 else itemIndex
                                tasks = tasks.map { if (it.id == task.id) it.copy(bigCardView = !it.bigCardView) else it }
                            },
                            modifier = if (expandedItemIndex == itemIndex) Modifier.fillMaxWidth() else Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun getItemsForDisplay(tasks: List<WellnessTask>, expandedItemIndex: Int, columns: Int): List<List<Pair<WellnessTask, Int>>> {
    val rows = mutableListOf<List<Pair<WellnessTask, Int>>>()
    var currentRow = mutableListOf<Pair<WellnessTask, Int>>()

    tasks.forEachIndexed { i, task ->
        if (i == expandedItemIndex) {
            if (currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
            }
            rows.add(listOf(task to i))
        } else {
            currentRow.add(task to i)
            if (currentRow.size == columns) {
                rows.add(currentRow)
                currentRow = mutableListOf()
            }
        }
    }
    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }

    return rows
}

@Composable
fun TaskCard(task: WellnessTask, isExpanded: Boolean, onTaskClick: () -> Unit, modifier: Modifier) {
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onTaskClick() }
            .height(cardHeight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = task.label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun generateTasks(): List<WellnessTask> {
    return List(20) { WellnessTask(id = it, label = "Task $it") }
}
