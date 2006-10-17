package de.moonflower.jfritz.autoupdate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Vector;

public class CreateUpdateFile {

	File targetDirectory;

	String version = "";

	String versionFile = "";

	Vector dirs = new Vector();

	public void setToDir(String targetDir) {
		targetDirectory = new File(targetDir);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setVersionFile(String versionFile) {
		this.versionFile = versionFile;
	}

	public Directory createAddDirectory() {
		Directory dir = new Directory();
		dirs.add(dir);
		return dir;
	}

	public class Directory {
		public Directory() {
		}

		String dir;

		public void setDirectory(String file) {
			this.dir = file;
		}

		public String getDirectory() {
			return dir;
		}
	}

	private void setVersion() {
		File versionFile = new File(targetDirectory.getAbsolutePath()
				+ System.getProperty("file.separator") + "current.txt");
		versionFile.delete();
		try {
			versionFile.createNewFile();
		} catch (IOException e1) {
			System.err.println("Could not create version file: "
					+ versionFile.getAbsolutePath());
		}

		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(versionFile), "UTF8"));
			pw.write(version);
			pw.close();
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Could not write file with UTF8 encoding");
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file " + versionFile);
		} catch (IOException e) {
			System.err.println("Could not write to file " + versionFile);
		}
	}

	private void setUpdateFiles() {
		File updateFile = new File(targetDirectory.getAbsolutePath()
				+ System.getProperty("file.separator") + "update.txt");
		updateFile.delete();
		try {
			updateFile.createNewFile();
		} catch (IOException e1) {
			System.err.println("Could not create update file: "
					+ updateFile.getAbsolutePath());
		}
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(updateFile), "UTF8"));
			for (Iterator it = dirs.iterator(); it.hasNext();) {
				Directory dir = (Directory) it.next();

				File[] entries = new File(dir.getDirectory()).listFiles();
				for (int j = 0; j < entries.length; j++) {
					String fileDigest = "";
					try {
						byte[] digest = MDGenerator.messageDigest(entries[j]
								.getAbsolutePath(), "MD5");
						for (int i = 0; i < digest.length; i++) {
							fileDigest += (Integer
									.toHexString(digest[i] & 0xFF));
						}
					} catch (Exception e) {
						System.err
								.println("Could not create message digest for file \""
										+ entries[j].getName() + "\"");
					}
					System.out.println("File: " + entries[j].getName() + " ("
							+ fileDigest + ")");
					pw.write(entries[j].getName() + ";" + fileDigest + ";"
							+ entries[j].length());
					pw.newLine();

				}
			}
			pw.close();
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Could not write file with UTF8 encoding");
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file " + updateFile);
		} catch (IOException e) {
			System.err.println("Could not write to file " + updateFile);
		}
	}

	public void execute() {
		System.out.println("Target directory: "
				+ targetDirectory.getAbsolutePath());
		setVersion();
		setUpdateFiles();
	}

}
