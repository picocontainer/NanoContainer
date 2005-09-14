package org.nanocontainer.script.groovy

import org.picocontainer.defaults.ComponentParameter
import org.picocontainer.defaults.UnsatisfiableDependenciesException

import org.nanocontainer.script.groovy.NanoContainerBuilder
import org.nanocontainer.testmodel.DefaultWebServerConfig
import org.nanocontainer.testmodel.WebServer
import org.nanocontainer.testmodel.WebServerConfig
import org.nanocontainer.testmodel.WebServerConfigBean
import org.nanocontainer.testmodel.WebServerImpl
import java.io.File

class GroovyNodeBuilderTestCase extends GroovyTestCase {

    void testInstantiateBasicComponent() {

        Xxx.reset()

        builder = new GroovyNodeBuilder()
        nano = builder.container() {
            component(Xxx.A)
        }

        startAndDispose(nano)

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder)
    }

    void testInstantiateBasicComponentInDeeperTree() {

        Xxx.reset()

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            container() {
                component(Xxx.A)
            }
        }

        startAndDispose(nano)

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder)
    }

    void testInstantiateWithImpossibleComponentDependanciesConsideringTheHierarchy() {

        // A and C have no no dependencies. B Depends on A.

        try {
            builder = new GroovyNodeBuilder()
            nano = builder.container {
                component(Xxx.B)
                container() {
                    component(Xxx.A)
                }
                component(Xxx.C)
            }

            startAndDispose(nano)

            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.")
        }
        catch (UnsatisfiableDependenciesException e) {
        }
    }

    void testInstantiateWithBespokeComponentAdapter() {

        sb = new StringBuffer();

        builder = new GroovyNodeBuilder()
        caf = new TestComponentAdapterFactory(sb)
        nano = builder.container(componentAdapterFactory:caf) {
            component(key:WebServerConfig, class:DefaultWebServerConfig)
            component(key:WebServer, class:WebServerImpl)
        }

        startAndDispose(nano)

        assertTrue(sb.toString().indexOf("called") != -1)
    }

    void testInstantiateWithInlineConfiguration() {

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            bean(beanClass:WebServerConfigBean, host:'foobar.com', port:4321)
            component(key:WebServer, class:WebServerImpl)
        }

        startAndDispose(nano)

        assertTrue("WebServerConfigBean and WebServerImpl expected", nano.pico.getComponentInstances().size() == 2)

        wsc = nano.pico.getComponentInstanceOfType(WebServerConfig)
        assertEquals("foobar.com", wsc.getHost())
        assertTrue(wsc.getPort() == 4321)
    }

    void testSoftInstantiateWithChildContainerWithDynamicClassPath() {

        File testCompJar = new File(System.getProperty("testcomp.jar"))
        testCompJar2 = new File(testCompJar.getParent(),"TestComp2.jar")
        compJarPath = testCompJar.getCanonicalPath()
        compJarPath2 = testCompJar2.getCanonicalPath()

        builder = new NanoContainerBuilder()
        child = null
        parent = builder.container {
            classPathElement(path:compJarPath)
            component(class:StringBuffer)
            component(class:"TestComp")
            child = container() {
                classPathElement(path:compJarPath2)
                component(class:"TestComp2")
            }
        }
        assertTrue(parent.pico.getComponentInstances().size() == 2)
        assertTrue(child.pico.getComponentInstances().size() == 1)
        assertNotNull(child.getComponentInstanceOfType("TestComp2"))

    }

    /*
    Now that the builder is building NanoContainer instances, we must agree on what the class
    paramter means - is it the NanoContainer type or the nested PicoContainer type?
    Is it even desirable to have bespoke implementations? Can we get the same flexibility
    by specifying various delegation classes rather than - sigh - inheritance? Inheritance
    is always painful to deal with. It's a pattern as bad as singletons...
    Aslak
    */
    void FIXMEtestInstantiateBasicComponentInCustomContainer() {

        Xxx.reset()

        builder = new GroovyNodeBuilder()
        nano = builder.container(class:TestContainer) {
            component(Xxx.A)
        }

        startAndDispose(nano)
        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder)
        assertEquals("org.nanocontainer.script.groovy.TestContainer",nano.getClass().getName())
    }

    void testInstantiateBasicComponentWithDeepTree() {

       Xxx.reset()

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            container() {
                container() {
                    component(Xxx.A)
                }
            }
            component(HashMap.class)
        }

        startAndDispose(nano)

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder)
    }

    void FIXMEtestInstantiateBasicComponentWithDeepNamedTree() {

        Xxx.reset()

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            container(name:"huey") {
                container(name:"duey") {
                    component(key:"Luis", class:Xxx.A)
                }
            }
            component(HashMap.class)
        }

        startAndDispose(nano)

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder)
        Object o = nano.pico.getComponentInstance("huey/duey/Luis")
        assertNotNull(o)
    }
    
    public void testComponentInstances() {
    
        builder = new GroovyNodeBuilder()
        nano = builder.container {
            component(key:"Louis", instance:"Armstrong")
            component(key:"Duke", instance: "Ellington")
        }
	
        assertEquals("Armstrong", nano.pico.getComponentInstance("Louis"))
        assertEquals("Ellington", nano.pico.getComponentInstance("Duke"))
    }
	
    public void testConstantParameters() {

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            component(key:"cat", class:HasParams, parameters:[ "c", "a", "t" ])
            component(key:"dog", class:HasParams, parameters:[ "d", "o", "g" ])
        }
	
        cat = nano.pico.getComponentInstance("cat");
        dog = nano.pico.getComponentInstance("dog");
        assertEquals("cat", cat.getParams());
        assertEquals("dog", dog.getParams());
    }

    public void testComponentParameters() {

        builder = new GroovyNodeBuilder()
        nano = builder.container {
            component(key:"a", class:Xxx.A)
            component(key:"b", class:Xxx.B, parameters:[ new ComponentParameter("a") ])
        }
	
        a = nano.pico.getComponentInstance("a");
        b = nano.pico.getComponentInstance("b");
        assertSame(a, b.getA())
    }

    protected void startAndDispose(nano) {
        nano.pico.start()
        nano.pico.dispose()
    }

}