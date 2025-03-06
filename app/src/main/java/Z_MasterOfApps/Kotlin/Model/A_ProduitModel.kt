package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@IgnoreExtraProperties
class A_ProduitModel(
    var id: Long = 0,
    var itsTempProduit: Boolean = false,
    init_nom: String = "",
    init_besoin_To_Be_Updated: Boolean = false,
    initialNon_Trouve: Boolean = false,
    init_colours_Et_Gouts: List<ColourEtGout_Model> = emptyList(),
    init_bonCommendDeCetteCota: GrossistBonCommandes? = null,
    initBonsVentDeCetteCota: List<ClientBonVentModel> = emptyList(),
    init_visible: Boolean = true,
    init_historiqueBonsVents: List<ClientBonVentModel> = emptyList(),
    init_historiqueBonsCommend: List<GrossistBonCommandes> = emptyList(),
) {
    var nom: String by mutableStateOf(init_nom)
    var besoinToBeUpdated: Boolean by mutableStateOf(init_besoin_To_Be_Updated)
    var non_Trouve: Boolean by mutableStateOf(initialNon_Trouve)
    var isVisible: Boolean by mutableStateOf(init_visible)

    var parentCategoryId by mutableLongStateOf(0L)
    var indexInParentCategorie by mutableIntStateOf(0)

    var statuesBase: StatuesBase by mutableStateOf(StatuesBase())
    @IgnoreExtraProperties
    class StatuesBase(
        coloursEtGoutsIds: List<Long> = emptyList(), // Changed parameter name to avoid shadowing
        var ilAUneCouleurAvecImage: Boolean = false,
        var characterProduit: CharacterProduit = CharacterProduit(),
        var infosCoutes: InfosCoutes = InfosCoutes(),
    ) {
        // Convert to mutable state list
        private val _coloursEtGoutsIds = coloursEtGoutsIds.toMutableStateList()
        var coloursEtGoutsIds: List<Long>
            get() = _coloursEtGoutsIds
            set(value) {
                _coloursEtGoutsIds.clear()
                _coloursEtGoutsIds.addAll(value)
            }

        // Function to add a new color ID
        fun addColorId(colorId: Long) {
            _coloursEtGoutsIds.add(colorId)
        }

        var naAucunImage: Boolean by mutableStateOf(false)
        var sonImageBesoinActualisation: Boolean by mutableStateOf(false)
        var imageGlidReloadTigger: Int by mutableStateOf(0)

        var prePourCameraCapture: Boolean by mutableStateOf(false)
        var seTrouveAuDernieDuCamionCarCCarton: Boolean by mutableStateOf(false)

        @IgnoreExtraProperties
        data class InfosCoutes(
            var monPrixAchat: Double = 0.0,
            var monPrixVent: Double = 0.0,
        )

        @IgnoreExtraProperties
        data class CharacterProduit(
            var emballageCartone: Boolean = false,
        )
    }

    @get:Exclude
    var coloursEtGouts: SnapshotStateList<ColourEtGout_Model> =
        init_colours_Et_Gouts.toMutableStateList()

    var coloursEtGoutsList: List<ColourEtGout_Model>
        get() = coloursEtGouts.toList()
        set(value) {
            coloursEtGouts.clear()
            coloursEtGouts.addAll(value)
        }
    @IgnoreExtraProperties
    class ColourEtGout_Model(
        val id: Long = 1,
        var nom: String = "Non Defini",
        var imogi: String = "🎨",
        var sonImageNeExistPas: Boolean = false,
        var position_Du_Couleur_Au_Produit: Long = 0,
    )

    var bonCommendDeCetteCota by mutableStateOf<GrossistBonCommandes?>(
        init_bonCommendDeCetteCota
    )
    @get:Exclude
    var historiqueBonsCommend: SnapshotStateList<GrossistBonCommandes> =
        init_historiqueBonsCommend.toMutableStateList()
    var historiqueBonsCommendList: List<GrossistBonCommandes>
        get() = historiqueBonsCommend.toList()
        set(value) {
            historiqueBonsCommend.clear()
            historiqueBonsCommend.addAll(value)
        }
    @IgnoreExtraProperties
    class GrossistBonCommandes(
        var vid: Long = 0,
        var idGrossistChoisi: Long = 0,
        init_coloursEtGoutsCommendee: List<ColoursGoutsCommendee> = emptyList(),
    ) {
        var mutableBasesStates: MutableBasesStates? by mutableStateOf(MutableBasesStates())
        @IgnoreExtraProperties
        class MutableBasesStates {
            var cPositionCheyCeGrossit: Boolean by mutableStateOf(false)
            var positionProduitDonGrossistChoisiPourAcheterCeProduit: Int by mutableStateOf(0)
            var dateInString by mutableStateOf("2025-01-01")
            var currentCreditBalance by mutableStateOf(0.0)
        }

        @get:Exclude
        var coloursEtGoutsCommendee: SnapshotStateList<ColoursGoutsCommendee> =
            init_coloursEtGoutsCommendee.toMutableStateList()
        var coloursEtGoutsCommendeeList: List<ColoursGoutsCommendee>
            get() = coloursEtGoutsCommendee.toList()
            set(value) {
                coloursEtGoutsCommendee.clear()
                coloursEtGoutsCommendee.addAll(value)
            }
        @IgnoreExtraProperties
        class ColoursGoutsCommendee(
            val id: Long = 1,
            var nom: String = "Non Defini",
            var emogi: String = "🎨",
        ) {
            var quantityAchete: Int by mutableIntStateOf(0)
        }
    }

    @get:Exclude
    var bonsVentDeCetteCota: SnapshotStateList<ClientBonVentModel> =
        initBonsVentDeCetteCota.toMutableStateList()

    // Update the bonsVentDeCetteCotaList setter
    var bonsVentDeCetteCotaList: List<ClientBonVentModel>
        get() = bonsVentDeCetteCota.toList()
        set(value) {
            bonsVentDeCetteCota.clear()
            bonsVentDeCetteCota.addAll(value)
        }
    @get:Exclude
    var historiqueBonsVents: SnapshotStateList<ClientBonVentModel> =
        init_historiqueBonsVents.toMutableStateList()

    var historiqueBonsVentsList: List<ClientBonVentModel>
        get() = historiqueBonsVents.toList()
        set(value) {
            historiqueBonsVents.clear()
            historiqueBonsVents.addAll(value)
        }
    @IgnoreExtraProperties
    class ClientBonVentModel(
        vid: Long = 0,
        var clientIdChoisi: Long = 0,
        var produitStatueDeBaseDeChezCeClient: StatueDeBase = StatueDeBase(),
        init_colours_achete: List<ColorAchatModel> = emptyList(),
    ) {
        // Basic information
        var bonStatueDeBase by mutableStateOf(BonStatueDeBase())
        @IgnoreExtraProperties
        data class StatueDeBase(
            var positionDonClientsList: Int = 0,
        )
        // Status management
        @IgnoreExtraProperties
        class BonStatueDeBase {
            var lastUpdateTimestamp: Long by mutableStateOf(System.currentTimeMillis())
        }

        @get:Exclude
        var colours_Achete: SnapshotStateList<ColorAchatModel> =
            init_colours_achete.toMutableStateList()

        var coloursAcheteList: List<ColorAchatModel>
            get() = colours_Achete.toList()
            set(value) {
                colours_Achete.clear()
                colours_Achete.addAll(value)
            }
        @IgnoreExtraProperties
        class ColorAchatModel(
            var vidPosition: Long = 0,
            var couleurId: Long = 0,
            var nom: String = "",
            var quantity_Achete: Int = 0,
            var imogi: String = ""
        )
    }
    companion object{
        fun A_ProduitModel.ExtraiGrossistInfos(
            viewModel: ViewModelInitApp
        ) = viewModel._modelAppsFather.grossistsDataBase.find { it.id == this.bonCommendDeCetteCota?.idGrossistChoisi }

    }

    constructor() : this(0)
}

