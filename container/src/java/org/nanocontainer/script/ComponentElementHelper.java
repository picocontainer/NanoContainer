/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer.script;

import org.picocontainer.Parameter;
import org.nanocontainer.ClassNameKey;
import org.nanocontainer.NanoContainer;


public class ComponentElementHelper {

    public static void makeComponent(Object cnkey, Object key, Parameter[] parameters, Object klass, NanoContainer current, Object instance) {
        if (cnkey != null)  {
            key = new ClassNameKey((String)cnkey);
        }

        if (klass instanceof Class) {
            Class clazz = (Class) klass;
            key = key == null ? clazz : key;
            current.getPico().registerComponentImplementation(key, clazz, parameters);
        } else if (klass instanceof String) {
            String className = (String) klass;
            key = key == null ? className : key;
            try {
                current.registerComponentImplementation(key, className, parameters);
            } catch (ClassNotFoundException e) {
                throw new NanoContainerMarkupException("ClassNotFoundException: " + e.getMessage(), e);
            }
        } else if (instance != null) {
            key = key == null ? instance.getClass() : key;
            current.getPico().registerComponentInstance(key, instance);
        } else {
            throw new NanoContainerMarkupException("Must specify a 'class' attribute for a component as a class name (string) or Class.");
        }
    }


}
