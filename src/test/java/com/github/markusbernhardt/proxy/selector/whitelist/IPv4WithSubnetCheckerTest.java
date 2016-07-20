package com.github.markusbernhardt.proxy.selector.whitelist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.markusbernhardt.proxy.selector.whitelist.IPWithSubnetChecker;

/*****************************************************************************
 * Some unit tests for the IP subnet mask checker.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IPv4WithSubnetCheckerTest {

	/*************************************************************************
	 * Test method.
	 ************************************************************************/
	@Test
	public void testIsValidIP4() {
		assertTrue("Accept 127.0.0.1/8", IPWithSubnetChecker.isValidIP4Range("127.0.0.1/8"));
		assertTrue("Accept 127.0.0.1/32", IPWithSubnetChecker.isValidIP4Range("127.0.0.1/32"));
		assertTrue("Accept 255.255.255.255/32", IPWithSubnetChecker.isValidIP4Range("255.255.255.255/32"));
		assertTrue("Accept 0.0.0.0/0", IPWithSubnetChecker.isValidIP4Range("0.0.0.0/0"));
		assertFalse("Reject 127.0.0.1", IPWithSubnetChecker.isValidIP4Range("127.0.0.1"));
		assertFalse("Reject localhost", IPWithSubnetChecker.isValidIP4Range("localhost"));
		assertFalse("Reject http://www.sick.de", IPWithSubnetChecker.isValidIP4Range("http://www.sick.de"));
		assertFalse("Reject test.sick.de", IPWithSubnetChecker.isValidIP4Range("test.sick.de"));
		assertFalse("Reject 400.400.400.400", IPWithSubnetChecker.isValidIP4Range("400.400.400.400"));
		assertFalse("Reject 127.0.0.1/33", IPWithSubnetChecker.isValidIP4Range("127.0.0.1/33"));
		assertFalse("Reject 127.0.0.*", IPWithSubnetChecker.isValidIP4Range("127.0.0.*"));
		assertFalse("Reject 127.0.0.*/8", IPWithSubnetChecker.isValidIP4Range("127.0.0.*/8"));
		assertFalse("Reject www.test.com/8", IPWithSubnetChecker.isValidIP4Range("www.test.com/8"));
		assertFalse("Reject 127.0.0.1/33.html", IPWithSubnetChecker.isValidIP4Range("127.0.0.1/33.html"));
	}

	/*************************************************************************
	 * Test method.
	 ************************************************************************/
	@Test
	public void testIsValidIP6() {
		assertTrue("Accept 2001:db8::/32", IPWithSubnetChecker.isValidIP6Range("2001:db8::/32"));
		assertTrue("Accept 0::0/0", IPWithSubnetChecker.isValidIP6Range("0::0/0"));
		assertTrue("Accept 2001:db8::/128", IPWithSubnetChecker.isValidIP6Range("2001:db8::/128"));

		assertFalse("Reject 2001:zb8::/32", IPWithSubnetChecker.isValidIP6Range("2001:zb8::/32"));
		assertFalse("Reject localhost", IPWithSubnetChecker.isValidIP6Range("localhost"));
	}

}
