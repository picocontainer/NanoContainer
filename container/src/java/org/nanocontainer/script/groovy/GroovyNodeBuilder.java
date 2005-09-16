/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/

/*
TODO:

don't depend on proxytoys - introduce a PointcutsFactoryFactory
*/

package org.nanocontainer.script.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.util.BuilderSupport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.nanocontainer.ClassNameKey;
import org.nanocontainer.ClassPathElement;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.NodeBuilderDecorationDelegate;
import org.nanocontainer.script.NullNodeBuilderDecorationDelegate;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ComponentMonitorStrategy;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DelegatingComponentMonitor;

/**
 * <p>
 * Builds node trees of PicoContainers and Pico components using GroovyMarkup.
 * </p>
 * <p>Simple example usage in your groovy script:
 * <code><pre>
 * builder = new org.nanocontainer.script.groovy.GroovyNodeBuilder()
 * pico = builder.container(parent:parent) {
 * &nbsp;&nbsp;component(class:org.nanocontainer.testmodel.DefaultWebServerConfig)
 * &nbsp;&nbsp;component(class:org.nanocontainer.testmodel.WebServerImpl)
 * }
 * </pre></code>
 * </p>
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov 
 * @author Mauro Talevi
 * @version $Revision$
 */
public class GroovyNodeBuilder extends BuilderSupport {
    
    private static final String NEW_BUILDER = "newBuilder";
    private static final String CONTAINER = "container";
    private static final String COMPONENT = "component";
    private static final String INSTANCE = "instance";
    private static final String KEY = "key";
    private static final String PARAMETERS = "parameters";
    private static final String BEAN = "bean";
    private static final String BEAN_CLASS = "beanClass";
    private static final String CLASS = "class";
    private static final String CLASS_NAME_KEY = "classNameKey";
    private static final String CLASSPATH_ELEMENT = "classPathElement";
    private static final String CLASSLOADER = "classLoader";
    private static final String PATH = "path";
    private static final String GRANT = "grant";
    private static final String HTTP = "http://";
    private static final String PARENT = "parent";
    private static final String COMPONENT_ADAPTER_FACTORY = "componentAdapterFactory";
    private static final String COMPONENT_MONITOR = "componentMonitor";
    private static final String EMPTY = "";
    private static final String DO_CALL = "doCall";
    
    private final NodeBuilderDecorationDelegate decorationDelegate;

    public GroovyNodeBuilder(NodeBuilderDecorationDelegate decorationDelegate) {
        this.decorationDelegate = decorationDelegate;
    }

    public GroovyNodeBuilder() {
        this(new NullNodeBuilderDecorationDelegate());
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object doInvokeMethod(String s, Object name, Object args) {
        //TODO use setDelegate() from Groovy JSR
        Object answer = super.doInvokeMethod(s, name, args);
        List list = InvokerHelper.asList(args);
        if (!list.isEmpty()) {
            Object o = list.get(list.size() - 1);
            if (o instanceof Closure) {
                Closure closure = (Closure) o;
                closure.setDelegate(answer);
            }
        }
        return answer;
    }

    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        Map attributes = new HashMap();
        attributes.put(CLASS, value);
        return createNode(name, attributes);
    }

    /**
     * Override of create node.  Called by BuilderSupport.  It examines the
     * current state of the builder and the given parameters and dispatches the
     * code to one of the create private functions in this object.
     * @param name The name of the groovy node we're building.  Examples are
     * 'container', and 'grant',
     * @param attributes Map  attributes of the current invocation.
     * @return Object the created object.
     */
    protected Object createNode(Object name, Map attributes) {
        Object current = getCurrent();
        if (current != null && current instanceof GroovyObject) {
            return createChildBuilder(current, name, attributes);
        } else if (current == null || current instanceof NanoContainer) {
            NanoContainer parent = (NanoContainer) current;
            Object parentAttribute = attributes.get(PARENT);
            if (parent != null && parentAttribute != null) {
                throw new NanoContainerMarkupException("You can't explicitly specify a parent in a child element.");
            }
            if (parent == null && (parentAttribute instanceof NanoContainer)) {
                // we're not in an enclosing scope - look at parent attribute instead
                parent = (NanoContainer) parentAttribute;
            }
            if (name.equals(CONTAINER)) {
                return createChildContainer(attributes, parent);
            } else {
                try {
                    return createChildOfContainerNode(parent, name, attributes, current);
                } catch (ClassNotFoundException e) {
                    throw new NanoContainerMarkupException("ClassNotFoundException: " + e.getMessage(), e);
                }
            }
        } else if (current instanceof ClassPathElement) {
            if (name.equals(GRANT)) {
                return createGrantPermission(attributes, (ClassPathElement) current);
            }
            return EMPTY;
        } else {
            // we don't know how to handle it - delegate to the decorator.
            return decorationDelegate.createNode(name, attributes, current);
        }
    }

