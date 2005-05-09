/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.nanocontainer.script;

import org.nanocontainer.integrationkit.LifecycleContainerBuilder;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/**
 * Baseclass for container builders based on scripting.
 *
 * @author Aslak Helles&oslash;y
 * @author Obie Fernandez
 * @version $Revision$
 */
public abstract class ScriptedContainerBuilder extends LifecycleContainerBuilder {
    protected final Reader script;
    protected final ClassLoader classLoader;
    protected final URL scriptURL;

    public ScriptedContainerBuilder(URL scriptURL, ClassLoader classLoader) {
        this.scriptURL = scriptURL;
        this.classLoader = classLoader;
        this.script = null;
    }

    /**
     * @deprecated
     */
    public ScriptedContainerBuilder(Reader script, ClassLoader classLoader) {
        if(script == null) { throw new NullPointerException("script was null"); }
        if(classLoader == null) { throw new NullPointerException("classLoader was null"); }
        this.script = script;
        this.classLoader = classLoader;
        this.scriptURL = null;
    }

    protected final PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope) {
        try {
            return createContainerFromScript(parentContainer, assemblyScope);
        } finally {
            try {
                if (script != null) {
                    script.close();
                }
            } catch (IOException e) {
            }
        }
    }

    // TODO: This should really return NanoContainer using a nano variable in the script. --Aslak
    protected abstract PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope);

    protected void composeContainer(MutablePicoContainer container, Object assemblyScope) {
        // do nothing. assume that this is done in createContainer().
    }
}