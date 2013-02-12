#include <Stream.h>

class SensorProtocol {
public:
	SensorProtocol(int location);
	void addValue(int sensorType, float value);
	char* createMessage();
	void setSensors(int sensors[], int length);
	
private:
	int location;
	int *sensorTypes;
	float *values;	
	int sensorLength;
};