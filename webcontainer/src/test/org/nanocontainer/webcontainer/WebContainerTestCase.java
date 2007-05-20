package org.nanocontainer.webcontainer;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.mortbay.util.IO;

public abstract class WebContainerTestCase extends TestCase {

    protected String getPage(String url) throws IOException, InterruptedException {
        try {
            return IO.toString(new URL(url).openStream());
        } catch (Exception e) {
            Thread.sleep(2 * 1000);
            try {
                return IO.toString(new URL(url).openStream());
            } catch (Exception e1) {
                return e1.getClass().getName() + ":" + e1.getMessage();
            }
        }
    }


}
