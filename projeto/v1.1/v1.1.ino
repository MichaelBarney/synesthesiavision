/*
-----LOCALIZADOR POR AUDIO 3D-----------------
-----V1.1-------------------------------------
-----EDITADO POR MICHAEL BARNEY------------
-----ORDEM dos pinos alteradas, aparfeiçoamento da variabilidade do numero de sensores------------
*/

/*
diagonal esquerda
trigPin 2
echoPin 3

frente
trigPin 4
echoPin 5

diagonal direita
trigPin 6
echoPin 7
*/


#define N 20
#define G 3
#define D 500
//n = number of values
//g = number of sensors

int sensors[G][N];
String alph = "abcdefghijklmnopqrstuvwxyz";

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
  //  0 = esquerda || 1 = frente || 2 = direita
  for(int i = 0; i < G; i++){
    Serial.println(alph.charAt(i));
    Serial.println(getDistance(i));
    //Serial.println("hello");
  }
  //Serial.println('a');
  //Serial.println (getDistance(1));
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
