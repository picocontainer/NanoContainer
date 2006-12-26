/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.nanocontainer.script.jython;

import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.testmodel.WebServer;
import org.nanocontainer.testmodel.WebServerImpl;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class JythonContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testSimpleConfigurationIsPossible() {
        Reader script = new StringReader("from org.nanocontainer.testmodel import *\n" +
                "pico = DefaultNanoPicoContainer()\n" +
                "pico.registerComponentImplementation(WebServerImpl)\n" +
                "pico.registerComponentImplementation(DefaultWebServerConfig)\n");

        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponentInstanceOfType(WebServer.class));
    }

    public void testDependenciesAreUnsatisfiableByChildContainers() throws IOException, ClassNotFoundException, PicoCompositionException {
        try {
            Reader script = new StringReader("" +
                    "from org.nanocontainer.testmodel import *\n" +
                    "pico = DefaultNanoPicoContainer()\n" +
                    "pico.registerComponentImplementation(WebServerImpl)\n" +
                    "childContainer = DefaultNanoPicoContainer(pico)\n" +
                    "childContainer.registerComponentImplementation(DefaultWebServerConfig)\n");
            PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
            pico.getComponentInstanceOfType(WebServer.class);
            fail();
        } catch (UnsatisfiableDependenciesException expected) {
        }
    }

    public void testDependenciesAreSatisfiableByParentContainer() throws IOException, ClassNotFoundException, PicoCompositionException {
        Reader script = new StringReader("" +
                "from org.nanocontainer.testmodel import *\n" +
                "pico = DefaultNanoPicoContainer()\n" +
                "pico.registerComponentImplementation(DefaultWebServerConfig)\n" +
                "child = pico.makeChildContainer()\n" +
                "child.registerComponentImplementation(WebServerImpl)\n" +
                "pico.registerComponentInstance('wayOfPassingSomethingToTestEnv', child.getComponentInstance(WebServerImpl))");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        WebServerImpl wsi = (WebServerImpl) pico.getComponentInstance("wayOfPassingSomethingToTestEnv");
        assertNotNull(wsi);
    }

    public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "pico = DefaultNanoPicoContainer(parent)\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //pico.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
    }

}
