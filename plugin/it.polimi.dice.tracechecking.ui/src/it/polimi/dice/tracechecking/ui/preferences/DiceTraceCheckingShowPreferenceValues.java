package it.polimi.dice.tracechecking.ui.preferences;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import it.polimi.dice.tracechecking.ui.TraceCheckingUI;

public class DiceTraceCheckingShowPreferenceValues extends AbstractHandler {

	@Override
	  public Object execute(ExecutionEvent event) throws ExecutionException {
	    Shell shell = HandlerUtil.getActiveWorkbenchWindowChecked(event)
	        .getShell();
	    String tcHost = TraceCheckingUI.getDefault().getPreferenceStore()
	        .getString(PreferenceConstants.TC_HOST.getName());
	    MessageDialog.openInformation(shell, "Info", tcHost);
	    String tcPort = TraceCheckingUI.getDefault().getPreferenceStore()
		        .getString(PreferenceConstants.TC_PORT.getName());
		    MessageDialog.openInformation(shell, "Info", tcPort);
		    String monitoringHost = TraceCheckingUI.getDefault().getPreferenceStore()
			        .getString(PreferenceConstants.MONITORING_HOST.getName());
			    MessageDialog.openInformation(shell, "Info", monitoringHost);
			    String monitoringPort = TraceCheckingUI.getDefault().getPreferenceStore()
				        .getString(PreferenceConstants.MONITORING_PORT.getName());
				    MessageDialog.openInformation(shell, "Info", monitoringPort);
		    return null;
	  }
}
