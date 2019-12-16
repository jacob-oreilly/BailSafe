## Import GPIO Library
import RPi.GPIO as GPIO


## Setup Pins
GPIO.setmode(GPIO.BOARD)
inputPin = 7
GPIO.setup(inputPin, GPIO.IN)

##  Need try and finally so if the program crashes
try:
    
    while True:
        if GPIO.input(inputPin)== 0:
            print("No Rider")
        else:
            print("On Seat")

finally:
    ## Need to close pins
    GPIO.cleanup()
    
    
    