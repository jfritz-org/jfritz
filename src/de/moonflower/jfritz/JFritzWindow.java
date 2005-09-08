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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
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
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.SwingWorker;
import de.moonflower.jfritz.utils.network.TelnetListener;
import de.moonflower.jfritz.utils.network.SyslogListener;
import de.moonflower.jfritz.utils.network.YAClistener;
import de.moonflower.jfritz.utils.network.CallmessageListener;

/**
 * This is main window class of JFritz, which creates the GUI.
 *
 * @author akw
 */
public class JFritzWindow extends JFrame implements Runnable, ActionListener,
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
		if (!JFritz.getProperty("option.startMinimized", "false")
				.equals("true")) {
			setVisible(true);
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
			switch (Integer.parseInt(JFritz.getProperty(
					"option.callMonitorType", "0"))) {
			case 1: {
				jfritz.setCallMonitor(new TelnetListener(jfritz));
				monitorButton.setSelected(true);
				break;
			}
			case 2: {
				jfritz.setCallMonitor(new SyslogListener(jfritz));
				monitorButton.setSelected(true);
				break;
			}
			case 3: {
				jfritz.setCallMonitor(new YAClistener(jfritz,
						Integer.parseInt(JFritz.getProperty("option.yacport",
								"10629"))));
				monitorButton.setSelected(true);
				break;
			}
			case 4: {
				jfritz.setCallMonitor(new CallmessageListener(jfritz, Integer
						.parseInt(JFritz.getProperty("option.callmessageport",
								"23232"))));
				monitorButton.setSelected(true);
				break;
			}
			}
		}
	}

	private void createGUI() {
		setTitle(JFritz.PROGRAM_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeel();
		ShutdownThread shutdownThread = new ShutdownThread(jfritz);
		Runtime.getRuntime().addShutdownHook(shutdownThread);
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

		jfritz.getCallerlist().fireTableStructureChanged();
		String ask = JFritz.getProperty("jfritz.password", Encryption
				.encrypt(JFritz.PROGRAM_SECRET + ""));
		String pass = JFritz
				.getProperty("box.password", Encryption.encrypt(""));
		if (!Encryption.decrypt(ask).equals(
				JFritz.PROGRAM_SECRET + Encryption.decrypt(pass))) {
			String password = showPasswordDialog("");
			if (password == null) { // PasswordDialog canceled
				Debug.errDlg("Eingabe abgebrochen");
				Debug.err("Eingabe abgebrochen");
				System.exit(0);
			} else if (!password.equals(Encryption.decrypt(pass))) {
				Debug.errDlg("Falsches Passwort!");
				Debug.err("Wrong password!");
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
			ex.printStackTrace();
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

		JButton button = new JButton();
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
		//JMenu editMenu = new JMenu(JFritz.getMessage("edit_menu"));
		JMenu optionsMenu = new JMenu(JFritz.getMessage("options_menu"));
		JMenu helpMenu = new JMenu(JFritz.getMessage("help_menu"));
		JMenu lnfMenu = new JMenu(JFritz.getMessage("lnf_menu"));
		JMenu exportMenu = new JMenu(JFritz.getMessage("export_menu"));
		JMenu viewMenu = new JMenu(JFritz.getMessage("view_menu"));

		JMenuItem item = new JMenuItem(JFritz.getMessage("fetchlist"), 'a');
		item.setActionCommand("fetchList");
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("reverse_lookup"), 'l');
		item.setActionCommand("reverselookup");
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("export_csv"), 'c');
		item.setActionCommand("export_csv");
		item.addActionListener(this);
		exportMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("export_excel"), 'c');
		item.setActionCommand("export_excel");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		exportMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("export_openoffice"), 'c');
		item.setActionCommand("export_openoffice");
		item.addActionListener(this);
		item.setEnabled(JFritz.DEVEL_VERSION);
		exportMenu.add(item);
		jfritzMenu.add(exportMenu);

		if (JFritz.runsOn() != "mac") {
			jfritzMenu.add(new JSeparator());
			item = new JMenuItem(JFritz.getMessage("prog_exit"), 'x');
			//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			//		ActionEvent.ALT_MASK));
			item.setActionCommand("exit");
			item.addActionListener(this);
			jfritzMenu.add(item);
		}

		item = new JMenuItem(JFritz.getMessage("help_content"), 'h');
		item.setActionCommand("help");
		item.addActionListener(this);
		//		item.setEnabled(JFritz.DEVEL_VERSION);
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

		if (JFritz.runsOn() != "mac") {
			item = new JMenuItem(JFritz.getMessage("config"), 'e');
			item.setActionCommand("config");
			item.addActionListener(this);
			optionsMenu.add(item);
		}

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
							jfritz.getCallerlist().getNewCalls();
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
							// oder das Netzwerk temporär nicht erreichbar ist.
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
					jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
					return null;
				}

				public void finished() {
					setBusy(false);
					isretrieving = false;
					//int rows = jfritz.getCallerlist().getRowCount();
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
		if (configDialog.showDialog()) {
			configDialog.storeValues();
			jfritz.saveProperties();
			monitorButton.setEnabled((Integer.parseInt(JFritz.getProperty(
					"option.callMonitorType", "0")) > 0));

			// Show / hide CallByCall column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showCallByCall", "false"))) {
				TableColumnModel colModel = jfritz.getJframe().getCallerTable()
						.getColumnModel();
				if (!colModel.getColumn(2).getHeaderValue().toString().equals(
						"Call-By-Call")) {
					colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getCallByCallColumn());
					colModel.moveColumn(colModel.getColumnCount() - 1, 2);
					colModel.getColumn(2).setPreferredWidth(
							Integer.parseInt(JFritz.getProperty(
									"column2.width", "60")));
					getCallerListPanel().getCallByCallButton().setEnabled(true);
				}
			} else {
				TableColumnModel colModel = jfritz.getJframe().getCallerTable()
						.getColumnModel();
				if (colModel.getColumn(2).getHeaderValue().toString().equals(
						"Call-By-Call")) {

					colModel.removeColumn(colModel.getColumn(2));
					getCallerListPanel().getCallByCallButton()
							.setEnabled(false);
				}
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
				+ "Robert Palmer <robotniko@gmx.de>\n\n" + JFritz.PROGRAM_URL
				+ "\n\n" + "This tool is developed and released under\n"
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
			// Speichern der Daten wird von ShutdownThread durchgeführt
			System.exit(0);
		}
	}

	/**
	 * Listener for window events
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
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
		String status = jfritz.getCallerlist().getRowCount() + " "
				+ JFritz.getMessage("entries") + ", "
				+ JFritz.getMessage("total_duration") + ": "
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
			exportCSV();
		else if (e.getActionCommand() == "config")
			showConfigDialog();
		else if (e.getActionCommand() == "callerlist")
			tabber.setSelectedComponent(callerListPanel);
		else if (e.getActionCommand() == "phonebook")
			tabber.setSelectedComponent(phoneBookPanel);
		else if (e.getActionCommand() == "quickdial")
			tabber.setSelectedComponent(quickDialPanel);
		else if (e.getActionCommand() == "stats")
			showStatsDialog();
		else if (e.getActionCommand() == "fetchList")
			fetchList();
		else if (e.getActionCommand() == "fetchTask")
			fetchTask(((JToggleButton) e.getSource()).isSelected());
		else if (e.getActionCommand() == "callMonitor") {
			boolean active = ((JToggleButton) e.getSource()).isSelected();
			if (active) {
				Debug.msg("start callMonitor");
				switch (Integer.parseInt(JFritz.getProperty(
						"option.callMonitorType", "0"))) {
				case 0: {
					jfritz.errorMsg("Kein Anrufmonitor ausgewählt");
					break;
				}
				case 1: {
					jfritz.setCallMonitor(new TelnetListener(jfritz));
					break;
				}
				case 2: {
					jfritz.setCallMonitor(new SyslogListener(jfritz));
					break;
				}
				case 3: {
					jfritz.setCallMonitor(new YAClistener(jfritz, Integer
							.parseInt(JFritz.getProperty("option.yacport",
									"10629"))));
					break;
				}
				case 4: {
					jfritz.setCallMonitor(new CallmessageListener(jfritz,
							Integer.parseInt(JFritz.getProperty(
									"option.callmessageport", "23232"))));
					monitorButton.setSelected(true);
					break;
				}
				}
			} else {
				Debug.msg("stop callMonitor");
				jfritz.stopCallMonitor();
			}

		} else if (e.getActionCommand() == "reverselookup")
			reverseLookup();
		else
			Debug.err("Unimplemented action: " + e.getActionCommand());

	}

	/**
	 * Exports caller list as CSV
	 */
	public void exportCSV() {
		JFileChooser fc = new JFileChooser();
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
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, "Soll die Datei "
						+ file.getName() + " überschrieben werden?",
						"Datei überschreiben?", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
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
	public void exportXML() {
		JFileChooser fc = new JFileChooser();
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
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, "Soll die Datei "
						+ file.getName() + " überschrieben werden?",
						"Datei überschreiben?", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					jfritz.getCallerlist().saveToXMLFile(
							file.getAbsolutePath(), false);
				}
			} else {
				jfritz.getCallerlist().saveToXMLFile(file.getAbsolutePath(),
						false);
			}
		}
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

	public void activatePhoneBook() {
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
		case JFritz.CALLMONITOR_START: {
			if (configDialog != null) {
				configDialog.setCallMonitorButtons(option);
			} else {
				jfritz.getJframe().getMonitorButton().setSelected(false);
			}
			break;
		}
		case JFritz.CALLMONITOR_STOP: {
			if (configDialog != null) {
				configDialog.setCallMonitorButtons(option);
			} else {
				jfritz.getJframe().getMonitorButton().setSelected(true);
			}
			break;
		}
		}

	}
}