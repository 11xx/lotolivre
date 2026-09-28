package org.x11xx.loterias.ui.share

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.android.awaitFrame
import org.x11xx.loterias.R
import org.x11xx.loterias.domain.LotteryResult
import org.x11xx.loterias.ui.ResultSummary
import org.x11xx.loterias.ui.resultAsText
import org.x11xx.loterias.ui.theme.AppFontFamily
import org.x11xx.loterias.ui.theme.LotoLivreTheme
import org.x11xx.loterias.ui.theme.ThemeMode
import java.io.File

/**
 * The image is composed at a density of its own rather than the screen's, so
 * a phone at 2x and a tablet at 3.5x produce the same picture.
 */
private val ShareDensity = Density(density = 3f, fontScale = 1f)

/** Shares a contest as a picture, with the plain-text version as its caption. */
suspend fun Context.shareResultImage(
    result: LotteryResult,
    summary: ResultSummary,
    dark: Boolean,
) {
    val activity = findActivity() ?: return
    val bitmap = renderShareImage(activity, result, dark)
    val uri = FileProvider.getUriForFile(this, "$packageName.shares", writePng(bitmap, result))

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, resultAsText(result, summary))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
}

@OptIn(ExperimentalTextApi::class)
private suspend fun renderShareImage(
    activity: ComponentActivity,
    result: LotteryResult,
    dark: Boolean,
): Bitmap {
    // Resolve the downloaded family first: an unresolved font would be caught
    // mid-swap and the picture would ship with the fallback face.
    runCatching { createFontFamilyResolver(activity).preload(AppFontFamily) }

    val widthPx = with(ShareDensity) { ShareCardWidth.roundToPx() }
    val host = activity.findViewById<ViewGroup>(android.R.id.content)
    val view = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            CompositionLocalProvider(LocalDensity provides ShareDensity) {
                LotoLivreTheme(if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                    ShareCard(result)
                }
            }
        }
    }

    // Laid out and drawn like any other view, just never over the screen. A
    // scroll container measures its child without a height limit, so a tall
    // result comes out whole rather than cut at the height of the screen.
    val frame = ScrollView(activity).apply {
        translationX = -100_000f
        addView(view, ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
    host.addView(frame, ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.MATCH_PARENT))
    try {
        while (view.width == 0 || view.height == 0) awaitFrame()
        awaitFrame()
        return view.drawToBitmap()
    } finally {
        host.removeView(frame)
    }
}

private fun Context.writePng(bitmap: Bitmap, result: LotteryResult): File {
    val directory = File(cacheDir, "shares").apply { mkdirs() }
    val file = File(directory, "${result.modality.slug}-${result.contest}.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return file
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
