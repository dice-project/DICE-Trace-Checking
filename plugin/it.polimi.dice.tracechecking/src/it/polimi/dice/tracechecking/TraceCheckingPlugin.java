package it.polimi.dice.tracechecking;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TraceCheckingPlugin extends Plugin{
	
	
	public static final String PLUGIN_ID = "it.polimi.dice.tracechecking"; //$NON-NLS-1$

	public static final String TRACE_CHECKING_LAUNCH_CONFIGURATION_TYPE_STORM = "it.polimi.dice.tracechecking.launchConfigurationTypeStorm"; //$NON-NLS-1$

	private static TraceCheckingPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public TraceCheckingPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static TraceCheckingPlugin getDefault() {
		return plugin;
	}


	
}