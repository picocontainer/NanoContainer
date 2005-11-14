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

/**
 * Handles 'doCall' nodes.
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 * @version $Revision: 2695 $
 */
public class DoCallNode extends AbstractCustomBuilderNode {

    public static final String NODE_NAME = "doCall";

    public DoCallNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(final NanoContainer parentContainer, final Map attributes) throws ClassNotFoundException {
        // TODO does this node need to be handled?
        return null;
    }
}
