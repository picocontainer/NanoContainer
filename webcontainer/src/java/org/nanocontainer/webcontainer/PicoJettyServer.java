/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.webcontainer;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.alternatives.EmptyPicoContainer;

public class PicoJettyServer extends EmptyPicoContainer implements PicoContainer, Startable {

    private final Server server;
    private final PicoContainer parentContainer;
    
    public PicoJettyServer(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
        server = new Server();
        server.setHandler(new HandlerList());
    }

    public PicoJettyServer(String host, int port, PicoContainer parentContainer) {
        this(parentContainer);
        createBlockingChannelConnector(host, port);
    }

    public Connector createBlockingChannelConnector(String host, int port) {
        BlockingChannelConnector connector = new BlockingChannelConnector();
        connector.setHost(host);
        connector.setPort(port);
        server.addConnector(connector);
        return connector;
    }

    public PicoContext createContext(String contextPath, boolean withSessionHandler) {
        Context context = new Context(server, contextPath, Context.SESSIONS);
        return new PicoContext(context, parentContainer, withSessionHandler);
    }

    public PicoWebAppContext addWebApplication(String contextPath, String warFile) {
        PicoWebAppContext context = new PicoWebAppContext(parentContainer);
        context.setContextPath(contextPath);
        context.setExtractWAR(true);
        context.setWar(warFile);
        context.setParentLoaderPriority(true);
        server.addHandler(context);
        return context;
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new JettyServerLifecycleException("Jetty couldn't start", e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new JettyServerLifecycleException("Jetty couldn't stop", e);
        }
    }

    public void addRequestLog(RequestLog requestLog) {
        RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(requestLog);
        server.addHandler(handler);
    }

}
