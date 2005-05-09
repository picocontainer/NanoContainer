package org.nanocontainer.script.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import org.nanocontainer.script.NanoContainerMarkupException;
import org.nanocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.PicoContainer;

import java.io.Reader;

/**
 * {@inheritDoc}
 * The script has to assign a "pico" variable with an instance of
 * {@link org.picocontainer.PicoContainer}.
 * There is an implicit variable named "parent" that may contain a reference to a parent
 * container. It is recommended to use this as a constructor argument to the instantiated
 * PicoContainer.
 *
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 * @version $Revision$
 */
public class BeanShellContainerBuilder extends ScriptedContainerBuilder {
    public BeanShellContainerBuilder(Reader script, ClassLoader classLoader) {
        super(script, classLoader);
    }

    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {
        Interpreter i = new Interpreter();
        try {
            i.set("parent", parentContainer);
            i.set("assemblyScope", assemblyScope);
            i.eval(script, i.getNameSpace(), "nanocontainer.bsh");
            return (PicoContainer) i.get("pico");
        } catch (EvalError e) {
            throw new NanoContainerMarkupException(e);
        }
    }
}