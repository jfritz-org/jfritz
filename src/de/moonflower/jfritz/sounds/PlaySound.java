package de.moonflower.jfritz.sounds;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

public class PlaySound {
	private Logger log = Logger.getLogger(PlaySound.class);

	private SoundProvider soundProvider;

	public PlaySound() {
		this.soundProvider = new SoundProvider();
	}

	public PlaySound(final SoundProvider sp) {
		this.soundProvider = sp;
	}

	public void playRingSound(final float volume) {
		playSound(this.soundProvider.getRingSound());
	}

	public void playCallSound(final float volume) {
		playSound(this.soundProvider.getCallSound());
	}

	public void playSound(final File file) {
		try {
			testPlay(AudioSystem.getAudioInputStream(file));
		} catch (UnsupportedAudioFileException e) {
			log.error("Could not play file: " + file.getAbsolutePath() + "!", e);
		} catch (IOException e) {
			log.error("Could not play file: " + file.getAbsolutePath() + "!", e);
		} catch (LineUnavailableException e) {
			log.error("Could not play file: " + file.getAbsolutePath() + "!", e);
		}
	}

	public void playSound(final URL url) {
		try {
			testPlay(AudioSystem.getAudioInputStream(url));
		} catch (UnsupportedAudioFileException e) {
			log.error("Could not play file: " + url.getFile() + "!", e);
		} catch (IOException e) {
			log.error("Could not play file: " + url.getFile() + "!", e);
		} catch (LineUnavailableException e) {
			log.error("Could not play file: " + url.getFile() + "!", e);
		}
	}

	private void testPlay(AudioInputStream ais) throws IOException, LineUnavailableException
	{
	    AudioInputStream din = null;
	    AudioFormat baseFormat = ais.getFormat();
	    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                          baseFormat.getSampleRate(),
                                          16,
                                          baseFormat.getChannels(),
                                          baseFormat.getChannels() * 2,
                                          baseFormat.getSampleRate(),
                                          false);
	    din = AudioSystem.getAudioInputStream(decodedFormat, ais);
	    // Play now.
	    rawplay(decodedFormat, din);
	    ais.close();
	}

	private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException,                                                                                                LineUnavailableException
	{
	  byte[] data = new byte[4096];
	  SourceDataLine line = getLine(targetFormat);
	  if (line != null)
	  {
	    // Start
	    line.start();
	    int numBytesRead = 0;
	    while (numBytesRead != -1)
	    {
	        numBytesRead = din.read(data, 0, data.length);
	        if (numBytesRead != -1) {
	        	line.write(data, 0, numBytesRead);
	        }
	    }
	    // Stop
	    line.drain();
	    line.stop();
	    line.close();
	    din.close();
	  }
	}

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
	  SourceDataLine res = null;
	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	  res = (SourceDataLine) AudioSystem.getLine(info);
	  res.open(audioFormat);
	  return res;
	}
}
