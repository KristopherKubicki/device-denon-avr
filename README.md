# Denon Network AVR Device Type
This is a networked receiver device type for SmartThings.  It allows you to change the input, adjust the volume, mute and turn off/standby your receiver via a SmartApp or the SmartThings UI.

<img src='http://content.abt.com/image.php/3_AVRS910W.jpg?image=/images/products/BDP_Images/3_AVRS910W.jpg&canvas=1&quality=100&min_w=450&min_h=320&ck=371'>

This device type is based off the <a href='https://github.com/KristopherKubicki/device-yamaha-rx'>Yamaha receiver device</a> I published a few months ago. I also suspect it could be easily modified for Marantz and some other recievers. 

I created a command class for all AV Tuners for SmartThings called "inputSelect".  To use it, just specify the name of the input you wish to tune to.  For example, if you want to tune to "Media Player", you would just send inputSelect("MPLAY") via your SmartApp.

The best way to interact with the receiver is to create <a href='https://community.smartthings.com/t/virtual-device-manager-create-virtual-devices-without-the-ide/27472'>a virtual momentary tile</a> for each input.  For example, I have one named "Nexus Player" that triggers the inputSelect('MPLAY') command via <a href='https://github.com/KristopherKubicki/device-denon-avr/blob/master/smartapp-button-to-av.groovy'>a sample SmartApp</a> I included in this repo.   Advanced users could also replicate this behavior via <a href='https://community.smartthings.com/t/release-rule-machine/28730'>Rule Machine<a>. 

##License 
Copyright (c) 2016, Kristopher Kubicki
All rights reserved.
