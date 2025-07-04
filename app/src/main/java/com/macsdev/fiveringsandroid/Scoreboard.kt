package com.macsdev.fiverings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Scoreboard(
    modifier: Modifier = Modifier,
    scoreWhite: Int,
    scoreBlack: Int,
    currentPlayer: Player,
    highlightColor: Color
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val whiteModifier = if (currentPlayer == Player.WHITE) {
            Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .border(2.dp, highlightColor, RoundedCornerShape(12.dp))
        } else Modifier

        val blackModifier = if (currentPlayer == Player.BLACK) {
            Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .border(2.dp, highlightColor, RoundedCornerShape(12.dp))
        } else Modifier

        ScoreItem(modifier = whiteModifier, "Белые", scoreWhite)
        ScoreItem(modifier = blackModifier, "Черные", scoreBlack)
    }
}

@Composable
private fun ScoreItem(modifier: Modifier = Modifier, label: String, score: Int) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f))
        Text(score.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
