package de.moonflower.jfritz.sounds;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.TestHelper;

public class PlaySoundTest {
	static SoundProvider sp;

	@BeforeClass
	public static void setup() {
		TestHelper.initLogging();
		sp = new SoundProvider();
	}

	@Test
	public void testPlayRingSound() {
		PlaySound ps = new PlaySound(sp);
		ps.playRingSound(1.0f);
	}

	@Test
	public void testPlayCallSound() {
		PlaySound ps = new PlaySound(sp);
		ps.playCallSound(1.0f);
	}

	@Test
	public void testPlayFile() {
		PlaySound ps = new PlaySound(sp);
		ps.playSound(new File("/home/robotniko/workspace/jfritz/src/de/moonflower/jfritz/resources/sounds/call_in.wav"));
	}
}
