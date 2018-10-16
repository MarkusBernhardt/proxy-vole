package com.github.markusbernhardt.proxy.selector.misc;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/*****************************************************************************
 * Implements a fallback selector to warp it around an existing ProxySelector.
 * This will remove proxies from a list of proxies and implement an automatic
 * retry mechanism.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class ProxyListFallbackSelector extends ProxySelector {

	// Retry a unresponsive proxy after 10 minutes per default.
	private static final int DEFAULT_RETRY_DELAY = 1000 * 60 * 10;

	private ProxySelector delegate;
	private ConcurrentHashMap<SocketAddress, Long> failedDelayCache;
	private long retryAfterMs;

	/*************************************************************************
	 * Constructor
	 * 
	 * @param delegate
	 *            the delegate to use.
	 ************************************************************************/

	public ProxyListFallbackSelector(ProxySelector delegate) {
		this(DEFAULT_RETRY_DELAY, delegate);
	}

	/*************************************************************************
	 * Constructor
	 * 
	 * @param retryAfterMs
	 *            the "retry delay" as amount of milliseconds.
	 * @param delegate
	 *            the delegate to use.
	 ************************************************************************/

	public ProxyListFallbackSelector(long retryAfterMs, ProxySelector delegate) {
		super();
		this.failedDelayCache = new ConcurrentHashMap<SocketAddress, Long>();
		this.delegate = delegate;
		this.retryAfterMs = retryAfterMs;
	}

	/*************************************************************************
	 * connectFailed
	 * 
	 * @see java.net.ProxySelector#connectFailed(java.net.URI,
	 *      java.net.SocketAddress, java.io.IOException)
	 ************************************************************************/

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		this.failedDelayCache.put(sa, System.currentTimeMillis());
	}

	/*************************************************************************
	 * select
	 * 
	 * @see java.net.ProxySelector#select(java.net.URI)
	 ************************************************************************/

	@Override
	public List<Proxy> select(URI uri) {
		cleanupCache();
		List<Proxy> proxyList = this.delegate.select(uri);
		List<Proxy> result = filterUnresponsiveProxiesFromList(proxyList);
		return result;
	}

	/*************************************************************************
	 * Cleanup the entries from the cache that are no longer unresponsive.
	 ************************************************************************/

	private void cleanupCache() {
		Iterator<Entry<SocketAddress, Long>> it = this.failedDelayCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<SocketAddress, Long> e = it.next();
			Long lastFailTime = e.getValue();
			if (retryDelayHasPassedBy(lastFailTime)) {
				it.remove();
			}
		}
	}

	/*************************************************************************
	 * Filters out proxies that are not reponding.
	 * 
	 * @param proxyList
	 *            a list of proxies to test.
	 * @return the filtered list.
	 ************************************************************************/

	private List<Proxy> filterUnresponsiveProxiesFromList(List<Proxy> proxyList) {
		if (this.failedDelayCache.isEmpty()) {
			return proxyList;
		}
		List<Proxy> result = new ArrayList<Proxy>(proxyList.size());
		for (Proxy proxy : proxyList) {
			if (isDirect(proxy) || isNotUnresponsive(proxy)) {
				result.add(proxy);
			}
		}
		if(result.isEmpty()){
		    result.add(Proxy.NO_PROXY);
		}
		return result;
	}

	/*************************************************************************
	 * Checks if the given proxy is representing a direct connection.
	 * 
	 * @param proxy
	 *            to inspect.
	 * @return true if it is direct else false.
	 ************************************************************************/

	private boolean isDirect(Proxy proxy) {
		return Proxy.NO_PROXY.equals(proxy);
	}

	/*************************************************************************
	 * Tests that a given proxy is not "unresponsive".
	 * 
	 * @param proxy
	 *            to test.
	 * @return true if not unresponsive.
	 ************************************************************************/

	private boolean isNotUnresponsive(Proxy proxy) {
		Long lastFailTime = this.failedDelayCache.get(proxy.address());
		return retryDelayHasPassedBy(lastFailTime);
	}

	/*************************************************************************
	 * Checks if the retry delay has passed.
	 * 
	 * @param lastFailTime
	 * @return true if the delay has passed.
	 ************************************************************************/

	private boolean retryDelayHasPassedBy(Long lastFailTime) {
		return lastFailTime == null || lastFailTime + this.retryAfterMs < System.currentTimeMillis();
	}

	/*************************************************************************
	 * Only used for unit testing not part of the public API.
	 * 
	 * @param retryAfterMs
	 *            The retryAfterMs to set.
	 ************************************************************************/

	final void setRetryAfterMs(long retryAfterMs) {
		this.retryAfterMs = retryAfterMs;
	}

	@Override
	public String toString() {
		return "ProxyListFallbackSelector{" +
				"delegate=" + delegate +
				", failedDelayCache=" + failedDelayCache +
				", retryAfterMs=" + retryAfterMs +
				'}';
	}
}
