/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.webcontainer;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.SessionHandler;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.EventListener;

public class PicoContextHandler {

    private final ContextHandler context;
    private final Server server;
    private final PicoContainer parentContainer;
    private final boolean withSessionHandler;
    private PicoServletHandler servletHandler;

    public static final int DEFAULT = 0;
    public static final int REQUEST = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int ERROR = 8;
    public static final int ALL = 15;

    public PicoContextHandler(ContextHandler context, Server server, PicoContainer parentContainer, boolean sessionManager) {
        this.context = context;
        this.server = server;
        this.parentContainer = parentContainer;
        this.withSessionHandler = sessionManager;
    }

    public PicoServletHolder addServletWithMapping(Class servletClass, String pathMapping) {
        PicoServletHandler handler = getServletHandler();
        return (PicoServletHolder) handler.addServletWithMapping(servletClass, pathMapping);
    }

    public Servlet addServletWithMapping(Servlet servlet, String pathMapping) {
        PicoServletHandler handler = getServletHandler();
        handler.addServletWithMapping(new ServletHolder(servlet), pathMapping);
        return servlet;
    }


    private synchronized PicoServletHandler getServletHandler() {
        if (servletHandler == null) {
            servletHandler = new PicoServletHandler(parentContainer);
            servletHandler.setServer(server);
            if (withSessionHandler) {
                SessionHandler sessionHandler = new SessionHandler();
                sessionHandler.setServer(server);
                context.addHandler(sessionHandler);
                sessionHandler.addHandler(servletHandler);
            } else {
                context.addHandler(servletHandler);
            }
        }
        return servletHandler;
    }

    public PicoFilterHolder addFilterWithMapping(Class filterClass, String pathMapping, int dispatchers) {
        PicoServletHandler handler = getServletHandler();
        return (PicoFilterHolder) handler.addFilterWithMapping(filterClass, pathMapping, dispatchers);
    }

    public Filter addFilterWithMapping(Filter filter, String pathMapping, int dispatchers) {
        PicoServletHandler servletHandler = getServletHandler();
        servletHandler.addFilterWithMapping(new FilterHolder(filter), pathMapping, dispatchers);
        return filter;
    }


    public EventListener addListener(Class listenerClass) {

        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.registerComponentImplementation(EventListener.class, listenerClass);
        EventListener instance = (EventListener) child.getComponentInstance(EventListener.class);

        return addListener(instance);

    }

    public EventListener addListener(EventListener listener) {
        context.addEventListener(listener);
        return listener;
    }


    public void setStaticContext(String absolutePath) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(absolutePath);
        context.addHandler(resourceHandler);
    }

    public void setStaticContext(String absolutePath, String welcomePage) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(absolutePath);
        resourceHandler.setWelcomeFiles(new String[]{welcomePage});
        context.addHandler(resourceHandler);
    }

    public void setDefaultHandling(final String absolutePath, String scratchDir, String pageSuffix) {
        context.setResourceBase(absolutePath);
        PicoServletHandler handler = getServletHandler();
        ServletHolder jspHolder = new PicoServletHolder(parentContainer);
        jspHolder.setName("jsp");
        jspHolder.setClassName("org.apache.jasper.servlet.JspServlet");
        jspHolder.setInitParameter("scratchdir", scratchDir);
        jspHolder.setInitParameter("logVerbosityLevel", "DEBUG");
        jspHolder.setInitParameter("fork", "false");
        jspHolder.setInitParameter("xpoweredBy", "false");
        jspHolder.setForcedPath(null);
        jspHolder.setInitOrder(0);


        handler.addServletWithMapping(jspHolder, pageSuffix);
        handler.addServletWithMapping(DefaultServlet.class.getName(), "/");

    }

    public void setDefaultHandling2(String absolutePath, String scratchDir) {
        //WebXmlConfiguration
    }

}
