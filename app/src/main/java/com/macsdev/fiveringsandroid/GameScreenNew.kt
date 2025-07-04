package com.macsdev.fiverings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreenNew() {
    val viewModel = GameViewModel
    val gameState = viewModel.gameState

    val colorScheme = darkColorScheme(
        primary = Color(0xFF4A90E2),
        background = Color(0xFF1A202C),
        surface = Color.White.copy(alpha = 0.05f),
        onPrimary = Color.White,
        onSurface = Color.White,
        onBackground = Color.White
    )

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
            BoxWithConstraints {
                val isPortrait = maxHeight > maxWidth

                if (isPortrait) {
                    // --- ПОРТРЕТНЫЙ РЕЖИМ ---
                    Column(
                        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Scoreboard(
                            scoreWhite = gameState.scoreWhite,
                            scoreBlack = gameState.scoreBlack,
                            currentPlayer = gameState.currentPlayer,
                            highlightColor = colorScheme.primary
                        )
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            GameCanvas(
                                modifier = Modifier.padding(16.dp),
                                gameState = gameState,
                                selectedRingId = viewModel.selectedRingId,
                                onRingSelected = { viewModel.onRingSelected(it) },
                                onRotate = { viewModel.onRotate(it) }
                            )
                        }
                        Button(
                            onClick = { viewModel.onNewGame() },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("Выйти", fontSize = 16.sp)
                        }
                    }
                } else {
                    // --- АЛЬБОМНЫЙ РЕЖИМ ---
                    Row(
                        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.5f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            GameCanvas(
                                modifier = Modifier.padding(16.dp),
                                gameState = gameState,
                                selectedRingId = viewModel.selectedRingId,
                                onRingSelected = { viewModel.onRingSelected(it) },
                                onRotate = { viewModel.onRotate(it) }
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Scoreboard(
                                scoreWhite = gameState.scoreWhite,
                                scoreBlack = gameState.scoreBlack,
                                currentPlayer = gameState.currentPlayer,
                                highlightColor = colorScheme.primary
                            )
                            Button(
                                onClick = { viewModel.onNewGame() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Выйти", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}