package de.moonflower.jfritz.ant.constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UpdateProgramConstants {
	String path = "";
	String revision = "";
	String date = "";

	public void setPath(String path) {
		this.path = path;
	}
	
	public void setRevision(String rev) {
		this.revision = rev;
	}

	public void setDate(String date) {
		this.date = date;
	}

	private void copy(String src, String dst, boolean replacePlaceholders) {
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(src));
			BufferedWriter outFile = new BufferedWriter(new FileWriter(dst));

			String currentLine = inFile.readLine();

			while (currentLine != null) {
				if (replacePlaceholders && currentLine.contains("String REVISION =")) {
					outFile.write("	public final static String REVISION = \"" + revision + "\";");
					outFile.newLine();
				} else if (replacePlaceholders && currentLine.contains("String BUILD_DATE =")) {
					outFile.write("	public final static String BUILD_DATE = \"" + date + "\";");
					outFile.newLine();
				} else {
					outFile.write(currentLine);
					outFile.newLine();
				}
				currentLine = inFile.readLine();
			}
			inFile.close();
			outFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void execute() {
		System.out.println("Using path: " + path);
		copy(path+"/src/de/moonflower/jfritz/constants/ProgramConstants.java", path+"/tmp", true);
		copy(path+"/tmp", path+"/src/de/moonflower/jfritz/constants/ProgramConstants.java", false);
		File f = new File(path+"/tmp");
		if (f.exists()) { 
			f.delete(); 
		}
	}
}
