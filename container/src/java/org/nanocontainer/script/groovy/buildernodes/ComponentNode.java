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

import java.util.List;
import java.util.Map;

import org.nanocontainer.ClassNameKey;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.NodeBuilderDecorationDelegate;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ConstantParameter;

/**
 *
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 * @version $Revision: 2695 $
 */
public class ComponentNode extends AbstractBuilderNode {

    public static final String NODE_NAME =  "component";

    /**
     * Attributes 'key'
     */
    public static final String KEY = "key";

    /**
     * Class attribute.
     */
    private static final String CLASS = "class";

    /**
     * Class Name Key Attribute.
     */
    private static final String CLASS_NAME_KEY = "classNameKey";

    /**
     * Instance attribute name.
     */
    private static final String INSTANCE = "instance";

    /**
     * Parameters attribute name.
     */
    private static final String PARAMETERS = "parameters";


    private final NodeBuilderDecorationDelegate delegate;

    public ComponentNode(NodeBuilderDecorationDelegate builderDelegate) {
        super(NODE_NAME);

        this.delegate = builderDelegate;


        //Supported attributes.
        this.addAttribute(KEY)
            .addAttribute(CLASS)
            .addAttribute(CLASS_NAME_KEY)
            .addAttribute(INSTANCE)
            .addAttribute(PARAMETERS);
    }

    /**
     * Execute the handler for the given node builder.
     * TODO - wrong Javadoc
     * @param name Object the parent object.
     * @param value The Node value. This is almost never used, but it kept
     *   in for consistency with the Groovy Builder API. Normally set to
     *   null.
     * @param current The current node.
     * @param attributes Map attributes specified in the groovy script for
     *   the builder node.
     * @return Object
     */
    public Object createNewNode(final Object current, final Map attributes) {
        delegate.rememberComponentKey(attributes);
        Object key = attributes.remove(KEY);
        Object cnkey = attributes.remove(CLASS_NAME_KEY);
        Object classValue = attributes.remove(CLASS);
        Object instance = attributes.remove(INSTANCE);
        List parameters = (List) attributes.remove(PARAMETERS);

        MutablePicoContainer pico = ((NanoContainer) current).getPico();

        if (cnkey != null)  {
            key = new ClassNameKey((String)cnkey);
        }

        Parameter[] parameterArray = getParameters(parameters);
        if (classValue instanceof Class) {
            Class clazz = (Class) classValue;
            key = key == null ? clazz : key;
            pico.registerComponentImplementation(key, clazz, parameterArray);
        } else if (classValue instanceof String) {
            String className = (String) classValue;
            key = key == null ? className : key;
            try {
                ((NanoContainer) current).registerComponentImplementation(key, className, parameterArray);
            } catch (ClassNotFoundException e) {
                throw new NanoContainerMarkupException("ClassNotFoundException: " + e.getMessage(), e);
            }
        } else if (instance != null) {
            key = key == null ? instance.getClass() : key;
            pico.registerComponentInstance(key, instance);
        } else {
            throw new NanoContainerMarkupException("Must specify a class attribute for a component as a class name (string) or Class. Attributes:" + attributes);
        }

        return this.getNodeName();
    }

    private Parameter[] getParameters(List paramsList) {
        if (paramsList == null) {
            return null;
        }
        int n = paramsList.size();
        Parameter[] parameters = new Parameter[n];
        for (int i = 0; i < n; ++i) {
            parameters[i] = toParameter(paramsList.get(i));
        }
        return parameters;
    }



    private Parameter toParameter(Object obj) {
        return obj instanceof Parameter ? (Parameter) obj : new ConstantParameter(obj);
    }
}
