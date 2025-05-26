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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
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
import kotlinx.coroutines.delay

// Definição das rotas de navegação (boas práticas)
object AppRoutes {
    const val TELA_INICIAL = "tela_inicial"
    const val TELA_EMERGENCIA = "tela_emergencia"
}

data class Helper(
    val id: String,
    val nome: String,
    val distanciaEstimada: String,
    // Poderíamos adicionar mais campos no futuro, como um contato anônimo, etc.
)

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

// Passo 4
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
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var locationTimestamp by remember { mutableStateOf<Long?>(null) }

    // NOVOS ESTADOS para os ajudantes
    var nearbyHelpers by remember { mutableStateOf<List<Helper>>(emptyList()) }
    var noHelpersFound by remember { mutableStateOf(false) } // Para indicar explicitamente que a busca terminou sem resultados

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var helperResponding by remember { mutableStateOf<Helper?>(null) }
    var isAwaitingHelperResponse by remember { mutableStateOf(false) } // Indica que estamos na fase de aguardar um ajudante

    fun simulateNearbyHelpersSearch(currentLocation: Location) {
        println("Simulando busca por ajudantes próximos...")
        val dummyHelpers = listOf(
            Helper(id = "user123", nome = "Ajudante Voluntário A", distanciaEstimada = "aprox. 150m"),
            Helper(id = "user456", nome = "Ajudante Voluntário B", distanciaEstimada = "aprox. 400m"),
            Helper(id = "user789", nome = "Ajudante Voluntário C", distanciaEstimada = "aprox. 750m")
        )
        if (System.currentTimeMillis() % 4 == 0L) {
            nearbyHelpers = emptyList()
            noHelpersFound = true
            isAwaitingHelperResponse = false // Não há ninguém para esperar
        } else {
            nearbyHelpers = dummyHelpers
            noHelpersFound = false
            isAwaitingHelperResponse = true // Helpers encontrados, agora aguardamos resposta
            helperResponding = null // Garante que não há um ajudante respondendo de uma busca anterior
        }
        isLoadingLocation = false // Terminou a busca inicial (localização + lista de ajudantes)
    }

    fun fetchCurrentUserLocation() {
        if (!hasLocationPermission) {
            locationError = "Permissão de localização não concedida."
            return
        }
        isLoadingLocation = true
        locationError = null
        userLocation = null // Limpa localização anterior para indicar nova busca
        nearbyHelpers = emptyList() // Limpa ajudantes anteriores
        noHelpersFound = false // Reseta o estado de "não encontrado"

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            locationError = "Serviços de localização desativados. Por favor, ative o GPS/Localização."
            isLoadingLocation = false
            return
        }
        try {
            val priority = Priority.PRIORITY_HIGH_ACCURACY
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    // isLoadingLocation será definido como false DENTRO de simulateNearbyHelpersSearch
                    if (location != null) {
                        userLocation = location
                        locationTimestamp = System.currentTimeMillis()
                        println("Localização obtida/atualizada: Lat ${location.latitude}, Lon ${location.longitude}")
                        simulateNearbyHelpersSearch(location) // Chama a simulação de busca
                    } else {
                        locationError = "Não foi possível obter/atualizar a localização (resultado nulo)."
                        isLoadingLocation = false
                    }
                }
                .addOnFailureListener { e ->
                    isLoadingLocation = false
                    locationError = "Erro ao obter/atualizar localização: ${e.message}"
                }
        } catch (e: SecurityException) {
            isLoadingLocation = false
            locationError = "Erro de segurança ao obter/atualizar localização."
            hasLocationPermission = false
        } catch (e: Exception) {
            isLoadingLocation = false
            locationError = "Erro inesperado ao obter/atualizar localização."
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                hasLocationPermission = true
                println("Permissão de Localização CONCEDIDA")
                if (userLocation == null) { // Busca apenas se não tiver uma localização ainda
                    fetchCurrentUserLocation()
                }
            } else {
                hasLocationPermission = false
                println("Permissão de Localização NEGADA")
                locationError = "Permissão de localização negada. Funcionalidade indisponível."
            }
        }
    )

