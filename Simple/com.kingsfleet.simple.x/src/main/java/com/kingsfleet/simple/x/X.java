// Distributed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
// Copyright Gerard Davison 2011
package com.kingsfleet.simple.x;

import java.io.InputStream;

import java.io.OutputStream;
import java.io.Reader;

import java.io.Writer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

/**
 * X aims to be a very simple XML api for reading and writing properties with
 * as a little boilerplate as possible. For example the following code allows you
 * to read from a stream and then select a value using the predefined n: prefix
 * take from the root element:
 * <pre>
 *     X x = X.in(inputStream);
 *     boolean enabled = "true".equals(x.get("n:project/@enabled"));
 *     if (!enabled)
 *     {
 *        x.set("n:project/@enabled", "true");
 *     }
 *     x.out(outputStream);
 * </pre>
 * <p>In order to simplify the API there is only one set method used for both element
 * and attribute values. You can select or selectList in order to get ranges or child
 * X elements.</p>
 * 
 * @TODO attributes be able to add as we can for elements?
 * @TODO implement add and remove methods of AbstractList
 * @TODO register new namespace prefixes
 * @TODO get/set on elements default to attribure if prefix with @
 */

public class X {
    private Node _node;
    private Context _context;
    private XList _children;
    private XLIst _attributes;


    // Methods for creating and storing X
    //

    private X(Node node, Context context) {
        _node = node;
        _context = context;
    }

    private X(Node node) {
        this(node, new Context());
        if (node.getNamespaceURI() != null) {
            _context.prefixToNamespace.put("n", node.getNamespaceURI());
        }
        _context.nodeToX.put(node, this);
    }


    public static X in(InputStream is) {
        return in(new InputSource(is));
    }

    public static X in(Reader r) {
        return in(new InputSource(r));
    }

    private static X in(InputSource is) {
        try {
            DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
            bf.setNamespaceAware(true);
            DocumentBuilder builder = bf.newDocumentBuilder();
            Document document = builder.parse(is);
            return new X(document.getDocumentElement());
        } catch (Exception ex) {
            throw new XException("Problem parsing XML", ex);
        }
    }


