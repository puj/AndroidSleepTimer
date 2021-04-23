# AndroidSleepTimer

Do you like falling asleep while watching streams? Tired of Netflix, Twitch, YouTube waking you up at night?  

![Screenshot of main screen](https://github.com/puj/AndroidSleepTimer/blob/master/screenshots/Main.png?raw=true)

This simple app allows you to specify a time of day (or night).  At that time of day, the app will:  
- Dim your screen  
- Mute your sound  
- Lock your device 
- Disables "Stay Awake while plugged in" 

The app currently does this at the specified time every 24-hours.   
![Screenshot of main screen](https://github.com/puj/AndroidSleepTimer/blob/master/screenshots/SetTime.png?raw=true) 

## Usage
1. Press the Edit button
2. Set a time
3. Now your device will sleep and stop playing media at that time every day

At 8AM, the device will restore brightness, unmute, and enable "Stay Awake while plugged in".    
The undo button in the app also does this.  

## Limitations/Improvements:
- Cannot set specific wake up time (only 8AM right now)
- Does not restore brightness level (set to .7)
- Does not restore "Stay Awake while plugged in", only turns it on when waking

## Why does the App need Admin permissions?  
In order to lock the screen.  This allows us to override WakeLocked applications from playing media.  

## Why does the App need System Setting permissions?  
In order to read/write the "Stay awake when plugged in" value  
