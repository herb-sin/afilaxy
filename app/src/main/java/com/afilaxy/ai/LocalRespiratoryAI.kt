package com.afilaxy.ai

import com.afilaxy.security.InputSanitizer

class LocalRespiratoryAI {
    
    // Contexto do projeto Afilaxy
    private val afilaxyContext = """
    O Afilaxy é um app que conecta pessoas com asma em situações de emergência.
    Nossa missão: transformar pacientes em agentes de saúde, criando uma rede solidária
    onde a própria comunidade se ajuda. Levamos boas práticas médicas para fora
    do ambiente clínico, empoderando pessoas no controle de síndromes respiratórias.
    """
    
    // Contexto médico sobre asma
    private val asthmaFacts = "A asma NÃO tem cura, mas tem tratamento eficaz disponibilizado gratuitamente pelo SUS. O controle adequado permite vida normal."
    private val susTreatment = "SUS oferece: medicamentos controladores, broncodilatadores, acompanhamento médico, educação em saúde - TUDO GRATUITO!"
    private val susVariations = "ATENÇÃO: Cada cidade/estado pode ter exigências diferentes para acesso aos medicamentos. Alguns exigem receita especializada, outros aceitam clínico geral."
    
    private val symptoms = listOf("tosse", "chiado", "falta de ar", "aperto", "cansaço", "respiração", "sufoco", "ofego")
    private val medications = listOf("bombinha", "inalador", "salbutamol", "corticoide", "broncodilatador", "remédio", "medicação")
    private val triggers = listOf("poeira", "ácaro", "pelo", "fumaça", "cigarro", "poluição", "perfume", "frio", "calor", "umidade", "seco")
    private val foodTriggers = listOf("leite", "ovo", "amendoim", "nozes", "peixe", "camarão", "soja", "trigo", "chocolate", "corante", "conservante", "sulfito")
    private val foodIndicators = listOf("comer", "beber", "tomar", "ingerir", "consumir", "alimento", "comida", "bebida")
    private val asthmaKeywords = listOf("asma", "sintomas", "crise", "respirar", "respiração", "pulmão", "pulmões")
    private val effectKeywords = listOf("amenizar", "melhorar", "piorar", "ajudar", "prejudicar", "afetar", "influenciar", "causar", "desencadear")
    private val animals = listOf("cachorro", "gato", "gata", "gatos", "pássaro", "hamster", "coelho", "jabuti", "tartaruga", "peixe", "réptil", "animal", "pet", "bichinho")
    private val transmissionKeywords = listOf("transmitir", "passar", "contagiar", "pegar", "contaminar", "infectar")
    private val hydrationKeywords = listOf("água", "hidratar", "hidratação", "beber água", "líquido")
    private val misinformationPatterns = listOf(
        "debaixo da cama", "embaixo da cama", "sob a cama",
        "amuleto", "sorte", "energia", "vibração",
        "cura milagrosa", "remédio caseiro", "receita da vó",
        "substitui medicação", "não precisa de médico",
        "tem cura", "pode curar", "cura definitiva",
        "pegou asma", "pegar asma", "pisar", "prego", "ferimento", "machucou", "cortou", "acidente"
    )
    private val injuryKeywords = listOf("prego", "ferimento", "machucou", "cortou", "acidente", "pisou", "pisar")
    private val contagionKeywords = listOf("pegou", "pegar", "contraiu", "adquiriu")
    private val temperatureKeywords = listOf("banho quente", "geladeira", "frio", "calor", "temperatura", "choque térmico")
    private val temperatureShock = listOf("banho quente", "geladeira", "ar condicionado", "freezer")
    private val cureQuestions = listOf("cura", "curar", "tem cura", "pode curar", "cura asma")
    private val treatmentQuestions = listOf("tratamento", "medicamento", "remédio", "sus", "gratuito", "público")
    private val accessQuestions = listOf("como conseguir", "onde pegar", "exigência", "documento", "receita", "especialista")
    private val childrenKeywords = listOf("criança", "crianças", "bebê", "filho", "filha")
    private val communityKeywords = listOf("ajudar", "comunidade", "solidário", "compartilhar", "dividir", "emprestar", "afilaxy")
    private val emergencyHelp = listOf("sem bombinha", "esqueci bombinha", "acabou bombinha", "não tenho", "preciso de ajuda")
    private val activities = listOf("exercício", "corrida", "natação", "caminhada", "esporte", "surfando", "surf", "futebol", "vôlei", "basquete", "dança", "academia", "treino", "atividade")
    private val emergency = listOf("crise", "emergência", "socorro", "urgente", "grave", "atacar", "ataque")
    
