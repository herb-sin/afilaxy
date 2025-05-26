package com.afilaxy // Ou o nome do seu pacote

import android.Manifest // Para permissões
import android.content.Context // Para getSystemService e outros usos de contexto
import android.content.pm.PackageManager // Para verificar permissões
import android.location.Location // Para o objeto Location
import android.location.LocationManager // Para verificar se a localização está ativa
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult // Para pedir permissão
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // Para pedir permissão
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator // Para indicador de carregamento
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Para executar lógica ao compor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Para obter o Context
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // Para verificar permissão
import androidx.core.location.LocationManagerCompat // Para LocationManagerCompat.isLocationEnabled
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afilaxy.ui.theme.AfilaxyTheme // Seu tema
import com.google.android.gms.location.FusedLocationProviderClient // Cliente de localização
import com.google.android.gms.location.LocationServices // Para obter o cliente
import com.google.android.gms.location.Priority // Para definir a prioridade da localização
import com.google.android.gms.tasks.CancellationTokenSource // Para cancelar a requisição

// Definição das rotas de navegação (boas práticas)
object AppRoutes {
    const val TELA_INICIAL = "tela_inicial"
    const val TELA_EMERGENCIA = "tela_emergencia"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfilaxyTheme {
                val navController = rememberNavController() // Cria o controlador de navegação

                NavHost(navController = navController, startDestination = AppRoutes.TELA_INICIAL) {
                    // Define a tela inicial
                    composable(AppRoutes.TELA_INICIAL) {
                        TelaInicialAfilaxy(
                            navController = navController, // Passa o navController para a tela inicial
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Define a tela de emergência
                    composable(AppRoutes.TELA_EMERGENCIA) {
                        EmergencyScreen(
                            navController = navController, // Passa o navController também
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Aqui você pode adicionar outras telas no futuro
                    // composable("outra_tela") { OutraTela(navController) }
                }
            }
        }
    }
}

// Passo 3: Modificar TelaInicialAfilaxy para aceitar e usar o NavController
@Composable
fun TelaInicialAfilaxy(navController: NavController, modifier: Modifier = Modifier) { // Adicionado navController
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem-vindo(a) ao Afilaxy!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sua comunidade de apoio e autocuidado para Asma.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                // AGORA NAVEGA PARA A TELA DE EMERGÊNCIA
                navController.navigate(AppRoutes.TELA_EMERGENCIA)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Emergência: Localizar Bombinha")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                println("Botão 'Acessar Comunidade' clicado!")
                // navController.navigate("rota_comunidade") // Exemplo para o futuro
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Acessar Comunidade")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                println("Botão 'Informações e Autocuidado' clicado!")
                // navController.navigate("rota_informacoes") // Exemplo para o futuro
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Informações e Autocuidado")
        }
    }
}

