package org.nanocontainer.script;

import org.picocontainer.alternatives.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.Parameter;

import java.util.HashMap;
import java.util.ArrayList;

import junit.framework.Assert;

public class FooDecoratingPicoContainer extends AbstractDelegatingMutablePicoContainer {
    public FooDecoratingPicoContainer(MutablePicoContainer delegate) {
        super(delegate);
    }
    public MutablePicoContainer makeChildContainer() {
        return null;
    }

    public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation, Parameter[] parameters) throws PicoRegistrationException {
        Assert.assertEquals(HashMap.class, componentImplementation);
        return super.registerComponentImplementation(ArrayList.class, ArrayList.class, parameters);
    }
    public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation) throws PicoRegistrationException {
        Assert.assertEquals(HashMap.class, componentImplementation);
        return super.registerComponentImplementation(ArrayList.class, ArrayList.class);
    }

}
