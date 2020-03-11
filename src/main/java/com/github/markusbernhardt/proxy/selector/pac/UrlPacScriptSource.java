package com.github.markusbernhardt.proxy.selector.pac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;

import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Script source that will load the content of a PAC file from an webserver. The
 * script content is cached once it was downloaded.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class UrlPacScriptSource implements PacScriptSource {

	private static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // seconds
	private static final int DEFAULT_READ_TIMEOUT = 20 * 1000; // seconds
	public static final String OVERRIDE_CONNECT_TIMEOUT = "com.btr.proxy.url.connectTimeout";
	public static final String OVERRIDE_READ_TIMEOUT = "com.btr.proxy.url.readTimeout";

	private final String scriptUrl;
	private String scriptContent;
	private long expireAtMillis;

	/*************************************************************************
	 * Constructor
	 * 
	 * @param url
	 *            the URL to download the script from.
	 ************************************************************************/

	public UrlPacScriptSource(String url) {
		super();
		this.expireAtMillis = 0;
		this.scriptUrl = url;
	}

	/*************************************************************************
	 * getScriptContent
	 * 
	 * @see com.github.markusbernhardt.proxy.selector.pac.PacScriptSource#getScriptContent()
	 ************************************************************************/

	public synchronized String getScriptContent() throws IOException {
		if (this.scriptContent == null
		        || (this.expireAtMillis > 0 && this.expireAtMillis < System.currentTimeMillis())) {
			try {
				// Reset it again with next download we should get a new expire
				// info
				this.expireAtMillis = 0;

				if (this.scriptUrl.startsWith("file:/") || this.scriptUrl.indexOf(":/") == -1) {
					this.scriptContent = readPacFileContent(this.scriptUrl);
				} else {
					this.scriptContent = downloadPacContent(this.scriptUrl);
				}
			} catch (IOException e) {
				Logger.log(getClass(), LogLevel.ERROR, "Loading script failed from: {} with error {}", this.scriptUrl,
				        e);
				this.scriptContent = "";
				throw e;
			}
		}
		return this.scriptContent;
	}

	/*************************************************************************
	 * Reads a PAC script from a local file.
	 * 
	 * @param scriptUrl
	 * @return the content of the script file.
	 * @throws IOException
	 * @throws URISyntaxException
	 ************************************************************************/

	private String readPacFileContent(String scriptUrl) throws IOException {
		try {
			File file = null;
			if (scriptUrl.indexOf(":/") == -1) {
				file = new File(scriptUrl);
			} else {
				file = new File(new URL(scriptUrl).toURI());
			}
			BufferedReader r = new BufferedReader(new FileReader(file));
			StringBuilder result = new StringBuilder();
			try {
				String line;
				while ((line = r.readLine()) != null) {
					result.append(line).append("\n");
				}
			} finally {
				r.close();
			}
			return result.toString();
		} catch (Exception e) {
			System.out.println(System.getProperty("user.dir"));
			Logger.log(getClass(), LogLevel.ERROR, "File reading error.", e);
			throw new IOException(e.getMessage());
		}
	}

	/*************************************************************************
	 * Downloads the script from a webserver.
	 * 
	 * @param url
	 *            the URL to the script file.
	 * @return the script content.
	 * @throws IOException
	 *             on read error.
	 ************************************************************************/

	private String downloadPacContent(String url) throws IOException {
		if (url == null) {
			throw new IOException("Invalid PAC script URL: null");
		}

		setPacProxySelectorEnabled(false);

		HttpURLConnection con = null;
		try {
			con = setupHTTPConnection(url);
			if (con.getResponseCode() != 200) {
				throw new IOException("Server returned: " + con.getResponseCode() + " " + con.getResponseMessage());
			}
			// Read expire date.
			this.expireAtMillis = con.getExpiration();

			BufferedReader r = getReader(con);
			String result = readAllContent(r);
			r.close();
			return result;
		} finally {
			setPacProxySelectorEnabled(true);
			if (con != null) {
				con.disconnect();
			}
		}
	}

	/*************************************************************************
	 * Enables/disables the PAC proxy selector while we download to prevent
	 * recursion. See issue: 26 in the change tracker.
	 ************************************************************************/

	private void setPacProxySelectorEnabled(boolean enable) {
		PacProxySelector.setEnabled(enable);
	}

	/*************************************************************************
	 * Reads the whole content available into a String.
	 * 
	 * @param r
	 *            to read from.
	 * @return the complete PAC file content.
	 * @throws IOException
	 ************************************************************************/

	private String readAllContent(BufferedReader r) throws IOException {
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			result.append(line).append("\n");
		}
		return result.toString();
	}

	/*************************************************************************
	 * Build a BufferedReader around the open HTTP connection.
	 * 
	 * @param con
	 *            to read from
	 * @return the BufferedReader.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 ************************************************************************/

	private BufferedReader getReader(HttpURLConnection con) throws UnsupportedEncodingException, IOException {
		String charsetName = parseCharsetFromHeader(con.getContentType());
		BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), charsetName));
		return r;
	}

	/*************************************************************************
	 * Configure the connection to download from.
	 * 
	 * @param url
	 *            to get the pac file content from
	 * @return a HTTPUrlConnecion to this url.
	 * @throws IOException
	 * @throws MalformedURLException
	 ************************************************************************/

	private HttpURLConnection setupHTTPConnection(String url) throws IOException, MalformedURLException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
		con.setConnectTimeout(getTimeOut(OVERRIDE_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT));
		con.setReadTimeout(getTimeOut(OVERRIDE_READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
		con.setInstanceFollowRedirects(true);
		con.setRequestProperty("accept", "application/x-ns-proxy-autoconfig, */*;q=0.8");
		return con;
	}

	/*************************************************************************
	 * Gets the timeout value from a property or uses the given default value if
	 * the property cannot be parsed.
	 * 
	 * @param overrideProperty
	 *            the property to define the timeout value in milliseconds
	 * @param defaultValue
	 *            the default timeout value in milliseconds.
	 * @return the value to use.
	 ************************************************************************/

	protected int getTimeOut(String overrideProperty, int defaultValue) {
		int timeout = defaultValue;
		String prop = System.getProperty(overrideProperty);
		if (prop != null && prop.trim().length() > 0) {
			try {
				timeout = Integer.parseInt(prop.trim());
			} catch (NumberFormatException e) {
				Logger.log(getClass(), LogLevel.DEBUG, "Invalid override property : {}={}", overrideProperty, prop);
				// In this case use the default value.
			}
		}
		return timeout;
	}

	/*************************************************************************
	 * Response Content-Type could be something like this:
	 * application/x-ns-proxy-autoconfig; charset=UTF-8
	 * 
	 * @param contentType
	 *            header field.
	 * @return the extracted charset if set else a default charset.
	 ************************************************************************/

	String parseCharsetFromHeader(String contentType) {
		String result = "ISO-8859-1";
		if (contentType != null) {
			String[] paramList = contentType.split(";");
			for (String param : paramList) {
				if (param.toLowerCase().trim().startsWith("charset") && param.indexOf("=") != -1) {
					result = param.substring(param.indexOf("=") + 1).trim();
				}
			}
		}
		return result;
	}

	/***************************************************************************
	 * @see java.lang.Object#toString()
	 **************************************************************************/
	@Override
	public String toString() {
		return this.scriptUrl;
	}

	/*************************************************************************
	 * isScriptValid
	 * 
	 * @see com.github.markusbernhardt.proxy.selector.pac.PacScriptSource#isScriptValid()
	 ************************************************************************/

	public boolean isScriptValid() {
		try {
			String script = getScriptContent();
			if (script == null || script.trim().length() == 0) {
				Logger.log(getClass(), LogLevel.DEBUG, "PAC script is empty. Skipping script!");
				return false;
			}
			if (script.indexOf("FindProxyForURL") == -1) {
				Logger.log(getClass(), LogLevel.DEBUG,
				        "PAC script entry point FindProxyForURL not found. Skipping script!");
				return false;
			}
			return true;
		} catch (IOException e) {
			Logger.log(getClass(), LogLevel.DEBUG, "File reading error: {}", e);
			return false;
		}
	}

}
