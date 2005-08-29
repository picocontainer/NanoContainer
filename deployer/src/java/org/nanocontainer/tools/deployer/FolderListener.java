package org.nanocontainer.tools.deployer;

import org.apache.commons.vfs.FileObject;

public interface FolderListener {
    void folderAdded(FileObject folder);
    void folderRemoved(FileObject fileObject);
}