# 🚨 INCIDENTE DE SEGURANÇA - API KEY EXPOSTA

## Situação
- **Data**: 2024-10-02
- **API Key Comprometida**: [REMOVIDA POR SEGURANÇA]
- **Projeto**: [REMOVIDO POR SEGURANÇA]
- **Exposição**: GitHub público via app-release.aab

## Ações Tomadas
1. ✅ Arquivo AAB removido do repositório
2. ✅ .gitignore atualizado para prevenir futuros vazamentos
3. ✅ API Key revogada no Google Cloud Console
4. ✅ Nova API Key gerada com restrições

## Próximos Passos
1. ✅ API keys movidas para variáveis de ambiente
2. ✅ Configuração segura implementada
3. ✅ Builds protegidos no .gitignore
4. ✅ Monitoramento implementado

## Prevenção
- ✅ Builds (.aab/.apk) agora no .gitignore
- ✅ Diretório release/ protegido
- ✅ Configuração segura implementada
- ✅ API keys em variáveis de ambiente

## Lições Aprendidas
- NUNCA commitar arquivos de build
- Sempre usar variáveis de ambiente para API keys
- Implementar CI/CD para builds seguros
- Revisar regularmente arquivos expostos