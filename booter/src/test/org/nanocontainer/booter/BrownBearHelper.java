package org.nanocontainer.booter;

import org.nanocontainer.DefaultNanoContainer;

import java.util.Map;

public class BrownBearHelper {

    public BrownBearHelper() throws ClassNotFoundException {
       DefaultNanoContainer nano = new DefaultNanoContainer();
        nano.registerComponentImplementation(Map.class, "java.util.HashMap");
    }

}
