/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.nanowar.server;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.mortbay.io.IO;
import org.picocontainer.defaults.DefaultPicoContainer;

public class DependencyInjectionServletTestCase extends TestCase {

    public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.registerComponentInstance(String.class, "Fred");

        PicoJettyServer server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContextHandler barContext = server.createContext("/bar");
        Class servletClass = DependencyInjectionTestServlet.class;
        PicoServletHolder holder = barContext.addServletWithMapping(servletClass, "/foo");
        holder.setInitParameter("foo", "bar");
       
        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));

        //Thread.sleep(50 * 1000);

        server.stop();

    }


}