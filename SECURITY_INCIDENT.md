# 🚨 INCIDENTE DE SEGURANÇA - API KEY EXPOSTA

## Situação
- **Data**: 2024-10-02
- **API Key Comprometida**: AIzaSyC_dm1Gc79BopDD6ytVeJDEJEIihIps-7o
- **Projeto**: dazzling-bruin-425416-j0
- **Exposição**: GitHub público via app-release.aab

## Ações Tomadas
1. ✅ Arquivo AAB removido do repositório
2. ✅ .gitignore atualizado para prevenir futuros vazamentos
3. ⏳ API Key deve ser revogada no Google Cloud Console
4. ⏳ Nova API Key deve ser gerada com restrições

## Próximos Passos
1. **URGENTE**: Revogar API key no Google Cloud Console
2. Gerar nova API key com restrições de IP/domínio
3. Atualizar local.properties com nova key
4. Recompilar app com nova configuração
5. Monitorar uso da API por atividade suspeita

## Prevenção
- ✅ Builds (.aab/.apk) agora no .gitignore
- ✅ Diretório release/ protegido
- ✅ Configuração segura implementada

## Lições Aprendidas
- NUNCA commitar arquivos de build
- Sempre usar variáveis de ambiente para API keys
- Implementar CI/CD para builds seguros