package org.nanocontainer.testmodel;

import java.util.Map;

public class MapSupport
{
    private Map aMapOfEntities;
    
    public MapSupport(Map aMapOfEntities)
    {
        this.aMapOfEntities = aMapOfEntities;
    }
    
    public Map getAMapOfEntities()
    {
        return aMapOfEntities;
    }
}
