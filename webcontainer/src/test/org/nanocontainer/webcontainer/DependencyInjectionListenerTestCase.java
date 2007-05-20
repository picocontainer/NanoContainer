package org.nanocontainer.webcontainer;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.picocontainer.alternatives.EmptyPicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class DependencyInjectionListenerTestCase extends TestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void testCanInstantiateWebContainerContextAndListener() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }

    public void testListenerInvokedBeforeFilterBeforeServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.registerComponentInstance(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);
        barContext.addServletWithMapping(DependencyInjectionTestServlet2.class, "/foo");
        barContext.addFilterWithMapping(DependencyInjectionTestFilter2.class, "/foo", 0);

        server.start();

        URL url = new URL("http://localhost:8080/bar/foo");
        url.openStream();

        assertEquals("-contextInitialized-Filter-Servlet", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-Filter-Servlet-contextDestroyed", sb.toString());

    }


    public void testCanInstantiateWebContainerContextAndListenerInstance() throws InterruptedException, IOException {

        StringBuffer sb = new StringBuffer();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);
        barContext.addListener(new DependencyInjectionTestListener(sb));

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }





}
