# Plano de Restauração do Afilaxy

## ✅ Já Restaurado
- HomeViewModel (sem Hilt)
- HomeScreen com funcionalidades básicas
- Login funcional com Firebase Auth

## 🔄 Próximos Passos (Ordem de Prioridade)

### 1. Funcionalidades Essenciais
- [ ] EmergencyViewModel (sem Hilt)
- [ ] EmergencyScreen com botão de emergência real
- [ ] Geolocalização básica
- [ ] Sistema de notificações simples

### 2. Funcionalidades Intermediárias  
- [ ] ComunidadeScreen com conteúdo real
- [ ] AutocuidadoScreen com informações
- [ ] Sistema de ajudantes próximos

### 3. Funcionalidades Avançadas
- [ ] Mapas (Google Maps)
- [ ] Room Database (opcional)
- [ ] Hilt/Dagger (se necessário)
- [ ] Workers para background tasks

## 🚫 Removido Temporariamente
- Hilt/Dagger (causava ANR)
- Room Database (dependia do Hilt)
- Workers complexos
- Componentes UI avançados

## 📝 Notas
- Manter sempre a versão sem Hilt funcionando
- Adicionar funcionalidades gradualmente
- Testar build a cada adição
- Priorizar funcionalidades core do app