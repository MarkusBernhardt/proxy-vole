package com.github.markusbernhardt.proxy.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*****************************************************************************
 * This resolver is used to prevent network lookups of DTD or XML schemas.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class EmptyXMLResolver implements EntityResolver {

	/*************************************************************************
	 * Overwritten to return an empty entity.
	 * 
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
	 *      java.lang.String)
	 ************************************************************************/

	public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
		return new InputSource(new ByteArrayInputStream("".getBytes()));
	}

}
