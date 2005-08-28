/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/
package org.nanocontainer.boot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;

public class NanoContainerBooter {

    private List commonClassLoaderURLs = new ArrayList();
    private List hiddenClassLoaderURLs = new ArrayList();

    public static void main(String[] args) throws Exception {
        new NanoContainerBooter(args);
    }

    public NanoContainerBooter(String[] args) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        File[] libs = new File("lib/common").listFiles();
        for (int i = 0; i < libs.length; i++) {
            File file = libs[i];
            URL fileURL = file.toURL();
            commonClassLoaderURLs.add(fileURL);
        }

        libs = new File("lib/hidden").listFiles();
        for (int i = 0; i < libs.length; i++) {
            File file = libs[i];
            URL fileURL = file.toURL();
            hiddenClassLoaderURLs.add(fileURL);
        }

        URL[] common = new URL[commonClassLoaderURLs.size()];
        commonClassLoaderURLs.toArray(common);

        URLClassLoader baseClassLoader = new URLClassLoader(common,
                        NanoContainerBooter.class.getClassLoader().getParent() );

        URL[] hidden = new URL[hiddenClassLoaderURLs.size()];
        hiddenClassLoaderURLs.toArray(hidden);

        URLClassLoader hiddenClassLoader = new URLClassLoader(
                        hidden, baseClassLoader );

        Class nanoStandalone = hiddenClassLoader.loadClass("org.nanocontainer.Standalone");
        Constructor ctor = nanoStandalone.getConstructors()[0];
        System.out.println("NanoContainer-Boot: Booting...");
        ctor.newInstance(new Object[]{args});
        System.out.println("NanoContainer-Boot: Booted.");

    }

}