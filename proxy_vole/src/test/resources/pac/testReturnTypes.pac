// Test that we have JS data types: e.g string and not java.lang.String

function FindProxyForURL(url, host) {
  return typeof dnsDomainLevels(host) + " " +
         typeof isResolvable(host) + " " +
         typeof myIpAddress();
}