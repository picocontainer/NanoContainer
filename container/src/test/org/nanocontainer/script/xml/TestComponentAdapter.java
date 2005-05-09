/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/
package org.nanocontainer.script.xml;


import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractComponentAdapter;

/**
 * component adapter to test script instantiation.
 */
public class TestComponentAdapter extends AbstractComponentAdapter {

    String foo;
    String blurge;
    int bar;

    public TestComponentAdapter(String foo, int bar, String blurge) {
        super(TestComponentAdapter.class, TestComponentAdapter.class);
        this.foo = foo;
        this.bar = bar;
        this.blurge = blurge;
    }


    public void verify(PicoContainer pico) {
    }

    public Object getComponentInstance(PicoContainer pico) {
        return null;
    }
}




