package org.nanocontainer.tools.deployer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.defaults.ObjectReference;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public interface Deployer {
    ObjectReference deploy(FileObject applicationFolder, ClassLoader parentClassLoader, ObjectReference parentContainerRef) throws FileSystemException, ClassNotFoundException;
}