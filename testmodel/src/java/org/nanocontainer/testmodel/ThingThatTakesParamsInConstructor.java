/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.nanocontainer.testmodel;

public class ThingThatTakesParamsInConstructor {
    private String value;
    private Integer intValue;

    public ThingThatTakesParamsInConstructor(String value, Integer intValue) {
        this.value = value;
        this.intValue = intValue;
    }

    public String getValue() {
        return value + intValue;
    }
}
