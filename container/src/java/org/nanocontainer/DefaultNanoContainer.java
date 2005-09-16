/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.nanocontainer;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanocontainer.script.NanoContainerMarkupException;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.BeanPropertyComponentAdapter;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * The default implementation of {@link NanoContainer}.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
public class DefaultNanoContainer implements NanoContainer {
    private static final Map primitiveNameToBoxedName = new HashMap();
    static {
        primitiveNameToBoxedName.put("int", Integer.class.getName());
        primitiveNameToBoxedName.put("byte", Byte.class.getName());
        primitiveNameToBoxedName.put("short", Short.class.getName());
        primitiveNameToBoxedName.put("long", Long.class.getName());
        primitiveNameToBoxedName.put("float", Float.class.getName());
        primitiveNameToBoxedName.put("double", Double.class.getName());
        primitiveNameToBoxedName.put("boolean", Boolean.class.getName());
    }

    private final List classPathElements = new ArrayList();
    private final MutablePicoContainer picoContainer;
    private final ClassLoader parentClassLoader;

    private ClassLoader componentClassLoader;
    private boolean componentClassLoaderLocked;

    private static String getClassName(String primitiveOrClass) {
        String fromMap = (String) primitiveNameToBoxedName.get(primitiveOrClass);
        return fromMap != null ? fromMap : primitiveOrClass;
    }

    public DefaultNanoContainer(ClassLoader parentClassLoader, MutablePicoContainer picoContainer) {
        this.parentClassLoader = parentClassLoader;
        if (picoContainer == null) {
            throw new NullPointerException("picoContainer");
        }
        this.picoContainer = picoContainer;
    }

    public DefaultNanoContainer(ClassLoader parentClassLoader) {
        this(parentClassLoader, new DefaultPicoContainer());
    }

    public DefaultNanoContainer(MutablePicoContainer picoContainer) {
        this(Thread.currentThread().getContextClassLoader(), picoContainer);
    }

    public DefaultNanoContainer(NanoContainer parent) {
        this(parent.getComponentClassLoader(), new DefaultPicoContainer(parent.getPico()));
    }

    /**
     * Beware - no parent container and no parent classloader.
     */
    public DefaultNanoContainer() {
        this(Thread.currentThread().getContextClassLoader(), new DefaultPicoContainer());
    }

