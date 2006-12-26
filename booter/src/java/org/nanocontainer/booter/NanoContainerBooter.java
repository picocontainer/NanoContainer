/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/
package org.nanocontainer.booter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;

/**
 * NanoContainerBooter instantiated the NanoContainer {@link org.nanocontainer.Standalone Standalone} 
 * startup class using a tree of common and hidden classloaders.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @see org.nanocontainer.Standalone
 */
public class NanoContainerBooter {

    public static void main(String[] args) throws Exception {
        new NanoContainerBooter(args);
    }

    /**
     * Instantiates the NanoContainer Standalone class
     * @param args the arguments passed on to Standalone
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IOException
     */
    public NanoContainerBooter(String[] args) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        URLClassLoader commonClassLoader = new URLClassLoader(buildClassLoaderURLs("lib/common"),
                        NanoContainerBooter.class.getClassLoader().getParent() );

        URLClassLoader hiddenClassLoader = new URLClassLoader(buildClassLoaderURLs("lib/hidden"), 
                        commonClassLoader );

        Class nanoStandalone = hiddenClassLoader.loadClass("org.nanocontainer.Standalone");
        Constructor constructor = nanoStandalone.getConstructors()[0];
        System.out.println("NanoContainer Booter: Booting...");
        constructor.newInstance(new Object[]{args});
        System.out.println("NanoContainer Booter: Booted.");

    }

    private URL[] buildClassLoaderURLs(String path) throws MalformedURLException{
        List urls = new ArrayList();
        File[] libs = new File(path).listFiles();
        for (int i = 0; i < libs.length; i++) {
            File file = libs[i];
            URL fileURL = file.toURL();
            urls.add(fileURL);
        }
        return (URL[])urls.toArray(new URL[urls.size()]);
    }
}