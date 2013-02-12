#include <Utils.h>
#include <string.h>

bool Utils::startsWith(char* buffer, char* item) {
  if(strlen(item) > strlen(buffer)) {
    return false; 
  }
  for(int i = 0; ;i++) {
    if(item[i] == '\0') {
      break;
    }
    if(item[i] != buffer[i]){
      return false; 
    }
  }
  return true;
}

bool Utils::endsWith(char* buffer, char* item) {
  if(strlen(item) > strlen(buffer)) {
    return false;
  } 
  for(int i = strlen(item) - 1, j = strlen(buffer) - 1;i >= 0; i--,j--){
    if(item[i] != buffer[j]){
      return false;
    }
  }
  return true;
}
