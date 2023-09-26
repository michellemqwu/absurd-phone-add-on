const int inflatePin = 2;
const int deflatePin = 3;
const int valvePin = 4;

const int redLed = 8;
const int blueLed = 9;

int lastNumOfNotifications = 0;
int currentNumOfNotifications = 0;

enum State {
  IDLE,
  INFLATING,
  DEFLATING,
  WAIT_INFLATE,
  WAIT_DEFLATE
};

State currentState = IDLE;
unsigned long stateChangeMillis = 0;  // When did we enter the current state?
int durationMillis = 0;
int difference = 0;

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

// ----V3--------
void loop() {
  unsigned long currentMillis = millis();

  if (Serial.available() > 0) {
    String inputString = Serial.readStringUntil('\n');
    lastNumOfNotifications = currentNumOfNotifications;
    currentNumOfNotifications = inputString.toInt();
    difference = currentNumOfNotifications - lastNumOfNotifications;

    Serial.print("current: ");
    Serial.println(currentNumOfNotifications);
    Serial.print("last: ");
    Serial.println(lastNumOfNotifications);
    Serial.print("difference: ");
    Serial.println(difference);

    // Update state immediately based on new difference value
    if (difference < 0) {
      currentState = DEFLATING;
      durationMillis = abs(difference) * 1000;
    } else if (difference > 0) {
      currentState = INFLATING;
      durationMillis = difference * 1000;
    }
    stateChangeMillis = currentMillis; // update state change time
  }

  switch (currentState) {
    case IDLE:
      break;

    case INFLATING:
      digitalWrite(blueLed, HIGH);
      digitalWrite(valvePin, HIGH);
      digitalWrite(inflatePin, HIGH);
      Serial.println("inflate start");
      currentState = WAIT_INFLATE;
      break;

    case DEFLATING:
      digitalWrite(redLed, HIGH);
      digitalWrite(valvePin, LOW);
      digitalWrite(deflatePin, HIGH);
      currentState = WAIT_DEFLATE;
      break;

    case WAIT_INFLATE:
      if (currentMillis - stateChangeMillis >= durationMillis) {
        digitalWrite(blueLed, LOW);
        digitalWrite(inflatePin, LOW);
        Serial.println("inflate stopped");
        currentState = IDLE;
      }
      break;

    case WAIT_DEFLATE:
      if (currentMillis - stateChangeMillis >= durationMillis) {
        digitalWrite(redLed, LOW);
        digitalWrite(deflatePin, LOW);
        currentState = IDLE;
      }
      break;
  }
}

// ------WORKING-------
// void loop() {
//   // ------WORKING V1---------
//   if (Serial.available() == 0) {
//     digitalWrite(redLed, LOW);
//     digitalWrite(blueLed, LOW);

//   }

//   else {
//     digitalWrite(redLed, HIGH);
//     digitalWrite(blueLed, HIGH);
//     Serial.println("HI");
//     delay(1000);
//     digitalWrite(redLed, LOW);
//     digitalWrite(blueLed, LOW);
//     Serial.println("HERE");
//     delay(1000);

//     String inputString = Serial.readStringUntil('\n');

//     lastNumOfNotifications = currentNumOfNotifications;
//     currentNumOfNotifications = inputString.toInt();
//     Serial.print("current: ");
//     Serial.println(currentNumOfNotifications);
//     Serial.print("last: ");
//     Serial.println(lastNumOfNotifications);

//     int difference = currentNumOfNotifications - lastNumOfNotifications;
//     Serial.print("difference: ");
//     Serial.println(difference);

//     if (difference < 0) {

//       for (int i = 0; i < 0 - difference; i++) {
//         digitalWrite(redLed, HIGH);
//         delay(500);
//         digitalWrite(redLed, LOW);
//       }

//       digitalWrite(valvePin, LOW);
//       digitalWrite(deflatePin, HIGH);
//       delay(abs(difference) * 1000);
//       digitalWrite(deflatePin, LOW);

//     } else if (difference > 0) {

//       for (int i = 0; i < difference, i++;) {
//         digitalWrite(blueLed, HIGH);
//         delay(500);
//         digitalWrite(blueLed, LOW);
//       }

//       digitalWrite(valvePin, HIGH);
//       Serial.println("valve open");
//       digitalWrite(inflatePin, HIGH);
//       Serial.println("inflate start");
//       delay(difference * 1000);
//       Serial.println("inflate stopped");
//       digitalWrite(inflatePin, LOW);

//     }
//   }

//   // //-------FOR PUMP TESTING---------
//   // Close the valve and inflate for 5 seconds
//   // digitalWrite(valvePin, HIGH);
//   // Serial.println("inflate start");
//   // digitalWrite(inflatePin, HIGH);
//   // delay(5000);
//   // Serial.println("inflate stopped");
//   // digitalWrite(inflatePin, LOW);

//   // delay(1000);  // Delay for 1 second before starting deflation

//   // // Open the valve and deflate for 5 seconds
//   // digitalWrite(valvePin, LOW);
//   // digitalWrite(deflatePin, HIGH);
//   // delay(5000);
//   // digitalWrite(deflatePin, LOW);

//   // delay(1000);  // Delay for 1 second before repeating
// }
//----------------------

//----------V2-------------
// void loop() {
//   unsigned long currentMillis = millis();

//   // Check for available Serial data
//   if (Serial.available() > 0) {
//     String inputString = Serial.readStringUntil('\n');
//     lastNumOfNotifications = currentNumOfNotifications;
//     currentNumOfNotifications = inputString.toInt();
//     difference = currentNumOfNotifications - lastNumOfNotifications;

//     Serial.print("current: ");
//     Serial.println(currentNumOfNotifications);
//     Serial.print("last: ");
//     Serial.println(lastNumOfNotifications);
//     Serial.print("difference: ");
//     Serial.println(difference);
//   }

//   switch (currentState) {
//     case IDLE:
//       if (difference < 0) {
//         currentState = DEFLATING;
//         durationMillis = abs(difference) * 1000;  // Save the duration here
//         stateChangeMillis = currentMillis;
//       } else if (difference > 0) {
//         currentState = INFLATING;
//         durationMillis = difference * 1000;  // Save the duration here
//         stateChangeMillis = currentMillis;
//       }
//       break;

//     case INFLATING:
//       digitalWrite(blueLed, HIGH);
//       digitalWrite(valvePin, HIGH);
//       digitalWrite(inflatePin, HIGH);
//       Serial.println("inflate start");
//       currentState = WAIT_INFLATE;
//       break;

//     case DEFLATING:
//       digitalWrite(redLed, HIGH);
//       digitalWrite(valvePin, LOW);
//       digitalWrite(deflatePin, HIGH);
//       currentState = WAIT_DEFLATE;
//       break;

//     case WAIT_INFLATE:
//       if (currentMillis - stateChangeMillis > durationMillis) {
//         digitalWrite(blueLed, LOW);
//         digitalWrite(inflatePin, LOW);
//         Serial.println("inflate stopped");
//         difference = 0;
//         currentState = IDLE;
//       }
//       break;

//     case WAIT_DEFLATE:
//       if (currentMillis - stateChangeMillis > durationMillis) {
//         digitalWrite(redLed, LOW);
//         digitalWrite(deflatePin, LOW);
//         difference = 0;
//         currentState = IDLE;
//       }
//       break;
//   }
// }
//--------------------------

