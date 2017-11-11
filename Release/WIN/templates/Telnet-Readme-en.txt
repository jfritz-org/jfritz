Since firmware version xx.03.96 there is an integrated call monitor in your FRITZ!Box. 
You do not need telnet access for this call monitor (FRITZ!Box call monitor). 

But you need telnet access for older FRITZ!Box-firmwares and the older call monitors(Telnet-, Syslog-,YAC- und Callmessage-call-monitors)

Here is a quick howto to enable telnet access:
1. Open "http://fritz.box" in your webbrowser.
2. Go to "fritz.box"->"System"->"Firmware-Update". Choose "telnet-ar7login-reset-debug.tar" (this file has been installed with JFritz) and start firmware update.
3. That's it. After a reboot of your FRITZ!Box telnet should have been started successfully.

To deactivate telnet access:
1. Connect to your FRITZ!Box via telnet and type in following commands:

echo > /var/flash/debug.cfg

/sbin/reboot