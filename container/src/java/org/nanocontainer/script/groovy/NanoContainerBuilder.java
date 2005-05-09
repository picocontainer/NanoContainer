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
import org.codehaus.groovy.runtime.InvokerHelper;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerBuilderDecorationDelegate;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.NullNanoContainerBuilderDecorationDelegate;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Builds trees of PicoContainers and Pico components using GroovyMarkup
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class NanoContainerBuilder extends BuilderSupport {

    private final NanoContainerBuilderDecorationDelegate nanoContainerBuilderDecorationDelegate;

    public NanoContainerBuilder(NanoContainerBuilderDecorationDelegate nanoContainerBuilderDecorationDelegate) {
        this.nanoContainerBuilderDecorationDelegate = nanoContainerBuilderDecorationDelegate;
    }

    public NanoContainerBuilder() {
        this(new NullNanoContainerBuilderDecorationDelegate());
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object doInvokeMethod(String s, Object name, Object args) {
        //TODO use setDelegate() from Groovy beta 7
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
        attributes.put("class", value);
        return createNode(name, attributes);
    }

    protected Object createNode(Object name, Map attributes) {
        Object current = getCurrent();
        if (current != null && current instanceof GroovyObject) {
            return createChildBuilder(current, name, attributes);
        } else if (current == null || current instanceof NanoContainer) {
            NanoContainer parent = (NanoContainer) current;
            Object parentAttribute = attributes.get("parent");
            if (parent != null && parentAttribute != null) {
                throw new NanoContainerMarkupException("You can't explicitly specify a parent in a child element.");
            }
            if (parent == null && (parentAttribute instanceof NanoContainer)) {
                // we're not in an enclosing scope - look at parent attribute instead
                parent = (NanoContainer) parentAttribute;
            }
            if (name.equals("container")) {
                return createChildContainer(attributes, parent);
            } else {
                try {
                    return createChildOfContainerNode(parent, name, attributes, current);
                } catch (ClassNotFoundException e) {
                    throw new NanoContainerMarkupException("ClassNotFoundException:" + e.getMessage(), e);
                }
            }
        } else {
            // we don't know how to handle it - delegate to the decorator.
            return nanoContainerBuilderDecorationDelegate.createNode(name, attributes, current);
        }
    }

    private Object createChildBuilder(Object current, Object name, Map attributes) {
        GroovyObject groovyObject = (GroovyObject) current;
        return groovyObject.invokeMethod(name.toString(), attributes);
    }

    private Object createChildOfContainerNode(NanoContainer parentContainer, Object name, Map attributes, Object current) throws ClassNotFoundException {
        if (name.equals("component")) {
            nanoContainerBuilderDecorationDelegate.rememberComponentKey(attributes);
            return createComponentNode(attributes, parentContainer, name);
        } else if (name.equals("bean")) {
            return createBeanNode(attributes, parentContainer.getPico());
        } else if (name.equals("classpathelement")) {
            return createClassPathElementNode(attributes, parentContainer);
        } else if (name.equals("doCall")) {
            // TODO ????
            //BuilderSupport builder = (BuilderSupport) attributes.remove("class");
            return null;

        } else if (name.equals("newBuilder")) {
            return createNewBuilderNode(attributes, parentContainer);
        } else {
            // we don't know how to handle it - delegate to the decorator.
            return nanoContainerBuilderDecorationDelegate.createNode(name, attributes, current);
        }
    }

    private Object createNewBuilderNode(Map attributes, NanoContainer parentContainer) {
        String builderClass = (String) attributes.remove("class");
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

    private Object createClassPathElementNode(Map attributes, NanoContainer nanoContainer) {

        String path = (String) attributes.remove("path");
        URL pathURL = null;
        try {
            if (path.toLowerCase().startsWith("http://")) {
                pathURL = new URL(path);
            } else {
                pathURL = new File(path).toURL();
            }
        } catch (MalformedURLException e) {
            throw new NanoContainerMarkupException("classpath '" + path + "' malformed ", e);
        }
        nanoContainer.addClassLoaderURL(pathURL);
        return pathURL;
    }

    private Object createBeanNode(Map attributes, MutablePicoContainer pico) {
        // lets create a bean
        Object answer = createBean(attributes);
        pico.registerComponentInstance(answer);
        return answer;
    }

    private Object createComponentNode(Map attributes, NanoContainer nano, Object name) throws ClassNotFoundException {
        Object key = attributes.remove("key");
        Object classValue = attributes.remove("class");
        Object instance = attributes.remove("instance");
        List parameters = (List) attributes.remove("parameters");

        Parameter[] parameterArray = getParameters(parameters);
        if (classValue instanceof Class) {
            Class clazz = (Class) classValue;
            key = key == null ? clazz : key;
            MutablePicoContainer pico = nano.getPico();
            pico.registerComponentImplementation(key, clazz, parameterArray);
        } else if (classValue instanceof String) {
            String className = (String) classValue;
            key = key == null ? className : key;
            nano.registerComponentImplementation(key, className, parameterArray);
        } else if (instance != null) {
            key = key == null ? instance.getClass() : key;
            nano.getPico().registerComponentInstance(key, instance);
        } else {
            throw new NanoContainerMarkupException("Must specify a class attribute for a component. Attributes:" + attributes);
        }

        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        return createNode(name, attributes);
    }

    protected NanoContainer createChildContainer(Map attributes, NanoContainer parent) {
        ComponentAdapterFactory componentAdapterFactory = (ComponentAdapterFactory) attributes.remove("componentAdapterFactory");
        componentAdapterFactory = componentAdapterFactory != null ? componentAdapterFactory : new DefaultComponentAdapterFactory();
        ComponentAdapterFactory wrappedComponentAdapterFactory = nanoContainerBuilderDecorationDelegate.decorate(componentAdapterFactory, attributes);

        ClassLoader parentClassLoader = null;
        MutablePicoContainer wrappedPicoContainer = null;
        if (parent != null) {
            parentClassLoader = parent.getComponentClassLoader();
            wrappedPicoContainer = new DefaultPicoContainer(wrappedComponentAdapterFactory, parent.getPico());
            parent.getPico().addChildContainer(wrappedPicoContainer);
        } else {
            parentClassLoader = Thread.currentThread().getContextClassLoader();
            wrappedPicoContainer = new DefaultPicoContainer(wrappedComponentAdapterFactory);
        }

        MutablePicoContainer decoratedPico = nanoContainerBuilderDecorationDelegate.decorate(wrappedPicoContainer);

        return new DefaultNanoContainer(parentClassLoader, decoratedPico);
    }

    protected Object createBean(Map attributes) {
        Class type = (Class) attributes.remove("beanClass");
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
