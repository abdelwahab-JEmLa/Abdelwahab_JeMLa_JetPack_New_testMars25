package B_Edit_Base_Donne

//@Composable
//fun DisplayAndroidLabPractice(
//    editeBaseDonneViewModel: EditeBaseDonneViewModel,
//    modifier: Modifier = Modifier,
//) {
//    val articlesBaseDonne by editeBaseDonneViewModel.articlesBaseDonne.collectAsState()
//
//    Column(modifier = modifier.fillMaxSize()) {
//        ListsController(
//            articles = articlesBaseDonne,
//            editeBaseDonneViewModel = editeBaseDonneViewModel
//        )
//    }
//}
//
//@Composable
//fun ListsController(
//    articles: List<BaseDonne>,
//    editeBaseDonneViewModel: EditeBaseDonneViewModel,
//    modifier: Modifier = Modifier,
//) {
//    LazyColumn(modifier = modifier) {
//        items(articles, key = { it.idArticle }) { article ->
//            StatesController(article = article, editeBaseDonneViewModel = editeBaseDonneViewModel, modifier = Modifier.fillMaxWidth())
//        }
//    }
//}
//
//@Composable
//fun StatesController(
//    article: BaseDonne,
//    editeBaseDonneViewModel: EditeBaseDonneViewModel,
//    modifier: Modifier = Modifier
//) {
//    var articleState by remember { mutableStateOf(article.copy()) }
//
//    DisplayArticle(
//        articleState = articleState,
//        onValueChangePrixDeVent = { newText ->
//            articleState = calculateNewValues(
//                newText, articleState, "monPrixVent",  editeBaseDonneViewModel
//            )
//        },
//        onValueChangeBenfice = { newText ->
//            articleState = calculateNewValues(
//                newText, articleState, "monBenfice", editeBaseDonneViewModel
//            )
//        },
//        modifier = modifier
//    )
//}
//
//fun calculateNewValues(
//    newValue: String?,
//    article: BaseDonne,
//    nomColonne: String,
//    editeBaseDonneViewModel: EditeBaseDonneViewModel
//): BaseDonne {
//    val newArticle = article.copy()
//    val value = newValue?.toDoubleOrNull() ?: 0.0
//
//    when (nomColonne) {
//        "monBenfice" -> {
//            newArticle.monBenfice = value
//        }
//        "prixDeVentTotaleChezClient" -> {
//            newArticle.prixDeVentTotaleChezClient = value
//        }
//        "monPrixVent" -> {
//            newArticle.monPrixVent = value
//        }
//    }
//
//    if (nomColonne != "monPrixVent") {
//        newArticle.monPrixVent = newArticle.monBenfice + article.monPrixAchat
//    }
//    if (nomColonne != "prixDeVentTotaleChezClient") {
//        newArticle.prixDeVentTotaleChezClient = article.clienPrixVentUnite * article.nmbrUnite
//    }
//    if (nomColonne != "monBenfice") {
//        newArticle.monBenfice = newArticle.monPrixVent - article.monPrixAchat
//    }
//    editeBaseDonneViewModel.updateArticle(newArticle)
//    return newArticle
//}
//
//@Composable
//fun DisplayArticle(
//    articleState: BaseDonne,
//    onValueChangePrixDeVent: (String) -> Unit,
//    onValueChangeBenfice: (String) -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    Spacer(modifier = Modifier.height(8.dp))
//    Card(modifier = modifier.padding(10.dp)) {
//        Column {
//            val monPrixVentToString = articleState.monPrixVent.toString()
//            OutlinedTextField(
//                value = monPrixVentToString,
//                onValueChange = onValueChangePrixDeVent,
//                label = { Text("mpv>$monPrixVentToString") },
//                modifier = modifier.fillMaxWidth(),
//                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
//            )
//            Spacer(modifier = Modifier.height(15.dp))
//            val monBenficeToString = articleState.monBenfice.toString()
//            OutlinedTextField(
//                value = monBenficeToString,
//                onValueChange = onValueChangeBenfice,
//                label = { Text("mBe>$monBenficeToString") },
//                modifier = modifier.fillMaxWidth(),
//                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
//            )
//        }
//    }
//}
