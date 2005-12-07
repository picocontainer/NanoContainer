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
 * @todo I'm sure there has got to be a better method to overriding builders
 * than a static code block.  But since resolution takes place during construction,
 * it makes it more of an issue. -MR
 */
public class ScriptedContainerBuilderFactory {

    public static final String GROOVY = ".groovy";
    public static final String BEANSHELL = ".bsh";
    public static final String JAVASCRIPT = ".js";
    public static final String JYTHON = ".py";
    public static final String XML = ".xml";

    public static final String DEFAULT_GROOVY_BUILDER = "org.nanocontainer.script.groovy.GroovyContainerBuilder";
    public static final String DEFAULT_BEANSHELL_BUILDER = "org.nanocontainer.script.bsh.BeanShellContainerBuilder";
    public static final String DEFAULT_JAVASCRIPT_BUILDER = "org.nanocontainer.script.rhino.JavascriptContainerBuilder";
    public static final String DEFAULT_XML_BUILDER = "org.nanocontainer.script.xml.XMLContainerBuilder";
    public static final String DEFAULT_JYTHON_BUILDER = "org.nanocontainer.script.jython.JythonContainerBuilder";


    private static final Map extensionToBuilders = new HashMap();

    static {
        resetBuilders();
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
        DefaultNanoContainer defaultNanoContainer;
        {
            // transient.
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
            defaultNanoContainer = new DefaultNanoContainer(factory);
        }
        ComponentAdapter componentAdapter = defaultNanoContainer.registerComponentImplementation(builderClass);
        containerBuilder = (ScriptedContainerBuilder) componentAdapter.getComponentInstance(defaultNanoContainer.getPico());
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

    /**
     * Retrieve the classname of the builder to use given the provided
     * extension.  Example:
     * <code><pre>
     * String groovyBuilderName = ScriptedContainerBuilderFactory.getBuilderClassName(".groovy");
     * assert "org.nanocontainer.script.groovy.GroovyContainerBuilder".equals(groovyBuilderName);
     * </pre></code>
     * @param extension String
     * @return String
     */
    public static String getBuilderClassName(final String extension) {
        String resultingBuilderClassName = null;
        synchronized(extensionToBuilders) {
            resultingBuilderClassName = (String)extensionToBuilders.get(extension);
        }
        return resultingBuilderClassName;
    }


    /**
     * Function to allow the resetting of the builder map to defaults.  Allows
     * testing of the static resource a bit better.
     */
    public static void resetBuilders() {
        synchronized(extensionToBuilders) {
            extensionToBuilders.clear();

            //This is a bit clunky compared to just registering the items
            //directly into the map, but this way IMO it provides a single access
            //point into the extensionToBuilders map.
            registerBuilder(GROOVY, DEFAULT_GROOVY_BUILDER );
            registerBuilder(BEANSHELL, DEFAULT_BEANSHELL_BUILDER);
            registerBuilder(JAVASCRIPT, DEFAULT_JAVASCRIPT_BUILDER);
            registerBuilder(XML, DEFAULT_XML_BUILDER);
            registerBuilder(JYTHON, DEFAULT_JYTHON_BUILDER);
        }

    }

    /**
     * Registers/replaces a new handler for a given extension.  Allows for customizable
     * behavior in the various builders or the possibility to dynamically add
     * handlers for new file types.  Example:
     * <code><pre>
     * ScriptedContainerBuilderFactory.registerBuilder(".groovy", "org.nanocontainer.script.groovy.GroovyContainerBuilder");
     * </pre></code>
     * <p>The internal code now requires synchronization of the builder extension map since
     * who knows what is using it when a new builder is registered.</p>
     * @param extension String the extension to register under.
     * @param className String the classname to use for the given extension.
     */
    public static void registerBuilder(final String extension, final String className) {
        synchronized(extensionToBuilders) {
            extensionToBuilders.put(extension,className);
        }
    }

    /**
     * Retrieve a list of all supported extensions.
     * @return String[] of extensions including the period in the name.
     */
    public static String[] getAllSupportedExtensions() {
        String[] resultingArrayOfExtensions;
        synchronized(extensionToBuilders) {
            resultingArrayOfExtensions = (String[])extensionToBuilders.keySet().toArray(new String[extensionToBuilders.size()]);
        }

        return resultingArrayOfExtensions;
    }

    private static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public ScriptedContainerBuilder getContainerBuilder() {
        return containerBuilder;
    }

}
