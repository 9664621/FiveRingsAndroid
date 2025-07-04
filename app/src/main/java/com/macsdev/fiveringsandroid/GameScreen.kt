package com.macsdev.fiverings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// --- UI-представления, специфичные для экрана ---
private data class UiRing(val id: Int, val x: Float, val y: Float, val radius: Float)
private data class UiPoint(val x: Float, val y: Float)

@Composable
fun GameScreen() {
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

@Composable
private fun Scoreboard(
    modifier: Modifier = Modifier,
    scoreWhite: Int,
    scoreBlack: Int,
    currentPlayer: Player,
    highlightColor: Color
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
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

@Composable
private fun GameCanvas(
    modifier: Modifier = Modifier,
    gameState: GameState,
    selectedRingId: Int?,
    onRingSelected: (Int?) -> Unit,
    onRotate: (Int) -> Unit
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val density = LocalDensity.current
        val sizePx = with(density) { minOf(maxWidth, maxHeight).toPx() }

        val (uiRings, uiPoints) = remember(sizePx) { calculateGeometry(sizePx) }

        Canvas(
            modifier = Modifier.fillMaxSize()
                .pointerInput(uiRings, selectedRingId) {
                    detectTapGestures { offset ->
                        if (selectedRingId != null) {
                            val buttonRadius = sizePx * 0.05f
                            val buttonsY = this.size.height - buttonRadius * 1f
                            val ccwButtonX = this.size.width / 2 - buttonRadius * 1.5f
                            val cwButtonX = this.size.width / 2 + buttonRadius * 1.5f

                            if (hypot(offset.x - ccwButtonX, offset.y - buttonsY) < buttonRadius) {
                                onRotate(-1); return@detectTapGestures
                            }
                            if (hypot(offset.x - cwButtonX, offset.y - buttonsY) < buttonRadius) {
                                onRotate(1); return@detectTapGestures
                            }
                        }

                        val tappedRing = uiRings.minByOrNull {
                            abs(hypot(offset.x - it.x, offset.y - it.y) - it.radius)
                        }

                        val tapTolerance = sizePx * 0.05f
                        if (tappedRing != null && abs(hypot(offset.x - tappedRing.x, offset.y - tappedRing.y) - tappedRing.radius) < tapTolerance) {
                            onRingSelected(tappedRing.id)
                        } else {
                            onRingSelected(null)
                        }
                    }
                }
        ) {
            uiRings.forEach { ring ->
                drawRing(ring, selectedRingId)
            }

            val stoneRadius = min(this.size.width, this.size.height) * 0.01f
            uiPoints.forEachIndexed { index, point ->
                val player = gameState.boardState[index] ?: Player.NONE
                if (player != Player.NONE) {
                    drawStone(point.x, point.y, stoneRadius, player)
                }
            }

            if (selectedRingId != null) {
                val buttonRadius = min(this.size.width, this.size.height) * 0.05f
                val buttonsY = this.size.height - buttonRadius * 1f
                drawRotationButtons(
                    Offset(this.size.width/2 - buttonRadius * 1.5f, buttonsY),
                    Offset(this.size.width/2 + buttonRadius * 1.5f, buttonsY),
                    buttonRadius
                )
            }
        }
    }
}

private fun DrawScope.drawRing(ring: UiRing, selectedRingId: Int?) {
    val isSelected = ring.id == selectedRingId
    drawCircle(
        color = if (isSelected) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.3f),
        radius = ring.radius,
        center = Offset(ring.x, ring.y),
        style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx())
    )
}

