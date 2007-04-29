package org.nanocontainer.webcontainer.groovy.adapters;

import org.nanocontainer.webcontainer.PicoContextHandler;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.util.Map;

import groovy.util.NodeBuilder;

public class WaffleAdapter {
    private final PicoContextHandler context;
    private final MutablePicoContainer parentContainer;

    public WaffleAdapter(PicoContextHandler context, MutablePicoContainer parentContainer, Map attributes) {
        this.context = context;
        this.parentContainer = parentContainer;
    }
    public NodeBuilder getNodeBuilder() {
        String className = "com.thoughtworks.waffle.groovy.WaffleNodeBuilder";
        NanoContainer factory = new DefaultNanoContainer();
        factory.getPico().registerComponentInstance(PicoContextHandler.class, context);
        factory.getPico().registerComponentInstance(PicoContainer.class, parentContainer);
        try {
            factory.registerComponentImplementation("wb", className);
            return (NodeBuilder) factory.getPico().getComponentInstance("wb");
        } catch (ClassNotFoundException e) {
            throw new org.nanocontainer.script.BuilderClassNotFoundException(className + " class name not found", e);
        }

    }

}
