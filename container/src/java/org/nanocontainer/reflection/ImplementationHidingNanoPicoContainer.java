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

import java.io.Serializable;

import org.nanocontainer.NanoPicoContainer;
import org.picocontainer.LifecycleManager;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.alternatives.ImplementationHidingPicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultLifecycleManager;

/**
 * This is a MutablePicoContainer that supports soft composition and hides implementations where it can.
 * <p/>
 * In terms of implementation it adopts the behaviour of ImplementationHidingPicoContainer
 * and DefaulNanoContainer
 *
 * @author Paul Hammant
 * @author Michael Rimov
 * @version $Revision$
 */
public class ImplementationHidingNanoPicoContainer extends AbstractNanoPicoContainer implements NanoPicoContainer, Serializable {

    private LifecycleManager lifecycleManager;

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent, LifecycleManager lifecycleManager) {
        super(new ImplementationHidingPicoContainer(caf, parent, lifecycleManager), classLoader);
        this.lifecycleManager = lifecycleManager;
    }

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent) {
        this(classLoader, caf, parent, new DefaultLifecycleManager());
    }

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, PicoContainer parent, LifecycleManager lifecycleManager) {
        super(new ImplementationHidingPicoContainer(new DefaultComponentAdapterFactory(), parent, lifecycleManager), classLoader);
        this.lifecycleManager = lifecycleManager;
    }

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, PicoContainer parent) {
        super(new ImplementationHidingPicoContainer(new DefaultComponentAdapterFactory(), parent), classLoader);
        this.lifecycleManager = new DefaultLifecycleManager();
    }

    public ImplementationHidingNanoPicoContainer(PicoContainer pc) {
        this(ImplementationHidingNanoPicoContainer.class.getClassLoader(), pc);
    }

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader) {
        this(classLoader, null);
    }

    public ImplementationHidingNanoPicoContainer() {
        this(ImplementationHidingNanoPicoContainer.class.getClassLoader(), null);
    }

    /**
     * Copy Constructor.  Makes a new ImplementationHidingNanoPicoContainer with the same
     * attributes - ClassLoader, LifecycleManager, child PicoContainer type, ComponentAdapterFactory - 
     * as the parent.
     * <p><tt>Note:</tt> This constructor is protected because are existing scripts
     * that call <tt>new ImplementationHidingNanoPicoContainer(PicoContainer)</tt>, and they get this
     * constructor instead (which has different behavior).</p>
     * @param parent ImplementationHidingNanoPicoContainer
     */
    protected ImplementationHidingNanoPicoContainer(final ImplementationHidingNanoPicoContainer parent) {
        super(parent.getDelegate().makeChildContainer(), parent.getComponentClassLoader());
        this.lifecycleManager = parent.lifecycleManager;
    }
    

    /**
     * Makes a child container with the same basic characteristics of <tt>this</tt>
     * object (ComponentAdapterFactory, PicoContainer type, LifecycleManager, etc)
     * @param name the name of the child container
     * @return The child MutablePicoContainer
     */    
    public MutablePicoContainer makeChildContainer(String name) {
        ImplementationHidingNanoPicoContainer child = new ImplementationHidingNanoPicoContainer(this);
        namedChildContainers.put(name, child);
        return child;
    }

}
