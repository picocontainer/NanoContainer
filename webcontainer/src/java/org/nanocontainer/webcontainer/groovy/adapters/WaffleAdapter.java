package org.nanocontainer.webcontainer.groovy.adapters;

import org.nanocontainer.webcontainer.PicoContext;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.util.Map;

import groovy.util.NodeBuilder;

/**
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @deprecated Use NodeBuilderAdapter
 */
public class WaffleAdapter {
    
    private static final String WAFFLE_NODE_BUILDER = "org.codehaus.waffle.groovy.WaffleNodeBuilder";
    
    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;
    
    public WaffleAdapter(PicoContext context, MutablePicoContainer parentContainer, Map attributes) {
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
            factory.registerComponentImplementation("waffleNodeBuilder", WAFFLE_NODE_BUILDER);
            return (NodeBuilder) factory.getPico().getComponentInstance("waffleNodeBuilder");
        } catch (ClassNotFoundException e) {
            throw new org.nanocontainer.script.BuilderClassNotFoundException(WAFFLE_NODE_BUILDER + " class name not found", e);
        }
    }

}
