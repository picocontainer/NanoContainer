package org.nanocontainer.testmodel;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class TestComponentAdapterFactory extends DefaultComponentAdapterFactory {

    public StringBuffer sb;

    public TestComponentAdapterFactory(StringBuffer sb) {
        this.sb = sb;
    }

    public ComponentAdapter createComponentAdapter(Object componentKey, Class componentImplementation, Parameter[] parameters) throws PicoIntrospectionException, AssignabilityRegistrationException, NotConcreteRegistrationException {
        sb.append("called");
        return super.createComponentAdapter(componentKey, componentImplementation, parameters);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
