package com.example.abdelwahabjemlajetpack

import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MainActionsFab(
    headOfViewModels: HeadOfViewModels,
    onClickToSwitchToKoinPrototypeNav: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isTimerActive by headOfViewModels.isTimerActive.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Colors update FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    headOfViewModels.updateColorsFromArticles()
                }
            },
            containerColor = Color.Red,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = "Update Colors",
                tint = Color.White
            )
        }

        // In FloatingActionButtonS.kt after the Colors update FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    // Switch to KoinPrototype navigation
                    onClickToSwitchToKoinPrototypeNav()
                }
            },
            containerColor = Color.Red,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Transform, // Or another appropriate icon
                contentDescription = "Switch to Koin Navigation",
                tint = Color.White
            )
        }

        // Categories sync FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    headOfViewModels.importCategoriesFromFirebase()
                }
            },
            containerColor = Color.Blue,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync Categories",
                tint = Color.White
            )
        }

        // Firebase data transfer FAB
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    headOfViewModels.transferFirebaseDataArticlesAcheteModele()
                }
            },
            containerColor = Color.Green,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = "Transfer Firebase Data",
                tint = Color.White
            )
        }

        // Timer press-hold button
        PressHoldButton(
            onPress = { headOfViewModels.startTimer() },
            onRelease = { headOfViewModels.stopTimer() },
            isActive = isTimerActive
        )
    }
}

@Composable
fun PressHoldButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    isActive: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed) {
        if (pressed) {
            onPress()
        } else {
            onRelease()
        }
    }

    FloatingActionButton(
        onClick = { /* Do nothing on click */ },
        modifier = Modifier.size(56.dp),
        containerColor = if (isActive) Color.Gray else Color.Blue,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Activate Timer",
            tint = Color.White
        )
    }
}

