autoproxy WIP alpha
=========

Autoproxy is a Java library to auto detect the platform network proxy settings. 
**Note this library is a fork of [proxy-vole](https://code.google.com/p/proxy-vole/) by Bernd Rosstauscher hosted at Google Code.**

Introduction
------------

The library provides some proxy setting search strategies to read the proxy settings from the system config 
(Windows, KDE, Gnome, OSX), browser config (Firefox, IE) or environment variables and provides you an ready to use proxy selector.

Why a fork?
-----------
* Can't contact Bernd Rosstauscher.
* Google Code is dead by now.
* proxy-vole seems to be dead even longer. Last commit over a year ago.
* proxy-vole is not available on any public Maven repository. Need to change the Maven coordinates and Java package names 
  to be able to push it to Maven Central on my own.
* I don't like the Windows DLL and usage of JNI. Will replace both by JNA. Hopefully ...
* I don't like the name ;-)

Motivation
----------

Today more and more applications try to take direct advantage of internet connectivity and webservices to bring webcontent
to your desktop. Java has very good network and internet connectivity with build in APIs for http, https, ftp and a webservices stack.
But the first thing that you need, to use all this fantastic technology, is a working internet connection. And this is where Java
lacks a lot in my opinion. When you develop an applet or an Java Webstart application the situation is quite good. The new Java 
Plugin will use the proxy settings and connections as used by the browser, but for standalone applications the situation is 
quite unsatisfactory. You have to ask your users to twiddle with System Properties and every Java application has 
it's own way of setting proxy configuration. In business environments where you often can find proxy configuration scripts you are stuck.

Current Situation
-----------------

To set the proxy settings in Java you can use some (documented but hard to find) System Properties on application startup. 
At runtime you can use the `ProxySelector` API to configure the proxy settings. Java even comes with a system property to 
detect the system proxy settings automatically but this one is poorly documented and unreliable in its behaviour.

The Solution
------------

To provide network connectivity out of the box for your Java application you can use the autoproxy library. It provides strategies 
for auto detecting the current proxy settings. There are many configurable strategies to choose from. At the moment autoproxy 
supports the following proxy detection strategies.

* Read platform settings (Supports: Windows, KDE, Gnome, OSX)
* Read browser setting (Supports: Firefox 3.x+, Internet Explorer; Chrome and Webkit use the platform settings)
* Read environment variables (often used variables on Linux / Unix server systems)
* Autodetection script by using WPAD/PAC (Not all variations supported)