    private Object createChildBuilder(Object current, Object name, Map attributes) {
        GroovyObject groovyObject = (GroovyObject) current;
        return groovyObject.invokeMethod(name.toString(), attributes);
    }

    private Object createGrantPermission(Map attributes, ClassPathElement cpe) {
        Permission perm = (Permission) attributes.remove(CLASS);
        return cpe.grantPermission(perm);

    }

    private Object createChildOfContainerNode(NanoContainer parentContainer, Object name, Map attributes, Object current) throws ClassNotFoundException {
        if (name.equals(COMPONENT)) {
            decorationDelegate.rememberComponentKey(attributes);
            return createComponentNode(attributes, parentContainer, name);
        } else if (name.equals(BEAN)) {
            return createBeanNode(attributes, parentContainer.getPico());
        } else if (name.equals(CLASSPATH_ELEMENT)) {
            return createClassPathElementNode(attributes, parentContainer);
        } else if (name.equals(DO_CALL)) {
            // TODO does this node need to be handled?
            return null;
        } else if (name.equals(NEW_BUILDER)) {
            return createNewBuilderNode(attributes, parentContainer);
        } else if (name.equals(CLASSLOADER)) {
            return createComponentClassLoader(parentContainer);
        } else {
            // we don't know how to handle it - delegate to the decorator.
            return decorationDelegate.createNode(name, attributes, current);
        }

    }

    private Object createNewBuilderNode(Map attributes, NanoContainer parentContainer) {
        String builderClass = (String) attributes.remove(CLASS);
        NanoContainer factory = new DefaultNanoContainer();
        MutablePicoContainer parentPico = parentContainer.getPico();
        factory.getPico().registerComponentInstance(MutablePicoContainer.class, parentPico);
        try {
            factory.registerComponentImplementation(GroovyObject.class, builderClass);
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException("ClassNotFoundException " + builderClass);
        }
        Object componentInstance = factory.getPico().getComponentInstance(GroovyObject.class);
        return componentInstance;
    }

