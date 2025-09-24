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
    private val _uiState = MutableStateFlow(ComunidadeUiState())
    val uiState: StateFlow<ComunidadeUiState> = _uiState.asStateFlow()
    
    init {
        loadComunidadeData()
    }
    
    private fun loadComunidadeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // TODO: Substituir por chamadas ao repositório
                val produtos = listOf(
                    Produto(nome = "Bombinha Portátil", descricao = "Ideal para emergências"),
                    Produto(nome = "Espaçador", descricao = "Facilita a inalação"),
                    Produto(nome = "Nebulizador", descricao = "Para uso doméstico")
                )
                
                val eventos = listOf(
                    Evento(titulo = "Live: Cuidados com Asma", data = "20/08/2025"),
                    Evento(titulo = "Encontro de Pacientes", data = "05/09/2025"),
                    Evento(titulo = "Webinar: DPOC", data = "12/09/2025")
                )
                
                val projetos = listOf(
                    ProjetoInfo(titulo = "Sobre o Afilaxy", texto = "Projeto social para conectar pacientes e voluntários."),
                    ProjetoInfo(titulo = "Missão", texto = "Ajudar pessoas em crise de asma rapidamente."),
                    ProjetoInfo(titulo = "Como funciona?", texto = "Localize ajuda próxima e acesse informações confiáveis.")
                )
                
                _uiState.update { it.copy(
                    produtos = produtos,
                    eventos = eventos,
                    projetos = projetos,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar dados da comunidade: ${e.message}"
                ) }
            }
        }
    }
}