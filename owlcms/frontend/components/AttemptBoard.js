import {PolymerElement, html} from '@polymer/polymer/polymer-element.js';

class CurrentAttempt extends PolymerElement {
    static get is() {
        return 'attempt-board-template'
    }
    
    static get template() {
    	return html`
<style>
* {
	box-sizing: border-box;
}

.wrapper {
	font: Arial;
	color: white;
	background-color: black;
	height: 100vh;
	width: 100vw;
}

.attemptBoard {
	font-family: Arial, Helvetica, sans-serif;
	color: white;
	background-color: black;
	display: grid;
	width: 100vw;
	height: 100vh;
	grid-template-columns: [firstName-start lastName-start teamName-start
		startNumber-start weight-start] 1fr[startNumber-end attempt-start] 2fr[attempt-end
		weight-end barbell-start down-start decision-start] 3fr[barbell-end
		timer-start] 3fr[timer-end name-end down-end decision-end];
	grid-template-rows: [lastName-start]1fr[lastName-end firstName-start]1fr[firstName-end
		teamName-start down-start]2fr[teamName-end  decision-start]1fr[startNumber-start
		attempt-start barbell-start timer-start]1fr[startNumber-end
		attempt-end weight-start]5fr[weight-end barbell-end timer-end down-end
		decision-end];
	justify-content: center;
	align-content: center;
	align-items: stretch;
	justify-items: stretch;
	padding: 5vmin;
}

.attemptBoard .lastName {
	justify-self: left;
	font-size: 12vh;
	line-height: 12vh;
	font-weight: bold;
	grid-area: lastName-start/lastName-start/lastName-end/lastName-end;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .lastName {
		font-size: 7vw;
		line-height: 7vw;
		font-weight: bold;
	}
}

.attemptBoard .firstName {
	justify-self: left;
	font-size: 10vh;
	grid-area: firstName-start/firstName-start/firstName-end/firstName-end;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .firstName {
		font-size: 7vw;
		line-height: 7vw;
	}
}

.attemptBoard .teamName {
	justify-self: left;
	font-size: 8vh;
	line-height: 8vh;
	grid-area: teamName-start/teamName-start/teamName-end/teamName-end;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .teamName {
		font-size: 8vh;
		line-height: 8vh;
		padding-top: 3vmin;
	}
}

.attemptBoard .startNumber {
	font-size: 10vh;
	grid-area: startNumber-start/startNumber-start/startNumber-end/startNumber-end;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .startNumber {
		font-size: 8vh;
	}
}

.attemptBoard .startNumber span {
	border-width: 0.2ex;
	border-style: solid;
	border-color: red;
	width: 1.5em;
	display: flex;
	justify-content: center;
}

.attemptBoard .attempt {
    display: block;
	font-size: 10vh;
	line-height: 10vh;
	align-self: center;
	grid-area: attempt-start/attempt-start/attempt-end/attempt-end;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .attempt {
		font-size: 8vh;
		line-height: 8vh;
	}
}

.attemptBoard .weight {
	color: aqua;
	font-size: 20vh;
	line-height: 20vh;
	font-weight: bold;
	grid-area: weight-start/weight-start/weight-end/weight-end;
	align-self: center;
	justify-self: stretch;
}

@media screen and (max-width: 1300px) {
	.attemptBoard .weight {
		font-size: 12vw;
		line-height: 7vw;
	}
}

.attemptBoard .barbell {
	grid-area: barbell-start/barbell-start/barbell-end/barbell-end;
	justify-self: center;
}

.attemptBoard .timer {
	font-size: 24vh;
	font-weight: bold;
	grid-area: timer-start/timer-start/timer-end/timer-end;
	align-self: center;
	justify-self: center;
}

@media screen and (max-width: 1025px) {
	.attemptBoard .timer {
		font-size: 12vw;
	}
}

@media screen and (max-width: 1300px) {
	.attemptBoard .timer {
		font-size: 15vw;
	}
}

.breakTime {
	/* color: #99CCFF; */
	color: SkyBlue;
}

.athleteTimer {
	color: yellow;
}

.attemptBoard .down {
	grid-area: down-start/down-start/down-end/down-end;
	align-self: stretch;
	justify-self: stretch;
	--iron-icon-height: 120%;
	--iron-icon-width: 120%;
	font-weight: normal;
	color: lime;
	display: none;
	overflow: hidden;
}

.attemptBoard .decision {
	grid-area: decision-start/decision-start/decision-end/decision-end;
	font-size: 30vh;
}

.v-system-error {
	display: none;
}
</style>
<div class="wrapper">
<div class="attemptBoard" id="attemptBoardDiv">
	<div class="lastName" id="lastNameDiv">[[lastName]]</div>
	<div class="firstName" id="firstNameDiv">[[firstName]]</div>
	<div class="teamName" id="teamNameDiv">[[teamName]]</div>
	<div class="startNumber" id="startNumberDiv">
		<span>[[startNumber]]</span>
	</div>
	<div class="attempt" id="attemptDiv" inner-h-t-m-l="[[attempt]]"></div><!-- kludge to have preformatted html -->
	<div class="weight" id="weightDiv">
		<nobr>[[weight]]<span style="font-size: 75%">[[kgSymbol]]</span></nobr>
	</div>
	<div class="barbell" id="barbellDiv">
		<slot name="barbell"></slot>
	</div>
	<div class="timer athleteTimer" id="athleteTimerDiv">
		<timer-element id="athleteTimer"></timer-element>
	</div>
	<div class="timer breakTime" id="breakTimerDiv">
		<timer-element id="breakTimer"></timer-element>
	</div>
	<div class="decision" id="decisionDiv" on-down="down" on-hideX="reset">
		<decision-element id="decisions"></decision-element>
	</div>
</div>
</div>`;
}

