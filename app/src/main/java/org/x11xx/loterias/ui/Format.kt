package org.x11xx.loterias.ui

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

private val BR = Locale.forLanguageTag("pt-BR")
private val dayMonth = DateTimeFormatter.ofPattern("dd/MM", BR)
private val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", BR)

fun BigDecimal.asMoney(): String = NumberFormat.getCurrencyInstance(BR).format(this)

fun LocalDate.asDayMonth(): String = format(dayMonth)

fun LocalDate.asFullDate(): String = format(fullDate)
