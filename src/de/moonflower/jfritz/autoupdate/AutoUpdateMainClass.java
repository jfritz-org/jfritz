package de.moonflower.jfritz.autoupdate;

public class AutoUpdateMainClass {

	// URL zum Update-Ordner auf der Homepage
	transient private static String updateURL = "";

	// Datei, die die Versionsinformationen auf der Homepage enthält
	transient private static String versionFile = "";

	// Datei, die auf der Homepage die Informationen über die neuen Dateien
	// enthält
	transient private static String updateFile = "";

	// Das Verzeichnis, in das die neuen Dateien heruntergeladen werden
	transient private static String updateDirectory = "";

	// Verzeichnis, wo die Einstellungen des Autoupdates gespeichert werden
	transient private static String propertiesDirectory = "";

    // Gepackte Verzeichnisse haben diese Dateiendung
	transient private static String directoriesZipedAsFilesEndWith = "";

	// Datei, die die zu löschenden Dateien enthält
	transient private static String deleteListFile = "";

	transient private static String installDirectory = "";

	private String className;

	public AutoUpdateMainClass(String className)
	{
		this.className = className;
	}

	/**
	 * Log a message, using className as a prefix
	 * @param msg
	 */
	protected void logMessage(final String msg)
	{
		Logger.msg("(" + className + ")" + msg);
	}

	/**
	 * Log an error message, using className as a prefix
	 * @param err
	 */
	protected void logError(final String err)
	{
		Logger.err("(" + className + ")" + err);
	}

	/**
	 * Setzt die URL zum Update-Ordner auf der Homepage
	 *
	 * @param URL
	 *            zum Update-Ordner auf der Homepage
	 */
	protected static void setUpdateURL(final String url) {
		if (!url.endsWith("/"))
		{
			url.concat("/");
		}
		updateURL = url;
	}

	protected static String getUpdateURL()
	{
		return updateURL;
	}

	/**
	 * Setzt den Dateiname auf die Datei, die die Versionsinformationen auf der
	 * Homepage enthält
	 *
	 * @param Dateiname
	 */
	protected static void setVersionFile(final String file) {
		versionFile = file;
	}

	protected static String getVersionFile()
	{
		return versionFile;
	}

	/**
	 * Setzt den Dateiname auf die Datei, die auf der Homepage die Informationen
	 * über die neuen Dateien enthält
	 *
	 * @param Dateiname
	 */
	protected static void setUpdateFile(final String file) {
		updateFile = file;
	}

	protected static String getUpdateFile()
	{
		return updateFile;
	}

	protected static void setPropertiesDirectory(final String dir)
	{
		propertiesDirectory = dir;
	}

	protected static String getPropertiesDirectory()
	{
		return propertiesDirectory;
	}

	/**
	 * Setzt das Verzeichnis, in das die neuen Dateien heruntergeladen werden
	 *
	 * @param updateDirectory
	 */
	protected static void setUpdateDirectory(final String dir) {
		updateDirectory = dir;
	}

	protected static String getUpdateDirectory()
	{
		return updateDirectory;
	}

	/**
	 * Setzt die Dateiendung für gepackte Verzeichnisse
	 * @param fileSuffix
	 */
	protected static void setDirectoriesZipedAsFilesEndWith(final String fileSuffix) {
		directoriesZipedAsFilesEndWith = fileSuffix;
	}

	protected static String getDirectoriesZipedAsFilesEndWith()
	{
		return directoriesZipedAsFilesEndWith;
	}

	/**
	 * Setzt den Dateinamen für die Datei, in der eine Liste der zu löschenden
	 * Dateien steht
	 *
	 * @param deleteListFile
	 */
	protected static void setDeleteListFile(final String file) {
		deleteListFile = file;
	}

	protected static String getDeleteListFile()
	{
		return deleteListFile;
	}

	protected static void setInstallDirectory(final String dir) {
		installDirectory = dir;
	}

	protected static String getInstallDirectory()
	{
		return installDirectory;
	}

}
