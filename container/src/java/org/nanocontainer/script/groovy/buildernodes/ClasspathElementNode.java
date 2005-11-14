/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/

package org.nanocontainer.script.groovy.buildernodes;

import java.util.Map;

import org.nanocontainer.NanoContainer;
import org.nanocontainer.script.NanoContainerMarkupException;
import java.net.MalformedURLException;
import java.security.PrivilegedAction;
import java.net.URL;
import java.security.AccessController;
import java.io.File;
import org.nanocontainer.ClassPathElement;

/**
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 * @version $Revision: 2695 $
 */
public class ClasspathElementNode extends AbstractCustomBuilderNode {

    public static final String NODE_NAME = "classPathElement";


    private static final String PATH = "path";

    private static final String HTTP = "http://";


    public ClasspathElementNode() {
        super(NODE_NAME);

        addAttribute(PATH);
    }


    public Object createNewNode(NanoContainer parentContainer, Map attributes) throws ClassNotFoundException {
        return createClassPathElementNode(attributes, parentContainer);
    }

    private ClassPathElement createClassPathElementNode(Map attributes, NanoContainer nanoContainer) {

        final String path = (String) attributes.remove(PATH);
        URL pathURL = null;
        try {
            if (path.toLowerCase().startsWith(HTTP)) {
                pathURL = new URL(path);
            } else {
                Object rVal = AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            File file = new File(path);
                            if (!file.exists()) {
                                return new NanoContainerMarkupException("classpath '" + path + "' does not exist ");
                            }
                            return file.toURL();
                        } catch (MalformedURLException e) {
                            return e;
                        }

                    }
                });
                if (rVal instanceof MalformedURLException) {
                    throw (MalformedURLException) rVal;
                }
                if (rVal instanceof NanoContainerMarkupException) {
                    throw (NanoContainerMarkupException) rVal;
                }
                pathURL = (URL) rVal;
            }
        } catch (MalformedURLException e) {
            throw new NanoContainerMarkupException("classpath '" + path + "' malformed ", e);
        }
        return nanoContainer.addClassLoaderURL(pathURL);
    }

}
