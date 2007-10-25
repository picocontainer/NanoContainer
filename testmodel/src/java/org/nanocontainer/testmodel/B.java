package org.nanocontainer.testmodel;



/**
 * @author Mauro Talevi
 */
public class B extends X {
    public A a;

    public B(A a) {
		if (a == null) {
			throw new NullPointerException("a");
		}
        this.a = a;
    }

    public A getA() {
        return a;
    }
}
