package org.nanocontainer.script.groovy;

import junit.framework.Assert;


/**
 * @author Mauro Talevi
 */
public class B extends X {
    A a;

    public B(A a) {
        Assert.assertNotNull(a);
        this.a = a;
    }

    public A getA() {
        return a;
    }
}
