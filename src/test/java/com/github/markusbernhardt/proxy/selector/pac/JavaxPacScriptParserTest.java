package com.github.markusbernhardt.proxy.selector.pac;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.pac.JavaxPacScriptParser;
import com.github.markusbernhardt.proxy.selector.pac.PacScriptParser;
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Tests for the javax.script PAC script parser.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class JavaxPacScriptParserTest {

	/*************************************************************************
	 * Set calendar for date and time base tests. Current date for all tests is:
	 * 15. December 1994 12:00.00 its a Thursday
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

		// TODO Rossi 26.08.2010 need to fake time
		// JavaxPacScriptParser.setCurrentTime(cal);
	}

	/*************************************************************************
	 * Cleanup after the tests.
	 ************************************************************************/
	@AfterClass
	public static void teadDown() {
		// JavaxPacScriptParser.setCurrentTime(null);
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptExecution() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("test1.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testCommentsInScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("test2.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	@Ignore // Test deactivated because it will not run in Java 1.5 and time
	        // based test are unstable
	public void testScriptWeekDayScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("testWeekDay.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	@Ignore // Test deactivated because it will not run in Java 1.5 and time
	        // based test are unstable
	public void testDateRangeScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("testDateRange.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	@Ignore // Test deactivated because it will not run in Java 1.5 and time
	        // based test are unstable
	public void testTimeRangeScript() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("testTimeRange.pac")));
		p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void methodsShouldReturnJsStrings() throws ProxyException, MalformedURLException {
		PacScriptParser p = new JavaxPacScriptParser(new UrlPacScriptSource(toUrl("testReturnTypes.pac")));
		String actual = p.evaluate(TestUtil.HTTP_TEST_URI.toString(), "host1.unit-test.invalid");
		Assert.assertEquals("number boolean string", actual);
	}

	/*************************************************************************
	 * Helper method to build the url to the given test file
	 * 
	 * @param testFile
	 *            the name of the test file.
	 * @return the URL.
	 * @throws MalformedURLException
	 ************************************************************************/

	private String toUrl(String testFile) throws MalformedURLException {
		return new File(TestUtil.TEST_DATA_FOLDER + "pac", testFile).toURI().toURL().toString();
	}

}
