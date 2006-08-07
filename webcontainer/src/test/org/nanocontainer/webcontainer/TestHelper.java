package org.nanocontainer.webcontainer;

import java.io.File;

public class TestHelper {

    public static File getTestWarFile() {
        String testcompJarProperty = System.getProperty("testwar.war");
        if (testcompJarProperty != null) {
            return new File(testcompJarProperty);
        }

        Class aClass = TestHelper.class;
        File base = new File(aClass.getProtectionDomain().getCodeSource().getLocation().getFile());
        File tj = new File(base,"testwar.war");
        while (!tj.exists()) {
            base = base.getParentFile();
            tj = new File(base,"testwar.war");
        }
        return tj;
    }


}
