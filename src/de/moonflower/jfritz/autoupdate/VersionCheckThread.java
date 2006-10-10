package de.moonflower.jfritz.autoupdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Dieser Thread überprüft, ob eine neue JFritz Version verfügbar ist
 *
 * Ist eine neue Version verfügbar, werden die neuen Dateien heruntergeladen und
 * die alten Dateien werden gelöscht bzw. mit den neuen Versionen überschrieben
 *
 *
 * @author Bastian Schaefer
 *
 */
public class VersionCheckThread implements Runnable {

	// URL zu der Datei, die die Versionsinformationen enthält
	private static final String versionURL = "http://www.jfritz.org/update/current.txt";

	// URL zu der Datei, die die Informationen über die neuen Dateien enthält
	private static final String remoteFilesURL = "http://www.jfritz.org/update/update.txt";

	// Enthält die neue Versionsnummer
	private String newJFritzVersion = "";

	// Eine Liste aller Dateien und Ordner auf dem update-server
	private List remoteFiles = new LinkedList();

	// Eine Liste aller lokalen Dateien und Ordner
	private List localFiles = new LinkedList();

	// Alle Dateien, die erneuert werden müssen
	private List updateFiles = new LinkedList();

	private boolean informNoNewVersion;

	private String jfritzHomedir;

	private int updateFileSize;

	public VersionCheckThread(boolean informNoNewVersion) {
		this.informNoNewVersion = informNoNewVersion;
		jfritzHomedir = JFritzUtils.getFullPath(".update");
		jfritzHomedir = jfritzHomedir.substring(0, jfritzHomedir.length() - 7);
	}

	public void run() {
		updateFileSize = 0;
		if (checkForNewVersion()) {
			Object[] options = { Main.getMessage("yes"),
					Main.getMessage("no") };
			int ok = JOptionPane.showOptionDialog(JFritz.getJframe(), Main
					.getMessage("new_version_text"), Main
					.getMessage("new_version"), JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, // don't use a
					// custom Icon
					options, // the titles of buttons
					options[0]); // default button title
			if (ok == JOptionPane.YES_OPTION) {
				getRemoteFiles();
				getLocalFiles();
				analyseRemoteFiles();
				analyseLocalFiles();
				processUpdateList();

				// Ask for restarting JFritz
				ok = JOptionPane.showOptionDialog(JFritz.getJframe(), Main
						.getMessage("new_version_restart"), Main
						.getMessage("new_version"), JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null, // don't use a
						// custom Icon
						options, // the titles of buttons
						options[0]); // default button title
				if (ok == JOptionPane.YES_OPTION) {
					// TODO: restart JFritz;
				}
			}
		} else if (informNoNewVersion == true) {
			JOptionPane.showMessageDialog(JFritz.getJframe(), Main
					.getMessage("no_new_version_found"), "JFritz",
					JOptionPane.INFORMATION_MESSAGE);
		}

		Debug.msg("CheckVersionThread: CheckVersionThread done");
	}

