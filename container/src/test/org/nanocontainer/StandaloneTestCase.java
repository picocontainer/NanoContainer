/*****************************************************************************
 * Copyright (C) NanoContainer Organization. All rights reserved.            *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.nanocontainer;

import junit.framework.TestCase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;


/**
 * @author Mauro Talevi
 */
public class StandaloneTestCase extends TestCase {

    public void testShouldBeAbleToInvokeMainMethodWithScriptFromFile() throws IOException, ClassNotFoundException {
        File absoluteScriptPath = getAbsoluteScriptPath();
        Standalone.main(new String[] {
            "-c",
            absoluteScriptPath.getAbsolutePath(),
            "-n"
        });
    }

    public void testShouldBeAbleToInvokeMainMethodWithScriptFromClasspathWithXmlIncludes() throws IOException, ClassNotFoundException {
        Standalone.main(new String[] {
            "-r",
            "/org/nanocontainer/standalone_test.xml",
            "-n"
        });
    }

    private File getAbsoluteScriptPath() {
        String className = getClass().getName();
        String relativeClassPath = "/" + className.replace('.', '/') + ".class";
        URL classURL = Standalone.class.getResource(relativeClassPath);
        String absoluteClassPath = classURL.getFile();
        File absoluteDirPath = new File(absoluteClassPath).getParentFile();
        File absoluteScriptPath = new File(absoluteDirPath, "nanocontainer.groovy");
        return absoluteScriptPath;
    }

    public void testCommandLineWithNoArgs() throws Exception {
        try {
            Standalone.getCommandLine(new String[]{}, Standalone.createOptions());
        } catch (ParseException e) {
            assertEquals("Exception message", "No arguments specified", e.getMessage());
        }
    }

    public void testCommandLineWithHelp() throws Exception {
        CommandLine cl = Standalone.getCommandLine(new String[]{"-h"}, Standalone.createOptions());
        assertTrue(cl.hasOption('h'));
        assertFalse(cl.hasOption('v'));
        assertNull(cl.getOptionValue('c'));
        assertFalse(cl.hasOption('q'));
        assertFalse(cl.hasOption('n'));
    }

    public void testCommandLineWithVersion() throws Exception {
        CommandLine cl = Standalone.getCommandLine(new String[]{"-v"}, Standalone.createOptions());
        assertFalse(cl.hasOption('h'));
        assertTrue(cl.hasOption('v'));
        assertNull(cl.getOptionValue('c'));
        assertFalse(cl.hasOption('q'));
        assertFalse(cl.hasOption('n'));
    }

    public void testCommandLineWithCompostion() throws Exception {
        CommandLine cl = Standalone.getCommandLine(new String[]{"-cpath"}, Standalone.createOptions());
        assertFalse(cl.hasOption('h'));
        assertFalse(cl.hasOption('v'));
        assertEquals("path", cl.getOptionValue('c'));
        assertFalse(cl.hasOption('q'));
        assertFalse(cl.hasOption('n'));
    }

    public void testCommandLineWithCompositionAndQuiet() throws Exception {
        CommandLine cl = Standalone.getCommandLine(new String[]{"-cpath", "-q"}, Standalone.createOptions());
        assertFalse(cl.hasOption('h'));
        assertFalse(cl.hasOption('v'));
        assertEquals("path", cl.getOptionValue('c'));
        assertTrue(cl.hasOption('q'));
        assertFalse(cl.hasOption('n'));
    }

    public void testCommandLineWithCompositionAndQuietAndNowait() throws Exception {
        CommandLine cl = Standalone.getCommandLine(new String[]{"-cpath", "-q", "-n"}, Standalone.createOptions());
        assertFalse(cl.hasOption('h'));
        assertFalse(cl.hasOption('v'));
        assertEquals("path", cl.getOptionValue('c'));
        assertTrue(cl.hasOption('q'));
        assertTrue(cl.hasOption('n'));
    }

}
