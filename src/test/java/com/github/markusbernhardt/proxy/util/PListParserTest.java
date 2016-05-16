package com.github.markusbernhardt.proxy.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.util.PListParser.Dict;
import com.github.markusbernhardt.proxy.util.PListParser.XmlParseException;

/*****************************************************************************
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PListParserTest {

    private static final String TEST_SETTINGS = TestUtil.TEST_DATA_FOLDER + File.separator + "osx" + File.separator
            + "osx_all.plist";

    private static Dict pList;

    /*************************************************************************
     * Setup the dictionary from the test data file.
     ************************************************************************/
    @BeforeClass
    public static void setupClass() throws XmlParseException, IOException {
        pList = PListParser.load(new File(TEST_SETTINGS));
    }

    /**
     * Test method for {@link com.btr.proxy.util.PListParser#load(java.io.File)}
     * .
     */
    @Test
    public void testLoadFile() {
        assertTrue(pList.size() > 0);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/

    @Test
    public void testStructure() {
        String currentSet = (String) pList.get("CurrentSet");
        assertNotNull(currentSet);
        Object networkServices = pList.get("NetworkServices");
        assertTrue(networkServices instanceof Dict);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/

    @Test
    public void testNavigate() {
        Object result = pList.getAtPath("NetworkServices/299B07C0-D0E0-4840-8486-9E77B9ED84DB/AppleTalk");
        assertNotNull(result);
        assertTrue(result instanceof Dict);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/

    @Test
    public void testNavigate2() {
        Object result = pList.getAtPath("/NetworkServices/299B07C0-D0E0-4840-8486-9E77B9ED84DB/AppleTalk");
        assertNotNull(result);
        assertTrue(result instanceof Dict);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/

    @Test
    public void testNavigate3() {
        Object result = pList.getAtPath("/NetworkServices/299B07C0-D0E0-4840-8486-9E77B9ED84DB/AppleTalk/");
        assertNotNull(result);
        assertTrue(result instanceof Dict);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/

    @Test
    public void testNavigate4() {
        Object result = pList.getAtPath("/NetworkServices/299B07C0-D0E0-4840-8486-9E77B9ED84DB/AppleTalkXXX/");
        assertNull(result);
    }

}