    public void out(OutputStream os) {
        try {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(_node), new StreamResult(os));
        } catch (Exception ex) {
            throw new XException("Problem writing XML", ex);
        }
    }

    public void out(Writer os) {
        try {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(_node), new StreamResult(os));
        } catch (Exception ex) {
            throw new XException("Problem writing XML", ex);
        }
    }


    // Selectors

    public X select(String xpath) {
        // TODO implenent code to use attribute directly if a very
        // simple xpath expression

        try {
            Node node = (Node)_context.xpath.evaluate(xpath, _node, XPathConstants.NODE);

            // TODO return a null X here?
            return node != null ? _context.findOrCreate(node) : null;
        } catch (Exception ex) {
            throw new XException("Problem processing xpath", ex);
        }
    }

    public List<X> selectList(String xpath) {
        try {
            List<X> result = new ArrayList<X>();
            NodeList list = (NodeList)_context.xpath.evaluate(xpath, _node, XPathConstants.NODESET);
            int i = list.getLength();
            for (int listItem = 0; listItem < i; listItem++) {
                result.add(_context.findOrCreate(list.item(listItem)));
            }
            return result;
        } catch (Exception ex) {
            throw new XException("Problem processing xpath", ex);
        }
    }

    public XList children() {

        if (_children == null) {
                
            class EList extends AbstractList<X> implements XList {
    
                @Override
                public X get(int index) {
                    NodeList list = _node.getChildNodes();
                    return _context.findOrCreate(list.item(0));
                }
    
                @Override
                public int size() {
                    NodeList list = _node.getChildNodes();
                    return list.getLength();
                }
                
                @Override
                public X remove(int index) {
                    NodeList list = _node.getChildNodes();
                    if (index < 0 && index >= size()) {
                        throw new IndexOutOfBoundsException(
                            "Index " + index + " out of bounds");
                    }

                    Node child = _node.removeChild(
                        list.item(index));
                    modCount ++;
                    return _context.findOrCreate(child);
                }
    
                @Override
                public X create(String localname) {
                    return create(_node.getNamespaceURI(), localname);
                }
    
                @Override
                public X create(String namespace, String localname) {
                    Element newElement = _node.getOwnerDocument().createElementNS(namespace, localname);
                    _node.appendChild(newElement);
                    modCount++;
                    return _context.findOrCreate(newElement);
                }
            }
    
    
            _children = new EList();
        }
        
        return _children;
    }

    public List<X> attributes() {
        if (_attributes == null) {
                
            class AttrList extends AbstractList<X> implements XList {
        
                @Override
                public X get(int index) {
                    NamedNodeMap list = _node.getAttributes();
                    return _context.findOrCreate(list.item(0));
                }
        
                @Override
                public int size() {
                    NamedNodeMap list = _node.getAttributes();
                    return list.getLength();
                }
                
                @Override
                public X remove(int index) {
                    NamedNodeMap list = _node.getAttributes();
                    if (index < 0 && index >= size()) {
                        throw new IndexOutOfBoundsException(
                            "Index " + index + " out of bounds");
                    }

                    Node child = _node.removeChild(
                        list.item(index));
                    modCount ++;
                    return _context.findOrCreate(child);
                }
        
                @Override
                public X create(String localname) {
                    // Assume unqualified attributes
                    return create(null, localname);
                }
        
                @Override
                public X create(String namespace, String localname) {
                    Attr newAttr = _node.getOwnerDocument().createAttributeNS(namespace, localname);
                    ((Element)_node).setAttributeNodeNS(newAttr);
                    modCount++;
                    return _context.findOrCreate(newAttr);
                }
            }
        
        
            _attributes = new AttrList();
        }
        
        return _attributes;
    }

    // Get and set values


    public X set(String value) {
        if (_node instanceof Element) {
            _node.setTextContent(value);
        } else {
            _node.setNodeValue(value);
        }
        return this;
    }

    public String get() {
        return _node instanceof Element ? _node.getTextContent() : _node.getNodeValue();
    }


    public X set(String xpath, String value) {

        // The the simple attribute case we can create new attributes
        // this means you can set attributes that are not there
        // TODO more complex paths
        // TODO namespaces
        if (_node instanceof Element &&
            xpath.matches("@\\w*")) {
            
            String attributeName = xpath.substring(1);
            ((Element)_node).setAttributeNS(null, attributeName, value);
        }
        else {
            X s = select(xpath);
            if (s != null) {
                s.set(value);
            } else {
                throw new XException("xpath not found");
            }
        }

        return this;
    }

    public String get(String xpath) {
        X s = select(xpath);
        if (s != null) {
            return s.get();
        } else {
            throw new XException("xpath not found");
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node parent;
        if (_node instanceof Attr) {
            sb.append("@");
            parent = ((Attr)_node).getOwnerElement();
        } else {
            parent = _node.getParentNode();
        }

        sb.append(_node.getLocalName());

        while (parent != null && !(parent instanceof Document)) {
            sb.append("<-");
            sb.append(parent.getLocalName());
            parent = parent.getParentNode();
        }

        return sb.toString();

    }


    //


    public static interface XList extends List<X> {
        public X create(String name);

        public X create(String namepace, String name);
    }

    public static class XException extends RuntimeException {

        public XException(Throwable cause) {
            super(cause);
        }

        public XException(String message, Throwable cause) {
            super(message, cause);
        }

        public XException(String message) {
            super(message);
        }
    }

    private static class Context {
        final Map<String, String> prefixToNamespace = new HashMap<String, String>();
        final XPath xpath = XPathFactory.newInstance().newXPath();
        {
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return prefixToNamespace.get(prefix != null ? prefix : "");
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return null;
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    return prefixToNamespace.keySet().iterator();
                }
            });
        }
        
        final Map<Node, X> nodeToX = new HashMap<Node,X>();
        private X findOrCreate(Node node) {
            X found = nodeToX.get(node);
            if (found==null) {
                found = new X(node, this);
                nodeToX.put(node, found);
            }
            
            return found;
        }
    }
}
