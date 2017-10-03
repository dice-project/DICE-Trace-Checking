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

public class TraceCheckingLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String tcServerAddress = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS,
				TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_HOST_ADDRESS);
		String tcServerPort = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER,
				TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_PORT_NUMBER);

		String monitoringServerAddress = configuration.getAttribute(
				TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS,
				TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_HOST_ADDRESS);
		String monitoringServerPort = configuration.getAttribute(
				TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER,
				TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_PORT_NUMBER);

		List<TopologyNodeFormula> nodeFormulae = new ArrayList<>();
		// get the serialized boltsFormulae parameter
		String s_boltFormulae = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.BOLTS_FORMULAE,
				"[]");
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
		String s_spoutsFormulae = configuration.getAttribute(TraceCheckingLaunchConfigurationAttributes.SPOUTS_FORMULAE,
				"[]");
		System.out.println(s_boltFormulae);
		try {
			List<TopologyNodeFormula> spoutFormulae = TraceCheckingToolSerializer.deserialize(s_spoutsFormulae);
			for (TopologyNodeFormula tnf : spoutFormulae) {
				if (tnf.isSelected()) {
					nodeFormulae.add(tnf);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		TraceCheckingRequest tcRequest = new TraceCheckingRequest("TOPOLOGY", nodeFormulae);

		String launchTCUrl = "http://"
				+ tcServerAddress.replaceFirst("^(http(?>s)://www\\.|http(?>s)://|www\\.|http(?s)://)", "") + ":"
				+ tcServerPort + "/run?ip=" + monitoringServerAddress + "&port=" + monitoringServerPort;

		ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
			public boolean shouldSkipField(FieldAttributes fieldAttributes) {
				if ("selected".equals(fieldAttributes.getName())) {
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
		System.out.println("Posting request:\n" + gsonBuilder.toJson(tcRequest) +"\n to:\n" + launchTCUrl);
		connectionSuccessful = nc.postJSONRequest(launchTCUrl, gsonBuilder.toJson(tcRequest));
		
		/*
		 * if (connectionSuccessful) { try { Display.getDefault().asyncExec(new
		 * Runnable() {
		 * 
		 * @Override public void run() { try { IWorkbenchPage page =
		 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 * IViewPart view =
		 * page.showView("it.polimi.dice.tracechecking.views.view1");
		 * 
		 * 
		 * } catch (PartInitException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } } });
		 * 
		 * Thread.sleep(5000); } catch (InterruptedException ex) {
		 * Thread.currentThread().interrupt(); } }
		 */

	}

}