    // Palavras que indicam atividade física mesmo se não estão na lista
    private val activityIndicators = listOf("fazendo", "praticando", "jogando", "durante", "enquanto", "pode", "posso")
    
    fun getAsthmaInfo(question: String): String {
        val sanitizedQuestion = InputSanitizer.sanitizeText(question)
        if (sanitizedQuestion.isBlank()) {
            return "Por favor, faça uma pergunta válida sobre asma."
        }
        val input = sanitizedQuestion.lowercase().trim()
        
        // Análise inteligente de contexto
        val isEmergency = containsAny(input, emergency)
        val hasSymptoms = containsAny(input, symptoms)
        val hasMedications = containsAny(input, medications)
        val hasTriggers = containsAny(input, triggers)
        val hasTemperatureShock = containsAny(input, temperatureShock)
        val hasFoodTriggers = (containsAny(input, foodTriggers) || containsAny(input, foodIndicators)) && !hasTemperatureShock
        val hasActivities = containsAny(input, activities) || containsAny(input, activityIndicators)
        val isPrevention = input.contains("prevenir") || input.contains("evitar")
        val isMedical = input.contains("médico") || input.contains("consulta")
        val isDailyLife = input.contains("trabalho") || input.contains("escola")
        val hasAsthmaContext = containsAny(input, asthmaKeywords)
        val hasEffectContext = containsAny(input, effectKeywords)
        val hasAnimals = containsAny(input, animals)
        val hasTransmission = containsAny(input, transmissionKeywords)
        val isAnimalTransmission = hasAnimals && hasTransmission
        val hasHydration = containsAny(input, hydrationKeywords)
        val hasMisinformation = containsAny(input, misinformationPatterns)
        val hasInjury = containsAny(input, injuryKeywords)
        val hasContagion = containsAny(input, contagionKeywords)
        val isInjuryMisconception = hasInjury && hasContagion && hasAsthmaContext
        val isTemperatureMisconception = hasTemperatureShock && hasContagion && hasAsthmaContext
        val hasChildren = containsAny(input, childrenKeywords)
        val hasCommunity = containsAny(input, communityKeywords)
        val hasEmergencyHelp = containsAny(input, emergencyHelp)
        val isAsthmaRelated = hasAsthmaContext || hasEffectContext
        
        val hasCureQuestions = containsAny(input, cureQuestions)
        val hasTreatmentQuestions = containsAny(input, treatmentQuestions)
        val hasAccessQuestions = containsAny(input, accessQuestions)
        
        // Lógica inteligente: combina contextos
        return try {
            when {
                isEmergency -> handleEmergency(input)
                hasCureQuestions -> handleCureResponse()
                hasTreatmentQuestions || hasAccessQuestions -> handleTreatmentResponse()
                isAnimalTransmission -> handleAnimalTransmission(input)
                hasAnimals && isAsthmaRelated -> handleAnimalsAndAsthma(input)
                hasActivities && hasSymptoms -> handleActivityWithSymptoms(input)
                hasActivities -> handleActivities(input)
                hasSymptoms && hasFoodTriggers -> handleFoodSymptoms(input)
                hasSymptoms && hasTriggers -> handleSymptomsWithTriggers(input)
                hasSymptoms -> handleSymptoms(input)
                hasMedications -> handleMedications(input)
                hasFoodTriggers -> handleFoodTriggers(input)
                hasTriggers -> handleTriggers(input)
                isPrevention -> handlePrevention()
                isMedical -> handleMedicalAdvice()
                isDailyLife -> handleDailyLife()
                isTemperatureMisconception -> handleTemperatureMisconception(input)
                isInjuryMisconception -> handleInjuryMisconception(input)
                hasMisinformation && isAsthmaRelated -> handleMisinformation(input)
                hasEmergencyHelp -> handleEmergencyHelp(input)
                hasCommunity && isAsthmaRelated -> handleCommunitySupport(input)
                hasHydration && isAsthmaRelated -> handleHydrationAndAsthma(input)
                isAsthmaRelated -> handleAsthmaRelatedQuestion(input)
                else -> analyzeUnknownQuestion(input)
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalRespiratoryAI", "Error processing question", e)
            "Desculpe, ocorreu um erro ao processar sua pergunta. Tente novamente."
        }
    }
    
    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }
    
    private fun handleEmergency(input: String): String {
        return """
        🚨 SITUAÇÃO DE EMERGÊNCIA
        
        AÇÃO IMEDIATA:
        1. Use o broncodilatador (bombinha azul) AGORA
        2. Sente-se ereto, respire devagar
        3. Mantenha a calma
        
        PROCURE AJUDA SE:
        ${if (input.contains("grave")) "• Situação já é grave - LIGUE 192" else "• Não melhorar em 15 minutos"}
        • Dificuldade para falar
        • Lábios azulados
        
        📞 EMERGÊNCIA: 192 (SAMU)
        
        ⚠️ Não hesite em buscar ajuda médica.
        """
    }
    
