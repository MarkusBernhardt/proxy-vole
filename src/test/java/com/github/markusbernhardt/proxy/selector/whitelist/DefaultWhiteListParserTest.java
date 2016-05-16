package com.github.markusbernhardt.proxy.selector.whitelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.github.markusbernhardt.proxy.selector.whitelist.DefaultWhiteListParser;
import com.github.markusbernhardt.proxy.util.UriFilter;

/*****************************************************************************
 * Unit tests for DefaultWhiteListParser
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DefaultWhiteListParserTest {

    private DefaultWhiteListParser parser = new DefaultWhiteListParser();

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldAllowAllPrefix() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("*.mynet.com");
        UriFilter filter = l.get(0);
        assertTrue(filter.accept(new URI("http://rossi.mynet.com")));
        assertFalse(filter.accept(new URI("http://rossi.mynet.com.test")));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldAllowAllPostfix() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("mynet.*");
        UriFilter filter = l.get(0);
        assertFalse(filter.accept(new URI("http://rossi.mynet.com")));
        assertTrue(filter.accept(new URI("http://mynet.junit.test")));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldSplitMultipleEntries() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("*.mynet.com; *.rossi.invalid; junit*");
        assertEquals(3, l.size());
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldAllowIpRange() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("192.168.0.0/24");
        UriFilter filter = l.get(0);
        assertTrue(filter.accept(new URI("http://192.168.0.1")));
        assertTrue(filter.accept(new URI("http://192.168.0.11")));
        assertFalse(filter.accept(new URI("http://rossi.mynet.com")));
        assertFalse(filter.accept(new URI("http://145.5.5.1")));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldHandleInvalidWithoutException() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("http://10.*.*.*");
        UriFilter filter = l.get(0);
        assertFalse(filter.accept(new URI("http://10.0.0.1")));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on error
     ************************************************************************/
    @Test
    public void shouldHandleLocalBypass() throws URISyntaxException {
        List<UriFilter> l = this.parser.parseWhiteList("<local>");
        UriFilter filter = l.get(0);
        assertTrue(filter.accept(new URI("http://localhost")));
    }

    //
    //
    // .mynet.com - Filters all host names ending with .mynet.com
    // * *.mynet.com - Filters all host names ending with .mynet.com
    // * www.mynet.* - Filters all host names starting with www.mynet.
    // * 123.12.32.1 - Filters the IP 123.12.32.1
    // * 123.12.32.1/255 - Filters the IP range
    // * http://www.mynet.com
    //

}
