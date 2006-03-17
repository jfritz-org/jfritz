package de.moonflower.jfritz.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import de.moonflower.jfritz.JFritz;

public class directoryChooser extends JPanel {

	private JFileChooser chooser;
	public final static String CHOOSER_TITLE = "Zielverzeichnis auswählen";

	/**
	 * @return the choosen directory
	 *
	 */
	public File getDirectory() {

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(JFritz.getProperty("backup.path", ".")));
		chooser.setDialogTitle(CHOOSER_TITLE);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			Debug.msg("getCurrentDirectory(): " + chooser.getSelectedFile());
			JFritz.setProperty("backup.path", chooser.getSelectedFile().toString());
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}

}
