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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Dieser Thread überprüft, ob eine neue Programmversion verfügbar ist
 *
 * Ist eine neue Version verfügbar, werden die neuen Dateien in den
 * update-Ordner heruntergeladen und eine Datei deleteList erstellt, in der die
 * zu löschenden Dateien und Ordner drinstehen
 *
 * @author Robert Palmer
 *
 */
public class DownloadFilesThread extends Thread {

	private final static String threadName = "(DownloadFilesThread) ";

	// URL zum Update-Ordner auf der Homepage
	private String updateURL = "";

	// Datei, die auf der Homepage die Informationen über die neuen Dateien
	// enthält
	private String updateFile = "";

	// Datei, die die zu löschenden Dateien enthält
	private String deleteListFile = "";

	// Das Verzeichnis, in das die neuen Dateien heruntergeladen werden
	private String updateDirectory = "";

	// Enthält die neue Versionsnummer
	private String newVersion = "";

	// Eine Liste aller Dateien und Ordner auf dem update-server
	private List<UpdateFile> remoteFilesList;

	// Eine Liste aller lokalen Dateien und Ordner
	private List<UpdateFile> localFilesList;

	// Alle Dateien, die erneuert werden müssen
	private List<UpdateFile> updateFilesList;

	// Liste alles Listener, die auf Events reagieren wollen
	private Vector<DownloadFilesListener> listener;

	// Wird auf true gesetzt, wenn der Thread von aussen unterbrochen wird
	private boolean wasInterrupted = false;

	public DownloadFilesThread(String newVersion, String updateURL,
			String updateFile, String updateDirectory, String deleteListFile) {
		remoteFilesList = new LinkedList<UpdateFile>();
		localFilesList = new LinkedList<UpdateFile>();
		updateFilesList = new LinkedList<UpdateFile>();
		listener = new Vector<DownloadFilesListener>();
		this.newVersion = newVersion;
		this.updateURL = updateURL;
		this.updateFile = updateFile;
		this.updateDirectory = updateDirectory;
		this.deleteListFile = deleteListFile;
	}

	public void run() {
		wasInterrupted = false;
		System.out.println(threadName + "Downloading files...");

		getRemoteFileList();
		getLocalFileList();
		analyseRemoteFileList();
		analyseLocalFileList();
		processUpdateList();
		updateLocalFileList();

		System.out.println(threadName + "...done");
	}

	/**
	 * Setzt die URL zum Update-Ordner auf der Homepage
	 *
	 * @param URL
	 *            zum Update-Ordner auf der Homepage
	 */
	public void setUpdateURL(String updateURL) {
		if (!updateURL.endsWith("/"))
			updateURL.concat("/");
		this.updateURL = updateURL;
	}

	/**
	 * Setzt den Dateiname auf die Datei, die auf der Homepage die Informationen
	 * über die neuen Dateien enthält
	 *
	 * @param Dateiname
	 */
	public void setUpdateFile(String updateFile) {
		this.updateFile = updateFile;
	}

	/**
	 * Setzt das Verzeichnis, in das die neuen Dateien heruntergeladen werden
	 *
	 * @param updateDirectory
	 */
	public void setUpdateDirectory(String updateDirectory) {
		this.updateDirectory = updateDirectory;
	}

	/**
	 * Setzt den Dateinamen für die Datei, in der eine Liste der zu löschenden
	 * Dateien steht
	 *
	 * @param deleteListFile
	 */
	public void setDeleteListFile(String deleteListFile) {
		this.deleteListFile = deleteListFile;
	}

	/**
	 * Registriert einen Listener, der auf Events reagieren soll
	 *
	 * @param newListener
	 */
	public void registerProgressListener(DownloadFilesListener newListener) {
		listener.add(newListener);
	}

	/**
	 * Entfernt einen Listener, der auf Events reagiert
	 *
	 * @param removeListener
	 */
	public void removeProgressListener(DownloadFilesListener removeListener) {
		listener.remove(removeListener);
	}

	/**
	 * Liefert zurück, ob der Thread unterbrochen wurde
	 *
	 * @return
	 */
	public boolean wasInterrupted() {
		return wasInterrupted;
	}

	/**
	 * Setzt den Wert wasInterrupted
	 *
	 */
	private void setWasInterrupted() {
		wasInterrupted = true;
	}

