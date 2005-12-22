/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.Map;
import java.security.*;

public class CustomPermissionsURLClassLoader extends URLClassLoader {
    private final Map permissionsMap;

    public CustomPermissionsURLClassLoader(URL[] urls, Map permissionsMap, ClassLoader parent) {
        super(urls, parent);
        this.permissionsMap = permissionsMap;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw decorateException(name, e);
        }
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            throw decorateException(name, e);
        }
    }

    private ClassNotFoundException decorateException(String name, ClassNotFoundException e) {
        if (name.startsWith("class ")) {
            return new ClassNotFoundException("Class '" + name + "' is not a classInstance.getName(). " +
                    "It's a classInstance.toString(). The clue is that it starts with 'class ', no classname contains a space.");
        }
        ClassLoader classLoader = this;
        StringBuffer sb = new StringBuffer("'").append(name).append("' classloader stack [");
        while (classLoader != null) {
            sb.append(classLoader.toString()).append("\n");
            final ClassLoader cl = classLoader;
            classLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return cl.getParent();
                }
            });

        }
        return new ClassNotFoundException(sb.append("]").toString(), e);
    }

    public String toString() {
        String result = CustomPermissionsURLClassLoader.class.getName() + " " + System.identityHashCode(this) + ":";
        URL[] urls = getURLs();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            result += "\n\t" + url.toString();
        }

        return result;
    }

    public PermissionCollection getPermissions(CodeSource codeSource) {
        return (Permissions) permissionsMap.get(codeSource.getLocation());
    }

}

