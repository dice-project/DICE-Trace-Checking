package it.polimi.dice.tracechecking.uml.diagrams.classdiagram;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;

import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;

public class Spout {

	private InstanceSpecification umlSpout;

	public Spout(org.eclipse.uml2.uml.InstanceSpecification c) {
		this.umlSpout=c;
	}

	public String getName() {
		return umlSpout.getName();
	}
	
	
	/** Do not use if you do not know what are you doing **/
	public InstanceSpecification getUMLSpout(){
		return umlSpout;
	}

	public int getParallelism() {
		Classifier umlSpoutType=umlSpout.getClassifiers().get(0);
		int parallelism= (Integer)umlSpout.getClassifiers().get(0).getValue(UML2ModelHelper.getStereotype(umlSpoutType, "Spout"), "parallelism");
		return parallelism;
	}

	public int getAverageEmitRate() {
		Classifier umlSpoutType=umlSpout.getClassifiers().get(0);
		int avgEmitRate= (Integer)umlSpout.getClassifiers().get(0).getValue(UML2ModelHelper.getStereotype(umlSpoutType, "Spout"), "avg_emit_rate");
		return avgEmitRate;
	}
	

}
