package org.nanocontainer.webcontainer;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.net.URL;

import org.picocontainer.alternatives.EmptyPicoContainer;
import org.mortbay.util.IO;

public class StaticFileTestCase extends TestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void testStaticFile() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar");
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath());

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/hello.html");
        assertEquals("<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", IO.toString(url.openStream()));

        Thread.sleep(1 * 1000);

    }



}
