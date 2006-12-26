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
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Stephen Molitor
 */
public class PicoContainerProxyTestCase extends TestCase {

    private MutablePicoContainer container = new DefaultPicoContainer();
    private ContainerLoader containerLoader = new ContainerLoader();

    public void testProxy() {
        PicoContainer proxy = PicoContainerProxy.create(containerLoader);
        containerLoader.setContainer(container);

        container.registerComponentInstance("key", "instance");
        assertEquals("instance", proxy.getComponentInstance("key"));
    }

}
