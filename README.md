# Afilaxy

Afilaxy é um aplicativo Android desenvolvido em Kotlin com Jetpack Compose, integrado ao Firebase para autenticação, gerenciamento de usuários e funcionalidades de localização. O app tem como objetivo conectar pessoas e facilitar pedidos de ajuda entre usuários próximos. Seu desenvolvimento possui auxílio total do Copilot.

## Funcionalidades

- **Autenticação Firebase:** Cadastro, login, verificação de e-mail, recuperação de senha.
- **Interface moderna:** Desenvolvida com Jetpack Compose, responsiva e intuitiva.
- **Geolocalização:** (Em desenvolvimento) Permite identificar e conectar usuários próximos.
- **Notificações:** (Em desenvolvimento) Envio de alertas/pedidos de ajuda via Firebase Cloud Messaging.
- **Validação de campos:** Mensagens de erro em português e feedback visual.
- **Design customizado:** Logotipo, cores e botões adaptados para melhor experiência.

## Como rodar o projeto

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/afilaxy.git
   ```

2. **Abra no Android Studio.**

3. **Configure o Firebase:**
   - Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
   - Ative o método de autenticação por e-mail/senha.
   - Baixe o arquivo `google-services.json` e coloque em `app/`.

4. **Adicione o logotipo:**
   - Coloque o arquivo `afilaxy_logo.png` em `app/src/main/res/drawable/`.

5. **Sincronize e rode o app.**

## Estrutura de pastas

```
app/
 └─ src/
     └─ main/
         ├─ java/com/afilaxy/
         │   ├─ MainActivity.kt
         │   ├─ ui/
         │   │   └─ LoginScreen.kt
         └─ res/
             └─ drawable/
                 └─ afilaxy_logo.png
```

## Tecnologias utilizadas

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore (em breve)
- Firebase Cloud Messaging (em breve)

## Contribuição

Contribuições são bem-vindas!  
Abra uma issue ou envie um pull request.

## Licença

Este projeto está sob a licença MIT.

---
