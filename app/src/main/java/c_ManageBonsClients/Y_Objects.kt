package com.example.abdelwahabjemlajetpack.c_ManageBonsClients

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import java.io.File
import kotlin.math.abs
import kotlin.math.round


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadImageFromPathBC(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc

    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageExist ?: defaultDrawable,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
        ) {
            it
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(1000) // Set a larger size
                .thumbnail(0.25f) // Start with 25% quality
                .fitCenter() // Ensure the image fits within the bounds
                .transition(DrawableTransitionOptions.withCrossFade()) // Smooth transition as quality improves
        }
    }
}
fun generateClientColor(clientName: String): Color {
    val hash = abs(clientName.hashCode())
    val hue = (hash % 360).toFloat()
    return Color.hsl(hue, 0.4f, 0.6f)
}
fun calculateTotalProfit(articles: List<ArticlesAcheteModele>): Double {
    return articles.sumOf { article ->
        val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
            article.monPrixVentBM else article.monPrixVentFireStoreBM
        val prixVente = round(monPrixVentDetermineBM * 10) / 10
        val prixAchatC = if (article.prixAchat == 0.0) prixVente else article.prixAchat
        val profit = prixVente - prixAchatC
        profit * article.totalQuantity
    }
}
fun calculateClientTotal(clientArticles: List<ArticlesAcheteModele>): Double {
    return clientArticles.filter { !it.nonTrouveState }.sumOf { article ->
        val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
            article.monPrixVentBM else article.monPrixVentFireStoreBM
        round(monPrixVentDetermineBM * 10) / 10 * article.totalQuantity
    }
}

fun calculateClientProfit(articles: List<ArticlesAcheteModele>, clientName: String): Double {
    return articles.filter { it.nomClient == clientName && !it.nonTrouveState }
        .sumOf { article ->
            val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
                article.monPrixVentBM else article.monPrixVentFireStoreBM
            val prixVente = round(monPrixVentDetermineBM * 10) / 10
            val prixAchatC = if (article.prixAchat == 0.0||(article.monPrixVentBM == 0.0 && article.monPrixVentFireStoreBM == 0.0)||(article.monBenificeFireStoreBM < 0.0 )) prixVente else article.prixAchat
            val profit = prixVente - prixAchatC
            profit * article.totalQuantity
        }
}
