package org.nanocontainer;

import java.io.Serializable;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

public class ClassPathElement implements Serializable {
    private final URL url;
    private final List permissions = new ArrayList();
    private PermissionCollection permissionCollection;

    public ClassPathElement(URL url) {
        this.url = url;

    }

    public Permission grantPermission(Permission permission) {
        permissions.add(permission);
        return permission;
    }

    public URL getUrl() {
        return url;
    }

    public PermissionCollection getPermissionCollection() {
        if (permissionCollection == null) {
            permissionCollection = new Permissions();
            for (int i = 0; i < permissions.size(); i++) {
                Permission permission = (Permission) permissions.get(i);
                permissionCollection.add(permission);
            }
        }
        return permissionCollection;
    }

    public String toString() {
        return "[" + System.identityHashCode(this) + " " + url + " " + permissions.size() +"]";
    }

}
