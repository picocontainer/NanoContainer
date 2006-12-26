/*****************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.nanocontainer.aop.defaults;

import org.nanocontainer.aop.AspectsApplicator;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.DecoratingComponentAdapter;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * @author Stephen Molitor
 */
public class AspectsComponentAdapter extends DecoratingComponentAdapter {

    private final AspectsApplicator aspectsApplicator;

    public AspectsComponentAdapter(AspectsApplicator aspectsApplicator, ComponentAdapter delegate) {
        super(delegate);
        this.aspectsApplicator = aspectsApplicator;
    }

    public Object getComponentInstance(PicoContainer pico) throws PicoInitializationException, PicoIntrospectionException,
            AssignabilityRegistrationException, NotConcreteRegistrationException {
        Object component = super.getComponentInstance(pico);
        return aspectsApplicator.applyAspects(getComponentKey(), component, pico);
    }

}