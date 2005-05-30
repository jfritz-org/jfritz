/**
 */

package de.moonflower.jfritz.window;

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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.jdic.desktop.Desktop;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.AddressPasswordDialog;
import de.moonflower.jfritz.dialogs.ConfigDialog;
import de.moonflower.jfritz.dialogs.phonebook.Person;
import de.moonflower.jfritz.dialogs.phonebook.PhoneBookDialog;
import de.moonflower.jfritz.dialogs.quickdial.QuickDialDialog;
import de.moonflower.jfritz.dialogs.stats.StatsDialog;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.SwingWorker;
import de.moonflower.jfritz.vcard.VCard;
import de.moonflower.jfritz.vcard.VCardList;

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

	Timer timer;

	JMenuBar menu;

	JToolBar mBar;

	JButton fetchButton, lookupButton, configButton, vcardButton;

	JToggleButton dateButton;

	JTextField searchFilter;

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
		setProperties(jfritz.getProperties(), jfritz.getParticipants());
		createGUI();
		if (!properties.getProperty("option.startMinimized", "false").equals(
				"true")) {
			setVisible(true);
		}
		if (properties.getProperty("option.timerAfterStart", "false").equals(
				"true")) {
			taskButton.doClick();
		}
		if (properties.getProperty("option.fetchAfterStart", "false").equals(
				"true")) {
			fetchButton.doClick();
		}

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

		jfritz.getCallerlist().fireTableStructureChanged();
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
		getContentPane().add(progressbar, BorderLayout.SOUTH);
	}

	/**
	 * Creates a tool bar
	 */
	public void createToolbar() {
		mBar = new JToolBar();
		mBar.setFloatable(false);

		JToolBar fBar = new JToolBar();
		fBar.setFloatable(false);

		fetchButton = new JButton();
		fetchButton.setToolTipText(jfritz.getMessages().getString("fetchlist"));
		fetchButton.setActionCommand("fetchList");
		fetchButton.addActionListener(this);
		fetchButton.setIcon(getImage("fetch.png"));
		fetchButton.setFocusPainted(false);
		mBar.add(fetchButton);
		taskButton = new JToggleButton();
		taskButton.setToolTipText(jfritz.getMessages().getString("fetchtask"));
		taskButton.setActionCommand("fetchTask");
		taskButton.addActionListener(this);
		taskButton.setIcon(getImage("clock.png"));
		mBar.add(taskButton);

		lookupButton = new JButton();
		lookupButton.setToolTipText(jfritz.getMessages().getString(
				"reverse_lookup"));
		lookupButton.setActionCommand("reverselookup");
		lookupButton.addActionListener(this);
		lookupButton.setIcon(getImage("reverselookup.png"));
		mBar.add(lookupButton);

		JButton button = new JButton();
		button.setActionCommand("phonebook");
		button.addActionListener(this);
		button.setIcon(getImage("phonebook.png"));
		button.setToolTipText(jfritz.getMessages().getString("phonebook"));
		button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial");
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png"));
		button.setToolTipText(jfritz.getMessages().getString("quickdials"));
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		mBar.addSeparator();

		button = new JButton();
		button.setActionCommand("export_csv");
		button.addActionListener(this);
		button.setIcon(getImage("csv.png"));
		button.setToolTipText(jfritz.getMessages().getString("export_csv"));
		mBar.add(button);

		vcardButton = new JButton();
		vcardButton.setActionCommand("export_vcard");
		vcardButton.addActionListener(this);
		vcardButton.setIcon(getImage("vcard.png"));
		vcardButton.setToolTipText(jfritz.getMessages().getString(
				"export_vcard"));
		mBar.add(vcardButton);

		button = new JButton();
		button.setActionCommand("stats");
		button.addActionListener(this);
		button.setIcon(getImage("stats.png"));
		button.setToolTipText(jfritz.getMessages().getString("stats"));
		button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

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
		mBar.add(button);

		mBar.addSeparator();

		configButton = new JButton();
		configButton.setActionCommand("config");
		configButton.addActionListener(this);
		configButton.setIcon(getImage("config.png"));
		configButton.setToolTipText(jfritz.getMessages().getString("config"));
		mBar.add(configButton);

		mBar.addSeparator();

		// FILTER TOOLBAR

		JToggleButton tb = new JToggleButton(getImage("callin_grey.png"), true);
		tb.setSelectedIcon(getImage("callin.png"));
		tb.setActionCommand("filter_callin");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_callin"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callin", "false")));
		fBar.add(tb);

		tb = new JToggleButton(getImage("callinfailed_grey.png"), true);
		tb.setSelectedIcon(getImage("callinfailed.png"));
		tb.setActionCommand("filter_callinfailed");
		tb.addActionListener(this);
		tb
				.setToolTipText(jfritz.getMessages().getString(
						"filter_callinfailed"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callinfailed", "false")));
		fBar.add(tb);

		tb = new JToggleButton(getImage("callout_grey.png"), true);
		tb.setSelectedIcon(getImage("callout.png"));
		tb.setActionCommand("filter_callout");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_callout"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callout", "false")));
		fBar.add(tb);

		tb = new JToggleButton(getImage("phone_grey.png"), true);
		tb.setSelectedIcon(getImage("phone.png"));
		tb.setActionCommand("filter_number");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_number"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.number", "false")));
		fBar.add(tb);

		tb = new JToggleButton(getImage("handy_grey.png"), true);
		tb.setSelectedIcon(getImage("handy.png"));
		tb.setActionCommand("filter_handy");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_handy"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.handy", "false")));
		fBar.add(tb);

		dateButton = new JToggleButton(getImage("calendar_grey.png"), true);
		dateButton.setSelectedIcon(getImage("calendar.png"));
		dateButton.setActionCommand("filter_date");
		dateButton.addActionListener(this);
		dateButton
				.setToolTipText(jfritz.getMessages().getString("filter_date"));
		dateButton.setSelected(!JFritzUtils.parseBoolean(properties
				.getProperty("filter.date", "false")));
		setDateFilterText();
		fBar.add(dateButton);

		fBar.addSeparator();

		fBar.add(new JLabel(jfritz.getMessages().getString("search") + ": "));
		searchFilter = new JTextField(properties.getProperty("filter.search",
				""), 10);
		searchFilter.addCaretListener(new CaretListener() {
			String filter = "";

			public void caretUpdate(CaretEvent e) {
				JTextField search = (JTextField) e.getSource();
				if (!filter.equals(search.getText())) {
					filter = search.getText();
					properties.setProperty("filter.search", filter);
					jfritz.getCallerlist().updateFilter();
					jfritz.getCallerlist().fireTableStructureChanged();
				}
			}

		});

		fBar.add(searchFilter);
		button = new JButton(jfritz.getMessages().getString("clear"));
		button.setActionCommand("clearSearchFilter");
		button.addActionListener(this);
		fBar.add(button);

		JPanel toolPanel = new JPanel(new BorderLayout());

		toolPanel.add(mBar, BorderLayout.NORTH);
		//mBar.add(fBar);
		toolPanel.add(fBar, BorderLayout.CENTER);
		getContentPane().add(toolPanel, BorderLayout.NORTH);
		//		getContentPane().add(toolbar, BorderLayout.NORTH);
		//		getContentPane().add(search, BorderLayout.NORTH);
	}

	/**
	 * Creates the caller table
	 *
	 */
	public void createTable() {
		callertable = new CallerTable(jfritz.getCallerlist(), jfritz
				.getMessages(), properties);
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

		item = new JMenuItem(jfritz.getMessages().getString("export_vcard"),
				'v');
		item.setActionCommand("export_vcard");
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

			timer
					.schedule(new TimerTask() {

						public void run() {
							Debug.msg("Running FetchListTask..");
							jfritz.getJframe().fetchList();
						}

					}, 5000, Integer.parseInt(properties
							.getProperty("fetch.timer")) * 60000);
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
					jfritz.getCallerlist().fireTableStructureChanged();
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
							String participant = call.getParticipant();
							if (!number.equals("") && (participant.equals(""))) {
								setStatus(jfritz.getMessages().getString(
										"reverse_lookup_for")
										+ " " + number + " ...");
								Debug.msg("Reverse lookup for " + number);
								if (participant.equals("")) {
									Person newPerson;
									participant = ReverseLookup.lookup(number);
									if (participant.equals("?")) {
										newPerson = new Person("?", "", "?",
												"", "", "", call.getNumber(),
												"", "", "", call.getNumber(),
												"", "");
									} else if (participant.equals("? (Mobil)")) {
										newPerson = new Person("?", "", "?",
												"", "", "", "", call
														.getNumber(), "", "",
												call.getNumber(), "", "");
									} else if (participant
											.equals("? (Freecall)")) {
										newPerson = new Person("?", "", "?",
												"", "", "", call.getNumber(),
												"", "", "", call.getNumber(),
												"", "");
									} else {
										if (participant.indexOf(",") > -1) {
											// Found Firstname and Lastname,
											// separated by ,
											String list[] = participant
													.split(",");
											System.out.println(list[0] + " "
													+ list[1]);
											newPerson = new Person(list[1], "",
													list[0], "", "", "", call
															.getNumber(), "",
													"", "", call.getNumber(),
													"", "");
										} else {
											// Found only one name
											// Probably a company
											newPerson = new Person("", "",
													participant, "", "", "",
													"", "", call.getNumber(),
													"", call.getNumber(), "",
													"");
										}
									}
									jfritz.getPhonebook().addEntry(newPerson);
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
					// jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
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
		QuickDialDialog dialog = new QuickDialDialog(this);
		if (dialog.showDialog()) {
			dialog.getDataModel().saveToXMLFile(JFritz.QUICKDIALS_FILE);
		}
		dialog.dispose();
	}

	/**
	 * Shows the stats dialog
	 *
	 * TODO: A lot..
	 */
	private void showStatsDialog() {
		StatsDialog dialog = new StatsDialog(this);
		if (dialog.showDialog()) {
		}
		dialog.dispose();
	}

	/**
	 * Shows the configuration dialog
	 */
	private void showConfigDialog() {
		ConfigDialog dialog = new ConfigDialog(this);
		if (dialog.showDialog()) {
			dialog.storeValues(properties);
			jfritz.saveProperties();
		}
		dialog.dispose();
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

		PhoneBookDialog pb = new PhoneBookDialog(this, jfritz);

		if (pb.showDialog()) {
		}
		pb.dispose();
		pb = null;

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
				+ JFritz.PROGRAM_URL + "\n\n"
				+ "This tool is developed and released under\n"
				+ "the terms of the GNU General Public License\n\n"
				+ "Long live Free Software!");
	}

	/**
	 * Shows the exit dialog
	 */
	public void showExitDialog() {
		// FIXME Option for direct closing
		boolean exit = true;

		if (properties.getProperty("option.confirmOnExit", "true") == "true")
			exit = JOptionPane.showConfirmDialog(this, jfritz.getMessages()
					.getString("really_quit"), JFritz.PROGRAM_NAME,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

		if (exit) {
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
		String status = jfritz.getCallerlist().getRowCount() + " "
				+ jfritz.getMessages().getString("entries") + ", "
				+ jfritz.getMessages().getString("total_duration") + ": "
				+ (jfritz.getCallerlist().getTotalDuration() / 60) + " min";
		progressbar.setString(status);
	}

	/**
	 * Sets text in the status bar
	 *
	 * @param status
	 */
	public void setStatus(String status) {
		if (status.equals(""))
			setStatus();
		else {
			progressbar.setString(status);
			// jfritz.infoMsg(status);
		}
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
		configButton.setEnabled(!busy);

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
			try {
				Desktop.browse(new URL(JFritz.DOCUMENTATION_URL));
			} catch (Exception e1) {
				Debug.err("Website opening works only on win32 platforms.");
				JOptionPane.showMessageDialog(this, "Please visit "
						+ JFritz.DOCUMENTATION_URL);
			}
		} else if (e.getActionCommand() == "website") {
			try {
				// Runtime.getRuntime().exec("rundll32
				// url.dll,FileProtocolHandler "+ JFritz.PROGRAM_URL);
				Desktop.browse(new URL(JFritz.PROGRAM_URL));
			} catch (Exception e1) {
				Debug.err("Website opening works only on win32 platforms.");
				JOptionPane.showMessageDialog(this, "Please visit "
						+ JFritz.PROGRAM_URL);
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
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				jfritz.getCallerlist().saveToCSVFile(file.getAbsolutePath());
			}
		} else if (e.getActionCommand() == "export_vcard") {
			exportVCard();
		} else if (e.getActionCommand() == "config") {
			showConfigDialog();
		} else if (e.getActionCommand() == "phonebook") {
			showPhoneBook();
		} else if (e.getActionCommand() == "quickdial") {
			showQuickDialDialog();
		} else if (e.getActionCommand() == "stats") {
			showStatsDialog();

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
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_callinfailed") {
			properties.setProperty("filter.callinfailed", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_callout") {
			properties.setProperty("filter.callout", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_number") {
			properties.setProperty("filter.number", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_handy") {
			properties.setProperty("filter.handy", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_date") {
			properties.setProperty("filter.date", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setDataFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "clearSearchFilter") {
			searchFilter.setText("");
			properties.setProperty("filter.search", "");
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else {
			Debug.err("Unimplemented action: " + e.getActionCommand());
		}
	}

	private void setDataFilterFromSelection() {
		Date from = null;
		Date to = null;
		try {
			int rows[] = getCallertable().getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) jfritz.getCallerlist()
						.getFilteredCallVector().get(rows[i]);

				if (to == null || call.getCalldate().after(to))
					to = call.getCalldate();

				if (from == null || call.getCalldate().before(from))
					from = call.getCalldate();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		if (to == null)
			to = new Date();
		if (from == null)
			from = new Date();
		String fromstr = new SimpleDateFormat("dd.MM.yy").format(from);
		String tostr = new SimpleDateFormat("dd.MM.yy").format(to);

		properties.setProperty("filter.date_from", fromstr);
		properties.setProperty("filter.date_to", tostr);
		setDateFilterText();
		jfritz.getCallerlist().updateFilter();
	}

	/**
	 *
	 */
	public void setDateFilterText() {
		if (JFritzUtils.parseBoolean(properties.getProperty("filter.date"))) {
			if (properties.getProperty("filter.date_from").equals(
					properties.getProperty("filter.date_to"))) {
				dateButton.setText(properties.getProperty("filter.date_from"));
			} else {
				dateButton.setText(properties.getProperty("filter.date_from")
						+ " - " + properties.getProperty("filter.date_to"));
			}
		} else {
			dateButton.setText("");
		}
	}

	/**
	 * Exports VCard or VCardList
	 */
	public void exportVCard() {
		VCardList list = new VCardList();
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(jfritz.getMessages().getString("export_vcard"));
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".vcf");
			}

			public String getDescription() {
				return "VCard (.vcf)";
			}
		});

		int rows[] = callertable.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			String name = (String) callertable.getModel()
					.getValueAt(rows[i], 3);
			String number = (String) callertable.getModel().getValueAt(rows[i],
					2);
			if (!name.startsWith("?") && !number.equals("")) {
				list.addVCard(new VCard(name, number));
			}
		}
		if (list.getCount() > 0) {
			if (list.getCount() == 1) {
				fc
						.setSelectedFile(new File(list.getVCard(0).getFon()
								+ ".vcf"));
			} else if (list.getCount() > 1) {
				fc.setSelectedFile(new File("jfritz.vcf"));
			}
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				list.saveToFile(file);
			}
		} else {
			Debug.err("No single valid row selected");
			JOptionPane.showMessageDialog(this,
					"Fehler:\n\nKeine einzige sinnvolle Zeile selektiert!\n\n"
							+ "Bitte eine oder mehrere Zeilen auswählen,\n"
							+ "um die Daten als VCard zu exportieren!");

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

	/**
	 * @return Returns the JFritz object.
	 */
	public final JFritz getJFritz() {
		return jfritz;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
	}

	/**
	 * @return Returns the callertable.
	 */
	public final CallerTable getCallertable() {
		return callertable;
	}
}