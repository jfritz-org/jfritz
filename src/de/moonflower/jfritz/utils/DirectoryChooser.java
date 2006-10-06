package de.moonflower.jfritz.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import de.moonflower.jfritz.Main;

public class DirectoryChooser  {

	private JFileChooser chooser;
	public final static String CHOOSER_TITLE = Main.getMessage("dialog_title_choose_dest_dir"); //$NON-NLS-1$

	/**
	 * @return the choosen directory
	 *
	 */
	public File getDirectory(JFrame frame) {

		chooser = new JFileChooser();
		chooser.setApproveButtonText(Main.getMessage("save")); //$NON-NLS-1$
		chooser.setCurrentDirectory(new java.io.File(JFritzUtils.deconvertSpecialChars(Main.getProperty("backup.path", ".")))); //$NON-NLS-1$,  //$NON-NLS-2$
		chooser.setDialogTitle(CHOOSER_TITLE);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			Debug.msg("getCurrentDirectory(): " + chooser.getSelectedFile()); //$NON-NLS-1$
			Main.setProperty("backup.path", JFritzUtils.convertSpecialChars(chooser.getSelectedFile().toString())); //$NON-NLS-1$
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}

}
