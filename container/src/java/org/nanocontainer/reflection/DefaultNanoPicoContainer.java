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
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * This is a MutablePicoContainer that also supports soft composition. i.e. assembly by class name rather that class
 * reference.
 * <p/>
 * In terms of implementation it adopts the behaviour of DefaultPicoContainer and DefaultNanoContainer
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Michael Rimov
 * @version $Revision$
 */
public class DefaultNanoPicoContainer extends AbstractNanoPicoContainer implements NanoPicoContainer, Serializable {

    public DefaultNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent) {
        super(new DefaultPicoContainer(caf, parent), classLoader);
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent) {
        super(new DefaultPicoContainer(new DefaultComponentAdapterFactory(), parent), classLoader);
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader, PicoContainer parent, ComponentMonitor componentMonitor) {
        super(new DefaultPicoContainer(new DefaultComponentAdapterFactory(componentMonitor), parent), classLoader);
    }

    public DefaultNanoPicoContainer(ComponentAdapterFactory caf) {
        super(new DefaultPicoContainer(caf, null), DefaultNanoPicoContainer.class.getClassLoader());
    }

    public DefaultNanoPicoContainer(PicoContainer pc) {
        super(new DefaultPicoContainer(pc), DefaultNanoPicoContainer.class.getClassLoader());
    }

    public DefaultNanoPicoContainer(ClassLoader classLoader) {
        super(new DefaultPicoContainer(), classLoader);
    }

    public DefaultNanoPicoContainer() {
        super(new DefaultPicoContainer(), DefaultNanoPicoContainer.class.getClassLoader());
    }
    
    /**
     * Copy Constructor.  Makes a new DefaultNanoPicoContainer with the same
     * attributes - ClassLoader, child PicoContainer type, ComponentAdapterFactory - 
     * as the parent.
     * <p><tt>Note:</tt> This constructor is protected because are existing scripts
     * that call <tt>new DefaultNanoPicoContainer(PicoContainer)</tt>, and they get this
     * constructor instead (which has different behavior).</p>
     * @param parent  The object to copy.
     */
    protected DefaultNanoPicoContainer(final DefaultNanoPicoContainer parent) {
        super(parent.getDelegate().makeChildContainer(),  parent.getComponentClassLoader());
    }

    /**
     * Makes a child container with the same basic characteristics of <tt>this</tt>
     * object (ComponentAdapterFactory, PicoContainer type, LifecycleManager, etc)
     * @param name the name of the child container
     * @return The child MutablePicoContainer
     */
    public MutablePicoContainer makeChildContainer(String name) {
        DefaultNanoPicoContainer child = new DefaultNanoPicoContainer(this);
        namedChildContainers.put(name, child);
        return child;
    }

}
