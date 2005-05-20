package org.nanocontainer.script.groovy;

import org.jmock.Mock;
import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.groovy.A;
import org.nanocontainer.script.groovy.B;
import org.nanocontainer.reflection.DefaultNanoPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.InstanceComponentAdapter;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

import java.io.Reader;
import java.io.StringReader;

/**
 * Test with groovy jsr parser
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @version $Revision: 1775 $
 */
public class NanoContainerBuilderJsrTestCase extends AbstractScriptedContainerBuilderTestCase {

    public void testInstantiateBasicScriptable() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.nanocontainer.script.groovy.X\n" +
                "import org.nanocontainer.script.groovy.A\n" +
                "X.reset()\n" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(A)\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        // LifecyleContainerBuilder starts the container
        pico.dispose();

        assertEquals("Should match the expression", "<A!A", X.componentRecorder);
    }

    public void testComponentInstances() throws PicoCompositionException {
        Reader script = new StringReader("" +
                "import org.nanocontainer.script.groovy.A\n" +
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
                "import org.nanocontainer.script.groovy.A\n" +
                "import org.nanocontainer.script.groovy.B\n" +
                "" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a1', class:A)\n" +
                "    component(key:'a2', class:A)\n" +
                "    component(key:'b1', class:B, parameters:[ new ComponentParameter('a1') ])\n" +
                "    component(key:'b2', class:B, parameters:[ new ComponentParameter('a2') ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        A a1 = (A) pico.getComponentInstance("a1");
        A a2 = (A) pico.getComponentInstance("a2");
        B b1 = (B) pico.getComponentInstance("b1");
        B b2 = (B) pico.getComponentInstance("b2");

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
                "import org.nanocontainer.script.groovy.A\n" +
                "import org.nanocontainer.script.groovy.B\n" +
                "" +
                "builder = new org.nanocontainer.script.groovy.NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(class:A)\n" +
                "    component(key:B, class:B, parameters:[ new ComponentParameter(A) ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        A a = (A) pico.getComponentInstance(A.class);
        B b = (B) pico.getComponentInstance(B.class);

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
                "    component(key:'a', class:A)\n" +
                "    component(key:'b', class:B, parameters:[ new ComponentParameter('a') ])\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        A a = (A) pico.getComponentInstance("a");
        B b = (B) pico.getComponentInstance("b");
        assertSame(a, b.a);
    }

    public void testShouldBeAbleToHandOffToNestedBuilder() {

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(key:'a', class:A)\n" +
                "    newBuilder(class:'org.nanocontainer.script.groovy.TestingChildBuilder') {\n" +
                "      component(key:'b', class:B)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Object a = pico.getComponentInstance("a");
        Object b = pico.getComponentInstance("b");

        assertNotNull(a);
        assertNotNull(b);
    }

    public void testInstantiateBasicComponentInDeeperTree() {

        X.reset();
        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    container() {\n" +
                "        component(A)\n" +
                "    }\n" +
                "}");

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        pico.dispose();

        assertEquals("Should match the expression", "<A!A", X.componentRecorder);
    }

    public void testShouldBeAbleToSpecifyCustomCompoentAdapterFactory() {

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container(componentAdapterFactory:assemblyScope) {\n" +
                "    component(A)\n" +
                "}");

        A a = new A();
        Mock cafMock = mock(ComponentAdapterFactory.class);
        cafMock.expects(once()).method("createComponentAdapter").with(same(A.class), same(A.class), eq(null)).will(returnValue(new InstanceComponentAdapter(A.class, a)));
        ComponentAdapterFactory componentAdapterFactory = (ComponentAdapterFactory) cafMock.proxy();
        PicoContainer picoContainer = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, componentAdapterFactory);
        assertSame(a, picoContainer.getComponentInstanceOfType(A.class));
    }

    public void testInstantiateWithImpossibleComponentDependenciesConsideringTheHierarchy() {

        X.reset();
        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(B)\n" +
                "    container() {\n" +
                "        component(A)\n" +
                "    }\n" +
                "    component(C)\n" +
                "}");

        try {
            buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.");
        } catch (UnsatisfiableDependenciesException expected) {
        }
    }

    public void testInstantiateWithChildContainerAndStartStopAndDisposeOrderIsCorrect() {

        X.reset();

        Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "builder = new NanoContainerBuilder()\n" +
                "nano = builder.container {\n" +
                "    component(A)\n" +
                "    container() {\n" +
                "         component(B)\n" +
                "    }\n" +
                "    component(C)\n" +
                "}\n");

        // A and C have no no dependancies. B Depends on A.

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        //pico.start();
        pico.stop();
        pico.dispose();

        assertEquals("Should match the expression", "<A<C<BB>C>A>!B!C!A", X.componentRecorder);
    }

	public void testBuildContainerWithParentAttribute() {
		DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
		parent.registerComponentInstance("hello", "world");

		Reader script = new StringReader("" +
                "package org.nanocontainer.script.groovy\n" +
                "nano = new NanoContainerBuilder().container(parent:parent) {\n" +
                "    component(A)\n" +
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
                "    component(A)\n" +
                "    container(parent:parent) {\n" +
                "         component(B)\n" +
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
