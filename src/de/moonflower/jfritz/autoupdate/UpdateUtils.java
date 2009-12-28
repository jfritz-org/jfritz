package de.moonflower.jfritz.autoupdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

public class UpdateUtils {

	private final static String className = "(UpdateUtils) ";

	/**
	 * Löscht eine Datei
	 *
	 * @param fileName
	 *            Die zu löschende Datei
	 */
	public static void deleteFile(String fileName) {
		File file = new File(fileName);
		Logger.msg(className + "Deleting file " + file.getAbsolutePath());
		if (!file.delete()) {
			Logger.err("Could not delete file "
					+ file.getAbsolutePath());
		}
	}

	/**
	 * Löscht ein Verzeichnis, samt Unterverzeichnissen
	 *
	 * @param path
	 *            Das zu löschende Verzeichnis
	 */
	public static void deleteTree(File path) {
		Logger.msg(className + "Deleting directory " + path.getAbsolutePath());
		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory())
					deleteTree(file);
				file.delete();
			}
			path.delete();
		}
	}

	/**
	 * Löscht ein Verzeichnis, samt Unterverzeichnissen, mit Ausnahme der Datei
	 * exceptFile
	 *
	 * @param path
	 *            Das zu löschende Verzeichnis
	 * @param exceptFile
	 *            Die zu verschonende Datei
	 */
	public static void deleteTreeWithoutFile(File path, String exceptFile) {
		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory())
					deleteTree(file);
				if (!file.getName().equals(exceptFile))
					file.delete();
			}
			path.delete();
		}
	}

	public static void copyFile(File src, File dest, int bufSize, boolean force)
			throws IOException {
		if (dest.exists()) {
			if (force) {
				dest.delete();
			} else {
				throw new IOException(className + "Cannot overwrite existing file: "
						+ dest.getAbsolutePath());
			}
		}
		byte[] buffer = new byte[bufSize];
		int read = 0;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			while (true) {
				read = in.read(buffer);
				if (read == -1) {
					// -1 bedeutet EOF
					break;
				}
				out.write(buffer, 0, read);
			}
		} finally {
			// Sicherstellen, dass die Streams auch
			// bei einem throw geschlossen werden.
			// Falls in null ist, ist out auch null!
			if (in != null) {
				// Falls tatsächlich in.close() und out.close()
				// Exceptions werfen, die jenige von 'out' geworfen wird.
				try {
					in.close();
				} finally {
					if (out != null) {
						out.close();
					}
				}
			}
		}
	}

	public static void unzipFile(String zipFile, String destinationDirectory) {
		File destinationDir = new File(destinationDirectory);
		if (!destinationDir.exists()) {
			destinationDir.mkdir();
		}
		ZipEntry target = new ZipEntry("");
		try {
			ZipFile zf = new ZipFile(zipFile);
			for (Enumeration<? extends ZipEntry> e = zf.entries(); e
					.hasMoreElements();) {
				target = e.nextElement();
				Logger.msg(className + target.getName() + " .");
				saveEntry(zf, target, destinationDirectory);
				Logger.msg(". unpacked");
			}
			zf.close();
		} catch (FileNotFoundException e) {
			Logger.err(className + "... file not found");
			JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("fileNotFound").replaceAll("%FILENAME", target.getName()), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (ZipException e) {
			Logger.err(className + "... zip error");
			JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("zipError").replaceAll("%FILENAME", target.getName()), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			Logger.err(className + "...io error");
			JOptionPane.showMessageDialog(null, UpdateLocale.getMessage("ioError").replaceAll("%FILENAME", target.getName()), UpdateLocale.getMessage("autoupdate_title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void saveEntry(ZipFile zf, ZipEntry target, String destinationDirectory)
			throws ZipException, IOException {
		File file = new File(destinationDirectory + System.getProperty("file.separator") + target.getName());
		if (target.isDirectory())
			file.mkdirs();
		else {
			InputStream is = zf.getInputStream(target);
			BufferedInputStream bis = new BufferedInputStream(is);
			new File(file.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			final int EOF = -1;
			for (int c; (c = bis.read()) != EOF;)
				// oder schneller
				bos.write((byte) c);
			is.close();
			bis.close();
			bos.close();
			fos.close();
		}
	}
}
