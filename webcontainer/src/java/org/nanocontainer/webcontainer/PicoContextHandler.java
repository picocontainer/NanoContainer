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
import org.mortbay.util.LazyList;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.util.EventListener;

public class PicoContextHandler {

    private final ContextHandler context;
    private final Server server;
    private final PicoContainer parentContainer;
    private PicoServletHandler handler;

    public static final int DEFAULT=0;
    public static final int REQUEST=1;
    public static final int FORWARD=2;
    public static final int INCLUDE=4;
    public static final int ERROR=8;
    public static final int ALL=15;

    public PicoContextHandler(ContextHandler context, Server server, PicoContainer parentContainer) {
        this.context = context;
        this.server = server;
        this.parentContainer = parentContainer;
    }

    public PicoServletHolder addServletWithMapping(Class servletClass, String pathMapping) {
        PicoServletHandler handler = getHandler();
        return (PicoServletHolder) handler.addServletWithMapping(servletClass, pathMapping);
    }

    private synchronized PicoServletHandler getHandler() {
        if (handler == null) {
            handler = new PicoServletHandler(parentContainer);
            context.addHandler(handler);
            handler.setServer(server);
        }
        return handler;
    }

    public PicoFilterHolder addFilterWithMapping(Class filterClass, String pathMapping, int dispatchers) {
        PicoServletHandler handler = getHandler();
        return (PicoFilterHolder) handler.addFilterWithMapping(filterClass, pathMapping, dispatchers);
    }

    public EventListener addListener(Class listenerClass) {

        //context.add
        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.registerComponentImplementation(EventListener.class, listenerClass);
        EventListener instance = (EventListener) child.getComponentInstance(EventListener.class);

        EventListener[] listeners=context.getEventListeners();
        EventListener[] newEventListeners;

        if (listeners != null) {
            listeners = (EventListener[]) listeners.clone();
            newEventListeners = (EventListener[]) LazyList.addToArray(listeners, instance, EventListener.class);
        } else {
            newEventListeners = new EventListener[] {(EventListener) instance};
        }

        context.setEventListeners(newEventListeners);

        return instance;

    }

    public void setStaticContext(String absolutePath) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(absolutePath);
        context.addHandler(resourceHandler);
    }
}