interface A_ProduitModelRepository {
    var modelDatas: SnapshotStateList<A_ProduitModel>
    val progressRepo: MutableStateFlow<Float> //

    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<A_ProduitModel>, Flow<Float>>
    fun updateModelDatas(datas: SnapshotStateList<A_ProduitModel>)

    companion object {
        val ancienFireBaseRef = firebaseDatabase.getReference("e_DBJetPackExport")
        val caReference = ref_HeadOfModels.child("produits")
    }
}

class A_ProduitModelRepositoryImpl : A_ProduitModelRepository {
    override var modelDatas: SnapshotStateList<A_ProduitModel> = mutableStateListOf()
    override val progressRepo: MutableStateFlow<Float> = MutableStateFlow(0f) // Initialize progressRepo

    private var listener: ValueEventListener? = null

    init {
        // Initialize the listener when the repository is created
        startDatabaseListener()
    }

    private fun startDatabaseListener() {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val datas = mutableListOf<A_ProduitModel>()
                    val totalItems = snapshot.childrenCount.toInt()
                    var processedItems = 0

                    modelDatas.clear()
                    progressRepo.value = 0f

                    for (dataSnapshot in snapshot.children) {
                        val data = dataSnapshot.getValue(A_ProduitModel::class.java)
                        data?.let { cat ->
                            datas.add(cat)
                            modelDatas.add(cat)
                        }

                        processedItems++
                        progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                    }

                    progressRepo.value = 1.0f
                } catch (e: Exception) {
                    // Handle the exception
                    Log.e("A_ProduitModelRepositoryImpl", "Error loading data: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("A_ProduitModelRepositoryImpl", "Database error: ${error.message}")
            }
        }

        // Attach the listener to the Firebase reference
        listener?.let {
            A_ProduitModelRepository.caReference.addValueEventListener(it)
        }
    }

    override suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<A_ProduitModel>, Flow<Float>> {
        val progressFlow = MutableStateFlow(0f)

        val datasLisning = suspendCancellableCoroutine<List<A_ProduitModel>> { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val datas = mutableListOf<A_ProduitModel>()
                        val totalItems = snapshot.childrenCount.toInt()
                        var processedItems = 0

                        modelDatas.clear()
                        progressFlow.value = 0f

                        for (dataSnapshot in snapshot.children) {
                            val data = dataSnapshot.getValue(A_ProduitModel::class.java)
                            data?.let { cat ->
                                datas.add(cat)
                                modelDatas.add(cat)
                            }

                            processedItems++
                            progressFlow.value = processedItems.toFloat() / totalItems.toFloat()
                        }

                        progressFlow.value = 1.0f
                        continuation.resume(datas)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Database error: ${error.message}"))
                }
            }

            A_ProduitModelRepository.caReference.addValueEventListener(listener)
            continuation.invokeOnCancellation {
                A_ProduitModelRepository.caReference.removeEventListener(listener)
            }
        }

        return Pair(datasLisning, progressFlow)
    }

    override fun updateModelDatas(datas: SnapshotStateList<A_ProduitModel>) {
        modelDatas = datas

        datas.forEach {
            A_ProduitModelRepository.caReference.child(it.id.toString()).setValue(it)
        }
    }

    fun cleanup() {
        // Remove the listener when the repository is no longer needed
        listener?.let {
            A_ProduitModelRepository.caReference.removeEventListener(it)
        }
    }
}
