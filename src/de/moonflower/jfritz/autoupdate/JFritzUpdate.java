package de.moonflower.jfritz.autoupdate;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.swing.JOptionPane;

import jd.nutils.OSDetector;

import de.moonflower.jfritz.ProgramConstants;
import de.moonflower.jfritz.utils.JFritzUtils;

public class JFritzUpdate extends AutoUpdateMainClass {

	private boolean informOnNoUpdate;

	private Update update;

	private String newVersion = "";

	private Vector<String> changelog;

	private boolean askForConfirmation = true;

	private boolean shutdownNecessary = false;

	public JFritzUpdate(boolean informOnNoUpdate, boolean betaUploadUrl) {
		super("JFritzUpdate");
		Logger.on();

		if (betaUploadUrl)
		{
			setUpdateURL("http://jfritz.robotniko.de/update/");
		} else {
			setUpdateURL("http://update.jfritz.org/");
		}
		setVersionFile("current.txt");
		setUpdateFile("update.txt");
		setChangelogFile("changelog.txt");
		setPropertiesDirectory(".jfritz");

		String installDirectory = "";
		installDirectory = JFritzUtils.getFullPath(JFritzUtils.binID);
		installDirectory = installDirectory.substring(0, installDirectory.length()-10);
		setInstallDirectory(installDirectory);
		setUpdateDirectory(installDirectory+"update");
		setDirectoriesZipedAsFilesEndWith(".zip");
		setDeleteListFile("deletefiles");

		this.informOnNoUpdate = informOnNoUpdate;

		File installDir = new File(installDirectory);
		if (!installDir.exists()) {
			installDir.mkdir();
		}

		update = new Update();
		update.loadSettings();
		update.setProgramVersion(ProgramConstants.PROGRAM_VERSION);

		askForConfirmation = true;

	}

	/**
	 * Führt ein Update der Dateien aus.
	 *
	 */
	private void updateFiles() {
		ProcessUpdateFolder updateFolderThread = new ProcessUpdateFolder();
		Thread updateFolder = new Thread(updateFolderThread);
		updateFolder.start();

		try {
			updateFolder.join();
			update.setProgramVersion(newVersion);
			update.saveSettings();
		} catch (InterruptedException e) {
			logError("ProcessUpdateFolder-Thread has been interrupted");
			e.printStackTrace();
        	Thread.currentThread().interrupt();
		}
		if (updateFolderThread.wasUpdateSuccessfull())
			AutoUpdateGUI.showUpdateSuccessfulMessage();
	}

	private void unconfirmedUpdateJFritz() {
		if (isUpdateAvailable()) {
			downloadFiles();
			updateFiles();
		} else {
			// gib hier die Meldung aus, dass keine neue Version gefunden
			// werden konnte
			if (informOnNoUpdate)
				AutoUpdateGUI.showNoNewVersionFoundDialog();
		}
	}

	public void confirmedUpdateJFritz() {
		shutdownNecessary = false;
		if (isUpdateAvailable()) {

			AutoUpdateGUI.showNotifyUpdateDialog();

//			if (askDoUpdate())
//			{
//				if (OSDetector.isWindows() &&
//						(OSDetector.getOSID() == OSDetector.OS_WINDOWS_XP)
//						|| (OSDetector.getOSID() == OSDetector.OS_WINDOWS_VISTA)
//						|| (OSDetector.getOSID() == OSDetector.OS_WINDOWS_7))
//				{
//					try {
//						shutdownNecessary = true;
//						JOptionPane.showMessageDialog(null, "JFritz muss als Administrator gestartet werden, damit das Update erfolgen kann");
//						startUpdateAsAdmin();
//					} catch (Exception e) {
//						logError("Start update as ADMIN failed");
//					}
//				}
//				else
//				{
//					downloadFiles();
//					updateFiles();
//				}
//			}
		} else {
			// gib hier die Meldung aus, dass keine neue Version gefunden
			// werden konnte
			if (informOnNoUpdate)
				AutoUpdateGUI.showNoNewVersionFoundDialog();
		}
	}

