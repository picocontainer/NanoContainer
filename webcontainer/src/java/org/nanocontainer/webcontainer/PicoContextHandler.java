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
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.servlet.*;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.EventListener;

// TODO rename to PicoContext
public class PicoContextHandler {

    private final Context context;
    private final PicoContainer parentContainer;
    private final boolean withSessionHandler;
    private PicoServletHandler servletHandler;

    public static final int DEFAULT = 0;
    public static final int REQUEST = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int ERROR = 8;
    public static final int ALL = 15;

    public PicoContextHandler(Context context, PicoContainer parentContainer, boolean sessionManager) {
        this.context = context;
        this.parentContainer = parentContainer;
        this.withSessionHandler = sessionManager;
    }

    public PicoServletHolder addServletWithMapping(Class servletClass, String pathMapping) {
        PicoServletHolder holder = new PicoServletHolder(servletClass, parentContainer);
        context.addServlet(holder, pathMapping);
        return holder;
    }

    public Servlet addServletWithMapping(Servlet servlet, String pathMapping) {
        ServletHolder holder = new ServletHolder(servlet);
        context.addServlet(holder, pathMapping);
        return servlet;
    }

    public PicoFilterHolder addFilterWithMapping(Class filterClass, String pathMapping, int dispatchers) {
        PicoFilterHolder filterHolder = new PicoFilterHolder(filterClass, parentContainer);
        context.addFilter(filterHolder, pathMapping, 0);
        return filterHolder;
    }

    public Filter addFilterWithMapping(Filter filter, String pathMapping, int dispatchers) {
        context.addFilter(new FilterHolder(filter), pathMapping, dispatchers);
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
        context.addServlet(DefaultServlet.class.getName(), "/");
        context.setResourceBase(absolutePath);
    }

    public void setStaticContext(String absolutePath, String welcomePage) {
        context.addServlet(DefaultServlet.class.getName(), "/");
        context.setResourceBase(absolutePath);
        context.setWelcomeFiles(new String[]{welcomePage});
    }

    public void setDefaultHandling(final String absolutePath, String scratchDir, String pageSuffix) {
        context.setResourceBase(absolutePath);
        ServletHolder jspHolder = new PicoServletHolder(parentContainer);
        jspHolder.setName("jsp");
        jspHolder.setClassName("org.apache.jasper.servlet.JspServlet");
        jspHolder.setInitParameter("scratchdir", scratchDir);
        jspHolder.setInitParameter("logVerbosityLevel", "DEBUG");
        jspHolder.setInitParameter("fork", "false");
        jspHolder.setInitParameter("xpoweredBy", "false");
        jspHolder.setForcedPath(null);
        jspHolder.setInitOrder(0);

        context.addServlet(jspHolder, "*.jsp");
        context.addServlet(DefaultServlet.class.getName(), "/");

    }

    public void addErrorHandler() {
        addErrorHandler(new ErrorPageErrorHandler());
    }

    public void addErrorHandler(ErrorHandler handler) {
        context.setErrorHandler(handler);
    }

    //     protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message)
//        throws IOException
  //  {
    //    writeErrorPage(request, writer, code, message, _showStacks);
   // }

}
