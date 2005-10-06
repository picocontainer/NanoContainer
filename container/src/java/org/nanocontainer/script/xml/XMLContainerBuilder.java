/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.nanocontainer.script.xml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nanocontainer.ClassPathElement;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.ClassNameKey;
import org.nanocontainer.integrationkit.ContainerPopulator;
import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ComponentMonitorStrategy;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DelegatingComponentMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class builds up a hierarchy of PicoContainers from an XML configuration file.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jeppe Cramon
 * @author Mauro Talevi
 * @version $Revision$
 */
public class XMLContainerBuilder extends ScriptedContainerBuilder implements ContainerPopulator {

    private final static String DEFAULT_COMPONENT_ADAPTER_FACTORY = DefaultComponentAdapterFactory.class.getName();
    private final static String DEFAULT_COMPONENT_INSTANCE_FACTORY = BeanComponentInstanceFactory.class.getName();
    private final static String DEFAULT_COMPONENT_MONITOR = DelegatingComponentMonitor.class.getName();

    private final static String CONTAINER = "container";
    private final static String CLASSPATH = "classpath";
    private final static String CLASSLOADER = "classloader";
    private static final String CLASS_NAME_KEY = "class-name-key";
    private final static String COMPONENT = "component";
    private final static String COMPONENT_IMPLEMENTATION = "component-implementation";
    private final static String COMPONENT_INSTANCE = "component-instance";
    private final static String COMPONENT_ADAPTER = "component-adapter";
    private final static String COMPONENT_ADAPTER_FACTORY = "component-adapter-factory";
    private final static String COMPONENT_INSTANCE_FACTORY = "component-instance-factory";
    private final static String COMPONENT_MONITOR = "component-monitor";
    private final static String CLASS = "class";
    private final static String FACTORY = "factory";
    private final static String FILE = "file";
    private final static String KEY = "key";
    private final static String PARAMETER = "parameter";
    private final static String URL = "url";

    private final static String CLASSNAME = "classname";
    private final static String CONTEXT = "context";
    private final static String VALUE = "value";

    private static final String EMPTY = "";

    private Element rootElement;
    /**
     * The XMLComponentInstanceFactory globally defined for the container.
     * It may be overridden at node level.
     */
    private XMLComponentInstanceFactory componentInstanceFactory;

