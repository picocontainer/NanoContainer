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
import org.picocontainer.defaults.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.LifecycleManager;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.NullComponentMonitor;
import org.picocontainer.defaults.DefaultLifecycleManager;

import java.io.Serializable;

/**
 * This is a MutablePicoContainer that also supports soft composition. i.e. assembly by class name rather that class
 * reference.
 * <p/>
 * In terms of implementation it adopts the behaviour of DefaultPicoContainer and DefaulReflectionContainerAdapter
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @version $Revision$
 */
public class DefaultNanoPicoContainer extends AbstractNanoPicoContainer implements NanoPicoContainer, Serializable {

    private LifecycleManager lifecycleManager;

    public DefaultNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent, LifecycleManager lifcycleManager) {
        super(new DefaultPicoContainer(caf, parent, lifcycleManager), classLoader);
        this.lifecycleManager = lifcycleManager;
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent) {
        this(classLoader, caf, parent, new DefaultLifecycleManager());

    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent, LifecycleManager lifcycleManager) {
        this(classLoader, new DefaultComponentAdapterFactory(), parent, lifcycleManager);
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent) {
        this(classLoader, parent, NullComponentMonitor.getInstance());
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent, ComponentMonitor componentMonitor) {
        super(new DefaultPicoContainer(new DefaultComponentAdapterFactory(componentMonitor), parent), classLoader);
        this.lifecycleManager = new DefaultLifecycleManager(componentMonitor);
    }

    public DefaultNanoPicoContainer(ComponentAdapterFactory caf) {
        super(new DefaultPicoContainer(caf, null), DefaultNanoPicoContainer.class.getClassLoader());
        this.lifecycleManager = new DefaultLifecycleManager();
    }

    public DefaultNanoPicoContainer(PicoContainer pc) {
        super(new DefaultPicoContainer(pc), DefaultNanoPicoContainer.class.getClassLoader());
        this.lifecycleManager = new DefaultLifecycleManager();
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader) {
        super(new DefaultPicoContainer(), classLoader);
        this.lifecycleManager = new DefaultLifecycleManager();
    }

    public DefaultNanoPicoContainer() {
        super(new DefaultPicoContainer(), DefaultNanoPicoContainer.class.getClassLoader());
        this.lifecycleManager = new DefaultLifecycleManager();
    }

    public MutablePicoContainer makeChildContainer(String name) {
        ClassLoader currentClassloader = container.getComponentClassLoader();
        DefaultNanoPicoContainer child = new DefaultNanoPicoContainer(currentClassloader, this, lifecycleManager);
        getDelegate().addChildContainer(child);
        namedChildContainers.put(name, child);
        return child;
    }

}