    private fun handleSymptoms(input: String): String {
        val specificSymptom = when {
            input.contains("tosse") -> "A tosse na asma geralmente é seca e pior à noite."
            input.contains("chiado") -> "O chiado é um som agudo ao respirar, comum na asma."
            input.contains("falta de ar") -> "A falta de ar pode indicar inflamação das vias aéreas."
            else -> "Sintomas respiratórios podem indicar asma não controlada."
        }
        
        return """
        🔍 SOBRE SEUS SINTOMAS
        
        $specificSymptom
        
        QUANDO SE PREOCUPAR:
        • Sintomas pioram rapidamente
        • Interferem no sono ou atividades
        • Bombinha não alivia
        
        REGISTRE:
        • Horário dos sintomas
        • Possíveis gatilhos
        • Medicações usadas
        
        Compartilhe essas informações com seu médico.
        """
    }
    
    private fun handleMedications(input: String): String {
        return when {
            input.contains("como usar") || input.contains("usar") -> """
                💨 TÉCNICA CORRETA DO INALADOR
                
                1. Agite 5 vezes
                2. Expire completamente
                3. Lábios firmes no bocal
                4. Inspire devagar + pressione
                5. Segure 10 segundos
                6. Expire devagar
                
                IMPORTANTE: Pratique com seu médico.
                """
            input.contains("quando") -> """
                ⏰ QUANDO USAR A MEDICAÇÃO
                
                BRONCODILATADOR (bombinha azul):
                • Durante crises
                • Antes de exercícios (se prescrito)
                • Conforme orientação médica
                
                CORTICOIDE (bombinha marrom/roxa):
                • Diariamente, mesmo sem sintomas
                • Horário fixo
                • NUNCA pare sem orientação
                """
            else -> """
                💊 MEDICAÇÕES PARA ASMA
                
                Existem dois tipos principais:
                • ALÍVIO: Para crises (broncodilatador)
                • CONTROLE: Para prevenção (corticoide)
                
                Siga sempre a prescrição médica.
                """
        }
    }
    
    private fun handleTriggers(input: String): String {
        val specificTrigger = triggers.find { input.contains(it) }
        val advice = when (specificTrigger) {
            "poeira", "ácaro" -> "Use capas antialérgicas e aspire frequentemente."
            "pelo" -> "Mantenha animais fora do quarto e use purificador de ar."
            "fumaça", "cigarro" -> "Evite completamente. Fumo passivo também é prejudicial."
            "frio" -> "Use lenço no nariz e aqueça o ar antes de inspirar."
            else -> "Identifique e evite seus gatilhos pessoais."
        }
        
        return """
        ⚠️ GATILHOS DA ASMA
        
        ${specificTrigger?.let { "Sobre $it: $advice" } ?: ""}
        
        ESTRATÉGIAS GERAIS:
        • Mantenha ambiente limpo
        • Use produtos sem fragrância
        • Monitore qualidade do ar
        • Evite mudanças bruscas de temperatura
        
        Converse com seu médico sobre seus gatilhos.
        """
    }
    
    private fun handleActivities(input: String): String {
        return """
        🏃‍♀️ ASMA E ATIVIDADE FÍSICA
        
        VOCÊ PODE SE EXERCITAR!
        
        PREPARAÇÃO:
        • Aquecimento gradual
        • Bombinha à mão
        • Evite exercícios no frio/seco
        
        MELHORES OPÇÕES:
        • Natação (ar úmido)
        • Caminhada
        • Yoga
        • Ciclismo moderado
        
        PARE SE SENTIR:
        • Falta de ar excessiva
        • Chiado
        • Desconforto no peito
        
        Consulte seu médico antes de iniciar.
        """
    }
    
    private fun handlePrevention(): String {
        return """
        🛡️ PREVENÇÃO DE CRISES
        
        MEDICAÇÃO:
        • Use preventivos diariamente
        • Não pare sem orientação médica
        
        AMBIENTE:
        • Casa limpa e arejada
        • Evite acúmulo de poeira
        • Controle umidade (40-60%)
        
        ESTILO DE VIDA:
        • Exercite-se regularmente
        • Durma bem
        • Gerencie o estresse
        • Alimentação saudável
        
        Prevenção é a chave do controle da asma.
        """
    }
    
