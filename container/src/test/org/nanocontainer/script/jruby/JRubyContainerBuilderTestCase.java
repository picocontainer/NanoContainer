package org.nanocontainer.script.jruby;

import org.jmock.Mock;
import org.nanocontainer.NanoPicoContainer;
import org.nanocontainer.TestHelper;
import org.nanocontainer.integrationkit.PicoCompositionException;
import org.nanocontainer.reflection.DefaultNanoPicoContainer;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.groovy.A;
import org.nanocontainer.script.groovy.B;
import org.nanocontainer.script.groovy.HasParams;
import org.nanocontainer.script.groovy.ParentAssemblyScope;
import org.nanocontainer.script.groovy.SomeAssemblyScope;
import org.nanocontainer.script.groovy.X;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.InstanceComponentAdapter;
import org.picocontainer.defaults.SetterInjectionComponentAdapter;
import org.picocontainer.defaults.SetterInjectionComponentAdapterFactory;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Nick Sieger
 * @author Paul Hammant
 * @author Chris Bailey
 * @author Mauro Talevi
 */
public class JRubyContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {
    private static final String ASSEMBLY_SCOPE = "SOME_SCOPE";


    public void testContainerCanBeBuiltWithParentGlobal() {
        Reader script = new StringReader(
                                         "StringBuffer = java.lang.StringBuffer\n" +
                                         "container(:parent => $parent) { \n" +
                                         "  component(StringBuffer)\n" +
                                         "}");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotNull(pico.getParent());
        assertNotSame(parent, pico.getParent());
        assertEquals(StringBuffer.class, pico.getComponentInstance(StringBuffer.class).getClass());
    }

    public void testContainerCanBeBuiltWithComponentImplementation() {
        X.reset();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "container {\n" +
                                         "    component(A)\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        // LifecyleContainerBuilder starts the container
        pico.dispose();

        assertEquals("Should match the expression", "<A!A", X.componentRecorder);
    }

