package org.x11xx.loterias.data

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.x11xx.loterias.domain.Modality
import java.io.IOException

/**
 * Where results come from. The portal endpoint is undocumented and sits behind
 * a CDN, so everything above this interface is written as if a mirror could
 * replace it tomorrow.
 */
interface ResultsDataSource {
    /** The most recent contest of [modality], as raw JSON. */
    suspend fun latest(modality: Modality): String

    /** One specific contest, as raw JSON. */
    suspend fun contest(modality: Modality, contest: Int): String
}

/** The requested contest does not exist — which the portal reports as a 500. */
class ContestNotFoundException(message: String) : IOException(message)

class CaixaDataSource(
    private val client: OkHttpClient,
    private val baseUrl: HttpUrl = DEFAULT_BASE_URL.toHttpUrl(),
) : ResultsDataSource {

    override suspend fun latest(modality: Modality): String =
        get(baseUrl.newBuilder().addPathSegment(modality.slug).build())

    override suspend fun contest(modality: Modality, contest: Int): String =
        get(
            baseUrl.newBuilder()
                .addPathSegment(modality.slug)
                .addPathSegment(contest.toString())
                .build(),
        )

    private fun get(url: HttpUrl): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            // An out-of-range contest answers 500 rather than 404, so a plain
            // isSuccessful check would report a server fault to the user.
            if (response.code == 500) {
                throw ContestNotFoundException("no contest at $url")
            }
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} for $url")
            }
            return response.body.string()
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://servicebus2.caixa.gov.br/portaldeloterias/api/"
    }
}

/** Shared parser: the payload carries many fields the app has no use for. */
val ResultsJson: Json = Json { ignoreUnknownKeys = true }
