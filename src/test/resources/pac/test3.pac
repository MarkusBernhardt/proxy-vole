// Test space in IP in scripts

function FindProxyForURL(url, host) {

  /*
   * This is a multiline comment
   */
if (isInNet(myIpAddress(), "0.0.0.0", "255.255.255.0 ")){
  return "DIRECT"; // This returns always DIRECT
}
}