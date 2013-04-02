#include "WiFly.h"
#include "Debug.h"
#define DEBUG_LEVEL 0
#define COMMAND_MODE_ENTER_RETRY_ATTEMPTS 5
#define COMMAND_MODE_GUARD_TIME 250 // in milliseconds
#define IP_ADDRESS_BUFFER_SIZE 16 // "255.255.255.255\0"
#define TIME_SIZE 11 // 1311006129
#define SOFTWARE_REBOOT_RETRY_ATTEMPTS 5

// Configuration options
#define WIFLY_BAUD 1
#define WAKE_TIMER 2
#define SLEEP_TIMER 3

// Join modes
#define WEP_MODE false
#define WPA_MODE true

/*
	PUBLIC METHODS
*/

void  WiFlyDevice::setUart(Stream* newUart)
{
  uart = newUart;
}

bool WiFlyDevice::begin() {
  /*
   */
  DEBUG_LOG(1, "Entered WiFlyDevice::begin()");
  
  if(softwareReboot()) {// Reboot to get device into known state
    enterCommandMode(true);
    //sendCommand(F("load tmp"), false, "AOK");
  //requireFlowControl();
  return true;
  } else {
	return false;
  }
}


boolean WiFlyDevice::join(const char *ssid) {
  /*
   */
  // TODO: Handle other authentication methods
  // TODO: Handle escaping spaces/$ in SSID
  // TODO: Allow for timeout?

  // TODO: Do we want to set the passphrase/key to empty when they're
  //       not required? (Probably not necessary as I think module
  //       ignores them when they're not required.)

  sendCommand(F("join "), true, "AOK");
  if (sendCommand(ssid, false, "Associated!")) {
    // TODO: Extract information from complete response?
    // TODO: Change this to still work when server mode not active
    waitForResponse("Listen on ");
    skipRemainderOfResponse();
    return true;
  }
  return false;
}


boolean WiFlyDevice::join(char *ssid, char *passphrase,
                          boolean isWPA) {
    // Handle escaping spaces/$ in passphrase and SSID
	for(int i = 0; ; i++) {
		if(ssid[i] == '\0') {
			break;
		}
		if(ssid[i] == ' ') {
			ssid[i] = '$';
		}
	}
	
	for(int i = 0; ; i++) {
		if(passphrase[i] == '\0') {
			break;
		}
		if(passphrase[i] == ' ') {
			passphrase[i] = '$';
		}
	}
	
	this->uart->println(ssid);
	this->uart->println(passphrase);

  // TODO: Do this better...
  sendCommand(F("set wlan "), true, "AOK");

  if (isWPA) {
    sendCommand(F("passphrase "), true, "AOK");
  } else {
    sendCommand(F("key "), true, "AOK");
  }

  sendCommand(passphrase, false, "AOK");

  return join(ssid);
}

boolean WiFlyDevice::join() {
  if (sendCommand(F("join"), false, "Associated!")) {
    waitForResponse("Listen on ");
    skipRemainderOfResponse();
	sendCommand(F("exit"), false, "EXIT");
    return true;
  }  
  return false;
}

boolean WiFlyDevice::configure(byte option, unsigned long value) {
  /*
   */

  // TODO: Allow options to be supplied earlier?

  switch (option) {
    case WIFLY_BAUD:
      // TODO: Use more of standard command sending method?
      enterCommandMode();
      uart->print("set uart instant ");
      uart->println(value);
      delay(10); // If we don't have this here when we specify the
                 // baud as a number rather than a string it seems to
                 // fail. TODO: Find out why.
      // For some reason the following check fails if it occurs before
      // the change of SPI UART serial rate above--even though the
      // documentation says the AOK is returned at the old baud
      // rate. TODO: Find out why
      if (!findInResponse("AOK", 100)) {
        return false;
      }
      break;
    case WAKE_TIMER:
      enterCommandMode();
      uart->print("set sys wake ");
      uart->println(value);
      if (!findInResponse("AOK", 100)) {
        return false;
      }
      break;
    case SLEEP_TIMER:
      enterCommandMode();
      uart->print("set sys sleep ");
      uart->println(value);
      if (!findInResponse("AOK", 100)) {
        return false;
      }
      break;
    default:
      return false;
      break;
  }
  return true;
}

