package org.nanocontainer.script;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ConstantParameter;
import org.nanocontainer.ClassNameKey;
import org.nanocontainer.NanoContainer;

import java.util.List;

public class ComponentElementHelper {

    public static void makeComponent(Object cnkey, Object key, Parameter[] parameters, Object klass, NanoContainer current, Object instance) {
        if (cnkey != null)  {
            key = new ClassNameKey((String)cnkey);
        }

        if (klass instanceof Class) {
            Class clazz = (Class) klass;
            key = key == null ? clazz : key;
            current.getPico().registerComponentImplementation(key, clazz, parameters);
        } else if (klass instanceof String) {
            String className = (String) klass;
            key = key == null ? className : key;
            try {
                current.registerComponentImplementation(key, className, parameters);
            } catch (ClassNotFoundException e) {
                throw new NanoContainerMarkupException("ClassNotFoundException: " + e.getMessage(), e);
            }
        } else if (instance != null) {
            key = key == null ? instance.getClass() : key;
            current.getPico().registerComponentInstance(key, instance);
        } else {
            throw new NanoContainerMarkupException("Must specify a 'class' attribute for a component as a class name (string) or Class.");
        }
    }


}
