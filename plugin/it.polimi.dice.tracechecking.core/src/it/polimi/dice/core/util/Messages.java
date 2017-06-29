package it.polimi.dice.core.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "it.polimi.dice.core.util.messages"; //$NON-NLS-1$
	public static String StreamRedirector_unableToRedirectStreamError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
