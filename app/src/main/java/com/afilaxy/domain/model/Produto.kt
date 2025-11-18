package com.afilaxy.domain.model

import java.math.BigDecimal

data class Produto(
    val id: String = "",
    val nome: String,
    val descricao: String,
    val imageUrl: String? = null,
    val preco: BigDecimal? = null,
    val precoOriginal: BigDecimal? = null,
    val desconto: String? = null,
    val farmacia: String? = null,
    val categoria: String? = null,
    val cupom: String? = null,
    val validadeCupom: String? = null
)