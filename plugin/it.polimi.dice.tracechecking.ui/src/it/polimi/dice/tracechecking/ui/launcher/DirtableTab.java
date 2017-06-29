package it.polimi.dice.tracechecking.ui.launcher;

import org.eclipse.swt.widgets.Shell;

public interface DirtableTab {
	
	public void setTabDirty(boolean dirty);
	
	public Shell getTabShell();
	
}
