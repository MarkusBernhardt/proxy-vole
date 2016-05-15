#autoproxy WIP alpha

Autoproxy is a Java library to auto detect the platform network proxy settings.  
**Note this library is a fork of [proxy-vole](https://code.google.com/p/proxy-vole/) by Bernd Rosstauscher hosted at Google Code.**

##Introduction
The library provides some proxy setting search strategies to read the proxy settings from the system config 
(Windows, KDE, Gnome, OSX), browser config (Firefox, IE) or environment variables and provides you an ready to use proxy selector.

##Why a fork?
* Can't contact Bernd Rosstauscher.
* Google Code is dead by now.
* proxy-vole seems to be dead even longer. Last commit over a year ago.
* proxy-vole is not available on any public Maven repository. Need to change the Maven coordinates and Java package names 
  to be able to push it to Maven Central on my own.
* I don't like the Windows DLL and usage of JNI. Will replace both by JNA. Hopefully ...
* I don't like the name ;-)

##Usage

###Using the default strategy to find the settings
```Java
// Use the static factory method getDefaultProxySearch to create a proxy search instance 
// configured with default proxy search strategies for the current environment.
ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

// Invoke the proxy search. This will create a ProxySelector with the detected proxy settings.
ProxySelector proxySelector = proxySearch.getProxySelector();

// Install this ProxySelector as default ProxySelector for all connections.
ProxySelector.setDefault(proxySelector);
```

##Motivation
Today more and more applications try to take direct advantage of Internet connectivity and web services to bring web content
to your desktop. Java has very good network and Internet connectivity with build in APIs for HTTP, HTTPS, FTP and a web services stack.
But the first thing that you need, to use all this fantastic technology, is a working Internet connection. And this is where Java
lacks a lot in my opinion. When you develop an applet or an Java Webstart application the situation is quite good. The Java 
Plugin will use the proxy settings and connections as used by the browser, but for standalone applications the situation is 
quite unsatisfactory. You have to ask your users to twiddle with system properties and every Java application has 
it's own way of setting proxy configurations. In business environments where you often can find proxy configuration scripts you are stuck.

##Current Situation
To set the proxy settings in Java you can use some (documented, but hard to find) system properties on application startup. 
At runtime you can use the `ProxySelector` API to configure the proxy settings. Java even comes with a system property to 
detect the system proxy settings automatically, but this one is poorly documented and unreliable in its behaviour.

##The Solution
Use the autoproxy library to provide network connectivity out of the box for your Java application. It provides strategies 
for auto detecting the current proxy settings. There are many configurable strategies to choose from. At the moment autoproxy 
supports the following proxy detection strategies.

* Read platform settings (Supports: Windows, KDE, Gnome, OSX)
* Read browser setting (Supports: Firefox 3.x+, Internet Explorer; Chrome and Webkit use the platform settings)
* Read environment variables (often used variables on Linux / Unix server systems)
* Auto detection script by using WPAD/PAC (Not all variations supported)

