/**
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;

import de.moonflower.jfritz.callerlist.CallerListPanel;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.dialogs.config.ConfigDialog;
import de.moonflower.jfritz.dialogs.phonebook.PhoneBookPanel;
import de.moonflower.jfritz.dialogs.quickdial.QuickDialPanel;
import de.moonflower.jfritz.dialogs.simple.AddressPasswordDialog;
import de.moonflower.jfritz.dialogs.stats.StatsDialog;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.DirectoryChooser;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.ImportOutlookContacts;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.PrintCallerList;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.SwingWorker;
import de.moonflower.jfritz.utils.network.CallmessageListener;
import de.moonflower.jfritz.utils.network.FBoxListenerV1;
import de.moonflower.jfritz.utils.network.FBoxListenerV3;
import de.moonflower.jfritz.utils.network.SyslogListener;
import de.moonflower.jfritz.utils.network.TelnetListener;
import de.moonflower.jfritz.utils.network.YAClistener;

/**
 * This is main window class of JFritz, which creates the GUI.
 *
 * @author akw
 */
public class JFritzWindow extends JFrame
		implements
			Runnable,
			ActionListener,
			ItemListener {

	private static final long serialVersionUID = 1;

	private JFritz jfritz;

	private Timer timer;

	private JMenuBar menu;

	private JToolBar mBar;

	private JButton fetchButton, lookupButton, configButton;

	private JToggleButton taskButton, monitorButton;

	private JProgressBar progressbar;

	private boolean isretrieving = false;

	private JTabbedPane tabber;

	private CallerListPanel callerListPanel;

	private PhoneBookPanel phoneBookPanel;

	private QuickDialPanel quickDialPanel;

	private ConfigDialog configDialog;

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		this.jfritz = jfritz;
		Debug.msg("Create JFritz-GUI");
		createGUI();
	}

	public void checkStartOptions() {
		if (!JFritz.getProperty("option.startMinimized", "false")
				.equals("true")) {
			setVisible(true);
		} else {
			if (!JFritz.SYSTRAY_SUPPORT)
				setVisible(true);
			setState(JFrame.ICONIFIED);
		}
		if (JFritz.getProperty("option.timerAfterStart", "false")
				.equals("true")) {
			taskButton.doClick();
		}
		if (JFritz.getProperty("option.fetchAfterStart", "false")
				.equals("true")) {
			fetchButton.doClick();
		}
		if (JFritz.getProperty("option.autostartcallmonitor", "false").equals(
				"true")) {
			startChosenCallMonitor();
		}
		setStatus();
	}

	private void createGUI() {
		setTitle(JFritz.PROGRAM_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeel();
		ShutdownThread shutdownThread = new ShutdownThread(jfritz);
		Runtime.getRuntime().addShutdownHook(shutdownThread);

		addKeyListener(KeyEvent.VK_F5, "F5");

		this
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								getClass()
										.getResource(
												"/de/moonflower/jfritz/resources/images/trayicon.png")));

		// Setting size and position
		int x = Integer.parseInt(JFritz.getProperty("position.left", "10"));
		int y = Integer.parseInt(JFritz.getProperty("position.top", "10"));
		int w = Integer.parseInt(JFritz.getProperty("position.width", "640"));
		int h = Integer.parseInt(JFritz.getProperty("position.height", "400"));
		setLocation(x, y);
		setSize(w, h);

		callerListPanel = new CallerListPanel(jfritz);
		phoneBookPanel = new PhoneBookPanel(jfritz);
		quickDialPanel = new QuickDialPanel(jfritz);

		tabber = new JTabbedPane(JTabbedPane.BOTTOM);
		tabber.addTab(JFritz.getMessage("callerlist"), callerListPanel);
		tabber.addTab(JFritz.getMessage("phonebook"), phoneBookPanel);
		tabber.addTab(JFritz.getMessage("quickdials"), quickDialPanel);
		tabber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("callerlist"))) {
					setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("phonebook"))) {
					phoneBookPanel.setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("quickdials"))) {
					quickDialPanel.setStatus();
				}
			}
		});

		// Adding gui components
		setJMenuBar(createMenu());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createMainToolBar(), BorderLayout.NORTH);
		getContentPane().add(tabber, BorderLayout.CENTER);
		getContentPane().add(createStatusBar(), BorderLayout.SOUTH);

		jfritz.getCallerlist().fireTableDataChanged();
		jfritz.getCallerlist().fireTableStructureChanged();
		String ask = JFritz.getProperty("jfritz.password", Encryption
				.encrypt(JFritz.PROGRAM_SECRET + ""));
		String pass = JFritz
				.getProperty("box.password", Encryption.encrypt(""));
		if (!Encryption.decrypt(ask).equals(
				JFritz.PROGRAM_SECRET + Encryption.decrypt(pass))) {
			String password = showPasswordDialog("");
			if (password == null) { // PasswordDialog canceled
				Debug.errDlg(JFritz.getMessage("input_canceled"));
				Debug.err("Eingabe abgebrochen");
				System.exit(0);
			} else if (!password.equals(Encryption.decrypt(pass))) {
				Debug.errDlg(JFritz.getMessage("wrong_password"));
				Debug.err(JFritz.getMessage("wrong_password"));
				System.exit(0);
			}
		}

	}

	/**
	 * Sets default Look'n'Feel
	 */
	public void setDefaultLookAndFeel() {
		setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(JFritz.getProperty("lookandfeel",
					UIManager.getSystemLookAndFeelClassName()));
			// Wunsch eines MAC Users, dass das Default LookAndFeel des
			// Betriebssystems genommen wird

		} catch (Exception ex) {
			Debug.err(ex.toString());
		}
	}

	/**
	 * Create the StatusBar
	 */
	public JProgressBar createStatusBar() {
		progressbar = new JProgressBar();
		progressbar.setValue(0);
		progressbar.setStringPainted(true);
		return progressbar;
	}

	/**
	 * Creates the main ToolBar
	 */
	public JToolBar createMainToolBar() {
		mBar = new JToolBar();
		mBar.setFloatable(true);

		fetchButton = new JButton();
		fetchButton.setToolTipText(JFritz.getMessage("fetchlist"));
		fetchButton.setActionCommand("fetchList");
		fetchButton.addActionListener(this);
		fetchButton.setIcon(getImage("fetch.png"));
		fetchButton.setFocusPainted(false);
		mBar.add(fetchButton);

		JButton button = new JButton();
//		button = new JButton();
//		button.setActionCommand("call");
//		button.addActionListener(this);
//		button.setIcon(getImage("Phone.gif"));
//		button.setToolTipText(JFritz.getMessage("call"));
//		mBar.add(button);

		taskButton = new JToggleButton();
		taskButton.setToolTipText(JFritz.getMessage("fetchtask"));
		taskButton.setActionCommand("fetchTask");
		taskButton.addActionListener(this);
		taskButton.setIcon(getImage("clock.png"));
		mBar.add(taskButton);

		monitorButton = new JToggleButton();
		monitorButton.setToolTipText(JFritz.getMessage("callmonitor"));
		monitorButton.setActionCommand("callMonitor");
		monitorButton.addActionListener(this);
		monitorButton.setIcon(getImage("monitor.png"));
		mBar.add(monitorButton);

		lookupButton = new JButton();
		lookupButton.setToolTipText(JFritz.getMessage("reverse_lookup"));
		lookupButton.setActionCommand("reverselookup");
		lookupButton.addActionListener(this);
		lookupButton.setIcon(getImage("reverselookup.png"));
		mBar.add(lookupButton);

		button = new JButton();
		button.setActionCommand("phonebook");
		button.addActionListener(this);
		button.setIcon(getImage("phonebook.png"));
		button.setToolTipText(JFritz.getMessage("phonebook"));
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial");
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png"));
		button.setToolTipText(JFritz.getMessage("quickdials"));
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("delete_fritzbox_callerlist");
		button.addActionListener(this);
		button.setIcon(getImage("DeleteList.gif"));
		button.setToolTipText(JFritz.getMessage("delete_fritzbox_callerlist"));
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("backup");
		button.addActionListener(this);
		button.setIcon(getImage("Backup.gif"));
		button.setToolTipText(JFritz.getMessage("backup"));
		mBar.add(button);

		mBar.addSeparator();

		button = new JButton();
		button.setActionCommand("stats");
		button.addActionListener(this);
		button.setIcon(getImage("stats.png"));
		button.setToolTipText(JFritz.getMessage("stats"));
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("help");
		button.addActionListener(this);
		button.setIcon(getImage("help.png"));
		button.setToolTipText(JFritz.getMessage("help_menu"));
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		mBar.addSeparator();

		configButton = new JButton();
		configButton.setActionCommand("config");
		configButton.addActionListener(this);
		configButton.setIcon(getImage("config.png"));
		configButton.setToolTipText(JFritz.getMessage("config"));
		mBar.add(configButton);

		mBar.addSeparator();
		return mBar;
	}

	/**
	 * Creates the menu bar
	 */
	public JMenuBar createMenu() {
		String menu_text = JFritz.PROGRAM_NAME;
		if (JFritz.runsOn() == "mac")
			menu_text = "Ablage";

		JMenu jfritzMenu = new JMenu(menu_text);
		// JMenu editMenu = new JMenu(JFritz.getMessage("edit_menu"));
		JMenu optionsMenu = new JMenu(JFritz.getMessage("options_menu"));
		JMenu helpMenu = new JMenu(JFritz.getMessage("help_menu"));
		JMenu lnfMenu = new JMenu(JFritz.getMessage("lnf_menu"));
		JMenu importMenu = new JMenu(JFritz.getMessage("import_menu"));
		JMenu exportMenu = new JMenu(JFritz.getMessage("export_menu"));
		JMenu viewMenu = new JMenu(JFritz.getMessage("view_menu"));
		JMenu languageMenu = new JMenu(JFritz.getMessage("language_menu"));

		//File menu
		JMenuItem item = new JMenuItem(JFritz.getMessage("fetchlist"), 'a');
		item.setActionCommand("fetchList");
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("reverse_lookup"), 'l');
		item.setActionCommand("reverselookup");
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("print_callerlist"));
		item.setActionCommand("print_callerlist");
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("delete_fritzbox_callerlist"));
		item.setActionCommand("delete_fritzbox_callerlist");
		item.setMnemonic(KeyEvent.VK_F);
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("backup"));
		item.setActionCommand("backup");
		item.addActionListener(this);
		jfritzMenu.add(item);

		//import submenu
		if (JFritz.runsOn().startsWith("Windows")) {
			item = new JMenuItem(JFritz.getMessage("import_contacts_outlook"));
			item.setActionCommand("import_outlook");
			item.addActionListener(this);
			importMenu.add(item);
		}

	    item = new JMenuItem(JFritz.getMessage("import_callerlist_csv"), 'i');
	    item.setActionCommand("import_callerlist_csv");
	    item.addActionListener(this);
	    importMenu.add(item);

	    item = new JMenuItem(JFritz.getMessage("phonebook_import"));
	    item.setActionCommand("phonebook_import");
	    item.addActionListener(this);
	    importMenu.add(item);

	    item = new JMenuItem(JFritz.getMessage("import_contacts_thunderbird_csv"));
	    item.setActionCommand("import_contacts_thunderbird_csv");
	    item.addActionListener(this);
	    importMenu.add(item);

	    jfritzMenu.add(importMenu);

	    //export submenu
		item = new JMenuItem(JFritz.getMessage("export_csv"), 'c');
		item.setActionCommand("export_csv");
		item.addActionListener(this);
		exportMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("export_csv_phonebook"));
		item.setActionCommand("export_phonebook");
		item.addActionListener(this);
		exportMenu.add(item);

		jfritzMenu.add(exportMenu);

		if (JFritz.runsOn() != "mac") {
			jfritzMenu.add(new JSeparator());
			item = new JMenuItem(JFritz.getMessage("prog_exit"), 'x');
			// item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			// ActionEvent.ALT_MASK));
			item.setActionCommand("exit");
			item.addActionListener(this);
			jfritzMenu.add(item);
		}

		//options menu
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

		//languages submenu
		item = new JMenuItem(JFritz.getMessage("german"));
		item.setActionCommand("german");
		item.addActionListener(this);
		languageMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("english"));
		item.setActionCommand("english");
		item.addActionListener(this);
		languageMenu.add(item);

		optionsMenu.add(languageMenu);

		if (JFritz.runsOn() != "mac") {
			item = new JMenuItem(JFritz.getMessage("config"), 'e');
			item.setActionCommand("config");
			item.addActionListener(this);
			optionsMenu.add(item);
		}

		//view menu
		item = new JMenuItem(JFritz.getMessage("callerlist"), null);
		item.setActionCommand("callerlist");
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("phonebook"), null);
		item.setActionCommand("phonebook");
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("quickdials"), null);
		item.setActionCommand("quickdial");
		item.addActionListener(this);
		viewMenu.add(item);

		//help menu
		item = new JMenuItem(JFritz.getMessage("help_content"), 'h');
		item.setActionCommand("help");
		item.addActionListener(this);
		helpMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("jfritz_website"), 'w');
		item.setActionCommand("website");
		item.addActionListener(this);
		helpMenu.add(item);

		if (JFritz.runsOn() != "mac") {
			helpMenu.add(new JSeparator());
			item = new JMenuItem(JFritz.getMessage("prog_info"), 'i');
			item.setActionCommand("about");
			item.addActionListener(this);
			helpMenu.add(item);
		}

		menu = new JMenuBar();
		menu.add(jfritzMenu);
		menu.add(optionsMenu);
		menu.add(viewMenu);
		menu.add(helpMenu);
		return menu;
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

					}, 5000, Integer.parseInt(JFritz.getProperty("fetch.timer",
							"3")) * 60000);
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
		fetchList(false);
	}

	/**
	 * Fetches list from box
	 */
	public void fetchList(final boolean deleteFritzBoxCallerList) {
		if (!isretrieving) { // Prevent multiple clicking
			isretrieving = true;
			tabber.setSelectedComponent(callerListPanel);
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					int connectionFailures = 0;
					while (!isdone) {
						try {
							setBusy(true);
							setStatus(JFritz.getMessage("fetchdata"));
							jfritz.getCallerlist().getNewCalls(
									deleteFritzBoxCallerList);
							isdone = true;
						} catch (WrongPasswordException e) {
							setBusy(false);
							setStatus(JFritz.getMessage("password_wrong"));
							String password = showPasswordDialog(Encryption
									.decrypt(JFritz.getProperty("box.password",
											"")));
							if (password == null) { // Dialog canceled
								isdone = true;
							} else {
								JFritz.setProperty("box.password", Encryption
										.encrypt(password));
							}
						} catch (IOException e) {
							// Warten, falls wir von einem Standby aufwachen,
							// oder das Netzwerk tempor?r nicht erreichbar ist.
							if (connectionFailures < 5) {
								Debug.msg("Waiting for FritzBox, retrying ...");
								connectionFailures++;
							} else {
								Debug.msg("Callerlist Box not found");
								setBusy(false);
								setStatus(JFritz.getMessage("box_not_found"));
								String box_address = showAddressDialog(JFritz
										.getProperty("box.address", "fritz.box"));
								if (box_address == null) { // Dialog canceled
									isdone = true;
								} else {
									JFritz.setProperty("box.address",
											box_address);
								}
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
					if (JFritz.getProperty("option.lookupAfterFetch", "false")
							.equals("true")) {
						lookupButton.doClick();
					}
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
			tabber.setSelectedComponent(callerListPanel);
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					int j = 0;
					while (!isdone) {
						setBusy(true);
						setStatus(JFritz.getMessage("reverse_lookup"));
						for (int i = 0; i < jfritz.getCallerlist()
								.getRowCount(); i++) {
							Vector data = jfritz.getCallerlist()
									.getFilteredCallVector();
							Call call = (Call) data.get(i);
							PhoneNumber number = call.getPhoneNumber();
							if (number != null && (call.getPerson() == null)) {
								j++;
								setStatus(JFritz
										.getMessage("reverse_lookup_for")
										+ " " + number.getIntNumber() + " ...");
								Debug.msg("Reverse lookup for "
										+ number.getIntNumber());

								Person newPerson = ReverseLookup.lookup(number);
								if (newPerson != null) {
									jfritz.getPhonebook().addEntry(newPerson);
									jfritz.getPhonebook()
											.fireTableDataChanged();
									jfritz.getCallerlist()
											.fireTableDataChanged();
								}

							}
						}
						isdone = true;
					}
					if (j > 0)
						jfritz.getPhonebook().saveToXMLFile(
								JFritz.PHONEBOOK_FILE);
					return null;
				}

				public void finished() {
					setBusy(false);
					isretrieving = false;
					// int rows = jfritz.getCallerlist().getRowCount();
					setStatus();
				}
			};
			worker.start();
		} else {
			Debug.err("Multiple clicking is disabled..");
		}
	}

	/**
	 * Shows the stats dialog
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
	public void showConfigDialog() {
		configDialog = new ConfigDialog(this);
		configDialog.setLocationRelativeTo(this);
		if (configDialog.showDialog()) {
			configDialog.storeValues();
			jfritz.saveProperties();
			if (jfritz.getSIPProviderTableModel().getProviderList().size() == 0) { // Noch
				// keine
				// SipProvider
				// eingelesen.
				try {
					Vector data = JFritzUtils.retrieveSipProvider(JFritz
							.getProperty("box.address", "192.168.178.1"),
							Encryption.decrypt(JFritz
									.getProperty("box.password")),
							new FritzBoxFirmware(JFritz
									.getProperty("box.firmware")));
					jfritz.getSIPProviderTableModel().updateProviderList(data);
					jfritz.getSIPProviderTableModel().fireTableDataChanged();
					jfritz.getSIPProviderTableModel().saveToXMLFile(
							JFritz.SIPPROVIDER_FILE);
					jfritz.getCallerlist().fireTableDataChanged();
				} catch (WrongPasswordException e1) {
					jfritz.errorMsg("Passwort ung?ltig!");
				} catch (IOException e1) {
					jfritz.errorMsg("FRITZ!Box-Adresse ung?ltig!");
				} catch (InvalidFirmwareException e1) {
					jfritz.errorMsg("Firmware-Erkennung gescheitert!");
				}
			}
			monitorButton.setEnabled((Integer.parseInt(JFritz.getProperty(
					"option.callMonitorType", "0")) > 0));

			TableColumnModel colModel = jfritz.getJframe().getCallerTable()
					.getColumnModel();

			// Show / hide CallByCall column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showCallByCallColumn", "true"))) {
					if (getCallerTable().getColumnIndex("callbycall") == -1)
						{ 	// No Call-By-Call column found. Add one
							colModel.addColumn(jfritz.getJframe().getCallerTable()
									.getCallByCallColumn());
							colModel
									.getColumn(colModel.getColumnCount() - 1)
										.setPreferredWidth(
											Integer.parseInt(JFritz.getProperty(
												"column.callbycall.width", "50")));
				}
			} else {
					// Try to remove Call-By-Call Column
					int columnIndex = getCallerTable().getColumnIndex("callbycall");
					if (columnIndex != -1)
						colModel.removeColumn(colModel.getColumn(columnIndex));
			}
			// Show / hide comment column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showCommentColumn", "true"))) {
					if (getCallerTable().getColumnIndex("comment") == -1)
					{	// No comment column found. Addone
						colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getCommentColumn());
						colModel.getColumn(colModel.getColumnCount() - 1)
							.setPreferredWidth(
									Integer.parseInt(JFritz.getProperty(
											"column.comment.width", "50")));
				}
			} else {
					// Try to remove comment column
					int columnIndex = getCallerTable().getColumnIndex("comment");
					if (columnIndex != -1)
						colModel.removeColumn(colModel.getColumn(columnIndex));
			}
			// Show / hide port column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showPortColumn", "true"))) {
					if (getCallerTable().getColumnIndex("port") == -1)
					{	// No port column found. Add one
						colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getPortColumn());
						colModel.getColumn(colModel.getColumnCount() - 1)
							.setPreferredWidth(
									Integer.parseInt(JFritz.getProperty(
											"column.port.width", "50")));
				}
			} else {
					// Try to remove port column
					int columnIndex = getCallerTable().getColumnIndex("port");
					if (columnIndex != -1)
						colModel.removeColumn(colModel.getColumn(columnIndex));
			}
		}
		configDialog.dispose();
	}

	/**
	 * Shows the password dialog
	 *
	 * @param old_password
	 * @return new_password
	 */
	public String showPasswordDialog(String old_password) {
		String password = null;
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
	 * Shows the address dialog
	 *
	 * @param old_address
	 * @return address
	 */
	public String showAddressDialog(String old_address) {
		String address = null;
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
				+ "Developers:\n" + JFritz.PROGRAM_AUTHOR + "\n"
				+ "Robert Palmer <robotniko@users.sourceforge.net>\n"
				+ "Christian Klein <kleinch@users.sourceforge.net>\n"
				+ "Benjamin Schmitt <little_ben@users.sourceforge.net>\n"
				+ "Bastian Schaefer <baefer@users.sourceforge.net>\n"
				+ "Brian Jensen <jensen@users.sourceforge.net\n"
				+ "\n" + JFritz.PROGRAM_URL + "\n\n"
				+ "This tool is developed and released under\n"
				+ "the terms of the GNU General Public License\n\n"
				+ "Long live Free Software!");
	}

	/**
	 * Shows the exit dialog
	 */
	public void showExitDialog() {
		boolean exit = true;

		if (JFritzUtils.parseBoolean(JFritz.getProperty("option.confirmOnExit",
				"false")))
			exit = JOptionPane.showConfirmDialog(this, JFritz
					.getMessage("really_quit"), JFritz.PROGRAM_NAME,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

		if (exit) {
			// Speichern der Daten wird von ShutdownThread durchgef?hrt
			System.exit(0);
		}
	}

	/**
	 * Listener for window events
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (JFritzUtils.parseBoolean(JFritz.getProperty("option.minimize",
					"false"))) {
				setState(JFrame.ICONIFIED);
			} else
				showExitDialog();
		} else if (e.getID() == WindowEvent.WINDOW_ICONIFIED) {
			setState(JFrame.ICONIFIED);
			if (JFritz.SYSTRAY_SUPPORT)
				setVisible(false);
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
				JFritz.setProperty("lookandfeel", info.getClassName());
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
		int duration = jfritz.getCallerlist().getTotalDuration();
		int hours = duration / 3600;
		int mins = duration % 3600 / 60;
		String status = jfritz.getCallerlist().getRowCount() + " "
				+ JFritz.getMessage("entries") + ", "
				+ JFritz.getMessage("total_duration") + ": " + hours + "h "
				+ mins + " min " + " (" + duration / 60 + " min)";
		;
		// + ((double)jfritz.getCallerlist().getTotalCosts() / 100)+ " Euro";
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
		}
	}

	/**
	 * Sets the busy mode of progress bar
	 *
	 * @param busy
	 */
	public void setBusy(boolean busy) {
		if (fetchButton != null) {
			fetchButton.setEnabled(!busy);
			lookupButton.setEnabled(!busy);
			configButton.setEnabled(!busy);
			monitorButton.setEnabled(!busy
					&& (Integer.parseInt(JFritz.getProperty(
							"option.callMonitorType", "0")) > 0));
		}
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
		if (e.getActionCommand() == "exit")
			showExitDialog();
		else if (e.getActionCommand() == "about")
			showAboutDialog();
		else if (e.getActionCommand() == "help") {
			BrowserLaunch.openURL(JFritz.DOCUMENTATION_URL);
		} else if (e.getActionCommand() == "website") {
			BrowserLaunch.openURL(JFritz.PROGRAM_URL);
		} else if (e.getActionCommand() == "export_csv")
			exportCallerListToCSV();
		else if (e.getActionCommand() == "export_phonebook")
			exportPhoneBookToCSV();
		else if (e.getActionCommand() == "print_callerlist")
			printCallerList();
		else if (e.getActionCommand() == "import_outlook")
			importOutlook();
		else if (e.getActionCommand() == "config")
			showConfigDialog();
		else if (e.getActionCommand() == "callerlist")
			tabber.setSelectedComponent(callerListPanel);
		else if (e.getActionCommand() == "phonebook")
			activatePhoneBook();
		else if (e.getActionCommand() == "quickdial")
			tabber.setSelectedComponent(quickDialPanel);
		else if (e.getActionCommand() == "stats")
			showStatsDialog();
		else if (e.getActionCommand() == "fetchList")
			fetchList();
		else if (e.getActionCommand() == "delete_fritzbox_callerlist")
			deleteFritzBoxCallerList();
		else if (e.getActionCommand() == "backup")
			backupToChoosenDirectory();
//		else if (e.getActionCommand() == "call"){
//			try{
//			CallDialog callDialog = new CallDialog(jfritz, jfritz.getCallerlist().getSelectedCall().getPhoneNumber());
//			callDialog.setVisible(true);
//			}catch(NullPointerException ex){
//				Debug.msg("Keine Nummer hinterlegt");
//			}
//		}
		else if (e.getActionCommand() == "fetchTask")
			fetchTask(((JToggleButton) e.getSource()).isSelected());
		else if (e.getActionCommand() == "callMonitor") {
			boolean active = ((JToggleButton) e.getSource()).isSelected();
			if (active) {
				Debug.msg("start callMonitor");
				startChosenCallMonitor();
			} else {
				Debug.msg("stop callMonitor");
				jfritz.stopCallMonitor();
			}

		} else if (e.getActionCommand() == "reverselookup")
			reverseLookup();
		else if (e.getActionCommand() == "F5")
			fetchList();
	    else if (e.getActionCommand() == "import_callerlist_csv")
	        importCallerlistCSV();
	    else if (e.getActionCommand() == "phonebook_import")
	    	phoneBookPanel.importFromXML();
	    else if (e.getActionCommand() == "import_contacts_thunderbird_csv")
			importContactsThunderbirdCSV();
	    else if (e.getActionCommand() == "german")
	    	setLanguage(new Locale("de", "DE"));
	    else if (e.getActionCommand() == "english")
	    	setLanguage(new Locale("en", "US"));

	    else
			Debug.err("Unimplemented action: " + e.getActionCommand());

	}

	/**
	 * Exports caller list as CSV
	 */
	public void exportCallerListToCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpath", null));
		fc.setDialogTitle(JFritz.getMessage("export_csv"));
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
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportCSVpath", path);
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, JFritz.getMessage("overwrite_file1")
						+ file.getName() + " " + JFritz.getMessage("overwrite_file2"),
						JFritz.getMessage("overwrite_file3"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					jfritz.getCallerlist().saveToCSVFile(
							file.getAbsolutePath(), false);
				}
			} else {
				jfritz.getCallerlist().saveToCSVFile(file.getAbsolutePath(),
						false);
			}
		}
	}

	/**
	 * Exports caller list as XML
	 */
	public void exportCallerListToXML() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportXMLpath", null));
		fc.setDialogTitle("Exportiere Anrufliste als XML-Datei");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xml");
			}

			public String getDescription() {
				return "XML-Dateien";
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportXMLpath", path);
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, JFritz.getMessage("overwrite_file1")
						+ file.getName() + " " + JFritz.getMessage("overwrite_file2"),
						JFritz.getMessage("overwrite_file3"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					jfritz.getCallerlist().saveToXMLFile(
						file.getAbsolutePath(), false);
				}
			} else {
				jfritz.getCallerlist().saveToXMLFile(file.getAbsolutePath(),
						false);
			}
		}
	}

	/**
	 * Exports phone book as CSV
	 * @author Bastian Schaefer
	 */
	public void exportPhoneBookToCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpathOfPhoneBook", null));
		fc.setDialogTitle(JFritz.getMessage("export_csv_phonebook"));
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.PHONEBOOK_CSV_FILE));
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
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportCSVpathOfPhoneBook", path);
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, "Soll die Datei "
						+ file.getName() + " Überschrieben werden?",
						"Datei Überschreiben?", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					jfritz.getPhonebook().saveToCSVFile(
							file.getAbsolutePath(), false);
				}
			} else {
				jfritz.getPhonebook().saveToCSVFile(file.getAbsolutePath(),
						false);
			}
		}
	}

	//
	public void printCallerList() {
		PrintCallerList printCallerList = new PrintCallerList(jfritz);
		printCallerList.print();
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
	 * @return Returns the callertable.
	 */
	public final CallerTable getCallerTable() {
		return callerListPanel.getCallerTable();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
	}

	/**
	 * @return Returns the phoneBookPanel.
	 */
	public final PhoneBookPanel getPhoneBookPanel() {
		return phoneBookPanel;
	}

	/**
	 * @return Returns the quickDialPanel.
	 */
	public final QuickDialPanel getQuickDialPanel() {
		return quickDialPanel;
	}

	public void activatePhoneBook() {
		Rectangle rect = phoneBookPanel.getPhoneBookTable().getCellRect(
				phoneBookPanel.getPhoneBookTable().getSelectedRow(), 0, true);
		phoneBookPanel.getPhoneBookTable().scrollRectToVisible(rect);
		tabber.setSelectedComponent(phoneBookPanel);
	}

	/**
	 * @return Returns the callerListPanel.
	 */
	public CallerListPanel getCallerListPanel() {
		return callerListPanel;
	}

	public JButton getFetchButton() {
		return fetchButton;
	}

	public void saveQuickDials() {
		quickDialPanel.getDataModel().saveToXMLFile(JFritz.QUICKDIALS_FILE);
	}

	public void switchMonitorButton() {
		monitorButton.doClick();
	}

	public JToggleButton getMonitorButton() {
		return monitorButton;
	}

	/**
	 * Let startCallMonitorButtons start or stop callMonitor Changes caption of
	 * buttons and their status
	 *
	 * @param option
	 *            CALLMONITOR_START or CALLMONITOR_STOP
	 */

	public void setCallMonitorButtons(int option) {
		switch (option) {
			case JFritz.CALLMONITOR_START : {
				if (configDialog != null) {
					configDialog.setCallMonitorButtons(option);
				} else {
					jfritz.getJframe().getMonitorButton().setSelected(false);
				}
				break;
			}
			case JFritz.CALLMONITOR_STOP : {
				if (configDialog != null) {
					configDialog.setCallMonitorButtons(option);
				} else {
					jfritz.getJframe().getMonitorButton().setSelected(true);
				}
				break;
			}
		}

	}

	private void importOutlook() {
		Debug.msg("Starte Import von Outlook");
		Thread thread = new Thread(new ImportOutlookContacts(jfritz));
		thread.start();
	}

	public void startChosenCallMonitor() {
		switch (Integer.parseInt(JFritz.getProperty("option.callMonitorType",
				"0"))) {
			case 1 : {
                FritzBoxFirmware currentFirm;
				try {
					currentFirm = JFritzUtils.detectBoxType(
							"", JFritz.getProperty("box.address"), Encryption
									.decrypt(JFritz.getProperty("box.password",
											"")));
					if (currentFirm.getMajorFirmwareVersion() == 3
							&& currentFirm.getMinorFirmwareVersion() < 96) {
						Debug
								.errDlg("Dieser Anrufmonitor funktioniert nur ab Firmware xx.03.96");
						monitorButton.setSelected(false);
						this.setCallMonitorButtons(JFritz.CALLMONITOR_START);
					} else {
                        if (currentFirm.getMajorFirmwareVersion()>=4 &&
                                currentFirm.getMinorFirmwareVersion()>=3) {
                            jfritz.setCallMonitor(new FBoxListenerV3(jfritz));
                        } else {
                            jfritz.setCallMonitor(new FBoxListenerV1(jfritz));
                        }
						this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
					}
				} catch (WrongPasswordException e) {
					jfritz.getJframe().setStatus(
							JFritz.getMessage("password_wrong"));
					String password = jfritz.getJframe().showPasswordDialog(
							Encryption.decrypt(JFritz.getProperty(
									"box.password", "")));
					if (password != null) { // Dialog not canceled
						JFritz.setProperty("box.password", Encryption
								.encrypt(password));
					}
				} catch (IOException e) {
					Debug.err("Konnte Box nicht erkennen.");
				}
				break;
			}
			case 2 : {
				jfritz.setCallMonitor(new TelnetListener(jfritz));
				this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
				break;
			}
			case 3 : {
				jfritz.setCallMonitor(new SyslogListener(jfritz));
				this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
				break;
			}
			case 4 : {
				jfritz.setCallMonitor(new YAClistener(jfritz,
						Integer.parseInt(JFritz.getProperty("option.yacport",
								"10629"))));
				this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
				break;
			}
			case 5 : {
				jfritz.setCallMonitor(new CallmessageListener(jfritz, Integer
						.parseInt(JFritz.getProperty("option.callmessageport",
								"23232"))));
				this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
				break;
			}
		}

	}

	/**
	 * Deletes the caller list in Fritz!Box after having actualized it with the
	 * JFritz-CallerList. Method uses JFritzWindow.fetchList(true) to delete the
	 * caller list.
	 *
	 * @author Benjamin Schmitt
	 */
	public void deleteFritzBoxCallerList() {
		// TODO:Set focus to Cancel-Button
		int answer = JOptionPane.showConfirmDialog(this, JFritz
				.getMessage("delete_fritzbox_callerlist_confirm_msg"), JFritz
				.getMessage("delete_fritzbox_callerlist"),
				JOptionPane.YES_NO_OPTION);

		if (answer == JOptionPane.YES_OPTION)
			fetchList(true); // param true indicates that FritzBox-CallerList
								// is to be deleted
	}

	/**
	 * Creates a backup to a user selected directory
	 *
	 * @author Bastian Schaefer
	 */
	public void backupToChoosenDirectory() {
		CopyFile backup = new CopyFile();
		try {
			String directory = new DirectoryChooser().getDirectory(
					jfritz.getJframe()).toString();
			backup.copy(".", "xml", directory);
		} catch (NullPointerException e) {
			Debug.msg("No directory choosen for backup!");
		}
	}

	/**
	 * Provides easy implementation of a KeyListener. Will add the KeyListener
	 * to the main Jframe and react without having the Focus.
	 */
	public void addKeyListener(int vkey, String listenerString) {

		this.getRootPane().registerKeyboardAction(this, listenerString,
				keyStroke(vkey), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	/**
	 * Provides easy creation of a KeyStroke object without a modifier
	 * and reaction onKeyReale
	 */
	private KeyStroke keyStroke(int vkey) {
		return KeyStroke.getKeyStroke(vkey, 0 , false);
	}

	  /**
	   * @author Brian Jensen
	   *
	   * opens the import csv dialog
	   * currently supported types: fritzbox native type and jfritz native type
	   *
	   * does a reverse lookup on return
	   */
	  public void importCallerlistCSV(){
	    JFileChooser fc = new JFileChooser(JFritz.getProperty(
	        "options.exportCSVpath", null));
	    fc.setDialogTitle(JFritz.getMessage("import_callerlist_csv"));
	    fc.setDialogType(JFileChooser.OPEN_DIALOG);
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
	      String path = fc.getSelectedFile().getPath();
	      path = path.substring(0, path.length()
	          - fc.getSelectedFile().getName().length());
	      JFritz.setProperty("options.exportCSVpath", path);
	      File file = fc.getSelectedFile();
	      if (!file.exists()) {
	        JOptionPane.showMessageDialog(this, "Error: File not found", "File Not Found", JOptionPane.ERROR_MESSAGE);

	      }else{
	        jfritz.getCallerlist().importFromCSVFile(file.getAbsolutePath());

	        if (JFritz.getProperty("option.lookupAfterFetch", "false")
					.equals("true")) {
				lookupButton.doClick();
			}
	      }
	    }
	  }

	  /**
	   * @author Brian Jensen
	   *
	   * opens the import thunderbird dialog
	   * selects a file then passes it on to
	   * PhoneBook.importFromThunderbirdCSVfile()
	   *
	   */
	  public void importContactsThunderbirdCSV(){
		  JFileChooser fc = new JFileChooser(JFritz.getProperty(
				  "options.exportCSVpath", null));
		  fc.setDialogTitle(JFritz.getMessage("import_contacts_thunderbird_csv"));
		  fc.setDialogType(JFileChooser.OPEN_DIALOG);
		  fc.setFileFilter(new FileFilter() {
			  public boolean accept(File f) {
				  return f.isDirectory()
				  || f.getName().toLowerCase().endsWith(".csv");
			  }

			  public String getDescription() {
				  return "CSV files";
			  }
		  });
		  if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			  String path = fc.getSelectedFile().getPath();
			  path = path.substring(0, path.length()
					  - fc.getSelectedFile().getName().length());
			  //options.import_contacts_thunderbird_CSVpath ???
			  //JFritz.setProperty("options.exportCSVpath", path);
			  File file = fc.getSelectedFile();
			  if (!file.exists()) {
				  JOptionPane.showMessageDialog(this, "Error: File not found", "File Not Found", JOptionPane.ERROR_MESSAGE);
			  }else{
			        jfritz.getPhonebook().importFromThunderbirdCSVfile(file.getAbsolutePath());

			        if (JFritz.getProperty("option.lookupAfterFetch", "false")
							.equals("true")) {
						lookupButton.doClick();
					}
			  }
		}
	  }

	  /**
	   * @author Brian Jensen
	   * This function changes the ResourceBundle in the jfritz instance
	   * Then the jfritz object destroys the current window and redraws a new one
	   *
	   * NOTE: This function is currently experimental
	   *
	   * @param locale to switch the language to
	   */
	  public void setLanguage(Locale locale){
		  jfritz.createNewWindow(locale);
		  //current window will be destroyed and a new one created

	  }

}