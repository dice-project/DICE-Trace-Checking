package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.annotations.SerializedName;

public enum Relation {
		@SerializedName("!=")
		NEQ ("!="),
		@SerializedName("=")
		EQ ("="),
		@SerializedName(">")
		GT (">"),
		@SerializedName(">=")
		GTE (">="),
		@SerializedName("<")
		LT ("<"),
		@SerializedName("<=")
		LTE ("<=");

	private final String stringRep;
	
	Relation(String stringRep){
		this.stringRep = stringRep;
	}
	
	public String getStringRep(){
		return stringRep;
	}
}
