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

	private final static String pointPattern = "\\.";

	private final static String dividerPattern = "\\|";

	// URL zum Update-Ordner auf der Homepage
	transient private String updateURL = "";

	// Datei, die die Versionsinformationen auf der Homepage enthält
	transient private String versionFile = "";

	// Enthält die aktuelle Versionsnummer
	transient private String programVersion = "";

	// Enthält die neue Versionsnummer
	transient private String newVersion = "";

	// Enthält die URL zum update
	transient private String urlstr = "";

	// Neue Version verfügbar?
	transient private boolean newVerAvailable = false;

	public CheckVersionThread(final String programVersion, final String updateURL, final String versionFile) {
		super();
		this.programVersion = programVersion;
		this.updateURL = updateURL;
		this.versionFile = versionFile;
		urlstr = updateURL + versionFile;
	}

	public void run() {
		Logger.msg(className + "Check for new program version...");
		if (checkForNewVersion()) {
			newVerAvailable = true;
		} else {
			newVerAvailable = false;
		}
		Logger.msg(className + "...done");
	}

	/**
	 * Setzt die URL zum Update-Ordner auf der Homepage
	 *
	 * @param URL
	 *            zum Update-Ordner auf der Homepage
	 */
	public void setUpdateURL(final String updateURL) {
		if (!updateURL.endsWith("/"))
		{
			updateURL.concat("/");
		}
		this.updateURL = updateURL;
	}

	/**
	 * Setzt den Dateiname auf die Datei, die die Versionsinformationen auf der
	 * Homepage enthält
	 *
	 * @param Dateiname
	 */
	public void setVersionFile(final String versionFile) {
		this.versionFile = versionFile;
	}

	/**
	 * Setzt die aktuelle Programmversionsnummer
	 *
	 * @param programVersion
	 */
	public void setProgramVersion(final String programVersion) {
		this.programVersion = programVersion;
	}

	/**
	 * Überprüft, ob eine neue Version verfügbar ist
	 *
	 * @return true, wenn neue Version verfügbar
	 */
	private boolean checkForNewVersion() {
		boolean result = true;

		// Don't check for new version, if programVersion is zero
		if ("0".equals(programVersion))
		{
			result = false;
		}

		if (result)
		{
			urlstr = updateURL + versionFile; //$NON-NLS-1$

			boolean foundNewVersion = false;
			BufferedReader buffReader;
			String currentLine;
			String remoteVersion;
			String localVersion;
			try {
				final URL url = new URL(urlstr);
				if (url != null) {

					URLConnection con;
					try {
						con = url.openConnection();
						// 1 Sekunde-Timeout für den Verbindungsaufbau
						con.setConnectTimeout(5000);

						// 30 Sekunde-Timeout für die Datenverbindung
						con.setReadTimeout(30000);
						buffReader = new BufferedReader(
								new InputStreamReader(con.getInputStream()));

						// Get remote version
						currentLine = buffReader.readLine();
						// Format remote version as 0.621
						remoteVersion = currentLine.replaceFirst(pointPattern, dividerPattern)
								.replaceAll(pointPattern, "").replaceFirst(dividerPattern, pointPattern);

						// Format local version as 0.621
						localVersion = programVersion.replaceFirst(pointPattern,
								dividerPattern).replaceAll(pointPattern, "").replaceFirst(dividerPattern,
								pointPattern);

						if (Double.valueOf(remoteVersion).compareTo(
								Double.valueOf(localVersion)) > 0) {
							newVersion = currentLine;
							foundNewVersion = true;
						}

						buffReader.close();

					} catch (IOException e1) {
						Logger.err(className + "Error while retrieving "
								+ urlstr
								+ " (possibly no connection to the internet)"); //$NON-NLS-1$
					}
				}
			} catch (MalformedURLException e) {
				Logger.err(className + "URL invalid: " + urlstr); //$NON-NLS-1$
			}
			result = foundNewVersion;
		}

		return result;
	}

	/**
	 * Ist eine neue Version verfügbar?
	 * @return
	 */
	public boolean isNewVersionAvailable() {
		return newVerAvailable;
	}

	/**
	 * Liefert die neue Version zurück
	 * @return
	 */
	public String getNewVersion() {
		return newVersion;
	}
}
