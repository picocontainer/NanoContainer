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

import dynaop.MixinFactory;
import org.jmock.MockObjectTestCase;
import org.nanocontainer.testmodel.IdentifiableMixin;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Stephen Molitor
 */
public class ContainerSuppliedMixinFactoryTestCase extends MockObjectTestCase {

    private MutablePicoContainer pico = new DefaultPicoContainer();
    private MixinFactory mixinFactory = new ContainerSuppliedMixinFactory(pico, IdentifiableMixin.class);

    public void testCreate() {
        Object mixin = (IdentifiableMixin) mixinFactory.create(null);
        assertTrue(mixin instanceof IdentifiableMixin);
    }

    public void testPropertiesNotNull() {
        assertNotNull(mixinFactory.getProperties());
    }

}