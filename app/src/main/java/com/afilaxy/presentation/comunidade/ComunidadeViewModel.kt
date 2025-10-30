package com.afilaxy.presentation.comunidade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.afilaxy.domain.model.Produto
import com.afilaxy.domain.model.Evento
import com.afilaxy.domain.model.ProjetoInfo

class ComunidadeViewModel : ViewModel() {
    
    val produtos = listOf(
        Produto("1", "Bombinha de Salbutamol", "Broncodilatador para alívio rápido"),
        Produto("2", "Espaçador", "Dispositivo para melhor inalação"),
        Produto("3", "Nebulizador", "Para medicação em casa")
    )
    
    val eventos = listOf(
        Evento("1", "Live: Controle da Asma", "25/01/2025", "Dicas para o dia a dia"),
        Evento("2", "Palestra: Medicamentos", "30/01/2025", "Uso correto dos inaladores")
    )
    
    val projetos = listOf(
        ProjetoInfo("1", "Missão Afilaxy", "Conectar pessoas com asma em emergências"),
        ProjetoInfo("2", "Parceria SUS", "Engajamento no tratamento público")
    )
    
    private fun getCommunityData(): CommunityData {
        return CommunityData(
            produtos = listOf(
                Produto("1", "Bombinha de Salbutamol", "Broncodilatador para alívio rápido"),
                Produto("2", "Espaçador", "Dispositivo para melhor inalação"),
                Produto("3", "Nebulizador", "Para medicação em casa")
            ),
            eventos = listOf(
                Evento("1", "Live: Controle da Asma", "25/01/2025", "Dicas para o dia a dia"),
                Evento("2", "Palestra: Medicamentos", "30/01/2025", "Uso correto dos inaladores")
            ),
            projetos = listOf(
                ProjetoInfo("1", "Missão Afilaxy", "Conectar pessoas com asma em emergências"),
                ProjetoInfo("2", "Parceria SUS", "Engajamento no tratamento público")
            )
        )
    }
    
    private data class CommunityData(
        val produtos: List<Produto>,
        val eventos: List<Evento>,
        val projetos: List<ProjetoInfo>
    )
}