	/**
	 * Überprüft, ob eine neue Version verfügbar ist
	 *
	 * @return true, wenn neue Version verfügbar
	 */
	public boolean checkForNewVersion() {
		URL url = null;
		String urlstr = versionURL; //$NON-NLS-1$

		boolean newVersion = false;

		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					Debug
							.msg("CheckVersionThread: Checking for new JFritz-Version...");
					con = url.openConnection();
					BufferedReader d = new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					// Get remote version
					String str = d.readLine();
					// Format remote version as 0.621
					String remoteVersion = str.replaceFirst("\\.", "\\|")
							.replaceAll("\\.", "").replaceFirst("\\|", "\\.");

					// Format local version as 0.621
					String localVersion = Main.PROGRAM_VERSION
							.replaceFirst("\\.", "\\|").replaceAll("\\.", "")
							.replaceFirst("\\|", "\\.");

					if (Double.valueOf(remoteVersion).compareTo(
							Double.valueOf(localVersion)) > 0) {
						newJFritzVersion = str;
						newVersion = true;
					}

					d.close();

				} catch (IOException e1) {
					Debug.err("CheckVersionThread: Error while retrieving "
							+ urlstr
							+ " (possibly no connection to the internet)"); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("CheckVersionThread: URL invalid: " + urlstr); //$NON-NLS-1$
		}
		return newVersion;

	}

	/**
	 * Lädt die Remote-Dateiliste runter
	 *
	 */
	private void getRemoteFiles() {
		String urlstr = remoteFilesURL; //$NON-NLS-1$
		URL url = null;
		try {
			url = new URL(urlstr);
			if (url != null) {
				URLConnection con;
				try {
					Debug.msg("CheckVersionThread: getting remote files...");
					con = url.openConnection();
					BufferedReader d = new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					String line = "";
					// Füge Dateien zur filesToDownload-Liste hinzu
					while (null != (line = d.readLine())) {
						String[] split = line.split(";");
						if (split.length < 3) {
							Debug
									.err("Check new JFritz-version: split-length < 3");
						}
						UpdateFile remoteFile = new UpdateFile(split[0],
								split[1], Integer.parseInt(split[2]));

						remoteFiles.add(remoteFile);
					}
					d.close();
				} catch (IOException e1) {
					Debug.err("CheckVersionThread: Error while retrieving "
							+ urlstr
							+ " (possibly no connection to the internet)"); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("CheckVersionThread: URL invalid: " + urlstr); //$NON-NLS-1$
		}
	}

	/**
	 * Lädt die Local-Dateiliste in den Speicher
	 *
	 */
	private void getLocalFiles() {
		try {
			Debug.msg("CheckVersionThread: getting local files...");

			File localVersionFile = new File(jfritzHomedir
					+ System.getProperty("file.separator") + "current.txt");

			BufferedReader pw = new BufferedReader(new InputStreamReader(
					new FileInputStream(localVersionFile), "UTF8"));

			String line = "";
			// Füge Dateien zur filesToDownload-Liste hinzu
			while (null != (line = pw.readLine())) {
				String[] split = line.split(";");
				if (split.length < 3) {
					Debug.err("Check new JFritz-version: split-length < 3");
				}
				UpdateFile localFile = new UpdateFile(split[0], split[1],
						Integer.parseInt(split[2]));

				localFiles.add(localFile);
			}
			pw.close();
		} catch (IOException e1) {
			Debug.err("CheckVersionThread: Error opening local version file"); //$NON-NLS-1$
		}
	}

	/**
	 * Analysiert die remote-Dateiliste
	 *
	 * Wenn eine neue Datei in der remote-Dateiliste vorhanden ist oder sich der
	 * Dateihash verändert hat, wird die Datei zur Update-Liste hinzugefügt
	 *
	 */
	private void analyseRemoteFiles() {
		for (int i = 0; i < remoteFiles.size(); i++) {
			UpdateFile currentRemoteFile = (UpdateFile) remoteFiles.get(i);
			Debug.msg("CheckVersionThread: Analysing file "
					+ currentRemoteFile.getName());
			if (localFiles.contains(currentRemoteFile)) {
				UpdateFile localFile = (UpdateFile) localFiles.get(localFiles
						.indexOf(currentRemoteFile));
				if (localFile.getHash().equals(currentRemoteFile.getHash())) {
					// Kein Update notwendig, <asm>nop</asm> ;-)
				} else {
					updateFiles.add(currentRemoteFile);
					updateFileSize += currentRemoteFile.getSize();
				}
			} else {
				updateFiles.add(currentRemoteFile);
				updateFileSize += currentRemoteFile.getSize();
			}
		}
	}

	/**
	 * Analysiert die locale Dateiliste
	 *
	 * Löscht alle nicht mehr benötigten Dateien/Ordner von der Festplatte
	 *
	 */
	private void analyseLocalFiles() {
		for (int i = 0; i < localFiles.size(); i++) {
			UpdateFile currentLocalFile = (UpdateFile) localFiles.get(i);
			if (!remoteFiles.contains(currentLocalFile)) {
				deleteFile(currentLocalFile.getName());
			}
		}
	}

	private void processUpdateList() {
		for (int i = 0; i < updateFiles.size(); i++) {
			UpdateFile currentFile = (UpdateFile) updateFiles.get(i);
			Debug.msg("Update file " + currentFile.getName());
			deleteFile(currentFile.getName());
		}
	}

	private void deleteFile(String fileName) {
		if (fileName.endsWith(".zip")) {
			// Es ist ein Verzeichnis. Lösche das Verzeichnis
			String dirName = fileName.substring(0, fileName.length() - 4);
			Debug.msg("Deleting directory " + jfritzHomedir
					+ System.getProperty("file.separator") + dirName);
			File dir = new File(jfritzHomedir
					+ System.getProperty("file.separator") + dirName);
			deleteTree(dir);
		} else {
			Debug.msg("Deleting file " + jfritzHomedir
					+ System.getProperty("file.separator") + fileName);
			File file = new File(jfritzHomedir
					+ System.getProperty("file.separator") + fileName);
			file.delete();
		}
	}

	private static void deleteTree(File path) {
		File[] fileList = path.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			if (file.isDirectory())
				deleteTree(file);
			System.err.println("Deleting file: " + file.getAbsolutePath());
			file.delete();
		}
		path.delete();
	}

	private void downloadFile(String fileName) {
		URL url = null;

		String urlstr = "http://www.jfritz.org/update/" + newJFritzVersion
				+ "/" + fileName;
		try {
			Debug.msg("CheckVersionThread: Download new file from " + urlstr);
			url = new URL(urlstr);
			URLConnection conn = url.openConnection();

			BufferedInputStream in = new BufferedInputStream(conn
					.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(jfritzHomedir + JFritzUtils.FILESEP
							+ fileName));

			int i = in.read();
			while (i != -1) {
				out.write(i);
				i = in.read();
			}
			in.close();
			out.flush();
			out.close();
			Debug.msg("CheckVersionThread: Saved file " + fileName + " to "
					+ jfritzHomedir + JFritzUtils.FILESEP + fileName);
		} catch (Exception e) {
			Debug.err("CheckVersionThread: Error (" + e.toString() + ")");
		}
	}

}
