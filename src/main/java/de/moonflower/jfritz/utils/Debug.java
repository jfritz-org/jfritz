/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz.utils;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritzDataDirectory;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

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
	private static final Logger log = Logger.getLogger(Debug.class);

	public static final LogSeverity LS_OFF = new LogSeverity(0, "LS_OFF", "OFF");
	public static final LogSeverity LS_ALWAYS = new LogSeverity(1, "LS_ALWAYS", "");
	public static final LogSeverity LS_ERROR = new LogSeverity(1, "LS_ERROR", "ERROR");
	public static final LogSeverity LS_WARNING = new LogSeverity(2, "LS_WARNING", "WARN");
	public static final LogSeverity LS_INFO = new LogSeverity(3, "LS_INFO", "INFO");
	public static final LogSeverity LS_DEBUG = new LogSeverity(4, "LS_DEBUG", "DEBUG");

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 9211082107025215527L;

	public static String debugLogFilePath = JFritzDataDirectory.getInstance().getDataDirectory() + "debug.log";

	private static JPanel main_panel = null;

	private static JTextArea log_area;

	private static JButton close_button;
	private static JButton save_button;
	private static JButton refresh_button;
	private static JComboBox<LogSeverity> log_severity_box;

	private static JScrollPane scroll_pane;

	private static JFrame display_frame;

	protected static PropertyProvider properties = PropertyProvider.getInstance();
	protected static MessageProvider messages = MessageProvider.getInstance();

	private static BufferedReader in;
	
	/**
	 * Show error Dialog with message
	 *
	 * @param message
	 */
	public static void errDlg(String message) {
		JOptionPane.showMessageDialog(null, message,
				messages.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
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
		log_severity_box = new JComboBox<LogSeverity>();
		log_severity_box.addItem(LS_OFF);
		log_severity_box.addItem(LS_ERROR);
		log_severity_box.addItem(LS_WARNING);
		log_severity_box.addItem(LS_INFO);
		log_severity_box.addItem(LS_DEBUG);
		log_severity_box.setSelectedItem(LS_INFO);
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
				JFileChooser fc = new JFileChooser(JFritzDataDirectory.getInstance().getDataDirectory()); //$NON-NLS-1$
				fc.setDialogTitle(messages.getMessage("save_debug_log")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.isDirectory()
								|| f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
					}

					public String getDescription() {
						return messages.getMessage("debug_files"); //$NON-NLS-1$
					}
				});
				if (fc.showSaveDialog(display_frame) == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getPath();
					path = path.substring(0, path.length()
							- fc.getSelectedFile().getName().length());
					properties.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
					File file = fc.getSelectedFile();
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(display_frame, messages.getMessage(
								"overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
								messages.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
							try {
								log_area.write(new FileWriter(file.getAbsolutePath()));
							} catch (IOException e) {
								log.error("Could not save debug log to file: "+file.getAbsolutePath());
								e.printStackTrace();
							}
						}
					} else {
						try {
							FileWriter fw = new FileWriter(file.getAbsolutePath());
							log_area.write(fw);
							fw.close();
						} catch (IOException e) {
							log.error("Could not save debug log to file: "+file.getAbsolutePath());
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

			in = new BufferedReader(new FileReader(debugLogFilePath));
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
		LogSeverity ls = LS_ALWAYS;
		String[] splitted = line.split("\\|");
		if (splitted.length > 3) {
			String logLevel = splitted[2];
			if (LS_DEBUG.getPrefix().equals(logLevel)) {
				return LS_DEBUG;
			} else if (LS_INFO.getPrefix().equals(logLevel)) {
				return LS_INFO;
			} else if (LS_WARNING.getPrefix().equals(logLevel)) {
				return LS_WARNING;
			} else if (LS_ERROR.getPrefix().equals(logLevel)) {
				return LS_ERROR;
			} else {
				return LS_ALWAYS;
			}
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
}
