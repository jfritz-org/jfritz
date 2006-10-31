package de.moonflower.jfritz.autoupdate;

public interface DownloadFilesListener {

	public void startNewDownload(int currentFileNum, int totalFileNum, UpdateFile newFile, int totalSize);

	public void progress(int increment);

	public void finished();

}
