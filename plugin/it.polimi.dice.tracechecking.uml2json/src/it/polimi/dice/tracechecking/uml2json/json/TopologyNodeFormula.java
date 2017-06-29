package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class TopologyNodeFormula {
	
/*	
	"name": "spoutA",
	"type": "spout",
	"parameter": "idleTime",
	"timewindow": 3600,
	"inputrate": 100,
	"method": "",
	"relation": "",
	"designvalue": 0.0
	
	*/
	
	private String name;
	private NodeType type;
	private NodeParameter parameter;
	@SerializedName("timewindow")
	private Float timeWindow;
	@SerializedName("timesubwindow")
	private Float timeSubWindow;
	@SerializedName("inputrate")
	private Float inputRate;
	private Method method;
	private Relation relation;
	@SerializedName("designvalue")
	private Float designValue;
	private boolean selected;
	
	public TopologyNodeFormula(String name, NodeType type, NodeParameter parameter, Float timeWindow, Float timeSubWindow, Float inputRate,
			Method method, Relation relation, Float designValue) {
		super();
		this.name = name;
		this.type = type;
		this.parameter = parameter;
		this.timeWindow = timeWindow;
		this.timeSubWindow = timeSubWindow;
		this.inputRate = inputRate;
		this.method = method;
		this.relation = relation;
		this.designValue = designValue;
		this.selected = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public NodeParameter getParameter() {
		return parameter;
	}

	public void setParameter(NodeParameter parameter) {
		this.parameter = parameter;
	}

	public Float getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(Float timeWindow) {
		this.timeWindow = timeWindow;
	}

	public Float getTimeSubWindow() {
		return timeSubWindow;
	}

	public void setTimeSubWindow(Float timeSubWindow) {
		this.timeSubWindow = timeSubWindow;
	}

	
	public Float getInputRate() {
		return inputRate;
	}

	public void setInputRate(Float inputRate) {
		this.inputRate = inputRate;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public Float getDesignValue() {
		return designValue;
	}

	public void setDesignValue(Float designValue) {
		this.designValue = designValue;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	public void setSelected(boolean selected){
		this.selected = selected;
	}

	public static void main(String[] args) {
		TopologyNodeFormula tnf = new TopologyNodeFormula("Pine", NodeType.BOLT, NodeParameter.SIGMA, new Float(100.0), new Float(1100.0), new Float(10.0), Method.COUNTING, Relation.LT, new Float(23.0));
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		System.out.println(gson.toJson(tnf));
	}
	
	
}
