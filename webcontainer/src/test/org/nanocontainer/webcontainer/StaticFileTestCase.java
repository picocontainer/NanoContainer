package org.nanocontainer.webcontainer;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.picocontainer.alternatives.EmptyPicoContainer;
import org.mortbay.util.IO;

public class StaticFileTestCase extends WebContainerTestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void testStaticFile() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath());

        server.start();

        assertEquals("<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", getPage("http://localhost:8080/bar/hello.html"));

    }

    public void testDifferentWelcomePage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath(), "hello.html");

        server.start();

        assertEquals("<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", getPage("http://localhost:8080/bar/"));


    }

    public void testMissingPage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath());

        server.start();

        URL url = new URL("http://localhost:8080/bar/HearMeRoar!");
        try {
            url.openStream();
            fail("should have barfed");
        } catch (FileNotFoundException e) {
            // expected
        }

    }

}
