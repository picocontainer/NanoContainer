/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.nanocontainer.script.rhino;

import org.mozilla.javascript.JavaScriptException;
import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.testmodel.WebServer;
import org.nanocontainer.testmodel.WebServerConfig;
import org.nanocontainer.TestHelper;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JavascriptContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testInstantiateBasicScriptable() throws IOException, ClassNotFoundException, PicoCompositionException, JavaScriptException {

        Reader script = new StringReader("" +
                "var pico = new DefaultNanoPicoContainer()\n" +
                "pico.registerComponentImplementation(Packages.org.nanocontainer.testmodel.DefaultWebServerConfig)\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        assertNotNull(pico.getComponentInstanceOfType(WebServerConfig.class).getClass());
    }

    public void testInstantiateWithBespokeComponentAdapter() throws IOException, ClassNotFoundException, PicoCompositionException, JavaScriptException {

        Reader script = new StringReader("" +
                "var pico = new DefaultNanoPicoContainer(new ConstructorInjectionComponentAdapterFactory())\n" +
                "pico.registerComponentImplementation(Packages.org.nanocontainer.testmodel.DefaultWebServerConfig)\n" +
                "pico.registerComponentImplementation(Packages.org.nanocontainer.testmodel.WebServerImpl)\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Object wsc = pico.getComponentInstanceOfType(WebServerConfig.class);
        Object ws1 = pico.getComponentInstanceOfType(WebServer.class);
        Object ws2 = pico.getComponentInstanceOfType(WebServer.class);

        assertNotSame(ws1, ws2);

        assertEquals("ClassLoader should be the same for both components", ws1.getClass().getClassLoader(), wsc.getClass().getClassLoader());
    }

    public void testClassLoaderHierarchy() throws ClassNotFoundException, IOException, PicoCompositionException, JavaScriptException {

        File testCompJar = TestHelper.getTestCompJarFile();
        assertTrue(testCompJar.isFile());

        final String testCompJarPath = testCompJar.getCanonicalPath().replace('\\', '/');
        Reader script = new StringReader(
                "var pico = new DefaultNanoPicoContainer()\n" +
                "pico.registerComponentImplementation('parentComponent', Packages." + FooTestComp.class.getName() + ")\n" +
                "child = new DefaultNanoPicoContainer(pico)\n" +
                "pico.addChildContainer(child)\n" +
                "url = new File('" + testCompJarPath + "').toURL()\n" +
                "child.addClassLoaderURL(url)\n" +
                "child.registerComponentImplementation('childComponent','TestComp')\n" +
                "pico.registerComponentInstance('wayOfPassingSomethingToTestEnv', child.getComponentInstance('childComponent'))"); // ugly hack for testing
        JavascriptContainerBuilder builder = new JavascriptContainerBuilder(script, getClass().getClassLoader());
        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Object parentComponent = pico.getComponentInstance("parentComponent");

        Object childComponent = pico.getComponentInstance("wayOfPassingSomethingToTestEnv");

        ClassLoader classLoader1 = parentComponent.getClass().getClassLoader();
        ClassLoader classLoader2 = childComponent.getClass().getClassLoader();
        assertNotSame(classLoader1, classLoader2);
        /*
        system cl -> loads FooTestComp
          parent container cl
            child container cl -> loads TestComp
        */
        assertSame(parentComponent.getClass().getClassLoader(), childComponent.getClass().getClassLoader().getParent());
    }

    public void testRegisterComponentInstance() throws JavaScriptException, IOException {
        Reader script = new StringReader("" +
                "var pico = new DefaultNanoPicoContainer()\n" +
                "pico.registerComponentInstance( new Packages." + FooTestComp.class.getName() + "())\n" +
                "pico.registerComponentInstance( 'foo', new Packages." + FooTestComp.class.getName() + "())\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        assertEquals(FooTestComp.class, pico.getComponentInstances().get(0).getClass());
        assertEquals(FooTestComp.class, pico.getComponentInstances().get(1).getClass());
    }

    public static class FooTestComp {

    }

    public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "var pico = new DefaultNanoPicoContainer(parent)\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
    }
}
