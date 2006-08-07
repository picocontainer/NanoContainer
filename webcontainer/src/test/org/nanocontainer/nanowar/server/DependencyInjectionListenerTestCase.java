package org.nanocontainer.nanowar.server;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

import org.picocontainer.defaults.DefaultPicoContainer;
import org.mortbay.io.IO;

public class DependencyInjectionListenerTestCase extends TestCase {

    public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);

        PicoJettyServer server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContextHandler barContext = server.createContext("/bar");
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());


    }


}