    private ClassPathElement createClassPathElementNode(Map attributes, NanoContainer nanoContainer) {

        final String path = (String) attributes.remove(PATH);
        URL pathURL = null;
        try {
            if (path.toLowerCase().startsWith(HTTP)) {
                pathURL = new URL(path);
            } else {
                Object rVal = AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            File file = new File(path);
                            if (!file.exists()) {
                                return new NanoContainerMarkupException("classpath '" + path + "' does not exist ");
                            }
                            return file.toURL();
                        } catch (MalformedURLException e) {
                            return e;
                        }

                    }
                });
                if (rVal instanceof MalformedURLException) {
                    throw (MalformedURLException) rVal;
                }
                if (rVal instanceof NanoContainerMarkupException) {
                    throw (NanoContainerMarkupException) rVal;
                }
                pathURL = (URL) rVal;
            }
        } catch (MalformedURLException e) {
            throw new NanoContainerMarkupException("classpath '" + path + "' malformed ", e);
        }
        return nanoContainer.addClassLoaderURL(pathURL);
    }

    private Object createBeanNode(Map attributes, MutablePicoContainer pico) {
        Object bean = createBean(attributes);
        pico.registerComponentInstance(bean);
        return bean;
    }

    private Object createComponentNode(Map attributes, NanoContainer nano, Object name) throws ClassNotFoundException {
        Object key = attributes.remove(KEY);
        Object cnkey = attributes.remove(CLASS_NAME_KEY);
        Object classValue = attributes.remove(CLASS);
        Object instance = attributes.remove(INSTANCE);
        List parameters = (List) attributes.remove(PARAMETERS);

        MutablePicoContainer pico = nano.getPico();

        if (cnkey != null)  {
            key = new ClassNameKey((String)cnkey);
        }

        Parameter[] parameterArray = getParameters(parameters);
        if (classValue instanceof Class) {
            Class clazz = (Class) classValue;
            key = key == null ? clazz : key;
            pico.registerComponentImplementation(key, clazz, parameterArray);
        } else if (classValue instanceof String) {
            String className = (String) classValue;
            key = key == null ? className : key;
            nano.registerComponentImplementation(key, className, parameterArray);
        } else if (instance != null) {
            key = key == null ? instance.getClass() : key;
            pico.registerComponentInstance(key, instance);
        } else {
            throw new NanoContainerMarkupException("Must specify a class attribute for a component as a class name (string) or Class. Attributes:" + attributes);
        }

        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        return createNode(name, attributes);
    }

    /**
     * Creates a new container.  There may or may not be a parent to this container.
     * Supported attributes are:
     * <ul>
     *  <li><tt>componentAdapterFactory</tt>: The ComponentAdapterFactory used for new container</li>
     *  <li><tt>componentMonitor</tt>: The ComponentMonitor used for new container</li>
     * </ul>
     * @param attributes Map Attributes defined by the builder in the script.
     * @param parent The parent container
     * @return The NanoContainer
     */
    protected NanoContainer createChildContainer(Map attributes, NanoContainer parent) {        
                
        ClassLoader parentClassLoader = null;
        MutablePicoContainer childContainer = null;
        if (parent != null) {
            parentClassLoader = parent.getComponentClassLoader();            
            if ( isAttribute(attributes, COMPONENT_ADAPTER_FACTORY) ) {
                ComponentAdapterFactory componentAdapterFactory = createComponentAdapterFactory(attributes);
                childContainer = new DefaultPicoContainer(
                        decorationDelegate.decorate(componentAdapterFactory, attributes), parent.getPico());
                if ( isAttribute(attributes, COMPONENT_MONITOR) ) {
                    changeComponentMonitor(childContainer, createComponentMonitor(attributes));
                }
                parent.getPico().addChildContainer(childContainer);
            } else if ( isAttribute(attributes, COMPONENT_MONITOR) ) {
                ComponentAdapterFactory componentAdapterFactory = new DefaultComponentAdapterFactory( 
                                                    createComponentMonitor(attributes));
                childContainer = new DefaultPicoContainer(
                        decorationDelegate.decorate(componentAdapterFactory, attributes), parent.getPico());
            } else {
                childContainer = parent.getPico().makeChildContainer();
            }
        } else {
            parentClassLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return PicoContainer.class.getClassLoader();
                }
            });
            ComponentAdapterFactory componentAdapterFactory = createComponentAdapterFactory(attributes);
            childContainer = new DefaultPicoContainer(
                    decorationDelegate.decorate(componentAdapterFactory, attributes));
            if ( isAttribute(attributes, COMPONENT_MONITOR) ) {
                changeComponentMonitor(childContainer, createComponentMonitor(attributes));
            }
        }

        MutablePicoContainer decoratedPico = decorationDelegate.decorate(childContainer);
        if ( isAttribute(attributes, CLASS) )  {
            Class clazz = (Class) attributes.get(CLASS);
            return createNanoContainer(clazz, decoratedPico, parentClassLoader);
        } else {
            return new DefaultNanoContainer(parentClassLoader, decoratedPico);
        }
    }

    private void changeComponentMonitor(MutablePicoContainer childContainer, ComponentMonitor monitor) {
        if ( childContainer instanceof ComponentMonitorStrategy ){
            ((ComponentMonitorStrategy)childContainer).changeMonitor(monitor);
        }        
    }

    private NanoContainer createNanoContainer(Class clazz, MutablePicoContainer decoratedPico, ClassLoader parentClassLoader) {
        DefaultPicoContainer instantiatingContainer = new DefaultPicoContainer();
        instantiatingContainer.registerComponentInstance(ClassLoader.class, parentClassLoader);
        instantiatingContainer.registerComponentInstance(MutablePicoContainer.class, decoratedPico);
        instantiatingContainer.registerComponentImplementation(NanoContainer.class, clazz);
        Object componentInstance = instantiatingContainer.getComponentInstance(NanoContainer.class);
        return (NanoContainer) componentInstance;
    }

    private boolean isAttribute(Map attributes, String key) {        
        return attributes.containsKey(key) && attributes.get(key) != null;
    }

    private ComponentAdapterFactory createComponentAdapterFactory(Map attributes) {
        final ComponentAdapterFactory factory = (ComponentAdapterFactory) attributes.remove(COMPONENT_ADAPTER_FACTORY);
        if ( factory == null ){
            return new DefaultComponentAdapterFactory();
        }
        return factory;
    }
    
    private ComponentMonitor createComponentMonitor(Map attributes) {
        final ComponentMonitor monitor = (ComponentMonitor) attributes.remove(COMPONENT_MONITOR);
        if ( monitor == null ){
            return new DelegatingComponentMonitor();
        }
        return monitor;
    }

    protected NanoContainer createComponentClassLoader(NanoContainer parent) {
        return new DefaultNanoContainer(parent.getComponentClassLoader(), parent.getPico());
    }


    protected Object createBean(Map attributes) {
        Class type = (Class) attributes.remove(BEAN_CLASS);
        if (type == null) {
            throw new NanoContainerMarkupException("Bean must have a beanClass attribute");
        }
        try {
            Object bean = type.newInstance();
            // now let's set the properties on the bean
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                InvokerHelper.setProperty(bean, name, value);
            }
            return bean;
        } catch (IllegalAccessException e) {
            throw new NanoContainerMarkupException("Failed to create bean of type '" + type + "'. Reason: " + e, e);
        } catch (InstantiationException e) {
            throw new NanoContainerMarkupException("Failed to create bean of type " + type + "'. Reason: " + e, e);
        }
    }

    private Parameter[] getParameters(List paramsList) {
        if (paramsList == null) {
            return null;
        }
        int n = paramsList.size();
        Parameter[] parameters = new Parameter[n];
        for (int i = 0; i < n; ++i) {
            parameters[i] = toParameter(paramsList.get(i));
        }
        return parameters;
    }

    private Parameter toParameter(Object obj) {
        return obj instanceof Parameter ? (Parameter) obj : new ConstantParameter(obj);
    }

}
