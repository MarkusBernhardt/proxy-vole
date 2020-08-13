package com.github.markusbernhardt.proxy.jna.win;

import com.github.markusbernhardt.proxy.util.Logger;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.ptr.ByReference;

/**
 * Pointer wrapper classes for various Windows SDK types. The JNA {@code WTypes} 
 * class already have a few of these, but oddly not for all.
 * 
 * <p>
 * TODO: Implement pointer wrapper classes for more WTypes, if and when needed.
 * 
 * @author phansson
 */
public class WTypes2 {
    
    private WTypes2() {}

    /**
     * A pointer to a LPWSTR.
     *
     * <p>
     * LPWSTR is itself a pointer, so a pointer to an LPWSTR is really a
     * pointer-to-pointer. This class hides this complexity and also takes care
     * of memory disposal.
     *
     * <p>
     * The class is useful where the Windows function <i>returns</i> a result
     * into a variable of type {@code LPWSTR*}. The class currently has no
     * setters so it isn't useful for the opposite case, i.e. where a Windows
     * function <i>accepts</i> a {@code LPWSTR*} as its input.
     *
     *
     * @author phansson
     */
    public static class LPWSTRByReference extends ByReference {

        public LPWSTRByReference() {
            super(Native.POINTER_SIZE);
			// memory cleanup
			getPointer().setPointer(0, null);
        }

        /**
         * Gets the LPWSTR from this pointer. In general its a lot more
         * convenient simply to use {@link #getString() getString}. 
         * 
         * @return LPWSTR from this pointer
         */
        public WTypes.LPWSTR getValue() {
            Pointer p = getPointerToString();
            if (p == null) {
                return null;
            }
            WTypes.LPWSTR h = new WTypes.LPWSTR(p);
            return h;
        }

        /**
         * Gets the string as pointed to by the LPWSTR or {@code null} if
         * there's no LPWSTR.
         * 
         * @return LPWSTR from this pointer
         */
        public String getString() {
            return getValue() == null ? null : getValue().getValue();
        }

        private Pointer getPointerToString() {
            return getPointer().getPointer(0);
        }

        /**
         * Memory disposal.
         *
         * @throws Throwable Something went wrong when cleaning up the memory.
         */
        @Override
        protected void finalize() throws Throwable {
            try {
                // Free the memory occupied by the string returned
                // from the Win32 function.
                Pointer strPointer = getPointerToString();
                if (strPointer != null) {
                    Pointer result = Kernel32.INSTANCE.GlobalFree(strPointer);
                    if (result != null) {
                        // The call to GlobalFree has failed. This should never
                        // happen. If it really does happen, there isn't much we 
                        // can do about it other than logging it.
                        Logger.log(getClass(), Logger.LogLevel.ERROR,
                                "Windows function GlobalFree failed while freeing memory for {0} object", 
                                getClass().getSimpleName());
                    }
                }
            } finally {
                // This will free the memory of the pointer-to-pointer
                super.finalize();
            }
        }

    }

}
