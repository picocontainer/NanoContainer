package org.nanocontainer.script.groovy;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class can generate a Groovy script from a preconfigured container.
 * This script can be passed to {@link GroovyContainerBuilder} to recreate
 * a new container with the same configuration.
 * <p/>
 * This is practical in situations where a container configuration needs
 * to be saved.
 *
 * @author Aslak Helles&oslash;y
 * @version $Revision$
 */
public class GroovyScriptGenerator {
    // This implementation is ugly and naive. But it's all I need for now.
    // When there are more requirements (in the form of tests), we can improve this.
    public String generateScript(MutablePicoContainer pico) {
        StringBuffer groovy = new StringBuffer();
        groovy.append("pico = new org.nanocontainer.reflection.DefaultNanoPicoContainer()\n");

        Collection componentAdapters = pico.getComponentAdapters();
        for (Iterator iterator = componentAdapters.iterator(); iterator.hasNext();) {
            ComponentAdapter componentAdapter = (ComponentAdapter) iterator.next();

            Object componentKey = componentAdapter.getComponentKey();
            String groovyKey = null;
            if (componentKey instanceof Class) {
                groovyKey = ((Class) componentKey).getName();
            } else if (componentKey instanceof String) {
                groovyKey = "\"" + componentKey + "\"";
            }

            Object componentInstance = componentAdapter.getComponentInstance(pico);

            if (componentInstance instanceof String) {
                groovy.append("pico.registerComponentInstance(" + groovyKey + ", \"" + componentInstance + "\")\n");
            } else {
                groovy.append("pico.registerComponentImplementation(" + groovyKey + ", " + componentInstance.getClass().getName() + ")\n");
            }
        }
        return groovy.toString();
    }
}