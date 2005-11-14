/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/

package org.nanocontainer.script.groovy.buildernodes;

import java.util.Map;

import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import groovy.lang.GroovyObject;
import org.nanocontainer.DefaultNanoContainer;
import org.picocontainer.MutablePicoContainer;

/**
 * Handles the child of container 'newBuilder' node.
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 * @version $Revision: 2695 $
 */
public class NewBuilderNode extends AbstractCustomBuilderNode {

    /**
     * Node name we're handling: 'newBuilder'.
     */
    public static final String NODE_NAME = "newBuilder";

    /**
     * Supported attribute: 'class'.
     */
    public static final String CLASS_ATTRIBUTE = "class";

    /**
     * Suppoerted attribute 'validating'.  Indicates that attributes should
     * be validated and NanoContainerMarkupException should be thrown
     * if invalid attributes are found.
     * @todo Not yet implemented. How do we get NanoContainer to register
     * a component instance?  -MR
     */
    public static final String VALIDATE_ATTRIBUTE = "validating";


    public NewBuilderNode() {
        super(NODE_NAME);

        addAttribute(CLASS_ATTRIBUTE);
        addAttribute(VALIDATE_ATTRIBUTE);
    }

    public Object createNewNode(final NanoContainer parentContainer, final Map attributes) throws ClassNotFoundException {
        String builderClass = (String) attributes.remove(CLASS_ATTRIBUTE);

        NanoContainer factory = new DefaultNanoContainer();
        MutablePicoContainer parentPico = parentContainer.getPico();
        factory.getPico().registerComponentInstance(MutablePicoContainer.class, parentPico);
        try {
            factory.registerComponentImplementation(GroovyObject.class, builderClass);
        } catch (ClassNotFoundException e) {
            throw new NanoContainerMarkupException("ClassNotFoundException " + builderClass);
        }
        Object componentInstance = factory.getPico().getComponentInstance(GroovyObject.class);
        return componentInstance;
    }

}
