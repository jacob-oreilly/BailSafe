# bailSafe
BailSafe is an app for keeping you safe out on the trail. The app is a paired with a switch that connects to your seat. 
The switch detects when you are sitting on the seat or off the seat. If you have fallen the switch sends a signal to the
app and based at how long you have been off the bike it will send a text to your emergency contact with your location
and saying that you have fallen.

# Set Up:
**Setting up the switch:**
For setting up the switch you will need to a Rasbperry Pi, bread board, wires to connect the Raspberry Pi to the breadboard
and a switch to connect to the bread board.

Once you have your Pi you will need to install Raspbian onto it.

You will need to figure out the correct circuits for setting up the Raspberry Pi with the bread board and switch.

Then clone the project and take the contents of the BailSafe_BackEnd folder and add the final.py file to your homescreen. 
Now if you want to the script to run on boot you will have to configure your bash script to run the file on boot. 

You also need to install PyBluez onto the Pi.

You should be all set. Now for the App

**Setting up the App:**

You can install the app on an Android device through Android Studio. It's still in developement so it's best to 
build it from Android Studio unofficially signed. 

Once the app is install on your device you will need to go into your Bluetooth settings and add the Pi to your phones connected
devices. 

Note: The first time connecting you may need to accept the connection on both the device and Raspberry Pi. You might also
need to make the Raspberry Pi Discoverable.

Now open the app and it will ask if you want to allow network and bluetooth access to the app.

Once allowed you will go to the top right menu and click Bluetooth. From there select the Raspberry Pi's IP address from the
list. This will start the connection. 

Now if you want to start a ride, just click start ride. Once the ride is started the timer will be set and will start
counting down unless you are on the switch. Once the switch is released and it counts down to zero it will send
the emergency text of your location.

You can click the end ride button if you don't want to send a text if you are off the bike.

That's it! Now go have fun and ride, and "remeber when you bail choose BailSafe".


