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
        val parser = createSecureParser()
        parser.setInput(inputStream, "UTF-8")
        
        val result = mutableMapOf<String, String>()
        var eventType = parser.eventType
        var currentTag = ""
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                }
                XmlPullParser.TEXT -> {
                    if (currentTag.isNotEmpty()) {
                        // Sanitizar texto para prevenir injection
                        val sanitizedText = InputSanitizer.sanitizeText(parser.text)
                        result[currentTag] = sanitizedText
                    }
                }
                XmlPullParser.END_TAG -> {
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        
        return result
    }
}