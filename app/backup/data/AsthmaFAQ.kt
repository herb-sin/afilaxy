package com.afilaxy.data

data class FAQItem(
    val question: String,
    val answer: String,
    val category: String
)

object AsthmaFAQ {
    private val _faqItems by lazy { listOf(
        // Categoria: Sobre Asma
        FAQItem(
            question = "A asma tem cura?",
            answer = """
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
                
                🏃♀️ COM TRATAMENTO ADEQUADO:
                Você pode trabalhar, estudar, praticar esportes
                e viver plenamente!
                
                📍 PROCURE UMA UBS para iniciar seu tratamento SUS.
            """.trimIndent(),
            category = "Sobre Asma"
        ),
        
        FAQItem(
            question = "Posso pegar asma de outra pessoa?",
            answer = """
                ❌ NÃO, asma NÃO é contagiosa!
                
                REALIDADE MÉDICA:
                • Asma NÃO é doença infecciosa
                • NÃO se "pega" por contato
                • NÃO é causada por vírus ou bactéria
                • É condição genética + fatores ambientais
                
                VERDADEIRAS CAUSAS DA ASMA:
                • Predisposição genética (hereditariedade)
                • Exposição a alérgenos (ácaros, poeira, pelos)
                • Infecções respiratórias na infância
                • Fatores ambientais (poluição, fumo)
                
                🧬 COMPONENTE GENÉTICO:
                • Se pais têm asma, filhos têm maior risco
                • Mas não é "transmissão" - é herança genética
                • Mesmo com predisposição, pode nunca desenvolver
                
                🌆 AFILAXY: Compartilhamos informações baseadas
                em evidências científicas, não mitos populares.
            """.trimIndent(),
            category = "Sobre Asma"
        ),
        
        // Categoria: Tratamento SUS
        FAQItem(
            question = "Como conseguir medicamentos para asma no SUS?",
            answer = """
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
            """.trimIndent(),
            category = "Tratamento SUS"
        ),
        
        // Categoria: Medicamentos
        FAQItem(
            question = "Como usar a bombinha corretamente?",
            answer = """
                💨 TÉCNICA CORRETA DO INALADOR
                
                PASSO A PASSO:
                1. Agite bem a bombinha
                2. Expire completamente
                3. Aproxime o bocal da bombinha à sua boca
                4. Inspire devagar + pressione
                5. Segure 10 segundos
                6. Expire devagar
                
                DICAS IMPORTANTES:
                • Pratique com seu médico
                • Use espaçador se disponível
                • Limpe o bocal regularmente
                • Verifique validade
                
                ERROS COMUNS:
                • Inspirar muito rápido
                • Não segurar a respiração
                • Não agitar antes do uso
                • Posição incorreta da cabeça
                
                TIPOS DE MEDICAÇÃO:
                • BRONCODILATADOR (bombinha azul): Para crises
                • CORTICOIDE (bombinha marrom/roxa): Preventivo diário
                
                ⚠️ NUNCA pare medicação preventiva sem orientação médica!
                
                IMPORTANTE: Pratique a técnica com seu médico
                ou enfermeiro para garantir eficácia máxima.
            """.trimIndent(),
            category = "Medicamentos"
        ),
        
        // Categoria: Gatilhos
        FAQItem(
            question = "Quais são os principais gatilhos da asma?",
            answer = """
                ⚠️ GATILHOS COMUNS DA ASMA
                
                ALÉRGENOS:
                • Ácaros da poeira doméstica
                • Pelos de animais (cães, gatos)
                • Pólen de plantas
                • Fungos e mofo
                • Baratas e seus dejetos
                
                IRRITANTES:
                • Fumaça de cigarro (ativo e passivo)
                • Poluição do ar
                • Produtos de limpeza fortes
                • Perfumes e fragrâncias
                • Tinta fresca
                
                FATORES CLIMÁTICOS:
                • Mudanças bruscas de temperatura
                • Ar muito frio ou seco
                • Alta umidade
                • Tempestades
                
                OUTROS GATILHOS:
                • Exercícios intensos
                • Estresse emocional
                • Infecções respiratórias
                • Refluxo gastroesofágico
                • Alguns medicamentos (aspirina, beta-bloqueadores)
                
                ESTRATÉGIAS DE PREVENÇÃO:
                • Identifique seus gatilhos pessoais
                • Mantenha ambiente limpo
                • Use capas antialérgicas
                • Evite produtos com fragrância
                • Controle umidade (40-60%)
                • Não fume e evite fumo passivo
                
                📝 DICA: Mantenha um diário de sintomas
                para identificar seus gatilhos específicos.
            """.trimIndent(),
            category = "Gatilhos"
        ),
        
        // Categoria: Emergência
        FAQItem(
            question = "O que fazer durante uma crise de asma?",
            answer = """
                🚨 CRISE DE ASMA - AÇÃO IMEDIATA
                
                PRIMEIROS PASSOS:
                1. Use o broncodilatador (bombinha azul) AGORA
                2. Sente-se ereto, respire devagar
                3. Mantenha a calma
                4. Afrouxe roupas apertadas
                
                TÉCNICA DE RESPIRAÇÃO:
                • Inspire pelo nariz (4 segundos)
                • Segure a respiração (4 segundos)
                • Expire pela boca (6 segundos)
                • Repita até melhorar
                
                PROCURE AJUDA MÉDICA SE:
                • Não melhorar em 15 minutos
                • Dificuldade para falar
                • Lábios ou unhas azulados
                • Respiração muito rápida
                • Confusão mental
                
                📞 EMERGÊNCIA: 192 (SAMU)
                
                O QUE NÃO FAZER:
                • Não deite a pessoa
                • Não dê água durante a crise
                • Não use remédios caseiros
                • Não espere "passar sozinho"
                
                APÓS A CRISE:
                • Procure seu médico
                • Revise seu plano de ação
                • Ajuste medicação se necessário
                
                🌆 AFILAXY: Em emergência, use nosso app
                para encontrar ajuda próxima rapidamente!
                
                ⚠️ Não hesite em buscar ajuda médica.
                Asma pode ser fatal se não tratada adequadamente.
            """.trimIndent(),
            category = "Emergência"
        ),
        
        // Categoria: Atividade Física
        FAQItem(
            question = "Posso praticar esportes tendo asma?",
            answer = """
                🏃♀️ ASMA E ATIVIDADE FÍSICA
                
                ✅ SIM, VOCÊ PODE SE EXERCITAR!
                
                BENEFÍCIOS DO EXERCÍCIO:
                • Fortalece músculos respiratórios
                • Melhora condicionamento cardiovascular
                • Reduz inflamação geral
                • Controla peso
                • Melhora qualidade de vida
                
                PREPARAÇÃO:
                • Aquecimento gradual (10-15 min)
                • Bombinha sempre à mão
                • Evite exercícios no frio/seco
                • Use broncodilatador 15 min antes (se prescrito)
                
                MELHORES OPÇÕES:
                • Natação (ar úmido e quente)
                • Caminhada
                • Yoga e pilates
                • Ciclismo moderado
                • Tênis (com pausas)
                
                EVITE:
                • Corrida de longa distância em clima frio
                • Esportes em ambientes poluídos
                • Atividades muito intensas sem preparo
                
                PARE SE SENTIR:
                • Falta de ar excessiva
                • Chiado no peito
                • Tosse persistente
                • Aperto no peito
                
                DICAS IMPORTANTES:
                • Consulte seu médico antes de iniciar
                • Comece devagar e aumente gradualmente
                • Mantenha medicação em dia
                • Hidrate-se adequadamente
                
                🏊‍♀️ MUITOS ATLETAS OLÍMPICOS TÊM ASMA
                e competem no mais alto nível!
                
                Consulte seu médico para criar um plano
                de exercícios adequado para você.
            """.trimIndent(),
            category = "Atividade Física"
        )
    ) }
    
    val faqItems: List<FAQItem> get() = _faqItems
    
    private val _categories by lazy { _faqItems.map { it.category }.distinct() }
    
    fun getCategories(): List<String> = _categories
    
    fun getItemsByCategory(category: String): List<FAQItem> {
        return _faqItems.filter { it.category == category }
    }
}