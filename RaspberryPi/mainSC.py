import avrduder  
import os  
import re  
import serial  
import subprocess  
import string  
import struct  
import time  
try: 
    subprocess.check_output(["pidof", "rfcomm"]) 
except: 
    subprocess.Popen(["sudo", "rfcomm", "watch", "hci0"]) 
    avrduder.uploadtoBoard("controller", "atmega2560")

def sendByte(port, num): 
    out = ''  
	out = struct.pack('!B', num) port.write(out) 

def sendInstruction(port, instruction): sendByte(port, ((instruction & 0xff00) >> 8)) sendByte(port, (instruction & 0xff)) 
def combineComAndDat(com, dat): instruction = com << 0xB instruction = instruction | 0x0080 instruction = instruction | ((dat & 0x0380) << 1) instruction = instruction | (dat & 0x007f) return instruction 
def getCommand(instruction): return (instruction & 0x7800) >> 0xB 
def getData(instruction): return ((instruction & 0x0700) >> 1) | (instruction & 0x0078)
	
# Control variables 
charging = False 
chargeStopTime =  ambientLight = 
# Serial devices

def wait(dev): 
    while True: 
        if os.path.exists(dev): 
            break 
        time.sleep(.1) wait("/dev/ttyS0") 
        arduino = serial.Serial("/dev/ttyS0", baudrate = 9600, timeout = .02) 
        print("Connected Arduino!") wait("/dev/rfcomm0") 
        app = serial.Serial("/dev/rfcomm0", baudrate = 9600, timeout = .01) 
        print("Connected Phone!") 
        time.sleep(1)

#Commands to the mega charge = 0x0 uncharge = 0x1 discharge = 0x2 undischarge = 0x3 leftrev = 0x4 leftfor = 0x5 rightrev = 0x6 rightfor = 0x7 turntablewid = 0x8 turntablesun = 0x9 powldec = 0xA powlinc = 0xB nocom = 0xC relaychk = 0xD projchk = 0xE trajectory = 0xF
# Command functions, direct

def chargeStart(): instruction = combineComAndDat(charge, 0) sendInstruction(arduino, instruction) 
def chargeStop(): instruction = combineComAndDat(uncharge, 0) sendInstruction(arduino, instruction) 
def dischargeStart(): instruction = combineComAndDat(discharge, 0) sendInstruction(arduino, instruction) 
def dischargeStop(): instruction = combineComAndDat(undischarge, 0) sendInstruction(arduino, instruction) 
def leftMotorRev(data): instruction = combineComAndDat(leftrev, data) sendInstruction(arduino, instruction) 
def leftMotorFor(data): instruction = combineComAndDat(leftfor, data) sendInstruction(arduino, instruction) 
def rightMotorRev(data): instruction = combineComAndDat(rightrev, data) sendInstruction(arduino, instruction) 
def rightMotorFor(data): instruction = combineComAndDat(rightfor, data) sendInstruction(arduino, instruction) 
def turntableWiddershins(data): instruction = combineComAndDat(turntablewid, data) sendInstruction(arduino, instruction) 
def turntableSunwise(data): instruction = combineComAndDat(turntablesun, data) sendInstruction(arduino, instruction) 
def powerLevDecrease(data): instruction = combineComAndDat(powldec, data) sendInstruction(arduino, instruction) 
def powerLevIncrease(data): instruction = combineComAndDat(powlinc, data) sendInstruction(arduino, instruction) 
def noCommand(): instruction = combineComAndDat(nocom, 0) sendInstruction(arduino, instruction) 
def relayCheck(): instruction = combineComAndDat(relaychk, 0) sendInstruction(arduino, instruction) return arduino.readline() 
def projectileCheck(): instruction = combineComAndDat(projchk, 0) sendInstruction(arduino, instruction) return int(re.search(r '\d+', arduino.readline()).group()) 
def trajectoryChange(data): instruction = combineComAndDat(trajectory, data) sendInstruction(arduino, instruction)

# Higher level control functions 
def chargeFor(dur): 
    chargeStart() 
    global chargeStopTime 
    global charging currentTime = time.time() 
    noCommand() 
    charging = True 
    chargeStopTime = currentTime + dur

def charge60(): 
    chargeStart() 
    global chargeStopTime 
    global charging 
    currentTime = time.time() 
    noCommand() 
    charging = True 
    chargeStopTime = currentTime +  

def stopCharge(): 
    chargeStop() 
    noCommand() 
def dischargeCapacitor(): 
    dischargeStart() 
    time.sleep(.1) 
    dischargeStop() 
    noCommand() 
    
def fire(): 
    check = int(projectileCheck()) 
    global ambientLight 
    print(ambientLight) 
    print(check) 
    
    if check > (ambientLight + 50): 
        dischargeStart() 
        time.sleep(.1) 
        dischargeStop() 
        ambientLight = int(projectileCheck()) 
        noCommand()  

	else :
        print("Projectile not loaded")
	    app.write('Projectile not loaded!') 
	
def leftMotor(speed): 
    if speed < 0: 
        speed = -speed 
        leftMotorRev(speed)  
	else :
        leftMotorFor(speed) noCommand() 

def stopLeftM(): 
    leftMotorRev(0) 
    noCommand() 

def rightMotor(speed): 
    if speed < 0: 
        speed = -speed 
        rightMotorRev(speed)  
	    
    else :rightMotorFor(speed) noCommand() 
        
def stopRightM(): 
    rightMotorRev(0) 
    noCommand() 

def turntable(speed): 
    if speed < 0:
        speed = -speed 
        turntableWiddershins(speed)  
	    
    else :
        turntableSunwise(speed) 
        
def powerLevel(speed): 
    if speed < 0: 
        speed = -speed 
        powerLevDecrease(speed)  
	
    else:
        powerLevIncrease(speed) 
        
def changeTrajectory(angle): 
    angle = angle /  * 1024 - 1 
    angle = int(angle) 
    if angle < 0: 
        angle = 0 
        trajectoryChange(angle) 
        noCommand()
        app.write("Make sure there is no projectile loaded.".encode('utf-8')) 

# Main function	# 
def main(): 
    global ambientLight 
    global charging 
    ambientLight = 600 
    noCommand()

	#app.write("Connected!") instruction = 0x5880  
	
    while True: 
        if charging: 
            if time.time() >= chargeStopTime: 
                stopCharge() 
                charging = False 
                print("Charge stopped!") 
                instruction = 0x5880  
	
        try: 
            received = app.read(4) 
            instruction = int(received, 16) 
            print(instruction, 16) 
            app.write(str(instruction)) 
        except: 
            time.sleep(.001)
	#continue
    #print("nothing received")
    #print(instruction)
    #instruction = int(re.search(r '\d+', received).group())
    # #app.write(str(instruction)) 
    command = getCommand(instruction) 
    data = getData(instruction)
    #print(command)
    # #print(data) 

	if command == charge: 
        print("Charging!") 
        charge60() 
    elif command == discharge: 
        fire() 
    elif command == leftrev: 
        data = data - 255 
        leftMotor(data)
    elif command == rightrev: 
        data = data - 255 
        rightMotor(data) 
    elif command == turntablewid: 
        data = data - 255 
        turntable(data) 
    elif command == powldec: 
        data = data - 255 
            powerLevel(data) 
    elif command == relaychk: 
        app.write(relayCheck())
     elif command == trajectory: 
         trajectoryChange(1023 - data) 
         main()  
