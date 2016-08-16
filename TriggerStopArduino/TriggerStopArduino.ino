#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <Wire.h>
#include <Adafruit_MMA8451.h>
#include <Adafruit_Sensor.h>
#include <Servo.h>

Adafruit_MMA8451 mma = Adafruit_MMA8451();
SoftwareSerial RFID (8, 9);
int officerListStart, officerListEnd, officerListVersion, officerListCount;
const int EEsize = 1024;
unsigned long officerList[50];
//unsigned long* tempNewList = 0;
boolean hasOfficerList = false;
boolean isDrawn = false;
int drawnTime = 0;
boolean safety = true;
bool isRaised = false;
int raisedTime = 0;
bool shot = false;
Servo safetyServo; 


void setupEEP() {
  //EEPROM[0] = 128;
  //Serial.print("update");
  officerList[0] = 2208127060;
  officerList[1] = 1774460217;
  officerList[2] = 1110418396;
  officerList[3] = 1158641696;
  officerList[4] = 2372790555;
  //delete [] officerList;
  officerListCount = 5;
  officerListVersion = 1;
  int index = 0;
  EEPROM.put(index, officerListVersion);
  index += sizeof(int);
  EEPROM.put(index, officerListCount);
  index += sizeof(int);
  for(int i = 0; i < 5; i ++){
    EEPROM.put(index, officerList[i]);
    index += sizeof(unsigned long);
    //saveEEPROM();
  }
}
void fired(){
  if(isDrawn){
  shot = true;
  }
}

void setup() {
  pinMode(7, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(7), fired, LOW );
  
  Serial1.begin(9600);
  //Serial.begin(9600);
  setupEEP();
  RFID.begin(9600);
  RFID.listen();
  while (!Serial1) {
    //waits until Serial is initialized
  }
//  }while(!Serial){
//    
//  }
  if (! mma.begin()) {
    //Serial.println("Couldnt start");
  }
  safetyServo.attach(10);
  safetyServo.write(7);
  //Serial.println("MMA8451 found!");
  mma.setRange(MMA8451_RANGE_2_G);
  int index = 0;
  int v = 0;
  EEPROM.get(index, v);
  //Serial.println(v);
  if(v> 4){
  //  Serial.print("update");
    setupEEP();
    EEPROM.get(index,v);
  }
  if (v > 0) {
    index += sizeof(int);
    hasOfficerList = true;
    int count = 0;
    EEPROM.get(index, count);
    index += sizeof(int);
    unsigned long list[count];
    for(int i=0; i<count; i++){
      EEPROM.get(index, list[i]);
      index += sizeof(unsigned long);
    }
    officerListVersion = v;
    officerListCount = count;
    for(int i=0; i< count; i++){
      officerList[i] = list[i];
      //Serial.println(officerList[i]);
    }
  } else if(v > 4){
    //Serial.print("update");
    setupEEP();
    EEPROM.get(index, officerListVersion);
  }
  while (Serial1.available() < 1) {}
  String in = Serial1.readStringUntil(':');
  if (in == "start") {
    Serial1.print("ready:1:");
    Serial1.print(officerListVersion);
    Serial1.print('\n');
  }




  mma.setRange(MMA8451_RANGE_2_G);
}

