package org.nanocontainer.tools.deployer;

import org.picocontainer.PicoException;

/**
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class DeploymentException extends PicoException {
    public DeploymentException(Throwable t) {
        super(t);
    }
}