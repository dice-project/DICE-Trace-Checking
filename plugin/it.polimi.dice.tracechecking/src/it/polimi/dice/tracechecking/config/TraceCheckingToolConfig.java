package it.polimi.dice.tracechecking.config;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public interface TraceCheckingToolConfig extends EObject {
	
	List<TopologyNodeFormula> getBoltsFormulae();
	List<TopologyNodeFormula> getSpoutsFormulae();
	

}