	/**
	 * Lädt die Remote-Dateiliste runter
	 *
	 */
	private void getRemoteFileList() {
		String urlstr = updateURL + updateFile; //$NON-NLS-1$
		URL url = null;
		try {
			url = new URL(urlstr);
			if (url != null) {
				URLConnection con;
				try {
					System.out
							.println(threadName + "getting remote file list");
					con = url.openConnection();
					// 5 Sekunden-Timeout für den Verbindungsaufbau
					con.setConnectTimeout(5000);

					BufferedReader d = new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					String line = "";
					// Füge Dateien zur filesToDownload-Liste hinzu
					while (null != (line = d.readLine())) {
						String[] split = line.split(";");
						if (split.length < 3) {
							System.err
									.println(threadName + "split-length < 3");
						}
						UpdateFile remoteFile = new UpdateFile(split[0],
								split[1], Integer.parseInt(split[2]));

						remoteFilesList.add(remoteFile);
					}
					d.close();
				} catch (IOException e1) {
					System.err
							.println(threadName + "Error while retrieving "
									+ urlstr
									+ " (possibly no connection to the internet)"); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			System.err.println(threadName + "URL invalid: " + urlstr); //$NON-NLS-1$
		}
	}

	/**
	 * Lädt die Local-Dateiliste in den Speicher
	 *
	 */
	private void getLocalFileList() {
		try {
			System.out.println(threadName + "getting local file list");

			File localVersionFile = new File(updateDirectory
					+ System.getProperty("file.separator") + updateFile);

			BufferedReader pw = new BufferedReader(new InputStreamReader(
					new FileInputStream(localVersionFile), "UTF8"));

			String line = "";
			// Füge Dateien zur filesToDownload-Liste hinzu
			while (null != (line = pw.readLine())) {
				if ( !line.equals("")) {
					String[] split = line.split(";");
					if (split.length < 3) {
						System.err.println(threadName + "split-length < 3 for line " + line);
					}
					UpdateFile localFile = new UpdateFile(split[0], split[1],
							Integer.parseInt(split[2]));

					localFilesList.add(localFile);
				}
			}
			pw.close();
		} catch (IOException e1) {
			System.err
					.println(threadName + "Error opening local version file"); //$NON-NLS-1$
		}
	}

	/**
	 * Analysiert die remote-Dateiliste
	 *
	 * Wenn eine neue Datei in der remote-Dateiliste vorhanden ist oder sich der
	 * Dateihash verändert hat, wird die Datei zur Update-Liste hinzugefügt
	 *
	 */
	private void analyseRemoteFileList() {
		for (int i = 0; i < remoteFilesList.size(); i++) {
			UpdateFile currentRemoteFile = remoteFilesList.get(i);
			System.out.println(threadName + "Analysing file "
					+ currentRemoteFile.getName());
			if (localFilesList.contains(currentRemoteFile)) {
				UpdateFile localFile = localFilesList.get(localFilesList
						.indexOf(currentRemoteFile));
				if (localFile.getHash().equals(currentRemoteFile.getHash())) {
					// Kein Update notwendig, <asm>nop</asm> ;-)
				} else {
					updateFilesList.add(currentRemoteFile);
				}
			} else {
				updateFilesList.add(currentRemoteFile);
			}
		}
	}

	/**
	 * Analysiert die locale Dateiliste
	 *
	 * Nicht mehr benötigte Dateien werden in die deleteList-Datei geschrieben,
	 * sodass diese beim nächsten Start des Programms gelöscht werden
	 *
	 */
	private void analyseLocalFileList() {
		File updateDir = new File(updateDirectory);
		updateDir.mkdir();
		File deleteFile = new File(updateDirectory
				+ System.getProperty("file.separator") + deleteListFile);
		deleteFile.delete();
		try {
			deleteFile.createNewFile();
		} catch (IOException e1) {
			System.err.println(threadName + "Could not create version file: "
					+ deleteFile.getAbsolutePath());
		}

		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(deleteFile), "UTF8"));
			for (int i = 0; i < localFilesList.size(); i++) {
				UpdateFile currentLocalFile = localFilesList.get(i);
				if (!remoteFilesList.contains(currentLocalFile)) {
					pw.write(currentLocalFile.getName());
					pw.newLine();
				}
			}
			pw.flush();
			pw.close();
		} catch (UnsupportedEncodingException e1) {
			System.err.println(threadName + "Could not write file with UTF8 encoding");
		} catch (FileNotFoundException e1) {
			System.err.println(threadName + "Could not find file " + deleteFile);
		} catch (IOException e) {
			System.err.println(threadName + "Could not write to file " + deleteFile);
		}
	}

	/**
	 * Arbeitet die Update Liste ab und lädt die neuen Dateien herunter
	 *
	 */
	private void processUpdateList() {
		int totalSize = 0;
		int totalFileNum = updateFilesList.size();
		for (int i = 0; i < updateFilesList.size(); i++) {
			UpdateFile currentFile = updateFilesList.get(i);
			totalSize += currentFile.getSize();
		}
		for (int i = 0; i < updateFilesList.size(); i++) {
			if (isInterrupted()) {
				setWasInterrupted();
				interrupt();
				return;
			}
			UpdateFile nextFileToDownload = updateFilesList.get(i);
			System.out.println(threadName + "Update file " + nextFileToDownload.getName());

			// Informiere alle Listener über den Download einer neuen Datei
			Enumeration<DownloadFilesListener> en = listener.elements();
			while (en.hasMoreElements()) {
				DownloadFilesListener currentListener = en.nextElement();
				currentListener.startNewDownload(i+1, totalFileNum, nextFileToDownload, totalSize);
			}
			downloadFile(newVersion + "/", nextFileToDownload.getName());
		}
		// Informiere alle Listener, dass alle Dateien heruntergeladen wurden
		Enumeration<DownloadFilesListener> en = listener.elements();
		while (en.hasMoreElements()) {
			DownloadFilesListener currentListener = en.nextElement();
			currentListener.finished();
		}
	}

	/**
	 * Lädt die neuen Dateien aus dem Internet herunter.
	 *
	 * @param directory,
	 *            Verzeichnis, in dem die Datei liegt
	 * @param fileName
	 *            Datei, die heruntergeladen werden soll
	 */
	private void downloadFile(String directory, String fileName) {
		URL url = null;
		String urlstr = updateURL + directory + fileName;
		int position = 0;
		try {
			System.out.println(threadName + "Download new file from "
					+ urlstr);
			url = new URL(urlstr);
			URLConnection conn = url.openConnection();
			BufferedInputStream in = new BufferedInputStream(conn
					.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(updateDirectory
							+ System.getProperty("file.separator") + fileName));

			int i = in.read();
			while (i != -1) {
				if (isInterrupted()) {
					setWasInterrupted();
					in.close();
					out.flush();
					out.close();
					interrupt();
					return;
				}
				out.write(i);
				i = in.read();
				position += 1;
				if (position % 1000 == 0) {
					// Informiere alle Listener über den Fortschritt des
					// Downloads
					Enumeration<DownloadFilesListener> en = listener.elements();
					while (en.hasMoreElements()) {
						DownloadFilesListener currentListener = en
								.nextElement();
						currentListener.progress(1000);
					}
				}
			}

			// Informiere alle Listener über den Fortschritt des
			// Downloads
			Enumeration<DownloadFilesListener> en = listener.elements();
			while (en.hasMoreElements()) {
				DownloadFilesListener currentListener = en.nextElement();
				currentListener.progress(1000);
			}
			in.close();
			out.flush();
			out.close();
			System.out.println(threadName + "Saved file " + fileName
					+ " to " + updateDirectory
					+ System.getProperty("file.separator") + fileName);
		} catch (Exception e) {
			System.err.println(threadName + "Error (" + e.toString()
					+ ")");
		}
	}

	private void updateLocalFileList() {
		UpdateFile remoteFileList = new UpdateFile(updateFile, "", 1000);
		// Informiere alle Listener über den Download einer neuen Datei
		Enumeration<DownloadFilesListener> en = listener.elements();
		while (en.hasMoreElements()) {
			DownloadFilesListener currentListener = en.nextElement();
			currentListener.startNewDownload(1, 1, remoteFileList, remoteFileList
					.getSize());
		}
		downloadFile("", updateFile);
	}
}
