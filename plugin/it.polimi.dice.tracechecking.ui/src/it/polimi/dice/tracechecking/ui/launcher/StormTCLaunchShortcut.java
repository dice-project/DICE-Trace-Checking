package it.polimi.dice.tracechecking.ui.launcher;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import it.polimi.dice.tracechecking.TraceCheckingPlugin;
import it.polimi.dice.tracechecking.core.logger.DiceLogger;
import it.polimi.dice.tracechecking.core.ui.dialogs.DialogUtils;
import it.polimi.dice.tracechecking.launcher.TraceCheckingLaunchConfigurationAttributes;
import it.polimi.dice.tracechecking.ui.TraceCheckingUI;
import it.polimi.dice.tracechecking.ui.preferences.PreferenceConstants;
import it.polimi.dice.tracechecking.uml.helpers.DiceProfileConstants;
import it.polimi.dice.tracechecking.uml.helpers.UML2ModelHelper;

public class StormTCLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (!structuredSelection.isEmpty()) {
				launch(structuredSelection.getFirstElement(), mode);
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		if (input != null){
			IFile file = (IFile) input.getAdapter(IFile.class);
			if (file != null) {
				launch(file, mode);
			}	
		}
	}
	
	protected void launch(Object type, String mode) {
		UmlModel model = type instanceof UmlModel ? (UmlModel) type : ((IAdaptable) type).getAdapter(UmlModel.class);
		if (model == null) {
			DiceLogger.logError(TraceCheckingUI.getDefault(),
					MessageFormat.format(Messages.VerificationLaunchShortcut_unexpectedArgError,
							TraceCheckingPlugin.TRACE_CHECKING_LAUNCH_CONFIGURATION_TYPE_STORM, model));
			return;
		}

		if (!ILaunchManager.RUN_MODE.equals(mode)) {
			DiceLogger.logWarning(TraceCheckingUI.getDefault(),
					MessageFormat.format(Messages.VerificationLaunchShortcut_unknownModeError, mode, model));
		}

		if (!UML2ModelHelper.hasProfileApplied(model.getURI().toString(), DiceProfileConstants.STORM_PROFILE_NAME)) {
			DiceLogger.logError(TraceCheckingUI.getDefault(),
					MessageFormat.format(Messages.TraceCheckingLaunchShortcut_missingProfileMessage,
							TraceCheckingPlugin.TRACE_CHECKING_LAUNCH_CONFIGURATION_TYPE_STORM,
							DiceProfileConstants.STORM_PROFILE_NAME));
			DialogUtils.getWarningDialog(null, Messages.TraceCheckingLaunchShortcut_missingProfileText,
					MessageFormat.format(Messages.TraceCheckingLaunchShortcut_missingProfileMessage,
							TraceCheckingPlugin.TRACE_CHECKING_LAUNCH_CONFIGURATION_TYPE_STORM,
							DiceProfileConstants.STORM_PROFILE_NAME));
			return;
		}

		try {
			ILaunchConfiguration launchConfiguration = findLaunchConfiguration(model, mode);
			if (launchConfiguration != null) {
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				DebugUITools.openLaunchConfigurationDialogOnGroup(shell, new StructuredSelection(launchConfiguration),
						IDebugUIConstants.ID_RUN_LAUNCH_GROUP, null);
			}
		} catch (CoreException e) {
			DiceLogger.logException(TraceCheckingUI.getDefault(), e);
		}

	}

	protected ILaunchConfiguration findLaunchConfiguration(UmlModel model, String mode) throws CoreException {

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

		ILaunchConfigurationType verLaunchConfigurationType = launchManager
				.getLaunchConfigurationType(TraceCheckingPlugin.TRACE_CHECKING_LAUNCH_CONFIGURATION_TYPE_STORM);

		ILaunchConfiguration[] existingConfigs = launchManager.getLaunchConfigurations(verLaunchConfigurationType);

		// We search through the existing configurations if the actual
		// configuration has been previously defined
		for (ILaunchConfiguration previousConfiguration : existingConfigs) {
			String previousFile = previousConfiguration
					.getAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE, StringUtils.EMPTY);
			if (previousFile.equals(model.getURI().toString())) {
				return previousConfiguration;
			}
		}

		String name = model.getURI().trimFileExtension().lastSegment();
		String casedName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		ILaunchConfigurationWorkingCopy launchConf = verLaunchConfigurationType.newInstance(null, casedName);
		launchConf.setAttribute(TraceCheckingLaunchConfigurationAttributes.INPUT_FILE, model.getURI().toString());
		launchConf.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_HOST_ADDRESS,
				TraceCheckingUI.getDefault().getPreferenceStore().getString(PreferenceConstants.TC_HOST.getName()));
		launchConf.setAttribute(TraceCheckingLaunchConfigurationAttributes.TC_PORT_NUMBER,
				TraceCheckingUI.getDefault().getPreferenceStore().getString(PreferenceConstants.TC_PORT.getName()));
		launchConf.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_HOST_ADDRESS, TraceCheckingUI
				.getDefault().getPreferenceStore().getString(PreferenceConstants.MONITORING_HOST.getName()));
		launchConf.setAttribute(TraceCheckingLaunchConfigurationAttributes.MONITORING_PORT_NUMBER, TraceCheckingUI
				.getDefault().getPreferenceStore().getString(PreferenceConstants.MONITORING_PORT.getName()));
		ILaunchConfiguration result = launchConf.doSave();

		return result;
	}

}
