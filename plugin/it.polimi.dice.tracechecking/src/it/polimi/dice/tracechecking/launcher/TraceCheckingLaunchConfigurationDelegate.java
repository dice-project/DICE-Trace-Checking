package it.polimi.dice.tracechecking.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.dice.tracechecking.config.TraceCheckingToolSerializer;
import it.polimi.dice.tracechecking.httpclient.HttpClient;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;
import it.polimi.dice.tracechecking.uml2json.json.TraceCheckingRequest;




public class TraceCheckingLaunchConfigurationDelegate extends LaunchConfigurationDelegate{

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String serverAddress = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.HOST_ADDRESS, "http://localhost");
		String serverPort = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.PORT_NUMBER, "5000");
		
		
		List<TopologyNodeFormula> nodeFormulae = new ArrayList<>();
		// get the serialized boltsFormulae parameter
		String s_boltFormulae = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE, "[]");
		System.out.println(s_boltFormulae);
		try {
			List<TopologyNodeFormula> boltFormulae = TraceCheckingToolSerializer.deserialize(s_boltFormulae);
			for (TopologyNodeFormula tnf : boltFormulae) {
				if (tnf.isSelected()) {
					nodeFormulae.add(tnf);
					System.out.println("Adding: " + tnf.getName());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get the serialized spoutFormulae parameter
		String s_spoutsFormulae = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE, "[]");
		System.out.println(s_boltFormulae);
		try {
			List<TopologyNodeFormula> spoutFormulae = TraceCheckingToolSerializer.deserialize(s_spoutsFormulae);
			for (TopologyNodeFormula tnf : spoutFormulae) {
				if (tnf.isSelected()) {
					nodeFormulae.add(tnf);
					System.out.println("Adding: " + tnf.getName());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		TraceCheckingRequest tcRequest = new TraceCheckingRequest("prova", nodeFormulae); 
				
		String launchTCUrl = serverAddress+":"+serverPort+"/"+"SERVICENAME";
		
		ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
		    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
		        if("selected".equals(fieldAttributes.getName())){
		            return true;
		        }
		        return false;
		    }

		    public boolean shouldSkipClass(Class<?> aClass) {
		        return false;
		    }
		};
		
		
		Gson gsonBuilder = new GsonBuilder().disableHtmlEscaping().setExclusionStrategies(exclusionStrategy).create();
		
		HttpClient nc = new HttpClient();
		boolean connectionSuccessful;
		System.out.println("Creating request:\n" + gsonBuilder.toJson(tcRequest));
		connectionSuccessful = nc.postJSONRequest(launchTCUrl, gsonBuilder.toJson(tcRequest));
		
		if (connectionSuccessful){
			try {
			    Thread.sleep(5000);                 
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			nc.getTaskStatusUpdatesFromServer();
	//		openNewBrowserTab(new URL(dashboardUrl), "task-list");
		}
		
		
	}

	
}
