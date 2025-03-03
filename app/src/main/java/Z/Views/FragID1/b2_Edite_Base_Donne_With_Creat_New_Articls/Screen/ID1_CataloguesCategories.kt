package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Screen

import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.UI.A_MainList
import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.ViewModel.ExtensionVM_A4FragID_1
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ID1_CataloguesCategories(
    extensionvmA4fragid1: ExtensionVM_A4FragID_1,
    modifier: Modifier = Modifier,
) {
    A_MainList(
        modifier = modifier,
        extensionvmA4fragid1
    )
}


