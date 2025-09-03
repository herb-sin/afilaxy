package com.afilaxy.domain.model

data class Produto(
    val id: String = "",
    val nome: String,
    val descricao: String,
    val imageUrl: String? = null,
    val preco: Double? = null,
    val categoria: String? = null
)