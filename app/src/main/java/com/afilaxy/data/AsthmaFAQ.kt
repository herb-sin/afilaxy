package com.afilaxy.data

data class FAQItem(
    val question: String,
    val answer: String,
    val category: String
)

object AsthmaFAQ {
    val faqItems = listOf(
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
                
                📍 NÃO DESISTA:
                Se uma UBS não tiver o medicamento ou criar
                dificuldades, procure a Secretaria de Saúde.
                É seu direito constitucional!
            """.trimIndent(),
            category = "Tratamento SUS"
        ),
        
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
                
                ⚠️ NUNCA pare medicação preventiva sem orientação médica!
                
                IMPORTANTE: Pratique a técnica com seu médico
                ou enfermeiro para garantir eficácia máxima.
            """.trimIndent(),
            category = "Medicamentos"
        ),
        
        FAQItem(
            question = "O que fazer durante uma crise de asma?",
            answer = """
                🚨 CRISE DE ASMA - AÇÃO IMEDIATA
                
                PRIMEIROS PASSOS:
                1. Use a bombinha (spray) com broncodilatador IMEDIATAMENTE
                2. Sente-se de forma ereta, respire devagar
                3. Mantenha a calma
                4. Afrouxe roupas apertadas
                
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
                
                🌆 AFILAXY: Em emergência, use nosso app
                para encontrar ajuda próxima rapidamente!
                
                ⚠️ Não hesite em buscar ajuda médica.
                Asma pode ser fatal se não tratada adequadamente.
            """.trimIndent(),
            category = "Emergência"
        ),
        
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
                
                MELHORES OPÇÕES:
                • Natação (ar úmido e quente)
                • Caminhada
                • Yoga e pilates
                • Ciclismo moderado
                • Tênis (com pausas)
                
                PREPARAÇÃO:
                • Aquecimento gradual (10-15 min)
                • Bombinha sempre à mão
                • Evite exercícios no frio/seco
                • Use broncodilatador 15 min antes (se prescrito)
                
                🏊♀️ MUITOS ATLETAS OLÍMPICOS TÊM ASMA
                e competem no mais alto nível!
                
                Consulte seu médico para criar um plano
                de exercícios adequado para você.
            """.trimIndent(),
            category = "Atividade Física"
        )
    )
}