// proxy_util_w23.cpp : Main methods of the DLL.

#include "stdafx.h"
#include "proxy_util_w32.h"

/*****************************************************************************
 * Class:     com_btr_proxy_search_desktop_win_Win32ProxyUtils
 * Method:    winHttpDetectAutoProxyConfigUrl
 * Signature: (I)Ljava/lang/String;
 ****************************************************************************/

JNIEXPORT jstring JNICALL Java_com_btr_proxy_search_desktop_win_Win32ProxyUtils_winHttpDetectAutoProxyConfigUrl
(JNIEnv *env, jobject source, jint mode) {


	LPWSTR ppwszAutoConfigUrl = NULL;
	BOOL result = WinHttpDetectAutoProxyConfigUrl( mode, &ppwszAutoConfigUrl );
	if (ppwszAutoConfigUrl == NULL) {
		return NULL;
	}
	
	jstring retValue = env->NewString((jchar*)ppwszAutoConfigUrl, 
									wcslen(ppwszAutoConfigUrl));

	GlobalFree( ppwszAutoConfigUrl );

	return retValue;
}

/*****************************************************************************
 * Class:     com_btr_proxy_search_desktop_win_Win32ProxyUtils
 * Method:    winHttpGetDefaultProxyConfiguration
 * Signature: ()Ljava/lang/String;
 ****************************************************************************/

JNIEXPORT jstring JNICALL Java_com_btr_proxy_search_desktop_win_Win32ProxyUtils_winHttpGetDefaultProxyConfiguration
(JNIEnv *env, jobject source) {

	WINHTTP_PROXY_INFO proxyInfo;

    // Retrieve the default proxy configuration.
    BOOL result = WinHttpGetDefaultProxyConfiguration( &proxyInfo );
	if (result == FALSE) {
		// TODO what to do in case of error.
		DWORD errorCode = GetLastError();
	}

	int proxyTypeLen = 0;
	int proxyLen = 0;
	int proxyBypassLen = 0;

	LPWSTR proxyType = NULL;
	if (proxyInfo.dwAccessType == WINHTTP_ACCESS_TYPE_NAMED_PROXY) {
		proxyType = L"PROXY ";
		proxyTypeLen = wcslen(proxyType);
	} else 
	if (proxyInfo.dwAccessType == WINHTTP_ACCESS_TYPE_NO_PROXY) {
		proxyType = L"DIRECT ";
		proxyTypeLen = wcslen(proxyType);
	} 
    if (proxyInfo.lpszProxy != NULL) {
		proxyLen += wcslen(proxyInfo.lpszProxy);
	}
    if (proxyInfo.lpszProxyBypass != NULL) {
		proxyBypassLen += wcslen(proxyInfo.lpszProxyBypass);
	}

	jstring retVal = proxyInfo.lpszProxy == NULL? NULL
		: env->NewString((jchar*)proxyInfo.lpszProxy, wcslen(proxyInfo.lpszProxy));

	if (proxyInfo.lpszProxy != NULL) {
        GlobalFree( proxyInfo.lpszProxy );
    }
    if (proxyInfo.lpszProxyBypass != NULL) {
        GlobalFree( proxyInfo.lpszProxyBypass );
    }
	return retVal;


	//int retValueLen = proxyTypeLen+proxyLen+1+proxyBypassLen+1;
	//int insertPos = 0;
	//LPWSTR combined = new WCHAR[retValueLen];
	//combined[retValueLen] = 0;

	//wcsncat_s(combined, retValueLen, proxyType, proxyTypeLen);
	//insertPos += proxyTypeLen;
	//retValueLen -= proxyTypeLen;

	//wcsncat_s(combined, retValueLen, proxyInfo.lpszProxy, proxyLen);
	//insertPos += proxyLen;
	//retValueLen -= proxyLen;

	//wcsncat_s(combined, retValueLen, TEXT("|"), 1);
	//insertPos += proxyLen;
	//retValueLen -= proxyLen;

	//wcsncat_s(combined, retValueLen, proxyInfo.lpszProxyBypass, proxyBypassLen);
	//insertPos += proxyBypassLen;
	//retValueLen -= proxyBypassLen;

 //   if (proxyInfo.lpszProxy != NULL) {
 //       GlobalFree( proxyInfo.lpszProxy );
 //   }
 //   if (proxyInfo.lpszProxyBypass != NULL) {
 //       GlobalFree( proxyInfo.lpszProxyBypass );
 //   }

	//jstring retVal = env->NewString((jchar*)combined, wcslen(combined));

	//return retVal;
}

