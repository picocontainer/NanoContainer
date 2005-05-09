/*****************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.nanocontainer.script.groovy;

import org.nanocontainer.aop.Dao;
import org.nanocontainer.aop.Identifiable;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.PicoContainer;

import java.io.StringReader;

/**
 * @author Stephen Molitor
 * @author Paul n Aslak (pairing)
 */
public class NanoContainerBuilderAopTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testContainerScopedInterceptor() {
        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "" +
                "log = new StringBuffer()\n" +
                "logger = new LoggingInterceptor(log)\n" +
                "\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +
                "nano = builder.container() {\n" +
                "    aspect(classCut:cuts.instancesOf(Dao.class), methodCut:cuts.allMethods(), interceptor:logger)\n" +
                "    component(key:Dao, class:DaoImpl)\n" +
                "    component(key:StringBuffer, instance:log)\n" +
                "}\n";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);

        StringBuffer log = (StringBuffer) pico.getComponentInstance(StringBuffer.class);
        verifyIntercepted(dao, log);
    }

    public void testContainerScopedPointcutWithNestedAdvices() {
        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "" +
                "log = new StringBuffer()\n" +
                "logger = new LoggingInterceptor(log)\n" +
                "\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +
                "nano = builder.container() {\n" +
                "    pointcut(classCut:cuts.instancesOf(Dao.class), methodCut:cuts.allMethods()) {\n" +
                "        aspect(interceptor:logger)\n" +
                "    }\n" +
                "    component(key:Dao, class:DaoImpl)\n" +
                "    component(key:StringBuffer, instance:log)\n" +
                "}\n";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);

        StringBuffer log = (StringBuffer) pico.getComponentInstance(StringBuffer.class);
        verifyIntercepted(dao, log);
    }

    public void testContainerScopedContainerSuppliedInterceptor() {

        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +
                "nano = builder.container() {\n" +
                "    aspect(classCut:cuts.instancesOf(Dao), methodCut:cuts.allMethods(), interceptorKey:LoggingInterceptor)\n" +
                "    component(key:'log', class:StringBuffer)\n" +
                "    component(LoggingInterceptor)\n" +
                "    component(key:Dao, class:DaoImpl)\n" +
                "}";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);
        StringBuffer log = (StringBuffer) pico.getComponentInstance("log");
        verifyIntercepted(dao, log);
    }

    public void testComponentScopedInterceptor() {

        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "log = new StringBuffer()\n" +
                "logger = new LoggingInterceptor(log)\n" +
                "\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +
                "nano = builder.container() {\n" +
                "    component(key:'intercepted', class:DaoImpl) {\n" +
                "        aspect(methodCut:cuts.allMethods(), interceptor:logger)\n" +
                "    }\n" +
                "    component(key:'log', instance:log)\n" +
                "    component(key:'notIntercepted', class:DaoImpl)\n" +
                "}\n";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");


        Dao intercepted = (Dao) pico.getComponentInstance("intercepted");
        Dao notIntercepted = (Dao) pico.getComponentInstance("notIntercepted");
        StringBuffer log = (StringBuffer) pico.getComponentInstance("log");

        verifyIntercepted(intercepted, log);
        verifyNotIntercepted(notIntercepted, log);
    }

    public void testContainerScopedMixin() {

        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +
                "nano = builder.container() {\n" +
                "    component(key:Dao, class:DaoImpl) \n" +
                "    aspect(classCut:cuts.instancesOf(Dao), mixinClass:IdentifiableMixin)\n" +
                "}";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);
        verifyMixin(dao);
    }

    public void testExplicitAspectsManagerAndDecorationDelegate() {

        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "aspectsManager = new org.nanocontainer.aop.dynaop.DynaopAspectsManager()\n" +
                "cuts = aspectsManager.getPointcutsFactory()\n" +
                "decorator = new org.nanocontainer.aop.defaults.AopDecorationDelegate(aspectsManager)\n" +
                "builder = new NanoContainerBuilder(decorator) \n" +
                "nano = builder.container() {\n" +
                "    component(key:Dao, class:DaoImpl) \n" +
                "    aspect(classCut:cuts.instancesOf(Dao), mixinClass:IdentifiableMixin)\n" +
                "}";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);
        verifyMixin(dao);
    }

    public void testScriptSuppliedCaf() {

        String script = "" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.nanocontainer.aop.*\n" +
                "import org.nanocontainer.aop.dynaop.*\n" +
                "log = new StringBuffer()\n" +
                "logger = new LoggingInterceptor(log)\n" +
                "caf = new org.picocontainer.defaults.DefaultComponentAdapterFactory()\n" +
                "\n" +
                "cuts = new DynaopPointcutsFactory()\n" +
                "builder = new DynaopNanoContainerBuilder()\n" +                
                "nano = builder.container(adapterFactory:caf) {\n" +
                "    aspect(classCut:cuts.instancesOf(Dao.class), methodCut:cuts.allMethods(), interceptor:logger)\n" +
                "    component(key:Dao, class:DaoImpl)\n" +
                "    component(key:'log', instance:log)\n" +
                "}";

        GroovyContainerBuilder builder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());

        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Dao dao = (Dao) pico.getComponentInstance(Dao.class);
        StringBuffer log = (StringBuffer) pico.getComponentInstance("log");

        verifyIntercepted(dao, log);

        //TODO - check that caf is the one actually used.

    }

    private void verifyIntercepted(Dao dao, StringBuffer log) {
        String before = log.toString();
        String data = dao.loadData();
        assertEquals("data", data);
        assertEquals(before + "startend", log.toString());
    }

    private void verifyNotIntercepted(Dao dao, StringBuffer log) {
        String before = log.toString();
        String data = dao.loadData();
        assertEquals("data", data);
        assertEquals(before, log.toString());
    }

    private void verifyMixin(Object component) {
        assertTrue(component instanceof Identifiable);
        Identifiable identifiable = (Identifiable) component;
        identifiable.setId("id");
        assertEquals("id", identifiable.getId());
    }

    private void verifyNoMixin(Object component) {
        assertFalse(component instanceof Identifiable);
    }

}