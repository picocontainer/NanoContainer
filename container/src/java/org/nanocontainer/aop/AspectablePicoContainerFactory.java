/*****************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.nanocontainer.aop;

import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;

/**
 * Produces <code>AspectablePicoContainer</code> objects. Mixes in an
 * <code>AspectsContainer</code> with a
 * <code>org.picocontainer.MutablePicoContainer</code> to produce an
 * <code>AspectablePicoContainer</code>.
 *
 * @author Stephen Molitor
 * @version $Revision$
 */
public interface AspectablePicoContainerFactory {

    /**
     * Creates a new <code>AspectablePicoContainer</code>.
     *
     * @param containerClass          the class of the basic container to delegate to.
     * @param aspectsManager          the aspects manager used to register and apply
     *                                aspects.
     * @param componentAdapterFactory the delegate component adapter factory
     *                                used to produce components.
     * @param parent                  the parent container.
     * @return a new <code>AspectablePicoContainer</code>.
     */
    public AspectablePicoContainer createContainer(Class containerClass, AspectsManager aspectsManager,
                                                   ComponentAdapterFactory componentAdapterFactory, PicoContainer parent);

    /**
     * Creates a new <code>AspectablePicoContainer</code>.
     *
     * @param containerClass          the class of the basic container to delegate to.
     * @param componentAdapterFactory the delegate component adapter factory
     *                                used to produce components.
     * @param parent                  the parent container.
     * @return a new <code>AspectablePicoContainer</code>.
     */
    AspectablePicoContainer createContainer(Class containerClass, ComponentAdapterFactory componentAdapterFactory,
                                            PicoContainer parent);

    /**
     * Creates a new <code>AspectablePicoContainer</code>. Uses
     * <code>org.picocontainer.defaults.DefaultPicoContainer</code> as the
     * delegate container.
     *
     * @param componentAdapterFactory the delegate component adapter factory
     *                                used to produce components.
     * @param parent                  the parent container.
     * @return a new <code>AspectablePicoContainer</code>.
     */
    AspectablePicoContainer createContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent);

    /**
     * Creates a new <code>AspectablePicoContainer</code>. Uses
     * <code>org.picocontainer.defaults.DefaultPicoContainer</code> as the
     * delegate container.
     *
     * @param componentAdapterFactory the delegate component adapter factory
     *                                used to produce components.
     * @return a new <code>AspectablePicoContainer</code>.
     */
    AspectablePicoContainer createContainer(ComponentAdapterFactory componentAdapterFactory);

    /**
     * Creates a new <code>AspectablePicoContainer</code>. Uses
     * <code>org.picocontainer.defaults.DefaultPicoContainer</code> as the
     * delegate container. Uses
     * <code>org.picocontainer.defaults.DefaultComponentAdapterFactory</code>
     * as the delegate component adapter factory.
     *
     * @param parent the parent container.
     * @return a new <code>AspectablePicoContainer</code>.
     */
    AspectablePicoContainer createContainer(PicoContainer parent);

    /**
     * Creates a new <code>AspectablePicoContainer</code>. Uses
     * <code>org.picocontainer.defaults.DefaultPicoContainer</code> as the
     * delegate container. Uses
     * <code>org.picocontainer.defaults.DefaultComponentAdapterFactory</code>
     * as the delegate component adapter factory.
     *
     * @return a new <code>AspectablePicoContainer</code>.
     */
    AspectablePicoContainer createContainer();

}