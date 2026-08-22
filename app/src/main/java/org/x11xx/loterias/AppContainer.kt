package org.x11xx.loterias

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import org.x11xx.loterias.data.CaixaDataSource
import org.x11xx.loterias.data.ResultsCache
import org.x11xx.loterias.data.ResultsRepository
import org.x11xx.loterias.ui.theme.ThemeMode
import java.io.File
import java.util.concurrent.TimeUnit

/** Every dependency the app has, wired by hand. There are few enough. */
class AppContainer(context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val repository = ResultsRepository(
        source = CaixaDataSource(client),
        cache = ResultsCache(File(context.cacheDir, "results")),
    )

    val themePreference = ThemePreference(context)
}

/** The one setting the app has. */
class ThemePreference(context: Context) {

    private val store = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(read())

    val mode: StateFlow<ThemeMode> = state.asStateFlow()

    fun set(mode: ThemeMode) {
        store.edit().putString(KEY, mode.name).apply()
        state.value = mode
    }

    private fun read(): ThemeMode =
        store.getString(KEY, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM

    private companion object {
        const val KEY = "theme_mode"
    }
}