const char * WiFlyDevice::getIp() {
  /*

    The return value is intended to be dropped directly
    into calls to 'print' or 'println' style methods.

   */
  static char ip[IP_ADDRESS_BUFFER_SIZE] = "";

  // TODO: Ensure we're not in a connection?

  enterCommandMode();

  // Version 2.19 of the WiFly firmware has a "get ip a" command but
  // we can't use it because we want to work with 2.18 too.
  sendCommand(F("get ip"), false, "IP=");

  char newChar;
  byte offset = 0;

  // Copy the IP address from the response into our buffer
  while (offset < IP_ADDRESS_BUFFER_SIZE) {
    newChar = uart->read();

    if (newChar == ':') {
      ip[offset] = '\x00';
      break;
    } else if (newChar != -1) {
      ip[offset] = newChar;
      offset++;
    }
  }

  // This handles the case when we reach the end of the buffer
  // in the loop. (Which should never happen anyway.)
  // And hopefully this prevents us from failing completely if
  // there's a mistake above.
  ip[IP_ADDRESS_BUFFER_SIZE-1] = '\x00';

  // This should skip the remainder of the output.
  // TODO: Handle this better?
  waitForResponse("<");
  while (uart->read() != ' ') {
    // Skip the prompt
  }

  // For some reason the "sendCommand" approach leaves the system
  // in a state where it misses the first/next connection so for
  // now we don't check the response.
  // TODO: Fix this
  uart->println("exit");
  //sendCommand("exit", false, "EXIT");

  return ip;
}



long WiFlyDevice::getTime(){

	/*
	Returns the time based on the NTP settings and time zone.
	*/

	char newChar;
	byte offset = 0;
	char buffer[TIME_SIZE+1];

	enterCommandMode();

	//sendCommand("time"); // force update if it's not already updated with NTP server
	sendCommand(F("show t t"), false, "RTC=");

	// copy the time from the response into our buffer
	while (offset < TIME_SIZE) {
			newChar = uart->read();

			if (newChar != -1) {
				buffer[offset++] = newChar;
			}
	}
	buffer[offset]=0;
  // This should skip the remainder of the output.
  // TODO: Handle this better?
  waitForResponse("<");
  waitForResponse(" ");

  // For some reason the "sendCommand" approach leaves the system
  // in a state where it misses the first/next connection so for
  // now we don't check the response.
  // TODO: Fix this
  uart->println(F("exit"));
  //sendCommand(F("exit"), false, "EXIT");


  return strtol(buffer, NULL, 0);
}

/*
	PRIVATE METHODS
*/

void WiFlyDevice::requireFlowControl() {
  /*


    Note: If flow control has been set but not saved then this
          function won't handle it correctly.

    Note: Any other configuration changes made since the last
          reboot will also be saved by this function so this
          function should ideally be called immediately after a
          reboot.

   */

  DEBUG_LOG(1, "Entered requireFlowControl");

  enterCommandMode();

  // TODO: Reboot here to ensure we get an accurate response and
  //       don't unintentionally save a configuration we don't intend?

  sendCommand(F("get uart"), false, "Flow=0x");

  while (!uart->available()) {
    // Wait to ensure we have the full response
  }

  char flowControlState = uart->read();

  uart->flush();

  if (flowControlState == '1') {
    return;
  }

  // Enable flow control
  sendCommand(F("set uart flow 1"), false, "AOK");

  sendCommand(F("save"), false, "Storing in config");

  // Without this (or some delay--but this seemed more useful/reliable)
  // the reboot will fail because we seem to lose the response from the
  // WiFly and end up with something like:
  //     "*ReboWiFly Ver 2.18"
  // instead of the correct:
  //     "*Reboot*WiFly Ver 2.18"
  // TODO: Solve the underlying problem
  sendCommand(F("get uart"), false, "Flow=0x1");

  softwareReboot();
}

