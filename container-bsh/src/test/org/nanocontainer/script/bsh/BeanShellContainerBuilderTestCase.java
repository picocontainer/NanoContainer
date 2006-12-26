/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/

/**
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @version $Revision$
 */
package org.nanocontainer.script.bsh;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;

import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class BeanShellContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "pico = new org.nanocontainer.reflection.DefaultNanoPicoContainer(parent);\n" +
                "pico.registerComponentInstance(\"hello\", \"BeanShell\");\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new BeanShellContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("BeanShell", pico.getComponentInstance("hello"));
    }

    /**
     * The following test requires the next beta of beanshell to work.
     * @todo Upgrade Maven1 Builds to Beanshell 2.0b4 so that this test case
     * can run.
     * @throws IOException
     */
    public void testWithParentClassPathPropagatesWithToBeanShellInterpreter()throws IOException {
        Reader script = new StringReader("" +
            "try {\n" +
            "    getClass(\"TestComp\");\n" +
            "} catch (ClassNotFoundException ex) {\n" +
            "     ClassLoader current = this.getClass().getClassLoader(); \n" +
            "     print(current.toString());\n" +
            "     print(current.getParent().toString());\n" +
            "     print(\"Failed ClassLoading: \");\n" +
            "     ex.printStackTrace();\n" +
            "}\n" +
                "pico = new org.nanocontainer.reflection.DefaultNanoPicoContainer(getClass(\"TestComp\").getClassLoader(), parent);\n" +
                "pico.registerComponentImplementation( \"TestComp\" );\n");

        File testCompJar = new File(System.getProperty("testcomp.jar"));
        URLClassLoader classLoader = new URLClassLoader(new URL[] {testCompJar.toURL()}, this.getClass().getClassLoader());
        Class testComp = null;
        PicoContainer parent = new DefaultPicoContainer();

        try {
            testComp = classLoader.loadClass("TestComp");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            fail("Unable to load test component from the jar using a url classloader");
        }

        PicoContainer pico = buildContainer(new BeanShellContainerBuilder(script, classLoader), parent, "SOME_SCOPE");
        assertNotNull(pico);
        Object testCompInstance = pico.getComponentInstance(testComp);
        assertNotNull(testCompInstance);
        assertEquals(testCompInstance.getClass().getName(), testComp.getName());

    }

}
