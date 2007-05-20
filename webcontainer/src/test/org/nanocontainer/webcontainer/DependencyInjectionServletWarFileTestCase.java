package org.nanocontainer.webcontainer;

import java.io.File;
import java.io.IOException;

import org.mortbay.jetty.webapp.WebAppContext;
import org.picocontainer.defaults.DefaultPicoContainer;

public class DependencyInjectionServletWarFileTestCase extends WebContainerTestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void testCanInstantiateWebContainerContextAndServlet()
            throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);
        parentContainer.registerComponentInstance(Integer.class, new Integer(5));

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        WebAppContext wac = server.addWebApplication("/bar", testWar.getAbsolutePath().replace('\\','/'));
        assertNotNull(wac);

        server.start();


        assertEquals("hello Fred bar", getPage("http://localhost:8080/bar/foo"));

        assertEquals("-contextInitialized", sb.toString());

    }

    public void testCanHostJspPage()
            throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");
        parentContainer.registerComponentInstance(StringBuffer.class, new StringBuffer());
        parentContainer.registerComponentInstance(Integer.class, new Integer(5));

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        WebAppContext wac = server.addWebApplication("/bar", testWar.getAbsolutePath().replace('\\','/'));
        assertNotNull(wac);

        server.start();

        assertEquals("<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Test JSP</TITLE>\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "    hello\n" +
                "  </BODY>\n" +
                "</HTML>", getPage("http://localhost:8080/bar/test.jsp"));


    }


}
