package com.github.markusbernhardt.proxy.search.wpad;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.ProxySearch.ScriptingEngineType;
import com.github.markusbernhardt.proxy.search.wpad.dhcp.DHCPMessage;
import com.github.markusbernhardt.proxy.search.wpad.dhcp.DHCPOptions;
import com.github.markusbernhardt.proxy.search.wpad.dhcp.DHCPSocket;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;

/*****************************************************************************
 * Uses automatic proxy script search (WPAD) to find an PAC file automatically.
 * <p>
 * Note: at the moment only the DNS name guessing schema is implemented. All
 * others are missing.
 * </p>
 * <p>
 * For more information about WPAD:
 * <a href="http://en.wikipedia.org/wiki/Web_Proxy_Autodiscovery_Protocol">
 * Web_Proxy_Autodiscovery_Protocol</a>
 * </p>
 * <p>
 * Outdated RFC draft: <a href=
 * "http://www.web-cache.com/Writings/Internet-Drafts/draft-ietf-wrec-wpad-01.txt">
 * draft-ietf-wrec-wpad-01.txt</a>
 * </p>
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class WpadProxySearchStrategy implements ProxySearchStrategy {

    private ScriptingEngineType engineType = ScriptingEngineType.NASHORHN;
    
  /*************************************************************************
   * Constructor
   ************************************************************************/

  public WpadProxySearchStrategy() {
    super();
  }
  
  public WpadProxySearchStrategy(ScriptingEngineType engineType) {
      this();
      this.engineType = engineType;
  }

  /*************************************************************************
   * Loads the proxy settings from a PAC file. The location of the PAC file is
   * determined automatically.
   * 
   * @return a configured ProxySelector, null if none is found.
   * @throws ProxyException
   *           on error.
   ************************************************************************/

  @Override
  public ProxySelector getProxySelector() throws ProxyException {
    try {
      Logger.log(getClass(), LogLevel.TRACE, "Using WPAD to find a proxy");

      String pacScriptUrl = detectScriptUrlPerDHCP();
      if (pacScriptUrl == null) {
        pacScriptUrl = detectScriptUrlPerDNS();
      }
      if (pacScriptUrl == null) {
        return null;
      }
      Logger.log(getClass(), LogLevel.TRACE, "PAC script url found: {0}", pacScriptUrl);
      return ProxyUtil.buildPacSelectorForUrl(engineType, pacScriptUrl);
    } catch (IOException e) {
      Logger.log(getClass(), LogLevel.ERROR, "Error during WPAD search.", e);
      throw new ProxyException(e);
    }
  }

  /*************************************************************************
   * Gets the printable name of the search strategy.
   * 
   * @return the printable name of the search strategy
   ************************************************************************/

  @Override
  public String getName() {
    return "wpad";
  }

  /*************************************************************************
   * Loads the settings and stores them in a properties map.
   * 
   * @return the settings.
   ************************************************************************/

  public Properties readSettings() {
    try {
      String pacScriptUrl = detectScriptUrlPerDHCP();
      if (pacScriptUrl == null) {
        pacScriptUrl = detectScriptUrlPerDNS();
      }
      if (pacScriptUrl == null) {
        return null;
      }
      Properties result = new Properties();
      result.setProperty("url", pacScriptUrl);
      return result;
    } catch (IOException e) {
      // Ignore and return empty properties.
      return new Properties();
    }
  }

  /*************************************************************************
   * Uses DNS to find the script URL. Attention: this detection method is known
   * to have some severe security issues.
   * 
   * @return the URL, null if not found.
   ************************************************************************/

  private String detectScriptUrlPerDNS() throws IOException {

    String fqdn = InetAddress.getLocalHost().getCanonicalHostName();
    if (fqdn.equalsIgnoreCase("localhost") || fqdn.length() == 0 || Character.isDigit(fqdn.charAt(0))) {
      return null;
    }

    Logger.log(getClass(), LogLevel.TRACE, "Searching per DNS guessing.");

    String[] fqdnParts = fqdn.split("\\.");
    for (int i = 0; i < fqdnParts.length; i++) {
      // Skip "wpad.<TLD>" (for instance "wpad.com"), as this is useless and unsafe
      if (i == fqdnParts.length - 2) {
	continue;
      }
      
      // Build URL
      StringBuilder stringBuilder = new StringBuilder("http://wpad");
      for (int j = i + 1; j < fqdnParts.length; j++) {
        stringBuilder.append('.');
        stringBuilder.append(fqdnParts[j]);
      }
      stringBuilder.append("/wpad.dat");

      // Try to connect to URL
      try {
        URL lookupURL = new URL(stringBuilder.toString());
        Logger.log(getClass(), LogLevel.TRACE, "Trying url: {0}", lookupURL);

        HttpURLConnection con = (HttpURLConnection) lookupURL.openConnection(Proxy.NO_PROXY);
        con.setInstanceFollowRedirects(true);
        con.setRequestProperty("accept", "application/x-ns-proxy-autoconfig");
        if (con.getResponseCode() == 200) {
          return lookupURL.toString();
        }
        con.disconnect();
      } catch (UnknownHostException e) {
        Logger.log(getClass(), LogLevel.DEBUG, "Not available!");
        // Not a real error, try next address
      }
    }

    return null;
  }

  /*************************************************************************
   * Uses DHCP to find the script URL.
   * 
   * @return the URL, null if not found.
   * @throws IOException 
   ************************************************************************/

  private String detectScriptUrlPerDHCP() throws IOException {
	  Logger.log(getClass(), LogLevel.TRACE, "Searching per DHCP.");
	String url = null;
	try {
	  Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	  while (interfaces.hasMoreElements()) {
        NetworkInterface network = interfaces.nextElement();
        if (!network.getName().equals("lo")) {
          byte[] mac = network.getHardwareAddress();
          Enumeration<InetAddress> ips = network.getInetAddresses();
          while (ips.hasMoreElements()) {
            InetAddress ip = ips.nextElement();
            // If ip is an IPv4 address
            if (ip.getAddress().length == 4)
              url = checkDhcpAckForPAC(ip, mac);
            if (url != null)
              return url;
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    return url;
  }
  
  private String getMacString(byte[] mac) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 6; i++)
      sb.append(String.format("%02X%s", mac[i], (i < 6 - 1) ? ":" : ""));
    return sb.toString();
  }

  private String checkDhcpAckForPAC(InetAddress ip, byte[] mac) throws IOException {
    DHCPMessage messageOut = new DHCPMessage(DHCPMessage.SERVER_PORT);
    DHCPMessage messageIn = new DHCPMessage(DHCPMessage.CLIENT_PORT);
    DHCPSocket bindSocket = new DHCPSocket(DHCPMessage.CLIENT_PORT, ip);
    // from DHCPINFORM Message Clarifications
    // https://tools.ietf.org/html/draft-ietf-dhc-dhcpinform-clarify-06

    // Clients are still required to fulfill the DHCPv4 requirements for
    // DHCPINFORM messages ([RFC2131], Sections 4.4.1 and 4.4.3). But
    // the following are clarified as in addition, or to overlay those
    // requirements:
    messageOut.setOp((byte) 1); // setup message to send a DCHPREQUEST
    // Clients MUST set 'ciaddr' to a working IPv4 address which they
    // can use to receive replies. This address SHOULD be an address
    // that is currently assigned to the interface upon which the client
    // is transmitting its DHCPINFORM, except in the condition where the
    // DHCP client is unable to determine a valid IP address for its
    // host, in which case the client MUST set 'ciaddr' to all-zero.
    messageOut.setCiaddr(ip.getAddress());
    // Clients MUST set 'chaddr', 'htype', and 'hlen' to the hardware
    // address of the interface upon which the DHCPINFORM message is
    // being transmitted, except in the condition where the DHCP client
    // is unable to determine this address, in which case all three
    // fields MUST be set all-zero.
    byte[] addr = new byte[16];
    if (mac != null)
      for (int i = 0; i < 6; i++)
        addr[i] = mac[i];
    // Despite what the spec says above if these are set to 0 you may
    // not get a response from some dhcp servers
    messageOut.setHtype((byte) 1); // 1 for Ethernet
    messageOut.setHlen((byte) 6); // 6 for IEEE 802 MAC addresses
    messageOut.setChaddr(addr);
    // Clients MUST set the 'flags' field to zero. This means that the
    // client MUST NOT set the 'BROADCAST' flag, and MUST be capable of
    // receiving IP unicasts.
    messageOut.setFlags((short) 0);
    messageOut.setHops((byte) 0);
    messageOut.setXid(new Random().nextInt());
    messageOut.setSecs((short) 0);
    messageOut.setYiaddr(InetAddress.getByName("0.0.0.0").getAddress());
    messageOut.setSiaddr(InetAddress.getByName("0.0.0.0").getAddress());
    messageOut.setGiaddr(InetAddress.getByName("0.0.0.0").getAddress());

    // From RFC 2131 Section 4.4 Table 5
    // https://tools.ietf.org/html/rfc2131#section-4.4
    // Looks like the only option we need to set for a DHCPINFORM
    // message is the DHCP message type
    messageOut.setOption(DHCPOptions.OPTION_DHCP_MESSAGE_TYPE, new byte[] { DHCPMessage.DHCPINFORM });
    Logger.log(getClass(), LogLevel.TRACE, "Trying DHCPInform on " + ip.getHostAddress() + (mac != null ? (" @ " + getMacString(mac)) : ""));
    bindSocket.send(messageOut);
    boolean sentinal = true;
    while (sentinal) {
      if (bindSocket.receive(messageIn)) {
        if (messageOut.getXid() == messageIn.getXid()) {
          byte[] pacFileLocationRaw = messageIn.getOption(DHCPOptions.OPTION_PROXY_AUTODISCOVERY);
          String pacFileLocation = null;
          if (pacFileLocationRaw != null)
            pacFileLocation = new String(pacFileLocationRaw);
          bindSocket.close();
          return pacFileLocation;
        } else {
          bindSocket.send(messageOut);
        }
      } else {
        Logger.log(getClass(), LogLevel.DEBUG, "Timed out.");
        sentinal = false;
      }
    }
    bindSocket.close();
    return null;
  }
}
