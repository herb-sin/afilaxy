package com.afilaxy.testing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.afilaxy.testing.SimpleEmergencyTest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testResults by remember { mutableStateOf<List<EmergencyFlowTester.TestResult>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚨 Testes de Emergência",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Validação completa do fluxo de emergência",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isRunning = true
                                testResults = emptyList()
                                
                                val tester = EmergencyFlowTester(context)
                                testResults = tester.runCompleteEmergencyTest()
                                
                                isRunning = false
                            }
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Testes Completos")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isRunning = true
                                testResults = emptyList()
                                
                                val simpleTester = SimpleEmergencyTest(context)
                                val simpleResults = simpleTester.runBasicTests()
                                
                                // Converter para o formato esperado
                                testResults = simpleResults.map { result ->
                                    EmergencyFlowTester.TestResult(
                                        testName = result.testName,
                                        success = result.success,
                                        message = result.message,
                                        duration = 0L
                                    )
                                }
                                
                                isRunning = false
                            }
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Testes Básicos")
                    }
                }
                
                if (isRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Executando testes...")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (testResults.isNotEmpty()) {
            TestResultsSummary(testResults)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testResults) { result ->
                    TestResultCard(result)
                }
            }
        }
    }
}

@Composable
private fun TestResultsSummary(results: List<EmergencyFlowTester.TestResult>) {
    val successCount = results.count { it.success }
    val totalCount = results.size
    val successRate = if (totalCount > 0) (successCount * 100) / totalCount else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (successRate >= 80) Color(0xFF4CAF50).copy(alpha = 0.1f)
            else Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Resumo dos Testes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sucessos: $successCount/$totalCount")
                Text("Taxa: $successRate%")
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = successRate / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = if (successRate >= 80) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun TestResultCard(result: EmergencyFlowTester.TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.testName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${result.duration}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}