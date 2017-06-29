package it.polimi.dice.core.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class Utils {

	public static void openExceptionDialog(Exception e, String additionalInfo) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageBox dialog = new MessageBox(activeShell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText(e.getMessage());
				dialog.setMessage(e.getMessage() + "\n" + additionalInfo);

				// open dialog and await user selection
				dialog.open();
			}
		});
	}
}
