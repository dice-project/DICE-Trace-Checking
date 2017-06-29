package it.polimi.dice.tracechecking.uml.diagrams.classdiagram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Property;

import com.google.gson.annotations.SerializedName;

import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;

public class BoltClass extends NodeClass{

	private transient static final double DEFAULT_SIGMA = 1;
	private transient static final double DEFAULT_ALPHA = 1;
	private transient static final int DEFAULT_PARALLELISM = 1;

	private double alpha;
	private double sigma;
	private transient List<NodeClass> nodeSubscriptionList;
	@SerializedName(value="subs")
	private List<String> stringSubscriptionList;
	@SerializedName(value="min_ttf")
	private int minimumTimeToFailure;
	@SerializedName(value="d")
	private double aggregateCoefficient;
	
	public BoltClass(org.eclipse.uml2.uml.Class c) {
		super(c);
		this.alpha = extractAlpha();
		this.sigma = extractSigma();
		this.parallelism = extractParallelism();
		this.stringSubscriptionList = extractSubsciptionList();
		this.aggregateCoefficient = 0;
}

	
	
	public double getAlpha() {
		return alpha;
	}



	public double getSigma() {
		return sigma;
	}



	public List<String> getStringSubscriptionList() {
		return stringSubscriptionList;
	}



	public int getMinimumTimeToFailure() {
		return minimumTimeToFailure;
	}



	public double getAggregateCoefficient() {
		return aggregateCoefficient;
	}



	public List<NodeClass> getNodeSubscriptionList() {
		return nodeSubscriptionList;
	}

	public void setNodeSubscriptionList(List<NodeClass> nodeSubscriptionSet) {
		this.nodeSubscriptionList = nodeSubscriptionSet;
	}
	public void setNodeSubscriptions(List<NodeClass> nodeSubscriptionSet) {
		this.nodeSubscriptionList = nodeSubscriptionSet;
		for (NodeClass node : nodeSubscriptionSet) {
			this.stringSubscriptionList.add(node.getId());
		}
	}
	
	public List<String> getStringSubscriptionSet() {
		return stringSubscriptionList;
	}

	public void setStringSubscriptionSet(List<String> stringSubscriptionSet) {
		this.stringSubscriptionList = stringSubscriptionSet;
	}

	public String getName() {
		return super.getUmlClass().getName();
	}
	
	
	/** Do not use if you do not know what are you doing **/
	public Class getUMLBolt(){
		return super.getUmlClass();
	}


	@Override
	protected int extractParallelism() {
		//Classifier umlBoltType=umlBolt.getClassifiers().get(0);
		String parallelism_s = (String)umlClass.getValue(UML2ModelHelper.getStereotype(umlClass, "StormBolt"), "parallelism");
		int parallelism = parallelism_s != null ? Integer.parseInt(parallelism_s) : DEFAULT_PARALLELISM;
		return parallelism;
	}

	protected Double extractAlpha() {
		String alpha_s = (String)umlClass.getValue(UML2ModelHelper.getStereotype(umlClass, "StormBolt"), "alpha");
		Double alpha = alpha_s != null ? Double.parseDouble(alpha_s) : DEFAULT_ALPHA;
		return alpha;
	}

	protected Double extractSigma() {
		String sigma_s = (String)umlClass.getValue(UML2ModelHelper.getStereotype(umlClass, "StormBolt"), "sigma");
		Double sigma = sigma_s != null ? Double.parseDouble(sigma_s) : DEFAULT_SIGMA ;
		return sigma;
	}
	
	protected List<String> extractSubsciptionList(){
		List<String> subsList = new ArrayList<String>();
		for (Association association : this.umlClass.getAssociations()) {
			for(Property property : association.getMemberEnds()){
				if (property.getOwner() != association && !property.getName().toLowerCase().equals(this.umlClass.getName().toLowerCase())) {
					subsList.add(property.getName().toUpperCase());
					//DiceLogger.logError(Uml2JsonPlugin.getDefault(), "Adding " + property.getName() + " to subsList of: " + this.umlClass.getName());
				}
			}	
		}
		return subsList;
	}
	
	@Override
	   public String toString() {
	      return "Bolt [id=" + id + ", parallelism=" + parallelism + ", alpha=" + alpha + ", sigma="+ sigma + "]";
	   }
	
}
