package org.nanocontainer.script;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.TestCase;

/**
 * Name/Builder Resolution Test Cases.
 * @author Michael Rimov
 */
public class ScriptBuilderResolverTestCase extends TestCase {
    private ScriptBuilderResolver scriptBuilderResolver = null;

    protected void setUp() throws Exception {
        super.setUp();
        scriptBuilderResolver = new ScriptBuilderResolver();
    }

    protected void tearDown() throws Exception {
        scriptBuilderResolver = null;
        super.tearDown();
    }


    public void testGetAllSupportedExtensions() {
        Set allExtensions = new TreeSet();

        allExtensions.add(ScriptBuilderResolver.BEANSHELL);
        allExtensions.add(ScriptBuilderResolver.GROOVY);
        allExtensions.add(ScriptBuilderResolver.JAVASCRIPT);
        allExtensions.add(ScriptBuilderResolver.JYTHON);
        allExtensions.add(ScriptBuilderResolver.XML);

        String[] actualReturn = scriptBuilderResolver.getAllSupportedExtensions();
        assertNotNull(actualReturn);

        List returnAsList = Arrays.asList(actualReturn);
        boolean someMerged = allExtensions.removeAll(returnAsList);
        assertTrue(someMerged);
        assertTrue(allExtensions.size() == 0);

    }

    public void testGetBuilderClassNameForFile() {

        File compositionFile = new File("test.groovy");
        String expectedReturn = ScriptBuilderResolver.DEFAULT_GROOVY_BUILDER;
        String actualReturn = scriptBuilderResolver.getBuilderClassName(compositionFile);
        assertEquals("return value", expectedReturn, actualReturn);
    }


    public void testGetBuilderClassNameForExtension() throws UnsupportedScriptTypeException {
        String expectedReturn = ScriptBuilderResolver.DEFAULT_JAVASCRIPT_BUILDER;
        String actualReturn = scriptBuilderResolver.getBuilderClassName(".js");
        assertEquals("return value", expectedReturn, actualReturn);

    }

    public void testGetBuilderForExtensionThrowsExceptionForUnknownBuilderType() {
        try {
            scriptBuilderResolver.getBuilderClassName(".foo");
            fail("Retrieving extension of type .foo should have thrown exception");
        } catch (UnsupportedScriptTypeException ex) {
            assertEquals(".foo",ex.getRequestedExtension());
        }

    }

    public void testGetBuilderClassName2() {
        final String resourceName = "/org/nanocontainer/script/groovy/GroovyNodeBuilderScriptedTestCase.groovy";
        URL compositionURL = this.getClass().getResource(resourceName);
        if (compositionURL == null) {
            fail("This test depended on resource '"+ resourceName + "' which appears to have been moved");
        }
        String expectedReturn = ScriptBuilderResolver.DEFAULT_GROOVY_BUILDER;
        String actualReturn = scriptBuilderResolver.getBuilderClassName(compositionURL);
        assertEquals("return value", expectedReturn, actualReturn);
    }

    public void testRegisterBuilder() {
        scriptBuilderResolver.registerBuilder(".foo","org.example.FooBar");
        assertEquals("org.example.FooBar", scriptBuilderResolver.getBuilderClassName(".foo"));
    }

    public void testResetBuilders() {
        scriptBuilderResolver.registerBuilder(".foo","org.example.FooBar");
        scriptBuilderResolver.resetBuilders();
        try {
            scriptBuilderResolver.getBuilderClassName(".foo");
            fail("Retrieving extension of type .foo should have thrown exception");
        } catch (UnsupportedScriptTypeException ex) {
            assertEquals(".foo",ex.getRequestedExtension());
        }
    }

}
