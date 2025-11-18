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
import java.math.BigDecimal

class ComunidadeViewModel : ViewModel() {
    
    val produtos = listOf(
        Produto(
            id = "1",
            nome = "Salbutamol 100mcg",
            descricao = "Broncodilatador para alívio rápido das crises de asma",
            preco = BigDecimal("12.90"),
            precoOriginal = BigDecimal("18.50"),
            desconto = "30% OFF",
            farmacia = "Drogasil",
            categoria = "Medicamentos",
            cupom = "AFILAXY30",
            validadeCupom = "31/12/2024"
        ),
        Produto(
            id = "2",
            nome = "Espaçador Infantil",
            descricao = "Dispositivo para melhor inalação de medicamentos",
            preco = BigDecimal("25.90"),
            precoOriginal = BigDecimal("35.00"),
            desconto = "25% OFF",
            farmacia = "Droga Raia",
            categoria = "Dispositivos",
            cupom = "AFILAXY25",
            validadeCupom = "31/12/2024"
        ),
        Produto(
            id = "3",
            nome = "Nebulizador Portátil",
            descricao = "Nebulizador ultrassônico para uso domiciliar",
            preco = BigDecimal("89.90"),
            precoOriginal = BigDecimal("129.90"),
            desconto = "30% OFF",
            farmacia = "Pague Menos",
            categoria = "Equipamentos",
            cupom = "AFILAXY30",
            validadeCupom = "31/12/2024"
        ),
        Produto(
            id = "4",
            nome = "Budesonida 200mcg",
            descricao = "Corticoide inalatório para controle preventivo",
            preco = BigDecimal("45.90"),
            precoOriginal = BigDecimal("65.00"),
            desconto = "30% OFF",
            farmacia = "Farmácias São João",
            categoria = "Medicamentos",
            cupom = "AFILAXY30",
            validadeCupom = "31/12/2024"
        ),
        Produto(
            id = "5",
            nome = "Peak Flow Meter",
            descricao = "Medidor de pico de fluxo para monitoramento",
            preco = BigDecimal("35.90"),
            precoOriginal = BigDecimal("49.90"),
            desconto = "28% OFF",
            farmacia = "Ultrafarma",
            categoria = "Monitoramento",
            cupom = "AFILAXY28",
            validadeCupom = "31/12/2024"
        )
    )
    
    val eventos = listOf(
        Evento(
            id = "1",
            titulo = "Workshop: Controle da Asma no Inverno",
            data = "15/12/2024",
            descricao = "Como prevenir crises durante o período mais crítico do ano",
            organizador = "ABRA - Associação Brasileira de Asmáticos",
            local = "Online - Zoom",
            horario = "14h às 16h"
        ),
        Evento(
            id = "2",
            titulo = "Palestra: Asma Grave - Novos Tratamentos",
            data = "20/12/2024",
            descricao = "Avanços na medicina para casos de asma grave e não controlada",
            organizador = "ASBAG - Associação Brasileira de Asmáticos Graves",
            local = "Hospital das Clínicas - SP",
            horario = "19h às 21h"
        ),
        Evento(
            id = "3",
            titulo = "Live: Qualidade do Ar e Saúde Respiratória",
            data = "28/12/2024",
            descricao = "Impacto da poluição na saúde respiratória e dicas de proteção",
            organizador = "Fundação ProAr",
            local = "YouTube e Instagram",
            horario = "20h às 21h30"
        ),
        Evento(
            id = "4",
            titulo = "Encontro: Crônicos do Dia-a-Dia",
            data = "05/01/2025",
            descricao = "Compartilhando experiências e estratégias de convivência com asma",
            organizador = "Crônicos do Dia-a-Dia",
            local = "Centro de Convenções - RJ",
            horario = "9h às 17h"
        ),
        Evento(
            id = "5",
            titulo = "Curso: Técnicas de Inalação Corretas",
            data = "12/01/2025",
            descricao = "Aprenda a usar corretamente bombinhas e espaçadores",
            organizador = "ABRA - Associação Brasileira de Asmáticos",
            local = "Sede ABRA - São Paulo",
            horario = "10h às 12h"
        ),
        Evento(
            id = "6",
            titulo = "Webinar: Asma na Infância",
            data = "18/01/2025",
            descricao = "Orientações para pais e cuidadores de crianças asmáticas",
            organizador = "Fundação ProAr",
            local = "Plataforma Teams",
            horario = "15h às 16h30"
        ),
        Evento(
            id = "7",
            titulo = "Mesa Redonda: Direitos do Paciente Asmático",
            data = "25/01/2025",
            descricao = "Conhecendo seus direitos no SUS e planos de saúde",
            organizador = "ASBAG - Associação Brasileira de Asmáticos Graves",
            local = "Auditório da Faculdade de Medicina - USP",
            horario = "18h às 20h"
        ),
        Evento(
            id = "8",
            titulo = "Grupo de Apoio: Vivendo com Asma",
            data = "02/02/2025",
            descricao = "Encontro mensal para troca de experiências e apoio mútuo",
            organizador = "Crônicos do Dia-a-Dia",
            local = "Centro Comunitário - Belo Horizonte",
            horario = "14h às 16h"
        )
    )
    
    val projetos = listOf(
        ProjetoInfo("1", "Site Afilaxy", ""),
        ProjetoInfo("2", "Asma no SUS", ""),
        ProjetoInfo("3", "Parque Tecnológico de Santos", "")
    )
    
    private fun getCommunityData(): CommunityData {
        return CommunityData(
            produtos = produtos,
            eventos = eventos,
            projetos = projetos
        )
    }
    
    private data class CommunityData(
        val produtos: List<Produto>,
        val eventos: List<Evento>,
        val projetos: List<ProjetoInfo>
    )
}