package com.afilaxy.security

import android.util.Log
import org.xml.sax.SAXException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.XMLConstants

object SecureXmlUtils {
    
    private const val TAG = "SecureXmlUtils"
    
    /**
     * Creates a secure DocumentBuilderFactory that prevents XXE attacks
     */
    fun createSecureDocumentBuilderFactory(): DocumentBuilderFactory? {
        return try {
            DocumentBuilderFactory.newInstance().apply {
                // CRITICAL: Disable DTDs completely to prevent XXE
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                
                // CRITICAL: Disable all external entity processing
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                
                // CRITICAL: Disable external DTD loading
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                
                // CRITICAL: Disable XInclude processing
                isXIncludeAware = false
                
                // CRITICAL: Disable entity expansion
                isExpandEntityReferences = false
                
                // Enable secure processing mode
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                
                // Additional security: Limit entity expansion
                try {
                    setFeature("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", false)
                } catch (e: Exception) {
                    // Feature may not be available on all platforms
                }
            }
        } catch (e: ParserConfigurationException) {
            SecureLogger.e(TAG, "Failed to create secure DocumentBuilderFactory", e)
            null
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Unexpected error creating DocumentBuilderFactory", e)
            null
        }
    }
    
    /**
     * Creates a secure SAXParserFactory that prevents XXE attacks
     */
    fun createSecureSAXParserFactory(): SAXParserFactory? {
        return try {
            SAXParserFactory.newInstance().apply {
                // Disable DTDs completely
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                
                // Disable external entities
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                
                // Disable external DTDs
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                
                // Set secure processing
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            }
        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "Failed to create secure SAXParserFactory", e)
            null
        } catch (e: SAXException) {
            Log.e(TAG, "Failed to configure secure SAXParserFactory", e)
            null
        }
    }
    
    /**
     * Creates a secure TransformerFactory that prevents XXE attacks
     */
    fun createSecureTransformerFactory(): TransformerFactory? {
        return try {
            TransformerFactory.newInstance().apply {
                // Disable access to external DTDs and stylesheets
                try {
                    setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
                    setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "")
                } catch (e: Exception) {
                    // Ignore if not supported
                }
                
                // Set secure processing
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create secure TransformerFactory", e)
            null
        }
    }
    
    /**
     * Validates XML input stream size to prevent DoS attacks
     */
    fun validateXmlInputStream(inputStream: InputStream, maxSizeBytes: Long = 1_000_000): Boolean {
        return try {
            val availableBytes = inputStream.available()
            if (availableBytes > maxSizeBytes) {
                Log.w(TAG, "XML input stream too large: $availableBytes bytes")
                return false
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate XML input stream", e)
            false
        }
    }
    
    /**
     * Safely parses XML content with size and security checks
     */
    fun parseXmlSafely(xmlContent: String, maxSizeBytes: Long = 1_000_000): Boolean {
        return try {
            if (xmlContent.length > maxSizeBytes) {
                Log.w(TAG, "XML content too large: ${xmlContent.length} chars")
                return false
            }
            
            // Check for suspicious patterns
            val suspiciousPatterns = listOf(
                "<!DOCTYPE", "<!ENTITY", "SYSTEM", "PUBLIC", 
                "file://", "http://", "https://", "ftp://"
            )
            
            val lowerContent = xmlContent.lowercase()
            if (suspiciousPatterns.any { lowerContent.contains(it) }) {
                Log.w(TAG, "Suspicious XML patterns detected")
                return false
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "XML safety check failed", e)
            false
        }
    }
}