// Este LaunchedEffect agora também verificará se a localização é antiga.
    LaunchedEffect(hasLocationPermission) { // Chave ainda é hasLocationPermission
        if (hasLocationPermission) {
            val agora = System.currentTimeMillis()
            val limiteDeTempoMs = 5 * 60 * 1000 // 5 minutos em milissegundos

            val precisaBuscarInicialmente = (userLocation == null)
            val localizacaoEstaAntiga = locationTimestamp?.let { ts ->
                (agora - ts) > limiteDeTempoMs
            } ?: precisaBuscarInicialmente // Se não houver timestamp e não tiver localização, considera "antiga" para forçar a busca inicial. Ou apenas true se userLocation for null.
            // Simplificando: se não tem localização, busca. Se tem, verifica se é antiga.

            if (precisaBuscarInicialmente || localizacaoEstaAntiga) {
                println("Necessário buscar localização: Inicial? $precisaBuscarInicialmente, Antiga? $localizacaoEstaAntiga")
                fetchCurrentUserLocation()
            } else {
                // Localização existe e é recente, não faz nada automaticamente.
                // Garante que o estado de carregamento esteja desativado se não estiver buscando.
                isLoadingLocation = false
                println("Localização existente e recente. Lat ${userLocation!!.latitude}, Lon ${userLocation!!.longitude}")
            }
        } else {
            // Sem permissão, garante que não está carregando.
            isLoadingLocation = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // Alinhado ao topo para a lista
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
                    Text(if (userLocation == null) "Obtendo sua localização..." else "Buscando ajudantes próximos...")
                }
                userLocation != null -> {
                    Text("Sua Localização Atual:", style = MaterialTheme.typography.titleSmall)
                    Text("Lat: ${userLocation!!.latitude}, Lon: ${userLocation!!.longitude}")
                    locationTimestamp?.let { ts ->
                        val minutesAgo = (System.currentTimeMillis() - ts) / 60000
                        Text("Obtida há $minutesAgo minuto(s).", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchCurrentUserLocation() }) {
                        Text("Atualizar Localização e Buscar Novamente")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // EXIBIR AJUDANTES
                    if (nearbyHelpers.isNotEmpty()) {
                        Text("Pessoas próximas que podem ajudar:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) { // weight(1f) para ocupar espaço disponível
                            items(nearbyHelpers) { helper ->
                                HelperItem(helper = helper)
                                Divider()
                            }
                        }
                    } else if (noHelpersFound) {
                        Text("Nenhuma pessoa com bombinha foi encontrada nas proximidades no momento.")
                    }
                    // Se nearbyHelpers estiver vazio e noHelpersFound for false, significa que ainda não buscou ou está buscando
                    // (coberto pelo isLoadingLocation)
                }
                locationError != null -> {
                    Text("Erro: $locationError", color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            hasLocationPermission = true
                            fetchCurrentUserLocation()
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }) {
                        Text("Tentar Novamente / Solicitar Permissão")
                    }
                }
                else -> {
                    Text("Preparando para buscar localização...")
                    Button(onClick = { fetchCurrentUserLocation() }) {
                        Text("Buscar Minha Localização")
                    }
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

        // Botão Voltar movido para o final ou pode ser gerenciado pelo NavController globalmente
        if (!isLoadingLocation) { // Evita sobrepor o botão de voltar com o loading ou a lista
            Spacer(modifier = Modifier.weight(1f, fill = nearbyHelpers.isEmpty() && !noHelpersFound )) // Empurra para baixo se a lista estiver vazia
            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Voltar para Tela Inicial")
            }
        }
    }
}

// 3. Adicione este novo Composable para exibir cada item da lista de ajudantes
@Composable
fun HelperItem(helper: Helper) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp) // Adicionado padding horizontal
    ) {
        Text(helper.nome, style = MaterialTheme.typography.titleMedium) // Estilo um pouco maior
        Spacer(modifier = Modifier.height(4.dp))
        Text("Localização: ${helper.distanciaEstimada}", style = MaterialTheme.typography.bodyMedium)
        // No futuro:
        // Button(onClick = { /* Lógica para contatar ou ver no mapa */ }) { Text("Contatar") }
    }
}


// ... Seus Previews ...
// Atualize ou adicione previews para testar a exibição da lista de ajudantes.
@Preview(showBackground = true, name = "Emergency Screen - Helpers Found")
@Composable
fun PreviewEmergencyScreenHelpersFound() {
    AfilaxyTheme {
        val dummyNavController = rememberNavController()
        // Simulação de dados para o preview
        val dummyHelpers = listOf(
            Helper(id = "1", nome = "Ajudante Preview 1", distanciaEstimada = "100m"),
            Helper(id = "2", nome = "Ajudante Preview 2", distanciaEstimada = "300m")
        )
        // Esta é uma forma simplificada de mostrar o estado no preview.
        // Você pode criar uma `EmergencyScreenContent` que recebe todos os estados como parâmetros.
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sua Localização Atual:", style = MaterialTheme.typography.titleSmall)
            Text("Lat: -23.5505, Lon: -46.6333")
            Text("Obtida há 0 minuto(s).", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }) { Text("Atualizar Localização e Buscar Novamente") }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Pessoas próximas que podem ajudar:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(dummyHelpers) { helper ->
                    HelperItem(helper = helper)
                    Divider()
                }
            }
            Spacer(modifier = Modifier.weight(1f, fill = false))
            Button(onClick = { /* dummy */ }) { Text("Voltar para Tela Inicial") }
        }
    }
}

@Preview(showBackground = true, name = "Emergency Screen - No Helpers Found")
@Composable
fun PreviewEmergencyScreenNoHelpers() {
    AfilaxyTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
            // ... outros elementos ...
            Spacer(modifier = Modifier.height(24.dp))
            Text("Nenhuma pessoa com bombinha foi encontrada nas proximidades no momento.")
            // ...
            Spacer(modifier = Modifier.weight(1f, fill = true))
            Button(onClick = { /* dummy */ }) { Text("Voltar para Tela Inicial") }
        }
    }
}

// ... Seus Previews ...
// Mantenha os Previews ou atualize-os para refletir os novos estados, se desejar.
// Exemplo de Preview para estado com localização e botão de atualizar:
@Preview(showBackground = true, name = "Emergency Screen - Local Obtido com Refresh")
@Composable
fun PreviewEmergencyScreenLocationFoundWithRefresh() {
    AfilaxyTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sua Localização Atual:")
            Text("Latitude: -23.5505")
            Text("Longitude: -46.6333")
            Text("Obtida há 0 minuto(s).", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* dummy */ }) { Text("Atualizar Localização") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Buscando ajuda próxima (simulação)...")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { /* dummy */ }) { Text("Voltar para Tela Inicial") }
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