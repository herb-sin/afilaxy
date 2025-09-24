package com.afilaxy.domain.model

import java.math.BigDecimal

data class Produto(
    val id: String = "",
    val nome: String,
    val descricao: String,
    val imageUrl: String? = null,
    val preco: BigDecimal? = null,
    val categoria: String? = null
)