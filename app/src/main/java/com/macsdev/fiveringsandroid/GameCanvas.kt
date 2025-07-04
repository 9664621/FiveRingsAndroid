package com.macsdev.fiverings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toPx
import kotlin.math.*

// --- UI-представления, специфичные для экрана ---
internal data class UiRing(val id: Int, val x: Float, val y: Float, val radius: Float)
internal data class UiPoint(val x: Float, val y: Float)

@Composable
internal fun GameCanvas(
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

        val textMeasurer = rememberTextMeasurer()
        val arrowTextLayoutResult = remember(sizePx) {
            textMeasurer.measure(
                "⟳",
                style = TextStyle(fontSize = (sizePx * 0.05f).sp)
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
                .pointerInput(uiRings, selectedRingId) {
                    detectTapGestures { offset ->
                        if (selectedRingId != null) {
                            val buttonRadius = sizePx * 0.05f
                            val buttonsY = sizePx - buttonRadius * 1.5f
                            val ccwButtonX = sizePx / 2 - buttonRadius * 1.8f
                            val cwButtonX = sizePx / 2 + buttonRadius * 1.8f

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
                val buttonsY = this.size.height - buttonRadius * 1.5f
                drawRotationButtons(
                    Offset(this.size.width/2 - buttonRadius * 1.8f, buttonsY),
                    Offset(this.size.width/2 + buttonRadius * 1.8f, buttonsY),
                    buttonRadius,
                    textMeasurer,
                    arrowTextLayoutResult
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
    radius: Float,
    textMeasurer: TextMeasurer,
    textLayoutResult: TextLayoutResult
) {
    val panelColor = Color.White.copy(alpha = 0.1f)
    val highlightColor = Color(0xFF4A90E2)
    val stroke = Stroke(width = 2.dp.toPx())

    // Левая кнопка (⟲)
    drawCircle(panelColor, radius, ccwPos)
    drawCircle(highlightColor, radius, ccwPos, style = stroke)
    drawText(
        textMeasurer = textMeasurer,
        text = "⟲",
        style = textLayoutResult.style,
        topLeft = Offset(
            ccwPos.x - textLayoutResult.size.width / 2,
            ccwPos.y - textLayoutResult.size.height / 2
        )
    )

    // Правая кнопка (⟳)
    drawCircle(panelColor, radius, cwPos)
    drawCircle(highlightColor, radius, cwPos, style = stroke)
    drawText(
        textMeasurer = textMeasurer,
        text = "⟳",
        style = textLayoutResult.style,
        topLeft = Offset(
            cwPos.x - textLayoutResult.size.width / 2,
            cwPos.y - textLayoutResult.size.height / 2
        )
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