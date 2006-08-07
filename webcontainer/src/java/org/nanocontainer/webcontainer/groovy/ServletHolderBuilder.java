package org.nanocontainer.webcontainer.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.mortbay.jetty.servlet.ServletHolder;

public class ServletHolderBuilder extends NodeBuilder {
    
    private ServletHolder servletHolder;

    public ServletHolderBuilder(ServletHolder servlet) {
        this.servletHolder = servlet;
    }

    protected Object createNode(Object name, Map map) {
        if (name.equals("initParam")) {
            return createInitParam(map);
        }
        return null;        
    }

    protected Object createInitParam(Map map) {
        String name = (String) map.remove("name");
        String value = (String) map.remove("value");
        servletHolder.setInitParameter(name, value);
        return null;
    }

}
