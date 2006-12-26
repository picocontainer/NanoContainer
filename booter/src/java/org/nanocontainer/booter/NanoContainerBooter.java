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

/**
 * NanoContainerBooter instantiated the NanoContainer {@link org.nanocontainer.Standalone Standalone} 
 * startup class using a tree of common and hidden classloaders.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 * @see org.nanocontainer.Standalone
 */
public class NanoContainerBooter {

    private static final String COMMON_PATH = "lib/common";
    private static final String HIDDEN_PATH = "lib/hidden";

    /**
     * Static entry point to NanoContainerBooter
     * @param args the arguments passed on to Standalone
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new NanoContainerBooter(args);
    }

    /**
     * Instantiates the NanoContainer Standalone class
     * 
     * @param args the arguments passed on to Standalone
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IOException
     */
    public NanoContainerBooter(String[] args) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        URLClassLoader commonClassLoader = new URLClassLoader(toURLs(COMMON_PATH),
                        NanoContainerBooter.class.getClassLoader().getParent() );

        URLClassLoader hiddenClassLoader = new URLClassLoader(toURLs(HIDDEN_PATH), 
                        commonClassLoader );

        System.out.println("NanoContainer Booter: Booting...");
        newStandalone(hiddenClassLoader, args);
        System.out.println("NanoContainer Booter: Booted.");

    }

    /**
     * Instantiates a new Standalone 
     * 
     * @param classLoader the ClassLoader used to instantiate class
     * @param args the arguments passed to Standalone
     * 
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void newStandalone(URLClassLoader classLoader, String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class nanoStandalone = classLoader.loadClass("org.nanocontainer.Standalone");
        Constructor constructor = nanoStandalone.getConstructors()[0];
        constructor.newInstance(new Object[]{args});
    }

    /**
     * Converts files path to URLs
     * @param path the files path
     * @return The array of URLs, one for each file in path
     * @throws MalformedURLException
     */
    private URL[] toURLs(String path) throws MalformedURLException{
        File[] files = new File(path).listFiles();
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i]= files[i].toURL();
        }
        return urls;
    }
}