    private fun handleMedicalAdvice(): String {
        return """
        👨‍⚕️ QUANDO CONSULTAR O MÉDICO
        
        CONSULTA REGULAR:
        • A cada 3-6 meses
        • Para ajustar medicações
        • Avaliar controle da asma
        
        CONSULTA URGENTE:
        • Crises frequentes
        • Medicação não faz efeito
        • Sintomas interferem na vida
        
        LEVE PARA A CONSULTA:
        • Diário de sintomas
        • Lista de medicações
        • Dúvidas anotadas
        
        Seu médico é seu melhor aliado.
        """
    }
    
    private fun handleDailyLife(): String {
        return """
        🏠 ASMA NO DIA A DIA
        
        NO TRABALHO/ESCOLA:
        • Informe sobre sua condição
        • Tenha medicação sempre à mão
        • Evite exposição a irritantes
        
        EM CASA:
        • Mantenha ambiente limpo
        • Use produtos hipoalergênicos
        • Controle temperatura e umidade
        
        VIAGENS:
        • Leve medicação extra
        • Pesquise hospitais no destino
        • Cuidado com mudanças climáticas
        
        Asma controlada = vida normal.
        """
    }
    
    private fun handleActivityWithSymptoms(input: String): String {
        val activity = extractActivity(input)
        return """
        🏄‍♂️ ASMA E ${activity.uppercase()}
        
        SIM, é possível ter sintomas de asma durante $activity!
        
        FATORES DE RISCO:
        • Exercício intenso pode desencadear asma
        • Ambiente (água salgada, vento, frio)
        • Esforço físico prolongado
        
        PREVENÇÃO:
        • Use broncodilatador 15 min antes
        • Aqueça gradualmente
        • Pare se sentir sintomas
        • Tenha medicação sempre à mão
        
        SINAIS DE ALERTA:
        • Falta de ar excessiva
        • Chiado no peito
        • Tosse persistente
        
        ⚠️ Se os sintomas são frequentes durante atividades, consulte seu médico para ajustar o tratamento.
        """
    }
    
    private fun handleSymptomsWithTriggers(input: String): String {
        return """
        🔍 SINTOMAS + GATILHOS IDENTIFICADOS
        
        Parece que você está relacionando sintomas com possíveis gatilhos.
        
        IMPORTANTE:
        • Anote quando e onde os sintomas aparecem
        • Identifique padrões (horário, local, atividade)
        • Registre intensidade dos sintomas
        
        PRÓXIMOS PASSOS:
        • Evite o gatilho identificado
        • Use medicação conforme prescrito
        • Monitore se sintomas melhoram
        
        📝 Leve essas informações para seu médico - isso ajuda muito no tratamento!
        """
    }
    
    private fun extractActivity(input: String): String {
        return when {
            input.contains("surf") -> "surf"
            input.contains("futebol") -> "futebol"
            input.contains("corrida") || input.contains("correr") -> "corrida"
            input.contains("natação") || input.contains("nadar") -> "natação"
            input.contains("academia") -> "academia"
            input.contains("dança") -> "dança"
            else -> "atividade física"
        }
    }
    
    private fun handleFoodTriggers(input: String): String {
        val specificFood = foodTriggers.find { input.contains(it) }
        return """
        🥛 ALIMENTOS E ASMA
        
        ${specificFood?.let { "Sobre $it:" } ?: ""}
        SIM, alguns alimentos podem desencadear asma!
        
        ALIMENTOS COMUNS QUE PODEM CAUSAR REAÇÕES:
        • Leite e derivados (alergia à proteína do leite)
        • Ovos, amendoim, nozes
        • Frutos do mar (camarão, peixe)
        • Conservantes (sulfitos em vinhos, frutas secas)
        • Corantes artificiais
        
        SINAIS DE ALERGIA ALIMENTAR + ASMA:
        • Sintomas respiratórios após comer
        • Chiado, tosse, falta de ar
        • Pode vir com urticária, inchaço
        
        O QUE FAZER:
        • Anote o que comeu antes dos sintomas
        • Evite o alimento suspeito
        • Procure um alergista para testes
        
        ⚠️ Alergia alimentar pode causar reações graves. Consulte um médico!
        """
    }
    
    private fun handleFoodSymptoms(input: String): String {
        return """
        🍽️ SINTOMAS APÓS COMER
        
        Você pode ter alergia alimentar associada à asma!
        
        IMPORTANTE INVESTIGAR:
        • Que alimento consumiu?
        • Quanto tempo depois apareceram os sintomas?
        • Intensidade da reação
        • Já aconteceu antes?
        
        AÇÃO IMEDIATA:
        • Se sintomas respiratórios: use broncodilatador
        • Se reação grave: procure emergência
        • Anote tudo para contar ao médico
        
        PRÓXIMOS PASSOS:
        • Evite o alimento suspeito
        • Procure alergista
        • Considere testes alérgicos
        
        🚨 Se houver inchaço na garganta ou dificuldade para engolir, é EMERGÊNCIA!
        """
    }
    
