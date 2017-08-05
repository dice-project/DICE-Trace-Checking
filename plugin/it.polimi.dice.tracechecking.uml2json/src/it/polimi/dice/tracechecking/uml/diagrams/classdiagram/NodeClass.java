package it.polimi.dice.tracechecking.uml.diagrams.classdiagram;

public abstract class NodeClass {

	protected transient org.eclipse.uml2.uml.Class umlClass;
	
	protected String id;

	protected int parallelism;
	
	public NodeClass(org.eclipse.uml2.uml.Class c){
		this.umlClass = c;
		this.id = this.umlClass.getName();
	}
	
	public org.eclipse.uml2.uml.Class getUmlClass(){
		return this.umlClass;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getParallelism() {
		return parallelism;
	}
	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}
	
	protected abstract int extractParallelism();

	

	
}
