/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script;

import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The main class for configuration of PicoContainer with various scripting languages.
 * When using the constructors taking a file, the extensions must be one of the following:
 * <ul>
 * <li>.groovy</li>
 * <li>.bsh</li>
 * <li>.js</li>
 * <li>.py</li>
 * <li>.xml</li>
 * </ul>
 * -And the content of the file likewise. See <a href="http://docs.codehaus.org/display/NANO/NanoContainer">NanoContainer documentation</a>
 * for details.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslah;y
 * @author Obie Fernandez
 */
public class ScriptedContainerBuilderFactory {

    public static final String GROOVY = ".groovy";
    public static final String BEANSHELL = ".bsh";
    public static final String JAVASCRIPT = ".js";
    public static final String JYTHON = ".py";
    public static final String XML = ".xml";

    private static final Map extensionToBuilders = new HashMap();

    static {
        extensionToBuilders.put(GROOVY, "org.nanocontainer.script.groovy.GroovyContainerBuilder");
        extensionToBuilders.put(BEANSHELL, "org.nanocontainer.script.bsh.BeanShellContainerBuilder");
        extensionToBuilders.put(JAVASCRIPT, "org.nanocontainer.script.rhino.JavascriptContainerBuilder");
        extensionToBuilders.put(XML, "org.nanocontainer.script.xml.XMLContainerBuilder");
        extensionToBuilders.put(JYTHON, "org.nanocontainer.script.jython.JythonContainerBuilder");
    }

    private ScriptedContainerBuilder containerBuilder;

    public ScriptedContainerBuilderFactory(File compositionFile, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        this(new FileReader(fileExists(compositionFile)), getBuilderClassName(compositionFile), classLoader);
    }

    public ScriptedContainerBuilderFactory(File compositionFile) throws IOException, ClassNotFoundException {
        this(new FileReader(fileExists(compositionFile)), getBuilderClassName(compositionFile), Thread.currentThread().getContextClassLoader());
    }

    public ScriptedContainerBuilderFactory(URL compositionURL) throws ClassNotFoundException {
        this(compositionURL, getBuilderClassName(compositionURL), Thread.currentThread().getContextClassLoader());
    }

    public ScriptedContainerBuilderFactory(Reader composition, String builderClass) throws ClassNotFoundException {
        this(composition, builderClass, Thread.currentThread().getContextClassLoader());
    }

    public ScriptedContainerBuilderFactory(Reader composition, String builderClass, ClassLoader classLoader) throws ClassNotFoundException {
        createContainerBuilder(composition, classLoader, builderClass);
    }

    public ScriptedContainerBuilderFactory(URL compositionURL, String builderClassName, ClassLoader contextClassLoader) throws ClassNotFoundException {
        createContainerBuilder(compositionURL, contextClassLoader, builderClassName);
    }

    private void createContainerBuilder(Object composition, ClassLoader classLoader, String builderClass) throws ClassNotFoundException {
        DefaultNanoContainer defaultReflectionContainerAdapter;
        {
            // disposable.
            DefaultPicoContainer factory = new DefaultPicoContainer();
            if(composition == null) {
                throw new NullPointerException("composition can't be null");
            }
            factory.registerComponentInstance(composition);

            if(classLoader == null) {
                // on some weird JVMs (like jeode) Thread.currentThread().getContextClassLoader() returns null !?!?
                classLoader = getClass().getClassLoader();
            }
            factory.registerComponentInstance(classLoader);
            defaultReflectionContainerAdapter = new DefaultNanoContainer(factory);
        }
        ComponentAdapter componentAdapter = defaultReflectionContainerAdapter.registerComponentImplementation(builderClass);
        containerBuilder = (ScriptedContainerBuilder) componentAdapter.getComponentInstance(defaultReflectionContainerAdapter.getPico());
    }

    private static File fileExists(File file) throws FileNotFoundException {
        if (file.exists()) {
            return file;
        } else {
            //todo a proper exception.
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist.");
        }
    }

    private static String getBuilderClassName(File compositionFile) {
        String language = getExtension(compositionFile.getAbsolutePath());
        return getBuilderClassName(language);
    }

    private static String getBuilderClassName(URL compositionURL) {
        String language = getExtension(compositionURL.getFile());
        return getBuilderClassName(language);
    }

    public static String getBuilderClassName(String extension) {
        return (String) extensionToBuilders.get(extension);
    }

    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public ScriptedContainerBuilder getContainerBuilder() {
        return containerBuilder;
    }

}