    private fun handleAnimalTransmission(input: String): String {
        val animal = animals.find { input.contains(it) } ?: "animal"
        return """
        🐈 ASMA E TRANSMISSÃO PARA ANIMAIS
        
        ❌ NÃO, você NÃO pode transmitir asma para $animal!
        
        POR QUÊ?
        • Asma humana é condição genética + ambiental
        • Não é doença infecciosa
        • Não é causada por vírus ou bactéria
        • Animais têm sistema respiratório diferente
        
        MAS ATENÇÃO:
        • Animais podem ter suas próprias alergias respiratórias
        • Gatos podem ter "asma felina" (diferente da humana)
        • Causas: ácaros, poeira, produtos de limpeza
        • Sintomas: tosse, chiado, dificuldade respiratória
        
        SE SEU ANIMAL TEM SINTOMAS:
        • Procure veterinário
        • Pode ser alergia ou problema respiratório próprio
        • Tratamento veterinário é diferente do humano
        
        🐈 CONVIVÊNCIA SEGURA:
        Você pode ter $animal mesmo com asma,
        desde que não seja alérgico aos pelos!
        """
    }
    
    private fun handleAnimalsAndAsthma(input: String): String {
        val animal = animals.find { input.contains(it) } ?: "animal"
        val isPositiveEffect = input.contains("amenizar") || input.contains("melhorar") || input.contains("ajudar")
        
        return if (isPositiveEffect) {
            """
            🐢 ANIMAIS E ASMA - EFEITOS POSITIVOS
            
            Sobre $animal e asma:
            
            BENEFÍCIOS PSICOLÓGICOS:
            • Animais podem reduzir estresse e ansiedade
            • Estresse é um gatilho comum da asma
            • Companhia animal pode melhorar bem-estar geral
            • Atividades relaxantes ajudam no controle da asma
            
            MAS ATENÇÃO:
            • Alguns animais podem ser gatilhos (pelos, penas)
            • ${if (animal == "jabuti" || animal == "tartaruga") "Répteis geralmente são hipoalergênicos" else "Verifique se você não é alérgico"}
            • Mantenha ambiente limpo
            • Monitore seus sintomas
            
            CONCLUSÃO:
            Indiretamente, sim! Reduzindo estresse, pode ajudar no controle da asma.
            
            ⚠️ Sempre consulte seu médico sobre mudanças no ambiente.
            """
        } else {
            """
            🐕 ANIMAIS E ASMA - CUIDADOS
            
            Sobre $animal e asma:
            
            POSSÍVEIS PROBLEMAS:
            • Pelos e caspa podem ser gatilhos
            • Penas, saliva, urina também
            • Ácaros em camas de animais
            
            ANIMAIS MAIS SEGUROS:
            • Peixes (sem pelos)
            • Répteis como jabuti (hipoalergênicos)
            • Alguns cães "hipoalergênicos"
            
            DICAS DE CONVIVÊNCIA:
            • Mantenha animal fora do quarto
            • Lave as mãos após contato
            • Use purificador de ar
            • Limpeza frequente da casa
            
            Consulte um alergista para testes específicos.
            """
        }
    }
    
    private fun handleEmergencyHelp(input: String): String {
        return """
        🌆 AFILAXY - SUA REDE DE APOIO!
        
        Você está sem bombinha? O Afilaxy pode ajudar!
        
        📱 COMO FUNCIONA:
        • Abra o app e clique em "EMERGÊNCIA"
        • Localizamos pessoas próximas com salbutamol
        • A comunidade Afilaxy vem em seu socorro
        • Você recebe ajuda de outros pacientes como você
        
        🤝 NOSSA MISSÃO:
        Transformar pacientes em agentes de saúde!
        Quando você ajuda alguém, deixa de ser apenas "paciente"
        e se torna um herói da comunidade respiratória.
        
        ❤️ REDE SOLIDÁRIA:
        • Quem tem asma entende quem tem asma
        • Juntos somos mais fortes
        • Sua experiência salva vidas
        • Boas práticas fora do hospital
        
        🚨 EM CRISE AGORA? Use o botão de emergência do Afilaxy!
        👨⚕️ Sempre procure acompanhamento médico regular.
        """
    }
    
