package it.polimi.dice.tracechecking.ui.launcher;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import it.polimi.dice.tracechecking.uml2json.json.TopologyNodeFormula;

public class TopologyNodeFormulaComparator extends ViewerComparator {
    private int propertyIndex;
    private static final int DESCENDING = 1;
    private int direction = DESCENDING;

    public TopologyNodeFormulaComparator() {
        this.propertyIndex = 0;
        direction = DESCENDING;
    }

    public int getDirection() {
        return direction == 1 ? SWT.DOWN : SWT.UP;
    }

    public void setColumn(int column) {
        if (column == this.propertyIndex) {
            // Same column as last sort; toggle the direction
            direction = 1 - direction;
        } else {
            // New column; do an ascending sort
            this.propertyIndex = column;
            direction = DESCENDING;
        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        TopologyNodeFormula tnf1 = (TopologyNodeFormula) e1;
        TopologyNodeFormula tnf2 = (TopologyNodeFormula) e2;
        int rc = 0;
        switch (propertyIndex) {
        case 0: // name column
            rc = tnf1.getName().compareTo(tnf2.getName());
            break;
        case 1: // selected column
        	if (tnf1.isSelected() == tnf2.isSelected()) {
                rc = 0;
            } else
                rc = (tnf1.isSelected() ? 1 : -1);
            break;
        case 2:
            rc = Double.compare(tnf1.getDesignValue(), tnf2.getDesignValue());
            break;
        default:
            rc = 0;
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }

}
