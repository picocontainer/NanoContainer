/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script.xml;

import org.nanocontainer.script.NanoContainerMarkupException;

/**
 * @deprecated Never thrown anymore
 */
public class EmptyCompositionException extends NanoContainerMarkupException {
    public EmptyCompositionException() {
        super("No components in the XML composition");
    }
}
