package it.polimi.dice.tracechecking.core.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "it.polimi.dice.tracechecking.core.ui.preferences.messages"; //$NON-NLS-1$
	public static String DummyPreferencePage_preferencesDescription;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
