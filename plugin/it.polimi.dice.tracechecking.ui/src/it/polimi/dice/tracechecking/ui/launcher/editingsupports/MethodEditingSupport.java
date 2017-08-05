package it.polimi.dice.tracechecking.ui.launcher.editingsupports;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.polimi.dice.tracechecking.ui.launcher.DirtableTab;
import it.polimi.dice.tracechecking.uml2json.json.Method;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class MethodEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    private final CellEditor editor;
    private final DirtableTab tab;

    public MethodEditingSupport(TableViewer viewer, DirtableTab tab) {
        super(viewer);
        this.viewer = viewer;
        String[] methods = new String[Method.values().length];
    	for (Method m : Method.values()) {
			methods[m.ordinal()] = m.getMethodName();
		}
        this.editor = new ComboBoxCellEditor(viewer.getTable(), methods);
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
        TopologyNodeFormula	tnf = (TopologyNodeFormula) element;
        for (Method m : Method.values()) {
        	if(tnf.getMethod().equals(m))
        		return m.ordinal();
        }
        return 1;
    }

    @Override
    protected void setValue(Object element, Object value) {
    	TopologyNodeFormula	tnf = (TopologyNodeFormula) element;
    	for (Method m : Method.values()) {
    		if(((Integer) value) == m.ordinal()){
    			tnf.setMethod(m);
    			tab.setTabDirty(true);
    		}
    	}
        viewer.update(element, null);
    }
}