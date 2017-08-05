package it.polimi.dice.tracechecking.ui.preferences;


public enum PreferenceConstants {
	TC_HOST ("tc-host", "DiceTraCT Server Host Address:"),
	TC_PORT ("tc-port", "DiceTraCT Server Port Number:"),
	MONITORING_HOST ("monitoring-host", "D-Mon Server Host Address:"),
	MONITORING_PORT ("monitoring-port", "D-Mon Server Port Number:");


	private String name;
	private String description;
	
	private PreferenceConstants(String name, String description){
		this.name = name;
		this.description = description;
	}


	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
}

