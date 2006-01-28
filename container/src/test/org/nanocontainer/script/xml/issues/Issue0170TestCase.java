package org.nanocontainer.script.xml.issues;

import java.io.Reader;
import java.io.StringReader;

import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.script.xml.XMLContainerBuilder;
import org.picocontainer.PicoContainer;

//http://jira.codehaus.org/browse/NANO-170
public class Issue0170TestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testHotSwappingCAF() {
        Reader script = new StringReader("" +
                "<container>" +
                "<component-adapter-factory key='factory' class='org.picocontainer.gems.adapters.HotSwappingComponentAdapterFactory'>"+
                "   <component-adapter-factory class='org.picocontainer.defaults.CachingComponentAdapterFactory'/>"+
                "   <component-adapter-factory class='org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory'/>"+
                "</component-adapter-factory>"+
                "</container>");

        PicoContainer pico = buildContainer(script);
        assertNotNull(pico);
        
    }

    private PicoContainer buildContainer(Reader script) {
        return buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
    }

}

   