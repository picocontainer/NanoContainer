package org.nanocontainer.webcontainer.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.webcontainer.PicoContext;
import org.picocontainer.PicoContainer;

public class CustomNodeBuilder extends NodeBuilder {
    private final PicoContainer parentContainer;
    private final PicoContext context;
    
    public CustomNodeBuilder(PicoContainer parentContainer, PicoContext context, Map attributes) {
        this.parentContainer = parentContainer;
        this.context = context;
    }

    public Object createNode(Object name, Map attributes) throws NanoContainerMarkupException {        
        String value = (String) attributes.get("name");
        if ( value == null || !value.equals("value") ){
            throw new NanoContainerMarkupException("Attribute 'name' should have value 'value'");
        }
        return value;
    }
    
}