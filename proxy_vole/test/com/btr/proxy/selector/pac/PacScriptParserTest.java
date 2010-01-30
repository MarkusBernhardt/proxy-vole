package com.btr.proxy.selector.pac;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.btr.proxy.TestUtil;
import com.btr.proxy.util.ProxyException;

/*****************************************************************************
 * Tests for the pac script parser. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PacScriptParserTest {
	
	/*************************************************************************
	 * Set calendar for date and time base tests.
	 * Current date for all tests  is: 15. December 1994 12:00.00 
	 * its a Thursday 
	 ************************************************************************/
	@BeforeClass
	public static void setup() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1994);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 00);
		cal.set(Calendar.MILLISECOND, 00);
		
		PacScriptParser.setCurrentTime(cal);
	}

	/*************************************************************************
	 * Cleanup after the tests.
	 ************************************************************************/
	@AfterClass
	public static void teadDown() {
		PacScriptParser.setCurrentTime(null);
	}
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDnsDomainIs() {
		assertEquals(true, PacScriptParser.dnsDomainIs("host1.unit-test.invalid", "unit-test.invalid"));
	}
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDnsDomainLevels() {
		assertEquals(2, PacScriptParser.dnsDomainLevels(TestUtil.HTTP_TEST_URI.toString()));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws UnknownHostException on resolve error.
	 ************************************************************************/
	@Test
	public void testDnsResolve() throws UnknownHostException {
		InetAddress adr = Inet4Address.getLocalHost();
		assertEquals(adr.getHostAddress(), PacScriptParser.dnsResolve(adr.getHostName()));
	}
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testIsInNet() {
		assertEquals(true, PacScriptParser.isInNet("192.168.0.122", "192.168.0.0", "255.255.255.0"));
	}
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testIsPlainHostName() {
		assertEquals(false, PacScriptParser.isPlainHostName("host1.unit-test.invalid"));
		assertEquals(true, PacScriptParser.isPlainHostName("host1"));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws UnknownHostException on resolve error.
	 ************************************************************************/
	@Test
	public void testIsResolveable() throws UnknownHostException {
		InetAddress adr = Inet4Address.getLocalHost();
		assertEquals(true, PacScriptParser.isResolvable(adr.getHostName()));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testLocalHostOrDomainIs() {
		assertEquals(true, PacScriptParser.localHostOrDomainIs("host1.unit-test.invalid", "host1.unit-test.invalid"));
	}

	/*************************************************************************
	 * Test method
	 * @throws UnknownHostException on resolve error.
	 ************************************************************************/
	@Test
	public void testMyIpAddress() throws UnknownHostException {
		InetAddress adr = Inet4Address.getLocalHost();
		assertEquals(adr.getHostAddress(), PacScriptParser.myIpAddress());
	}
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testShExpMatch() {
		assertEquals(true, PacScriptParser.shExpMatch("host1.unit-test.invalid", "host1.unit-test.*"));
		assertEquals(true, PacScriptParser.shExpMatch("host1.unit-test.invalid", "*.unit-test.invalid"));
	}	
	
	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testWeekdayRange() {
		assertEquals(true, PacScriptParser.weekdayRange("MON", "SUN", "GMT"));
		assertEquals(true, PacScriptParser.weekdayRange("SUN", "SAT", null));
		assertEquals(false, PacScriptParser.weekdayRange("MON", "WED", null));
	}	

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDateRange() {
		assertEquals(true, PacScriptParser.dateRange(15, "undefined", "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange(15, "DEC", "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange(15, "DEC", 1994, "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange(15, 17, "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange("OCT", "JAN", "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange(1994, 1994, "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.dateRange(1, "DEC", 1994, 1, "JAN", 1995, "GTM"));
		
		assertEquals(false, PacScriptParser.dateRange(16, "DEC", 1994, 1, "JAN", 1995, "GTM"));
	}	

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testTimeRange() {
		assertEquals(true, PacScriptParser.timeRange(12, "undefined", "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.timeRange(11, 13, "undefined", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.timeRange(11, 13, "gmt", "undefined", "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.timeRange(11, 30, 13, 30, "undefined", "undefined", "undefined"));
		assertEquals(true, PacScriptParser.timeRange(11, 30, 15, 13, 30, 15, "undefined"));
		assertEquals(true, PacScriptParser.timeRange(11, 30, 15, 13, 30, 15, "GMT"));

		assertEquals(false, PacScriptParser.timeRange(12, 50, 00, 9, 30, 00, "GMT"));
	}	
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testScriptExecution() throws ProxyException, MalformedURLException {
		PacScriptParser p = new PacScriptParser(new UrlPacScriptSource(toUrl("test1.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testCommentsInScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new PacScriptParser(new UrlPacScriptSource(toUrl("test2.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testScriptWeekDayScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new PacScriptParser(new UrlPacScriptSource(toUrl("testWeekDay.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testDateRangeScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new PacScriptParser(new UrlPacScriptSource(toUrl("testDateRange.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testTimeRangeScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new PacScriptParser(new UrlPacScriptSource(toUrl("testTimeRange.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}
	
	/*************************************************************************
	 * Helper method to build the url to the given test file
	 * @param testFile the name of the test file.
	 * @return the URL. 
	 * @throws MalformedURLException
	 ************************************************************************/
	
	private String toUrl(String testFile) throws MalformedURLException {
		return new File(TestUtil.TEST_DATA_FOLDER+"pac", testFile).toURI().toURL().toString();
	}
		
}

