package org.nanocontainer.script.groovy.buildernodes;

import java.util.Map;

import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;

public class DecoratingPicoContainerNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "decoratingPicoContainer";

    public DecoratingPicoContainerNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(Object current, Map attributes) {

        if (!(current instanceof NanoContainer)) {
            throw new NanoContainerMarkupException("Don't know how to create a 'decoratingPicoContainer' child of a '" + current.getClass() + "' parent");
        }

        Class clazz = (Class) attributes.remove("class");
        NanoContainer container = (NanoContainer) current;

        return container.addDecoratingPicoContainer(clazz);
    }

}
