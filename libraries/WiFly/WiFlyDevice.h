#ifndef __WIFLY_DEVICE_H__
#define __WIFLY_DEVICE_H__

class WiFlyDevice {
  public:
    void setUart(Stream* newUart);
    bool begin();

    boolean join(const char *ssid);
    boolean join( char *ssid,  char *passphrase, 
                 boolean isWPA = true);
	boolean join();

    boolean configure(byte option, unsigned long value);
	const char * getIp();
	long getTime();
    
  private:
    Stream* uart; 
    
    void requireFlowControl();
    void setConfiguration();
    boolean sendCommand(const char *command,
                        boolean isMultipartCommand, // Has default value
                        const char *expectedResponse); // Has default value
	boolean sendCommand(const __FlashStringHelper *command,
                        boolean isMultipartCommand, // Has default value
                        const char *expectedResponse); // Has default value
    void skipRemainderOfResponse();
    boolean responseMatched(const char *toMatch);

    boolean findInResponse(const char *toMatch, unsigned int timeOut);
	boolean waitForResponse(const char *toMatch);
    boolean enterCommandMode(boolean isAfterBoot = false);
    boolean softwareReboot(boolean isAfterBoot = false);

    friend class WiFlyClient;
};

#endif
