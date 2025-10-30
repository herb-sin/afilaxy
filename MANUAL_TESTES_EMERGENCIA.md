# 🚨 Manual de Testes - Fluxo de Emergência

## 📋 Checklist de Testes Completos

### 🔧 Preparação
- [ ] App compilado e instalado
- [ ] Permissões de localização concedidas
- [ ] Conexão com internet ativa
- [ ] Firebase configurado corretamente

### 🧪 Testes Automatizados

#### 1. **Acesso aos Testes**
1. Abra o app Afilaxy
2. Na tela inicial, clique em "🧪 Testes de Emergência"
3. Clique em "Executar Testes"

#### 2. **Validação dos Resultados**
Os testes automatizados verificam:

- ✅ **Autenticação Firebase**: Usuário logado
- ✅ **Serviços de Localização**: GPS funcionando
- ✅ **Sistema de Notificações**: FCM inicializado
- ✅ **Fluxo Completo de Emergência**: Envio e processamento
- ✅ **Cancelamento de Emergência**: Funcionalidade de cancelar
- ✅ **Toggle Status Helper**: Alternar status de ajudante

**Meta**: Taxa de sucesso ≥ 80%

### 🎯 Testes Manuais Específicos

#### **Teste 1: Fluxo Completo de Emergência**
1. **Preparação**:
   - Faça login no app
   - Ative as permissões de localização
   - Certifique-se de estar conectado à internet

2. **Execução**:
   - Vá para "Emergência" 
   - Clique em "🆘 SOLICITAR AJUDA"
   - Observe o mapa carregando
   - Verifique se a localização é detectada
   - Confirme o envio da emergência

3. **Validação**:
   - [ ] Mapa carrega corretamente
   - [ ] Localização atual é exibida
   - [ ] Botão de emergência responde
   - [ ] Status muda para "emergência ativa"
   - [ ] Helpers próximos são simulados

#### **Teste 2: Sistema de Notificações**
1. **Preparação**:
   - Ative como "ajudante" na tela inicial
   - Mantenha o app em background

2. **Execução**:
   - Simule uma emergência de outro usuário
   - Verifique se recebe notificação
   - Clique na notificação

3. **Validação**:
   - [ ] Notificação é recebida
   - [ ] Conteúdo da notificação está correto
   - [ ] Clique abre a tela de resposta

#### **Teste 3: Geolocalização Real**
1. **Execução**:
   - Vá para "Emergência"
   - Observe a localização no mapa
   - Mova-se fisicamente (se possível)
   - Clique em "Atualizar Localização"

2. **Validação**:
   - [ ] Localização inicial é precisa
   - [ ] Mapa centraliza na posição correta
   - [ ] Atualização funciona corretamente

#### **Teste 4: Cenários de Erro**
1. **Sem Internet**:
   - Desative WiFi/dados móveis
   - Tente solicitar emergência
   - [ ] App funciona em modo offline
   - [ ] Mensagem de erro apropriada

2. **Sem GPS**:
   - Desative localização do dispositivo
   - Tente solicitar emergência
   - [ ] Usa localização padrão (São Paulo)
   - [ ] Informa sobre limitação

3. **Sem Autenticação**:
   - Faça logout
   - Tente acessar emergência
   - [ ] Redireciona para login
   - [ ] Mantém funcionalidade básica

### 📊 Métricas de Performance

#### **Tempos Esperados**:
- Carregamento do mapa: < 3 segundos
- Obtenção de GPS: < 5 segundos
- Envio de emergência: < 2 segundos
- Inicialização FCM: < 3 segundos

#### **Taxa de Sucesso Esperada**:
- Autenticação: 100%
- GPS: ≥ 90% (com permissões)
- Notificações: ≥ 95%
- Fluxo completo: ≥ 85%

### 🔍 Testes de Stress

#### **Teste de Múltiplas Emergências**
1. Solicite emergência
2. Cancele imediatamente
3. Solicite novamente
4. Repita 5 vezes rapidamente
5. **Validação**: App permanece estável

#### **Teste de Conectividade Intermitente**
1. Inicie emergência com internet
2. Desative internet no meio do processo
3. Reative internet
4. **Validação**: App se recupera graciosamente

### 📱 Testes em Diferentes Dispositivos

#### **Emulador Android**:
- [ ] Funcionalidade básica
- [ ] GPS simulado
- [ ] Notificações

#### **Dispositivo Real**:
- [ ] GPS real
- [ ] Notificações push
- [ ] Performance

### 🚀 Cenários de Produção

#### **Teste Beta**:
1. Instale via Firebase App Distribution
2. Execute todos os testes acima
3. Documente problemas encontrados

#### **Teste de Carga**:
1. Simule múltiplos usuários
2. Teste Firebase Firestore
3. Monitore performance

### 📋 Relatório de Testes

Após executar todos os testes, documente:

- ✅ **Sucessos**: Funcionalidades que passaram
- ❌ **Falhas**: Problemas encontrados
- ⚠️ **Melhorias**: Sugestões de otimização
- 📊 **Métricas**: Tempos e taxas de sucesso

### 🔧 Próximos Passos

Com base nos resultados:
1. **Taxa ≥ 90%**: Pronto para produção
2. **Taxa 70-89%**: Correções menores necessárias
3. **Taxa < 70%**: Revisão significativa necessária

---

**Importante**: Execute os testes em ordem e documente todos os resultados para garantir a qualidade do sistema de emergência.