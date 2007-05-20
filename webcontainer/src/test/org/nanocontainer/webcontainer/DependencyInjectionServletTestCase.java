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

public class DependencyInjectionServletTestCase extends WebContainerTestCase {

    PicoJettyServer server;
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class servletClass = DependencyInjectionTestServlet.class;
        PicoServletHolder holder = barContext.addServletWithMapping(servletClass, "/foo");
        holder.setInitParameter("foo", "bar");

        server.start();

        assertEquals("hello Fred bar", getPage("http://localhost:8080/bar/foo"));


    }

    public void testCanInstantiateWebContainerContextAndServletInstance() throws InterruptedException, IOException {


        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);

        DependencyInjectionTestServlet servlet0 = new DependencyInjectionTestServlet("Fred");
        DependencyInjectionTestServlet servlet1 = (DependencyInjectionTestServlet)
                barContext.addServletWithMapping(servlet0, "/foo");
        servlet1.setFoo("bar");

        server.start();

        assertEquals("hello Fred bar", getPage("http://localhost:8080/bar/foo"));

    }



}