package it.polimi.dice.tracechecking.ui.launcher.editingsupports;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import it.polimi.dice.tracechecking.ui.TraceCheckingUI;
import it.polimi.dice.tracechecking.ui.launcher.DirtableTab;
import it.polimi.dice.tracechecking.ui.launcher.MainLaunchConfigurationTab;
import it.polimi.dice.tracechecking.ui.launcher.Messages;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class SelectedEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private final CellEditor editor;
	private final Shell shell;
	private final DirtableTab tab;

	public SelectedEditingSupport(TableViewer viewer, Shell shell, MainLaunchConfigurationTab tab) {
		super(viewer);
		this.viewer = viewer;
		this.editor = new CheckboxCellEditor(viewer.getTable(), SWT.CHECK);
		this.shell = shell;
		this.tab = tab;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
		//return new CheckboxCellEditor(null, SWT.CHECK);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((TopologyNodeFormula) element).isSelected();
	}


	@Override
	protected void setValue(Object element, Object userInputValue) {
		try {
			Boolean value = (Boolean)userInputValue;   //Boolean.valueOf(userInputValue.toString());
			((TopologyNodeFormula) element).setSelected(value);
			tab.setTabDirty(true);
			viewer.update(element, null);
		} catch (Throwable t) {
			ErrorDialog.openError(shell, Messages.MainLaunchConfigurationTab_errorTitle, Messages.MainLaunchConfigurationTab_invalidBooleanError, new Status(IStatus.ERROR, TraceCheckingUI.PLUGIN_ID, t.getLocalizedMessage(), t));
		}
	}
}