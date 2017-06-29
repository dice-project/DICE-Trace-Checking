package it.polimi.dice.tracechecking.ui.launcher.editingsupports;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.polimi.dice.tracechecking.ui.launcher.DirtableTab;
import it.polimi.dice.tracechecking.uml2json.json.Relation;
import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class RelationEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    private final DirtableTab tab;
    private final CellEditor editor;
    
    public RelationEditingSupport(TableViewer viewer, DirtableTab tab) {
        super(viewer);
        this.viewer = viewer;
    	String[] relations = new String[Relation.values().length];
    	for (Relation r : Relation.values()) {
    		relations[r.ordinal()] = r.getStringRep();
		}
        this.editor = new ComboBoxCellEditor(viewer.getTable(), relations);
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
        for (Relation r : Relation.values()) {
        	if(tnf.getRelation().equals(r))
        		return r.ordinal();
        }
        return 1;
    }

    @Override
    protected void setValue(Object element, Object value) {
    	TopologyNodeFormula	tnf = (TopologyNodeFormula) element;
    	for (Relation r : Relation.values()) {
    		if(((Integer) value) == r.ordinal()){
    			tnf.setRelation(r);
    	        viewer.update(element, null);
    	        tab.setTabDirty(true);
    		}
    	}
    }
}