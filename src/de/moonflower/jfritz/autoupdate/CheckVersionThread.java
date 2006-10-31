package de.moonflower.jfritz.autoupdate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Dieser Thread überprüft, ob eine neue Programmversion verfügbar ist
 *
 * Ist eine neue Version verfügbar, werden die neuen Dateien in den
 * update-Ordner heruntergeladen und eine Datei deletefiles erstellt, in der die
 * zu löschenden Dateien und Ordner drinstehen
 *
 * @author Robert Palmer
 *
 */
public class CheckVersionThread extends Thread {

	private final static String className = "(CheckVersionThread) ";

	// URL zum Update-Ordner auf der Homepage
	private String updateURL = "";

	// Datei, die die Versionsinformationen auf der Homepage enthält
	private String versionFile = "";

	// Enthält die aktuelle Versionsnummer
	private String programVersion = "";

	// Enthält die neue Versionsnummer
	private String newVersion = "";

	// Neue Version verfügbar?
	private boolean newVersionAvailable = false;

	public CheckVersionThread(String programVersion, String updateURL, String versionFile) {
		this.programVersion = programVersion;
		this.updateURL = updateURL;
		this.versionFile = versionFile;
	}

	public void run() {
		System.out.println(className + "Check for new program version...");
		if (checkForNewVersion()) {
			newVersionAvailable = true;
		} else {
			newVersionAvailable = false;
		}
		System.out.println(className + "...done");
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
	 * Setzt den Dateiname auf die Datei, die die Versionsinformationen auf der
	 * Homepage enthält
	 *
	 * @param Dateiname
	 */
	public void setVersionFile(String versionFile) {
		this.versionFile = versionFile;
	}

	/**
	 * Setzt die aktuelle Programmversionsnummer
	 *
	 * @param programVersion
	 */
	public void setProgramVersion(String programVersion) {
		this.programVersion = programVersion;
	}

	/**
	 * Überprüft, ob eine neue Version verfügbar ist
	 *
	 * @return true, wenn neue Version verfügbar
	 */
	private boolean checkForNewVersion() {
		// Don't check for new version, if programVersion is zero
		if (programVersion.equals("0"))
			return false;


		URL url = null;
		String urlstr = updateURL + versionFile; //$NON-NLS-1$

		boolean foundNewVersion = false;

		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();
					// 1 Sekunde-Timeout für den Verbindungsaufbau
					con.setConnectTimeout(5000);

					// 30 Sekunde-Timeout für die Datenverbindung
					con.setReadTimeout(30000);
					BufferedReader d = new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					// Get remote version
					String str = d.readLine();
					// Format remote version as 0.621
					String remoteVersion = str.replaceFirst("\\.", "\\|")
							.replaceAll("\\.", "").replaceFirst("\\|", "\\.");

					// Format local version as 0.621
					String localVersion = programVersion.replaceFirst("\\.",
							"\\|").replaceAll("\\.", "").replaceFirst("\\|",
							"\\.");

					if (Double.valueOf(remoteVersion).compareTo(
							Double.valueOf(localVersion)) > 0) {
						newVersion = str;
						foundNewVersion = true;
					}

					d.close();

				} catch (IOException e1) {
					System.err.println(className + "Error while retrieving "
							+ urlstr
							+ " (possibly no connection to the internet)"); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			System.err.println(className + "URL invalid: " + urlstr); //$NON-NLS-1$
		}
		return foundNewVersion;
	}

	/**
	 * Ist eine neue Version verfügbar?
	 * @return
	 */
	public boolean isNewVersionAvailable() {
		return newVersionAvailable;
	}

	/**
	 * Liefert die neue Version zurück
	 * @return
	 */
	public String getNewVersion() {
		return newVersion;
	}
}
