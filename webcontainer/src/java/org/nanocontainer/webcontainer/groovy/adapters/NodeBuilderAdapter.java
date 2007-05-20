package org.nanocontainer.webcontainer.groovy.adapters;

import org.nanocontainer.webcontainer.PicoContext;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.util.Map;

import groovy.util.NodeBuilder;

public class NodeBuilderAdapter {
    
    private final String nodeBuilderClassName;    
    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;

    public NodeBuilderAdapter(String nodeBuilderClassName, PicoContext context, MutablePicoContainer parentContainer, Map attributes) {
        this.nodeBuilderClassName = nodeBuilderClassName;
        this.context = context;
        this.parentContainer = parentContainer;
        this.attributes = attributes;
    }
    
    public NodeBuilder getNodeBuilder() {
        NanoContainer factory = new DefaultNanoContainer();
        factory.getPico().registerComponentInstance(PicoContext.class, context);
        factory.getPico().registerComponentInstance(PicoContainer.class, parentContainer);
        factory.getPico().registerComponentInstance(Map.class, attributes);
        try {
            factory.registerComponentImplementation("nodeBuilder", nodeBuilderClassName);
            return (NodeBuilder) factory.getPico().getComponentInstance("nodeBuilder");
        } catch (ClassNotFoundException e) {
            throw new org.nanocontainer.script.BuilderClassNotFoundException(nodeBuilderClassName + " class name not found", e);
        }
    }

}
