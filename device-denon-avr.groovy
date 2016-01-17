/**
 *  Denon Network Receiver
 *     Works on Network Receivers newer than 2012
 *    SmartThings driver to connect your Denon Network Receiver to SmartThings
 *
 */

preferences {
	input("destIp", "text", title: "IP", description: "The device IP")
    input("destPort", "number", title: "Port", description: "The port you wish to connect", defaultValue: 80)
}
 

metadata {
	definition (name: "Denon Network Receiver", namespace: "KristopherKubicki", 
    	author: "kristopher@acm.org") {
        capability "Actuator"
        capability "Switch" 
        capability "Polling"
        capability "Music Player"
        
        attribute "input", "string"
        attribute "inputChan", "enum"
        
        command "inputSelect"
        command "inputNext"
        
      	}

	simulator {
		// TODO-: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics16"
            state "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics16"
        }
		standardTile("poll", "device.poll", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
		}
        standardTile("input", "device.input", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "input", label: '${currentValue}', action: "inputNext", icon: "", backgroundColor: "#FFFFFF"
		}
        standardTile("mute", "device.mute", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "muted", label: '${name}', action:"unmute", backgroundColor: "#79b821", icon:"st.Electronics.electronics13"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: "#ffffff", icon:"st.Electronics.electronics13"
		}
        controlTile("level", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range: "(-80..10)") {
			state "level", label: '${name}', action:"setLevel"
		}
        
		main "switch"
        details(["switch","input","mute","level","poll"])
	}
}



def parse(String description) {
	log.debug "Parsing '${description}'"
    
 	def map = stringToMap(description)

    
    if(!map.body || map.body == "DQo=") { return }
        log.debug "${map.body} "
	def body = new String(map.body.decodeBase64())
    
	def statusrsp = new XmlSlurper().parseText(body)
	def power = statusrsp.Power.value.text()
    if(power == "ON") { 
    	sendEvent(name: "switch", value: 'on')
    }
    if(power != "" && power != "ON") { 
    	sendEvent(name: "switch", value: 'off')
    }
    

    def muteLevel = statusrsp.Mute.value.text()
    if(muteLevel == "on") { 
    	sendEvent(name: "mute", value: 'muted')
	}
    if(muteLevel != "" && muteLevel != "on") {
	    sendEvent(name: "mute", value: 'unmuted')
    }
    
	def inputCanonical = statusrsp.InputFuncSelect.value.text()
    def inputTmp = []
 	statusrsp.VideoSelectLists.value.each {
    	if(it.@index != "ON" && it.@index != "OFF") {
            inputTmp.push(it.'@index')
            if(it.toString().trim() == inputCanonical) {     
            	sendEvent(name: "input", value: it.'@index')
            }
        }
    }
    sendEvent(name: "inputChan", value: inputTmp)
    
    if(statusrsp.MasterVolume.value.text()) { 
    	def float volLevel = statusrsp.MasterVolume.value.toFloat() ?: -40.0
        log.debug "VOL: $volLevel" 
   		def int curLevel = -40.0
        try {
        	curLevel = device.currentValue("level")
        } catch(NumberFormatException nfe) { 
        	curLevel = -40.0
        }
        if(curLevel != volLevel) {
    		sendEvent(name: "level", value: volLevel)
        }
    } 
}


def setLevel(val) {
	sendEvent(name: "mute", value: "unmuted")     
    sendEvent(name: "level", value: val)
    
    request("cmd0=PutMasterVolumeSet%2F$val")
}

def on() {
	sendEvent(name: "switch", value: 'on')
	request('cmd0=PutZone_OnOff%2FON')
}

def off() { 
	sendEvent(name: "switch", value: 'off')
	request('cmd0=PutZone_OnOff%2FOFF')
}

def mute() { 
	sendEvent(name: "mute", value: "muted")
	request('cmd0=PutVolumeMute%2FON')
}

def unmute() { 
	sendEvent(name: "mute", value: "unmuted")
	request('cmd0=PutVolumeMute%2FOFF')
}

def inputNext() { 

	def cur = device.currentValue("input")   
    def selectedInputs = device.currentValue("inputChan").substring(1,device.currentValue("inputChan").length()-1).split(', ').collect{it}
    selectedInputs.push(selectedInputs[0])
//    log.debug "SELECTED: $selectedInputs"
    
    def semaphore = 0
    for(selectedInput in selectedInputs) {
    	if(semaphore == 1) { 
//        	log.debug "SELECT: ($semaphore) '$selectedInput'"
        	return inputSelect(selectedInput)
        }
    	if(cur == selectedInput) { 
        	semaphore = 1
        }
    }
}


def inputSelect(channel) {
 	sendEvent(name: "input", value: channel	)
	request("cmd0=PutZone_InputFunction%2F$channel")
}

def poll() { 
	refresh()
}

def refresh() {

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'GET',
    		'path': "/goform/formMainZone_MainZoneXml.xml",
            'headers': [ HOST: "$destIp:$destPort" ] 
		) 
        
    hubAction
}

def request(body) { 

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'POST',
    		'path': "/MainZone/index.put.asp",
        	'body': body,
        	'headers': [ HOST: "$destIp:$destPort" ]
		) 
              
    hubAction
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}
