package it.polimi.dice.tracechecking.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import it.polimi.dice.tracechecking.launcher.TraceCheckingLaunchConfigurationAttributes;
import it.polimi.dice.tracechecking.ui.TraceCheckingUI;

public class DiceTraceCheckingPreferencesInitializer extends AbstractPreferenceInitializer {

	  public DiceTraceCheckingPreferencesInitializer() {
	 	}

	  @Override
	  public void initializeDefaultPreferences() {
	    IPreferenceStore store = TraceCheckingUI.getDefault().getPreferenceStore();
	    store.setDefault(PreferenceConstants.TC_HOST.getName(), TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_HOST_ADDRESS);
	    store.setDefault(PreferenceConstants.TC_PORT.getName(), TraceCheckingLaunchConfigurationAttributes.DEFAULT_TC_PORT_NUMBER);
	    store.setDefault(PreferenceConstants.MONITORING_HOST.getName(), TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_HOST_ADDRESS);
	    store.setDefault(PreferenceConstants.MONITORING_PORT.getName(), TraceCheckingLaunchConfigurationAttributes.DEFAULT_MONITORING_PORT_NUMBER);
	    
	  }


}
