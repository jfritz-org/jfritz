package de.moonflower.jfritz.autoupdate;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.utils.JFritzUtils;

public class JFritzUpdate extends AutoUpdateMainClass {

	private final static String className = "(JFritzUpdate) ";

	private boolean informOnNoUpdate;

	public JFritzUpdate(boolean informOnNoUpdate) {
		super("JFritzUpdate");
		Logger.on();

		setUpdateURL("http://update.jfritz.org/");
		setVersionFile("current.txt");
		setUpdateFile("update.txt");
		setPropertiesDirectory(".jfritz");

		String installDirectory = "";
		installDirectory = JFritzUtils.getFullPath(JFritzUtils.binID);
		installDirectory = installDirectory.substring(0, installDirectory.length()-10);
		//updateFile = installDirectory + "update.txt"; //?? why ??
		setInstallDirectory(installDirectory);
		setUpdateDirectory(installDirectory+"update");
		setDirectoriesZipedAsFilesEndWith(".zip");
		setDeleteListFile("deletefiles");

		this.informOnNoUpdate = informOnNoUpdate;

		File installDir = new File(installDirectory);
		if (!installDir.exists()) {
			installDir.mkdir();
		}
	}

	/**
	 * Lädt die aktuelle Version runter, sofern eine vorhanden ist
	 *
	 * @param update
	 */
	public void downloadNewFiles(Update update) {
		try {
			// Überprüfe auf neue Version
			CheckVersion checkVersionThread = new CheckVersion(
					update.getProgramVersion());
			Thread checkVersion = new Thread(checkVersionThread);
			checkVersion.start();

			// Warte, bis der Thread beendet ist
			checkVersion.join();

			if (checkVersionThread.isNewVersionAvailable()) {
				if (AutoUpdateGUI.showConfirmDialog() == JOptionPane.YES_OPTION) {

					// Lade Dateien herunter
					DownloadFiles downloadFilesThread = new DownloadFiles(
							checkVersionThread.getNewVersion());
					Thread downloadFiles = new Thread(downloadFilesThread);

					AutoUpdateGUI auGui = new AutoUpdateGUI(downloadFiles);
					auGui.setModal(true);
					downloadFilesThread.registerProgressListener(auGui);
					downloadFiles.start();
					auGui.setVisible(true);

					try {
						downloadFiles.join();
						if (downloadFilesThread.wasInterrupted()) {
							cleanupUpdateDirectory();
						}
						update.setProgramVersion(downloadFilesThread.getNewVersion());
						auGui.dispose();
					} catch (InterruptedException e) {
						Logger.err(className
								+ "DownloadFiles-Thread has been interrupted");
			        	Thread.currentThread().interrupt();
					}
				}
			} else {
				// gib hier die Meldung aus, dass keine neue Version gefunden
				// werden konnte
				if (informOnNoUpdate)
					AutoUpdateGUI.showNoNewVersionFoundDialog();
			}
		} catch (InterruptedException e) {
			// Thread wurde unterbrochen
			cleanupUpdateDirectory();
			Logger.err(className
					+ "CheckNewVersion-Thread has been interrupted");
        	Thread.currentThread().interrupt();
		}
	}

	/**
	 * Führt ein Update der Dateien aus.
	 *
	 */
	public void updateFiles() {
		ProcessUpdateFolder updateFolderThread = new ProcessUpdateFolder();
		Thread updateFolder = new Thread(updateFolderThread);
		updateFolder.start();

		try {
			updateFolder.join();

		} catch (InterruptedException e) {
			Logger.err(className
					+ "ProcessUpdateFolder-Thread has been interrupted");
			e.printStackTrace();
        	Thread.currentThread().interrupt();
		}
		if (updateFolderThread.wasUpdateSuccessfull())
			AutoUpdateGUI.showUpdateSuccessfulMessage();
	}

	private void updateJFritz() {
		Update update = new Update();
		update.loadSettings();
		if (update.getUpdateOnStart())
			downloadNewFiles(update);
		updateFiles();

		update.saveSettings();
	}

	/**
	 * Löscht alle Dateien im Update-Verzeichnis ausser der updateFile
	 *
	 */
	private void cleanupUpdateDirectory() {
		Logger.msg(className + "Cleaning up update directory");
		File upDir = new File(getUpdateDirectory());
		UpdateUtils.deleteTreeWithoutFile(upDir, getUpdateFile());
	}

	public static void main(String[] args) {
		JFritzUpdate jfritzUpdate = new JFritzUpdate(false);
		jfritzUpdate.updateJFritz();
		jfritzUpdate = null;

		startJFritz(args);
	}

	/**
	 * Startet eine Instanz von JFritz aus der Datei jfritz-internals.jar
	 *
	 * @param args
	 *            Die Kommandozeilenparameter
	 */
	private static void startJFritz(String[] args) {

		String installDirectory = JFritzUtils.getFullPath(JFritzUtils.binID);
		installDirectory = installDirectory.substring(0, installDirectory.length()-10);
		File jfritzJAR = new File(installDirectory + "jfritz-internals.jar");

		if (!jfritzJAR.exists()) {
			JOptionPane.showMessageDialog(null, UpdateLocale
					.getMessage("wrongWorkingDirectory"), UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
			throw new UpdateException(
					"Wrong working directory! Could not find jfritz-internals.jar. Searched in " + installDirectory);
		}

		Class<?>[] parameterTypes = new Class[] { String[].class };
		Object[] arguments = new Object[] { args };

		try {
			final URL jfritzInternalsJARurl = new URL("file", "localhost",
					jfritzJAR.getAbsolutePath());
			URLClassLoader loader = new URLClassLoader(
					new URL[] { jfritzInternalsJARurl });
			final Class<?> jfritzInternalsJAR = loader
					.loadClass("de.moonflower.jfritz.Main");
			Method mainMethod = jfritzInternalsJAR.getMethod("main",
					parameterTypes);
			mainMethod.invoke(args, arguments);
		} catch (MalformedURLException e) {
			Logger.err(className + "ERROR: malformed URL: "+ e.toString());
			JOptionPane.showMessageDialog(null, "Malformed URL", UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (ClassNotFoundException e) {
			Logger.err(className + "ERROR: class not found: " + e.toString());
			JOptionPane.showMessageDialog(null, UpdateLocale
					.getMessage("wrongWorkingDirectory"), UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (NoSuchMethodException e) {
			Logger.err(className + "ERROR: No such method: " + e.toString());
		} catch (IllegalArgumentException e) {
			Logger.err(className + "ERROR: illegal argument exception: " + e.toString());
		} catch (IllegalAccessException e) {
			Logger.err(className + "ERROR: illegal access exception:" + e.toString());
		} catch (InvocationTargetException e) {
			Logger.err(className + "ERROR: invocation target exception:" + e.toString());
		}
	}
}
