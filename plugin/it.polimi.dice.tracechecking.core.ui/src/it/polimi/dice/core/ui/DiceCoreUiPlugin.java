package it.polimi.dice.core.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class DiceCoreUiPlugin extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "it.polimi.dice.core.ui"; //$NON-NLS-1$

	// The shared instance
	private static DiceCoreUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public DiceCoreUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DiceCoreUiPlugin getDefault() {
		return plugin;
	}
}
