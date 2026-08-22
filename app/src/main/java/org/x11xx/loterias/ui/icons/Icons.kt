package org.x11xx.loterias.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The glyphs the app needs, drawn on one 24dp grid at one stroke weight rather
 * than pulled from an icon set. Icon() tints them, so they are built in black.
 */
private fun icon(name: String, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }.build()

val MenuIcon: ImageVector = icon("menu") {
    moveTo(4f, 7f); lineTo(20f, 7f)
    moveTo(4f, 12f); lineTo(20f, 12f)
    moveTo(4f, 17f); lineTo(20f, 17f)
}

val ListIcon: ImageVector = icon("list") {
    moveTo(4f, 7f); lineTo(20f, 7f)
    moveTo(4f, 12f); lineTo(20f, 12f)
    moveTo(4f, 17f); lineTo(14f, 17f)
}

val BackIcon: ImageVector = icon("back") {
    moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
}

val ForwardIcon: ImageVector = icon("forward") {
    moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
}

val ShareIcon: ImageVector = icon("share") {
    moveTo(12f, 15f); lineTo(12f, 4f)
    moveTo(8.5f, 7.5f); lineTo(12f, 4f); lineTo(15.5f, 7.5f)
    moveTo(5f, 13f); lineTo(5f, 20f); lineTo(19f, 20f); lineTo(19f, 13f)
}

val InfoIcon: ImageVector = icon("info") {
    moveTo(21f, 12f)
    arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3f, 12f)
    arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 21f, 12f)
    moveTo(12f, 11.5f); lineTo(12f, 16.5f)
    moveTo(12f, 7.9f); lineTo(12f, 8.1f)
}
