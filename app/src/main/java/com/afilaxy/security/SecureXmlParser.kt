package com.afilaxy.security

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.XMLReader

object SecureXmlParser {
    
    fun createSecureDocumentBuilder(): DocumentBuilderFactory {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            
            // Disable XXE vulnerabilities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory.isXIncludeAware = false
            factory.isExpandEntityReferences = false
            
            factory
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureXmlParser", "Failed to create secure XML parser", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("XML parser configuration failed")
        }
    }
    
    fun createSecureSAXParser(): XMLReader {
        return try {
            val factory = SAXParserFactory.newInstance()
            
            // Disable XXE vulnerabilities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            
            factory.newSAXParser().xmlReader
        } catch (e: Exception) {
            SecurityUtils.safeLog("SecureXmlParser", "Failed to create secure SAX parser", SecurityUtils.LogLevel.ERROR)
            throw SecurityException("SAX parser configuration failed")
        }
    }
}