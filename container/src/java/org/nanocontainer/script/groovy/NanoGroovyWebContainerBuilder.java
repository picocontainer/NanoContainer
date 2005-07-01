package org.nanocontainer.script.groovy;

import org.nanocontainer.NanoContainer;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import groovy.util.BuilderSupport;

public class NanoGroovyWebContainerBuilder extends BuilderSupport  {

    protected void setParent(Object object, Object object1) {
    }

    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        Map attributes = new HashMap();
        attributes.put("class", value);
        return createNode(name, attributes);
    }

    protected Object createNode(Object object, Map map) {
        //TODO.
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Object createNode(Object name, Map attributes, Object object1) {
        return createNode(name, attributes);
    }

    public NanoGroovyWebContainerBuilder(Map attributes, NanoContainer parentContainer) {

    }
}
