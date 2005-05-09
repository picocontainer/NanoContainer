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

import org.nanocontainer.aop.defaults.AopDecorationDelegate;
import org.nanocontainer.script.groovy.NanoContainerBuilder;
import org.picocontainer.defaults.ComponentMonitor;

/**
 * A {@link org.nanocontainer.script.groovy.NanoContainerBuilder} that supports
 * scripting of aspects via dynaop.
 *
 * @author Stephen Molitor
 * @version $Revision$
 */
public class DynaopNanoContainerBuilder extends NanoContainerBuilder {

    /**
     * Creates a new <code>DynaopNanoContainerBuilder</code> that will use
     * the default @{link DynaopAspectsManager} to apply aspects.
     */
    public DynaopNanoContainerBuilder(ComponentMonitor componentMonitor) {
        super(new AopDecorationDelegate(new DynaopAspectsManager()));
    }

    /**
     * Creates a new <code>DynaopNanoContainerBuilder</code> that will use
     * the default @{link DynaopAspectsManager} to apply aspects.
     */
    public DynaopNanoContainerBuilder() {
        super(new AopDecorationDelegate(new DynaopAspectsManager()));
    }


}
