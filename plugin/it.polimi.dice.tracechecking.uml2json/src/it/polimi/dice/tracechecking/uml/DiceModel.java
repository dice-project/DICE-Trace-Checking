package it.polimi.dice.tracechecking.uml;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;

import it.polimi.dice.tracechecking.uml.diagrams.classdiagram.ClassDiagram;
import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;

public class DiceModel{
	
	/** The Eclipse UML2 model */
	private Model uml_model;
	
	/** The system package */
	private org.eclipse.uml2.uml.Package system_package;
	/** The property package */
	private org.eclipse.uml2.uml.Package property_package;
	
	//private static final Logger LOGGER = Logger.getLogger(MadesModel.class); 

    public DiceModel(org.eclipse.uml2.uml.Model m){
    	this.uml_model=m;
    	
    	try{
	    	for(PackageableElement p: this.uml_model.getPackagedElements()){
	    		if(UML2ModelHelper.hasStereotype(p, "System")) this.system_package=(org.eclipse.uml2.uml.Package) p;
	    		if(UML2ModelHelper.hasStereotype(p, "Property")) this.property_package=(org.eclipse.uml2.uml.Package) p;
	    	}
	    	if(this.system_package==null){
	    		throw new Exception("System package not found: unable to translate the model");
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
	public ClassDiagram getClassdiagram() {
		return new ClassDiagram(this.system_package);
	}
	
/*	public Set<SequenceDiagram> getSequenceDiagrams(){
		Set<SequenceDiagram> sequencediagrams=new HashSet<SequenceDiagram>();
		for(Element e:this.system_package.getOwnedElements()){
			if(e instanceof org.eclipse.uml2.uml.Interaction && !UML2ModelHelper.hasStereotype(e, "Ignore")){
				sequencediagrams.add(new SequenceDiagram((org.eclipse.uml2.uml.Interaction)e));
			}
		}
		return sequencediagrams;
	}
*/	
/*	public SequenceDiagram findSequenceDiagram(String name){
		for(Element e:this.system_package.getOwnedElements()){
			if(e instanceof org.eclipse.uml2.uml.Interaction && ((org.eclipse.uml2.uml.Interaction) e).getName().equals(name) && !UML2ModelHelper.hasStereotype(e, "Ignore")){
				return new SequenceDiagram((org.eclipse.uml2.uml.Interaction) e);
			}
		}
		return null;
	}
*/
	public Model getUMLModel() {
		return this.uml_model;
	}

/*	public Set<IOD> getIODs() {
		Set<IOD> iods=new HashSet<IOD>();
		for(Element e:this.system_package.getOwnedElements()){
			if(e instanceof org.eclipse.uml2.uml.Activity && !UML2ModelHelper.hasStereotype(e, "Ignore")){
				iods.add(new IOD((org.eclipse.uml2.uml.Activity)e));
			}
		}
		return iods;
	}
*/	
/*	public Property getProperty(){
		for(Element e: this.property_package.getOwnedElements()){
			if(UML2ModelHelper.hasStereotype(e, "Property")){
				return new Property((Class) e);
			}
		}
		return null;
	}
*/
	public boolean hasProperty() {
		//no property package at all
		if(this.property_package==null) return false;
		
		//there is a property package, let's look for the property
		for(Element e: this.property_package.getOwnedElements()){
			if(UML2ModelHelper.hasStereotype(e, "Property")){
				return true;
			}
		}
		
		//property not found in the property package
		return false;
	}

}
