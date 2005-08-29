package org.nanocontainer.tools.deployer;

import org.apache.commons.vfs.FileObject;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public interface FolderContentHandler {
    void setCurrentChildren(FileObject[] currentChildren);

    FileObject getFolder();
}