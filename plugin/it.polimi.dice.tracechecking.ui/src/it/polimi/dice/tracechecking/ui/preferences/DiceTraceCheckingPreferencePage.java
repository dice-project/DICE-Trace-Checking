package it.polimi.dice.tracechecking.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import it.polimi.dice.tracechecking.ui.TraceCheckingUI;

public class DiceTraceCheckingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public DiceTraceCheckingPreferencePage() {
		super(GRID);
		setPreferenceStore(TraceCheckingUI.getDefault().getPreferenceStore());
		setDescription("Basic Preferences for DICE Trace Checking tool");
	}

	@Override
	public void init(IWorkbench workbench) {
		 
		
	}

	@Override
	protected void createFieldEditors() {
	    addField(new StringFieldEditor(PreferenceConstants.TC_HOST.getName(), PreferenceConstants.TC_HOST.getDescription(),
	            getFieldEditorParent()));
	    addField(new StringFieldEditor(PreferenceConstants.TC_PORT.getName(), PreferenceConstants.TC_PORT.getDescription(),
	            getFieldEditorParent()));
	    addField(new StringFieldEditor(PreferenceConstants.MONITORING_HOST.getName(), PreferenceConstants.MONITORING_HOST.getDescription(),
	            getFieldEditorParent()));
	    addField(new StringFieldEditor(PreferenceConstants.MONITORING_PORT.getName(), PreferenceConstants.MONITORING_PORT.getDescription(),
	            getFieldEditorParent()));

		
	}


}
