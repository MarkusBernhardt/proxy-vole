package com.github.markusbernhardt.proxy.selector.whitelist;

import java.util.regex.Pattern;

/*****************************************************************************
 * Checks if the given string is a IP4 range subnet definition of the format
 * 192.168.0/24 Based on a contribution by Jan Engler
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IPWithSubnetChecker {

	private static Pattern IP_SUB_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
	        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
	        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])/(\\d|([12]\\d|3[0-2]))$");

	// Could be improved
	private static Pattern IP6_SUB_PATTERN = Pattern.compile("^[a-f0-9:]*/[0-9]+$");

	/*************************************************************************
	 * Tests if a given string is of in the correct format for an IP4 subnet
	 * mask.
	 * 
	 * @param possibleIPAddress
	 *            to test for valid format.
	 * @return true if valid else false.
	 ************************************************************************/

	public static boolean isValidIP4Range(String possibleIPAddress) {
		return IP_SUB_PATTERN.matcher(possibleIPAddress).matches();
	}

	/*************************************************************************
	 * Tests if a given string is of in the correct format for an IP6 subnet
	 * mask.
	 * 
	 * @param possibleIPAddress
	 *            to test for valid format.
	 * @return true if valid else false.
	 ************************************************************************/

	public static boolean isValidIP6Range(String possibleIPAddress) {
		return IP6_SUB_PATTERN.matcher(possibleIPAddress).matches();
	}
}