    public XMLContainerBuilder(Reader script, ClassLoader classLoader) {
        super(script, classLoader);
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parse(documentBuilder, new InputSource(script));
        } catch (ParserConfigurationException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    public XMLContainerBuilder(final URL script, ClassLoader classLoader) {
        super(script, classLoader);
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documentBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws IOException {
                    URL url = new URL(script, systemId);
                    return new InputSource(url.openStream());
                }
            });
            parse(documentBuilder, new InputSource(script.toString()));
        } catch (ParserConfigurationException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    private void parse(DocumentBuilder documentBuilder, InputSource inputSource) {
        try {
            rootElement = documentBuilder.parse(inputSource).getDocumentElement();
        } catch (SAXException e) {
            throw new NanoContainerMarkupException(e);
        } catch (IOException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {
        try {
            // create ComponentInstanceFactory for the container
            componentInstanceFactory = createComponentInstanceFactory(rootElement.getAttribute(COMPONENT_INSTANCE_FACTORY));
            MutablePicoContainer childContainer = createMutablePicoContainer(rootElement.getAttribute(COMPONENT_ADAPTER_FACTORY), 
                    rootElement.getAttribute(COMPONENT_MONITOR), parentContainer);
            populateContainer(childContainer);
            return childContainer;
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException("Class not found:" + e.getMessage(), e);
        }
    }

    private MutablePicoContainer createMutablePicoContainer(String cafName, String monitorName, PicoContainer parentContainer) throws PicoCompositionException, ClassNotFoundException {
        MutablePicoContainer container = new DefaultPicoContainer(createComponentAdapterFactory(cafName), parentContainer);
        if ( !notSet(monitorName) ){
            ComponentMonitor monitor = createComponentMonitor(monitorName);
            ((ComponentMonitorStrategy)container).changeMonitor(monitor);
        }
        return container;
    }

    public void populateContainer(MutablePicoContainer container) {
        try {
            NanoContainer nanoContainer = new DefaultNanoContainer(getClassLoader(), container);
            registerComponentsAndChildContainers(nanoContainer, rootElement);
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException("Class not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new NanoContainerMarkupException(e);
        } catch (SAXException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    private void registerComponentsAndChildContainers(NanoContainer parentContainer, Element containerElement) throws ClassNotFoundException, IOException, SAXException {

        NodeList children = containerElement.getChildNodes();
        // register classpath first, regardless of order in the document.
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                String name = childElement.getNodeName();
                if (CLASSPATH.equals(name)) {
                    registerClasspath(parentContainer, childElement);
                }
            }
        }
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                String name = childElement.getNodeName();
                if (CONTAINER.equals(name)) {
                    MutablePicoContainer childContainer = parentContainer.getPico().makeChildContainer();
                    NanoContainer childNanoContainer = new DefaultNanoContainer(parentContainer.getComponentClassLoader(), childContainer);
                    registerComponentsAndChildContainers(childNanoContainer, childElement);
                } else if (COMPONENT_IMPLEMENTATION.equals(name)
                        || COMPONENT.equals(name)) {
                    registerComponentImplementation(parentContainer, childElement);
                } else if (COMPONENT_INSTANCE.equals(name)) {
                    registerComponentInstance(parentContainer, childElement);
                } else if (COMPONENT_ADAPTER.equals(name)) {
                    registerComponentAdapter(parentContainer, childElement);
                } else if (CLASSLOADER.equals(name)) {
                    registerClassLoader(parentContainer, childElement);
                } else if (CLASSPATH.equals(name) != true) {
                    throw new NanoContainerMarkupException("Unsupported element:" + name);
                }
            }
        }
    }

    private void registerClassLoader(NanoContainer parentContainer, Element childElement) throws IOException, SAXException, ClassNotFoundException {
        NanoContainer nano = new DefaultNanoContainer(parentContainer.getComponentClassLoader(), parentContainer.getPico());
        registerComponentsAndChildContainers(nano, childElement);
    }

    private void registerClasspath(NanoContainer container, Element classpathElement) throws IOException, ClassNotFoundException {
        NodeList children = classpathElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String fileName = childElement.getAttribute(FILE);
                String urlSpec = childElement.getAttribute(URL);
                URL url = null;
                if (urlSpec != null && !EMPTY.equals(urlSpec)) {
                    url = new URL(urlSpec);
                } else {
                    File file = new File(fileName);
                    if (!file.exists()) {
                        throw new IOException(file.getAbsolutePath() + " doesn't exist");
                    }
                    url = file.toURL();
                }
                ClassPathElement cpe = container.addClassLoaderURL(url);
                registerPermissions(cpe, childElement);
            }
        }
    }

    private void registerPermissions(ClassPathElement classPathElement, Element classPathXmlElement) throws ClassNotFoundException {
        NodeList children = classPathXmlElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String permissionClassName = childElement.getAttribute(CLASSNAME);
                String action = childElement.getAttribute(CONTEXT);
                String value = childElement.getAttribute(VALUE);
                MutablePicoContainer mpc = new DefaultPicoContainer();
                mpc.registerComponentImplementation(Permission.class, Class.forName(permissionClassName),new Parameter[] {new ConstantParameter(action), new ConstantParameter(value)});

                Permission permission = (Permission) mpc.getComponentInstanceOfType(Permission.class);
                classPathElement.grantPermission(permission);
            }
        }

    }

    private void registerComponentImplementation(NanoContainer container, Element element) throws ClassNotFoundException, MalformedURLException {
        String className = element.getAttribute(CLASS);
        if (notSet(className)) {
            throw new NanoContainerMarkupException("'" + CLASS + "' attribute not specified for " + element.getNodeName());
        }

        Parameter[] parameters = createChildParameters(container, element);
        Class clazz = container.getComponentClassLoader().loadClass(className);
        Object key = element.getAttribute(KEY);
        String classKey = element.getAttribute(CLASS_NAME_KEY);
        if (notSet(key)) {
            if (!notSet(classKey)) {
                key = new ClassNameKey(classKey);
            } else {
                key = clazz;
            }
        }
        if (parameters == null) {
            container.getPico().registerComponentImplementation(key, clazz);
        } else {
            container.getPico().registerComponentImplementation(key, clazz, parameters);
        }
    }

    private Parameter[] createChildParameters(NanoContainer container, Element element) throws ClassNotFoundException, MalformedURLException {
        List parametersList = new ArrayList();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                if (PARAMETER.equals(childElement.getNodeName())) {
                    String key = childElement.getAttribute(KEY);
                    if (key != null && !EMPTY.equals(key)) {
                        parametersList.add(new ComponentParameter(key));
                    } else {
                        parametersList.add(createConstantParameter(container.getPico(), childElement));
                    }
                }
            }
        }

        Parameter[] parameters = null;
        if (!parametersList.isEmpty()) {
            parameters = (Parameter[]) parametersList.toArray(new Parameter[parametersList.size()]);
        }
        return parameters;
    }

    private Parameter createConstantParameter(PicoContainer pico, Element element) throws ClassNotFoundException, MalformedURLException {
        XMLComponentInstanceFactory factory = createComponentInstanceFactory(element.getAttribute(FACTORY));

        Element instanceElement = getFirstChildElement(element);
        Object instance = factory.makeInstance(pico, instanceElement, getClassLoader());
        return new ConstantParameter(instance);
    }

    private void registerComponentInstance(NanoContainer container, Element element) throws ClassNotFoundException, PicoCompositionException, MalformedURLException {
        XMLComponentInstanceFactory factory = createComponentInstanceFactory(element.getAttribute(FACTORY));
        Element componentElement = getFirstChildElement(element);
        Object instance = factory.makeInstance(container.getPico(), componentElement, getClassLoader());

        String key = element.getAttribute(KEY);
        String classKey = element.getAttribute(CLASS_NAME_KEY);
        if (notSet(key)) {
            if (!notSet(classKey)) {
                container.getPico().registerComponentInstance(new ClassNameKey(key), instance);
            } else {
                container.getPico().registerComponentInstance(instance);
            }
        } else {
            container.getPico().registerComponentInstance(key, instance);
        }
    }

    private Element getFirstChildElement(Element parent) {
        NodeList children = parent.getChildNodes();
        Element child = null;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                child = (Element) children.item(i);
                break;
            }
        }
        if (child == null) {
            throw new NanoContainerMarkupException(parent.getNodeName() + " needs a child element");
        }
        return child;
    }

    private XMLComponentInstanceFactory createComponentInstanceFactory(String factoryClass) throws ClassNotFoundException {
        if ( notSet(factoryClass)) {
            // no factory has been specified for the node
            // return globally defined factory for the container - if there is one
            if (componentInstanceFactory != null) {
                return componentInstanceFactory;
            }
            factoryClass = DEFAULT_COMPONENT_INSTANCE_FACTORY;
        }

        NanoContainer adapter = new DefaultNanoContainer();
        adapter.registerComponentImplementation(XMLComponentInstanceFactory.class.getName(), factoryClass);
        return (XMLComponentInstanceFactory) adapter.getPico().getComponentInstances().get(0);
    }

    private void registerComponentAdapter(NanoContainer container, Element element) throws ClassNotFoundException, PicoCompositionException, MalformedURLException {
        String factoryName = element.getAttribute(FACTORY);
        if (notSet(factoryName)) {
            factoryName = DEFAULT_COMPONENT_ADAPTER_FACTORY;
        }
        String key = element.getAttribute(KEY);
        if (notSet(key)) {
            throw new NanoContainerMarkupException("'" + KEY + "' attribute not specified for " + element.getNodeName());
        }
        String className = element.getAttribute(CLASS);
        if (notSet(className)) {
            throw new NanoContainerMarkupException("'" + CLASS + "' attribute not specified for " + element.getNodeName());
        }
        Class implementationClass = getClassLoader().loadClass(className);
        Parameter[] parameters = createChildParameters(container, element);
        ComponentAdapterFactory componentAdapterFactory = createComponentAdapterFactory(factoryName);
        container.getPico().registerComponent(componentAdapterFactory.createComponentAdapter(key, implementationClass, parameters));
    }
    
    private ComponentAdapterFactory createComponentAdapterFactory(String factoryName) throws ClassNotFoundException, PicoCompositionException {
        if (notSet(factoryName)) {
            factoryName = DEFAULT_COMPONENT_ADAPTER_FACTORY;
        }
        Class factoryClass = getClassLoader().loadClass(factoryName);
        try {
            return (ComponentAdapterFactory) factoryClass.newInstance();
        } catch (InstantiationException e) {
            throw new NanoContainerMarkupException(e);
        } catch (IllegalAccessException e) {
            throw new NanoContainerMarkupException(e);
        }
    }

    private ComponentMonitor createComponentMonitor(String monitorName) throws ClassNotFoundException, PicoCompositionException {
        if (notSet(monitorName)) {
            monitorName = DEFAULT_COMPONENT_MONITOR;
        }
        Class monitorClass = getClassLoader().loadClass(monitorName);
        try {
            return (ComponentMonitor) monitorClass.newInstance();
        } catch (InstantiationException e) {
            throw new NanoContainerMarkupException(e);
        } catch (IllegalAccessException e) {
            throw new NanoContainerMarkupException(e);
        }
    }    
    private boolean notSet(Object string) {
        return string == null || string.equals(EMPTY);
    }

}
