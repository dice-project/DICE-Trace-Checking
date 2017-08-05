package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.annotations.SerializedName;

public enum Method {
	@SerializedName("average")
	AVERAGE ("average", 0),
	@SerializedName("counting")
	COUNTING ("counting", 1),
	@SerializedName("max")
	MAX ("max", 2),
	@SerializedName("pairaverage")
	PAIRAVERAGE ("pairaverage", 3);
	
	private String methodName;
	private int code;
	
	private Method(String methodName, int code){
		this.methodName = methodName;
		this.code = code;
	}
	
	public String getMethodName(){
		return methodName;
	}
	
	public int getCode(){
		return code;
	}
}
