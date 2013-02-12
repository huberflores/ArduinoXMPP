/*
 * WifiDevice.h
 *
 *  Created on: 05.03.2012
 *      Author: Kaarel
 */

#ifndef WIFIDEVICE_H_
#define WIFIDEVICE_H_

#define N_PROMPTS 8
#define WIFLY_MSG_EXPECTED  0
#define WIFLY_MSG_AOK  1
#define WIFLY_MSG_CMD  2
#define WIFLY_MSG_ERR  3
#define WIFLY_MSG_PROMPT  4
#define WIFLY_MSG_PROMPT2  5
#define WIFLY_MSG_CLOSE  6
#define WIFLY_MSG_OPEN  7

// Auth Modes for Network Authentication
// See WiFly manual for details
#define WIFI_AUTH_OPEN        0    // Open (default)
#define WIFI_AUTH_WEP         1    // WEP-128
#define WIFI_AUTH_WPA1        2    // WPA1
#define WIFI_AUTH_WPA1_2      3    // Mixed-mode WPA1 and WPA2-PSK
#define WIFI_AUTH_WPA2_PSK    4    // WPA2-PSK
#define WIFI_AUTH_ADHOC       6    // Ad-hoc, join any Ad-hoc network
static char* WiFlyFixedPrompts[N_PROMPTS] = { "", "AOK", "CMD", "ERR: ?", "",
		">", "*CLOS*", "*OPEN*" };

class WiFiDevice {
public:
	WiFiDevice();

	void setUart(Stream* newUart);

	void sendCommand(const char *command, bool isMultipartCommand,
			const char *expectedResponse);
	void enderCommandMode();

private:
	Stream* uart;
};

#endif /* WIFIDEVICE_H_ */
