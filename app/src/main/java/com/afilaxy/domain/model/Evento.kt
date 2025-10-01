package com.afilaxy.domain.model

data class Evento(
    val id: String = "",
    val titulo: String,
    val data: String,
    val descricao: String? = null,
    val local: String? = null,
    val organizador: String? = null,
    val participantesMax: Int? = null
)