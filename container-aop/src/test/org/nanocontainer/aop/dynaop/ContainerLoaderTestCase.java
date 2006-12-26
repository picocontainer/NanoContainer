/*****************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.nanocontainer.aop.dynaop;

import junit.framework.TestCase;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Stephen Molitor
 */
public class ContainerLoaderTestCase extends TestCase {

    private ContainerLoader loader = new ContainerLoader();
    private PicoContainer container = new DefaultPicoContainer();

    public void testContainerSet() {
        loader.setContainer(container);
        assertSame(container, loader.getContainer());
    }

    public void testContainerNotSet() {
        try {
            loader.getContainer();
            fail("PicoInitializationException should have been raised");
        } catch (PicoInitializationException e) {
        }
    }

}