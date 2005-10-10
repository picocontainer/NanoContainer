package org.nanocontainer;

import junit.framework.TestCase;

/**
 * 
 * @author Mauro Talevi
 */
public class ClassNameKeyTestCase extends TestCase {

    public void testGetClassName(){
        String className = ClassNameKey.class.getName();
        ClassNameKey key = new ClassNameKey(className);
        assertEquals(className, key.getClassName());
    }
}
