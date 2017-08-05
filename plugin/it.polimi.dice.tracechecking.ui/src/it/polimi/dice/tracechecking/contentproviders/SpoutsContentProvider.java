package it.polimi.dice.tracechecking.contentproviders;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import it.polimi.dice.tracechecking.ui.launcher.TCStormMainLaunchConfigurationTab.FormData;

public class SpoutsContentProvider implements IStructuredContentProvider {
	@Override
	public Object[] getElements(Object inputElement) {
		return ((FormData) inputElement).getSpoutsFormulae().toArray();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}
}