private fun DrawScope.drawStone(x: Float, y: Float, radius: Float, player: Player) {
    drawCircle(
        color = if (player == Player.WHITE) Color.White else Color(0xFF212121),
        radius = radius,
        center = Offset(x, y)
    )
    if (player == Player.BLACK) {
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = radius,
            center = Offset(x, y),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawRotationButtons(
    ccwPos: Offset,
    cwPos: Offset,
    radius: Float
) {
    val stroke = Stroke(width = 2.dp.toPx())
    val arcRadius = radius * 0.6f
    val arcSize = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)

    // Левая кнопка (↶)
    drawCircle(Color.White.copy(alpha = 0.1f), radius, ccwPos)
    drawCircle(Color(0xFF4A90E2), radius, ccwPos, style = stroke)
    drawArc(
        color = Color.White,
        startAngle = -90f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(ccwPos.x - arcRadius, ccwPos.y - arcRadius),
        size = arcSize,
        style = stroke
    )
    val ccwAngleRad = Math.toRadians(180.0).toFloat()
    val ccwArrowCenter = ccwPos + Offset(cos(ccwAngleRad), sin(ccwAngleRad)) * arcRadius
    drawArrowHead(ccwArrowCenter, angleDeg = -90f, length = radius * 0.3f)

    // Правая кнопка (↷)
    drawCircle(Color.White.copy(alpha = 0.1f), radius, cwPos)
    drawCircle(Color(0xFF4A90E2), radius, cwPos, style = stroke)
    drawArc(
        color = Color.White,
        startAngle = -90f,
        sweepAngle = -270f,
        useCenter = false,
        topLeft = Offset(cwPos.x - arcRadius, cwPos.y - arcRadius),
        size = arcSize,
        style = stroke
    )
    val cwAngleRad = Math.toRadians(0.0).toFloat()
    val cwArrowCenter = cwPos + Offset(cos(cwAngleRad), sin(cwAngleRad)) * arcRadius
    drawArrowHead(cwArrowCenter, angleDeg = -90f, length = radius * 0.3f)
}

private fun DrawScope.drawArrowHead(center: Offset, angleDeg: Float, length: Float) {
    val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val dir = Offset(cos(angleRad), sin(angleRad))
    val base = center - dir * (length * 0.5f)

    val perp = Offset(-dir.y, dir.x) * (length / 2)
    val p1 = base + perp
    val p2 = base - perp
    val p3 = center + dir * (length * 0.2f)

    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        },
        color = Color.White
    )
}

private fun calculateGeometry(size: Float): Pair<List<UiRing>, List<UiPoint>> {
    val majorRadius = size * 0.17f
    val innerRadius = size * 0.25f
    val outerRadius = size * 0.28f
    val centerX = size / 2f
    val centerY = size / 2f

    val uiRings = (0 until 10).map { id ->
        val angle = (id / 2).toFloat() / 5f * 2f * PI.toFloat() - PI.toFloat() / 2f
        val majorX = centerX + cos(angle) * majorRadius
        val majorY = centerY + sin(angle) * majorRadius
        UiRing(id, majorX, majorY, if (id % 2 == 0) innerRadius else outerRadius)
    }

    val uiPointsMap = mutableMapOf<String, UiPoint>()
    for (i in uiRings.indices) {
        for (j in i + 1 until uiRings.size) {
            val intersections = calculateIntersections(uiRings[i], uiRings[j])
            for (p in intersections) {
                val key = "${p.x.roundToInt()},${p.y.roundToInt()}"
                if (!uiPointsMap.containsKey(key)) {
                    uiPointsMap[key] = UiPoint(p.x, p.y)
                }
            }
        }
    }
    val sortedUiPoints = uiPointsMap.values.sortedWith(compareBy({ it.y }, { it.x }))
    return uiRings to sortedUiPoints
}

private fun calculateIntersections(c1: UiRing, c2: UiRing): List<Offset> {
    val d = hypot(c2.x - c1.x, c2.y - c1.y)
    if (d > c1.radius + c2.radius || d < abs(c1.radius - c2.radius) || d == 0f) return emptyList()
    val a = (c1.radius.pow(2) - c2.radius.pow(2) + d.pow(2)) / (2 * d)
    val h = sqrt(max(0f, c1.radius.pow(2) - a.pow(2)))
    val x0 = c1.x + a * (c2.x - c1.x) / d
    val y0 = c1.y + a * (c2.y - c1.y) / d
    val rx = -h * (c2.y - c1.y) / d
    val ry = h * (c2.x - c1.x) / d
    return listOf(Offset(x0 + rx, y0 + ry), Offset(x0 - rx, y0 - ry))
}