package org.nanocontainer.webcontainer;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.alternatives.EmptyPicoContainer;
import org.nanocontainer.webcontainer.PicoJettyServer;
import org.nanocontainer.webcontainer.PicoContextHandler;
import org.mortbay.util.IO;

public class DependencyInjectionListenerTestCase extends TestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1 * 1000);
    }

    public void testCanInstantiateWebContainerContextAndListener() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContextHandler barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }

    public void testListenerInvokedBeforeServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContextHandler barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);
        barContext.addServletWithMapping(DependencyInjectionTestServlet2.class, "/foo");

        server.start();

        Thread.sleep(1 * 1000);
        URL url = new URL("http://localhost:8080/bar/foo");
        url.openStream();

        assertEquals("-contextInitialized-hello", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-hello-contextDestroyed", sb.toString());

    }


    public void testCanInstantiateWebContainerContextAndListenerInstance() throws InterruptedException, IOException {

        StringBuffer sb = new StringBuffer();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar", false);
        barContext.addListener(new DependencyInjectionTestListener(sb));

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }





}
