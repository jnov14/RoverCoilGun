#include <Servo.h> //Pins and variables for the motor driver driving the //coil adjustment and turntable

const int l298n[4] = {43, 41, 39, 37};
const int nPow[2] = {2, 3};
volatile byte turntableSpeed = 0;
volatile byte coilSpeed = 0; //Pins and variables for the motor shield driving the tracks
const int motorShield[4] = {10, 11, 12, 13};
volatile byte leftSpeed = 0;
volatile byte rightSpeed = 0; //Charge and fire relay pins
const int charge = 48;
const int fire = 49; //Servo pin and setup
const int trajPin = 9;
Servo angle;
volatile int trajData = 1000; //Projectile loaded checking pin
const int projChk = A0;
volatile int projVal; //Relay checking pins
const int resetChkPow = 47;
const int resetChk = 46;
const int resetChkInd = 29;        //Instruction input variables
volatile int instruction = 0x3000; //No Op instruction
volatile byte input = 0x60;
volatile byte half1;
volatile byte half0;
volatile int inputGet = 0;
volatile int byteSide;

void setup(void)
{
    for (int i = 0; i < 4; i++)
        pinMode(l298n[i], OUTPUT);
    for (int i = 0; i < 2; i++)
        pinMode(motorShield[i + 2], OUTPUT);
    pinMode(charge, OUTPUT);
    pinMode(fire, OUTPUT);
    digitalWrite(fire, HIGH);
    angle.attach(trajPin);
    pinMode(resetChkPow, OUTPUT);
    pinMode(resetChk, INPUT);
    pinMode(resetChkInd, OUTPUT);
    delay(50);
    Serial.begin(9600);
} //Function that handles the charge and fire relays
void coilGun(int code)
{
    switch (code)
    {
    case 0x0:
        digitalWrite(charge, HIGH);
        break;
    case 0x1:
        digitalWrite(charge, LOW);
        break;
    case 0x2:
        digitalWrite(fire, LOW);
        break;
    case 0x3:
        digitalWrite(fire, HIGH);
        break;
    case 0x5:
        inputGet = 0;
        break;
    default:
        digitalWrite(charge, LOW);
        digitalWrite(fire, HIGH);
    }
} //Function which drives the left motor
void left(int dir)
{
    if (dir == 1)
        digitalWrite(motorShield[3], HIGH);
    else
        digitalWrite(motorShield[3], LOW);
    analogWrite(motorShield[1], leftSpeed);
} //Function which drives the right motor
void right(int dir)
{
    if (dir == 1)
        digitalWrite(motorShield[2], HIGH);
    else
        digitalWrite(motorShield[2], LOW);
    analogWrite(motorShield[0], rightSpeed);
} //Base function for driving the track motors
void wheelDriver(int code, int data)
{
    if ((code & 0x2) == 0x0)
    {
        leftSpeed = data;
        if ((code & 0x1) == 0x1)
            left(1);
        else
            left(0);
    }
    else
    {
        rightSpeed = data;
        if ((code & 0x1) == 0x1)
            right(1);
        else
            right(0);
    }
} //Function which drives the turntable motor
void turntable(int dir)
{
    if (dir == 1)
    {
        digitalWrite(l298n[0], HIGH);
        digitalWrite(l298n[1], LOW);
    }
    else
    {
        digitalWrite(l298n[0], LOW);
        digitalWrite(l298n[1], HIGH);
    }
    analogWrite(nPow[0], turntableSpeed);
} //Function which drives the coil adjustment motor
void coil(int dir)
{
    if (dir == 1)
    {
        digitalWrite(l298n[2], HIGH);
        digitalWrite(l298n[3], LOW);
    }
    else
    {
        digitalWrite(l298n[2], LOW);
        digitalWrite(l298n[3], HIGH);
    }
    analogWrite(nPow[1], coilSpeed);
} //Base driver function for the turntable and coil adjustment motors
void l298nDriver(int code, int data)
{
    if ((code & 0x2) == 0x2)
    {
        coilSpeed = data;
        if ((code & 0x1) == 0x1)
            coil(1);
        else
            coil(0);
    }
    else
    {
        turntableSpeed = data;
        if ((code & 0x1) == 0x1)
            turntable(1);
        else
            turntable(0);
    }
} //Function to change the angle of fire of the barrel
void changeTrajectory(int data)
{
    trajData = data + 500;
    angle.writeMicroseconds(trajData);
} //Function to check if the fire relay has fused upon discharging //the capacitors
void resetCheck()
{
    digitalWrite(resetChkPow, HIGH);
    if (!digitalRead(resetChk))
    {
        digitalWrite(resetChkInd, HIGH);
        Serial.println("Fix Relay!");
    }
    else
    {
        digitalWrite(resetChkInd, LOW);
        Serial.println("Relay OK!");
    }
    digitalWrite(resetChkPow, LOW);
} //Function to check if a projectile is loaded in the barrel
void checkProj(void)
{
    projVal = analogRead(projChk);
    delay(10);
    Serial.println(projVal);
    projVal = 0;
}
//Outtermost section of command processing, sends the code //to each respective hardware handler based on the  //instruction given
void decode(int code, int data)
{
    switch (code)
    {
    case 0x0:
    case 0x1:
    case 0x2:
    case 0x3:
        coilGun(code);
        break;
    case 0x4:
    case 0x5:
    case 0x6:
    case 0x7:
        wheelDriver(code, data);
        break;
    case 0x8:
    case 0x9:
    case 0xa:
    case 0xb:
        l298nDriver(code, data);
        break;
    case 0xd:
        resetCheck();
        break;
    case 0xe:
        checkProj();
        break;
    case 0xf:
        changeTrajectory(data);
        break;
    default:
        break;
    }
}

//Splits the instruction up into the command and data //to send to be processed
void instruct(void)
{
    int code = ((instruction & 0x3c00) >> 10);
    int data = instruction & 0x03ff;
    decode(code, data);
}

//Combines input halves from the serial into the command that
//the mega works with

void instructionCombine(void)
{
    instruction = instruction & 0x3f80;
    instruction = instruction | half1;
    instruction = instruction & 0x007f;
    instruction = instruction | (half0 << 7);
}

void loop(void) {}

//Gets the two bytes of the incoming instruction, on interrupt, and turns them into the instruction that is used by the controller array

void serialEvent(void)
{
    inputGet += 2;
    input = Serial.read();
    if (inputGet == 4)
    {
        if ((input & 0x80) == 0x80)
        {
            if (byteSide == 0)
            {
                half1 = input;
                instructionCombine();
                instruct();
                inputGet = 0;
            }
        }
        else if (byteSide == 1)
        {
            half0 = input;
            instructionCombine();
            instruct();
            inputGet = 0;
        }
    }
    else
    {
        if ((input & 0x80) == 0x80)
        {
            byteSide = 1;
            half1 = input;
        }
        else
        {
            byteSide = 0;
            half0 = input;
        }
    }
}
}
