package com.afilaxy.security

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.XMLReader

object SecureXmlParser {
    
    fun createSecureDocumentBuilder(): DocumentBuilderFactory {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            
            // Comprehensive XXE prevention
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
            
            // Additional security settings
            factory.isXIncludeAware = false
            factory.isExpandEntityReferences = false
            factory.isNamespaceAware = true
            factory.isValidating = false
            
            // Set access external restrictions
            factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
            factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "")
            
            factory
        } catch (e: Exception) {
            android.util.Log.e("SecureXmlParser", "Failed to create secure XML parser", e)
            throw SecurityException("XML parser configuration failed: ${e.message}")
        }
    }
    
    fun createSecureSAXParser(): XMLReader {
        return try {
            val factory = SAXParserFactory.newInstance()
            
            // Comprehensive XXE prevention for SAX
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
            
            // Additional security settings
            factory.isNamespaceAware = true
            factory.isValidating = false
            
            val parser = factory.newSAXParser()
            val xmlReader = parser.xmlReader
            
            // Set additional security properties on XMLReader
            xmlReader.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
            xmlReader.setProperty("http://javax.xml.XMLConstants/property/accessExternalSchema", "")
            
            xmlReader
        } catch (e: Exception) {
            android.util.Log.e("SecureXmlParser", "Failed to create secure SAX parser", e)
            throw SecurityException("SAX parser configuration failed: ${e.message}")
        }
    }
}