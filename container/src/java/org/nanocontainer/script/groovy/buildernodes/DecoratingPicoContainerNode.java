package org.nanocontainer.script.groovy.buildernodes;

import org.nanocontainer.script.groovy.BuilderNode;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.ClassPathElement;
import org.nanocontainer.NanoContainer;

import java.util.Map;
import java.security.Permission;

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