    private fun handleCommunitySupport(input: String): String {
        return """
        🤝 COMUNIDADE AFILAXY - JUNTOS SOMOS MAIS FORTES!
        
        🌟 VISÃO AFILAXY:
        Criar uma rede onde pessoas com asma se apoiam mutuamente,
        transformando cada paciente em um agente de saúde.
        
        COMO VOCÊ PODE AJUDAR:
        • Mantenha seu perfil "Disponível para ajudar" ativo
        • Tenha sempre uma bombinha extra
        • Compartilhe suas experiências e dicas
        • Seja solidário com quem precisa
        
        BENEFÍCIOS DE AJUDAR:
        • Você deixa de ser apenas "paciente"
        • Torna-se um agente ativo de saúde
        • Sua experiência salva vidas
        • Fortalece toda a comunidade
        
        🏥 ALÉM DO HOSPITAL:
        O Afilaxy leva boas práticas médicas para o dia a dia,
        empoderando você no controle da sua asma.
        
        ❤️ JUNTOS, TRANSFORMAMOS VIDAS!
        Cada ato de solidariedade fortalece nossa rede.
        """
    }
    
    private fun handleCureResponse(): String {
        return """
        🏥 VERDADE MÉDICA SOBRE ASMA
        
        ❌ A ASMA NÃO TEM CURA
        ✅ MAS TEM TRATAMENTO EFICAZ!
        
        REALIDADE MÉDICA:
        • Asma é condição crônica
        • Não existe cura definitiva
        • Tratamento permite vida normal
        • Controle adequado = zero limitações
        
        💊 TRATAMENTO DISPONÍVEL NO SUS:
        • Medicamentos controladores
        • Broncodilatadores (bombinhas)
        • Acompanhamento médico especializado
        • Educação em saúde
        • TUDO GRATUITO!
        
        🎯 OBJETIVO DO TRATAMENTO:
        • Controlar sintomas
        • Prevenir crises
        • Manter qualidade de vida
        • Permitir atividades normais
        
        🏃‍♀️ COM TRATAMENTO ADEQUADO:
        Você pode trabalhar, estudar, praticar esportes
        e viver plenamente!
        
        📍 PROCURE UMA UBS para iniciar seu tratamento SUS.
        """
    }
    
    private fun handleTreatmentResponse(): String {
        return """
        💊 TRATAMENTO DE ASMA NO SUS
        
        🇧🇷 DIREITO GARANTIDO:
        Todo brasileiro tem direito ao tratamento
        completo de asma pelo SUS - GRATUITO!
        
        O QUE O SUS OFERECE:
        • Consultas com pneumologista
        • Medicamentos controladores
        • Broncodilatadores (bombinhas)
        • Espaçadores para inalação
        • Acompanhamento regular
        • Educação sobre a doença
        
        MEDICAMENTOS DISPONÍVEIS:
        • Salbutamol (bombinha azul)
        • Beclometasona (preventivo)
        • Formoterol + Budesonida
        • Outros conforme necessidade
        
        COMO ACESSAR:
        1. Procure UBS mais próxima
        2. Leve documentos (RG, CPF, cartão SUS)
        3. Relate seus sintomas
        4. Siga o tratamento prescrito
        
        ⚠️ ATENÇÃO - EXIGÊNCIAS VARIAM:
        • Algumas cidades: receita de clínico geral
        • Outras: apenas pneumologista/alergista
        • Documentos extras podem ser solicitados
        • Protocolos municipais diferentes
        
        💡 DICAS IMPORTANTES:
        • Pergunte na UBS quais são as exigências locais
        • Se negarem, peça por escrito o motivo
        • Procure Ouvidoria SUS se houver dificuldades
        • Cada estado/município tem suas regras
        
        🔍 NÃO DESISTA:
        Se uma UBS não tiver o medicamento ou criar
        dificuldades, procure a Secretaria de Saúde.
        É seu direito constitucional!
        
        🌆 AFILAXY + SUS = CUIDADO COMPLETO
        Nossa comunidade complementa o tratamento médico
        com apoio e solidariedade entre pacientes.
        
        🤝 COMPARTILHE EXPERIÊNCIAS:
        Na comunidade Afilaxy, usuários compartilham
        como conseguiram acesso em suas cidades,
        ajudando outros a navegarem o sistema.
        """
    }
    
