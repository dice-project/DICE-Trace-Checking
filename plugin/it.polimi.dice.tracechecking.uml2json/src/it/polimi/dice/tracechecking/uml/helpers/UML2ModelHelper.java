package it.polimi.dice.tracechecking.uml.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;


public class UML2ModelHelper {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(UML2ModelHelper.class);
	
	/** The source UML model */
	private Model model = null;

	/** Get a Eclipse UML2 model */
	public UML2ModelHelper(Model model) {
		this.model = model;
	}

	/** Save the Decorated UML2 model to a file */	
	public void saveModel(String file){
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.createResource(URI.createFileURI(file));
        resource.getContents().add(model);
        try {
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Given a file containing the Eclipse UML2 Model returns the model */
	public static Model loadModel(String pathToModel){
		//A collection of related persistent documents.
		ResourceSet set = new ResourceSetImpl();
		
		java.net.URI inputFileUri = java.net.URI.create(pathToModel);
		File inputFile = new File(inputFileUri);
		
		URI uri = URI.createURI(inputFile.toURI().toString());
		
		set.createResource(uri);
		Resource r = set.getResource(uri, true);
		
		Model m=(Model)EcoreUtil.getObjectByType(r.getContents(), UMLPackage.eINSTANCE.getModel());
		return m;
	}
	
	public static boolean hasStereotype(Element e, String name){
		for(Stereotype st: e.getAppliedStereotypes()){
			if(st.getName().equals(name)) return true;
		}
		return false;
	}
	
	public static boolean hasProfileApplied(String inputFilePath, String profileName) {
		Model model = loadModel(inputFilePath);
		boolean found = false;
		for (Profile p : model.getAllAppliedProfiles()) {
			if (p.getName() != null)
				if (p.getName().equals(profileName))
					found = true;
		}
		return found;
	}
	
	
	public static boolean isSpoutInstance(Element e){
		if(e instanceof InstanceSpecification){
			InstanceSpecification is=(InstanceSpecification) e;
			for(Classifier c: is.getClassifiers()){
				if(isSpout(c)) return true;
			}
		}
		return false;
	}

	public static boolean isBoltInstance(Element e){
		if(e instanceof InstanceSpecification){
			InstanceSpecification is=(InstanceSpecification) e;
			for(Classifier c: is.getClassifiers()){
				if(isBolt(c)) return true;
			}
		}
		return false;
	}

	public static boolean isBolt(Element e){
		if(hasStereotype(e, "StormBolt")) 
			return true;
		return false;
	}
	
	public static boolean isStreamSubscription(Element e){
		if(hasStereotype(e, "StreamSubscription")) 
			return true;
		return false;
	}

	public static boolean isStream(Element e){
		if(hasStereotype(e, "StormStreamStep")) 
			return true;
		return false;
	}
	
	
	public static boolean isSpout(Element e){
		if(hasStereotype(e, "StormSpout")) 
			return true;
		return false;
	}
	
	

	public static Stereotype getStereotype(Element e, String name){
		for(Stereotype st: e.getAppliedStereotypes()){
			if(st.getName().equals(name)) return st;
		}
		return null;
	}


}