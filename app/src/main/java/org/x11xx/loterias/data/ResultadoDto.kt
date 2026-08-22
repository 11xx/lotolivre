package org.x11xx.loterias.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

/**
 * The wire shape of a contest. All eleven modalities answer with this same
 * object and differ only in which optional fields carry values, so the
 * per-game difference belongs in [ResultMapper], not here.
 */
@Serializable
data class ResultadoDto(
    val numero: Int,
    val dataApuracao: String,
    val listaDezenas: List<String>? = null,
    val listaDezenasSegundoSorteio: List<String>? = null,
    val trevosSorteados: List<String>? = null,
    val nomeTimeCoracaoMesSorte: String? = null,
    val acumulado: Boolean = false,
    val dataProximoConcurso: String? = null,
    val numeroConcursoProximo: Int? = null,
    @Serializable(with = MoneySerializer::class)
    val valorAcumuladoProximoConcurso: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = MoneySerializer::class)
    val valorEstimadoProximoConcurso: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = MoneySerializer::class)
    val valorArrecadado: BigDecimal = BigDecimal.ZERO,
    val listaRateioPremio: List<RateioDto> = emptyList(),
    val listaResultadoEquipeEsportiva: List<EquipeDto>? = null,
)

@Serializable
data class RateioDto(
    val descricaoFaixa: String = "",
    val faixa: Int = 0,
    val numeroDeGanhadores: Int = 0,
    @Serializable(with = MoneySerializer::class)
    val valorPremio: BigDecimal = BigDecimal.ZERO,
)

@Serializable
data class EquipeDto(
    val nuSequencial: Int = 0,
    val nomeEquipeUm: String = "",
    val nomeEquipeDois: String = "",
    val nuGolEquipeUm: Int = 0,
    val nuGolEquipeDois: Int = 0,
)

/**
 * Money arrives as a JSON double. Reading it through the literal's own text
 * keeps the centavos the server sent; going through [Double] would round them.
 */
object MoneySerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Money", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val text = when (decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content
            else -> decoder.decodeString()
        }
        return text.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }
}
