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
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.alternatives.ImplementationHidingPicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;

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

    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, ComponentAdapterFactory caf, PicoContainer parent) {
        super(new ImplementationHidingPicoContainer(caf, parent), classLoader);
    }


    public ImplementationHidingNanoPicoContainer(ClassLoader classLoader, PicoContainer parent) {
        super(new ImplementationHidingPicoContainer(new DefaultComponentAdapterFactory(), parent), classLoader);
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
     * attributes - ClassLoader, child PicoContainer type, ComponentAdapterFactory -
     * as the parent.
     * <p><tt>Note:</tt> This constructor is protected because are existing scripts
     * that call <tt>new ImplementationHidingNanoPicoContainer(PicoContainer)</tt>, and they get this
     * constructor instead (which has different behavior).</p>
     * @param parent ImplementationHidingNanoPicoContainer
     */
    protected ImplementationHidingNanoPicoContainer(final ImplementationHidingNanoPicoContainer parent) {
        super(parent.getDelegate().makeChildContainer(), parent.getComponentClassLoader());
    }


    protected AbstractNanoPicoContainer createChildContainer() {
        return new ImplementationHidingNanoPicoContainer(this);
    }
}