    public void testContainerCanBeBuiltWithComponentInstance() {
        Reader script = new StringReader(
                                         "container { \n" +
                                         "  component(:key => 'string', :instance => 'foo')\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, "SOME_SCOPE");

        assertEquals("foo", pico.getComponentInstance("string"));
    }

    public void testBuildingWithPicoSyntax() {
        Reader script = new StringReader(
                                         "$parent.registerComponentImplementation('foo', Java::JavaClass.for_name('java.lang.String'))\n"
                                         +
                                         "DefaultPicoContainer = org.picocontainer.defaults.DefaultPicoContainer\n" +
                                         "pico = DefaultPicoContainer.new($parent)\n" +
                                         "pico.registerComponentImplementation(Java::JavaClass.for_name('org.nanocontainer.script.groovy.A'))\n"
                                         +
                                         "pico");

        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(script, parent, "SOME_SCOPE");

        assertNotSame(parent, pico.getParent());
        assertNotNull(pico.getComponentInstance(A.class));
        assertNotNull(pico.getComponentInstance("foo"));
    }

    public void testContainerBuiltWithMultipleComponentInstances() {
        Reader script = new StringReader(
                                         "container {\n" +
                                         "    component(:key => 'a', :instance => 'apple')\n" +
                                         "    component(:key => 'b', :instance => 'banana')\n" +
                                         "    component(:instance => 'noKeySpecified')\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        assertEquals("apple", pico.getComponentInstance("a"));
        assertEquals("banana", pico.getComponentInstance("b"));
        assertEquals("noKeySpecified", pico.getComponentInstance(String.class));
    }

    public void testShouldFailWhenNeitherClassNorInstanceIsSpecifiedForComponent() {
        Reader script = new StringReader(
                                         "container {\n" +
                                         "  component(:key => 'a')\n" +
                                         "}");

        try {
            buildContainer(script, null, ASSEMBLY_SCOPE);
            fail("NanoContainerMarkupException should have been raised");
        } catch(NanoContainerMarkupException e) {
            // expected
        }
    }

    public void testAcceptsConstantParametersForComponent() {
        Reader script = new StringReader(
                                         "HasParams = org.nanocontainer.script.groovy.HasParams\n" +
                                         "container {\n" +
                                         "    component(:key => 'byClass', :class => HasParams, :parameters => [ 'a', 'b', constant('c')])\n"
                                         +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        HasParams byClass = (HasParams) pico.getComponentInstance("byClass");
        assertEquals("abc", byClass.getParams());
    }

    public void testAcceptsComponentClassNameAsString() {
        Reader script = new StringReader(
                                         "container {\n" +
                                         "    component(:key => 'byClassString', :class => 'org.nanocontainer.script.groovy.HasParams', :parameters => [ 'c', 'a', 't' ])\n"
                                         +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        HasParams byClassString = (HasParams) pico.getComponentInstance("byClassString");
        assertEquals("cat", byClassString.getParams());
    }

    public void testAcceptsComponentParametersForComponent() {
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "B = org.nanocontainer.script.groovy.B\n" +
                                         "container {\n" +
                                         "    component(:key => 'a1', :class => A)\n" +
                                         "    component(:key => 'a2', :class => A)\n" +
                                         "    component(:key => 'b1', :class => B, :parameters => [ key('a1') ])\n" +
                                         "    component(:key => 'b2', :class => B, :parameters => key('a2'))\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a1 = (A) pico.getComponentInstance("a1");
        A a2 = (A) pico.getComponentInstance("a2");
        B b1 = (B) pico.getComponentInstance("b1");
        B b2 = (B) pico.getComponentInstance("b2");

        assertNotNull(a1);
        assertNotNull(a2);
        assertNotNull(b1);
        assertNotNull(b2);

        assertSame(a1, b1.getA());
        assertSame(a2, b2.getA());
        assertNotSame(a1, a2);
        assertNotSame(b1, b2);
    }

    public void testAcceptsComponentParameterWithClassNameKey() {
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "B = org.nanocontainer.script.groovy.B\n" +
                                         "container {\n" +
                                         "    component(:class => A)\n" +
                                         "    component(:key => B, :class => B, :parameters => key(A))\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        A a = (A) pico.getComponentInstance(A.class);
        B b = (B) pico.getComponentInstance(B.class);

        assertNotNull(a);
        assertNotNull(b);
        assertSame(a, b.getA());
    }

    public void testInstantiateBasicComponentInDeeperTree() {
        X.reset();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "container {\n" +
                                         "  container {\n" +
                                         "    component(A)\n" +
                                         "  }\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        pico.dispose();
        assertEquals("Should match the expression", "<A!A", X.componentRecorder);
    }

    public void testCustomComponentAdapterFactoryCanBeSpecified() {
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "container(:component_adapter_factory => $assembly_scope) {\n" +
                                         "    component(A)\n" +
                                         "}");

        A a = new A();
        Mock cafMock = mock(ComponentAdapterFactory.class);
        cafMock.expects(once()).method("createComponentAdapter").with(same(A.class), same(A.class), eq(null))
            .will(returnValue(new InstanceComponentAdapter(A.class, a)));
        PicoContainer pico = buildContainer(script, null, cafMock.proxy());
        assertSame(a, pico.getComponentInstanceOfType(A.class));
    }

    public void testCustomComponentMonitorCanBeSpecified() {
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "StringWriter = java.io.StringWriter\n" +
                                         "WriterComponentMonitor = org.picocontainer.monitors.WriterComponentMonitor\n" +
                                         "writer = StringWriter.new\n" +
                                         "monitor = WriterComponentMonitor.new(writer) \n" +
                                         "container(:component_monitor => monitor) {\n" +
                                         "    component(A)\n" +
                                         "    component(:key => StringWriter, :instance => writer)\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        StringWriter writer = (StringWriter) pico.getComponentInstanceOfType(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    public void testCustomComponentMonitorCanBeSpecifiedWhenCAFIsSpecified() {
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "StringWriter = java.io.StringWriter\n" +
                                         "WriterComponentMonitor = org.picocontainer.monitors.WriterComponentMonitor\n" +
                                         "DefaultComponentAdapterFactory = org.picocontainer.defaults.DefaultComponentAdapterFactory\n" +
                                         "writer = StringWriter.new\n" +
                                         "monitor = WriterComponentMonitor.new(writer) \n" +
                                         "container(:component_adapter_factory => DefaultComponentAdapterFactory.new, :component_monitor => monitor) {\n"
                                         +
                                         "    component(A)\n" +
                                         "    component(:key => StringWriter, :instance => writer)\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        StringWriter writer = (StringWriter) pico.getComponentInstanceOfType(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    public void testCustomComponentMonitorCanBeSpecifiedWhenParentIsSpecified() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "StringWriter = java.io.StringWriter\n" +
                                         "WriterComponentMonitor = org.picocontainer.monitors.WriterComponentMonitor\n" +
                                         "writer = StringWriter.new\n" +
                                         "monitor = WriterComponentMonitor.new(writer) \n" +
                                         "container(:parent => $parent, :component_monitor => monitor) {\n" +
                                         "    component(A)\n" +
                                         "    component(:key => StringWriter, :instance => writer)\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        StringWriter writer = (StringWriter) pico.getComponentInstanceOfType(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    public void testCustomComponentMonitorCanBeSpecifiedWhenParentAndCAFAreSpecified() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "StringWriter = java.io.StringWriter\n" +
                                         "WriterComponentMonitor = org.picocontainer.monitors.WriterComponentMonitor\n" +
                                         "DefaultComponentAdapterFactory = org.picocontainer.defaults.DefaultComponentAdapterFactory\n" +
                                         "writer = StringWriter.new\n" +
                                         "monitor = WriterComponentMonitor.new(writer) \n" +
                                         "container(:parent => $parent, :component_adapter_factory => DefaultComponentAdapterFactory.new, :component_monitor => monitor) {\n"
                                         +
                                         "    component(A)\n" +
                                         "    component(:key => StringWriter, :instance => writer)\n" +
                                         "}");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        StringWriter writer = (StringWriter) pico.getComponentInstanceOfType(StringWriter.class);
        assertTrue(writer.toString().length() > 0);
    }

    public void testInstantiateWithImpossibleComponentDependenciesConsideringTheHierarchy() {
        X.reset();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "B = org.nanocontainer.script.groovy.B\n" +
                                         "C = org.nanocontainer.script.groovy.C\n" +
                                         "container {\n" +
                                         "    component(B)\n" +
                                         "    container() {\n" +
                                         "        component(A)\n" +
                                         "    }\n" +
                                         "    component(C)\n" +
                                         "}");

        try {
            buildContainer(script, null, ASSEMBLY_SCOPE);
            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.");
        } catch(UnsatisfiableDependenciesException expected) {
        }
    }

    public void testInstantiateWithChildContainerAndStartStopAndDisposeOrderIsCorrect() {
        X.reset();
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "B = org.nanocontainer.script.groovy.B\n" +
                                         "C = org.nanocontainer.script.groovy.C\n" +
                                         "container {\n" +
                                         "    component(A)\n" +
                                         "    container() {\n" +
                                         "         component(B)\n" +
                                         "    }\n" +
                                         "    component(C)\n" +
                                         "}\n");

        // A and C have no no dependancies. B Depends on A.
        PicoContainer pico = buildContainer(script, null, ASSEMBLY_SCOPE);
        pico.stop();
        pico.dispose();

        assertEquals("Should match the expression", "<A<C<BB>C>A>!B!C!A", X.componentRecorder);
    }

