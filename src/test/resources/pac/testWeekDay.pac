// Test weekday functions

function FindProxyForURL(url, host) {
  weekdayRange("MON");
  weekdayRange("MON", "GMT");	
  weekdayRange("FRI", "MON");
  weekdayRange("MON", "WED", "GMT");
  	
  return "DIRECT";
}