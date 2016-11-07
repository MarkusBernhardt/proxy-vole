#proxy-vole

Proxy Vole is a Java library to auto detect the platform network proxy settings.  
Note: This library is a fork of the now dead [proxy-vole](https://code.google.com/p/proxy-vole/) project by Bernd Rosstauscher hosted at Google Code.

##Introduction
The library provides some proxy setting search strategies to read the proxy settings from the system config 
(Windows, KDE, Gnome, OSX), browser config (Firefox, IE) or environment variables and provides you an ready to use proxy selector.

##Why a fork?
* Can't contact Bernd Rosstauscher.
* Google Code is dead by now.
* proxy-vole seems to be dead even longer. Last commit mid 2015. Last release end 2013.
* proxy-vole is not available on any public Maven repository. Needed to change the Maven coordinates and Java package names
  to be able to push it to Maven Central on my own.
* I don't like the Windows DLL and usage of JNI. Replaced both by JNA.

##Usage

###Using the default strategy to find the settings
```Java
// Use the static factory method getDefaultProxySearch to create a proxy search instance 
// configured with the default proxy search strategies for the current environment.
ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

// Invoke the proxy search. This will create a ProxySelector with the detected proxy settings.
ProxySelector proxySelector = proxySearch.getProxySelector();

// Install this ProxySelector as default ProxySelector for all connections.
ProxySelector.setDefault(proxySelector);
```

###Modifying the search strategy
```Java
// Create a not configured proxy search instance and configure customized proxy search strategies.
ProxySearch proxySearch = new ProxySearch();
if (PlatformUtil.getCurrentPlattform() == Platform.WIN) {
    proxySearch.addStrategy(Strategy.IE);
    proxySearch.addStrategy(Strategy.FIREFOX);
    proxySearch.addStrategy(Strategy.JAVA);
} else if (PlatformUtil.getCurrentPlattform() == Platform.LINUX) {
    proxySearch.addStrategy(Strategy.GNOME);
    proxySearch.addStrategy(Strategy.KDE);
    proxySearch.addStrategy(Strategy.FIREFOX);
} else {
    proxySearch.addStrategy(Strategy.OS_DEFAULT);
}
```

###Improving PAC performance
When your system uses a proxy automation script (PAC) Javascript is used to determine the actual proxy. 
If your program needs to access a lot of HTTP URLs, then this might become a performance bottleneck.
To speed things up a little bit, you can activate a cache that will store already processed URLs.
When a cached URL is accessed the Javascript execution will be skipped and the cached proxy is used.
```Java
// Use the static factory method getDefaultProxySearch to create a proxy search instance 
// configured with the default proxy search strategies for the current environment.
ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();

// Cache 20 hosts for up to 10 minutes. This is the default.
proxySearch.setPacCacheSettings(20, 1000*60*5, CacheScope.CACHE_SCOPE_HOST);
```

###How to handle proxy authentication
Some proxy servers request a login from the user before they will allow any connections. Proxy Vole 
has no support to handle this automatically. This needs to be done manually, because there is no way to read 
the login and password. These settings are stored encrypted. You need to install an authenticator in your Java
program manually and e.g. ask the user in a dialog to enter the username and password.
```Java
Authenticator.setDefault(new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
            return new PasswordAuthentication("proxy-user", "proxy-password".toCharArray());
        } else { 
            return super.getPasswordAuthentication();
        }
    }               
});
```

###Choose the right proxy
Please be aware a Java ProxySelector returns a list of valid proxies for a given URL and sometimes simply 
choosing the first one is not good enough. Very often a check of the supported protocol is neccessary.

The following code chooses the first HTTP/S proxy.
```Java
Proxy proxy = Proxy.NO_PROXY;

// Get list of proxies from default ProxySelector available for given URL
List<Proxy> proxies = null;
if (ProxySelector.getDefault() != null) {
    proxies = ProxySelector.getDefault().select(uri);
}

// Find first proxy for HTTP/S. Any DIRECT proxy in the list returned is only second choice
if (proxies != null) {
    loop: for (Proxy p : proxies) {
        switch (p.type()) {
        case HTTP:
            proxy = p;
            break loop;
        case DIRECT:
            proxy = p;
            break;
        }
    }
}
```

###Logging
Proxy Vole does not use Log4J, LogBack or SLF4J to make the library as light weight as possible with no external dependencies.
If you need to know what is going on inside of the library you may want to install a logger.
```Java
// Register MyLogger instance 
Logger.setBackend(new MyLogger());
```

###Testing PAC
Testing the PAC parser can be problematic, because the myIPAddress() method returns different results on different machines.
Therefore the system property com.github.markusbernhardt.proxy.pac.overrideLocalIP can be set for unit tests.
It's value will always be used as myIPAddress in all PAC scripts.
```Java
System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "123.123.123.123");
```

###Proxy Vole Tester
There is also a small GUI to test the different search strategies. Simply start the [jar-with-dependencies](http://search.maven.org/remotecontent?filepath=com/github/markusbernhardt/proxy-vole/1.0.1/proxy-vole-1.0.1-jar-with-dependencies.jar) 
or directly the class `com.github.markusbernhardt.proxy.ui.ProxyTester`.

![Screenshot](https://raw.githubusercontent.com/MarkusBernhardt/proxy-vole/master/src/site/screenshots/proxy-vole-tester.png "Proxy Vole Tester")

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
Use the Proxy Vole library to provide network connectivity out of the box for your Java application. It provides strategies 
for auto detecting the current proxy settings. There are many configurable strategies to choose from. At the moment Proxy Vole 
supports the following proxy detection strategies.

* Read platform settings (Supports: Windows, KDE, Gnome, OSX)
* Read browser setting (Supports: Firefox 3.x+, Internet Explorer; Chrome and Webkit use the platform settings)
* Read environment variables (often used variables on Linux / Unix server systems)
* Auto detection script by using WPAD/PAC (Not all variations supported)

