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

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.tck.AbstractImplementationHidingPicoContainerTestCase;
import org.nanocontainer.NanoPicoContainer;

/**
 * @author Paul Hammant
 * @version $Revision$
 */

public class ImplementationHidingNanoPicoContainerTestCase extends AbstractImplementationHidingPicoContainerTestCase {

    protected MutablePicoContainer createImplementationHidingPicoContainer() {
        return new ImplementationHidingNanoPicoContainer();
    }

    protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
        return new ImplementationHidingNanoPicoContainer(this.getClass().getClassLoader(), parent);
    }
    // test methods inherited. This container is part compliant.

    public void testMakeRemoveChildContainer() {
        final NanoPicoContainer parent = (NanoPicoContainer) createPicoContainer(null);
        parent.registerComponentInstance("java.lang.String", "This is a test");
        MutablePicoContainer pico = parent.makeChildContainer();
        // Verify they are indeed wired together.
        assertNotNull(pico.getComponentInstance("java.lang.String"));
        boolean result = parent.removeChildContainer(pico);
        assertTrue(result);
    }

}
