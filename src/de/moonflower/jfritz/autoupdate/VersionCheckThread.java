package de.moonflower.jfritz.autoupdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
 * Ist eine neue Version verfügbar, werden die neuen Dateien in den
 * update-Ordner heruntergeladen und eine Datei deletefiles erstellt, in der die
 * zu löschenden Dateien und Ordner drinstehen
 *
 * @author Bastian Schaefer
 *
 */
public class VersionCheckThread implements Runnable {

	// URL zu der Datei, die die Versionsinformationen enthält
	private static final String versionURL = "http://update.jfritz.org/current.txt";

	// URL zu der Datei, die die Informationen über die neuen Dateien enthält
	private static final String remoteFilesURL = "http://update.jfritz.org/update.txt";

	// Enthält die neue Versionsnummer
	private String newJFritzVersion = "";

	// Eine Liste aller Dateien und Ordner auf dem update-server
	private List remoteFiles = new LinkedList();

	// Eine Liste aller lokalen Dateien und Ordner
	private List localFiles = new LinkedList();

	// Alle Dateien, die erneuert werden müssen
	private List updateFiles = new LinkedList();

	private boolean informNoNewVersion;

	private String updateDirectory;

	private int updateFileSize;

	private AutoUpdateGUI auGui;

	public VersionCheckThread(boolean informNoNewVersion) {
		this.informNoNewVersion = informNoNewVersion;
		updateDirectory = Main.getHomeDirectory() + JFritzUtils.FILESEP + "update";
	}

	public void run() {
		updateFileSize = 0;
		if (checkForNewVersion()) {
			Object[] options = { Main.getMessage("yes"), Main.getMessage("no") };
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
				auGui = new AutoUpdateGUI();
				auGui.setVisible(true);
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
					//Main.exit(0);
					System.exit(0); //FIXME later
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
					String localVersion = Main.PROGRAM_VERSION.replaceFirst(
							"\\.", "\\|").replaceAll("\\.", "").replaceFirst(
							"\\|", "\\.");

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

			File localVersionFile = new File(Main.getHomeDirectory()
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
	 * Nicht mehr benötigte Dateien werden in die "deletefiles"-Datei
	 * geschrieben, sodass diese beim nächsten Start von JFritz gelöscht werden
	 *
	 */
	private void analyseLocalFiles() {
		System.err.println(updateDirectory);
		File updateDir = new File(updateDirectory);
		updateDir.mkdir();
		File deleteFile = new File(updateDirectory
				+ System.getProperty("file.separator") + "deletefiles");
		deleteFile.delete();
		try {
			deleteFile.createNewFile();
		} catch (IOException e1) {
			System.err.println("Could not create version file: "
					+ deleteFile.getAbsolutePath());
		}

		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(deleteFile), "UTF8"));
			for (int i = 0; i < localFiles.size(); i++) {
				UpdateFile currentLocalFile = (UpdateFile) localFiles.get(i);
				if (!remoteFiles.contains(currentLocalFile)) {
					pw.write(currentLocalFile.getName());
				}
			}
			pw.flush();
			pw.close();
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Could not write file with UTF8 encoding");
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file " + deleteFile);
		} catch (IOException e) {
			System.err.println("Could not write to file " + deleteFile);
		}
	}

	private void processUpdateList() {
		int totalSize = 0;
		for (int i = 0; i < updateFiles.size(); i++) {
			UpdateFile currentFile = (UpdateFile) updateFiles.get(i);
			totalSize += currentFile.getSize();
		}
		auGui.setTotalProgress(0);
		auGui.setTotalSize(totalSize);
		for (int i = 0; i < updateFiles.size(); i++) {
			UpdateFile currentFile = (UpdateFile) updateFiles.get(i);
			Debug.msg("Update file " + currentFile.getName());
			auGui.setCurrentFile(currentFile.getName());
			auGui.setCurrentFileProgress(0);
			auGui.setCurrentFileSize(currentFile.getSize());
			downloadFile(currentFile.getName());
		}
	}

	private void downloadFile(String fileName) {
		URL url = null;

		String urlstr = "http://www.jfritz.org/update/" + newJFritzVersion
				+ "/" + fileName;
		int position = 0;
		try {
			Debug.msg("CheckVersionThread: Download new file from " + urlstr);
			url = new URL(urlstr);
			URLConnection conn = url.openConnection();

			BufferedInputStream in = new BufferedInputStream(conn
					.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(updateDirectory + JFritzUtils.FILESEP
							+ fileName));

			int i = in.read();
			while (i != -1) {
				out.write(i);
				i = in.read();
				position += 1;
				if ( position % 100 == 0) {
					auGui.setCurrentFileProgress(position);
					auGui.setTotalProgress(auGui.getTotalProgress()+100);
				}
			}
			auGui.setCurrentFileProgress(position);
			auGui.setTotalProgress(auGui.getTotalProgress()+100);
			in.close();
			out.flush();
			out.close();
			Debug.msg("CheckVersionThread: Saved file " + fileName + " to "
					+ updateDirectory + JFritzUtils.FILESEP + fileName);
		} catch (Exception e) {
			Debug.err("CheckVersionThread: Error (" + e.toString() + ")");
		}
	}

}
