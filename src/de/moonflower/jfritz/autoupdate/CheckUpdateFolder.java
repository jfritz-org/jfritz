package de.moonflower.jfritz.autoupdate;

import java.io.File;

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
public class CheckUpdateFolder {

	public boolean updateDirectoryExists() {
		File updateDirectory = new File("update");
		return updateDirectory.exists();
	}

	public void removeUpdateFiles() {

	}

	public void updateFiles() {

	}

	public void deleteUpdateDirectory() {

	}
}
