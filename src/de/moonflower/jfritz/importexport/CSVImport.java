package de.moonflower.jfritz.importexport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import de.moonflower.jfritz.utils.Debug;

/**
 *
 * @author Robert
 *
 */
public class CSVImport {
	private Hashtable<Integer, String> mappedColumns;
	private String separator = ";";
	private String fileName = "";
	private FileReader fileReader;
	private BufferedReader bufferedReader;

	public CSVImport(final String fileName) {
		this.fileName = fileName;
		mappedColumns = new Hashtable<Integer, String>();
	}

	public void openFile() {
		try {
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			Debug.errDlg("File '" + fileName + "' does not exist!", e);
		}
	}

	public void closeFile() {
		if (bufferedReader != null) {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fileReader != null) {
			try {
				fileReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String readHeader(int lineNr) {
		String line = "";
		try {
			this.closeFile();
			this.openFile();
			for (int i=0; i<lineNr+1; i++) {
				line = bufferedReader.readLine();
			}
			this.closeFile();
			this.openFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return line;
	}

	public String readLine() {
		String response = null;
		try {
			response = bufferedReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public void reset() {
		if (bufferedReader != null) {
			try {
				bufferedReader.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setSeparaor(final String separator) {
		this.separator = separator;
	}

	public Vector<String> split(final String line) {
		String[] split = line.split(separator);
		Vector<String> response = new Vector<String>(split.length);
		String correct = "";
		for (int i=0; i<split.length; i++) {
			if (split[i].startsWith("\"") &&
					split[i].endsWith("\"")) {
				// beginnt und endet mit "
				// entferne das " am Anfang und am Ende des Strings
				String tmp = split[i];
				tmp = tmp.substring(1);
				tmp = tmp.substring(0, tmp.length()-1);
				response.add(tmp);
			} else if (split[i].startsWith("\"")
					&& !split[i].endsWith("\"")) {
				// beginnt mit ", aber endet nicht mit "
				correct = split[i].substring(1); // entferne das " am Anfang des Strings
			} else if (!split[i].startsWith("\"")
					&& (split[i].endsWith("\"")))
			{
				correct += separator + split[i];
				response.add(correct.substring(0, correct.length()-1)); // entferne das " am Ende des Strings
			} else {
				response.add(split[i]);
			}
		}
		return response;
	}

	public void mapColumn(int id, String column) {
		mappedColumns.put(id, column);
	}

	public String getMappedColumn(int id) {
		return mappedColumns.get(id);
	}

	public void clearMappedColumns() {
		mappedColumns.clear();
	}
}
