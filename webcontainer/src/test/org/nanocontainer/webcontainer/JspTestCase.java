package org.nanocontainer.webcontainer;

import junit.framework.TestCase;
import org.mortbay.util.IO;
import org.picocontainer.alternatives.EmptyPicoContainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class JspTestCase extends TestCase {

    PicoJettyServer server;

    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1 * 1000);
    }


    public void testCanInstantiateWebContainerContextAndSimpleJspPage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar");
        String absolutePath = warFile.getParentFile().getAbsolutePath();
        String scratchDir = warFile.getParentFile().getParentFile().getParentFile().getAbsolutePath() + File.separator + "target" + File.separator + "work";
        new File(scratchDir).mkdirs();
        barContext.setDefaultHandling(absolutePath + "/", scratchDir, "*.jsp");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/org/nanocontainer/webcontainer/test.jsp");
        assertEquals("<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Test JSP</TITLE>\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "    hello\n" +
                "  </BODY>\n" +
                "</HTML>", IO.toString(url.openStream()));

        Thread.sleep(1 * 1000);


    }


}
