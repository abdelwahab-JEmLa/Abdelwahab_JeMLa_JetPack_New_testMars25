package com.example.abdelwahabjemlajetpack

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

private val refFirebase = Firebase.database.getReference("tasks")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun A_Edite_Base_Screen() {
    var articlesList by rememberSaveable { mutableStateOf(generateArticles()) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(remember { derivedStateOf { listState.firstVisibleItemIndex } }) {
        coroutineScope.launch {
            articlesList = articlesList.map { it.copy(funChangeImagsDimention = false) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LazyVerticalGrid Sample") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(paddingValues)
        ) {
            items(items = articlesList.chunked(2)) { pairOfArticles ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pairOfArticles.forEach { article ->
                            TestCard(article) { updatedArticle ->
                                articlesList = articlesList.map {
                                    if (it.a_c_idarticle_c == updatedArticle.a_c_idarticle_c) updatedArticle else it.copy(funChangeImagsDimention = false)
                                }
                                selectedArticle = updatedArticle
                            }
                        }
                    }
                    val clickedArticle = pairOfArticles.firstOrNull { it.funChangeImagsDimention }
                    if (clickedArticle != null) {
                        DisplayClickedArticle(clickedArticle)
                    }
                }
            }
        }
    }
}

@Composable
fun TestCard(article: Article, onClick: (Article) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick(article.copy(funChangeImagsDimention = !article.funChangeImagsDimention)) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_1"
                LoadImageFromPath(imagePath = imagePath)
            }
        }
    }
}

@Composable
fun DisplayClickedArticle(article: Article) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column {
            val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_1"
            LoadImageFromPath(imagePath = imagePath)
        }
    }
}

@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(imagePath)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}

@Serializable
data class Article(
    val a_c_idarticle_c: Int,
    var a_d_nomarticlefinale_c: String,
    var a_b_classementc: Double,
    val a_e_nomarab_c: String,
    val a_f_nombrcat_c: Int,
    var a_g_cat1_c: String?,
    var a_h_cat2_c: String?,
    var a_i_cat3_c: String?,
    var a_j_cat4_c: String?,
    var a_k_catego_c: String?,
    val a_l_nmbunite_c: Int,
    val a_m_nmbucaron_c: Int,
    var a_n_affichageu_c: Boolean,
    val a_o_commment_se_vent_c: String?,
    val a_p_affiche_boit_si_uniter_sidispo_c: String?,
    var a_q_prixachat_c: Double,
    var a_r_prixdevent_c: Double,
    val a_s_quan__1_c: Int,
    var a_t_benfice_prix_1_q1_c: Double,
    var a_u_prix_1_q1_c: Double,
    var a_v_nomvocale: String,
    val a_w_idcatalogue_categorie: String,
    var a_x_idcategorie: Double,
    var funChangeImagsDimention: Boolean = false,
    var a_z_namecate: String,
    var b_a_idcatalogue: Double,
    val b_b_idcatalogueac0: String,
    var b_c_namecatalogue: String,
    val b_d_datecreationcategorie: String
)

fun generateArticles(): List<Article> {
    return List(200) {
        Article(
            a_c_idarticle_c = it,
            a_d_nomarticlefinale_c = "Article $it",
            a_b_classementc = 0.0,
            a_e_nomarab_c = "Arabic Name $it",
            a_f_nombrcat_c = it,
            a_g_cat1_c = "Category 1",
            a_h_cat2_c = "Category 2",
            a_i_cat3_c = "Category 3",
            a_j_cat4_c = "Category 4",
            a_k_catego_c = "Category",
            a_l_nmbunite_c = 10,
            a_m_nmbucaron_c = 5,
            a_n_affichageu_c = true,
            a_o_commment_se_vent_c = "Comment",
            a_p_affiche_boit_si_uniter_sidispo_c = "Display",
            a_q_prixachat_c = 100.0,
            a_r_prixdevent_c = 150.0,
            a_s_quan__1_c = 10,
            a_t_benfice_prix_1_q1_c = 50.0,
            a_u_prix_1_q1_c = 100.0,
            a_v_nomvocale = "Vocal Name",
            a_w_idcatalogue_categorie = "Catalogue",
            a_x_idcategorie = 1.0,
            a_z_namecate = "Name",
            b_a_idcatalogue = 1.0,
            b_b_idcatalogueac0 = "2021-01-01",
            b_c_namecatalogue = "Catalogue Name",
            b_d_datecreationcategorie = "2021-01-01"
        )
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
    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
        val initialTasks = refFirebase.get().await().children.mapNotNull { it.getValue(WellnessTask::class.java) }
        // _tasks is assumed to be a mutable list
        _tasks.clear()
        _tasks.addAll(initialTasks)
    }
}
