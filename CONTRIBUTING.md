# Contribuindo

> Este guia de contribuição foi inspirado no projeto [Brazilian Utils Python](https://github.com/brazilian-utils/python/blob/main/CONTRIBUTING.md)

Obrigado por seu interesse em contribuir com o **Afilaxy**! 🫁

Este projeto tem um impacto social importante, conectando pessoas com asma em situações de emergência através da tecnologia.

## Sumário

- [Código de Conduta](#código-de-conduta)
- [Como posso contribuir?](#como-posso-contribuir)
- [Configuração do ambiente de desenvolvimento](#configuração-do-ambiente-de-desenvolvimento)
- [Executando os testes](#executando-os-testes)
- [Padrões de código](#padrões-de-código)
- [Processo de Pull Request](#processo-de-pull-request)
- [Reportando bugs](#reportando-bugs)
- [Sugerindo melhorias](#sugerindo-melhorias)

## Código de Conduta

Este projeto adere ao [Código de Conduta do Contributor Covenant](https://www.contributor-covenant.org/version/2/1/code_of_conduct/). Ao participar, você deve seguir este código.

## Como posso contribuir?

Existem várias maneiras de contribuir com o Afilaxy:

### 🐛 Reportando bugs

Antes de reportar um bug, verifique se ele já não foi reportado nas [issues existentes](https://github.com/herb-sin/afilaxy/issues).

### ✨ Sugerindo melhorias

Sugestões de melhorias são sempre bem-vindas! Abra uma issue descrevendo sua ideia.

### 💻 Contribuindo com código

- Correções de bugs
- Implementação de novas funcionalidades
- Melhorias na documentação
- Otimizações de performance
- Melhorias de segurança

### 📚 Melhorando a documentação

- Corrigindo erros de digitação
- Melhorando explicações
- Adicionando exemplos
- Traduzindo conteúdo

## Configuração do ambiente de desenvolvimento

### Pré-requisitos

- **Android Studio** Flamingo (2022.3.1) ou superior
- **JDK 17** ou superior
- **Android SDK** versão 34+
- **Git** configurado

### Configuração inicial

1. **Fork** o repositório no GitHub

2. **Clone** seu fork localmente:
   ```bash
   git clone https://github.com/SEU_USUARIO/afilaxy.git
   cd afilaxy
   ```

3. **Configure o remote upstream**:
   ```bash
   git remote add upstream https://github.com/herb-sin/afilaxy.git
   ```

4. **Configure o Firebase** (OBRIGATÓRIO):
   ```bash
   ./setup_secure_config.sh
   ```
   
   ⚠️ **IMPORTANTE**: 
   - Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
   - Ative **Authentication** com método Email/Password
   - Ative **Firestore Database**
   - **NUNCA** commite credenciais reais

5. **Build inicial**:
   ```bash
   ./gradlew build
   ```

### Estrutura do projeto

```
app/src/main/java/com/afilaxy/
├── data/              # Repositórios e fontes de dados
├── domain/            # Modelos de negócio e casos de uso
├── presentation/      # UI e ViewModels
├── security/          # Classes de segurança
└── ui/theme/          # Tema e estilos
```

## Executando os testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes unitários
./gradlew testDebugUnitTest

# Executar verificação de segurança
./security_check.sh

# Build de debug
./gradlew assembleDebug
```

## Padrões de código

### Arquitetura

O projeto segue **Clean Architecture** com padrão **MVVM**:

- **Domain**: Entidades e casos de uso
- **Data**: Repositórios e fontes de dados
- **Presentation**: ViewModels e UI (Jetpack Compose)

### Kotlin

- Use **Kotlin** idiomático
- Prefira **imutabilidade**
- Use **coroutines** para operações assíncronas
- Implemente **injeção de dependência** com Hilt

```kotlin
// ✅ Bom
class EmergencyViewModel @Inject constructor(
    private val repository: EmergencyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    
    fun createEmergency(location: Location) {
        viewModelScope.launch {
            try {
                repository.createEmergency(location)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

// ❌ Ruim
class EmergencyViewModel {
    var error: String? = null
    
    fun createEmergency(location: Location) {
        // Operação síncrona, sem tratamento de erro
    }
}
```

### Jetpack Compose

- Componentes **stateless** quando possível
- Use **preview** para todos os componentes
- Siga **Material Design 3**

```kotlin
// ✅ Bom
@Composable
fun EmergencyButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text("🚨 EMERGÊNCIA")
    }
}

@Preview
@Composable
fun EmergencyButtonPreview() {
    AfilaxyTheme {
        EmergencyButton(onClick = {})
    }
}
```

### Segurança

**SEMPRE** use as classes de segurança existentes:

```kotlin
// ✅ Bom - Com validação de segurança
fun createEmergency(location: Location) {
    if (!AuthGuard.isUserAuthenticated()) {
        throw SecurityException("Authentication required")
    }
    
    val validatedLocation = CentralizedValidator.validateCoordinates(
        location.latitude, 
        location.longitude
    )
    
    if (!validatedLocation.isValid) {
        SecurityMonitor.logSecurityEvent("INVALID_COORDINATES")
        return
    }
}

// ❌ Ruim - Sem validação
fun createEmergency(location: Location) {
    // Usar dados diretamente sem validação
}
```

### Convenções de nomenclatura

- **Classes**: PascalCase (`EmergencyRepository`)
- **Funções**: camelCase (`createEmergency`)
- **Variáveis**: camelCase (`userLocation`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`)

## Processo de Pull Request

### Antes de abrir o PR

1. **Sincronize** com o repositório upstream:
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Crie** uma branch para sua feature:
   ```bash
   git checkout -b feature/nome-da-feature
   ```

3. **Desenvolva** seguindo os padrões

4. **Teste** suas mudanças:
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ./security_check.sh
   ```

5. **Commit** suas mudanças:
   ```bash
   git add .
   git commit -m "feat(emergency): adiciona validação de coordenadas"
   ```

6. **Push** para seu fork:
   ```bash
   git push origin feature/nome-da-feature
   ```

### Template do Pull Request

```markdown
## Descrição

Breve descrição das mudanças realizadas.

## Tipo de mudança

- [ ] Bug fix (mudança que corrige um problema)
- [ ] Nova feature (mudança que adiciona funcionalidade)
- [ ] Breaking change (mudança que quebra compatibilidade)
- [ ] Documentação (mudança apenas na documentação)

## Como foi testado?

Descreva os testes realizados para verificar suas mudanças.

## Checklist

- [ ] Meu código segue os padrões do projeto
- [ ] Realizei uma auto-revisão do código
- [ ] Comentei partes complexas do código
- [ ] Minhas mudanças não geram novos warnings
- [ ] Adicionei testes que provam que minha correção/feature funciona
- [ ] Testes unitários novos e existentes passam
- [ ] Validação de segurança passou
```

### Revisão

Todos os PRs passam por:

1. **Revisão automática** (CI/CD)
2. **Revisão de código** por maintainers
3. **Testes de segurança**
4. **Aprovação** de pelo menos um maintainer

## Reportando bugs

Use o template abaixo para reportar bugs:

```markdown
**Descrição do bug**
Descrição clara e concisa do problema.

**Passos para reproduzir**
1. Vá para '...'
2. Clique em '...'
3. Role para baixo até '...'
4. Veja o erro

**Comportamento esperado**
Descrição clara do que deveria acontecer.

**Screenshots**
Se aplicável, adicione screenshots.

**Ambiente:**
 - Dispositivo: [ex: Pixel 6]
 - OS: [ex: Android 13]
 - Versão do app: [ex: 1.0.0]

**Contexto adicional**
Qualquer outra informação relevante.
```

## Sugerindo melhorias

Use o template abaixo para sugerir melhorias:

```markdown
**A melhoria está relacionada a um problema?**
Descrição clara do problema. Ex: Fico frustrado quando [...]

**Descreva a solução que você gostaria**
Descrição clara da solução desejada.

**Descreva alternativas consideradas**
Descrição de soluções alternativas consideradas.

**Contexto adicional**
Qualquer outra informação relevante.
```

## Convenções de commit

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
tipo(escopo): descrição

[corpo opcional]

[rodapé opcional]
```

### Tipos

- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Documentação
- `style`: Formatação (sem mudança de lógica)
- `refactor`: Refatoração
- `test`: Testes
- `chore`: Manutenção
- `security`: Correções de segurança

### Exemplos

```bash
feat(auth): adiciona autenticação com Firebase
fix(emergency): corrige validação de coordenadas
docs(readme): atualiza instruções de instalação
security(input): adiciona sanitização de dados
```

## Dúvidas?

- 📧 **Email**: afilaxy@gmail.com
- 💬 **Discussions**: [GitHub Discussions](https://github.com/herb-sin/afilaxy/discussions)
- 🐛 **Issues**: [GitHub Issues](https://github.com/herb-sin/afilaxy/issues)

---
