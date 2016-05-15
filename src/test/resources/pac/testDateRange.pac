// Test date range functions

function FindProxyForURL(url, host) {
  dateRange(1, 30);
  dateRange("JUN", "JUL");	
  dateRange(2008, 2009);	
  dateRange("JUN", "JUL", "GMT");	
  dateRange(1, "JUN", 2008, 30, "JUL", 2099, "GMT");	
  	
  return "DIRECT";
}