    public void testBuildContainerWithParentAttribute() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        parent.registerComponentInstance("hello", "world");

        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "container(:parent => $parent) {\n" +
                                         "    component(A)\n" +
                                         "}\n");

        PicoContainer pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        // Should be able to get instance that was registered in the parent container
        assertEquals("world", pico.getComponentInstance("hello"));
    }

    public void testBuildContainerWithParentDependencyAndAssemblyScope() throws Exception {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        parent.registerComponentImplementation("a", A.class);

        String source =
                        "B = org.nanocontainer.script.groovy.B\n" +
                        "SomeAssemblyScope = org.nanocontainer.script.groovy.SomeAssemblyScope\n" +
                        "container(:parent => $parent) {\n" +
                        "  if $assembly_scope.kind_of?(SomeAssemblyScope)\n " +
                        "    component(B)\n" +
                        "  end\n " +
                        "}\n";

        Reader script = new StringReader(source);

        PicoContainer pico = buildContainer(script, parent, new SomeAssemblyScope());
        assertNotNull(pico.getComponentInstanceOfType(B.class));

        script = new StringReader(source);
        pico = buildContainer(script, parent, ASSEMBLY_SCOPE);
        assertNull(pico.getComponentInstanceOfType(B.class));
    }

    public void testBuildContainerWithParentAndChildAssemblyScopes() throws IOException {
        String scriptValue =
                             "A = org.nanocontainer.script.groovy.A\n" +
                             "B = org.nanocontainer.script.groovy.B\n" +
                             "ParentAssemblyScope = org.nanocontainer.script.groovy.ParentAssemblyScope\n" +
                             "SomeAssemblyScope = org.nanocontainer.script.groovy.SomeAssemblyScope\n" +
                             "container(:parent => $parent) {\n" +
                             "  puts 'assembly_scope:'+$assembly_scope.inspect\n " +
                             "  case $assembly_scope\n" +
                             "  when ParentAssemblyScope\n " +
                             "    puts 'parent scope'\n " +
                             "    component(A)\n" +
                             "  when SomeAssemblyScope\n " +
                             "    puts 'child scope'\n " +
                             "    component(B)\n" +
                             "  else \n" +
                             "     raise 'Invalid Scope: ' +  $assembly_scope.inspect\n" +
                             "  end\n " +
                             "}\n";

        Reader script = new StringReader(scriptValue);
        NanoPicoContainer parent = new DefaultNanoPicoContainer(
            buildContainer(script, null, new ParentAssemblyScope()));
        assertNotNull(parent.getComponentAdapterOfType(A.class));

        script = new StringReader(scriptValue);
        PicoContainer pico = buildContainer(script, parent, new SomeAssemblyScope());
        assertNotNull(pico.getComponentInstance(B.class));
    }

    public void FAILING_testBuildContainerWithParentAttributesPropagatesComponentAdapterFactory() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer(new SetterInjectionComponentAdapterFactory());
        Reader script = new StringReader("container(:parent => $parent)\n");

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, parent, ASSEMBLY_SCOPE);
        // Should be able to get instance that was registered in the parent container
        ComponentAdapter componentAdapter = pico.registerComponentImplementation(String.class);
        assertTrue("ComponentAdapter should be originally defined by parent",
                   componentAdapter instanceof SetterInjectionComponentAdapter);
    }

    public void testExceptionThrownWhenParentAttributeDefinedWithinChild() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer(new SetterInjectionComponentAdapterFactory());
        Reader script = new StringReader(
                                         "A = org.nanocontainer.script.groovy.A\n" +
                                         "B = org.nanocontainer.script.groovy.B\n" +
                                         "container() {\n" +
                                         "    component(A)\n" +
                                         "    container(:parent => $parent) {\n" +
                                         "         component(B)\n" +
                                         "    }\n" +
                                         "}\n");

        try {
            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("NanoContainerMarkupException should have been thrown.");
        } catch(NanoContainerMarkupException ignore) {
            // expected
        }
    }

    //TODO
    public void testSpuriousAttributes() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();

        Reader script = new StringReader(
                                         "container(:jim => 'Jam', :foo => 'bar')");
        try {
            buildContainer(script, parent, ASSEMBLY_SCOPE);
            //fail("Should throw exception upon spurious attributes?");
        } catch(NanoContainerMarkupException ex) {
            //ok?
        }
    }

    public void testWithDynamicClassPathThatDoesNotExist() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        try {
            Reader script = new StringReader(
                                             "container {\n" +
                                             "  classPathElement(:path => 'this/path/does/not/exist.jar')\n" +
                                             "  component(:class => \"FooBar\")\n" +
                                             "}");

            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("should have barfed with bad path exception");
        } catch(NanoContainerMarkupException e) {
            // excpected
        }

    }

    public void testWithDynamicClassPath() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        Reader script = new StringReader(
            "TestHelper = org.nanocontainer.TestHelper\n"
            + "testCompJar = TestHelper.getTestCompJarFile()\n"
            + "compJarPath = testCompJar.getCanonicalPath()\n"
            + "container {\n"
            + "  classPathElement(:path => compJarPath)\n"
            + "  component(:class => \"TestComp\")\n"
            + "}" );

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, parent, ASSEMBLY_SCOPE);

        assertEquals(1, pico.getComponentInstances().size());
        assertEquals("TestComp", pico.getComponentInstances().get(0).getClass()
            .getName());
    }

    public void testWithDynamicClassPathWithPermissions() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        Reader script = new StringReader(
            "TestHelper = org.nanocontainer.TestHelper\n" +
            "SocketPermission = java.net.SocketPermission\n"
            + "testCompJar = TestHelper.getTestCompJarFile()\n"
            + "compJarPath = testCompJar.getCanonicalPath()\n"
            + "container {\n"
            + "  classPathElement(:path => compJarPath) {\n"
            + "    grant(:perm => SocketPermission.new('google.com','connect'))\n"
            + "  }\n"
            + "  component(:class => \"TestComp\")\n"
            + "}" );

        MutablePicoContainer pico = (MutablePicoContainer) buildContainer(script, parent, ASSEMBLY_SCOPE);

        assertEquals(1, pico.getComponentInstances().size());
        // can't actually test the permission under JUNIT control. We're just
        // testing the syntax here.
    }

    public void testGrantPermissionInWrongPlace() {
        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer();
        try {
            Reader script = new StringReader(
                "TestHelper = org.nanocontainer.TestHelper\n" +
                "SocketPermission = java.net.SocketPermission\n" +
                "testCompJar = TestHelper.getTestCompJarFile()\n" +
                "container {\n" +
                "  grant(:perm => SocketPermission.new('google.com','connect'))\n" +
                "}");

            buildContainer(script, parent, ASSEMBLY_SCOPE);
            fail("should barf with [Don't know how to create a 'grant' child] exception");
        } catch(PicoCompositionException e) {
            String message = e.getCause().getMessage();
            assertNotNull(message);
            assertTrue(message.indexOf("undefined method `grant' for #<Nano::Container:") != -1);
        }

    }


    public void testWithParentClassPathPropagatesWithNoParentContainer() throws IOException {
        File testCompJar = TestHelper.getTestCompJarFile();

        URLClassLoader classLoader = new URLClassLoader(new URL[]{testCompJar.toURL()},
                                                        this.getClass().getClassLoader());
        Class testComp = null;

        try {
            testComp = classLoader.loadClass("TestComp");
        } catch(ClassNotFoundException ex) {
            fail("Unable to load test component from the jar using a url classloader");
        }
        Reader script = new StringReader(
            "container(:parent => $parent) {\n"
            + "  component(:class => \"TestComp\")\n"
            + "}");

        PicoContainer pico = buildContainer(new JRubyContainerBuilder(script, classLoader), null, null);
        assertNotNull(pico);
        Object testCompInstance = pico.getComponentInstance(testComp.getName());
        assertSame(testCompInstance.getClass(), testComp);

    }

//    public void testExceptionThrownWhenParentAttributeDefinedWithinChild() {
//        DefaultNanoPicoContainer parent = new DefaultNanoPicoContainer(new SetterInjectionComponentAdapterFactory() );
//        Reader script = new StringReader("" +
//                "package org.nanocontainer.script.groovy\n" +
//                "nano = new GroovyNodeBuilder().container() {\n" +
//                "    component(A)\n" +
//                "    container(parent:parent) {\n" +
//                "         component(B)\n" +
//                "    }\n" +
//                "}\n");
//
//        try {
//            buildContainer(script, parent, ASSEMBLY_SCOPE);
//            fail("NanoContainerMarkupException should have been thrown.");
//        } catch (NanoContainerMarkupException ignore) {
//            // ignore
//        }
//    }

    private PicoContainer buildContainer(Reader script, PicoContainer parent, Object scope) {
        return buildContainer(new JRubyContainerBuilder(script, getClass().getClassLoader()), parent, scope);
    }
}
