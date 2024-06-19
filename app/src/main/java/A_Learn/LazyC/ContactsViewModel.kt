package A_Learn.LazyC

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class ContactsViewModel : ViewModel() {
    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    init {
        // Populate with sample data
        _contacts.value = listOf(
            Contact("Alice", "Smith"),
            Contact("Bob", "Johnson"),
            Contact("Charlie", "Williams"),
            Contact("a1", "a1"),
            Contact("b2", "b2"),
            Contact("c3", "c3"),
            Contact("d4", "d4"),
            Contact("e5", "e5"),
            Contact("f6", "f6"),
            Contact("g7", "g7"),
            Contact("h8", "h8"),
            Contact("i9", "i9"),
            Contact("j10", "j10"),
            Contact("k11", "k11"),
            Contact("l12", "l12"),
            Contact("m13", "m13"),
            Contact("n14", "n14"),


            // Add more contacts
        )
    }

    val groupedContacts: LiveData<Map<Char, List<Contact>>> = _contacts.map { contacts ->
        contacts.groupBy { it.firstName[0] }
    }
}
