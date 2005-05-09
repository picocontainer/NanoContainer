/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.DomReader;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.integrationkit.ContainerPopulator;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class builds up a hierarchy of PicoContainers from an XML configuration file.
 *
 * @author Konstantin Pribluda
 * @version $Revision$
 */
public class XStreamContainerBuilder extends ScriptedContainerBuilder implements ContainerPopulator {
    private final Element rootElement;

    private final static String IMPLEMENTATION = "implementation";
    private final static String INSTANCE = "instance";
    private final static String ADAPTER = "adapter";
    private final static String CLASS = "class";
    private final static String KEY = "key";
    private final static String CONSTANT = "constant";
    private final static String DEPENDENCY = "dependency";
    private final static String CONSTRUCTOR = "constructor";

    private final HierarchicalStreamDriver xsdriver;

    /**
    * construct with just reader, use context classloader
    */
    public XStreamContainerBuilder(Reader script) {
        this(script,Thread.currentThread().getContextClassLoader());
    }
    
    /**
    * construct with given script and specified classloader
    */
    public XStreamContainerBuilder(Reader script, ClassLoader classLoader) {
        this(script, classLoader, new DomDriver());
    }

    public XStreamContainerBuilder(Reader script, ClassLoader classLoader, HierarchicalStreamDriver driver) {
        super(script, classLoader);
        xsdriver = driver;
        InputSource inputSource = new InputSource(script);
        try {
            rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource).getDocumentElement();
        } catch (SAXException e) {
            throw new NanoContainerMarkupException(e);
        } catch (IOException e) {
            throw new NanoContainerMarkupException(e);
        } catch (ParserConfigurationException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    public void populateContainer(MutablePicoContainer container) {
        populateContainer(container, rootElement);
    }

    /**
     * just a convenience method, so we can work recursively with subcontainers
     * for whatever puproses we see cool.
     */
    private void populateContainer(MutablePicoContainer container, Element rootElement) {
        NodeList children = rootElement.getChildNodes();
        Node child;
        String name;
        short type;
        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            type = child.getNodeType();

            if (type == Document.ELEMENT_NODE) {
                name = child.getNodeName();
                if (IMPLEMENTATION.equals(name)) {
                    try {
                        insertImplementation(container, (Element) child);
                    } catch (ClassNotFoundException e) {
                        throw new NanoContainerMarkupException(e);
                    }
                } else if (INSTANCE.equals(name)) {
                    insertInstance(container, (Element) child);
                } else if (ADAPTER.equals(name)) {
                    insertAdapter(container, (Element) child);
                } else {
                    throw new NanoContainerMarkupException("Unsupported element:" + name);
                }
            }
        }

    }

    /**
     * process adapter node
     */
    protected void insertAdapter(MutablePicoContainer container, Element rootElement) {
        String key = rootElement.getAttribute(KEY);
        String klass = rootElement.getAttribute(CLASS);
        try {
            DefaultPicoContainer nested = new DefaultPicoContainer();
            populateContainer(nested, rootElement);

            if (key != null) {
                container.registerComponent((ComponentAdapter) nested.getComponentInstance(key));
            } else if (klass != null) {
                Class clazz = classLoader.loadClass(klass);
                container.registerComponent((ComponentAdapter) nested.getComponentInstanceOfType(clazz));
            } else {
                container.registerComponent((ComponentAdapter) nested.getComponentInstanceOfType(ComponentAdapter.class));
            }
        } catch (ClassNotFoundException ex) {
            throw new NanoContainerMarkupException(ex);
        }

    }

