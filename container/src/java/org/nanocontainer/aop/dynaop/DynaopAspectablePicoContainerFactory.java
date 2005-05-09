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

import dynaop.Aspects;
import dynaop.Pointcuts;
import dynaop.ProxyFactory;
import org.nanocontainer.aop.AspectablePicoContainer;
import org.nanocontainer.aop.AspectablePicoContainerFactory;
import org.nanocontainer.aop.AspectsContainer;
import org.nanocontainer.aop.AspectsManager;
import org.nanocontainer.aop.defaults.AspectsComponentAdapterFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Uses dynaop to create <code>AspectablePicoContainer</code> objects.
 *
 * @author Stephen Molitor
 * @version $Revision$
 */
public class DynaopAspectablePicoContainerFactory implements AspectablePicoContainerFactory {

    public AspectablePicoContainer createContainer(Class containerClass, AspectsManager aspectsManager,
                                                   ComponentAdapterFactory componentAdapterFactory, PicoContainer parent) {

        ComponentAdapterFactory aspectsCaFactory = new AspectsComponentAdapterFactory(aspectsManager,
                componentAdapterFactory);
        MutablePicoContainer pico = createBasicContainer(containerClass, aspectsCaFactory, parent);

        Aspects aspects = new Aspects();
        aspects.mixin(Pointcuts.ALL_CLASSES, new Class[]{AspectsContainer.class}, new InstanceMixinFactory(aspectsManager));
        aspects.interfaces(Pointcuts.ALL_CLASSES, new Class[]{AspectablePicoContainer.class});

        return (AspectablePicoContainer) ProxyFactory.getInstance(aspects).wrap(pico);
    }

    public AspectablePicoContainer createContainer(Class containerClass,
                                                   ComponentAdapterFactory componentAdapterFactory, PicoContainer parent) {
        return createContainer(containerClass, new DynaopAspectsManager(), componentAdapterFactory, parent);
    }

    public AspectablePicoContainer createContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent) {
        return createContainer(DefaultPicoContainer.class, componentAdapterFactory, parent);
    }

    public AspectablePicoContainer createContainer(ComponentAdapterFactory componentAdapterFactory) {
        return createContainer(componentAdapterFactory, null);
    }

    public AspectablePicoContainer createContainer(PicoContainer parent) {
        return createContainer(new DefaultComponentAdapterFactory(), parent);
    }

    public AspectablePicoContainer createContainer() {
        return createContainer(new DefaultComponentAdapterFactory());
    }

    private MutablePicoContainer createBasicContainer(Class containerClass, ComponentAdapterFactory caFactory,
                                                      PicoContainer parent) {
        MutablePicoContainer temp = new DefaultPicoContainer();
        temp.registerComponentImplementation(containerClass);
        temp.registerComponentInstance(ComponentAdapterFactory.class, caFactory);
        if (parent != null) {
            temp.registerComponentInstance(PicoContainer.class, parent);
        }
        return (MutablePicoContainer) temp.getComponentInstance(containerClass);
    }

}