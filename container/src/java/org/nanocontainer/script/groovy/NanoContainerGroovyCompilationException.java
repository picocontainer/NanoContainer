package org.nanocontainer.script.groovy;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.nanocontainer.script.NanoContainerMarkupException;

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
        List errors = getErrors(compilationFailedException);
        for (int i = 0; i < errors.size(); i++) {
            Object o = errors.get(i);
            if (o instanceof ExceptionMessage) {
                ExceptionMessage em = (ExceptionMessage) o;
                sb.append(em.getCause().getMessage() + "\n");
            }
        }
        return sb.toString();
    }

    private List getErrors(CompilationFailedException e) {
        List errors = e.getUnit().getErrorCollector().getErrors();
        // errors can definitely be null:
        if ( errors == null ){
            return Collections.EMPTY_LIST;                
        }
        return errors;
    }
}
