package org.nanocontainer.script;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import org.nanocontainer.script.groovy.GroovyContainerBuilder;
import junit.framework.TestCase;

public class ScriptedContainerBuilderFactoryTestCase
    extends TestCase {

    private static final String TEST_SCRIPT_PATH = "/org/nanocontainer/nanocontainer.groovy";


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testScriptedContainerBuilderFactoryWithUrl() throws ClassNotFoundException {
        URL resource = getClass().getResource(TEST_SCRIPT_PATH);
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());
    }

    public void testBuildWithReader() throws ClassNotFoundException {
        Reader script = new StringReader("" +
            "import org.nanocontainer.script.groovy.X\n" +
            "import org.nanocontainer.script.groovy.A\n" +
            "X.reset()\n" +
            "builder = new org.nanocontainer.script.groovy.GroovyNodeBuilder()\n" +
            "nano = builder.container {\n" +
            "    component(A)\n" +
            "}");

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(script,
            GroovyContainerBuilder.class.getName());
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());
    }

    public void testBuildWithFile() throws ClassNotFoundException, IOException {
        File resource = new File("src/test/org/nanocontainer/nanocontainer.groovy");
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());

    }


}
