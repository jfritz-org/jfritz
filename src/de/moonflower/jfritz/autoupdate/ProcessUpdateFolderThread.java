package de.moonflower.jfritz.autoupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;

/**
 * Diese Klasse überprüft, ob der Ordner update im Hauptverzeichnis existiert.
 * Ist der Ordner vorhanden, so existiert in dem update-Ordner eine
 * deletefiles-Datei, in denen alle Dateien stehen, die gelöscht werden müssen.
 *
 * Alle Dateien und Ordner in der deletefiles-Datei werden gelöscht und der
 * Inhalt des update-Ordners wird ins JFritz Hauptverzeichnis kopiert.
 * Anschließend wird der update-Ordner gelöscht
 *
 * @author Rob
 *
 */
public class ProcessUpdateFolderThread extends Thread {

	private String threadName = "(ProcessUpdateFolderThread) ";

	private String updateFile = "";

	private String updateDirectory = "";

	private String installDirectory = "";

	private String deleteListFile = "";

	private String directoriesZipedAsFilesEndWith = "";

	private boolean updateSuccessfull = false;

	public ProcessUpdateFolderThread(String updateDirectory, String updateFile,
			String installDirectory, String deleteListFile,
			String directoriesZipedAsFilesEndWith) {
		this.updateDirectory = updateDirectory;
		this.updateFile = updateFile;
		this.installDirectory = installDirectory;
		this.deleteListFile = deleteListFile;
		this.directoriesZipedAsFilesEndWith = directoriesZipedAsFilesEndWith;
	}

	public void run() {
		if (updateDirectoryExists()) {
			processDeleteListFile();
			updateFiles();
		}
	}

	/**
	 * Liefert TRUE zurück, wenn das Update-Directory existiert
	 *
	 * @return
	 */
	public boolean updateDirectoryExists() {
		File updateDir = new File(updateDirectory);
		return updateDir.exists();
	}

	/**
	 * Löscht alle Dateien und Verzeichnisse, die in der Datei deleteListFile
	 * stehen
	 */
	public void processDeleteListFile() {
		File deleteFile = new File(updateDirectory
				+ System.getProperty("file.separator") + deleteListFile);

		String fileToDelete = "";
		String dirName = "";
		File deleteDirectory;
		if (deleteFile.exists()) {
			BufferedReader pw;
			try {
				pw = new BufferedReader(new InputStreamReader(
						new FileInputStream(deleteFile), "UTF8"));

				String line = "";
				// Füge Dateien zur filesToDownload-Liste hinzu
				while (null != (line = pw.readLine())) {
					if (line.endsWith(directoriesZipedAsFilesEndWith)) {
						// File is an ziped directory
						// Delete whole directory
						dirName = line.substring(0, line
								.indexOf(directoriesZipedAsFilesEndWith));
						fileToDelete = installDirectory + System.getProperty("file.seperator") + dirName;
						deleteDirectory = new File(fileToDelete);
						UpdateUtils.deleteTree(deleteDirectory);
					} else {
						fileToDelete = installDirectory + System.getProperty("file.separator") + line;
						UpdateUtils.deleteFile(fileToDelete);
					}
				}

				pw.close();
				deleteFile.delete();
			} catch (UnsupportedEncodingException e) {
				Logger.err(threadName + "ERROR: Encoding not supported");
//				JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("encodingNotSupported"), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				Logger.err(threadName + "ERROR: File not found");
//				JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("fileNotFound").replaceAll("%FILENAME", fileToDelete), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException ioe) {
				Logger.err(threadName + "ERROR: IO exception");
//				JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("ioError").replaceAll("%FILENAME", fileToDelete), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
				ioe.printStackTrace();
			}
		}
	}

	public void updateFiles() {
		File updateDir = new File(updateDirectory);

		if (updateDir.exists()) {
			for (File file : updateDir.listFiles()) {
				if (!((file.getName().equals(deleteListFile)) || (file
						.getName().equals(updateFile)))) {

					// Datei ist ein gezipptes Verzeichnis
					// Entpacke die Datei ins installDirectory
					if (file.getName().endsWith(directoriesZipedAsFilesEndWith)) {
						Logger.msg(threadName + file.getAbsolutePath()
								+ " is a ziped directory");
						UpdateUtils.unzipFile(file.getAbsolutePath(), installDirectory);
					} else {
						File destinationFile = new File(installDirectory
								+ System.getProperty("file.separator")
								+ file.getName());
						try {
							UpdateUtils.copyFile(file, destinationFile, 10,
									true);
							updateSuccessfull = true;
						} catch (IOException e) {
							Logger.err(threadName + "ERROR: IO exception");
							JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("ioError").replaceAll("%FILENAME", destinationFile.getAbsolutePath()), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}

					// lösche die abgearbeitete Datei
					file.delete();
				}
			}
		}
	}

	public void setDeleteListFile(String deleteListFile) {
		this.deleteListFile = deleteListFile;
	}

	public void setDirectoriesZipedAsFilesEndWith(
			String directoriesZipedAsFilesEndWith) {
		this.directoriesZipedAsFilesEndWith = directoriesZipedAsFilesEndWith;
	}

	public void setInstallDirectory(String installDirectory) {
		this.installDirectory = installDirectory;
	}

	public void setUpdateDirectory(String updateDirectory) {
		this.updateDirectory = updateDirectory;
	}

	public void setUpdateFile(String updateFile) {
		this.updateFile = updateFile;
	}

	public boolean wasUpdateSuccessfull() {
		return updateSuccessfull;
	}
}