	private boolean isUpdateAvailable()
	{
		// Überprüfe auf neue Version
		CheckVersion checkVersionThread = new CheckVersion(
				update.getProgramVersion());
		Thread checkVersion = new Thread(checkVersionThread);
		checkVersion.start();

		// Warte, bis der Thread beendet ist
		try {
			checkVersion.join();
			newVersion = checkVersionThread.getNewVersion();
			changelog = checkVersionThread.getChangelog();
			return checkVersionThread.isNewVersionAvailable();
		} catch (InterruptedException e) {
			// Thread wurde unterbrochen
			cleanupUpdateDirectory();
			logError("CheckNewVersion-Thread has been interrupted");
        	Thread.currentThread().interrupt();
		}

		return false;
	}

	private boolean askDoUpdate()
	{
		AutoUpdateGUI.setChangelog(changelog);
		return AutoUpdateGUI.showConfirmUpdateDialog() == JOptionPane.YES_OPTION;
	}

	private void downloadFiles()
	{
		// Lade Dateien herunter
		DownloadFiles downloadFilesThread = new DownloadFiles(newVersion);
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
			logError("DownloadFiles-Thread has been interrupted");
        	Thread.currentThread().interrupt();
		}
	}

	/**
	 * Löscht alle Dateien im Update-Verzeichnis ausser der updateFile
	 *
	 */
	private void cleanupUpdateDirectory() {
		logMessage("Cleaning up update directory");
		File upDir = new File(getUpdateDirectory());
		UpdateUtils.deleteTreeWithoutFile(upDir, getUpdateFile());
	}

	private boolean getUpdateOnStart() {
		return update.getUpdateOnStart();
	}

	private void checkParameters(String[] args) {
		if (args.length > 0)
		{
			for (int i=0; i<args.length; i++)
			{
				if (args[i].equals("download"))
				{
					askForConfirmation = false;
				} else if (args[i].equals("--updateBeta"))
				{
					setUpdateURL("http://jfritz.robotniko.de/update/");
				}
			}
		}
	}
	public static void main(String[] args) {
		JFritzUpdate jfritzUpdate = new JFritzUpdate(false, false);
		jfritzUpdate.checkParameters(args);

		if (jfritzUpdate.askForConfirmation == false)
		{
			jfritzUpdate.unconfirmedUpdateJFritz();
		}
		else
		{
			if (jfritzUpdate.getUpdateOnStart())
			{
				jfritzUpdate.confirmedUpdateJFritz();
				if (jfritzUpdate.isShutdownNecessary())
				{
					System.exit(0);
				}
			}
		}

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
		final String className = "JFritzUpdate";

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
			JOptionPane.showMessageDialog(null, "No such method", UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (IllegalArgumentException e) {
			Logger.err(className + "ERROR: illegal argument exception: " + e.toString());
			JOptionPane.showMessageDialog(null, "Illegal arguments", UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (IllegalAccessException e) {
			Logger.err(className + "ERROR: illegal access exception:" + e.toString());
			JOptionPane.showMessageDialog(null, "Illegal access", UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (InvocationTargetException e) {
			Logger.err(className + "ERROR: invocation target exception:" + e.toString());
			JOptionPane.showMessageDialog(null, "Invocation exception", UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception " + e.getMessage(), UpdateLocale
					.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setProgramVersion(final String version)
	{
		update.setProgramVersion(version);
	}

    private void startUpdateAsAdmin() throws Exception
    {
    	String fileName = getInstallDirectory()+"autoupdate.exe";
    	String[] commands = {"cmd", "/c", "start", "\"Autoupdate\"",fileName, "download"};

  	  	ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Process p = pb.start();
    }


    public boolean isShutdownNecessary()
    {
    	return shutdownNecessary;
    }
}
