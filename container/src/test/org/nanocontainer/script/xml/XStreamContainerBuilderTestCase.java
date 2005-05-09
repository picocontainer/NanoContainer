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

import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.testmodel.DefaultWebServerConfig;
import org.nanocontainer.testmodel.ThingThatTakesParamsInConstructor;
import org.nanocontainer.testmodel.WebServerImpl;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DecoratingComponentAdapter;

import java.io.Reader;
import java.io.StringReader;

public class XStreamContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testContainerBuilding() {

        Reader script = new StringReader("" +
                "<container>" +
                "    <instance key='foo'>" +
                "    	<string>foo bar</string>" +
                "    </instance>" +
                "    <instance key='bar'>" +
                "    	<int>239</int>" +
                "    </instance>" +
                "    <instance>" +
                "    	<org.nanocontainer.testmodel.DefaultWebServerConfig>" +
                " 			<port>555</port>" +
                "    	</org.nanocontainer.testmodel.DefaultWebServerConfig>" +
                "    </instance>" +
                "	 <implementation class='org.nanocontainer.testmodel.WebServerImpl'>" +
                "		<dependency class='org.nanocontainer.testmodel.DefaultWebServerConfig'/>" +
                "	 </implementation>" +
                "	 <implementation key='konstantin needs beer' class='org.nanocontainer.testmodel.ThingThatTakesParamsInConstructor'>" +
                "		<constant>" +
                "			<string>it's really late</string>" +
                "		</constant>" +
                "		<constant>" +
                "			<int>239</int>" +
                "		</constant>" +
                "	 </implementation>" +
                "</container>");

        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertEquals(5, pico.getComponentInstances().size());
        assertEquals("foo bar", pico.getComponentInstance("foo"));
        assertEquals(new Integer(239), pico.getComponentInstance("bar"));
        assertEquals(555, ((DefaultWebServerConfig) pico.getComponentInstance(DefaultWebServerConfig.class)).getPort());

        assertNotNull(pico.getComponentInstanceOfType(WebServerImpl.class));
        assertNotNull(pico.getComponentInstanceOfType(ThingThatTakesParamsInConstructor.class));
        assertSame(pico.getComponentInstance("konstantin needs beer"), pico.getComponentInstanceOfType(ThingThatTakesParamsInConstructor.class));
        assertEquals("it's really late239", ((ThingThatTakesParamsInConstructor) pico.getComponentInstance("konstantin needs beer")).getValue());
    }

    public void testComponentAdapterInjection() throws Throwable {
        Reader script = new StringReader("<container>" +
                "<adapter key='testAdapter'>" +
                "<instance key='firstString'>" +
                "<string>bla bla</string>" +
                "</instance>" +
                "<instance key='secondString' >" +
                "<string>glarch</string>" +
                "</instance>" +
                "<instance key='justInt'>" +
                "<int>777</int>" +
                "</instance>" +
                "<implementation key='testAdapter' class='org.nanocontainer.script.xml.TestComponentAdapter'>" +
                "<dependency key='firstString'/>" +
                "<dependency key='justInt'/>" +
                "<dependency key='secondString'/>" +
                "</implementation>" +
                "</adapter>" +
                "</container>");

        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        TestComponentAdapter tca = (TestComponentAdapter) pico.getComponentAdapter(TestComponentAdapter.class);
        assertNotNull(tca);
    }

    public void testInstantiationOfComponentsWithInstancesOfSameComponent() throws Exception {
        Reader script = new StringReader("" +
                "<container>" +
                "  <instance key='bean1'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello1</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </instance>" +
                "  <instance key='bean2'>" +
                "	<org.nanocontainer.script.xml.TestBean>" +
                "		<foo>10</foo>" +
                "		<bar>hello2</bar>" +
                "	</org.nanocontainer.script.xml.TestBean>" +
                "  </instance>" +
                "  <implementation class='org.nanocontainer.script.xml.TestBeanComposer'>" +
                "		<dependency key='bean1'/>" +
                "		<dependency key='bean2'/>" +
                "  </implementation>" +
                "</container>");
        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponentInstance(TestBeanComposer.class));
        TestBeanComposer composer = (TestBeanComposer) pico.getComponentInstance(TestBeanComposer.class);
        assertEquals("bean1", "hello1", composer.getBean1().getBar());
        assertEquals("bean2", "hello2", composer.getBean2().getBar());
    }
    
    // do not know how to extract parameters off adapter....
    public void testThatDependencyUsesClassAsKey() {
        Reader script = new StringReader("" +
        "<container>" +                                          
        "   <implementation key='foo' class='org.nanocontainer.script.xml.TestBean'>" +
        "       <dependency class='java.lang.String'/>" +
        "       <dependency class='java.lang.Integer'/>" + 
        "   </implementation>" + 
        "</container>"
        );
        
       PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null,null);
       DecoratingComponentAdapter adapter = (DecoratingComponentAdapter)pico.getComponentAdapter("foo");
       
    }
    
    
    public void testDefaultContsructorRegistration() throws Exception {
        
        Reader script = new StringReader(
        "<container>" + 
        "   <implementation class='org.nanocontainer.script.xml.TestBean' constructor='default'/>" +
        "   <instance>" + 
        "       <string>blurge</string>" + 
        "   </instance>" + 
        "</container>"
         );  
        
        
        PicoContainer pico = buildContainer(new XStreamContainerBuilder(script, getClass().getClassLoader()), null,null);
        TestBean bean = (TestBean)pico.getComponentInstanceOfType(TestBean.class);
        assertEquals("default",bean.getConstructorCalled());
    }
}

