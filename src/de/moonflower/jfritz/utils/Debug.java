/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;


import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import de.moonflower.jfritz.Main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write debug messages to STDOUT or FILE. Show Error-Dialog with a special
 * message
 *
 * 14.05.06 Added support for redirecting System.out and System.err Now all
 * exceptions are also included in the debug file Brian Jensen
 *
 * @author Arno Willig
 *
 */
public class Debug{

	private static final long serialVersionUID = 9211082107025215527L;

	private static int debugLevel;

	private static boolean verboseMode = false;

	private static boolean logFile = false;

	private static PrintStream fileRedirecter, originalOut;

	private static JPanel main_panel = null;

	private static JTextArea log_area;

	private static JButton close_button;
	private static JButton save_button;
	private static JButton clear_button;

	private static JScrollPane scroll_pane;

	private static JFrame display_frame;

	private static StringBuffer debugBuffer;

	/**
	 * Turns debug-mode on
	 *
	 */
	public static void on() {
		verboseMode = true;
		Debug.debugLevel = 3;
		debugBuffer = new StringBuffer();
		msg("debugging mode has been enabled"); //$NON-NLS-1$
	}

	/**
	 * This function works by redirecting System.out and System.err to fname The
	 * original console stream is saved as originalout 15.05.06 Brian Jensen
	 *
	 * Turn on logging mode to file
	 *
	 * @param fname
	 *            Filename to log into
	 */
	public static void logToFile(String fname) {
		Debug.debugLevel = 3;
		logFile = true;
		// Save the original outputstream so we can write to the console too!
		originalOut = System.out;

		//if our file name contains no path, then save in our save dir
		if(!fname.contains(System.getProperty("file.separator")))
			fname = Main.SAVE_DIR + fname;

		try {
			// setup the redirection of Sysem.out and System.err
			FileOutputStream tmpOutputStream = new FileOutputStream(
					fname);
			fileRedirecter = new PrintStream(tmpOutputStream);
			System.setOut(fileRedirecter);
			System.setErr(fileRedirecter);
		}

		catch (Exception e) {
			System.out.println("EXCEPTION when writing to LOGFILE"); //$NON-NLS-1$
		}

		fileRedirecter.println("------------------------------------------"); //$NON-NLS-1$
		msg("logging to file \"" + fname + "\" has been enabled"); //$NON-NLS-1$,  //$NON-NLS-2$
	}

	/**
	 * Print message with priority 1
	 *
	 * @param message
	 */
	public static void msg(String message) {
		msg(1, message);
	}

	/**
	 *
	 * @return current Time HH:mm:ss
	 */
	private static String getCurrentTime() {
		Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		return df.format(now);
	}

	/**
	 * Print message with prioriry level
	 *
	 * @param level
	 * @param message
	 */
	public static void msg(int level, String message) {
		if ( debugLevel >= level) {
			message = "(" + getCurrentTime() + ") DEBUG: " + message; //$NON-NLS-1$,  //$NON-NLS-2$
			System.out.println(message);

				//only write the message to the panel if we have already created it
			if(main_panel != null){
				log_area.append(message+"\n");
				autoScroll();
				//otherwise save it for later use, if the panel gets created later
			}else {
				debugBuffer.append(message+"\n");
			}

			// if both verbose mode and logging enabled, make sure output
			// still lands on the console as well!
			if (logFile && verboseMode) {
				originalOut.println(message);
			}
		}
	}

	/**
	 * This is a modified message function, used by the network subsystem
	 * so the debug output is more readable
	 *
	 * @param message
	 */
	public static void netMsg(String message){

		message = "(" + getCurrentTime() + ") NETWORK: " + message; //$NON-NLS-1$,  //$NON-NLS-2$
		System.out.println(message);

		//only write the message to the panel if we have already created it
		if(main_panel != null){
			log_area.append(message+"\n");
			autoScroll();
			//otherwise save it for later use, if the panel gets created later
		}else {
			debugBuffer.append(message+"\n");
		}

		// if both verbose mode and logging enabled, make sure output
		// still lands on the console as well!
		if (logFile && verboseMode) {
			originalOut.println(message);
		}

	}