// Passo 4: Criar a nova tela EmergencyScreen (Composable)
@Composable
fun EmergencyScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Estado para a localização do usuário
    var userLocation by remember { mutableStateOf<Location?>(null) }
    // Estado para indicar se estamos carregando a localização
    var isLoadingLocation by remember { mutableStateOf(false) }
    // Estado para mensagens de erro relacionadas à localização
    var locationError by remember { mutableStateOf<String?>(null) }

    // Cliente para obter a localização
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                hasLocationPermission = true
                println("Permissão de Localização CONCEDIDA")
            } else {
                hasLocationPermission = false
                println("Permissão de Localização NEGADA")
                locationError = "Permissão de localização negada. Funcionalidade indisponível."
            }
        }
    )

    // Este LaunchedEffect tentará obter a localização quando a permissão for concedida
    // e ainda não tivermos uma localização.
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            isLoadingLocation = true
            locationError = null // Limpa erros anteriores

            // Verifica se os serviços de localização estão ativos no dispositivo
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                locationError = "Serviços de localização desativados. Por favor, ative o GPS/Localização."
                isLoadingLocation = false
                return@LaunchedEffect // Sai do LaunchedEffect
            }

            try {
                val priority = Priority.PRIORITY_HIGH_ACCURACY
                val cancellationTokenSource = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                    .addOnSuccessListener { location: Location? ->
                        isLoadingLocation = false
                        if (location != null) {
                            userLocation = location
                            println("Localização obtida: Lat ${location.latitude}, Lon ${location.longitude}")
                        } else {
                            locationError = "Não foi possível obter a localização atual (resultado nulo)."
                            println("Não foi possível obter a localização atual (location is null).")
                            // Poderia tentar lastLocation como fallback aqui, se desejado
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoadingLocation = false
                        locationError = "Erro ao obter localização: ${e.message}"
                        println("Erro ao obter localização: ${e.message}")
                    }
            } catch (e: SecurityException) {
                // Isso não deveria acontecer se hasLocationPermission é true
                isLoadingLocation = false
                locationError = "Erro de segurança ao obter localização: Verifique as permissões."
                println("SecurityException ao obter localização: ${e.message}")
                // Re-verificar a permissão explicitamente, ou guiar o usuário para as configurações
                hasLocationPermission = false // Força a UI a mostrar o pedido de permissão novamente
            } catch (e: Exception) {
                isLoadingLocation = false
                locationError = "Erro inesperado ao obter localização."
                println("Erro inesperado ao obter localização: ${e.message}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Tela de Emergência",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (hasLocationPermission) {
            when {
                isLoadingLocation -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Obtendo sua localização...")
                }
                userLocation != null -> {
                    Text("Sua Localização Atual:")
                    Text("Latitude: ${userLocation!!.latitude}")
                    Text("Longitude: ${userLocation!!.longitude}")
                    // Aqui viria a lógica para "buscar ajuda próxima" usando essa localização
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Buscando ajuda próxima (simulação)...")
                }
                locationError != null -> {
                    Text("Erro: $locationError", color = MaterialTheme.colorScheme.error)
                    // Botão para tentar novamente, se apropriado
                    Button(onClick = {
                        // Força a re-execução do LaunchedEffect limpando o erro e mantendo isLoadingLocation false
                        // Ou, melhor, refatorar a lógica de busca para uma função que pode ser chamada.
                        // Por ora, simples:
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            hasLocationPermission = true // Garante que o LaunchedEffect possa tentar de novo se a permissão ainda estiver lá.
                            userLocation = null // Limpa para tentar de novo
                            isLoadingLocation = false // Reseta o loading para permitir nova tentativa no LaunchedEffect
                            locationError = null // Limpa o erro para permitir nova tentativa
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }) {
                        Text("Tentar Novamente / Solicitar Permissão")
                    }
                }
                else -> {
                    // Estado inicial após permissão concedida, antes de iniciar o carregamento
                    // ou se a localização não pôde ser determinada sem erro explícito.
                    Text("Preparando para buscar localização...")
                }
            }
        } else { // Se não tem permissão
            Text(
                locationError ?: "Para localizar ajuda próxima, o Afilaxy precisa da sua permissão para acessar a localização do dispositivo.",
                textAlign = TextAlign.Center,
                color = if (locationError != null && locationError!!.contains("negada")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("Solicitar Permissão de Localização")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Voltar para Tela Inicial")
        }
    }
}

// Passo 5: Atualizar o Preview da TelaInicialAfilaxy (opcional, mas bom para o preview)
@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewTelaInicialAfilaxy() {
    AfilaxyTheme {
        // Para o preview funcionar, precisamos de um NavController "falso"
        // ou não passar um NavController real que dependa de um NavHost.
        // A forma mais simples é criar uma versão do Composable que não exige NavController para o Preview,
        // ou passar um NavController vazio que não fará nada no Preview.
        // Por simplicidade aqui, vamos assumir que o preview pode quebrar ou você pode criar
        // um NavController de preview: val previewNavController = rememberNavController()
        // e chamaria TelaInicialAfilaxy(navController = previewNavController)
        // OU, mais simples para agora, o preview pode não refletir a navegação.
        // Para este exemplo, vamos focar no app rodando.
        // Se o preview quebrar, você pode comentar o parâmetro navController na TelaInicialAfilaxy
        // temporariamente para o preview ou criar um NavController fake.

        // Alternativa para Preview:
        val dummyNavController = rememberNavController()
        TelaInicialAfilaxy(navController = dummyNavController, modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, name = "Emergency Screen - Sem Permissão")
@Composable
fun PreviewEmergencyScreenNoPermission() {
    AfilaxyTheme {
        val dummyNavController = rememberNavController()
        // Para simular esse estado, você precisaria de uma função de conteúdo separada
        // ou passar `hasLocationPermission` como um parâmetro para EmergencyScreen no Preview.
        // Por simplicidade, o preview real mostrará o estado inicial.
        EmergencyScreen(navController = dummyNavController)
    }
}

@Preview(showBackground = true, name = "Emergency Screen - Carregando")
@Composable
fun PreviewEmergencyScreenLoading() {
    AfilaxyTheme {
        // Simulação do estado de carregamento
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Obtendo sua localização...")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { /* dummy */ }) { Text("Voltar para Tela Inicial") }
        }
    }
}

@Preview(showBackground = true, name = "Emergency Screen - Local Obtido")
@Composable
fun PreviewEmergencyScreenLocationFound() {
    AfilaxyTheme {
        // Simulação do estado com localização obtida
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sua Localização Atual:")
            Text("Latitude: -23.5505")
            Text("Longitude: -46.6333")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Buscando ajuda próxima (simulação)...")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { /* dummy */ }) { Text("Voltar para Tela Inicial") }
        }
    }
}

// Adicionar um preview para a EmergencyScreen também é uma boa ideia
@Preview(showBackground = true)
@Composable
fun PreviewEmergencyScreen() {
    AfilaxyTheme {
        val dummyNavController = rememberNavController()
        EmergencyScreen(navController = dummyNavController, modifier = Modifier.fillMaxSize())
    }
}