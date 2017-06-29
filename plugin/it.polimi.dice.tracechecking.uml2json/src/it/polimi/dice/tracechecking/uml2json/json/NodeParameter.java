package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.annotations.SerializedName;

public enum NodeParameter {
	@SerializedName("avg_emit_rate")
	AVG_EMIT_RATE ("avg_emit_rate", NodeType.SPOUT),
	@SerializedName("sigma")
	SIGMA ("sigma",  NodeType.BOLT),
	@SerializedName("alpha")
	ALPHA ("alpha",  NodeType.BOLT);
	
	private String parameterName;
	private NodeType[] nodeTypes;
	
	private NodeParameter(String parameterName, NodeType... nodeTypes){
		this.parameterName = parameterName;
		this.nodeTypes = nodeTypes;
	}
	
	public String getParameterName() {
		return parameterName;
	}
	public NodeType[] getNodeTypes() {
		return nodeTypes;
	}
}
