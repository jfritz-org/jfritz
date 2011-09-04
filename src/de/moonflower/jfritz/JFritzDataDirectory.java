package de.moonflower.jfritz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import jd.nutils.OSDetector;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;

public class JFritzDataDirectory {
	private final static Logger log = Logger.getLogger(JFritzDataDirectory.class);
	private String SAVE_DIR = null;
	private String SAVE_DIR_TEXT = "Save_Directory=";

	private static JFritzDataDirectory INSTANCE;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	private JFritzDataDirectory() {
		if (SAVE_DIR == null) {
			SAVE_DIR = getDefaultSaveDirectory();
		}
	}

	public static JFritzDataDirectory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JFritzDataDirectory();
		}
		return INSTANCE;
	}

	public String getDataDirectory() {
		return SAVE_DIR;
	}

	public String getDefaultSaveDirectory() {
		String defDir = "";
		if (OSDetector.isWindows()) {
			defDir = System.getenv("APPDATA") + File.separator + "JFritz" + File.separator;
		} else if (OSDetector.isMac()) {
			defDir = System.getProperty("user.home") + "/Library/Application Support/JFritz/";
		} else if (OSDetector.isLinux()) {
			defDir = System.getProperty("user.home") + File.separator + Main.JFRITZ_HIDDEN_DIR + File.separator;
		} else {
			log.warn("Could not detect OS! Using default directory");
		}
		log.debug("Default data directory: " + defDir);
		return defDir;
	}

	/**
	 * Funktion reads the user specified save location from a simple text file
	 * If any error occurs the function bails out and uses the current directory
	 * as the data directory, as the functionality was in JFritz < 0.6.0
	 *
	 * @author Brian Jensen
	 *
	 */
	public void loadSaveDir() {
		BufferedReader br = null;
		try {
			final String jfritzConfigFile = Main.USER_DIR + File.separator + Main.USER_JFRITZ_FILE;
			br = new BufferedReader(new FileReader(jfritzConfigFile));
			String line = br.readLine();
			if (line == null) {
				br.close();
				log.warn("File '" + jfritzConfigFile + "' is empty");
				SAVE_DIR = getDefaultSaveDirectory();
			} else {
				while (line != null) {
					if (line.contains("=")) {
						String[] entries = line.split("=");
						if (!entries[1].equals("")) {
							if (entries[0].equals("Save_Directory")) {
								SAVE_DIR = entries[1];
								File file = new File(SAVE_DIR);
								if (!file.isDirectory())
									SAVE_DIR = getDefaultSaveDirectory();
								else if (!SAVE_DIR.endsWith(File.separator))
									SAVE_DIR = SAVE_DIR + File.separator;
							}
						}
					}
					line = br.readLine();
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("Error processing the user save location(File not found), using defaults");
			SAVE_DIR = getDefaultSaveDirectory();
			// If something happens, just bail out and use the standard dir
		} catch (IOException ioe) {
			log.warn("Error processing the user save location, using defaults");
			SAVE_DIR = getDefaultSaveDirectory();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				log.error("Error closing stream");
			}
		}
		log.debug("Loaded data directory: " + SAVE_DIR);
	}

	/**
	 * This function writes a file $HOME/.jfritz/jfritz.txt, which contains the
	 * location of the folder containing jfritz's data If the dir $HOME/.jfritz
	 * does not exist, it is created if the save location isnt a directory, then
	 * the default data directory is used
	 *
	 * @author Brian Jensen
	 *
	 */
	public void writeSaveDir() {
		try {

			// if $HOME/.jfritz doesn't exist create it
			File file = new File(Main.USER_DIR);
			if (!file.isDirectory() && !file.isFile())
			{
				file.mkdir();
			}
			final String jfritzConfigFile = Main.USER_DIR + File.separator + Main.USER_JFRITZ_FILE;
			BufferedWriter bw = new BufferedWriter(new FileWriter(jfritzConfigFile, false));

			// make sure the user didn't screw something up
			if (!SAVE_DIR.endsWith(File.separator))
			{
				SAVE_DIR = SAVE_DIR + File.separator;
			}

			file = new File(SAVE_DIR);
			if (!file.exists()) {
				file.mkdir();
			}

			if (!file.isDirectory())
			{
				SAVE_DIR = getDefaultSaveDirectory();
			}

			bw.write("[Settings]");
			bw.newLine();
			bw.write(SAVE_DIR_TEXT + SAVE_DIR);
			bw.newLine();
			bw.close();
			log.debug("Successfully wrote data dir '" + SAVE_DIR + "' to '" + jfritzConfigFile + "'");

		} catch (Exception e) {
			SAVE_DIR = getDefaultSaveDirectory();
			log.warn("Error writing data dir to disk, reverting back to default data dir: " + SAVE_DIR);
			// if there was an error, bail out and revert to the default save location
		}
	}

	public void changeSaveDir(String path) {
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}

		File dst = new File(path);
		if (!dst.isFile())
		{
			File src = new File(SAVE_DIR);
			try {
				FileUtils.moveDirectory(src, dst);
				saveNewDirectory(path);
//				// update logger
//				Logger.getRootLogger().removeAppender("log4j-file-appender");
//				initLog4jAppender();
			} catch (IOException e) {
				log.error("Could not move data directory!", e);
				try {
					FileUtils.copyDirectory(src, dst);

					log.debug("Changed data directory from '" + SAVE_DIR + "' to '" + path + "'");
					saveNewDirectory(path);
					try {
						FileUtils.deleteDirectory(src);
					} catch (IOException e1) {
						Debug.errDlg("Could not delete old data directory '" + SAVE_DIR + "'!\n" +
								e.getMessage());
					}
				} catch (IOException e1) {
					Debug.errDlg("Could not copy data directory from '" + SAVE_DIR + "' to '" + path + "'!\n" +
							e.getMessage());
				}
			}
		} else {
			Debug.errDlg("You selected a file, please choose a directory!");
		}
	}

	private void saveNewDirectory(String path) {
		SAVE_DIR = path;
		writeSaveDir();
	}
}