/*****************************************************************************
 * Class:     com_btr_proxy_search_desktop_win_Win32ProxyUtils
 * Method:    WinHttpGetIEProxyConfigForCurrentUser
 * Signature: ()Lcom/btr/proxy/search/desktop/win/WinIESettings;
 ****************************************************************************/

JNIEXPORT jobject JNICALL Java_com_btr_proxy_search_desktop_win_Win32ProxyUtils_winHttpGetIEProxyConfigForCurrentUser
(JNIEnv *env, jobject source) {

	WINHTTP_CURRENT_USER_IE_PROXY_CONFIG ieProxyInfo;

    // Retrieve the IE proxy configuration.
    BOOL result = WinHttpGetIEProxyConfigForCurrentUser( &ieProxyInfo );
	if (result == FALSE) {
		DWORD errorCode = GetLastError();
		return NULL;
	}

	jboolean autoDetect = ieProxyInfo.fAutoDetect;
	jstring autoConfigUrl = NULL;
	jstring proxy = NULL;
	jstring proxyBypass = NULL;

	if (ieProxyInfo.lpszAutoConfigUrl != NULL) {
		autoConfigUrl = env->NewString((jchar*)ieProxyInfo.lpszAutoConfigUrl, wcslen(ieProxyInfo.lpszAutoConfigUrl));
        GlobalFree( ieProxyInfo.lpszAutoConfigUrl );
    }
    if (ieProxyInfo.lpszProxy != NULL) {
		proxy = env->NewString((jchar*)ieProxyInfo.lpszProxy, wcslen(ieProxyInfo.lpszProxy));
		GlobalFree( ieProxyInfo.lpszProxy );
    }
    if (ieProxyInfo.lpszProxyBypass != NULL) {
		proxyBypass = env->NewString((jchar*)ieProxyInfo.lpszProxyBypass, wcslen(ieProxyInfo.lpszProxyBypass));
		GlobalFree( ieProxyInfo.lpszProxyBypass );
    }

	// Build result container object.
	jclass retValueClass = env->FindClass("com/btr/proxy/search/desktop/win/WinIESettings");
	if ( retValueClass == NULL ) {
		return NULL;
	}
	
	jmethodID jmid = env->GetMethodID(retValueClass, "<init>", "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
	if (jmid == NULL) {
		return NULL;
	}

	// WinIESettings(boolean autoDetect, String autoConfigUrl, String proxy, String proxyBypass)
	jobject retValue = env->NewObject(retValueClass, jmid, autoDetect, autoConfigUrl, proxy, proxyBypass);

	return retValue;
}

/*****************************************************************************
 * Class:     com_btr_proxy_search_desktop_win_Win32ProxyUtils
 * Method:    readUserHomedir
 * Signature: ()Ljava/lang/String;
 ****************************************************************************/

JNIEXPORT jstring JNICALL Java_com_btr_proxy_search_desktop_win_Win32ProxyUtils_readUserHomedir
(JNIEnv *env, jobject source) {
	HKEY key;
	int result = RegOpenKeyEx(HKEY_CURRENT_USER, 
#ifdef _WIN64			
				"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
#else				
				L"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
#endif				
				0, KEY_QUERY_VALUE, &key); 
	if (0 != ERROR_SUCCESS) {
		LPWSTR errorMsg = L"ERROR: Key open failed";
		return env->NewString((jchar*)errorMsg, wcslen(errorMsg));
	}

	BYTE pvData[1000];
	DWORD dataSize = 1000;
	
#ifdef _WIN64			
	result = RegQueryValueEx(key, "AppData", NULL, NULL, pvData, &dataSize);
#else
	result = RegQueryValueEx(key, L"AppData", NULL, NULL, pvData, &dataSize);
#endif	
	RegCloseKey(key);
	if (result != ERROR_SUCCESS) {
		LPWSTR errorMsg = L"ERROR: Read value failed";
		return env->NewString((jchar*)errorMsg, wcslen(errorMsg));
	}

	jstring retValue = env->NewString((jchar*)pvData, (dataSize-1)/2);
	return retValue;
}


