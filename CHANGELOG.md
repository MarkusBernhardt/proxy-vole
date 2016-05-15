#Change Log
This file contains the change log. The changes are marked with the following symbology.

##Unreleased
* Fork on github
* Made it deployable to Maven Central for me (Maven coordinates, package names)
* Added OsxFirefoxProfileSource
* Removed the Rhino Javascript parser needed for Java 5 

##Last version off proxy-vole at Google Code repository 
* Preparing migration to Maven
* We now support on Linux Gnome the dconf settings format
* Fixed  issue 55 : Improved shExpMatch method
* Fixed  issue 51 :	UnsatisfiedLinkError if dll was cleaned up by another instance that is closed.
* Fixed  issue 50 : Mixed white list with "local bypass" enabled
* Fixed  issue 48 : Expiry date for redownload of pac script content was broken.
* Fixed  issue 47 : If no PAC selector is available ignore the whitelist settings (OSX only).

##20131209
* Fixed issue with myIPAddress() returning 127.0.0.1 instead of the real address on linux
* Fixed  issue 46 : Handle cleanup of temp files more robust
* Fixed  issue 44 : Selecting bluetooth interface in OS/X 10.8
* Fixed  issue 39 : Default timeout is used when fetching PAC script, causes 5 minute delay
* Fixed  issue 38 : Detect invalid PAC scripts early and abort
* Fixed  issue 36 : methods returned java.lang.String objects instead of JS strings

##20121203
* Fixed  issue 31 : Improved the support for "no proxy" definitions.
* Fixed  issue 29 : KDE settings file was not found on KDE4. This is solved now.
* Fixed  issue 25 : Disabled DTD and schema validation in settings parsers.

##20120920
* Improved the fix for issue 26 PAC download recursion bug

##20120905
* Fixed  issue 26 : Implemented (hopefully) better fix for PAC download recursion bug 
* Fixed  issue 27 + 22 : JavaProxySearch was used always even if no http.proxyHost property was set.

##20120727
* Fixed  issue 19 : Added check to return null if no KDE settings are available.
* Fixed  issue 20 : Fix in isInNet method. Resolve host names to IP addresses.
* Fixed  issue 21 : Improved handling of empty/invalid URIs

##20111102
* Added support for automatic retry if PAC returns a list of fallback proxies.
* Started to implement IPv6 support
* Fixed  issue 14 : Added code to cleanup temporary dll files that were not deleted on VM exit.
* Fixed  issue 18 : Added code to support the https.proxy system properties too.
* Fixed  issue  9 : IE PAC file urls are now patched to be in line with Firefox URLs.

##20110515
* Added (experiemental) support for Max OSX. This is not very well tested yet but should work. 
* Fixed  issue 15 : The Firefox proxy search lacked support for socks proxy. This is now available.
* Fixed  issue 12 : The PAC JavaScript method isInNet did not work for all patterns. This is fixed now.

##20110210
* Added a debug system property "com.btr.proxy.pac.overrideLocalIP" for the PAC parser 
* Added Support for the IE "Bypass Proxy Server for local addresses" feature
* Fixed issue 11: Parsing was unreliable when a PAC script returned more than one proxy 
* Some code cleanup to fix some minor problems (platform detection issues and others) 

##20100914
* PAC support on Java 1.6 will now use the javax.script framework and the internal javascript engine that 
  is shipped with JRE6. This will allow you to support PAC without bundling the Rhino engine which will dramatically 
  reduce the footprint of the library. Java 1.5 will still be supported but then you need to bundle it with the Rhino engine.
* Added contributed dlls to support 64 windows version for the IE proxy detection.
* Some fixes for the http client that is used to download PAC scripts from a webserver. We accept now all content-type 
  send by the server and we support now different charsets for the scripts.
* Fixed some unit tests that did not pass and did some other small fixes.

##20100724
* Fixed Issue 5: PAC Parser was not working correctly in multithreaded environments.

##20100411
* Fixed Issue 4: Improved parser used to parse proxy urls from environment varibales and other places