void WiFlyDevice::setConfiguration() {
  enterCommandMode();

  // Turn off auto-connect
  sendCommand(F("set wlan join 0"), false, "AOK");
  // Turn off remote connect message
  sendCommand(F("set comm remote 0"), false, "AOK");

  sendCommand(F("set t z 23"), false, "AOK");
  sendCommand(F("set time address 129.6.15.28"), false, "AOK");
  sendCommand(F("set time port 123"), false, "AOK");
  sendCommand(F("set t e 15"), false, "AOK");
  sendCommand(F("set wlan auth 4"), false, "AOK");
  sendCommand(F("set ip dhcp 1"), false, "AOK");
}

boolean WiFlyDevice::sendCommand(const char *command,
                                 boolean isMultipartCommand = false,
                                 const char *expectedResponse = "AOK") {
  /*
   */
  DEBUG_LOG(1, "Entered sendCommand");
  DEBUG_LOG(2, "Command:");
  DEBUG_LOG(2, command);
  uart->print(command);
  delay(20);
  if (!isMultipartCommand) {
    uart->flush();
    uart->println();
    if (!findInResponse(expectedResponse, 1000)) {
      return false;
    }
  }
  DEBUG_LOG(2, "sendCommand exit True"); 

  return true;
}

boolean WiFlyDevice::sendCommand(const __FlashStringHelper *command,
                                 boolean isMultipartCommand = false,
                                 const char *expectedResponse = "AOK") {
  /*
   */
  DEBUG_LOG(1, "Entered sendCommand");
  DEBUG_LOG(2, "Command:");
  DEBUG_LOG(2, command);
  uart->print(command);
  delay(20);
  if (!isMultipartCommand) {
    uart->flush();
    uart->println();
	
    if (!waitForResponse(expectedResponse)) {
      return false;
    }
  }
  DEBUG_LOG(2, "sendCommand exit True");

  return true;
}

void WiFlyDevice::skipRemainderOfResponse() {
  /*
   */

  DEBUG_LOG(3, "Entered skipRemainderOfResponse");

    while (!(uart->available() && (uart->read() == '\n'))) {
      // Skip remainder of response
    }
}

boolean WiFlyDevice::responseMatched(const char *toMatch) {
  /*
   */
  boolean matchFound = true;
  unsigned long timeout;

DEBUG_LOG(3, "Entered responseMatched");
  for (unsigned int offset = 0; offset < strlen(toMatch); offset++) {
    timeout = millis();
    while (!uart->available()) {
      // Wait, with optional time out.
      if (millis() - timeout > 5000) {
          return false;
        }
      delay(1); // This seems to improve reliability slightly
    }
DEBUG_LOG(3,(char)uart->peek());
    if (uart->read() != toMatch[offset]) {
      matchFound = false;
      break;
    }
  }
  return matchFound;
}

