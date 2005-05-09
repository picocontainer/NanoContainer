/*****************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.nanocontainer.script.groovy;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * @author Stephen Molitor
 * @version $Revision$
 */
public class DummyAdapterFactory implements ComponentAdapterFactory {
    private boolean invoked;
    
    public DummyAdapterFactory() {
    }

    public ComponentAdapter createComponentAdapter(Object componentKey, Class componentImplementation,
            Parameter[] parameters) throws PicoIntrospectionException, AssignabilityRegistrationException,
            NotConcreteRegistrationException {
        return (new DefaultComponentAdapterFactory()).createComponentAdapter(componentKey, componentImplementation,
                parameters);
    }

    public boolean wasInvoked() {
        return invoked;
    }

}