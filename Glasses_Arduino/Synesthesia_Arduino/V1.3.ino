/*
Copyright (C) 2017  Synesthesia Vision

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#define N 10
#define G 3


//n = number of values
//g = number of sensors

int sensors[G][N];       //array that stores the sensors values
String alph = "abcdefghijklmnopqrstuvwxyz"; //alphabetical to identify sensor and the distance
String bluetooth_tx_buffer = "";  //buffer used to send the data

const byte primaryButton = 2;

// Variables will change:
int ledState = HIGH;         // the current state of the output pin
int buttonState;             // the current reading from the input pin
int lastButtonState = LOW;   // the previous reading from the input pin

// the following variables are unsigned long's because the time, measured in miliseconds,
// will quickly become a bigger number than can be stored in an int.
unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 50;    // the debounce time; increase if the output flickers

char DELIMITER = '\n';  //added in the final string before its send

void setup() {
  //Begin Serial
  Serial.begin (9600);
  
  //set pins for sensors
  for(int x = 4; x <= (G*3); x++){
    //if value is pair, so set to input
    if(x % 2){
      pinMode(x, INPUT);
    }
    //else set it to output
    else{
      pinMode(x, OUTPUT);
    }
  }

  //Set the pins 2 and 3 to HIGH state internally
  pinMode(primaryButton, INPUT_PULLUP);
  //pinMode(secondaryButton, INPUT_PULLUP);
}

void loop() {
  
  //Sets the first pin (5) for activate the trigger
  for(int i = 0; i < G; i++){
      Serial.print(alph.charAt(i));
      Serial.println(getDistance(i));
  }
  //check if the weather forecast button is pressioned, if true send a forecast requisition to smartphone
  if(checkButtonState(primaryButton)) Serial.println("getweather");
  delay(1000);
}


long getDistance(int s){
  //int n = s - 1;
  //get the pins
  int t = 4 + 2*s;
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

int checkButtonState(int button){
  //Source: Arduino, modified by: KilnerJhow
  // read the state of the switch into a local variable:
  int reading = digitalRead(button);

  // check to see if you just pressed the button
  // (i.e. the input went from LOW to HIGH),  and you've waited
  // long enough since the last press to ignore any noise:

  // If the switch changed, due to noise or pressing:
  if (reading != lastButtonState) {
    // reset the debouncing timer
    lastDebounceTime = millis();
  }

  if ((millis() - lastDebounceTime) > debounceDelay) {
    // whatever the reading is at, it's been there for longer
    // than the debounce delay, so take it as the actual current state:

    // if the button state has changed:
    if (reading != buttonState) {
      buttonState = reading;

      /// return 1 if the state of button has changed, 0 else not
      if (buttonState == LOW) {
        
        // save the reading.  Next time through the loop,
        // it'll be the lastButtonState:
        lastButtonState = reading;
        return 1;
      }
    }
  }
  // save the reading.  Next time through the loop,
  // it'll be the lastButtonState:
  lastButtonState = reading;
  return 0;
}
/*
void turnOn(){
  delay(500);
  Serial.println("turnon");
}*/

