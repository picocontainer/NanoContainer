package org.nanocontainer.script.groovy;

import org.jmock.Mock;
import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.groovy.Xxx.A;
import org.nanocontainer.script.groovy.Xxx.B;
import org.nanocontainer.reflection.DefaultNanoPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.InstanceComponentAdapter;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

import java.io.Reader;
import java.io.StringReader;

/**
 * Test with groovy classic parser - ie before jsr releases.
 * 
 * @author Paul Hammant
 * @version $Revision$
 */
public class NanoContainerBuilderClassicTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testInstantiateBasicScriptable() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.nanocontainer.script.groovy.Xxx\n" +
                "import org.nanocontainer.script.groovy.Xxx$A\n" +
                "Xxx.reset()\n" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(Xxx$A)\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        // LifecyleContainerBuilder starts the container
        pico.dispose();

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder);
    }

    public void testComponentInstances() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.nanocontainer.script.groovy.Xxx$A\n" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a', instance:'apple')\n" +
                "    component(key:'b', instance:'banana')\n" +
                "    component(instance:'noKeySpecified')\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertEquals("apple", pico.getComponentInstance("a"));
        assertEquals("banana", pico.getComponentInstance("b"));
        assertEquals("noKeySpecified", pico.getComponentInstance(String.class));
    }

    public void testShouldFailWhenNeitherClassNorInstanceIsSpecifiedForComponent() {
        Reader script = new StringReader("" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a')\n" +
                "}");

        try {
            buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
            fail("NanoContainerMarkupException should have been raised");
        } catch (NanoContainerMarkupException e) {
            // expected
        }
    }

    public void testShouldAcceptConstantParametersForComponent() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.defaults.ConstantParameter\n" +
                "import org.nanocontainer.script.groovy.HasParams\n" +
                "" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'byClass', class:HasParams, parameters:[ 'a', 'b', new ConstantParameter('c') ])\n" +
                "    component(key:'byClassString', class:'org.nanocontainer.script.groovy.HasParams', parameters:[ 'c', 'a', 't' ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        HasParams byClass = (HasParams) pico.getComponentInstance("byClass");
        assertEquals("abc", byClass.getParams());

        HasParams byClassString = (HasParams) pico.getComponentInstance("byClassString");
        assertEquals("cat", byClassString.getParams());
    }

    public void testShouldAcceptComponentParametersForComponent() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.defaults.ComponentParameter\n" +
                "import org.nanocontainer.script.groovy.Xxx$A\n" +
                "import org.nanocontainer.script.groovy.Xxx$B\n" +
                "" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a1', class:Xxx$A)\n" +
                "    component(key:'a2', class:Xxx$A)\n" +
                "    component(key:'b1', class:Xxx$B, parameters:[ new ComponentParameter('a1') ])\n" +
                "    component(key:'b2', class:Xxx$B, parameters:[ new ComponentParameter('a2') ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Xxx.A a1 = (Xxx.A) pico.getComponentInstance("a1");
        Xxx.A a2 = (Xxx.A) pico.getComponentInstance("a2");
        Xxx.B b1 = (Xxx.B) pico.getComponentInstance("b1");
        Xxx.B b2 = (Xxx.B) pico.getComponentInstance("b2");

        assertNotNull(a1);
        assertNotNull(a2);
        assertNotNull(b1);
        assertNotNull(b2);

        assertSame(a1, b1.a);
        assertSame(a2, b2.a);
        assertNotSame(a1, a2);
        assertNotSame(b1, b2);
    }

    public void testShouldAcceptComponentParameterWithClassNameKey() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.picocontainer.defaults.ComponentParameter\n" +
                "import org.nanocontainer.script.groovy.Xxx$A\n" +
                "import org.nanocontainer.script.groovy.Xxx$B\n" +
                "" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(class:Xxx$A)\n" +
                "    component(key:Xxx$B, class:Xxx$B, parameters:[ new ComponentParameter(Xxx$A) ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Xxx.A a = (Xxx.A) pico.getComponentInstance(Xxx.A.class);
        Xxx.B b = (Xxx.B) pico.getComponentInstance(Xxx.B.class);

        assertNotNull(a);
        assertNotNull(b);
        assertSame(a, b.a);
    }

    public void testComponentParametersScript() {
        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "import org.picocontainer.defaults.ComponentParameter\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a', class:Xxx$A)\n" +
                "    component(key:'b', class:Xxx$B, parameters:[ new ComponentParameter('a') ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        Xxx.A a = (A) pico.getComponentInstance("a");
        Xxx.B b = (B) pico.getComponentInstance("b");
        assertSame(a, b.a);
    }

    public void testShouldBeAbleToHandOffToNestedBuilder() {

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a', class:Xxx$A)\n" +
                "    newBuilder(class:'org.nanocontainer.script.groovy.TestingChildBuilder') {\n" +
                "      component(key:'b', class:Xxx$B)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Object a = pico.getComponentInstance("a");
        Object b = pico.getComponentInstance("b");

        assertNotNull(a);
        assertNotNull(b);
    }

    public void testInstantiateBasicComponentInDeeperTree() {

        Xxx.reset();
        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    container() {\n" +
                "        component(Xxx$A)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        pico.dispose();

        assertEquals("Should match the expression", "<A!A", Xxx.componentRecorder);
    }

    public void testShouldBeAbleToSpecifyCustomCompoentAdapterFactory() {

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container(componentAdapterFactory:assemblyScope) {\n" +
                "    component(Xxx$A)\n" +
                "}");

        A a = new A();
        Mock cafMock = mock(ComponentAdapterFactory.class);
        cafMock.expects(once()).method("createComponentAdapter").with(same(A.class), same(A.class), eq(null)).will(returnValue(new InstanceComponentAdapter(A.class, a)));
        ComponentAdapterFactory componentAdapterFactory = (ComponentAdapterFactory) cafMock.proxy();
        PicoContainer picoContainer = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, componentAdapterFactory);
        assertSame(a, picoContainer.getComponentInstanceOfType(A.class));
    }

    public void testInstantiateWithImpossibleComponentDependenciesConsideringTheHierarchy() {

        Xxx.reset();
        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(Xxx$B)\n" +
                "    container() {\n" +
                "        component(Xxx$A)\n" +
                "    }\n" +
                "    component(Xxx$C)\n" +
                "}");

        try {
            buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.");
        } catch (UnsatisfiableDependenciesException expected) {
        }
    }

    public void testInstantiateWithChildContainerAndStartStopAndDisposeOrderIsCorrect() {

        Xxx.reset();

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(Xxx$A)\n" +
                "    container() {\n" +
                "         component(Xxx$B)\n" +
                "    }\n" +
                "    component(Xxx$C)\n" +
                "}\n");

        // A and C have no no dependancies. B Depends on A.

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        //pico.start();
        pico.stop();
        pico.dispose();

        assertEquals("Should match the expression", "<A<C<BB>C>A>!B!C!A", Xxx.componentRecorder);
    }

	public void testBuildContainerWithParentAttribute() {
		DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
		parent.registerComponentInstance("hello", "world");

		Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "nano = new NanoContainerBuilder().container(parent:parent) {\n" +
                "    component(Xxx$A)\n" +
                "}\n");

		PicoContainer pico = buildContainer(
				new GroovyContainerBuilder(script, getClass().getClassLoader()),
				parent,
				"SOME_SCOPE");

		// Should be able to get instance that was registered in the parent container
        assertEquals("world", pico.getComponentInstance("hello"));
	}

	public void testExceptionThrownWhenParentAttributeDefinedWithinChild() {
		Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "nano = new NanoContainerBuilder().container() {\n" +
                "    component(Xxx$A)\n" +
                "    container(parent:parent) {\n" +
                "         component(Xxx$B)\n" +
                "    }\n" +
                "}\n");

		try {
			buildContainer(
					new GroovyContainerBuilder(script, getClass().getClassLoader()),
					new DefaultNanoPicoContainer(),
					"SOME_SCOPE");

			fail("NanoContainerMarkupException should have been thrown.");
		} catch (NanoContainerMarkupException ignore) {
			// ignore
		}
	}

}
