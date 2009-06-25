package de.moonflower.jfritz.autoupdate;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.utils.JFritzUtils;

public class JFritzUpdate {

	private final static String className = "(JFritzUpdate) ";

	private String updateURL = "http://update.jfritz.org/";

	private String versionFile = "current.txt";

	private String updateFile = "update.txt";

	private String propertiesDirectory = ".jfritz";

	private String updateDirectory = "./update";

	private String deleteListFile = "deletefiles";

	// TODO: noch auf den richtigen Pfad wechseln
	private String installDirectory = ".";

	private String directoriesZipedAsFilesEndWith = ".zip";

	private boolean informOnNoUpdate;

	public JFritzUpdate(boolean informOnNoUpdate) {
		Logger.on();
		this.informOnNoUpdate = informOnNoUpdate;
		installDirectory = JFritzUtils.getFullPath(JFritzUtils.binID);
		installDirectory = installDirectory.substring(0, installDirectory.length()-10);
		//updateFile = installDirectory + "update.txt"; //?? why ??
		updateDirectory = installDirectory+"update";
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
			CheckVersionThread checkVersionThread = new CheckVersionThread(
					update.getProgramVersion(), updateURL, versionFile);
			checkVersionThread.start();

			// Warte, bis der Thread beendet ist
			checkVersionThread.join();

			if (checkVersionThread.isNewVersionAvailable()) {
				if (AutoUpdateGUI.showConfirmDialog() == JOptionPane.YES_OPTION) {

					// Lade Dateien herunter
					DownloadFilesThread downloadFilesThread = new DownloadFilesThread(
							checkVersionThread.getNewVersion(), updateURL,
							updateFile, updateDirectory, deleteListFile);
					AutoUpdateGUI auGui = new AutoUpdateGUI(downloadFilesThread);
					auGui.setModal(true);
					downloadFilesThread.registerProgressListener(auGui);
					downloadFilesThread.start();
					auGui.setVisible(true);

					try {
						downloadFilesThread.join();
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
		ProcessUpdateFolderThread updateFolderThread = new ProcessUpdateFolderThread(
				updateDirectory, updateFile, installDirectory, deleteListFile,
				directoriesZipedAsFilesEndWith);
		updateFolderThread.start();

		try {
			updateFolderThread.join();

		} catch (InterruptedException e) {
			Logger.err(className
					+ "ProcessUpdateFolder-Thread has been interrupted");
			e.printStackTrace();
        	Thread.currentThread().interrupt();
		}
		if (updateFolderThread.wasUpdateSuccessfull())
			AutoUpdateGUI.showUpdateSuccessfulMessage();
	}

	/**
	 * Liefert das Verzeichnis, in dem die Einstellungen für das Update
	 * gespeichert werden
	 *
	 * @return
	 */
	public String getPropertiesDirectory() {
		return propertiesDirectory;
	}

	private void updateJFritz() {
		Update update = new Update(propertiesDirectory);
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
		File upDir = new File(updateDirectory);
		UpdateUtils.deleteTreeWithoutFile(upDir, updateFile);
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

		Class[] parameterTypes = new Class[] { String[].class };
		Object[] arguments = new Object[] { args };

		try {
			final URL jfritzInternalsJARurl = new URL("file", "localhost",
					jfritzJAR.getAbsolutePath());
			URLClassLoader loader = new URLClassLoader(
					new URL[] { jfritzInternalsJARurl });
			final Class jfritzInternalsJAR = loader
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
