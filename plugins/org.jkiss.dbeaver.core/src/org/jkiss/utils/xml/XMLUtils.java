/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.utils.xml;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Common XML utils
 */
public class XMLUtils
{

	public static org.w3c.dom.Document parseDocument(String fileName)
		throws XMLException
	{
		return parseDocument(new java.io.File(fileName));
	}

	public static org.w3c.dom.Document parseDocument(java.io.File file)
		throws XMLException
	{
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = dbf.newDocumentBuilder();
			return xmlBuilder.parse(file);
		} catch (Exception er) {
			throw new XMLException("Error parsing XML document", er);
		}
	}

	public static org.w3c.dom.Document parseDocument(java.io.InputStream is)
		throws XMLException
	{
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = dbf.newDocumentBuilder();
			return xmlBuilder.parse(is);
		} catch (Exception er) {
			throw new XMLException("Error parsing XML document", er);
		}
	}

	public static org.w3c.dom.Element getChildElement(org.w3c.dom.Element element,
		String childName)
	{
		for (org.w3c.dom.Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
				((org.w3c.dom.Element) node).getTagName().equals(childName)) {
				return (org.w3c.dom.Element) node;
			}
		}
		return null;
	}

	public static String getChildElementBody(org.w3c.dom.Element element,
		String childName)
	{
		for (org.w3c.dom.Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
				((org.w3c.dom.Element) node).getTagName().equals(childName)) {
				return getElementBody((org.w3c.dom.Element) node);
			}
		}
		return null;
	}

	public static String getElementBody(org.w3c.dom.Element element)
	{
		org.w3c.dom.Node valueNode = element.getFirstChild();
		if (valueNode == null) {
			return null;
		}
		if (valueNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			return valueNode.getNodeValue();
		} else {
			return null;
		}
	}

	// Get list of all child elements of specified node
	public static Collection<Element> getChildElementList(
        Element parent,
        String nodeName)
	{
		List<Element> list = new ArrayList<Element>();
		for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
				nodeName.equals(node.getNodeName()))
			{
				list.add((Element) node);
			}
		}
		return list;
	}

	// Get list of all child elements of specified node
	public static Collection<Element> getChildElementListNS(
		org.w3c.dom.Element parent,
		String nsURI)
	{
		List<Element> list = new ArrayList<Element>();
		for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
				node.getNamespaceURI().equals( nsURI ))
			{
				list.add((Element) node);
			}
		}
		return list;
	}

	// Get list of all child elements of specified node
	public static Collection<Element> getChildElementListNS(
		org.w3c.dom.Element parent,
		String nodeName,
		String nsURI)
	{
		List<Element> list = new ArrayList<Element>();
		for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
				node.getLocalName().equals( nodeName ) &&
				node.getNamespaceURI().equals( nsURI ))
			{
				list.add((Element) node);
			}
		}
		return list;
	}

	// Get list of all child elements of specified node
	public static Collection<Element> getChildElementList(
		org.w3c.dom.Element parent,
		String[] nodeNameList)
	{
		List<Element> list = new ArrayList<Element>();
		for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				for (int i = 0; i < nodeNameList.length; i++) {
					if (node.getNodeName().equals( nodeNameList[i] )) {
						list.add((Element) node);
					}
				}
			}
		}
		return list;
	}

	// Find one child element with specified name
	public static org.w3c.dom.Element findChildElement(
		org.w3c.dom.Element parent)
	{
		for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{
				return (org.w3c.dom.Element)node;
			}
		}
		return null;
	}

	public static Object escapeXml(Object obj) {
		if (obj == null) {
			return null;
		} else if (obj instanceof CharSequence) {
			return escapeXml((CharSequence)obj);
		} else {
			return obj;
		}
	}

	public static String escapeXml(CharSequence str) {
		if (str == null) {
			return null;
		}
		StringBuilder res = null;
		int strLength = str.length();
		for (int i = 0; i < strLength; i++) {
			char c = str.charAt(i);
			String repl = encodeXMLChar(c);
			if (repl == null) {
				if (res != null) {
					res.append(c);
				}
			} else {
				if (res == null) {
					res = new StringBuilder(str.length() + 5);
					for (int k = 0; k < i; k++) {
						res.append(str.charAt(k));
					}
				}
				res.append(repl);
			}
		}
		return res == null ? str.toString() : res.toString();
	}

	public static boolean isValidXMLChar(char c)
	{
		return (c >= 32 || c == '\n' || c == '\r' || c == '\t');
	}

	/**
	 * Encodes a char to XML-valid form replacing &,',",<,> with special XML encoding.
	 *
	 * @param ch char to convert
	 * @return XML-encoded text
	 */
	public static String encodeXMLChar(char ch) {
		switch (ch) {
			case '&':
				return "&amp;";
			case '\"':
				return "&quot;";
			case '\'':
				return "&#39;";
			case '<':
				return "&lt;";
			case '>':
				return "&gt;";
			default:
				return null;
		}
	}

    public static XMLException adaptSAXException(Exception toCatch)
    {
        if (toCatch instanceof XMLException) {
            return (XMLException)toCatch;
        } else if (toCatch instanceof org.xml.sax.SAXException) {
            String message = toCatch.getMessage();
            Exception embedded = ((org.xml.sax.SAXException)toCatch).getException();
            if (embedded != null && embedded.getMessage() != null && embedded.getMessage().equals(message)) {
                // Just SAX wrapper - skip it
                return adaptSAXException(embedded);
            } else {
                return new XMLException(
					message,
					embedded != null ? adaptSAXException(embedded) : null);
            }
        } else {
            return new XMLException(toCatch.getMessage(), toCatch);
        }
    }

}
