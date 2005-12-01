/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/
package org.nanocontainer.script.groovy;

import java.security.Permission;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.nanocontainer.ClassPathElement;
import org.nanocontainer.DefaultNanoContainer;
import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.NodeBuilderDecorationDelegate;
import org.nanocontainer.script.NullNodeBuilderDecorationDelegate;
import org.nanocontainer.script.groovy.buildernodes.BeanNode;
import org.nanocontainer.script.groovy.buildernodes.ChildContainerNode;
import org.nanocontainer.script.groovy.buildernodes.ClasspathElementNode;
import org.nanocontainer.script.groovy.buildernodes.ComponentNode;
import org.nanocontainer.script.groovy.buildernodes.DoCallNode;
import org.nanocontainer.script.groovy.buildernodes.NewBuilderNode;
import org.picocontainer.MutablePicoContainer;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.util.BuilderSupport;

/**
 * Builds node trees of PicoContainers and Pico components using GroovyMarkup.
 * <p>Simple example usage in your groovy script:
 * <code><pre>
 * builder = new org.nanocontainer.script.groovy.CustomGroovyNodeBuilder()
 * pico = builder.container(parent:parent) {
 * &nbsp;&nbsp;component(class:org.nanocontainer.testmodel.DefaultWebServerConfig)
 * &nbsp;&nbsp;component(class:org.nanocontainer.testmodel.WebServerImpl)
 * }
 * </pre></code>
 * </p>
 * <h4>Extending/Enhancing CustomGroovyNodeBuilder</h4>
 * <p>Often-times people need there own assembly commands that are needed
 * for extending/enhancing the node builder tree.  The perfect example of this
 * is <tt>DynaopGroovyNodeBuilder</tt> which provides a new vocabulary for
 * the groovy node builder with terms such as 'aspect', 'pointcut', etc.</p>
 * <p>GroovyNodeBuilder provides two primary ways of enhancing the nodes supported
 * by the groovy builder: {@link org.nanocontainer.script.NodeBuilderDecorationDelegate}
 * and special node handlers {@link org.nanocontainer.script.groovy.BuilderNode}.
 * Using NodeBuilderDecorationDelegate is often a preferred method because it is
 * ultimately script independent.  However, replacing an existing CustomGroovyNodeBuilder's
 * behavior is currently the only way to replace the behavior of an existing
 * groovy node handler.
 * </p>
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 * @version $Revision: 2695 $
 */
public class CustomGroovyNodeBuilder extends BuilderSupport {

    private static final String CLASS = "class";

    private static final String GRANT = "grant";

    private static final String PARENT = "parent";

    private static final String EMPTY = "";


    /**
     * Flag indicating that the attribute validation should be performed.
     */
    public static boolean PERFORM_ATTRIBUTE_VALIDATION = true;


    /**
     * Flag indicating that attribute validation should be skipped.
     */
    public static boolean SKIP_ATTRIBUTE_VALIDATION = false;


    /**
     * Decoration delegate. The traditional method of adding functionality to
     * the Groovy builder.
     */
    private final NodeBuilderDecorationDelegate decorationDelegate;

    /**
     * Map of node handlers.
     */
    private Map nodeBuilderHandlers = new HashMap();


    private final boolean performAttributeValidation;


    /**
     * Allows the composition of a <tt>{@link NodeBuilderDecorationDelegate}</tt> -- an
     * object that extends the capabilities of the <tt>CustomGroovyNodeBuilder</tt>
     * with new tags, new capabilities, etc.
     * @param decorationDelegate NodeBuilderDecorationDelegate
     * @param performAttributeValidation should be set to PERFORM_ATTRIBUTE_VALIDATION
     * or SKIP_ATTRIBUTE_VALIDATION
     * @see org.nanocontainer.aop.defaults.AopNodeBuilderDecorationDelegate
     */
    public CustomGroovyNodeBuilder(NodeBuilderDecorationDelegate decorationDelegate, boolean performAttributeValidation) {
        this.decorationDelegate = decorationDelegate;
        this.performAttributeValidation = performAttributeValidation;

        //Build and register node handlers.
        this.setNode(new ComponentNode(decorationDelegate))
            .setNode(new ChildContainerNode(decorationDelegate))
            .setNode(new BeanNode())
            .setNode(new ClasspathElementNode())
            .setNode(new DoCallNode())
            .setNode(new NewBuilderNode())
            .setNode(new ClasspathElementNode());

    }

    /**
     * Default constructor.
     */
    public CustomGroovyNodeBuilder() {
        this(new NullNodeBuilderDecorationDelegate(), SKIP_ATTRIBUTE_VALIDATION);
    }





    protected void setParent(Object parent, Object child) {
    }

