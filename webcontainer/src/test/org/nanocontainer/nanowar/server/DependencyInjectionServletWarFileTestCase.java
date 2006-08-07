package org.nanocontainer.nanowar.server;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.mortbay.io.IO;
import org.mortbay.jetty.webapp.WebAppContext;
import org.picocontainer.defaults.DefaultPicoContainer;

public class DependencyInjectionServletWarFileTestCase extends TestCase {

    public void testCanInstantiateWebContainerContextAndServlet()
            throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");

        PicoJettyServer server = new PicoJettyServer("localhost", 8080, parentContainer);
        WebAppContext wac = server.addWebApplication("/bar", "testwar.war");
        assertNotNull(wac);
        
        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred", IO.toString(url.openStream()));

        //Thread.sleep(50 * 1000);

        server.stop();

    }


}
