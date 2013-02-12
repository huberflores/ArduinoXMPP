#include <SensorProtocol.h>
#include <avr/pgmspace.h>
#include <stdio.h>
#include <stdlib.h>
#include <Arduino.h>

prog_char sensor_data[] PROGMEM = "SensorData{\"location\":%i, \"data\":[%s]}";

prog_char data_object[] PROGMEM = "{\"type\":%i, \"value\":%s}";

SensorProtocol::SensorProtocol(int location) {
	this->location = location;
}

void SensorProtocol::setSensors(int sensors[], int length) {
	this->sensorTypes = sensors;
	this->sensorLength = length;
	this->values = (float*) malloc(length * sizeof(float));
}

void SensorProtocol::addValue(int sensorType, float value) {
	this->values[sensorType - 1] = value;
}

char* SensorProtocol::createMessage() {
	int tmpLength = strlen_P(sensor_data) + 1;
	char temp[tmpLength];
	strcpy_P(temp, sensor_data);
	
	int dataLength = strlen_P(data_object) + 1;
	char dataTemp[dataLength];
	strcpy_P(dataTemp, data_object);
	
	int dataArrLength = dataLength + 5;
	int totalDataArrLength = sensorLength * dataArrLength + 1;
	char totalDataArr[totalDataArrLength];
	for(int i = 0; i < sensorLength; i++) {
		char num[6];
		dtostrf(this->values[i], 6, 2, num);
		char dataArray[dataArrLength + 1];
		sprintf(dataArray, dataTemp, this->sensorTypes[i], num);
		if(i == 0) {
			strcpy(totalDataArr, dataArray);
		} else {
			strcat(totalDataArr, ",");
			strcat(totalDataArr, dataArray);
		}
	}
	int totalMessageLength = sensorLength * dataArrLength + tmpLength;
	char buffer[totalMessageLength];
	sprintf(buffer, temp, this->location, totalDataArr);
	return buffer;
}