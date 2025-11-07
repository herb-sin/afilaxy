# Debug Suggestions

## 1. Verificar se há helpers no banco
- Acesse o Firebase Console
- Vá em Firestore Database
- Verifique a coleção "helpers"

## 2. Aumentar raio de busca temporariamente
```kotlin
// Em HelperRepository, mude de 0.26km para 5km para teste
val radiusInKm = 5.0 // era 0.26
```

## 3. Adicionar logs na query
```kotlin
Log.d("HelperRepository", "Query executada com sucesso, documentos: ${querySnapshot.size()}")
querySnapshot.documents.forEach { doc ->
    Log.d("HelperRepository", "Helper encontrado: ${doc.id}")
}
```

## 4. Testar com dados mock
- Cadastre manualmente um helper no Firestore
- Use coordenadas próximas para teste