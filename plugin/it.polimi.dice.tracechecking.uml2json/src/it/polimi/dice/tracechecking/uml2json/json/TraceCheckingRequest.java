package it.polimi.dice.tracechecking.uml2json.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TraceCheckingRequest {
	
	@SerializedName("topologyName")
	private String topologyName;
	@SerializedName("nodes")
	private List<TopologyNodeFormula> nodeFormulae;
	//	@SerializedName("formulae")
	//	private List<FreeFormula> freeFormulae;
	
	public TraceCheckingRequest(String topologyname, List<TopologyNodeFormula> nodeFormulae){
		this.nodeFormulae = nodeFormulae;
		this.topologyName = topologyname; 
	}

	public String getTopologyName() {
		return topologyName;
	}

	public List<TopologyNodeFormula> getNodeFormulae() {
		return nodeFormulae;
	}
	
	

}
