package com.afilaxy.presentation.comunidade

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afilaxy.domain.model.Evento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDetailScreen(
    evento: Evento,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header do evento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = evento.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    evento.organizador?.let {
                        Text(
                            text = "Organizado por: $it",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Informações básicas
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📅 Informações do Evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Data
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Data: ${evento.data}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Horário
                    evento.horario?.let { horario ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Horário: $horario",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Local
                    evento.local?.let { local ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local: $local",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Descrição
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 Sobre o Evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    evento.descricao?.let { descricao ->
                        Text(
                            text = descricao,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Informações do organizador
            evento.organizador?.let { organizador ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🏢 Sobre o Organizador",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val infoOrganizador = when {
                            organizador.contains("ABRA") -> """
                                A Associação Brasileira de Asmáticos (ABRA) é uma organização sem fins lucrativos dedicada a melhorar a qualidade de vida de pessoas com asma.
                                
                                🎯 Missão: Educar, apoiar e defender os direitos dos asmáticos brasileiros.
                            """.trimIndent()
                            
                            organizador.contains("ASBAG") -> """
                                A Associação Brasileira de Asmáticos Graves (ASBAG) foca especificamente em casos de asma grave e não controlada.
                                
                                🎯 Missão: Promover acesso a tratamentos avançados e suporte especializado.
                            """.trimIndent()
                            
                            organizador.contains("ProAr") -> """
                                A Fundação ProAr é uma instituição de referência em pesquisa e tratamento de doenças respiratórias.
                                
                                🎯 Missão: Desenvolver pesquisas e oferecer tratamento de excelência.
                            """.trimIndent()
                            
                            organizador.contains("Crônicos") -> """
                                Crônicos do Dia-a-Dia é uma comunidade de apoio para pessoas com doenças crônicas.
                                
                                🎯 Missão: Compartilhar experiências e estratégias de convivência.
                            """.trimIndent()
                            
                            else -> "Organização comprometida com a saúde respiratória e bem-estar dos pacientes."
                        }
                        
                        Text(
                            text = infoOrganizador,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Como participar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎫 Como Participar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val instrucoes = when {
                        evento.local?.contains("Online") == true || evento.local?.contains("YouTube") == true -> """
                            📱 Evento Online:
                            • Acesse o link no horário do evento
                            • Não é necessário inscrição prévia
                            • Participe pelo chat durante a transmissão
                            • Grave suas dúvidas para o momento de perguntas
                        """.trimIndent()
                        
                        evento.local?.contains("Zoom") == true || evento.local?.contains("Teams") == true -> """
                            💻 Evento Virtual:
                            • Inscreva-se previamente para receber o link
                            • Teste sua conexão antes do evento
                            • Mantenha microfone mutado durante as apresentações
                            • Use o chat para fazer perguntas
                        """.trimIndent()
                        
                        else -> """
                            🏢 Evento Presencial:
                            • Confirme sua presença com antecedência
                            • Chegue 15 minutos antes do horário
                            • Traga documento de identificação
                            • Vagas limitadas - garante já a sua!
                        """.trimIndent()
                    }
                    
                    Text(
                        text = instrucoes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { /* TODO: Implementar inscrição */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quero Participar")
                    }
                }
            }
        }
    }
}