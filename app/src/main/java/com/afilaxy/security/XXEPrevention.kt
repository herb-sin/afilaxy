package com.afilaxy.security

import org.xml.sax.SAXException
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.TransformerFactory
import javax.xml.validation.SchemaFactory

object XXEPrevention {
    
    fun createSecureDocumentBuilderFactory(): DocumentBuilderFactory? {
        return try {
            if (!AuthGuard.requireAuthentication("xml_processing")) {
                SecureLogger.security("XXE_PREVENTION", "UNAUTHENTICATED_XML_ACCESS")
                return null
            }
            
            DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                isXIncludeAware = false
                isExpandEntityReferences = false
                isNamespaceAware = true
            }
        } catch (e: Exception) {
            SecureLogger.e("XXEPrevention", "Failed to create secure DocumentBuilderFactory", e)
            null
        }
    }
    
    fun createSecureSAXParserFactory(): SAXParserFactory? {
        return try {
            if (!AuthGuard.requireAuthentication("xml_processing")) {
                SecureLogger.security("XXE_PREVENTION", "UNAUTHENTICATED_SAX_ACCESS")
                return null
            }
            
            SAXParserFactory.newInstance().apply {
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                isNamespaceAware = true
            }
        } catch (e: Exception) {
            SecureLogger.e("XXEPrevention", "Failed to create secure SAXParserFactory", e)
            null
        }
    }
    
    fun createSecureTransformerFactory(): TransformerFactory {
        return TransformerFactory.newInstance().apply {
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "")
        }
    }
    
    fun createSecureSchemaFactory(): SchemaFactory {
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).apply {
            setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        }
    }
    
    fun validateXMLContent(content: String): Boolean {
        return try {
            if (!AuthGuard.requireAuthentication("xml_validation")) {
                SecureLogger.security("XXE_PREVENTION", "UNAUTHENTICATED_XML_VALIDATION")
                return false
            }
            
            if (content.isBlank() || content.length > 1_000_000) {
                SecureLogger.security("XXE_PREVENTION", "INVALID_XML_SIZE")
                return false
            }
            
            val dangerousPatterns = listOf(
                "<!DOCTYPE", "<!ENTITY", "SYSTEM", "PUBLIC", 
                "file://", "http://", "https://", "ftp://",
                "&lt;!ENTITY", "&lt;!DOCTYPE", "CDATA", "<![CDATA["
            )
            
            val hasDangerousPattern = dangerousPatterns.any { pattern ->
                content.contains(pattern, ignoreCase = true)
            }
            
            if (hasDangerousPattern) {
                SecureLogger.security("XXE_PREVENTION", "DANGEROUS_XML_PATTERN_DETECTED")
                return false
            }
            
            true
        } catch (e: Exception) {
            SecureLogger.e("XXEPrevention", "XML validation error", e)
            false
        }
    }
}