    public ComponentAdapter registerComponentImplementation(String componentImplementationClassName) throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
        return picoContainer.registerComponentImplementation(loadClass(componentImplementationClassName));
    }

    public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName) throws ClassNotFoundException {
        Class componentImplementation = loadClass(componentImplementationClassName);
        if (key instanceof ClassNameKey) {
            key = loadClass(((ClassNameKey) key).getClassName());
        }
        return picoContainer.registerComponentImplementation(key, componentImplementation);
    }


    public ComponentAdapter registerComponentImplementation(Object key, String componentImplementationClassName, Parameter[] parameters) throws ClassNotFoundException {
        Class componentImplementation = loadClass(componentImplementationClassName);
        if (key instanceof ClassNameKey) {
            key = loadClass(((ClassNameKey) key).getClassName());

        }
        return picoContainer.registerComponentImplementation(key, componentImplementation, parameters);
    }

    public ComponentAdapter registerComponentImplementation(Object key,
                                                            String componentImplementationClassName,
                                                            String[] parameterTypesAsString,
                                                            String[] parameterValuesAsString) throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
        Class componentImplementation = getComponentClassLoader().loadClass(componentImplementationClassName);
        if (key instanceof ClassNameKey) {
            key = loadClass(((ClassNameKey) key).getClassName());

        }
        return registerComponentImplementation(parameterTypesAsString, parameterValuesAsString, key, componentImplementation);
    }

    public ComponentAdapter registerComponentImplementation(String componentImplementationClassName,
                                                            String[] parameterTypesAsString,
                                                            String[] parameterValuesAsString) throws PicoRegistrationException, ClassNotFoundException, PicoIntrospectionException {
        Class componentImplementation = getComponentClassLoader().loadClass(componentImplementationClassName);
        return registerComponentImplementation(parameterTypesAsString, parameterValuesAsString, componentImplementation, componentImplementation);
    }

    private ComponentAdapter registerComponentImplementation(String[] parameterTypesAsString, String[] parameterValuesAsString, Object key, Class componentImplementation) throws ClassNotFoundException {
        Parameter[] parameters = new Parameter[parameterTypesAsString.length];
        for (int i = 0; i < parameters.length; i++) {
            Object value = BeanPropertyComponentAdapter.convert(parameterTypesAsString[i], parameterValuesAsString[i], getComponentClassLoader());
            parameters[i] = new ConstantParameter(value);
        }
        return picoContainer.registerComponentImplementation(key, componentImplementation, parameters);
    }

    private Class loadClass(final String className) throws ClassNotFoundException {
        ClassLoader classLoader = getComponentClassLoader();
        String cn = getClassName(className);
        return classLoader.loadClass(cn);
    }

    public ClassPathElement addClassLoaderURL(URL url) {
        if (componentClassLoaderLocked) throw new IllegalStateException("ClassLoader URLs cannot be added once this instance is locked");

        ClassPathElement classPathElement = new ClassPathElement(url);
        classPathElements.add(classPathElement);
        return classPathElement;
    }

    public ClassLoader getComponentClassLoader() {
        if (componentClassLoader == null) {
            //final URL[] urlz = (URL[]) urls.toArray(new URL[urls.size()]);
            componentClassLoaderLocked = true;
            componentClassLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new URLPrintingClassLoader(classPathElements, parentClassLoader);
                }
            });
        }
        return componentClassLoader;
    }

    public MutablePicoContainer getPico() {
        return picoContainer;
    }

    public static class URLPrintingClassLoader extends URLClassLoader {
        private final List classPathElements;
        private Map permissionsMap;

        public URLPrintingClassLoader(List classPathElements, ClassLoader parent) {
            super(getURLs(classPathElements), parent);
            this.classPathElements = classPathElements;
        }

        private static URL[] getURLs(List classPathElemelements) {
            final URL[] urlz = new URL[classPathElemelements.size()];
            for(int i = 0; i < urlz.length; i++) {
                urlz[i] = ((ClassPathElement) classPathElemelements.get(i)).getUrl();
            }
            return urlz;
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            try {
                return super.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw decorateCNFE(name, e);
            }
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                throw decorateCNFE(name, e);
            }
        }

        private ClassNotFoundException decorateCNFE(String name, ClassNotFoundException e) {
            ClassLoader classLoader = this;
            StringBuffer sb = new StringBuffer("'").append(name).append("' classloader stack [");
            while(classLoader != null) {
                sb.append(classLoader.toString()).append("\n");
                final ClassLoader cl = classLoader;
                classLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return cl.getParent();
                    }
                });

            }
            return new ClassNotFoundException(sb.append("]").toString() , e);
        }

        public String toString() {
            String result = super.toString();
            URL[] urls = getURLs();
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                result += "\n\t" + url.toString();
            }

            return result;
        }

        protected PermissionCollection getPermissions(CodeSource codeSource) {
            if (permissionsMap == null ) {
                permissionsMap = new HashMap();
                for (int i = 0; i < classPathElements.size(); i++) {
                    ClassPathElement cpe =  (ClassPathElement) classPathElements.get(i);
                    PermissionCollection permissionCollection = cpe.getPermissionCollection();
                    permissionsMap.put(cpe.getUrl(),permissionCollection);
                }
            }
            Permissions perms = (Permissions) permissionsMap.get(codeSource.getLocation());
            return (PermissionCollection) perms;
            //return super.getPermissions(codeSource);
        }
    }

    public Object getComponentInstanceOfType(String componentType) {
        try {
            Class compType = getComponentClassLoader().loadClass(componentType);
            Object componentInstance = picoContainer.getComponentInstanceOfType(compType);
            return componentInstance;
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException("Can't resolve class as type '" + componentType + "'");
        }
    }

}
