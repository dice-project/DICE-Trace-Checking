package it.polimi.dice.tracechecking.ui.launcher.editingsupports;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.polimi.dice.tracechecking.ui.launcher.DirtableTab;
import it.polimi.dice.tracechecking.uml2json.json.NodeParameter;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class ParameterEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private final CellEditor editor;
	private final DirtableTab tab;

	public ParameterEditingSupport(TableViewer viewer, DirtableTab tab) {
		super(viewer);
		this.viewer = viewer;
		String[] parameters = new String[NodeParameter.values().length];
		for (NodeParameter p : NodeParameter.values()) {
			parameters[p.ordinal()] = p.getParameterName();
		}
		this.editor = new ComboBoxCellEditor(viewer.getTable(), parameters);
		this.tab = tab;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		CCombo combo = (CCombo) editor.getControl();
		combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setValue(element, combo.getSelectionIndex());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		TopologyNodeFormula tnf = (TopologyNodeFormula) element;
		for (NodeParameter p : NodeParameter.values()) {
			if (tnf.getParameter().equals(p))
				return p.ordinal();
		}
		return 0;
	}

	@Override
	protected void setValue(Object element, Object value) {
		TopologyNodeFormula tnf = (TopologyNodeFormula) element;
		for (NodeParameter p : NodeParameter.values()) {
			if (((Integer) value) == p.ordinal()) {
				tnf.setParameter(p);
				viewer.update(element, null);
				tab.setTabDirty(true);
			}
		}
	}
}