    /**
     * process implementation node
     */
    protected void insertImplementation(MutablePicoContainer container, Element rootElement) throws ClassNotFoundException {
        String key = rootElement.getAttribute(KEY);
        String klass = rootElement.getAttribute(CLASS);
        String constructor = rootElement.getAttribute(CONSTRUCTOR);
        if (klass == null || "".equals(klass)) {
            throw new NanoContainerMarkupException("class specification is required for component implementation");
        }

        Class clazz = classLoader.loadClass(klass);

        List parameters = new ArrayList();

        NodeList children = rootElement.getChildNodes();
        Node child;
        String name;
        String dependencyKey;
        String dependencyClass;
        Object parseResult;

        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            if (child.getNodeType() == Document.ELEMENT_NODE) {
                name = child.getNodeName();
                // constant parameter. it does not have any attributes.
                if (CONSTANT.equals(name)) {
                    // create constant with xstream
                    parseResult = parseElementChild((Element) child);
                    if (parseResult == null) {
                        throw new NanoContainerMarkupException("could not parse constant parameter");
                    }
                    parameters.add(new ConstantParameter(parseResult));
                } else if (DEPENDENCY.equals(name)) {
                    // either key or class must be present. not both
                    // key has prececence
                    dependencyKey = ((Element) child).getAttribute(KEY);
                    if (dependencyKey == null || "".equals(dependencyKey)) {
                        dependencyClass = ((Element) child).getAttribute(CLASS);
                        if (dependencyClass == null || "".equals(dependencyClass)) {
                            throw new NanoContainerMarkupException("either key or class must be present for dependency");
                        } else {
                            parameters.add(new ComponentParameter(classLoader.loadClass(dependencyClass)));
                        }
                    } else {
                        parameters.add(new ComponentParameter(dependencyKey));
                    }
                }
            }
        }

        // ok , we processed our children. insert implementation
        Parameter[] parameterArray = (Parameter[]) parameters.toArray(new Parameter[parameters.size()]);
        if (parameters.size() > 0 || "default".equals(constructor)) {
            if (key == null || "".equals(key)) {
                // without  key. clazz is our key
                container.registerComponentImplementation(clazz, clazz, parameterArray);
            } else {
                // with key
                container.registerComponentImplementation(key, clazz, parameterArray);
            }
        } else {
            if (key == null || "".equals(key)) {
                // without  key. clazz is our key
                container.registerComponentImplementation(clazz, clazz);
            } else {
                // with key
                container.registerComponentImplementation(key, clazz);
            }

        }
    }

    /**
     * process instance node. we get key from atributes ( if any ) and leave content
     * to xstream. we allow only one child node inside. ( first  one wins )
     */
    protected void insertInstance(MutablePicoContainer container, Element rootElement) {
        String key = rootElement.getAttribute(KEY);
        Object result = parseElementChild(rootElement);
        if (result == null) {
            throw new NanoContainerMarkupException("no content could be parsed in instance");
        }
        if (key != null && !"".equals(key)) {
            // insert with key
            container.registerComponentInstance(key, result);
        } else {
            // or without
            container.registerComponentInstance(result);
        }
    }

    /**
     * parse element child with xstream and provide object
     */
    protected Object parseElementChild(Element rootElement) {
        NodeList children = rootElement.getChildNodes();
        Node child;
        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            if (child.getNodeType() == Document.ELEMENT_NODE) {
                return (new XStream(xsdriver)).unmarshal(new DomReader((Element) child));
            }
        }
        return null;
    }

    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {
        try {
            String cafName = rootElement.getAttribute("componentadapterfactory");
            if ("".equals(cafName) || cafName == null) {
                cafName = DefaultComponentAdapterFactory.class.getName();
            }
            Class cafClass = classLoader.loadClass(cafName);
            ComponentAdapterFactory componentAdapterFactory = (ComponentAdapterFactory) cafClass.newInstance();
            MutablePicoContainer picoContainer = new DefaultPicoContainer(componentAdapterFactory);
            NanoContainer nano = new DefaultNanoContainer(classLoader, picoContainer);
            populateContainer(nano.getPico());
            return nano.getPico();
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException(e);
        } catch (InstantiationException e) {
            throw new NanoContainerMarkupException(e);
        } catch (IllegalAccessException e) {
            throw new NanoContainerMarkupException(e);
        }
    }
}