void loop() {
  if(drawnTime >20 ){
    drawnTime = 0;
    isDrawn = false;
    safety = true;
    lock(true);
  }
  if(isRaised){
    raisedTime ++;
  }if(raisedTime >10){
    raisedTime = 0;
    isRaised = false;
  }
  if(Serial1.available()){
  String in = Serial1.readStringUntil(':');
  if (in == "start") {
    Serial1.print("ready:1:");
    Serial1.print(officerListVersion);
    Serial1.print('\n');
  }
  if (in == "?newList") {
    updateOfficerList();
  }
  }
  char tag[8];
  unsigned long id;
  if (RFID.available()) {
    if(RFID.read() == 2){
    //Serial.write(RFID.read());
    int i = 0;
    RFID.read();
    RFID.read();
    RFID.read();
    RFID.read();
    while(i < 9){
      tag[i] = RFID.read();
    i++;
  }
  id = strtoul(tag,NULL,16);
  if(validateTag(id)&& !isDrawn){
    drawnTime = 0;
    if(safety){
      //Serial.println("drawn!");
      Serial1.print("drawn::");
      Serial1.print(id);
      Serial1.print('\n');
    }
    lock(false);
    safety = false;
  }else if(validateTag(id)){
    drawnTime = 0;
    //lock(true);
    //safety = true;
  }
    delay(150);
  }
  }
  if(isDrawn){
    if(shot){
      Serial1.print("fired::");
      Serial1.print(id);
      Serial1.print('\n');
      shot = false;
    }
    drawnTime ++;
  }

  
  mma.read();
 
  uint8_t o = mma.getOrientation(); 
//  switch (o) {
//    case MMA8451_PL_PUF: 
//      Serial.println("Portrait Up Front");
//      break;
//    case MMA8451_PL_PUB: 
//      Serial.println("Portrait Up Back");
//      break;    
//    case MMA8451_PL_PDF: 
//      Serial.println("Portrait Down Front");
//      break;
//    case MMA8451_PL_PDB: 
//      Serial.println("Portrait Down Back");
//      break;
//    case MMA8451_PL_LRF: 
//      Serial.println("Landscape Right Front");
//      break;
//    case MMA8451_PL_LRB: 
//      Serial.println("Landscape Right Back");
//      break;
//    case MMA8451_PL_LLF: 
//      Serial.println("Landscape Left Front");
//      break;
//    case MMA8451_PL_LLB: 
//      Serial.println("Landscape Left Back");
//      break;
//    }
  
  if(o ==MMA8451_PL_LRF && isDrawn){
    if(!isRaised){
      Serial1.print("raised::");
      Serial1.print(id);
      Serial1.print('\n');
      isRaised = true;
      raisedTime = 0;
    }
  }


  delay(30);
  //Serial.println("loop");
}
void lock(boolean safe){
  if(safe){
    safetyServo.write(7);
    isDrawn = false;
  }else{
    safetyServo.write(100);
    isDrawn = true;
  }
}
void updateOfficerList() {
  //Serial.println("Update");
  delay(30);
  int newListVersion, newListCount;
  String indata = Serial1.readStringUntil(':');
  //indata.remove(indata.length()-1);
  //  Serial1.println(indata);
  boolean failed = false;
  if (indata != NULL) {
  //Serial.println(indata);
    newListVersion = indata.toInt();
    //Serial.println(newListVersion);
    indata = Serial1.readStringUntil(':');
    //indata.remove(indata.length()-1);
    if (indata != NULL) {
      newListCount = indata.toInt();
      //Serial.print(newListCount);
      unsigned long tempNewList[newListCount];
      Serial1.flush();
      Serial1.print("ack:" + indata + ":ready\n");
      //Serial1.flush();

      //      delay(200);
      for (int i = 0; i < newListCount; i++) {
        delay(100);
        while (Serial1.available() < 1) {}
        indata = Serial1.readStringUntil(':');
        if (indata.length() != 10) {
          failed = true;
          break;
        }
        //Serial.println(indata);
        String out = "ack::" + indata + '\n';
        //Serial.println(out);
        Serial1.print(out);
        //Serial1.flush();
        //Serial.println(tempNewList[i]);
        //char buff = new char[indata.length()];
        //indata.toCharArray(buff, indata.length());
        //Serial.print(indata.c_str);
        tempNewList[i] = atol(indata.c_str());
        //Serial.println(tempNewList[i]);
      }
      if (!failed) {
        //Serial.println("good Update");
        officerListVersion = newListVersion;
        officerListCount = newListCount;
        //Serial.print(newListVersion);
        for(int i=0; i< newListCount;i++){
          officerList[i] = tempNewList[i];
          //Serial.println(officerList[i]);
        }
        //delete [] officerList;
        //officerList = new unsigned long[newListCount];
        // Serial.println("ct:");

        //Serial.println(newListCount);
        newListCount = officerListCount;
//        for(int i=0;i<newListCount; i++){
//          officerList[i] = tempNewList[i];
//          Serial.println(officerList[i]);
//        }
        int index = 0;
        EEPROM.put(index, newListVersion);
        index += sizeof(int);
       
        EEPROM.put(index, newListCount);
        index += sizeof(int);
                   //Serial.println("ct:");

        //Serial.println(newListCount);
        for(int i=0; i< newListCount; i++){
           EEPROM.put(index, tempNewList[i]);
           //Serial.println(tempNewList[i]);
           //Serial.println("for");
           index += sizeof(unsigned long);
        }
        //EEPROM.put(index, officerList);
        //saveEEPROM();
      }
    }
  }
}
boolean validateTag(unsigned long id) {
  for (int i = 0; i < officerListCount; i++) {
    if (officerList[i] == id) {
      return true;
    }
  }
  return false;
}