    protected Object doInvokeMethod(String s, Object name, Object args) {
        //TODO use setDelegate() from Groovy JSR
        Object answer = super.doInvokeMethod(s, name, args);
        List list = InvokerHelper.asList(args);
        if (!list.isEmpty()) {
            Object o = list.get(list.size() - 1);
            if (o instanceof Closure) {
                Closure closure = (Closure) o;
                closure.setDelegate(answer);
            }
        }
        return answer;
    }

    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        Map attributes = new HashMap();
        attributes.put(CLASS, value);
        return createNode(name, attributes);
    }

    /**
     * Override of create node.  Called by BuilderSupport.  It examines the
    * current state of the builder and the given parameters and dispatches the
     * code to one of the create private functions in this object.
     * @param name The name of the groovy node we're building.  Examples are
     * 'container', and 'grant',
     * @param attributes Map  attributes of the current invocation.
     * @param value A closure passed into the node.  Currently unused.
     * @return Object the created object.
     * @todo Refactor ClassPathElement and grant permissions into custom node
     * builders as well.
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        Object current = getCurrent();
        if (current != null && current instanceof GroovyObject) {
            return createChildBuilder(current, name, attributes);
        } else if (current == null || current instanceof NanoContainer) {
            NanoContainer parent = extractOrCreateValidNanoContainer(attributes, current);
            try {
                //
                //Previously, there was a if name.equals('container') here.  But
                //since container is a registered node handler, then fold
                //the logic here.  -MR
                //

                BuilderNode nodeHandler = this.getNode(name.toString());

                if (nodeHandler == null) {
                    // we don't know how to handle it - delegate to the decorator.
                    return getDecorationDelegate().createNode(name, attributes, current);
                } else {
                    //We found a handler.

                    if (performAttributeValidation) {
                        //Validate
                        nodeHandler.validateScriptedAttributes(attributes);
                    }

                    //Execute.
                    return nodeHandler.createNewNode(parent, attributes);
                }

            } catch (ClassNotFoundException e) {
                throw new NanoContainerMarkupException("ClassNotFoundException: " + e.getMessage(), e);
            }
        } else if (current instanceof ClassPathElement) {
            if (name.equals(GRANT)) {
                return createGrantPermission(attributes, (ClassPathElement) current);
            }
            return EMPTY;
        } else {
            // we don't know how to handle it - delegate to the decorator.
            return getDecorationDelegate().createNode(name, attributes, current);
        }
    }

    /**
     * Pulls the nanocontainer from the 'current' method or possibly creates
     * a new blank one if needed.
     * @param attributes Map the attributes of the current node.
     * @param current Object the current node.
     * @return NanoContainer, never null.
     * @throws NanoContainerMarkupException
     */
    private NanoContainer extractOrCreateValidNanoContainer(final Map attributes,
        final Object current) throws NanoContainerMarkupException {
        NanoContainer parent = (NanoContainer) current;
        Object parentAttribute = attributes.get(PARENT);
        if (parent != null && parentAttribute != null) {
            throw new NanoContainerMarkupException("You can't explicitly specify a parent in a child element.");
        }
        if (parent == null && (parentAttribute instanceof MutablePicoContainer)) {
            // we're not in an enclosing scope - look at parent attribute instead
            parent = new DefaultNanoContainer((MutablePicoContainer) parentAttribute);
        }
        if (parent == null && (parentAttribute instanceof NanoContainer)) {
            // we're not in an enclosing scope - look at parent attribute instead
            parent = (NanoContainer) parentAttribute;
        }
        return parent;
    }


    private Object createChildBuilder(Object current, Object name, Map attributes) {
        GroovyObject groovyObject = (GroovyObject) current;
        return groovyObject.invokeMethod(name.toString(), attributes);
    }

    private Object createGrantPermission(Map attributes, ClassPathElement cpe) {
        Permission perm = (Permission) attributes.remove(CLASS);
        return cpe.grantPermission(perm);

    }

    /**
     * Retrieve the current decoration delegate.
     * @return NodeBuilderDecorationDelegate, should never be null.
     */
    public NodeBuilderDecorationDelegate getDecorationDelegate() {
        return this.decorationDelegate;
    }


    /**
     * Returns an appropriate node handler for a given node and
     * @param tagName String
     * @return CustomGroovyNode the appropriate node builder for the given
     * tag name, or null if no handler exists. (In which case, the Delegate
     * receives the createChildContainer() call)
     */
    public synchronized BuilderNode getNode(final String tagName) {
        return (BuilderNode)nodeBuilderHandlers.get(tagName);
    }

    /**
     * Add's a groovy node handler to the table of possible handlers. If a node
     * handler with the same node name already exists in the map of handlers, then
     * the <tt>GroovyNode</tt> replaces the existing node handler.
     * @param newGroovyNode CustomGroovyNode
     * @return CustomGroovyNodeBuilder to allow for method chaining.
     */
    public synchronized CustomGroovyNodeBuilder setNode(final BuilderNode newGroovyNode) {
        nodeBuilderHandlers.put(newGroovyNode.getNodeName(), newGroovyNode);
        return this;
    }


    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }



}
