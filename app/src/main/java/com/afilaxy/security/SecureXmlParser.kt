package com.afilaxy.security

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

object SecureXmlParser {
    
    fun createSecureParser(): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        
        // Desabilitar processamento de entidades externas para prevenir XXE
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        
        return factory.newPullParser()
    }
    
    fun parseSecurely(inputStream: InputStream): Map<String, String> {
        return try {
            val parser = createSecureParser()
            parser.setInput(inputStream, "UTF-8")
            
            val result = mutableMapOf<String, String>()
            var eventType = parser.eventType
            var currentTag = ""
            var elementCount = 0
            
            while (eventType != XmlPullParser.END_DOCUMENT && elementCount < 1000) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = InputSanitizer.sanitizeText(parser.name) ?: ""
                        elementCount++
                    }
                    XmlPullParser.TEXT -> {
                        if (currentTag.isNotEmpty() && parser.text.length < 10000) {
                            val sanitizedText = InputSanitizer.sanitizeText(parser.text)
                            if (sanitizedText.isNotBlank()) {
                                result[currentTag] = sanitizedText
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
            
            result
        } catch (e: Exception) {
            android.util.Log.e("SecureXmlParser", "Erro ao processar XML: ${e.message}")
            emptyMap()
        }
    }
}