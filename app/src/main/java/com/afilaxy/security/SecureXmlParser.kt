package com.afilaxy.security

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.XMLReader

object SecureXmlParser {
    
    fun createSecureDocumentBuilder(): DocumentBuilderFactory {
        val factory = DocumentBuilderFactory.newInstance()
        
        // Disable XXE vulnerabilities
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.isXIncludeAware = false
        factory.isExpandEntityReferences = false
        
        return factory
    }
    
    fun createSecureSAXParser(): XMLReader {
        val factory = SAXParserFactory.newInstance()
        
        // Disable XXE vulnerabilities
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        
        return factory.newSAXParser().xmlReader
    }
}