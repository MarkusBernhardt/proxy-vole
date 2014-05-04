cl "-IC:/Program Files/Java/jdk1.6.0_21/include" "-IC:/Program Files/Java/jdk1.6.0_21/include/win32" -LD proxy_util_w32.cpp dllmain.cpp stdafx.cpp -Feproxy_util_amd64.dll winhttp.lib Advapi32.lib 