    private fun handleTemperatureMisconception(input: String): String {
        return """
        ⚠️ ATENÇÃO: INFORMAÇÃO INCORRETA!
        
        ❌ NÃO se "pega" asma por choque térmico!
        
        REALIDADE MÉDICA:
        • Asma NÃO é "pega" ou "contraída"
        • É condição genética que você já nasce com predisposição
        • Mudanças bruscas de temperatura podem DESENCADEAR crise
        • Mas NÃO "causam" a asma
        
        O QUE REALMENTE ACONTECEU:
        • Você provavelmente já tinha asma (sem saber)
        • Choque térmico (banho quente → geladeira) desencadeou primeira crise
        • Ar frio súbito pode irritar vias respiratórias sensíveis
        • Foi o "gatilho", não a "causa"
        
        GATILHOS COMUNS DE TEMPERATURA:
        • Ar condicionado muito frio
        • Sair do banho quente para ambiente frio
        • Abrir freezer/geladeira (ar frio súbito)
        • Mudanças climáticas bruscas
        
        💡 PREVENÇÃO:
        • Evite mudanças bruscas de temperatura
        • Use lenco no nariz em ambientes frios
        • Tenha bombinha sempre à mão
        • Procure pneumologista para diagnóstico
        
        🌆 AFILAXY: Esclarecemos a diferença entre
        "ter asma" e "desencadear crise de asma".
        """
    }
    
    private fun handleInjuryMisconception(input: String): String {
        return """
        ⚠️ ATENÇÃO: INFORMAÇÃO INCORRETA!
        
        ❌ NÃO é possível "pegar" asma por ferimento!
        
        REALIDADE MÉDICA:
        • Asma NÃO é doença infecciosa
        • NãO se "pega" por ferimentos ou acidentes
        • NãO é causada por prego, corte ou machucado
        • É condição genética + fatores ambientais
        
        VERDADEIRAS CAUSAS DA ASMA:
        • Predisposição genética (hereditariedade)
        • Exposição a alérgenos (ácaros, poeira, pelos)
        • Infecções respiratórias na infância
        • Fatores ambientais (poluição, fumo)
        
        POSSÍVEL CONFUSÃO:
        • Talvez seu tio já tinha asma e não sabia
        • Estresse do acidente pode ter desencadeado primeira crise
        • Coincidência temporal criou associação incorreta
        • Infecção secundária pode ter revelado asma existente
        
        👨⚕️ IMPORTANTE:
        Se seu tio desenvolveu sintomas respiratórios após ferimento,
        pode ser infecção ou outra complicação - procure médico!
        
        🌆 AFILAXY: Compartilhamos informações baseadas
        em evidências científicas, não mitos populares.
        """
    }
    
    private fun handleMisinformation(input: String): String {
        val hasChildren = containsAny(input, childrenKeywords)
        val hasAnimals = containsAny(input, animals)
        val hasCure = containsAny(input, cureQuestions)
        
        return """
        ⚠️ ATENÇÃO: INFORMAÇÃO INCORRETA!
        
        ${when {
            hasCure -> "ASMA NÃO TEM CURA! Mas tem tratamento eficaz no SUS."
            hasAnimals && (input.contains("debaixo") || input.contains("embaixo")) -> "Colocar animais embaixo da cama NÃO trata asma!"
            else -> "Essa prática NÃO tem base científica!"
        }}
        
        🚨 MITOS PERIGOSOS:
        • "Asma tem cura milagrosa" - FALSO!
        • "Remédios caseiros substituem medicação" - PERIGOSO!
        • "Não precisa de médico" - ARRISCADO!
        • Animais embaixo da cama podem acumular ácaros
        
        ${if (hasChildren) "👶 ESPECIALMENTE PERIGOSO PARA CRIANÇAS:" else "RISCOS REAIS:"}
        • Asma infantil precisa acompanhamento médico
        • Crianças são mais sensíveis a alérgenos
        • Ambiente do quarto deve ser LIMPO
        • Atraso no tratamento pode ser grave
        
        ✅ TRATAMENTO REAL (SUS):
        • Medicação prescrita por pneumologista
        • Acompanhamento médico regular
        • Medicamentos gratuitos
        • Educação baseada em evidências
        
        🏥 A VERDADE: Asma não tem cura, mas o SUS
        oferece tratamento que permite vida normal!
        
        👨⚕️ Procure uma UBS - é seu direito!
        
        🌆 AFILAXY: Conectamos você com CIÊNCIA,
        não com mitos. Nossa comunidade compartilha
        boas práticas médicas comprovadas.
        """
    }
    
