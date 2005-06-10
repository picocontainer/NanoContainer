package org.nanocontainer.script.xml;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mauro Talevi
 */
public class SpecialDateFormat extends SimpleDateFormat {

    Date someDate;
    
    public SpecialDateFormat() {
        super();
    }

    /**
     * @return Returns the someDate.
     */
    public Date getSomeDate() {
        return someDate;
    }
    /**
     * @param someDate The someDate to set.
     */
    public void setSomeDate(Date someDate) {
        this.someDate = someDate;
    }
}
