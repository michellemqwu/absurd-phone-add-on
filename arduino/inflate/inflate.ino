const int inflatePin = 2;
const int deflatePin = 3;
const int valvePin = 4;

const int redLed = 8;
const int blueLed = 9;

int lastNumOfNotifications = 0;
int currentNumOfNotifications = 0;

char notifications[50];
int index = 0;
int numOfNotifications;

char buffer[10];

void setup() {
  Serial.begin(9600);

  pinMode(inflatePin, OUTPUT);
  pinMode(deflatePin, OUTPUT);
  pinMode(valvePin, OUTPUT);

  //debug
  pinMode(redLed, OUTPUT);
  pinMode(blueLed, OUTPUT);

  // Initialize everything to off state
  digitalWrite(valvePin, LOW);
  digitalWrite(inflatePin, LOW);
  digitalWrite(deflatePin, LOW);
}

void loop() {
  if (Serial.available() == 0) {
    digitalWrite(redLed, LOW);
    digitalWrite(blueLed, LOW);
    
  }

  else {
    digitalWrite(redLed, HIGH);
    digitalWrite(blueLed, HIGH);
    Serial.println("HI");
    delay(1000);
    digitalWrite(redLed, LOW);
    digitalWrite(blueLed, LOW);
    Serial.println("HERE");
    delay(1000);

    String inputString = Serial.readStringUntil('\n');
    
    lastNumOfNotifications = currentNumOfNotifications;
    currentNumOfNotifications = inputString.toInt();
    Serial.print("current: ");
    Serial.println(currentNumOfNotifications);
    Serial.print("last: ");
    Serial.println(lastNumOfNotifications);

    int difference = currentNumOfNotifications - lastNumOfNotifications;
    Serial.print("difference: ");
    Serial.println(difference);

    if (difference < 0) {

      for (int i = 0; i < 0 - difference; i++) {
        digitalWrite(redLed, HIGH);
        delay(500);
        digitalWrite(redLed, LOW);
      }

      digitalWrite(valvePin, LOW);
      digitalWrite(deflatePin, HIGH);
      delay(abs(difference) * 1000);
      digitalWrite(deflatePin, LOW);

    } else if (difference > 0) {

      for (int i = 0; i < difference, i++;) {
        digitalWrite(blueLed, HIGH);
        delay(500);
        digitalWrite(blueLed, LOW);
      }

      digitalWrite(valvePin, HIGH);
      Serial.println("valve open");
      digitalWrite(inflatePin, HIGH);
      Serial.println("inflate start");
      delay(difference * 1000);
      Serial.println("inflate stopped");
      digitalWrite(inflatePin, LOW);

    }
  }


  // // Close the valve and inflate for 5 seconds
  // digitalWrite(valvePin, HIGH);
  // Serial.println("inflate start");
  // digitalWrite(inflatePin, HIGH);
  // delay(5000);
  // Serial.println("inflate stopped");
  // digitalWrite(inflatePin, LOW);

  // delay(1000);  // Delay for 1 second before starting deflation

  // // Open the valve and deflate for 5 seconds
  // digitalWrite(valvePin, LOW);
  // digitalWrite(deflatePin, HIGH);
  // delay(5000);
  // digitalWrite(deflatePin, LOW);

  // delay(1000);  // Delay for 1 second before repeating
}
