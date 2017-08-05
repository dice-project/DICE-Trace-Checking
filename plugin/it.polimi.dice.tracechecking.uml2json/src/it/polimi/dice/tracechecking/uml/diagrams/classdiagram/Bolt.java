package it.polimi.dice.tracechecking.uml.diagrams.classdiagram;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;

import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;

public class Bolt {

	private InstanceSpecification umlBolt;

	public Bolt(org.eclipse.uml2.uml.InstanceSpecification c) {
		this.umlBolt=c;
	}

	public String getName() {
		return umlBolt.getName();
	}
	
	
	/** Do not use if you do not know what are you doing **/
	public InstanceSpecification getUMLBolt(){
		return umlBolt;
	}

	public int getParallelism() {
		Classifier umlBoltType=umlBolt.getClassifiers().get(0);
		int parallelism= (Integer)umlBolt.getClassifiers().get(0).getValue(UML2ModelHelper.getStereotype(umlBoltType, "Bolt"), "parallelism");
		return parallelism;
	}

	public int getSigma() {
		Classifier umlBoltType=umlBolt.getClassifiers().get(0);
		int sigma= (Integer)umlBolt.getClassifiers().get(0).getValue(UML2ModelHelper.getStereotype(umlBoltType, "Bolt"), "sigma");
		return sigma;
	}
	
	public int getAlpha() {
		Classifier uml_bolttype=umlBolt.getClassifiers().get(0);
		int alpha = (Integer)umlBolt.getClassifiers().get(0).getValue(UML2ModelHelper.getStereotype(uml_bolttype, "Bolt"), "alpha");
		return alpha;
	}


	
}