	/**
	 * Print error-message
	 *
	 * @param message
	 */
	public static void err(String message) {
			message = "(" + getCurrentTime() + ") ERROR: " + message; //$NON-NLS-1$,  //$NON-NLS-2$
			System.err.println(message);

			//only write the message to the panel if we have already created it
			if(main_panel != null){
				log_area.append(message+"\n");
				autoScroll();
				//otherwise save it for later use, if the panel gets created later
			}else {
				debugBuffer.append(message+"\n");
			}

			// if both verbose mode and logging enabled, make sure output
			// still lands on the console as well!
			if (logFile && verboseMode) {
				originalOut.println(message);
			}
	}

	/**
	 * Show Dialog with message
	 *
	 * @param message
	 */
	public static void msgDlg(String message) {
		msg(message);
		JOptionPane.showMessageDialog(null, message, Main
				.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Show error Dialog with message
	 *
	 * @param message
	 */
	public static void errDlg(String message) {
		err(message);
		JOptionPane.showMessageDialog(null, message,
				Main.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	public static void generatePanel()
	{
		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout());
		log_area = new JTextArea(25, 80);
		scroll_pane = new JScrollPane(log_area);
		main_panel.add(scroll_pane, BorderLayout.NORTH);
		JPanel button_panel = new JPanel();
		button_panel.setLayout(new GridLayout(1,3));

		close_button = new JButton();
		close_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				display_frame.setVisible(false);
				display_frame.dispose();
			}
		});
		close_button.setText("Close");

		save_button = new JButton();
		save_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(Main.SAVE_DIR); //$NON-NLS-1$
				fc.setDialogTitle(Main.getMessage("save_debug_log")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.isDirectory()
								|| f.getName().toLowerCase().endsWith(".dbg"); //$NON-NLS-1$
					}

					public String getDescription() {
						return Main.getMessage("debug_files"); //$NON-NLS-1$
					}
				});
				if (fc.showSaveDialog(display_frame) == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getPath();
					path = path.substring(0, path.length()
							- fc.getSelectedFile().getName().length());
					Main.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
					File file = fc.getSelectedFile();
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(display_frame, Main.getMessage(
								"overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
								Main.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
							try {
								log_area.write(new FileWriter(file.getAbsolutePath()));
							} catch (IOException e) {
								Debug.err("Could not save debug log to file: "+file.getAbsolutePath());
								e.printStackTrace();
							}
						}
					} else {
						try {
							FileWriter fw = new FileWriter(file.getAbsolutePath());
							log_area.write(fw);
							fw.close();
						} catch (IOException e) {
							Debug.err("Could not save debug log to file: "+file.getAbsolutePath());
							e.printStackTrace();
						}
					}
				}
			}
		});
		save_button.setText("Save");

		clear_button = new JButton();
		clear_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				log_area.setText("");
			}
		});
		clear_button.setText("Clear");

		((GridLayout)button_panel.getLayout()).setHgap(20);
		((GridLayout)button_panel.getLayout()).setVgap(20);
		button_panel.add(save_button);
		button_panel.add(clear_button);
		button_panel.add(close_button);
		main_panel.add(button_panel, BorderLayout.SOUTH);

		log_area.append(debugBuffer.toString());
		autoScroll();
	}

	public static JPanel getPanel()
	{
		return main_panel;
	}

	public static void SetCloseButtonText(String text)
	{
		close_button.setText(text);
	}

	public static void SetSaveButtonText(String text)
	{
		save_button.setText(text);
	}

	public static void SetClearButtonText(String text)
	{
		clear_button.setText(text);
	}

	public static void setFrame(JFrame frame)
	{
		display_frame = frame;
	}

	private static void autoScroll()
	{
		log_area.setCaretPosition(log_area.getDocument().getLength());
	}
}
