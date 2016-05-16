package com.github.markusbernhardt.proxy.selector.pac;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Test program submitted to test the issue 27 with PAC proxy selector that is
 * while downloading the PAC file invoking itself. This has lead to a endless
 * loop. The issue is now solved but I keep this test program for future PAC
 * testing.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PacProxyDebugging {

    private static final String TEST_URL = "http://www.asetune.com";

    /*************************************************************************
     * Setup a console logger.
     ************************************************************************/

    private void installLogger() {
        Logger.setBackend(new Logger.LogBackEnd() {
            public void log(Class<?> clazz, LogLevel loglevel, String msg, Object... params) {
                System.out.println(loglevel + "\t" + MessageFormat.format(msg, params));
            }

            public boolean isLogginEnabled(LogLevel logLevel) {
                return true;
            }
        });
    }

    /*************************************************************************
     * Main entry point for the test application.
     * 
     * @param args
     *            the command line arguments.
     ************************************************************************/

    public static void main(String[] args) {
        // System.setProperty("http.proxyHost", "10.65.12.21");
        // System.setProperty("http.proxyPort", "8080");
        // System.setProperty("java.net.useSystemProxies", "true");

        PacProxyDebugging pt = new PacProxyDebugging();
        pt.installLogger();

        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

        // ProxySearch proxySearch = new ProxySearch();
        // proxySearch.addStrategy(Strategy.JAVA);
        // proxySearch.addStrategy(Strategy.BROWSER);
        // proxySearch.addStrategy(Strategy.OS_DEFAULT);
        // proxySearch.addStrategy(Strategy.ENV_VAR);

        ProxySelector myProxySelector = proxySearch.getProxySelector();

        ProxySelector.setDefault(myProxySelector);
        System.out.println("Using proxy selector: " + myProxySelector);

        // String webAddress = "http://www.google.com";
        String webAddress = TEST_URL;
        try {
            URL url = new URL(webAddress);
            // List<Proxy> result = myProxySelector.select(url.toURI());
            // if (result == null || result.size() == 0)
            // {
            // System.out.println("No proxy found for this url.");
            // return;
            // }
            // System.out.println("Proxy Settings found using 'xxx' strategy.\n"
            // +
            // "Proxy used for URL is: "+result.get(0));

            System.out.println("Now open a connection to the url: " + webAddress);
            System.out.println("==============================================");

            // open the connection and prepare it to POST
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(10 * 1000);

            // Return the response
            InputStream in = conn.getInputStream();
            LineNumberReader lr = new LineNumberReader(new InputStreamReader(in));
            String line;
            while ((line = lr.readLine()) != null) {
                System.out.println("response line " + lr.getLineNumber() + ": " + line);
            }
            System.out.println("---- END -------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
