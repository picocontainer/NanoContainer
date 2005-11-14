package org.nanocontainer.script.groovy;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import org.nanocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.PicoContainer;

/**
 *
 * @author Michael Rimov
 * @version 1.0
 */
public class GroovyCompilationExceptionTestCase extends AbstractScriptedContainerBuilderTestCase {


    public void testGroovyCompilationExceptionContainsOriginalReasonInStackTrace() {
        //Bogus script where imports are not kosher.
        Reader script = new StringReader("" +
            "def unresolvedVariable = new TestBean()\n" +
            "");

        try {
            PicoContainer pico = buildContainer(script, null, "Some Assembly Scope");
        } catch (GroovyCompilationException ex) {
            CharArrayWriter outputArray = new CharArrayWriter();
            PrintWriter output = new PrintWriter(outputArray);
            ex.printStackTrace(output);
            String resultingString = outputArray.toString();
//            System.out.println(resultingString);

            assertTrue(resultingString.indexOf("Caused by") > -1);

            //This may change from version to version.  Is there a better way for verification?  -MR
            assertTrue(resultingString.indexOf("No such class") > -1);
        }
    }

    private PicoContainer buildContainer(Reader script, PicoContainer parent, Object scope) {
        return buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, scope);
    }


}
