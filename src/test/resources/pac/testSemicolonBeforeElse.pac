// Nashorn scripting engine fails to parse this script with 
// jdk.nashorn.internal.runtime.ParserException: :8:2 Expected an operand but found else
// else return "PROXY proxy:3128";
function FindProxyForURL(url, host)
    {
        if (shExpMatch(host, "deploy.abc.eu"))

            return "PROXY proxy:3128";

        else if (isInNet(myIpAddress(), "10.25.48.0", "255.255.240.0"))

	    return "PROXY dmzproxy:3128; PROXY proxy:3128; DIRECT";

        else if (isInNet(myIpAddress(), "10.26.128.0", "255.255.248.0"))

	    return "PROXY proxy:3128; DIRECT";

        else if (isInNet(myIpAddress(), "10.25.0.0", "255.255.0.0") 
		)

	    return "PROXY dmzproxy:3128; PROXY proxy:3128; DIRECT";

        else

	    return "PROXY proxy.:3128; PROXY dmzproxy:3128";
    }