boolean WiFlyDevice::findInResponse(const char *toMatch,
                                    unsigned int timeOut = 1000) {
  /*

   */

  // TODO: Change 'sendCommand' to use 'findInResponse' and have timeouts,
  //       and then use 'sendCommand' in routines that call 'findInResponse'?

  // TODO: Don't reset timer after successful character read? Or have two
  //       types of timeout?

  int byteRead;

  unsigned long timeOutTarget; // in milliseconds

  DEBUG_LOG(1, "Entered findInResponse");
  DEBUG_LOG(2, "Want to match:");
  DEBUG_LOG(2, toMatch);
  DEBUG_LOG(3, "Found:");

  for (unsigned int offset = 0; offset < strlen(toMatch); offset++) {

    // Reset after successful character read
    timeOutTarget = millis() + timeOut; // Doesn't handle timer wrapping

    while (!uart->available()) {
      // Wait, with optional time out.
      if (timeOut > 0) {
        if (millis() > timeOutTarget) {
          return false;
        }
      }
      delay(1); // This seems to improve reliability slightly
    }

    // We read this separately from the conditional statement so we can
    // log the character read when debugging.
    byteRead = uart->read();

    delay(1); // Removing logging may affect timing slightly

    DEBUG_LOG(5, "Offset:");
    DEBUG_LOG(5, offset);
    DEBUG_LOG(3, (char) byteRead);
    DEBUG_LOG(4, byteRead);

    if (byteRead != toMatch[offset]) {
      offset = 0;
      // Ignore character read if it's not a match for the start of the string
      if (byteRead != toMatch[offset]) {
        offset = -1;
      }
      continue;
    }
  }
  DEBUG_LOG(2, "Response found");

  return true;
}

boolean WiFlyDevice::waitForResponse(const char *toMatch) {
  /*
   */
   // Note: Never exits if the correct response is never found
   return findInResponse(toMatch);
}

boolean WiFlyDevice::enterCommandMode(boolean isAfterBoot) {
  /*

   */

  DEBUG_LOG(1, "Entered enterCommandMode");

  // Note: We used to first try to exit command mode in case we were
  //       already in it. Doing this actually seems to be less
  //       reliable so instead we now just ignore the errors from
  //       sending the "$$$" in command mode.

  for (int retryCount = 0;
       retryCount < COMMAND_MODE_ENTER_RETRY_ATTEMPTS;
       retryCount++) {

    // At first I tried automatically performing the
    // wait-send-wait-send-send process twice before checking if it
    // succeeded. But I removed the automatic retransmission even
    // though it makes things  marginally less reliable because it speeds
    // up the (hopefully) more common case of it working after one
    // transmission. We also now have automatic-retries for the whole
    // process now so it's less important anyway.

    if (isAfterBoot) {
      delay(1000); // This delay is so characters aren't missed after a reboot.
    }

    delay(COMMAND_MODE_GUARD_TIME);

    uart->print(F("$$$"));

    delay(COMMAND_MODE_GUARD_TIME);

    // We could already be in command mode or not.
    // We could also have a half entered command.
    // If we have a half entered command the "$$$" we've just added
    // could succeed or it could trigger an error--there's a small
    // chance it could also screw something up (by being a valid
    // argument) but hopefully it's not a general issue.  Sending
    // these two newlines is intended to clear any partial commands if
    // we're in command mode and in either case trigger the display of
    // the version prompt (not that we actually check for it at the moment
    // (anymore)).

    // TODO: Determine if we need less boilerplate here.

    uart->println();
    uart->println();

    // TODO: Add flush with timeout here?

    // This is used to determine whether command mode has been entered
    // successfully.
    // TODO: Find alternate approach or only use this method after a (re)boot?
    uart->println(F("ver"));

    if (findInResponse("\r\nWiFly Ver", 1000)) {
      // TODO: Flush or leave remainder of output?
      return true;
    }
  }
  return false;
}

boolean WiFlyDevice::softwareReboot(boolean isAfterBoot) {
  DEBUG_LOG(1, "Entered softwareReboot");
  
  boolean found = false;
  for (int retryCount = 0;
       retryCount < SOFTWARE_REBOOT_RETRY_ATTEMPTS;
       retryCount++) {

    // TODO: Have the post-boot delay here rather than in enterCommandMode()?

    if (!enterCommandMode(isAfterBoot)) {
      return false; // If the included retries have failed we give up
    }

    uart->println(F("reboot"));

    // For some reason the full "*Reboot*" message doesn't always
    // seem to be received so we look for the later "*READY*" message instead.

    // TODO: Extract information from boot? e.g. version and MAC address

    if (findInResponse("*READY*", 2000)) {
      found = true;
	  break;
    }
  }

  return found;
}

WiFlyDevice WiFly;
