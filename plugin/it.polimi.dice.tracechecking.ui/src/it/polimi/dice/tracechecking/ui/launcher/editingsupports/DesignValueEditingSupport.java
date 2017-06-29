package it.polimi.dice.tracechecking.ui.launcher.editingsupports;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import it.polimi.dice.tracechecking.ui.TraceCheckingUI;
import it.polimi.dice.tracechecking.ui.launcher.DirtableTab;
import it.polimi.dice.tracechecking.ui.launcher.Messages;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;


public class DesignValueEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private final CellEditor editor;
	private DirtableTab tab; 

	public DesignValueEditingSupport(TableViewer viewer, DirtableTab tab) {
		super(viewer);
		this.viewer = viewer;
		this.editor = new TextCellEditor(viewer.getTable());
		this.tab = tab;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}


	@Override
	protected Object getValue(Object element) {
		return ((TopologyNodeFormula) element).getDesignValue().toString();
	}

	@Override
	protected void setValue(Object element, Object userInputValue) {
		try {
			Float value = Float.valueOf(userInputValue.toString());
			((TopologyNodeFormula) element).setDesignValue(value);
			viewer.update(element, null);
			tab.setTabDirty(true);
		} catch (Throwable t) {
			ErrorDialog.openError(tab.getTabShell(), Messages.MainLaunchConfigurationTab_errorTitle, Messages.MainLaunchConfigurationTab_invalidFloatError, new Status(IStatus.ERROR, TraceCheckingUI.PLUGIN_ID, t.getLocalizedMessage(), t));
		}
	}
}