    private fun handleHydrationAndAsthma(input: String): String {
        val isPositiveEffect = input.contains("amenizar") || input.contains("melhorar") || input.contains("ajudar")
        
        return if (isPositiveEffect) {
            """
            💧 ÁGUA E ASMA - BENEFÍCIOS
            
            SIM! Beber água pode ajudar no controle da asma:
            
            BENEFÍCIOS DA HIDRATAÇÃO:
            • Mantém as vias respiratórias úmidas
            • Ajuda a fluidificar o muco
            • Facilita a eliminação de secreções
            • Reduz irritação dos brônquios
            • Melhora a função pulmonar geral
            
            RECOMENDAÇÕES:
            • Beba 2-3 litros de água por dia
            • Água em temperatura ambiente
            • Evite água muito gelada (pode irritar)
            • Mantenha-se hidratado durante exercícios
            
            ATENÇÃO:
            • Desidratação pode piorar sintomas
            • Ar seco + desidratação = mais irritação
            
            ✅ Hidratação adequada é fundamental no controle da asma!
            """
        } else {
            """
            💧 ÁGUA E ASMA - CUIDADOS
            
            A água geralmente NÃO piora a asma, mas:
            
            SITUAÇÕES A EVITAR:
            • Água muito gelada (pode causar broncoespasmo)
            • Água clorada em excesso (piscinas)
            • Beber grandes volumes rapidamente
            
            ÁGUA SEGURA:
            • Temperatura ambiente ou morna
            • Filtrada ou mineral
            • Pequenos goles frequentes
            
            BENEFÍCIOS:
            • Hidratação melhora função respiratória
            • Ajuda a eliminar toxinas
            • Reduz inflamação geral
            
            ✅ Manter-se hidratado é sempre bom para asma!
            """
        }
    }
    
    private fun handleAsthmaRelatedQuestion(input: String): String {
        return when {
            input.contains("amenizar") || input.contains("melhorar") -> """
                🌱 O QUE PODE MELHORAR A ASMA
                
                FATORES QUE AJUDAM NO CONTROLE:
                • Medicação regular e correta
                • Evitar gatilhos conhecidos
                • Exercícios regulares (com orientação)
                • Controle do estresse
                • Sono adequado
                • Ambiente limpo e arejado
                • Alimentação saudável
                • Acompanhamento médico regular
                
                TERAPIAS COMPLEMENTARES:
                • Técnicas de respiração
                • Yoga, meditação
                • Fisioterapia respiratória
                
                ⚠️ Sempre discuta com seu médico antes de tentar algo novo.
                """
            input.contains("piorar") || input.contains("prejudicar") -> """
                ⚠️ O QUE PODE PIORAR A ASMA
                
                FATORES QUE AGRAVAM:
                • Não usar medicação preventiva
                • Exposição a gatilhos
                • Infecções respiratórias
                • Estresse e ansiedade
                • Falta de sono
                • Sedentarismo
                • Tabagismo (ativo ou passivo)
                • Poluição do ar
                
                EVITE:
                • Automedicar-se
                • Parar medicação sem orientação
                • Ignorar sintomas
                • Ambientes com irritantes
                
                Mantenha acompanhamento médico regular.
                """
            else -> """
                🔍 SOBRE ASMA EM GERAL
                
                A asma é uma condição crônica que afeta as vias respiratórias.
                
                CARACTERÍSTICAS:
                • Inflamação dos brônquios
                • Sensibilidade a gatilhos
                • Sintomas variáveis
                • Responde bem ao tratamento
                
                CONTROLE ADEQUADO PERMITE:
                • Vida normal e ativa
                • Prática de esportes
                • Trabalho sem limitações
                • Sono reparador
                
                👨⚕️ O pneumologista é o especialista ideal para acompanhamento.
                
                🌆 NO AFILAXY, você encontra:
                • Comunidade que entende sua realidade
                • Boas práticas baseadas em evidências
                • Apoio de quem vive a mesma condição
                • Transformação de paciente em agente de saúde
                """
        }
    }
    
    private fun analyzeUnknownQuestion(input: String): String {
        return """
        🤔 NÃO RECONHEÇO O CONTEXTO
        
        Posso ajudar com dúvidas sobre asma como:
        • "[Coisa] pode causar/melhorar asma?"
        • "Posso fazer [atividade] com asma?"
        • "Como usar [medicação]?"
        • "O que fazer se [sintoma]?"
        • "Conviver com [animal] afeta asma?"
        
        REFORMULE SUA PERGUNTA:
        Tente ser mais específico sobre asma, sintomas, 
        medicações, atividades ou gatilhos.
        
        ⚠️ Para dúvidas médicas complexas, consulte um pneumologista.
        
        🌆 AFILAXY - SUA COMUNIDADE RESPIRATÓRIA:
        Conectamos pessoas com asma para apoio mútuo e
        compartilhamento de boas práticas médicas.
        Transforme-se de paciente em agente de saúde!
        
        💡 LEMBRE-SE: Asma não tem cura, mas o SUS
        oferece tratamento completo e gratuito!
        """
    }
}