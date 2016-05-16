package com.github.markusbernhardt.proxy.selector.pac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.pac.PacScriptMethods;

/*****************************************************************************
 * Tests for the global PAC script methods that are used as context inside of
 * the scripts.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PacScriptMethodsTest {

    /*************************************************************************
     * Get a methods implementation with a calendar for date and time base tests
     * set to a hardcoded data. Current date for all tests is: 15. December 1994
     * 12:00.00 its a Thursday
     ************************************************************************/

    private PacScriptMethods buildParser() {
        PacScriptMethods result = new PacScriptMethods();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1994);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 00);
        result.setCurrentTime(cal);

        return result;
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testDnsDomainIs() {
        assertEquals(true, buildParser().dnsDomainIs("host1.unit-test.invalid", "unit-test.invalid"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testDnsDomainLevels() {
        assertEquals(2, buildParser().dnsDomainLevels(TestUtil.HTTP_TEST_URI.toString()));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws UnknownHostException
     *             on resolve error.
     ************************************************************************/
    @Test
    public void testDnsResolve() throws UnknownHostException {
        InetAddress adr = Inet4Address.getLocalHost();
        assertEquals(adr.getHostAddress(), buildParser().dnsResolve(adr.getHostName()));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testIsInNet() {
        assertEquals(true, buildParser().isInNet("192.168.0.122", "192.168.0.0", "255.255.255.0"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testIsInNet2() {
        assertEquals(true, buildParser().isInNet("10.13.75.47", "10.13.72.0", "255.255.252.0"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testIsInNetExForIP4() {
        // isInNetEx(host, "198.95.249.79/32");
        // true if the IP address of host matches exactly 198.95.249.79
        assertEquals(true, buildParser().isInNetEx("198.95.249.79", "198.95.249.79/32"));

        assertEquals(false, buildParser().isInNetEx("198.95.249.80", "198.95.249.79/32"));

        // isInNetEx(host, "198.95.0.0/16");
        // true if the IP address of the host matches 198.95.*.*
        assertEquals(true, buildParser().isInNetEx("198.95.249.79", "198.95.0.0/16"));
        assertEquals(true, buildParser().isInNetEx("198.95.249.80", "198.95.0.0/16"));

        assertEquals(false, buildParser().isInNetEx("198.96.249.80", "198.95.0.0/16"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testIsInNetExForIP6() {
        // isInNetEx(host, "3ffe:8311:ffff/48");
        // true if the IP address of the host matches 3ffe:8311:fff:*:*:*:*:*
        assertEquals(true, buildParser().isInNetEx("3ffe:8311:ffff::", "3ffe:8311:ffff::/48"));
        assertEquals(true, buildParser().isInNetEx("3ffe:8311:ffff:1111:0:0:0:0", "3ffe:8311:ffff::/48"));

        assertEquals(false, buildParser().isInNetEx("3ffe:8312:ffff:1111:0:0:0:0", "3ffe:8311:ffff::/48"));
    }

    /*************************************************************************
     * Test Method sortIpAddressList(2001:4898:28:3:201:2ff:feea:fc14;
     * 157.59.139.22; fe80::5efe:157.59.139.22"); returns
     * "fe80::5efe:157.59.139.22;2001:4898:28:3:201:2ff:feea:fc14;157.59.139.22"
     * A list of sorted IP addresses. If there both IPv6 and IPv4 IP addresses
     * are passed as input to this function, then the sorted IPv6 addresses are
     * followed by sorted IPv4 addresses
     * 
     ************************************************************************/
    @Test
    public void testSortIpAddressList() {
        assertEquals("fe80::5efe:157.59.139.22;2001:4898:28:3:201:2ff:feea:fc14;157.59.139.22", buildParser()
                .sortIpAddressList("2001:4898:28:3:201:2ff:feea:fc14; 157.59.139.22; fe80::5efe:157.59.139.22"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testIsPlainHostName() {
        assertEquals(false, buildParser().isPlainHostName("host1.unit-test.invalid"));
        assertEquals(true, buildParser().isPlainHostName("host1"));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws UnknownHostException
     *             on resolve error.
     ************************************************************************/
    @Test
    public void testIsResolveable() throws UnknownHostException {
        InetAddress adr = Inet4Address.getLocalHost();
        assertEquals(true, buildParser().isResolvable(adr.getHostName()));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testLocalHostOrDomainIs() {
        assertEquals(true, buildParser().localHostOrDomainIs("host1.unit-test.invalid", "host1.unit-test.invalid"));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws UnknownHostException
     *             on resolve error.
     ************************************************************************/
    @Test
    public void testMyIpAddress() throws UnknownHostException {
        String myIP = buildParser().myIpAddress();
        assertFalse("127.0.0.1".equals(myIP));
        assertFalse("".equals(myIP));
        assertNotNull(myIP);
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testShExpMatch() {
        assertEquals(true, buildParser().shExpMatch("host1.unit-test.invalid", "host1.unit-test.*"));
        assertEquals(true, buildParser().shExpMatch("host1.unit-test.invalid", "*.unit-test.invalid"));
        assertEquals(true, buildParser().shExpMatch("host1.unit-test.invalid", "*.unit*.invalid"));

        assertEquals(false, buildParser().shExpMatch("202.310.65.6", "10.*"));
        assertEquals(false, buildParser().shExpMatch("202.310.65.6", "*.65"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testWeekdayRange() {
        assertEquals(true, buildParser().weekdayRange("MON", "SUN", "GMT"));
        assertEquals(true, buildParser().weekdayRange("SUN", "SAT", null));
        assertEquals(false, buildParser().weekdayRange("MON", "WED", null));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testDateRange() {
        assertEquals(true, buildParser().dateRange(15, "undefined", "undefined", "undefined", "undefined", "undefined",
                "undefined"));
        assertEquals(true,
                buildParser().dateRange(15, "DEC", "undefined", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true,
                buildParser().dateRange(15, "DEC", 1994, "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true,
                buildParser().dateRange(15, 17, "undefined", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true,
                buildParser().dateRange("OCT", "JAN", "undefined", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true,
                buildParser().dateRange(1994, 1994, "undefined", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true, buildParser().dateRange(1, "DEC", 1994, 1, "JAN", 1995, "GTM"));

        assertEquals(false, buildParser().dateRange(16, "DEC", 1994, 1, "JAN", 1995, "GTM"));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testTimeRange() {
        assertEquals(true, buildParser().timeRange(12, "undefined", "undefined", "undefined", "undefined", "undefined",
                "undefined"));
        assertEquals(true,
                buildParser().timeRange(11, 13, "undefined", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true, buildParser().timeRange(11, 13, "gmt", "undefined", "undefined", "undefined", "undefined"));
        assertEquals(true, buildParser().timeRange(11, 30, 13, 30, "undefined", "undefined", "undefined"));
        assertEquals(true, buildParser().timeRange(11, 30, 15, 13, 30, 15, "undefined"));
        assertEquals(true, buildParser().timeRange(11, 30, 15, 13, 30, 15, "GMT"));

        assertEquals(false, buildParser().timeRange(12, 50, 00, 9, 30, 00, "GMT"));
    }

}
