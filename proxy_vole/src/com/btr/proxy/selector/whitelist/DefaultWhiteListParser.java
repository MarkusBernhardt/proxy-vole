package com.btr.proxy.selector.whitelist;

import java.util.ArrayList;
import java.util.List;

import com.btr.proxy.selector.whitelist.HostnameFilter.Mode;
import com.btr.proxy.util.UriFilter;

/*****************************************************************************
 * Default implementation for an white list parser. This will support the most
 * common forms of filters found in white lists. 
 * The white list is a comma (or space) separated list of domain names or IP addresses.
 * The following section shows some examples.
 * 
 *  .mynet.com  		- Filters all host names ending with .mynet.com
 *  *.mynet.com  		- Filters all host names ending with .mynet.com
 *  123.12.32.1 		- Filters the IP 123.12.32.1
 *  123.12.32.1/255 	- Filters the IP range   
 * 
 * Example of a list:   .mynet.com, *.my-other-net.org, 123.55.23.222, 123.55.23.0/24
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DefaultWhiteListParser implements WhiteListParser {

	/*************************************************************************
	 * parseWhiteList
	 * @see com.btr.proxy.selector.whitelist.WhiteListParser#parseWhiteList(java.lang.String)
	 ************************************************************************/
	
	public List<UriFilter> parseWhiteList(String whiteList) {
		List<UriFilter> result = new ArrayList<UriFilter>();
		
		String[] token = whiteList.split("[, ]+");
		for (int i = 0; i < token.length; i++) {
			token[i] = token[i].trim();
			if (token[i].contains("/")) {
				result.add(new IpRangeFilter(token[i]));
				continue;
			} else 
			if (token[i].endsWith("*")) {
				token[i] = token[i].substring(0, token[i].length()-1);
				result.add(new HostnameFilter(Mode.BEGINS_WITH, token[i]));
				continue;
			} else 
			if (token[i].trim().startsWith("*")) {
				token[i] = token[i].substring(1);
				result.add(new HostnameFilter(Mode.ENDS_WITH, token[i]));
			} else {
				result.add(new HostnameFilter(Mode.ENDS_WITH, token[i]));
			}
		}
		
		return result;
	}

}
