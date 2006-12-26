package org.nanocontainer.script.groovy;

import org.nanocontainer.reflection.DefaultNanoPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;

/**
 * @author Paul Hammant
 * @version $Revision: 3144 $
 */
public class TestContainer extends DefaultNanoPicoContainer {

    public TestContainer(ComponentAdapterFactory caf, PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), caf, parent);
    }

    public TestContainer(PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), parent);
    }

    public TestContainer() {
    }
}
