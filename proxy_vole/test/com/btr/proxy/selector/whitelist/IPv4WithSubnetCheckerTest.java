package com.btr.proxy.selector.whitelist;

import static org.junit.Assert.*;

import org.junit.Test;

/*****************************************************************************
 * Some unit tests for the IP subnet mask checker. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class IPv4WithSubnetCheckerTest {

	/*************************************************************************
	 * Test method.
	 ************************************************************************/
	@Test
	public void testIsValid() {
		assertTrue("Accept 127.0.0.1/8", IPv4WithSubnetChecker.isValid("127.0.0.1/8"));
		assertTrue("Accept 127.0.0.1/32", IPv4WithSubnetChecker.isValid("127.0.0.1/32"));
		assertTrue("Accept 255.255.255.255/32", IPv4WithSubnetChecker.isValid("255.255.255.255/32"));
		assertTrue("Accept 0.0.0.0/0", IPv4WithSubnetChecker.isValid("0.0.0.0/0"));
		assertFalse("Reject 127.0.0.1", IPv4WithSubnetChecker.isValid("127.0.0.1"));
		assertFalse("Reject localhost", IPv4WithSubnetChecker.isValid("localhost"));
		assertFalse("Reject http://www.sick.de", IPv4WithSubnetChecker.isValid("http://www.sick.de"));
		assertFalse("Reject test.sick.de", IPv4WithSubnetChecker.isValid("test.sick.de"));
		assertFalse("Reject 400.400.400.400", IPv4WithSubnetChecker.isValid("400.400.400.400"));
		assertFalse("Reject 127.0.0.1/33", IPv4WithSubnetChecker.isValid("127.0.0.1/33"));
		assertFalse("Reject 127.0.0.*", IPv4WithSubnetChecker.isValid("127.0.0.*"));
		assertFalse("Reject 127.0.0.*/8", IPv4WithSubnetChecker.isValid("127.0.0.*/8"));
		assertFalse("Reject www.test.com/8", IPv4WithSubnetChecker.isValid("www.test.com/8"));
		assertFalse("Reject 127.0.0.1/33.html", IPv4WithSubnetChecker.isValid("127.0.0.1/33.html"));
	}

}
