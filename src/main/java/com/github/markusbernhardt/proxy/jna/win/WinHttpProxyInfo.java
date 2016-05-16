package com.github.markusbernhardt.proxy.jna.win;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;

/**
 * The WINHTTP_PROXY_INFO structure contains the session or default proxy
 * configuration.
 * 
 * @author Markus Bernhardt, Copyright 2016
 */
public class WinHttpProxyInfo extends Structure {

    /**
     * Unsigned long integer value that contains the access type. This can be
     * one of the following values:
     * <ul>
     * <li>WINHTTP_ACCESS_TYPE_NO_PROXY</li>
     * <li>WINHTTP_ACCESS_TYPE_DEFAULT_PROXY</li>
     * <li>WINHTTP_ACCESS_TYPE_NAMED_PROXY</li>
     * </ul>
     */
    public WinDef.DWORD dwAccessType;

    /**
     * Pointer to a string value that contains the proxy server list.
     */
    public WTypes.LPWSTR lpszProxy;

    /**
     * Pointer to a string value that contains the proxy bypass list.
     */
    public WTypes.LPWSTR lpszProxyBypass;

    /**
     * Create WinHttpProxyInfo structure.
     */
    public WinHttpProxyInfo() {
        super();
    }

    /**
     * Create WinHttpProxyInfo structure cast onto pre-allocated memory.
     * 
     * @param pointer
     *            pointer to pre-allocated memory
     */
    public WinHttpProxyInfo(Pointer pointer) {
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
        return Arrays.asList("dwAccessType", "lpszProxy", "lpszProxyBypass");
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
