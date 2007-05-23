package org.nanocontainer.testmodel;

import java.util.List;

/**
 * Test for list support.
 * 
 * @author Jeff Steward
 */
public class ListSupport
{
    private List aListOfEntityObjects;

    public ListSupport(List aListOfEntityObjects)
    {
        this.aListOfEntityObjects = aListOfEntityObjects;
    }
    
    public List getAListOfEntityObjects()
    {
        return aListOfEntityObjects;
    }
}
