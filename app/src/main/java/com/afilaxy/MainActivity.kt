package com.afilaxy

import android.Manifest // permissões
import android.content.Context // getSystemService e outros usos de contexto
import android.content.pm.PackageManager // verificar permissões
import android.location.Location // objeto Location
import android.location.LocationManager // verificar se a localização está ativa
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult // pedir permissão
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // pedir permissão
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator // Para indicador de carregamento
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // Para verificar permissão
import androidx.core.location.LocationManagerCompat // Para LocationManagerCompat.isLocationEnabled
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TextField
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
    const val TELA_AUTOCUIDADO = "tela_autocuidado"

    const val TELA_PREPARADOR_CONSULTA = "tela_preparador_consulta"
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
                    // Define a tela de Autocuidado
                    composable(AppRoutes.TELA_AUTOCUIDADO) {
                        TelaAutocuidadoScreen(
                            navController = navController,
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
                navController.navigate(AppRoutes.TELA_AUTOCUIDADO)
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
    var nearbyHelpers by remember { mutableStateOf<List<Helper>>(emptyList()) }
    var noHelpersFound by remember { mutableStateOf(false) }

    // NOVOS ESTADOS para gerenciar a resposta do ajudante
    var helperResponding by remember { mutableStateOf<Helper?>(null) }
    var isAwaitingHelperResponse by remember { mutableStateOf(false) } // Indica que estamos na fase de aguardar um ajudante
    var showEmergencyInstructions by remember { mutableStateOf(false) }

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun simulateNearbyHelpersSearch(currentLocation: Location) {
        println("Simulando busca por ajudantes próximos...")
        val dummyHelpers = listOf(
            Helper(id = "user123", nome = "Ajudante Voluntário A", distanciaEstimada = "aprox. 150m"),
            Helper(id = "user456", nome = "Ajudante Voluntário B", distanciaEstimada = "aprox. 400m"),
            Helper(id = "user789", nome = "Ajudante Voluntário C", distanciaEstimada = "aprox. 750m")
        )
        if (System.currentTimeMillis() % 4 == 0L) { // Para variar os resultados
            nearbyHelpers = emptyList()
            noHelpersFound = true
            isAwaitingHelperResponse = false
        } else {
            nearbyHelpers = dummyHelpers
            noHelpersFound = false
            isAwaitingHelperResponse = true // Helpers encontrados, agora aguardamos resposta
            helperResponding = null
        }
        isLoadingLocation = false
    }

    fun fetchCurrentUserLocation() {
        if (!hasLocationPermission) {
            locationError = "Permissão de localização não concedida."
            return
        }
        isLoadingLocation = true
        locationError = null
        userLocation = null
        nearbyHelpers = emptyList()
        noHelpersFound = false
        isAwaitingHelperResponse = false // Reseta ao iniciar nova busca
        helperResponding = null      // Reseta ao iniciar nova busca

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
                    if (location != null) {
                        userLocation = location
                        locationTimestamp = System.currentTimeMillis()
                        println("Localização obtida/atualizada: Lat ${location.latitude}, Lon ${location.longitude}")
                        simulateNearbyHelpersSearch(location)
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
                if (userLocation == null) {
                    fetchCurrentUserLocation()
                }
            } else {
                hasLocationPermission = false
                println("Permissão de Localização NEGADA")
                locationError = "Permissão de localização negada. Funcionalidade indisponível."
            }
        }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val agora = System.currentTimeMillis()
            val limiteDeTempoMs = 5 * 60 * 1000 // 5 minutos

            val precisaBuscarInicialmente = (userLocation == null)
            val localizacaoEstaAntiga = locationTimestamp?.let { ts ->
                (agora - ts) > limiteDeTempoMs
            } ?: precisaBuscarInicialmente

            if (precisaBuscarInicialmente || localizacaoEstaAntiga) {
                println("Necessário buscar localização: Inicial? $precisaBuscarInicialmente, Antiga? $localizacaoEstaAntiga")
                fetchCurrentUserLocation()
            } else {
                isLoadingLocation = false
                println("Localização existente e recente. Lat ${userLocation!!.latitude}, Lon ${userLocation!!.longitude}")
            }
        } else {
            isLoadingLocation = false
        }
    }

    // NOVO LaunchedEffect para simular um ajudante respondendo
    LaunchedEffect(isAwaitingHelperResponse, nearbyHelpers) {
        if (isAwaitingHelperResponse && nearbyHelpers.isNotEmpty()) {
            println("Aguardando um ajudante aceitar o pedido...")
            delay(10000L) // Simula um atraso de 10 segundos

            if (isAwaitingHelperResponse && helperResponding == null && nearbyHelpers.isNotEmpty()) {
                val respondingHelperFromList = nearbyHelpers.random()
                helperResponding = respondingHelperFromList
                isAwaitingHelperResponse = false
                println("${respondingHelperFromList.nome} aceitou o pedido.")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tela de Emergência", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (hasLocationPermission) {
            when {
                isLoadingLocation -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (userLocation == null && nearbyHelpers.isEmpty()) "Obtendo sua localização..." else "Buscando ajudantes próximos...")
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

                    // LÓGICA DE EXIBIÇÃO ATUALIZADA
                    if (helperResponding != null) {
                        Text(
                            "${helperResponding!!.nome} está a caminho!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Localização: ${helperResponding!!.distanciaEstimada}")
                    } else if (nearbyHelpers.isNotEmpty()) {
                        Text("Possíveis ajudantes próximos:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                            items(nearbyHelpers) { helper ->
                                HelperItem(helper = helper)
                                Divider()
                            }
                        }
                        if (isAwaitingHelperResponse) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Seu pedido foi enviado. Aguardando um ajudante confirmar...")
                        }
                    } else if (noHelpersFound) {
                        Text("Nenhuma pessoa com bombinha foi encontrada nas proximidades no momento.")
                    }
                    // NOVO BOTÃO AQUI
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEmergencyInstructions = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MOSTRAR INSTRUÇÕES PARA AJUDANTE")
                    }
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

        // Botão Voltar
        val showSpacerBeforeBackButton = when {
            isLoadingLocation -> false
            isAwaitingHelperResponse -> false
            helperResponding != null -> true
            nearbyHelpers.isEmpty() && !noHelpersFound && userLocation != null -> true // Se tem localização mas ainda não achou/buscou helpers
            else -> nearbyHelpers.isEmpty() && !noHelpersFound
        }

        if (showSpacerBeforeBackButton) {
            Spacer(modifier = Modifier.weight(1f))
        }


        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Voltar para Tela Inicial")
        }
    }

    if (showEmergencyInstructions) {
        EmergencyInstructionsDialog(
            userName = "Usuário Afilaxy", // Simulado por enquanto
            userLocation = userLocation,
            isLoadingLocation = isLoadingLocation,
            isAwaitingHelperResponse = isAwaitingHelperResponse,
            helperResponding = helperResponding,
            noHelpersFound = noHelpersFound,
            locationError = locationError,
            onDismiss = { showEmergencyInstructions = false }
        )
    }
}
// Após TelaInicialAfilaxy ou EmergencyScreen
@Composable
fun TelaAutocuidadoScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: PreparadorConsultaViewModel = viewModel()
    var pergunta by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Assistente de Autocuidado",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Digite sua dúvida sobre Asma ou DPOC e receba orientações seguras.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = pergunta,
            onValueChange = { pergunta = it },
            label = { Text("Pergunte sobre Asma ou DPOC") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.prepararResumoConsulta(pergunta) },
            enabled = pergunta.isNotBlank()
        ) {
            Text("Perguntar")
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (uiState) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> Text((uiState as UiState.Success).resumo)
            is UiState.Error -> Text((uiState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar para Tela Inicial")
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

// Passo 4.5: Novo Composable para caixa de diálogo da mensagem de ajuda transeunte
// Coloque esta função no mesmo nível que EmergencyScreen, TelaInicialAfilaxy, etc.

@Composable
fun EmergencyInstructionsDialog(
    userName: String,
    userLocation: Location?,
    isLoadingLocation: Boolean,
    isAwaitingHelperResponse: Boolean,
    helperResponding: Helper?,
    noHelpersFound: Boolean,
    locationError: String?,
    onDismiss: () -> Unit
) {
    val appStatusMessage = when {
        isLoadingLocation && userLocation == null -> "O aplicativo Afilaxy está buscando a localização do paciente."
        isAwaitingHelperResponse && helperResponding == null -> "O aplicativo Afilaxy encontrou possíveis ajudantes e está aguardando a confirmação de quem trará a bombinha de Asma."
        helperResponding != null -> "O aplicativo Afilaxy confirmou! ${helperResponding.nome} está trazendo a bombinha e está a ${helperResponding.distanciaEstimada}."
        noHelpersFound -> "O aplicativo Afilaxy buscou, mas não encontrou ajudantes com bombinha nas proximidades no momento."
        locationError != null -> "O aplicativo Afilaxy está com dificuldades técnicas: $locationError. Tente verificar as permissões e o GPS."
        userLocation == null && !isLoadingLocation -> "O aplicativo Afilaxy aguarda o início da busca por localização."
        else -> "Verificando status do aplicativo Afilaxy..."
    }

    val locationString = userLocation?.let {
        "Localização Atual (aproximada): Latitude ${"%.4f".format(it.latitude)}, Longitude ${"%.4f".format(it.longitude)}"
    } ?: "Localização ainda não obtida pelo aplicativo."

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "!!! EMERGÊNCIA DE ASMA !!!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            // --- INÍCIO DA ALTERAÇÃO ---

            // 1. Crie o estado de rolagem para a coluna de texto
            val scrollState = rememberScrollState()

            Column(
                // 2. Adicione este modificador para tornar a coluna rolável
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Esta pessoa está tendo uma crise de Asma e precisa de ajuda URGENTE.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "1. LIGUE IMEDIATAMENTE PARA O SAMU (192).",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "2. Informe ao SAMU:",
                    style = MaterialTheme.typography.titleSmall
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text("Nome do Paciente: $userName")
                    Text(locationString)
                }
                Text(
                    "3. Status do Aplicativo Afilaxy:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(appStatusMessage, modifier = Modifier.padding(start = 16.dp))
                Text(
                    "4. Permaneça com o paciente e aguarde o socorro do SAMU e do ajudante com a bombinha (se um for confirmado pelo app).",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Mantenha a calma e ofereça conforto.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // --- FIM DA ALTERAÇÃO ---
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("FECHAR MENSAGEM")
            }
        }
    )
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