/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.nanocontainer.reflection;

import org.nanocontainer.NanoPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.LifecycleManager;
import org.picocontainer.tck.AbstractPicoContainerTestCase;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class DefaultNanoPicoContainerTestCase extends AbstractPicoContainerTestCase {

    protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
        return new DefaultNanoPicoContainer(this.getClass().getClassLoader(), parent);
    }

    protected MutablePicoContainer createPicoContainer(PicoContainer parent, LifecycleManager lifecycleManager) {
        return new DefaultNanoPicoContainer(this.getClass().getClassLoader(), parent, lifecycleManager);
    }

    // TODO - go to a Nano TCK?
    public void testNamedChildContainerIsAccessible() {
        StringBuffer sb = new StringBuffer();
        final NanoPicoContainer parent = (NanoPicoContainer) createPicoContainer(null);
        parent.registerComponentInstance(sb);
        final NanoPicoContainer child = (NanoPicoContainer) parent.makeChildContainer("foo");
        child.registerComponentImplementation(LifeCycleMonitoring.class,LifeCycleMonitoring.class);
        LifeCycleMonitoring o = (LifeCycleMonitoring) parent.getComponentInstance("foo/*" + LifeCycleMonitoring.class.getName());
        assertNotNull(o);
    }

    // TODO - go to a Nano TCK?
    public void testNamedChildContainerIsAccessibleForStringKeys() {
        StringBuffer sb = new StringBuffer();
        final NanoPicoContainer parent = (NanoPicoContainer) createPicoContainer(null);
        parent.registerComponentInstance(sb);
        final MutablePicoContainer child = parent.makeChildContainer("foo");
        child.registerComponentImplementation("lcm",LifeCycleMonitoring.class);
        Object o = parent.getComponentInstance("foo/lcm");
        assertNotNull(o);
        assertTrue(sb.toString().indexOf("-instantiated") != -1);
    }

    // TODO - go to a Nano TCK?
    public void testNamedChildContainerIsAccessibleForClassKeys() {
        StringBuffer sb = new StringBuffer();
        final NanoPicoContainer parent = (NanoPicoContainer) createPicoContainer(null);
        parent.registerComponentInstance(sb);
        final MutablePicoContainer child = parent.makeChildContainer("foo");
        child.registerComponentImplementation(LifeCycleMonitoring.class,LifeCycleMonitoring.class);
        Object o = parent.getComponentInstance("foo/*" + LifeCycleMonitoring.class.getName());
        assertNotNull(o);
        assertTrue(sb.toString().indexOf("-instantiated") != -1);
    }

    // test methods inherited. This container is otherwise fully compliant.
}
