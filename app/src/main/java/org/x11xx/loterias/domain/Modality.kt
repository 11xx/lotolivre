package org.x11xx.loterias.domain

import androidx.annotation.StringRes
import org.x11xx.loterias.R

/**
 * The eleven lotteries the app shows, in the order the results list presents
 * them. [slug] is the path segment the API uses; [labelRes] is the only place a
 * game is named, so relabelling one is a single string edit; [hue] is the OKLCH
 * hue angle its colour is derived from, at a lightness and chroma the theme
 * fixes for every game alike.
 */
enum class Modality(
    val slug: String,
    @param:StringRes val labelRes: Int,
    val hue: Float,
) {
    MEGA_SENA("megasena", R.string.game_mega_sena, 158f),
    LOTOFACIL("lotofacil", R.string.game_lotofacil, 330f),
    QUINA("quina", R.string.game_quina, 276f),
    LOTOMANIA("lotomania", R.string.game_lotomania, 55f),
    DUPLA_SENA("duplasena", R.string.game_dupla_sena, 25f),
    MAIS_MILIONARIA("maismilionaria", R.string.game_mais_milionaria, 300f),
    TIMEMANIA("timemania", R.string.game_timemania, 135f),
    DIA_DE_SORTE("diadesorte", R.string.game_dia_de_sorte, 78f),
    SUPER_SETE("supersete", R.string.game_super_sete, 110f),
    LOTECA("loteca", R.string.game_loteca, 205f),
    FEDERAL("federal", R.string.game_federal, 245f);

    /** The extra token some games draw alongside their numbers. */
    val extraToken: ExtraToken?
        get() = when (this) {
            TIMEMANIA -> ExtraToken.HEART_TEAM
            DIA_DE_SORTE -> ExtraToken.LUCKY_MONTH
            else -> null
        }

    companion object {
        fun ofSlug(slug: String): Modality? = entries.firstOrNull { it.slug == slug }
    }
}

enum class ExtraToken(@param:StringRes val labelRes: Int) {
    HEART_TEAM(R.string.label_heart_team),
    LUCKY_MONTH(R.string.label_lucky_month),
}
