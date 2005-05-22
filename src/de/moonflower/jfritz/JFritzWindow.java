/**
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;

/**
 * This is main window class of JFritz, which creates the GUI.
 *
 * @author akw
 */
public class JFritzWindow extends JFrame implements Runnable, ActionListener,
		ItemListener {

	JFritz jfritz;

	JFritzProperties properties, participants;

	CallerTable callertable;

	TableModelEvent callertableevent;

	Timer timer;

	JMenuBar menu;

	JToolBar toolbar;

	JButton fetchButton, lookupButton, vcardButton;

	JToggleButton taskButton;

	JProgressBar progressbar;

	boolean isretrieving = false;

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		this.jfritz = jfritz;
		setProperties(jfritz.properties, jfritz.participants);
		createGUI();
		setVisible(true);
	}

	public void run() {
		jfritz.getCallerlist().loadFromXMLFile(JFritz.CALLS_FILE);
		jfritz.getCallerlist().sortAllRowsBy(1, false);
		setStatus();
	}

	private void createGUI() {
		setTitle(JFritz.PROGRAM_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeel();

		// Setting size and position
		int x = Integer.parseInt(properties.getProperty("position.left", "10"));
		int y = Integer.parseInt(properties.getProperty("position.top", "10"));
		int w = Integer.parseInt(properties
				.getProperty("position.width", "640"));
		int h = Integer.parseInt(properties.getProperty("position.height",
				"400"));
		setLocation(x, y);
		setSize(w, h);

		// Adding gui components
		getContentPane().setLayout(new BorderLayout());
		createMenu();
		createToolbar();
		createTable();
		createStatusbar();

		jfritz.getCallerlist().updateFilter();
		callertable.tableChanged(callertableevent);
	}

	public void setDefaultLookAndFeel() {
		setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(properties.getProperty("lookandfeel",
					UIManager.getCrossPlatformLookAndFeelClassName()));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create the status bar
	 */
	public void createStatusbar() {
		// TODO: This can be done nice (more info fields, etc.)
		progressbar = new JProgressBar();
		progressbar.setValue(0);
		progressbar.setStringPainted(true);
		setStatus();
		getContentPane().add(progressbar, BorderLayout.SOUTH);
	}

	/**
	 * Creates a tool bar
	 */
	public void createToolbar() {
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		fetchButton = new JButton();
		fetchButton.setToolTipText(jfritz.getMessages().getString("fetchlist"));
		fetchButton.setActionCommand("fetchList");
		fetchButton.addActionListener(this);
		fetchButton.setIcon(getImage("fetch.png"));
		fetchButton.setFocusPainted(false);
		toolbar.add(fetchButton);

		taskButton = new JToggleButton();
		taskButton.setToolTipText(jfritz.getMessages().getString("fetchtask"));
		taskButton.setActionCommand("fetchTask");
		taskButton.addActionListener(this);
		taskButton.setIcon(getImage("clock.png"));
		toolbar.add(taskButton);

		lookupButton = new JButton();
		lookupButton.setToolTipText(jfritz.getMessages().getString(
				"reverse_lookup"));
		lookupButton.setActionCommand("reverselookup");
		lookupButton.addActionListener(this);
		lookupButton.setIcon(getImage("reverselookup.png"));
		toolbar.add(lookupButton);

		JButton button = new JButton();
		button.setActionCommand("phonebook");
		button.addActionListener(this);
		button.setIcon(getImage("phonebook.png"));
		button.setToolTipText(jfritz.getMessages().getString("phonebook"));
		button.setEnabled(JFritz.DEVEL_VERSION);
		toolbar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial");
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png"));
		button.setToolTipText(jfritz.getMessages().getString("quickdial"));
		button.setEnabled(JFritz.DEVEL_VERSION);
		toolbar.add(button);

		toolbar.addSeparator();

		button = new JButton();
		button.setActionCommand("export_csv");
		button.addActionListener(this);
		button.setIcon(getImage("csv.png"));
		button.setToolTipText(jfritz.getMessages().getString("export_csv"));
		toolbar.add(button);

		vcardButton = new JButton();
		vcardButton.setActionCommand("export_vcard");
		vcardButton.addActionListener(this);
		vcardButton.setIcon(getImage("vcard.png"));
		vcardButton.setToolTipText(jfritz.getMessages().getString(
				"export_vcard"));
		toolbar.add(vcardButton);

		/*
		 * button = new JButton(); button.setActionCommand("export_excel");
		 * button.addActionListener(this); button.setIcon(new
		 * ImageIcon(Toolkit.getDefaultToolkit().getImage(
		 * getClass().getResource(
		 * "/de/moonflower/jfritz/resources/images/excel.png"))));
		 * button.setToolTipText(messages.getString("export_excel"));
		 * button.setEnabled(IS_RELEASE); toolbar.add(button);
		 *
		 * button = new JButton(); button.setActionCommand("export_openoffice");
		 * button.addActionListener(this); button.setIcon(new
		 * ImageIcon(Toolkit.getDefaultToolkit().getImage(
		 * getClass().getResource(
		 * "/de/moonflower/jfritz/resources/images/excel.png"))));
		 * button.setToolTipText(messages.getString("export_excel"));
		 * button.setEnabled(true); toolbar.add(button);
		 *
		 * button.setIcon(new
		 * ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(
		 * "/de/moonflower/jfritz/resources/images/openoffice.png"))));
		 * button.setToolTipText(messages.getString("export_openoffice"));
		 * button.setEnabled(IS_RELEASE); toolbar.add(button);
		 */
		button = new JButton();
		button.setActionCommand("help");
		button.addActionListener(this);
		button.setIcon(getImage("help.png"));
		button.setToolTipText(jfritz.getMessages().getString("help_menu"));
		button.setEnabled(JFritz.DEVEL_VERSION);
		toolbar.add(button);

		toolbar.addSeparator();

		button = new JButton();
		button.setActionCommand("config");
		button.addActionListener(this);
		button.setIcon(getImage("config.png"));
		button.setToolTipText(jfritz.getMessages().getString("config"));
		toolbar.add(button);

		toolbar.addSeparator();

		// FILTER BUTTONS

		JToggleButton tb = new JToggleButton(getImage("callin_grey.png"), true);
		tb.setSelectedIcon(getImage("callin.png"));
		tb.setActionCommand("filter_callin");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_callin"));
		tb.setSelected(!Boolean.parseBoolean(properties.getProperty(
				"filter.callin", "false")));
		toolbar.add(tb);

		tb = new JToggleButton(getImage("callinfailed_grey.png"), true);
		tb.setSelectedIcon(getImage("callinfailed.png"));
		tb.setActionCommand("filter_callinfailed");
		tb.addActionListener(this);
		tb
				.setToolTipText(jfritz.getMessages().getString(
						"filter_callinfailed"));
		tb.setSelected(!Boolean.parseBoolean(properties.getProperty(
				"filter.callinfailed", "false")));
		toolbar.add(tb);

		tb = new JToggleButton(getImage("callout_grey.png"), true);
		tb.setSelectedIcon(getImage("callout.png"));
		tb.setActionCommand("filter_callout");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_callout"));
		tb.setSelected(!Boolean.parseBoolean(properties.getProperty(
				"filter.callout", "false")));
		toolbar.add(tb);

		tb = new JToggleButton(getImage("phone_grey.png"), true);
		tb.setSelectedIcon(getImage("phone.png"));
		tb.setActionCommand("filter_number");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_number"));
		tb.setSelected(!Boolean.parseBoolean(properties.getProperty(
				"filter.number", "false")));
		toolbar.add(tb);

		getContentPane().add(toolbar, BorderLayout.NORTH);
	}

	/**
	 * Creates the caller table
	 *
	 */
	public void createTable() {
		callertable = new CallerTable(jfritz.getCallerlist(), jfritz
				.getMessages(), properties);
		callertableevent = new TableModelEvent(jfritz.getCallerlist());

		getContentPane().add(new JScrollPane(callertable), BorderLayout.CENTER);
	}

	/**
	 * Creates the menu bar
	 *
	 */
	public void createMenu() {
		JMenu jfritzMenu = new JMenu(JFritz.PROGRAM_NAME);
		JMenu editMenu = new JMenu(jfritz.getMessages().getString("edit_menu"));
		JMenu optionsMenu = new JMenu(jfritz.getMessages().getString(
				"options_menu"));
		JMenu helpMenu = new JMenu(jfritz.getMessages().getString("help_menu"));
		JMenu lnfMenu = new JMenu(jfritz.getMessages().getString("lnf_menu"));
		JMenu exportMenu = new JMenu(jfritz.getMessages().getString(
				"export_menu"));

		JMenuItem item = new JMenuItem(jfritz.getMessages().getString(
				"fetchlist"), 'a');
		item.setActionCommand("fetchList");
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(jfritz.getMessages().getString("reverse_lookup"),
				'l');
		item.setActionCommand("reverselookup");
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(jfritz.getMessages().getString("export_csv"), 'c');
		item.setActionCommand("export_csv");
		item.addActionListener(this);
		exportMenu.add(item);
		item = new JMenuItem(jfritz.getMessages().getString("export_excel"),
				'c');
		item.setActionCommand("export_excel");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		exportMenu.add(item);

		item = new JMenuItem(jfritz.getMessages()
				.getString("export_openoffice"), 'c');
		item.setActionCommand("export_openoffice");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		exportMenu.add(item);
		jfritzMenu.add(exportMenu);

		jfritzMenu.add(new JSeparator());
		item = new JMenuItem(jfritz.getMessages().getString("phonebook"), 'b');
		item.setActionCommand("phonebook");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		jfritzMenu.add(item);
		item = new JMenuItem(jfritz.getMessages().getString("quickdials"));
		item.setActionCommand("quickdial");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		jfritzMenu.add(item);
		jfritzMenu.add(new JSeparator());
		item = new JMenuItem(jfritz.getMessages().getString("prog_exit"), 'x');
		item.setActionCommand("exit");
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(jfritz.getMessages().getString("help_content"),
				'h');
		item.setActionCommand("help");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		helpMenu.add(item);
		item = new JMenuItem(jfritz.getMessages().getString("jfritz_website"),
				'w');
		item.setActionCommand("website");
		item.addActionListener(this);
		helpMenu.add(item);
		helpMenu.add(new JSeparator());
		item = new JMenuItem(jfritz.getMessages().getString("prog_info"), 'i');
		item.setActionCommand("about");
		item.addActionListener(this);
		helpMenu.add(item);

		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();
		for (int i = 0; i < lnfs.length; i++) {
			JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i]
					.getName());
			lnfMenu.add(rbmi);
			rbmi.setSelected(UIManager.getLookAndFeel().getClass().getName()
					.equals(lnfs[i].getClassName()));
			rbmi.putClientProperty("lnf name", lnfs[i]);
			rbmi.addItemListener(this);
			lnfgroup.add(rbmi);
		}
		optionsMenu.add(lnfMenu);
		item = new JMenuItem(jfritz.getMessages().getString("config"), 'e');
		item.setActionCommand("config");
		item.addActionListener(this);
		optionsMenu.add(item);

		menu = new JMenuBar();
		menu.add(jfritzMenu);
		menu.add(optionsMenu);
		menu.add(helpMenu);
		setJMenuBar(menu);
	}

	/**
	 * start/stop timer for cyclic caller list fetching
	 *
	 * @param enabled
	 */
	private void fetchTask(boolean enabled) {
		if (enabled) {
			timer = new Timer();
			timer.schedule(new FetchListTask(this), 5000, Integer
					.parseInt(properties.getProperty("fetch.timer")) * 60000);
			Debug.msg("Timer enabled");
		} else {
			timer.cancel();
			Debug.msg("Timer disabled");
		}
	}

	/**
	 * Fetches list from box
	 */
	public void fetchList() {
		if (!isretrieving) { // Prevent multiple clicking
			isretrieving = true;
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					while (!isdone) {
						try {
							setBusy(true);
							setStatus(jfritz.getMessages().getString(
									"fetchdata"));
							jfritz.getCallerlist().getNewCalls();
							isdone = true;
						} catch (WrongPasswordException e) {
							setBusy(false);
							setStatus(jfritz.getMessages().getString(
									"password_wrong"));
							String password = showPasswordDialog(properties
									.getProperty("box.password"));
							if (!password.equals("")) {
								properties
										.setProperty("box.password", password);
							} else { // Cancel
								isdone = true;
							}
						} catch (IOException e) {
							setBusy(false);
							setStatus(jfritz.getMessages().getString(
									"box_not_found"));
							String box_address = showAddressDialog(properties
									.getProperty("box.address"));
							if (!box_address.equals("")) {
								properties.setProperty("box.address",
										box_address);
							} else { // Cancel
								isdone = true;
							}
						}
					}
					return null;
				}

				public void finished() {
					setBusy(false);
					setStatus();
					jfritz.getCallerlist().sortAllRowsBy(1, false);
					jfritz.getCallerlist().updateFilter();
					callertable.tableChanged(callertableevent);
					isretrieving = false;
				}
			};
			worker.start();
		}
	}

	/**
	 * Does a reverse lookup for the whole list
	 */
	public void reverseLookup() {
		if (!isretrieving) { // Prevent multiple clicking
			isretrieving = true;
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					while (!isdone) {
						setBusy(true);
						setStatus(jfritz.getMessages().getString(
								"reverse_lookup"));
						for (int i = 0; i < jfritz.getCallerlist()
								.getRowCount(); i++) {
							Vector data = jfritz.getCallerlist()
									.getFilteredCallVector();
							Call call = (Call) data.get(i);
							String number = call.getNumber();
							String participant = jfritz.getCallerlist()
									.getParticipantFromNumber(call.getNumber());
							if (!number.equals("") && (participant.equals(""))) {
								setStatus(jfritz.getMessages().getString(
										"reverse_lookup_for")
										+ " " + number + " ...");
								Debug.msg("Reverse lookup for " + number);
								if (participant.equals("")) {
									participant = ReverseLookup.lookup(number);
								}
								if (!participant.equals("")) {
									participants.setProperty(number,
											participant);
									jfritz.getCallerlist().setParticipant(
											participant, i);
								}
							}
						}

						isdone = true;
					}
					return null;
				}

				public void finished() {
					setBusy(false);
					isretrieving = false;
					int rows = jfritz.getCallerlist().getRowCount();
					setStatus();
				}
			};
			worker.start();
		} else {
			Debug.err("Multiple clicking is disabled..");
		}
	}

	/**
	 * Shows the quick dial dialog
	 *
	 * TODO: A lot..
	 */
	private void showQuickDialDialog() {
		QuickDialDialog p = new QuickDialDialog(this);

		//p.setValues(properties);
		if (p.showDialog()) {
			//			p.storeValues(properties);
			//			saveProperties();
		}
		p.dispose();
		p = null;
	}

	/**
	 * Shows the configuration dialog
	 */
	private void showConfigDialog() {
		ConfigDialog p = new ConfigDialog(this);
		p.setValues(properties);
		if (p.showDialog()) {
			p.storeValues(properties);
			jfritz.saveProperties();
		}
		p.dispose();
		p = null;
	}

	/**
	 * Shows the password dialog
	 *
	 * @param old_password
	 * @return new_password
	 */
	public String showPasswordDialog(String old_password) {
		String password = "";
		AddressPasswordDialog p = new AddressPasswordDialog(this, true);
		p.setPass(old_password);

		if (p.showDialog()) {
			password = p.getPass();
		}
		p.dispose();
		p = null;
		return password;
	}

	/**
	 * Shows the phone book
	 */
	public void showPhoneBook() {
		// TODO: A phonebook (jtable) in which the participants can be edited.

		// PhoneBook pb = new PhoneBook(this);
	}

	/**
	 * Shows the address dialog
	 *
	 * @param old_address
	 * @return address
	 */
	public String showAddressDialog(String old_address) {
		String address = "";
		AddressPasswordDialog p = new AddressPasswordDialog(this, false);
		p.setAddress(old_address);

		if (p.showDialog()) {
			address = p.getAddress();
		}
		p.dispose();
		p = null;
		return address;

	}

	/**
	 * Shows the about dialog
	 */
	public void showAboutDialog() {
		JOptionPane.showMessageDialog(this, JFritz.PROGRAM_NAME + " v"
				+ JFritz.PROGRAM_VERSION + "\n"
				+ JFritzUtils.getVersionFromCVSTag(JFritz.CVS_TAG) + "\n\n"
				+ "(c) 2005 by " + JFritz.PROGRAM_AUTHOR + "\n\n"
				+ "This tool is developed and released under\n"
				+ "the terms of the GNU General Public License\n\n"
				+ "Long live Free Software!");
	}

	/**
	 * Shows the exit dialog
	 */
	public void showExitDialog() {
		int exit = JOptionPane.showConfirmDialog(this, jfritz.getMessages()
				.getString("really_quit"), JFritz.PROGRAM_NAME,
				JOptionPane.YES_NO_OPTION);
		if (exit == JOptionPane.YES_OPTION) {
			jfritz.saveProperties();
			System.exit(0);
		}
	}

	/**
	 * Listener for window events
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			showExitDialog();
		} else {
			super.processWindowEvent(e);
		}
	}

	/**
	 * ItemListener for LookAndFeel Menu
	 *
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) ie.getSource();
		if (rbmi.isSelected()) {
			UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) rbmi
					.getClientProperty("lnf name");
			try {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(this);
				properties.setProperty("lookandfeel", info.getClassName());
			} catch (Exception e) {
				Debug.err("Unable to set UI " + e.getMessage());
			}
		}
	}

	/**
	 * Sets standard info into the status bar
	 *
	 */
	public void setStatus() {
		progressbar.setString(jfritz.getCallerlist().getRowCount() + " "
				+ jfritz.getMessages().getString("entries") + ", "
				+ jfritz.getMessages().getString("total_duration") + ": "
				+ (jfritz.getCallerlist().getTotalDuration() / 60) + " min");
	}

	/**
	 * Sets text in the status bar
	 *
	 * @param status
	 */
	public void setStatus(String status) {
		if (status.equals(""))
			setStatus();
		else
			progressbar.setString(status);
	}

	/**
	 * Sets the busy mode of progress bar
	 *
	 * @param busy
	 */
	public void setBusy(boolean busy) {
		/*
		 * TODO: Activate this code when all buttons are implemented Component[]
		 * c = toolbar.getComponents(); for (int i=0;i <c.length;i++) { if
		 * (c[i].getClass().equals(JButton.class)) c[i].setEnabled(!busy); }
		 */
		fetchButton.setEnabled(!busy);
		lookupButton.setEnabled(!busy);

		menu.setEnabled(!busy);
		progressbar.setIndeterminate(busy);
		if (busy)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Action Listener for menu and toolbar
	 */
	public void actionPerformed(ActionEvent e) {
		Debug.msg("Action " + e.getActionCommand());
		if (e.getActionCommand() == "exit") {
			showExitDialog();
		} else if (e.getActionCommand() == "about") {
			showAboutDialog();
		} else if (e.getActionCommand() == "help") {
			Debug.err("No help available yet");
		} else if (e.getActionCommand() == "website") {
			final String url = "http://jfritz.sourceforge.net/";
			try {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} catch (IOException e1) {
				Debug.err("Website opening works only on win32 platforms.");
			}
		} else if (e.getActionCommand() == "export_csv") {

			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(jfritz.getMessages().getString("export_csv"));
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			fc.setSelectedFile(new File(JFritz.CALLS_CSV_FILE));
			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".csv");
				}

				public String getDescription() {
					return "CSV-Dateien";
				}
			});

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				jfritz.getCallerlist().saveToCSVFile(file.getAbsolutePath());
			}
		} else if (e.getActionCommand() == "export_vcard") {
			if (callertable.getSelectedRow() >= 0
					&& !((String) callertable.getModel().getValueAt(
							callertable.getSelectedRow(), 3)).startsWith("?")
					&& !((String) callertable.getModel().getValueAt(
							callertable.getSelectedRow(), 3)).equals("")) {
				String name = (String) callertable.getModel().getValueAt(
						callertable.getSelectedRow(), 3);
				String number = (String) callertable.getModel().getValueAt(
						callertable.getSelectedRow(), 2);
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle(jfritz.getMessages()
						.getString("export_vcard"));
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
				fc.setSelectedFile(new File(number + ".vcf"));
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.isDirectory()
								|| f.getName().toLowerCase().endsWith(".vcf");
					}

					public String getDescription() {
						return "VCard (.vcf)";
					}
				});

				if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					VCard vcard = new VCard(name, number);
					vcard.saveToFile(file);
				}
			} else {
				Debug.err("No valid row selected");
			}
		} else if (e.getActionCommand() == "config") {
			showConfigDialog();
		} else if (e.getActionCommand() == "phonebook") {
			showPhoneBook();
		} else if (e.getActionCommand() == "quickdial") {
			showQuickDialDialog();
		} else if (e.getActionCommand() == "fetchList") {
			fetchList();
		} else if (e.getActionCommand() == "fetchTask") {
			fetchTask(((JToggleButton) e.getSource()).isSelected());
		} else if (e.getActionCommand() == "reverselookup") {
			reverseLookup();
		} else if (e.getActionCommand() == "filter_callin") {
			properties.setProperty("filter.callin", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			callertable.tableChanged(callertableevent);
		} else if (e.getActionCommand() == "filter_callinfailed") {
			properties.setProperty("filter.callinfailed", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			callertable.tableChanged(callertableevent);
		} else if (e.getActionCommand() == "filter_callout") {
			properties.setProperty("filter.callout", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			callertable.tableChanged(callertableevent);
		} else if (e.getActionCommand() == "filter_number") {
			properties.setProperty("filter.number", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			callertable.tableChanged(callertableevent);
		} else {
			Debug.err("Unimplemented action: " + e.getActionCommand());
		}
	}

	/**
	 * @return Returns the messages.
	 */
	public ResourceBundle getMessages() {
		return jfritz.getMessages();
	}

	/**
	 * @return Returns the properties.
	 */
	public final JFritzProperties getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            The properties to set.
	 */
	public final void setProperties(JFritzProperties properties,
			JFritzProperties participants) {
		this.properties = properties;
		this.participants = participants;
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename)));
	}

}