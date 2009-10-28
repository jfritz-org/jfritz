/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import de.moonflower.jfritz.Main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
 * @author Robert Palmer
 *
 */
public class Debug {
	public static final LogSeverity LS_ALWAYS = new LogSeverity(0, "LS_ALWAYS", "");
	public static final LogSeverity LS_ERROR = new LogSeverity(1, "LS_ERROR", ": ERROR");
	public static final LogSeverity LS_WARNING = new LogSeverity(2, "LS_WARNING", ": WARNING");
	public static final LogSeverity LS_NETWORK = new LogSeverity(3, "LS_NETWORK", ": NETWORK");
	public static final LogSeverity LS_INFO = new LogSeverity(4, "LS_INFO", ": INFO");
	public static final LogSeverity LS_DEBUG = new LogSeverity(5, "LS_DEBUG", ": DEBUG");

	private static final long serialVersionUID = 9211082107025215527L;

	private static LogSeverity debugLevel;
	private static String debugLogFile;

	private static boolean verboseMode = false;

	private static PrintStream originalOut;
	private static PrintStream originalErr;

	private static JPanel main_panel = null;

	private static JTextArea log_area;

	private static JButton close_button;
	private static JButton save_button;
	private static JButton refresh_button;
	private static JComboBox log_severity_box;

	private static JScrollPane scroll_pane;

	private static JFrame display_frame;
	/**
	 * Turns debug-mode on
	 *
	 */
	public static void on() {
		verboseMode = false;
		Debug.debugLevel = LS_DEBUG;
		logToFile("debug.log");
	}

	public static void off() {
		System.setOut(originalOut);
		System.setErr(originalErr);
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
	public static void logToFile(final String fName) {
		debugLogFile = fName;

		// Save the original outputstream so we can write to the console too!
		originalOut = System.out;
		originalErr = System.err;

		//if our file name contains no path, then save in our save dir
		if(!debugLogFile.contains(System.getProperty("file.separator")))
			debugLogFile = Main.SAVE_DIR + debugLogFile;

		try {
			// setup the redirection of Sysem.out and System.err
			FileOutputStream tmpOutputStream = new FileOutputStream(
					debugLogFile);
			PrintStream outputFileRedirector = new PrintStream(tmpOutputStream);
			ConsoleAndFilePrintStream errorFileRedirector = new ConsoleAndFilePrintStream(tmpOutputStream);
			System.setOut(outputFileRedirector);
			System.setErr(errorFileRedirector);
		}

		catch (Exception e) {
			System.err.println("EXCEPTION when writing to LOGFILE"); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * @return current Time HH:mm:ss
	 */
	private static String getCurrentTime() {
		Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss"); //$NON-NLS-1$
		return df.format(now);
	}

	/**
	 * Print message with prioriry level
	 *
	 * @param level
	 * @param message
	 */
	private static void msg(LogSeverity level, final String msg) {
		if ( (debugLevel == null)
				|| (debugLevel != null) && (debugLevel.getId() >= level.getId())) {
			String message = msg;
			message = "(" + getCurrentTime() + ")"+ level.getPrefix() + ": "+ message; //$NON-NLS-1$,  //$NON-NLS-2$
			System.out.println(message);

			// if both verbose mode and logging enabled, make sure output
			// still lands on the console as well!
			if (verboseMode) {
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
	public static void netMsg(final String msg){
		msg(LS_NETWORK, msg);
	}

	public static void always(String msg) {
		msg(LS_ALWAYS, msg);
	}

	public static void error(String msg) {
		msg(LS_ERROR, msg);
	}

	public static void warning(String msg) {
		msg(LS_WARNING, msg);
	}

	public static void info(String msg) {
		msg(LS_INFO, msg);
	}

	public static void debug(String msg) {
		msg(LS_DEBUG, msg);
	}

	/**
	 * Show Dialog with message
	 *
	 * @param message
	 */
	public static void msgDlg(String message) {
		msg(LS_ALWAYS, message);
		JOptionPane.showMessageDialog(null, message, Main
				.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Show error Dialog with message
	 *
	 * @param message
	 */
	public static void errDlg(String message) {
		error(message);
		JOptionPane.showMessageDialog(null, message,
				Main.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	public static void generatePanel()
	{
		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout());
		log_area = new JTextArea(0, 80);
		scroll_pane = new JScrollPane(log_area);
		scroll_pane.setPreferredSize(new Dimension(640, 320));
		main_panel.add(scroll_pane, BorderLayout.CENTER);

		JPanel top_panel = new JPanel();
		log_severity_box = new JComboBox();
		log_severity_box.addItem(LS_ALWAYS);
		log_severity_box.addItem(LS_ERROR);
		log_severity_box.addItem(LS_WARNING);
		log_severity_box.addItem(LS_NETWORK);
		log_severity_box.addItem(LS_INFO);
		log_severity_box.addItem(LS_DEBUG);
		log_severity_box.setSelectedItem(LS_DEBUG);
		log_severity_box.setActionCommand("severity_changed");
		log_severity_box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("severity_changed".equals(e.getActionCommand()))
				{
					loadDebugFile();
				}
			}
		});
		top_panel.add(log_severity_box);
		main_panel.add(top_panel, BorderLayout.NORTH);

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
								|| f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
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
								error("Could not save debug log to file: "+file.getAbsolutePath());
								e.printStackTrace();
							}
						}
					} else {
						try {
							FileWriter fw = new FileWriter(file.getAbsolutePath());
							log_area.write(fw);
							fw.close();
						} catch (IOException e) {
							Debug.error("Could not save debug log to file: "+file.getAbsolutePath());
							e.printStackTrace();
						}
					}
				}
			}
		});
		save_button.setText("Save");

