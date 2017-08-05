package it.polimi.dice.tracechecking.uml2json.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public enum NodeType {
	@SerializedName("spout")
	SPOUT ("spout"),
	@SerializedName("bolt")
	BOLT ("bolt");
	
	private final String typeName;
	
	private NodeType(String id) {
		this.typeName = id;
	}
	
	public String getTypeName(){
		return typeName;
	}
	
	public List<NodeParameter> getParameters(){
		List<NodeParameter> params = new ArrayList<>();
		for (NodeParameter np : NodeParameter.values()) {
			if(Arrays.asList(np.getNodeTypes()).contains(this))
				params.add(np);
		}
		return params;
		
	}
	
	
	public static void main(String[] args) {
		for (NodeType nt : NodeType.values()) {
			System.out.println(nt.getParameters());
		}
	}
}

