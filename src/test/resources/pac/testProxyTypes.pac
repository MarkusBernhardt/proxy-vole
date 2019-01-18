function FindProxyForURL(url, host)
{
    return "PROXY  my-proxy.com:80 ; HTTP my-proxy2.com: 80; HTTPS my-proxy3.com :486; SOCKS my-proxy4.com:80; SOCKS4 my-proxy5.com:80;  SOCKS5 my-proxy6.com:80;";
}