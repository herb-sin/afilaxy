package com.afilaxy.security

import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Secure XML parser that prevents XXE attacks
 */
object SecureXmlParser {
    
    @Throws(ParserConfigurationException::class)
    fun createSecureDocumentBuilder(): DocumentBuilder {
        val factory = DocumentBuilderFactory.newInstance()
        
        // Disable DTDs completely
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        
        // Disable external DTDs
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        
        // Disable external DTDs as well
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        
        // Make parser namespace aware
        factory.isNamespaceAware = true
        
        // Disable XInclude
        factory.isXIncludeAware = false
        
        // Disable expansion of entity reference nodes
        factory.isExpandEntityReferences = false
        
        return factory.newDocumentBuilder()
    }
    
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun parseSecurely(inputStream: InputStream): Document? {
        return try {
            val builder = createSecureDocumentBuilder()
            builder.parse(inputStream)
        } catch (e: Exception) {
            SecureLogger.e("SecureXmlParser", "Failed to parse XML securely", e)
            null
        }
    }
}