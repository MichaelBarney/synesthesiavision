/*
-----LOCALIZADOR POR AUDIO 3D-----------------
-----V1.3-------------------------------------
-----EDITADO POR MICHAEL BARNEY--------------
-----3 Sensores------------------------------

*/



#define N 10
#define G 5

//n = number of values
//g = number of sensors
//d = delay

int sensors[G][N];
String alph = "abcdefghijklmnopqrstuvwxyz";
String received = "";

void setup() {
  //Begin Serial
  Serial.begin (9600);
  
  //set pins for sensors
  for(int x = 2; x <= (1 + G*2); x++){
    //if odd number then set it as a trigger to outpput
    if(x % 2){
      pinMode(x, INPUT);
    }
    //else, set it as a echo pin to input
    else{
      pinMode(x, OUTPUT);
    }
  }
}



void loop() {

  if(Serial.available()) {
    while(Serial.available()) {
      received += (char) Serial.read();
    }
  }
  
  for(int i = 0; i < G; i++){
     if(i == 0 ||i == 2 || i == 4){
        Serial.print(alph.charAt(i));
        Serial.println(getDistance(i));
     }
  }
}

void parseReadBuffer() {
  
  // Find the first delimiter in the buffer
  int inx = received.indexOf(DELIMITER);
  
  // If there is none, exit
  if (inx == -1) return;
  
  // Get the complete message, minus the delimiter
  String s = received.substring(0, inx);
  
  // Remove the message from the buffer
  received = received.substring(inx + 1);
  
  // Process the message
  gotMessage(s);
  
  // Look for more complete messages
  parseReadBuffer();
}


void gotMessage(String message) {
  
  Serial.println("[RECV]: " + message);
  
  if(message == "CONNECTED") {
    startCycle = true;
  }
}


long getDistance(int s){
  //int n = s - 1;
  //get the pins
  int t = 2 + 2*s;
  int e = t + 1;

  //get duration and distance
  long duration, distance;
  digitalWrite(t, LOW);
  delayMicroseconds(2); 
  digitalWrite(t, HIGH);
  delayMicroseconds(10); 
  digitalWrite(t, LOW);
  duration = pulseIn(e, HIGH);
  distance = (duration/2) / 29.1;
  
  //filter
  long filtered;  
  for(int i = N-1; i > 0; i--){
    sensors[s][i] = sensors[s][i - 1];
  }
  sensors[s][0] = distance;
  
  long sum = 0;
  for(int i = 0; i < N; i++){
    sum += sensors[s][i];
  }
  filtered = sum/N;
  
  return filtered;
}
