package it.polimi.dice.tracechecking.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TraceCheckingUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "it.polimi.dice.tracechecking.ui"; //$NON-NLS-1$

	public static final String IMG_TC_MAIN_TAB = "IMG_TC_MAIN_TAB";
	public static final String IMG_CHECKED = "IMG_CHECKED";
	public static final String IMG_UNCHECKED = "IMG_UNCHECKED";
	
	// The shared instance
	private static TraceCheckingUI plugin;
	
	/**
	 * The constructor
	 */
	public TraceCheckingUI() {
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
	public static TraceCheckingUI getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(IMG_TC_MAIN_TAB, getImageDescriptor("icons/tc_icon.png").createImage()); //$NON-NLS-1$
		reg.put(IMG_CHECKED, getImageDescriptor("icons/checked.gif").createImage());
		reg.put(IMG_UNCHECKED, getImageDescriptor("icons/unchecked.gif").createImage());
	}

}
