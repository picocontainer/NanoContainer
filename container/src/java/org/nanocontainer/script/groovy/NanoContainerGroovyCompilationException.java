package org.nanocontainer.script.groovy;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.nanocontainer.script.NanoContainerMarkupException;

import java.util.List;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class NanoContainerGroovyCompilationException extends NanoContainerMarkupException {
    private CompilationFailedException compilationFailedException;

    public NanoContainerGroovyCompilationException(String message, CompilationFailedException e) {
        super(message);
        this.compilationFailedException = e;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getMessage() + "\n");
        List errors = compilationFailedException.getUnit().getErrors();
        // errors can definitely be null:
        if (errors != null) {
            for (int i = 0; i < errors.size(); i++) {
                Object o = errors.get(i);
                if (o instanceof ExceptionMessage) {
                    ExceptionMessage em = (ExceptionMessage) o;
                    sb.append(em.getCause().getMessage() + "\n");
                }
            }
        }
        return sb.toString();
    }
}
