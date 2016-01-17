/**
 *  Button to AV Mode
 *   
 */

definition(
	name: "Button to AV Mode",
	namespace: "KristopherKubicki",
	author: "kristopher@acm.org",
	description: "When a momentary tile is pushed, send inputSelect() to a receiver",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/remote.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/remote@2x.png"
)

preferences {
	section("Control these AV Receivers...") {
        //  Ideally, I would specify capability.avTuner instead
		input "receivers", "capability.musicPlayer", title: "Which Receivers?", multiple:true, required: true
	}
    section("Whenever this button is turned on") {
			input "switches", "capability.momentary", title: "Which button?", multiple:false, required: true
	}
    section("With this input...") {
		input(name: "inputChan", type: "text", title: "Which channel?", required: false)
        input(name: "level", type: "number", title: "What volume level?", required: false)
	}
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

private def initialize() {
	log.debug("initialize() with settings: ${settings}")
	subscribe(switches, "switch.on", receiverHandler)
    subscribe(app, receiverHandler)
}

def receiverHandler(evt) {
	receivers?.inputSelect(inputChan)
    receivers?.setLevel(level)
    receivers?.refresh()
}
