package A_Learn.LazyC

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class A_ContactScren {
    @Composable
    fun CharacterHeader(initial: Char) {
        Text(
            text = initial.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray)
                .padding(8.dp)
        )
    }

    @Composable
    fun ContactListItem(contact: Contact) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "${contact.firstName} ${contact.lastName}")
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ContactsList(grouped: Map<Char, List<Contact>>) {
        LazyColumn {
            grouped.forEach { (initial, contactsForInitial) ->
                stickyHeader {
                    CharacterHeader(initial)
                }
                items(contactsForInitial) { contact ->
                    ContactListItem(contact)
                }
            }
        }
    }

    @Composable
    fun StaggeredPhotoGrid() {
        val itemsList = (0..5).toList()
        val itemsIndexedList = listOf("A",
            "Bggggggggggggggggggggggg",
            "Cddddddddddddddddddddddd",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",

        )
        val itemModifier = Modifier
            .border(1.dp, Color.Blue)
            .padding(16.dp)
            .wrapContentSize()

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(itemsList) {
                Text("Item is $it", itemModifier)
            }
            item {
                Text("Single item", itemModifier)
            }
            itemsIndexed(itemsIndexedList) { index, item ->
                Text("Item at index $index is $item", itemModifier)
            }
        }
    }

    @Composable
    fun MainScreen2(viewModel: ContactsViewModel = viewModel()) {
        val groupedContacts by viewModel.groupedContacts.observeAsState(emptyMap())
        Column {
           // ContactsList(grouped = groupedContacts)
            Spacer(modifier = Modifier.height(16.dp))
            StaggeredPhotoGrid()
        }
    }
}