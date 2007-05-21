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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;

import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.script.BarDecoratingPicoContainer;
import org.nanocontainer.script.FooDecoratingPicoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.testmodel.CustomerEntityImpl;
import org.nanocontainer.testmodel.DefaultWebServerConfig;
import org.nanocontainer.testmodel.Entity;
import org.nanocontainer.testmodel.ListSupport;
import org.nanocontainer.testmodel.MapSupport;
import org.nanocontainer.testmodel.OrderEntityImpl;
import org.nanocontainer.testmodel.WebServerConfig;
import org.nanocontainer.testmodel.WebServerConfigComp;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.monitors.WriterComponentMonitor;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jeppe Cramon
 * @author Mauro Talevi
 * @version $Revision$
 */
public class XMLContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    //TODO some tests for XMLContainerBuilder that use a classloader that is retrieved at testtime.
    // i.e. not a programatic consequence of this.getClass().getClassLoader()

    public void testCreateSimpleContainer() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='java.lang.StringBuffer'/>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertEquals(3, pico.getComponentInstances().size());
        assertNotNull(pico.getComponentInstance(StringBuffer.class));
        assertNotNull(pico.getComponentInstance(DefaultWebServerConfig.class));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServer"));
    }

    public void testCreateSimpleContainerWithExplicitKeysAndParameters() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation key='aBuffer' class='java.lang.StringBuffer'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServerConfig' class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'>" +
                " 		<parameter key='org.nanocontainer.testmodel.WebServerConfig'/>" +
                " 		<parameter key='aBuffer'/>" +
                "  </component-implementation>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertEquals(3, pico.getComponentInstances().size());
        assertNotNull(pico.getComponentInstance("aBuffer"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServerConfig"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServer"));
    }

    public void testCreateSimpleContainerWithExplicitKeysAndImplicitParameter() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation key='aBuffer' class='java.lang.StringBuffer'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServerConfig' class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'>" +
                "       <parameter/>" +
                "       <parameter key='aBuffer'/>" +
                "  </component-implementation>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertEquals(3, pico.getComponentInstances().size());
        assertNotNull(pico.getComponentInstance("aBuffer"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServerConfig"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServer"));
    }

    public void testNonParameterElementsAreIgnoredInComponentImplementation() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation key='aBuffer' class='java.lang.StringBuffer'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServerConfig' class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'>" +
                " 		<parameter key='org.nanocontainer.testmodel.WebServerConfig'/>" +
                " 		<parameter key='aBuffer'/>" +
                " 		<any-old-stuff/>" +
                "  </component-implementation>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertEquals(3, pico.getComponentInstances().size());
        assertNotNull(pico.getComponentInstance("aBuffer"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServerConfig"));
        assertNotNull(pico.getComponentInstance("org.nanocontainer.testmodel.WebServer"));
    }

    public void testContainerCanHostAChild() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "  <component-implementation class='java.lang.StringBuffer'/>" +
                "  <container>" +
                "    <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'/>" +
                "  </container>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance(DefaultWebServerConfig.class));

        StringBuffer sb = (StringBuffer) pico.getComponentInstance(StringBuffer.class);
        assertTrue(sb.toString().indexOf("-WebServerImpl") != -1);
    }

    public void testClassLoaderHierarchy() throws IOException {
        String testcompJarFileName = System.getProperty("testcomp.jar", "src/test-comp/TestComp.jar");
        // Paul's path to TestComp. PLEASE do not take out.
        //testcompJarFileName = "D:/OSS/PN/java/nanocontainer/src/test-comp/TestComp.jar";
        File testCompJar = new File(testcompJarFileName);
        assertTrue("The testcomp.jar system property should point to java/nanocontainer/src/test-comp/TestComp.jar", testCompJar.isFile());
        File testCompJar2 = new File(testCompJar.getParentFile(), "TestComp2.jar");
        File notStartableJar = new File(testCompJar.getParentFile(), "NotStartable.jar");

        assertTrue(testCompJar.isFile());
        assertTrue(testCompJar2.isFile());

        Reader script = new StringReader("" +
                "<container>" +
                "  <classpath>" +
                "    <element file='" + testCompJar.getCanonicalPath() + "'>" +
                "      <grant classname='java.io.FilePermission' context='*' value='read' />" +
                "    </element>" +
                "  </classpath>" +
                "  <component-implementation key='foo' class='TestComp'/>" +
                "  <container>" +
                "    <classpath>" +
                "      <element file='" + testCompJar2.getCanonicalPath() + "'/>" +
                "      <element file='" + notStartableJar.getCanonicalPath() + "'/>" +
                "    </classpath>" +
                "    <component-implementation key='bar' class='TestComp2'/>" +
                "    <component-implementation key='phony' class='NotStartable'/>" +
                "  </container>" +
                "  <component-implementation class='java.lang.StringBuffer'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object fooTestComp = pico.getComponentInstance("foo");
        assertNotNull("Container should have a 'foo' component", fooTestComp);

        StringBuffer sb = (StringBuffer) pico.getComponentInstance(StringBuffer.class);
        assertTrue("Container should have instantiated a 'TestComp2' component because it is Startable", sb.toString().indexOf("-TestComp2") != -1);
        // We are using the DefaultLifecycleManager, which only instantiates Startable components, and not non-Startable components.
        assertTrue("Container should NOT have instantiated a 'NotStartable' component because it is NOT Startable", sb.toString().indexOf("-NotStartable") == -1);
    }

    public void testUnknownclassThrowsNanoContainerMarkupException() {
        try {
            Reader script = new StringReader("" +
                    "<container>" +
                    "  <component-implementation class='Foo'/>" +
                    "</container>");
            buildContainer(script);
            fail();
        } catch (NanoContainerMarkupException expected) {
            assertTrue(expected.getCause() instanceof ClassNotFoundException);
        }
    }

    public void testNoImplementationClassThrowsNanoContainerMarkupException() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation/>" +
                "</container>");
        try {
            buildContainer(script);
        } catch (NanoContainerMarkupException expected) {
            assertEquals("'class' attribute not specified for component-implementation", expected.getMessage());
        }
    }

    public void testConstantParameterWithNoChildElementThrowsNanoContainerMarkupException() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                " 		<parameter>" +
                "		</parameter>" +
                "  </component-implementation>" +
                "</container>");

        try {
            buildContainer(script);
        } catch (NanoContainerMarkupException e) {
            assertEquals("parameter needs a child element", e.getMessage());
        }
    }

    public void testEmptyScriptDoesNotThrowsEmptyCompositionException() {
        Reader script = new StringReader("<container/>");
        buildContainer(script);
    }

    public void testCreateContainerFromScriptThrowsSAXException() {
        Reader script = new StringReader("" +
                "<container component-adapter-factory='" + ConstructorInjectionComponentAdapterFactory.class.getName() + "'>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "<container>");
        try {
            buildContainer(script);
        } catch (NanoContainerMarkupException e) {
            assertTrue("SAXException", e.getCause() instanceof SAXException);
        }
    }

    public void testCreateContainerFromNullScriptThrowsNullPointerException() {
        Reader script = null;
        try {
            buildContainer(script);
            fail("NullPointerException expected");
        } catch (NullPointerException expected) {
        }
    }

    public void testShouldThrowExceptionForNonExistantCafClass() {
        Reader script = new StringReader("" +
                "<container component-adapter-factory='org.nanocontainer.SomeInexistantFactory'>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "</container>");
        try {
            buildContainer(script);
            fail();
        } catch (NanoContainerMarkupException expected) {
            assertTrue("Message of exception does not contain missing class", expected.getMessage().indexOf("org.nanocontainer.SomeInexistantFactory") > 0);
        }
    }

    public void testComponentInstanceWithNoChildElementThrowsNanoContainerMarkupException() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance>" +
                "  </component-instance>" +
                "</container>");

        try {
            buildContainer(script);
            fail();
        } catch (NanoContainerMarkupException expected) {
            assertEquals("component-instance needs a child element", expected.getMessage());
        }
    }

    public void testComponentInstanceWithFactoryCanBeUsed() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance factory='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$TestFactory'>" +
                "    <config-or-whatever/>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object instance = pico.getComponentInstances().get(0);
        assertNotNull(instance);
        assertTrue(instance instanceof String);
        assertEquals("Hello", instance.toString());
    }

    public void testComponentInstanceWithDefaultFactory() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object instance = pico.getComponentInstances().get(0);
        assertNotNull(instance);
        assertTrue(instance instanceof TestBean);
        assertEquals(10, ((TestBean) instance).getFoo());
        assertEquals("hello", ((TestBean) instance).getBar());
    }

    public void testComponentInstanceWithBeanFactory() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance factory='org.nanocontainer.script.xml.BeanComponentInstanceFactory'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object instance = pico.getComponentInstances().get(0);
        assertNotNull(instance);
        assertTrue(instance instanceof TestBean);
        assertEquals(10, ((TestBean) instance).getFoo());
        assertEquals("hello", ((TestBean) instance).getBar());
    }

    public void testComponentInstanceWithBeanFactoryAndInstanceThatIsDefinedInContainer() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance key='date' factory='org.nanocontainer.script.xml.BeanComponentInstanceFactory'>" +
                "    <java.util.Date>" +
                "       <time>0</time>" +
                "    </java.util.Date>" +
                "  </component-instance>" +
                "  <component-instance factory='org.nanocontainer.script.xml.BeanComponentInstanceFactory'>" +
                "    <java.text.SimpleDateFormat>" +
                "       <lenient>false</lenient>" +
                "       <date name='2DigitYearStart'>date</date>" +
                "    </java.text.SimpleDateFormat>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object instance = pico.getComponentInstance(SimpleDateFormat.class);
        assertNotNull(instance);
        assertTrue(instance instanceof SimpleDateFormat);
        SimpleDateFormat format = ((SimpleDateFormat) instance);
        assertFalse(format.isLenient());
        assertEquals(new Date(0), format.get2DigitYearStart());
    }

    public void testComponentInstanceWithKey() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance key='aString'>" +
                "    <string>Hello</string>" +
                "  </component-instance>" +
                "" +
                "  <component-instance key='aLong'>" +
                "    <long>22</long>" +
                "  </component-instance>" +
                "" +
                "  <component-instance key='aButton'>" +
                "    <javax.swing.JButton>" +
                "      <text>Hello</text>" +
                "      <alignmentX>0.88</alignmentX>" +
                "    </javax.swing.JButton>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertEquals("Hello", pico.getComponentInstance("aString"));
        assertEquals(new Long(22), pico.getComponentInstance("aLong"));
        JButton button = (JButton) pico.getComponentInstance("aButton");
        assertEquals("Hello", button.getText());
        assertEquals(0.88, button.getAlignmentX(), 0.01);
    }

    public void testComponentInstanceWithClassKey() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance class-name-key='java.util.Map' factory='org.nanocontainer.script.xml.XStreamComponentInstanceFactory'>" +
                "    <properties>" +
                "      <property name='foo' value='bar'/>" +
                "    </properties>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Map map = (Map)pico.getComponentInstance(Map.class);
        assertNotNull(map);
        assertEquals("bar", map.get("foo"));
    }

    public void testComponentInstanceWithFactoryAndKey() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance factory='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$TestFactory'" +
                "						key='aKey'>" +
                "    <config-or-whatever/>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object instance = pico.getComponentInstance("aKey");
        assertNotNull(instance);
        assertTrue(instance instanceof String);
        assertEquals("Hello", instance.toString());
    }

    public void testComponentInstanceWithContainerFactoryAndKey() {
        Reader script = new StringReader("" +
                "<container component-instance-factory='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$ContainerTestFactory'>" +
                "  <component-instance factory='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$TestFactory'" +
                "						key='firstKey'>" +
                "    <config-or-whatever/>" +
                "  </component-instance>" +
                "  <component-instance key='secondKey'>" +
                "    <config-or-whatever/>" +
                "  </component-instance>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object first = pico.getComponentInstance("firstKey");
        assertNotNull(first);
        assertTrue(first instanceof String);
        assertEquals("Hello", first.toString());
        Object second = pico.getComponentInstance("secondKey");
        assertNotNull(second);
        assertTrue(second instanceof String);
        assertEquals("ContainerHello", second.toString());
    }

    public static class TestFactory implements XMLComponentInstanceFactory {
        public Object makeInstance(PicoContainer pico, Element elem, ClassLoader classLoader) {
            return "Hello";
        }
    }

    public static class ContainerTestFactory implements XMLComponentInstanceFactory {
        public Object makeInstance(PicoContainer pico, Element elem, ClassLoader classLoader) {
            return "ContainerHello";
        }
    }

    public void testInstantiationOfComponentsWithParams() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='org.nanocontainer.testmodel.WebServerConfigComp'>" +
                "    <parameter><string>localhost</string></parameter>" +
                "    <parameter><int>8080</int></parameter>" +
                "  </component-implementation>" +
                "  <component-implementation key='org.nanocontainer.testmodel.WebServer' class='org.nanocontainer.testmodel.WebServerImpl'/>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance(WebServerConfigComp.class));
        WebServerConfigComp config = (WebServerConfigComp) pico.getComponentInstanceOfType(WebServerConfigComp.class);
        assertEquals("localhost", config.getHost());
        assertEquals(8080, config.getPort());
    }

    public void testInstantiationOfComponentsWithParameterInstancesOfSameComponent() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                " 		<parameter>" +
                "			<org.nanocontainer.script.xml.TestBean>" +
                "				<foo>10</foo>" +
                "				<bar>hello1</bar>" +
                "			</org.nanocontainer.script.xml.TestBean>" +
                "		</parameter>" +
                " 		<parameter>" +
                "			<org.nanocontainer.script.xml.TestBean>" +
                "				<foo>10</foo>" +
                "				<bar>hello2</bar>" +
                "			</org.nanocontainer.script.xml.TestBean>" +
                "		</parameter>" +
                "  </component-implementation>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance(TestBeanComposer.class));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance(TestBeanComposer.class);
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    public void testInstantiationOfComponentsWithParameterInstancesOfSameComponentAndBeanFactory() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-implementation class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                " 		<parameter factory='org.nanocontainer.script.xml.BeanComponentInstanceFactory'>" +
                "			<org.nanocontainer.script.xml.TestBean>" +
                "				<foo>10</foo>" +
                "				<bar>hello1</bar>" +
                "			</org.nanocontainer.script.xml.TestBean>" +
                "		</parameter>" +
                " 		<parameter factory='org.nanocontainer.script.xml.BeanComponentInstanceFactory'>" +
                "			<org.nanocontainer.script.xml.TestBean>" +
                "				<foo>10</foo>" +
                "				<bar>hello2</bar>" +
                "			</org.nanocontainer.script.xml.TestBean>" +
                "		</parameter>" +
                "  </component-implementation>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance(TestBeanComposer.class));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance(TestBeanComposer.class);
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    public void testInstantiationOfComponentsWithParameterKeys() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance key='bean1'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello1</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-instance key='bean2'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello2</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-implementation class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                " 		<parameter key='bean1'/>" +
                " 		<parameter key='bean2'/>" +
                "  </component-implementation>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance(TestBeanComposer.class));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance(TestBeanComposer.class);
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    public void testInstantiationOfComponentsWithComponentAdapter() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance key='bean1'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello1</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-instance key='bean2'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello2</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-adapter key='beanKey' class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                " 		<parameter key='bean1'/>" +
                " 		<parameter key='bean2'/>" +
                "  </component-adapter>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance("beanKey"));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance("beanKey");
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    public void testComponentAdapterWithSpecifiedFactory() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-instance key='bean1'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello1</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-instance key='bean2'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello2</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </component-instance>" +
                "  <component-adapter key='beanKey' class='org.nanocontainer.script.xml.TestBeanComposer'" +
                "					factory='" + DefaultComponentAdapterFactory.class.getName() + "'>" +
                " 		<parameter key='bean1'/>" +
                " 		<parameter key='bean2'/>" +
                "  </component-adapter>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponentInstance("beanKey"));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance("beanKey");
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }

    public void testComponentAdapterWithNoKeyUsesTypeAsKey() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-adapter class='org.nanocontainer.script.xml.TestBeanComposer'/>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        ComponentAdapter adapter = (ComponentAdapter)pico.getComponentAdapters().iterator().next();
        assertSame(TestBeanComposer.class, adapter.getComponentImplementation());
    }

    public void testComponentAdapterWithNoClassThrowsNanoContainerMarkupException() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-adapter key='beanKey'/> " +
                "</container>");
        try {
            buildContainer(script);
            fail();
        } catch (NanoContainerMarkupException expected) {
            assertEquals("'class' attribute not specified for component-adapter", expected.getMessage());
        }
    }

    // This is of little value given that nested adapters can't be specified in XML.
    public void testComponentAdapterClassCanBeSpecifiedInContainerElement() {
        Reader script = new StringReader("" +
                "<container component-adapter-factory='" + ConstructorInjectionComponentAdapterFactory.class.getName() + "'>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        Object wsc1 = pico.getComponentInstanceOfType(WebServerConfig.class);
        Object wsc2 = pico.getComponentInstanceOfType(WebServerConfig.class);

        assertNotSame(wsc1, wsc2);
    }

    public void testComponentMonitorCanBeSpecified() {
        Reader script = new StringReader("" +
                "<container component-monitor='" + StaticWriterComponentMonitor.class.getName() + "'>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        pico.getComponentInstanceOfType(WebServerConfig.class);
        assertTrue(StaticWriterComponentMonitor.WRITER.toString().length() > 0);
    }

    public void testComponentMonitorCanBeSpecifiedIfCAFIsSpecified() {
        Reader script = new StringReader("" +
                "<container component-adapter-factory='" +DefaultComponentAdapterFactory.class.getName() +
                "' component-monitor='" + StaticWriterComponentMonitor.class.getName() + "'>" +
                "  <component-implementation class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);
        pico.getComponentInstanceOfType(WebServerConfig.class);
        assertTrue(StaticWriterComponentMonitor.WRITER.toString().length() > 0);
    }

    public void testComponentCanUsePredefinedCAF() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-adapter-factory class='org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory' key='factory'/>" +
                "  <component-adapter class='org.nanocontainer.testmodel.DefaultWebServerConfig' factory='factory'/>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        WebServerConfig cfg1 = (WebServerConfig)pico.getComponentInstanceOfType(WebServerConfig.class);
        WebServerConfig cfg2 = (WebServerConfig)pico.getComponentInstanceOfType(WebServerConfig.class);
        assertNotSame("Instances for components registered with a CICA must not be the same", cfg1, cfg2);
    }

    public void testComponentCanUsePredefinedNestedCAF() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-adapter-factory class='org.picocontainer.defaults.ImplementationHidingComponentAdapterFactory' key='factory'>" +
                "    <component-adapter-factory class='org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory'/>" +
                "  </component-adapter-factory>" +
                "  <component-adapter class-name-key='org.nanocontainer.testmodel.WebServerConfig' class='org.nanocontainer.testmodel.DefaultWebServerConfig' factory='factory'/>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        WebServerConfig cfg1 = (WebServerConfig)pico.getComponentInstanceOfType(WebServerConfig.class);
        WebServerConfig cfg2 = (WebServerConfig)pico.getComponentInstanceOfType(WebServerConfig.class);
        assertNotSame("Instances for components registered with a CICA must not be the same", cfg1, cfg2);
        assertFalse("Instance exposes only interface", cfg1 instanceof DefaultWebServerConfig);
    }

    public void testComponentCanUsePredefinedCAFWithParameters() {
        Reader script = new StringReader("" +
                "<container>" +
                "  <component-adapter-factory class='org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory' key='factory'>" +
                "    <parameter><boolean>true</boolean></parameter>" +
                "  </component-adapter-factory>" +
                "  <component-adapter key='pc1' class='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$PrivateComponent' factory='org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory'/>" +
                "  <component-adapter key='pc2' class='org.nanocontainer.script.xml.XMLContainerBuilderTestCase$PrivateComponent' factory='factory'/>" +
                "</container>");
        PicoContainer pico = buildContainer(script);
        PrivateComponent pc2 = (PrivateComponent)pico.getComponentInstance("pc2");
        assertNotNull(pc2);
        try {
            pico.getComponentInstance("pc1");
            fail("Thrown " + PicoException.class.getName() + " expected");
        } catch (final PicoException e) {
            assertTrue(e.getMessage().indexOf(PrivateComponent.class.getName())>0);
        }
    }

    public void testChainOfDecoratingPicoContainersCanDoInterceptionOfMutablePicoContainerMethods() throws ClassNotFoundException {

       Reader script = new StringReader("" +
                "<container>\n" +
               "   <decorating-picocontainer class='"+FooDecoratingPicoContainer.class.getName()+"'/>" +
               "   <decorating-picocontainer class='"+BarDecoratingPicoContainer.class.getName()+"'/>" +
                "  <component-implementation class='java.util.Vector'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);

        // decorators are fairly dirty - they replace a very select implementation in this TestCase.
        assertNotNull(pico.getComponentInstanceOfType(ArrayList.class));
        assertNull(pico.getComponentInstanceOfType(Vector.class));
    }

    public void testChainOfWrappedComponents() {

       Reader script = new StringReader("" +
                "<container>\n" +
               "   <component-implementation key='wrapped' class='"+SimpleTouchable.class.getName()+"'/>" +
               "   <component-implementation class-name-key=\'"+Touchable.class.getName()+"\' class='"+WrapsTouchable.class.getName()+"'/>" +
                "</container>");

        PicoContainer pico = buildContainer(script);

        // decorators are fairly dirty - they replace a very select implementation in this TestCase.
        assertNotNull(pico.getComponentInstanceOfType(Touchable.class));
    }
    
    public void testListSupport() {

        Reader script = new StringReader("" +
                 "<container>\n" +
                "   <component-implementation class='"+ListSupport.class.getName()+"'>" +
                "       <parameter empty-collection='false' component-value-type='"+Entity.class.getName()+"'/>" +
                "   </component-implementation>" +               
                "   <component-implementation class=\'"+CustomerEntityImpl.class.getName()+"\'/>" +
                "   <component-implementation class=\'"+OrderEntityImpl.class.getName()+"\'/>" +
                 "</container>");

         PicoContainer pico = buildContainer(script);
         
         ListSupport listSupport = (ListSupport)pico.getComponentInstanceOfType(ListSupport.class);

         assertNotNull(listSupport);
         assertNotNull(listSupport.getAListOfEntityObjects());
         assertEquals(2, listSupport.getAListOfEntityObjects().size());

         Entity entity1 = (Entity)listSupport.getAListOfEntityObjects().get(0);
         Entity entity2 = (Entity)listSupport.getAListOfEntityObjects().get(1);
         
         assertNotNull(entity1);
         assertEquals(CustomerEntityImpl.class, entity1.getClass());
         
         assertNotNull(entity2);
         assertEquals(OrderEntityImpl.class, entity2.getClass());
     }
    
    public void testNoEmptyCollectionWithComponentKeyTypeFailure() {

        Reader script = new StringReader("" +
                 "<container>\n" +
                "   <component-implementation class='"+ MapSupport.class.getName()+ "'>" +
                "       <parameter empty-collection='false' component-key-type='"+Entity.class.getName()+"'/>" +
                "   </component-implementation>" +               
                "   <component-implementation key='customer' class=\'"+CustomerEntityImpl.class.getName()+"\'/>" +
                "   <component-implementation key='order' class=\'"+OrderEntityImpl.class.getName()+"\'/>" +
                 "</container>");

        try {
            buildContainer(script);
            fail("Thrown " + PicoException.class.getName() + " expected");
        } catch (final PicoException e) {
            assertTrue(e.getMessage().indexOf("one or both of the emptyCollection")>0);
        }
     }
    
    public void testNoComponentValueTypeWithComponentKeyTypeFailure() {

        Reader script = new StringReader("" +
                 "<container>\n" +
                "   <component-implementation class='"+ MapSupport.class.getName()+ "'>" +
                "       <parameter component-value-type='"+Entity.class.getName()+"' component-key-type='"+Entity.class.getName()+"'/>" +
                "   </component-implementation>" +               
                "   <component-implementation key='customer' class=\'"+CustomerEntityImpl.class.getName()+"\'/>" +
                "   <component-implementation key='order' class=\'"+OrderEntityImpl.class.getName()+"\'/>" +
                 "</container>");

        try {
            buildContainer(script);
            fail("Thrown " + PicoException.class.getName() + " expected");
        } catch (final PicoException e) {
            assertTrue(e.getMessage().indexOf("but one or both of the emptyCollection")>0);
        }
     }   
    
    public void testNoEmptyCollectionWithComponentValueTypeFailure() {

        Reader script = new StringReader("" +
                 "<container>\n" +
                "   <component-implementation class='"+ MapSupport.class.getName()+ "'>" +
                "       <parameter component-value-type='"+Entity.class.getName()+"'/>" +
                "   </component-implementation>" +               
                "   <component-implementation key='customer' class=\'"+CustomerEntityImpl.class.getName()+"\'/>" +
                "   <component-implementation key='order' class=\'"+OrderEntityImpl.class.getName()+"\'/>" +
                 "</container>");

        try {
            buildContainer(script);
            fail("Thrown " + PicoException.class.getName() + " expected");
        } catch (final PicoException e) {
            System.out.println(e);
            
            assertTrue(e.getMessage().indexOf("but the emptyCollection () was empty or null")>0);
        }
     }

    private PicoContainer buildContainer(Reader script) {
        return buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
    }

    static public class StaticWriterComponentMonitor extends WriterComponentMonitor {
        static Writer WRITER = new StringWriter();

        public StaticWriterComponentMonitor() {
            super(WRITER);
        }

    }

    static private class PrivateComponent {
    }

    // TODO: Move this into pico-tck as soon as nano is dependend on a pico snapshot again ...
    public static class WrapsTouchable implements Touchable {
        private final Touchable wrapped;
        
        public WrapsTouchable(final Touchable wrapped) {
            this.wrapped = wrapped;
        }

        public void touch() {
            this.wrapped.touch();
        }
    }
}

