package br.unirio.gedapp.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Document(

    val id: String,

    val tenant: String,

    val title: String,

    val summary: String,

    val date: LocalDate,

    val content: String,

    val fileName: String,

    val category: Long,

    val registeredBy: Long,

    val registeredAt: LocalDateTime
)