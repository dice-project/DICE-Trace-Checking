package it.polimi.dice.tracechecking.core.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DialogUtils {

	public static void getWarningDialog(Exception e, String text, String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageBox dialog = new MessageBox(activeShell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText(text);
				dialog.setMessage(message);

				// open dialog and await user selection
				dialog.open();

			}
		});
	}
}
