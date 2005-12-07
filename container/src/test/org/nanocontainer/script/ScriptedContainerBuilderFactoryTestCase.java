package org.nanocontainer.script;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class ScriptedContainerBuilderFactoryTestCase
    extends TestCase {

    protected void tearDown() throws Exception {
        ScriptedContainerBuilderFactory.resetBuilders();
        super.tearDown();
    }

    public void testGetAllSupportedExtensions() {
        ScriptedContainerBuilderFactory.resetBuilders();

        List resultingList = Arrays.asList(ScriptedContainerBuilderFactory.getAllSupportedExtensions());

        assertTrue(resultingList.contains(ScriptedContainerBuilderFactory.GROOVY));
        assertTrue(resultingList.contains(ScriptedContainerBuilderFactory.BEANSHELL));
        assertTrue(resultingList.contains(ScriptedContainerBuilderFactory.XML));
        assertTrue(resultingList.contains(ScriptedContainerBuilderFactory.JAVASCRIPT));
        assertTrue(resultingList.contains(ScriptedContainerBuilderFactory.JYTHON));
    }

    public void testGetBuilderClassName() {
        assertNull(ScriptedContainerBuilderFactory.getBuilderClassName(".hi-diddle-diddle"));
        assertEquals(ScriptedContainerBuilderFactory.DEFAULT_GROOVY_BUILDER,
            ScriptedContainerBuilderFactory.getBuilderClassName(ScriptedContainerBuilderFactory.GROOVY));
    }

    public void testRegisterBuilder() {
        //Test a precondition
        assertNull(ScriptedContainerBuilderFactory.getBuilderClassName(".foo"));

        //Now test the real thing.
        ScriptedContainerBuilderFactory.registerBuilder(".foo", "org.example.FooBuilder");
        assertEquals("org.example.FooBuilder", ScriptedContainerBuilderFactory.getBuilderClassName(".foo"));

        //Test override
        ScriptedContainerBuilderFactory.registerBuilder(ScriptedContainerBuilderFactory.GROOVY,
            "org.example.MyGroovyBuilder");

        assertEquals("org.example.MyGroovyBuilder",
            ScriptedContainerBuilderFactory.getBuilderClassName(ScriptedContainerBuilderFactory.GROOVY));

    }

    public void testResetBuilders() {
        ScriptedContainerBuilderFactory.registerBuilder(".foo", "org.example.FooBuilder");
        int beforeResetSize = ScriptedContainerBuilderFactory.getAllSupportedExtensions().length;

        ScriptedContainerBuilderFactory.resetBuilders();
        int afterResetSize = ScriptedContainerBuilderFactory.getAllSupportedExtensions().length;

        assertTrue(afterResetSize < beforeResetSize);
        assertNull(ScriptedContainerBuilderFactory.getBuilderClassName(".foo"));
    }

}
