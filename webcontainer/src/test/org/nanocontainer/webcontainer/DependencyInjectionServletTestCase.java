/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.webcontainer;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.mortbay.util.IO;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.alternatives.EmptyPicoContainer;

public class DependencyInjectionServletTestCase extends TestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1 * 1000);
    }

    public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContextHandler barContext = server.createContext("/bar");
        Class servletClass = DependencyInjectionTestServlet.class;
        PicoServletHolder holder = barContext.addServletWithMapping(servletClass, "/foo");
        holder.setInitParameter("foo", "bar");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));


    }

    public void testCanInstantiateWebContainerContextAndServletInstance() throws InterruptedException, IOException {


        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContextHandler barContext = server.createContext("/bar");
        
        DependencyInjectionTestServlet servlet = (DependencyInjectionTestServlet)
                barContext.addServletWithMapping(new DependencyInjectionTestServlet("Fred"), "/foo");
        servlet.setFoo("bar");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));


    }



}