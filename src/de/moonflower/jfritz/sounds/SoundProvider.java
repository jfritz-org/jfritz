package de.moonflower.jfritz.sounds;

import java.net.URL;

public class SoundProvider {
	private static URL ringSound;
	private static URL callSound;

	public SoundProvider() {
		init();
	}

	private void init() {
		ringSound = getClass().getResource(
				"/de/moonflower/jfritz/resources/sounds/call_in.wav"); //$NON-NLS-1$
		callSound = getClass().getResource(
				"/de/moonflower/jfritz/resources/sounds/call_out.wav"); //$NON-NLS-1$
	}

	public URL getRingSound() {
		return ringSound;
	}

	public URL getCallSound() {
		return callSound;
	}
}
