package it.polimi.dice.tracechecking.launcher;

public interface TraceCheckingLaunchConfigurationAttributes {
		public static final String CONSOLE_TEXT = "it.polimi.dice.tracechecking.launcher.console.text";
		public static final String INPUT_FILE = "it.polimi.dice.tracechecking.launcher.console.inputModel"; //"INPUT_MODEL"; //$NON-NLS-1$
		public static final String KEEP_INTERMEDIATE_FILES= "it.polimi.dice.tracechecking.launcher.console.keepIntermediateFiles"; //"KEEP_INTERMEDIATE_FILES"; //$NON-NLS-1$
		public static final String INTERMEDIATE_FILES_DIR = "it.polimi.dice.tracechecking.launcher.console.intermediateFilesFolder"; 
		public static final String TIME_BOUND = "it.polimi.dice.tracechecking.launcher.console.timeBound"; //"TIME BOUND"; //$NON-NLS-1$
		public static final String TRACE_CHECKING_CONFIGURATION = "it.polimi.dice.tracechecking.launcher.console.traceheckingConfiguration"; //"VERIFICATION_CONFIGURATION"; //$NON-NLS-1$
//		public static final String TOPOLOGY_NODES_FORMULAE = "it.polimi.dice.tracechecking.launcher.console.topologyNodesFormulae"; //"VERIFICATION_CONFIGURATION"; //$NON-NLS-1$
		public static final String BOLTS_FORMULAE = "it.polimi.dice.tracechecking.launcher.console.boltsFormulae"; //$NON-NLS-1$
		public static final String SPOUTS_FORMULAE = "it.polimi.dice.tracechecking.launcher.console.spoutsFormulae"; //$NON-NLS-1$
		public static final String TC_HOST_ADDRESS = "it.polimi.dice.tracechecking.launcher.console.tcHostAddress";
		public static final String TC_PORT_NUMBER = "it.polimi.dice.tracechecking.launcher.console.tcPortNumber";
		public static final String MONITORING_HOST_ADDRESS = "it.polimi.dice.tracechecking.launcher.console.monitoringHostAddress";
		public static final String MONITORING_PORT_NUMBER = "it.polimi.dice.tracechecking.launcher.console.monitoringPortNumber";
		
		public static final String DEFAULT_TC_HOST_ADDRESS = "http://localhost";
		public static final String DEFAULT_TC_PORT_NUMBER = "5050";
		public static final String DEFAULT_MONITORING_HOST_ADDRESS = "http://109.231.122.169";
		public static final String DEFAULT_MONITORING_PORT_NUMBER = "5001";

	 
	} 
