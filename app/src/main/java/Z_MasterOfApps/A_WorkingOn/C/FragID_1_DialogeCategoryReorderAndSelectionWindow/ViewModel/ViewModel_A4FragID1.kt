package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepository
import androidx.lifecycle.ViewModel

class ViewModel_A4FragID1(
    val a_ProduitModelRepository: A_ProduitModelRepository,
    val i_CategoriesRepository: I_CategoriesRepository,
    val h_GroupesCategoriesRepository: H_GroupesCategoriesRepository
) : ViewModel() {

}