		refresh_button = new JButton();
		refresh_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				loadDebugFile();
			}
		});
		refresh_button.setText("Refresh");

		((GridLayout)button_panel.getLayout()).setHgap(20);
		((GridLayout)button_panel.getLayout()).setVgap(20);
		button_panel.add(save_button);
		button_panel.add(refresh_button);
		button_panel.add(close_button);
		main_panel.add(button_panel, BorderLayout.SOUTH);

		loadDebugFile();
		autoScroll();
	}

	private static void loadDebugFile(){
		try {
			int selectedLogSeverityIndex = log_severity_box.getSelectedIndex();
			LogSeverity selectedLogSeverity = (LogSeverity) log_severity_box.getItemAt(selectedLogSeverityIndex);
			log_area.setText("");
			BufferedReader in = new BufferedReader(new FileReader(debugLogFile));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				if (selectedLogSeverity.getId() >= returnLineSeverity(zeile).getId())
				{
					log_area.append(zeile + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static LogSeverity returnLineSeverity(String line)
	{
		LogSeverity ls = LS_DEBUG;
		if (line.substring(19).startsWith(LS_DEBUG.getPrefix())) {
			ls = LS_DEBUG;
		} else if (line.substring(19).startsWith(LS_INFO.getPrefix())) {
			ls = LS_INFO;
		} else if (line.substring(19).startsWith(LS_NETWORK.getPrefix())) {
			ls = LS_NETWORK;
		} else if (line.substring(19).startsWith(LS_WARNING.getPrefix())) {
			ls = LS_WARNING;
		} else if (line.substring(19).startsWith(LS_ERROR.getPrefix())) {
			ls = LS_ERROR;
		} else {
			ls = LS_ALWAYS;
		}

		return ls;
	}

	public static JPanel getPanel()
	{
		loadDebugFile();
		return main_panel;
	}

	public static void setCloseButtonText(String text)
	{
		close_button.setText(text);
	}

	public static void setSaveButtonText(String text)
	{
		save_button.setText(text);
	}

	public static void setRefreshButtonText(String text)
	{
		refresh_button.setText(text);
	}

	public static void setFrame(JFrame frame)
	{
		display_frame = frame;
	}

	private static void autoScroll()
	{
		log_area.setCaretPosition(log_area.getDocument().getLength());
	}

	public static void setVerbose(boolean verbose)
	{
		verboseMode = verbose;
	}

	public static boolean isVerbose() {
		return verboseMode;
	}

	public static void setDebugLevel(LogSeverity level)
	{
		debugLevel = level;
		info("Set debug level to " + level.getName());
	}
}
