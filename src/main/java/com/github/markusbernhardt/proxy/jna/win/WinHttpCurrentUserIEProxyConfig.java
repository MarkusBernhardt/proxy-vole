package com.github.markusbernhardt.proxy.jna.win;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WTypes;

/**
 * The WINHTTP_CURRENT_USER_IE_PROXY_CONFIG structure contains the Internet
 * Explorer proxy configuration information.
 * 
 * @author Markus Bernhardt, Copyright 2016
 */
public class WinHttpCurrentUserIEProxyConfig extends Structure {

	/**
	 * If TRUE, indicates that the Internet Explorer proxy configuration for the
	 * current user specifies "automatically detect settings".
	 */
	public boolean fAutoDetect;

	/**
	 * Pointer to a null-terminated Unicode string that contains the
	 * auto-configuration URL if the Internet Explorer proxy configuration for
	 * the current user specifies "Use automatic proxy configuration".
	 */
	public WTypes.LPWSTR lpszAutoConfigUrl;

	/**
	 * Pointer to a null-terminated Unicode string that contains the proxy URL
	 * if the Internet Explorer proxy configuration for the current user
	 * specifies "use a proxy server".
	 */
	public WTypes.LPWSTR lpszProxy;

	/**
	 * Pointer to a null-terminated Unicode string that contains the optional
	 * proxy by-pass server list.
	 */
	public WTypes.LPWSTR lpszProxyBypass;

	/**
	 * Create WinHttpCurrentUserIeProxyConfig structure.
	 */
	public WinHttpCurrentUserIEProxyConfig() {
		super();
	}

	/**
	 * Create WinHttpCurrentUserIeProxyConfig structure cast onto pre-allocated
	 * memory.
	 * 
	 * @param pointer
	 *            pointer to pre-allocated memory
	 */
	public WinHttpCurrentUserIEProxyConfig(Pointer pointer) {
		super(pointer);
		read();
	}

	/**
	 * Return this Structure's field names in their proper order. For example,
	 * 
	 * <pre>
	 * <code>
	 * protected List getFieldOrder() {
	 *     return Arrays.asList(new String[] { ... });
	 * }
	 * </code>
	 * </pre>
	 * 
	 * <strong>IMPORTANT</strong> When deriving from an existing Structure
	 * subclass, ensure that you augment the list provided by the superclass,
	 * e.g.
	 * 
	 * <pre>
	 * <code>
	 * protected List getFieldOrder() {
	 *     List fields = new ArrayList(super.getFieldOrder());
	 *     fields.addAll(Arrays.asList(new String[] { ... }));
	 *     return fields;
	 * }
	 * </code>
	 * </pre>
	 *
	 * Field order must be explicitly indicated, since the field order as
	 * returned by {@link Class#getFields()} is not guaranteed to be
	 * predictable.
	 * 
	 * @return ordered list of field names
	 */
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("fAutoDetect", "lpszAutoConfigUrl", "lpszProxy", "lpszProxyBypass");
	}

	/**
	 * Tagging interface to indicate the address of an instance of the Structure
	 * type is to be used within a <code>Structure</code> definition rather than
	 * nesting the full Structure contents. The default behavior is to inline
	 * <code>Structure</code> fields.
	 */
	public static class ByReference extends WinHttpProxyInfo implements Structure.ByReference {
	}

	/**
	 * Tagging interface to indicate the value of an instance of the
	 * <code>Structure</code> type is to be used in function invocations rather
	 * than its address. The default behavior is to treat <code>Structure</code>
	 * function parameters and return values as by reference, meaning the
	 * address of the structure is used.
	 */
	public static class ByValue extends WinHttpProxyInfo implements Structure.ByValue {
	}

}