    static get properties() {
    	return {
    		javaComponentId: {
    			type: String,
    			value: ''
    		},
    		lastName: {
    			type: String,
    			value: ''
    		},
    		firstName: {
    			type: String,
    			value: ''
    		},
    		teamName: {
    			type: String,
    			value: ''
    		},
    		startNumber: {
    			type: Number,
    			value: 0
    		},
    		attempt: {
    			type: String,
    			value: ''
    		}, 
    		weight: {
    			type: Number,
    			value: 0
    		}
    	}
    }

    ready() {
    	super.ready();
    	this.doBreak(); 
    	this.$.athleteTimerDiv.style.display="none";
    }

    start() {
    	this.$.timer.start();
    }

    reset() {
    	console.debug("attemptBoard reset "+this.javaComponentId);
    	this.$.attemptBoardDiv.style.display="grid";
    	this.$.attemptBoardDiv.style.color="white";
    	this.$.athleteTimer.reset();
    	this.$.athleteTimerDiv.style.display="block";
    	this.$.firstNameDiv.style.display="block";
    	this.$.teamNameDiv.style.display="block";
    	this.$.attemptDiv.style.display="block";
    	this.$.breakTimerDiv.style.display="none";
    	this.$.weightDiv.style.display="block";
    	this.$.startNumberDiv.style.display="block";
    	this.$.barbellDiv.style.display="block";
    	this.$.decisionDiv.style.display="none";
    	console.debug("end of attemptBoard reset "+this.javaComponentId);
    }

    down() {
    	console.debug("attemptBoard done "+this.javaComponentId);
    	this.$.athleteTimerDiv.style.display="none";
    	this.$.breakTimerDiv.style.display="none";
    	this.$.barbellDiv.style.display="none";
    	this.$.decisionDiv.style.display="block";
    	console.debug("end of attemptBoard dome "+this.javaComponentId);
    }

    doBreak() {
    	console.debug("attemptBoard doBreak "+this.javaComponentId);
    	this.$.attemptBoardDiv.style.display="grid";
    	this.$.attemptBoardDiv.style.color="white";
    	this.$.athleteTimerDiv.style.display="none";
    	this.$.breakTimerDiv.style.display="block";
    	this.$.firstNameDiv.style.display="block";
    	this.$.teamNameDiv.style.display="none";
    	this.$.attemptDiv.style.display="none";
    	this.$.weightDiv.style.display="none";
    	this.$.startNumberDiv.style.display="none";
    	this.$.barbellDiv.style.display="none";
    	this.$.decisionDiv.style.display="none";
    	console.debug("attemptBoard end doBreak "+this.javaComponentId);
    }

    groupDone() {
    	console.debug("attemptBoard groupDone "+this.javaComponentId);
    	this.$.attemptBoardDiv.style.display="grid";
    	this.$.attemptBoardDiv.style.color="white";
    	// this.$.breakTimer.reset();
    	this.$.athleteTimerDiv.style.display="none";
    	this.$.firstNameDiv.style.display="none";
    	this.$.teamNameDiv.style.display="none";
    	this.$.attemptDiv.style.display="none";
    	this.$.breakTimerDiv.style.display="none";
    	this.$.weightDiv.style.display="none";
    	this.$.startNumberDiv.style.display="none";
    	this.$.barbellDiv.style.display="none";
    	this.$.decisionDiv.style.display="none";
    	console.debug("attemptBoard end groupDone "+this.javaComponentId);
    }

    clear() {
    	console.debug("attemptBoard clear "+this.javaComponentId);
    	this.$.attemptBoardDiv.style.display="none";
    	console.debug("attemptBoard end clear "+this.javaComponentId);
    }
}

customElements.define(CurrentAttempt.is, CurrentAttempt);

