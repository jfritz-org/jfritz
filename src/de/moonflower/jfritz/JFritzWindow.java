/**
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Insets;
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

	private Rectangle maxBounds;

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		this.jfritz = jfritz;
		Debug.msg("Create JFritz-GUI"); //$NON-NLS-1$
		maxBounds = null;
		createGUI();
	}

	public void checkStartOptions() {
		if (!JFritz.getProperty("option.startMinimized", "false") //$NON-NLS-1$,  //$NON-NLS-2$,
				.equals("true")) { //$NON-NLS-1$
			setVisible(true);
		} else {
			if (!JFritz.SYSTRAY_SUPPORT)
				setVisible(true);
			setState(JFrame.ICONIFIED);
		}
		if (JFritz.getProperty("option.timerAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			taskButton.doClick();
		}
		if (JFritz.getProperty("option.fetchAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			fetchButton.doClick();
		}
		if (JFritz.getProperty("option.autostartcallmonitor", "false").equals( //$NON-NLS-1$,  //$NON-NLS-2$
				"true")) { //$NON-NLS-1$
			startChosenCallMonitor();
		}
		setStatus();
	}


	public void checkOptions() {
		if (JFritz.getProperty("option.timerAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			taskButton.doClick();
		}
		if (JFritz.getProperty("option.fetchAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			fetchButton.doClick();
		}
		if (JFritz.getProperty("option.autostartcallmonitor", "false").equals( //$NON-NLS-1$,  //$NON-NLS-2$
				"true")) { //$NON-NLS-1$
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

		addKeyListener(KeyEvent.VK_F5, "F5"); //$NON-NLS-1$

		this
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								getClass()
										.getResource(
												"/de/moonflower/jfritz/resources/images/trayicon.png"))); //$NON-NLS-1$

		// Setting size and position
		int x = Integer.parseInt(JFritz.getProperty("position.left", "10")); //$NON-NLS-1$,  //$NON-NLS-2$
		int y = Integer.parseInt(JFritz.getProperty("position.top", "10")); //$NON-NLS-1$,  //$NON-NLS-2$
		int w = Integer.parseInt(JFritz.getProperty("position.width", "640")); //$NON-NLS-1$,  //$NON-NLS-2$
		int h = Integer.parseInt(JFritz.getProperty("position.height", "400")); //$NON-NLS-1$,  //$NON-NLS-2$
		setLocation(x, y);
		setSize(w, h);

		callerListPanel = new CallerListPanel(jfritz);
		phoneBookPanel = new PhoneBookPanel(jfritz);
		quickDialPanel = new QuickDialPanel(jfritz);

		tabber = new JTabbedPane(JTabbedPane.BOTTOM);
		tabber.addTab(JFritz.getMessage("callerlist"), callerListPanel); //$NON-NLS-1$
		tabber.addTab(JFritz.getMessage("phonebook"), phoneBookPanel); //$NON-NLS-1$
		tabber.addTab(JFritz.getMessage("quickdials"), quickDialPanel); //$NON-NLS-1$
		tabber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("callerlist"))) { //$NON-NLS-1$
					setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("phonebook"))) { //$NON-NLS-1$
					phoneBookPanel.setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						JFritz.getMessage("quickdials"))) { //$NON-NLS-1$
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
		String ask = JFritz.getProperty("jfritz.password", Encryption //$NON-NLS-1$
				.encrypt(JFritz.PROGRAM_SECRET + "")); //$NON-NLS-1$
		String pass = JFritz
				.getProperty("box.password", Encryption.encrypt("")); //$NON-NLS-1$,  //$NON-NLS-2$
		if (!Encryption.decrypt(ask).equals(
				JFritz.PROGRAM_SECRET + Encryption.decrypt(pass))) {
			String password = showPasswordDialog(""); //$NON-NLS-1$
			if (password == null) { // PasswordDialog canceled
				Debug.errDlg(JFritz.getMessage("input_canceled")); //$NON-NLS-1$
				Debug.err("Eingabe abgebrochen"); //$NON-NLS-1$
				System.exit(0);
			} else if (!password.equals(Encryption.decrypt(pass))) {
				Debug.errDlg(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
				Debug.err(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
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
			UIManager.setLookAndFeel(JFritz.getProperty("lookandfeel", //$NON-NLS-1$
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
		fetchButton.setToolTipText(JFritz.getMessage("fetchlist")); //$NON-NLS-1$
		fetchButton.setActionCommand("fetchList"); //$NON-NLS-1$
		fetchButton.addActionListener(this);
		fetchButton.setIcon(getImage("fetch.png")); //$NON-NLS-1$
		fetchButton.setFocusPainted(false);
		mBar.add(fetchButton);

		JButton button = new JButton();
		// button = new JButton();
		// button.setActionCommand("call");
		// button.addActionListener(this);
		// button.setIcon(getImage("Phone.gif"));
		// button.setToolTipText(JFritz.getMessage("call"));
		// mBar.add(button);

		taskButton = new JToggleButton();
		taskButton.setToolTipText(JFritz.getMessage("fetchtask")); //$NON-NLS-1$
		taskButton.setActionCommand("fetchTask"); //$NON-NLS-1$
		taskButton.addActionListener(this);
		taskButton.setIcon(getImage("clock.png")); //$NON-NLS-1$
		mBar.add(taskButton);

		monitorButton = new JToggleButton();
		monitorButton.setToolTipText(JFritz.getMessage("callmonitor")); //$NON-NLS-1$
		monitorButton.setActionCommand("callMonitor"); //$NON-NLS-1$
		monitorButton.addActionListener(this);
		monitorButton.setIcon(getImage("monitor.png")); //$NON-NLS-1$
		mBar.add(monitorButton);

		lookupButton = new JButton();
		lookupButton.setToolTipText(JFritz.getMessage("reverse_lookup")); //$NON-NLS-1$
		lookupButton.setActionCommand("reverselookup"); //$NON-NLS-1$
		lookupButton.addActionListener(this);
		lookupButton.setIcon(getImage("reverselookup.png")); //$NON-NLS-1$
		mBar.add(lookupButton);

		button = new JButton();
		button.setActionCommand("phonebook"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("phonebook.png")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("phonebook")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("quickdials")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("delete_fritzbox_callerlist"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("DeleteList.gif")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("delete_fritzbox_callerlist")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("backup"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("Backup.gif")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("backup")); //$NON-NLS-1$
		mBar.add(button);

		mBar.addSeparator();

		button = new JButton();
		button.setActionCommand("stats"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("stats.png")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("stats")); //$NON-NLS-1$
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("help"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("help.png")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("help_menu")); //$NON-NLS-1$
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		mBar.addSeparator();

		configButton = new JButton();
		configButton.setActionCommand("config"); //$NON-NLS-1$
		configButton.addActionListener(this);
		configButton.setIcon(getImage("config.png")); //$NON-NLS-1$
		configButton.setToolTipText(JFritz.getMessage("config")); //$NON-NLS-1$
		mBar.add(configButton);

		mBar.addSeparator();
		return mBar;
	}

	/**
	 * Creates the menu bar
	 */
	public JMenuBar createMenu() {
		String menu_text = JFritz.PROGRAM_NAME;
		if (JFritz.runsOn().equals("Mac")) //$NON-NLS-1$
			menu_text = "Ablage"; //$NON-NLS-1$

		JMenu jfritzMenu = new JMenu(menu_text);
		// JMenu editMenu = new JMenu(JFritz.getMessage("edit_menu"));
		JMenu optionsMenu = new JMenu(JFritz.getMessage("options_menu")); //$NON-NLS-1$
		JMenu helpMenu = new JMenu(JFritz.getMessage("help_menu")); //$NON-NLS-1$
		JMenu lnfMenu = new JMenu(JFritz.getMessage("lnf_menu")); //$NON-NLS-1$
		JMenu importMenu = new JMenu(JFritz.getMessage("import_menu")); //$NON-NLS-1$
		JMenu exportMenu = new JMenu(JFritz.getMessage("export_menu")); //$NON-NLS-1$
		JMenu viewMenu = new JMenu(JFritz.getMessage("view_menu")); //$NON-NLS-1$

		// File menu
		JMenuItem item = new JMenuItem(JFritz.getMessage("fetchlist"), 'a'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("fetchList"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("reverse_lookup"), 'l'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("reverselookup"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("delete_fritzbox_callerlist")); //$NON-NLS-1$
		item.setActionCommand("delete_fritzbox_callerlist"); //$NON-NLS-1$
		item.setMnemonic(KeyEvent.VK_F);
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("backup")); //$NON-NLS-1$
		item.setActionCommand("backup"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("print_callerlist")); //$NON-NLS-1$
		item.setActionCommand("print_callerlist"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		// export submenu
		item = new JMenuItem(JFritz.getMessage("export_csv"), 'c'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("export_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		exportMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("export_csv_phonebook")); //$NON-NLS-1$
		item.setActionCommand("export_phonebook"); //$NON-NLS-1$
		item.addActionListener(this);
		exportMenu.add(item);

		jfritzMenu.add(exportMenu);

		// import submenu

		item = new JMenuItem(JFritz.getMessage("import_callerlist_csv"), 'i'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("phonebook_import")); //$NON-NLS-1$
		item.setActionCommand("phonebook_import"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		item = new JMenuItem(JFritz
				.getMessage("import_contacts_thunderbird_csv")); //$NON-NLS-1$
		item.setActionCommand("import_contacts_thunderbird_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		if (JFritz.runsOn().startsWith("Windows")) { //$NON-NLS-1$
			item = new JMenuItem(JFritz.getMessage("import_contacts_outlook")); //$NON-NLS-1$
			item.setActionCommand("import_outlook"); //$NON-NLS-1$
			item.addActionListener(this);
			importMenu.add(item);
		}

		jfritzMenu.add(importMenu);

		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			jfritzMenu.add(new JSeparator());
			item = new JMenuItem(JFritz.getMessage("prog_exit"), 'x'); //$NON-NLS-1$,  //$NON-NLS-2$
			// item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			// ActionEvent.ALT_MASK));
			item.setActionCommand("exit"); //$NON-NLS-1$
			item.addActionListener(this);
			jfritzMenu.add(item);
		}

		// options menu
		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();
		for (int i = 0; i < lnfs.length; i++) {
			JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i]
					.getName());
			lnfMenu.add(rbmi);
			rbmi.setSelected(UIManager.getLookAndFeel().getClass().getName()
					.equals(lnfs[i].getClassName()));
			rbmi.putClientProperty("lnf name", lnfs[i]); //$NON-NLS-1$
			rbmi.addItemListener(this);
			lnfgroup.add(rbmi);
		}
		optionsMenu.add(lnfMenu);


		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			item = new JMenuItem(JFritz.getMessage("config"), 'e'); //$NON-NLS-1$,  //$NON-NLS-2$
			item.setActionCommand("config"); //$NON-NLS-1$
			item.addActionListener(this);
			optionsMenu.add(item);
		}

		// view menu
		item = new JMenuItem(JFritz.getMessage("callerlist"), null); //$NON-NLS-1$
		item.setActionCommand("callerlist"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("phonebook"), null); //$NON-NLS-1$
		item.setActionCommand("phonebook"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("quickdials"), null); //$NON-NLS-1$
		item.setActionCommand("quickdial"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);

		// help menu
		item = new JMenuItem(JFritz.getMessage("help_content"), 'h'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("help"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);
		item = new JMenuItem(JFritz.getMessage("jfritz_website"), 'w'); //$NON-NLS-1$,  //$NON-NLS-2$
		item.setActionCommand("website"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);

		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			helpMenu.add(new JSeparator());
			item = new JMenuItem(JFritz.getMessage("prog_info"), 'i'); //$NON-NLS-1$,  //$NON-NLS-2$
			item.setActionCommand("about"); //$NON-NLS-1$
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
							Debug.msg("Running FetchListTask.."); //$NON-NLS-1$
							jfritz.getJframe().fetchList();
						}

					}, 5000, Integer.parseInt(JFritz.getProperty("fetch.timer", //$NON-NLS-1$
							"3")) * 60000); //$NON-NLS-1$
			Debug.msg("Timer enabled"); //$NON-NLS-1$
		} else {
			timer.cancel();
			Debug.msg("Timer disabled"); //$NON-NLS-1$
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
		if (!isretrieving) {  // Prevent multiple clicking
			isretrieving = true;
			tabber.setSelectedComponent(callerListPanel);
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					int connectionFailures = 0;
					while (!isdone) {
						try {
							setBusy(true);
							setStatus(JFritz.getMessage("fetchdata")); //$NON-NLS-1$
							jfritz.getCallerlist().getNewCalls(
									deleteFritzBoxCallerList);
							isdone = true;
						} catch (WrongPasswordException e) {
							setBusy(false);
							setStatus(JFritz.getMessage("password_wrong")); //$NON-NLS-1$
							String password = showPasswordDialog(Encryption
									.decrypt(JFritz.getProperty("box.password", //$NON-NLS-1$
											""))); //$NON-NLS-1$
							if (password == null) { // Dialog canceled
								isdone = true;
							} else {
								JFritz.setProperty("box.password", Encryption //$NON-NLS-1$
										.encrypt(password));
							}
						} catch (IOException e) {
							// Warten, falls wir von einem Standby aufwachen,
							// oder das Netzwerk temporär nicht erreichbar ist.
							if (connectionFailures < 5) {
								Debug.msg("Waiting for FritzBox, retrying ..."); //$NON-NLS-1$
								connectionFailures++;
							} else {
								Debug.msg("Callerlist Box not found"); //$NON-NLS-1$
								setBusy(false);
								setStatus(JFritz.getMessage("box_not_found")); //$NON-NLS-1$
								String box_address = showAddressDialog(JFritz
										.getProperty("box.address", "fritz.box")); //$NON-NLS-1$,  //$NON-NLS-2$
								if (box_address == null) { // Dialog canceled
									isdone = true;
								} else {
									JFritz.setProperty("box.address", //$NON-NLS-1$
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
					if (JFritz.getProperty("option.lookupAfterFetch", "false") //$NON-NLS-1$,  //$NON-NLS-2$
							.equals("true")) { //$NON-NLS-1$
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
						setStatus(JFritz.getMessage("reverse_lookup")); //$NON-NLS-1$
						for (int i = 0; i < jfritz.getCallerlist()
								.getRowCount(); i++) {
							Vector data = jfritz.getCallerlist()
									.getFilteredCallVector();
							Call call = (Call) data.get(i);
							PhoneNumber number = call.getPhoneNumber();
							if (number != null && (call.getPerson() == null)) {
								j++;
								setStatus(JFritz
										.getMessage("reverse_lookup_for") //$NON-NLS-1$
										+ " " + number.getIntNumber() + " ..."); //$NON-NLS-1$,  //$NON-NLS-2$
								Debug.msg("Reverse lookup for " //$NON-NLS-1$
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
			Debug.err("Multiple clicking is disabled.."); //$NON-NLS-1$
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
							.getProperty("box.address", "192.168.178.1"), //$NON-NLS-1$,  //$NON-NLS-2$
							Encryption.decrypt(JFritz
									.getProperty("box.password")), //$NON-NLS-1$
							new FritzBoxFirmware(JFritz
									.getProperty("box.firmware"))); //$NON-NLS-1$
					jfritz.getSIPProviderTableModel().updateProviderList(data);
					jfritz.getSIPProviderTableModel().fireTableDataChanged();
					jfritz.getSIPProviderTableModel().saveToXMLFile(
							JFritz.SIPPROVIDER_FILE);
					jfritz.getCallerlist().fireTableDataChanged();
				} catch (WrongPasswordException e1) {
                    jfritz.errorMsg(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
                    Debug.errDlg(JFritz.getMessage("wrong_password")); //$NON-NLS-1$
				} catch (IOException e1) {
                    jfritz.errorMsg(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
                    Debug.errDlg(JFritz.getMessage("box_address_wrong")); //$NON-NLS-1$
				} catch (InvalidFirmwareException e1) {
                    jfritz.errorMsg(JFritz.getMessage("unknown_firmware")); //$NON-NLS-1$
                    Debug.errDlg(JFritz.getMessage("unknown_firmware")); //$NON-NLS-1$
				}
			}
			monitorButton.setEnabled((Integer.parseInt(JFritz.getProperty(
					"option.callMonitorType", "0")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$

			TableColumnModel colModel = jfritz.getJframe().getCallerTable()
					.getColumnModel();

			// Show / hide CallByCall column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showCallByCallColumn", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$

				// No Call-by-call column found. Add one
				if (getCallerTable().getColumnIndex("callbycall") == -1) {  //$NON-NLS-1$
					colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getCallByCallColumn());
					colModel.getColumn(colModel.getColumnCount() - 1)
							.setPreferredWidth(
									Integer.parseInt(JFritz.getProperty(
											"column.callbycall.width", "50"))); //$NON-NLS-1$, //$NON-NLS-2$
				}
			} else {
				// Try to remove Call-By-Call Column
				int columnIndex = getCallerTable().getColumnIndex("callbycall"); //$NON-NLS-1$
				if (columnIndex != -1)
					colModel.removeColumn(colModel.getColumn(columnIndex));
			}
			// Show / hide comment column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showCommentColumn", "true"))) { //$NON-NLS-1$, //$NON-NLS-2$

				// No comment column found. Add one
				if (getCallerTable().getColumnIndex("comment") == -1) { //$NON-NLS-1$
					colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getCommentColumn());
					colModel.getColumn(colModel.getColumnCount() - 1)
							.setPreferredWidth(
									Integer.parseInt(JFritz.getProperty(
											"column.comment.width", "50")));//$NON-NLS-1$, //$NON-NLS-2$
				}
			} else {
				// Try to remove comment column
				int columnIndex = getCallerTable().getColumnIndex("comment");//$NON-NLS-1$
				if (columnIndex != -1)
					colModel.removeColumn(colModel.getColumn(columnIndex));
			}
			// Show / hide port column
			if (JFritzUtils.parseBoolean(JFritz.getProperty(
					"option.showPortColumn", "true"))) {//$NON-NLS-1$, //$NON-NLS-2$

				// No port column found. Add one
				if (getCallerTable().getColumnIndex("port") == -1) { //$NON-NLS-1$
					colModel.addColumn(jfritz.getJframe().getCallerTable()
							.getPortColumn());
					colModel.getColumn(colModel.getColumnCount() - 1)
							.setPreferredWidth(
									Integer.parseInt(JFritz.getProperty(
											"column.port.width", "50")));//$NON-NLS-1$, //$NON-NLS-2$
				}
			} else {
				// Try to remove port column
				int columnIndex = getCallerTable().getColumnIndex("port");//$NON-NLS-1$
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
		JOptionPane.showMessageDialog(this, JFritz.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ JFritz.PROGRAM_VERSION + "\n" //$NON-NLS-1$
				+ JFritzUtils.getVersionFromCVSTag(JFritz.CVS_TAG) + "\n\n" //$NON-NLS-1$
				+ "(c) 2005 by " + JFritz.PROGRAM_AUTHOR + "\n\n" //$NON-NLS-1$,  //$NON-NLS-2$
				+ "Developers:\n" + JFritz.PROGRAM_AUTHOR + "\n" //$NON-NLS-1$,  //$NON-NLS-2$
				+ "Robert Palmer <robotniko@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Christian Klein <kleinch@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Benjamin Schmitt <little_ben@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Bastian Schaefer <baefer@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Brian Jensen <jensen@users.sourceforge.net>\n" + "\n" //$NON-NLS-1$,  //$NON-NLS-2$
				+ JFritz.PROGRAM_URL + "\n\n" //$NON-NLS-1$
				+ "This tool is developed and released under\n" //$NON-NLS-1$
				+ "the terms of the GNU General Public License\n\n" //$NON-NLS-1$
				+ "Long live Free Software!"); //$NON-NLS-1$
	}

	/**
	 * Shows the exit dialog
	 */
	public void showExitDialog() {
		boolean exit = true;

		if (JFritzUtils.parseBoolean(JFritz.getProperty("option.confirmOnExit", //$NON-NLS-1$
				"false"))) //$NON-NLS-1$
			exit = JOptionPane.showConfirmDialog(this, JFritz
					.getMessage("really_quit"), JFritz.PROGRAM_NAME, //$NON-NLS-1$
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
			if (JFritzUtils.parseBoolean(JFritz.getProperty("option.minimize", //$NON-NLS-1$
					"false"))) { //$NON-NLS-1$
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
					.getClientProperty("lnf name"); //$NON-NLS-1$
			try {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(this);
				JFritz.setProperty("lookandfeel", info.getClassName()); //$NON-NLS-1$
				jfritz.refreshWindow();
			} catch (Exception e) {
				Debug.err("Unable to set UI " + e.getMessage()); //$NON-NLS-1$
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
		String status =
				JFritz.getMessage("telephone_entries").replaceAll("%N",Integer.toString(jfritz.getCallerlist().getRowCount())) + ", " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				+ JFritz.getMessage("total_duration") + ": " + hours + "h " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				+ mins + " min " + " (" + duration / 60 + " min)"; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		;
		progressbar.setString(status);
	}

	/**
	 * Sets text in the status bar
	 *
	 * @param status
	 */
	public void setStatus(String status) {
		if (status.equals("")) //$NON-NLS-1$
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
							"option.callMonitorType", "0")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$
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
		Debug.msg("Action " + e.getActionCommand()); //$NON-NLS-1$
		if (e.getActionCommand().equals("exit")) //$NON-NLS-1$
			showExitDialog();
		else if (e.getActionCommand().equals("about")) //$NON-NLS-1$
			showAboutDialog();
		else if (e.getActionCommand().equals("help")) { //$NON-NLS-1$
			BrowserLaunch.openURL(JFritz.DOCUMENTATION_URL);
		} else if (e.getActionCommand().equals("website")) { //$NON-NLS-1$
			BrowserLaunch.openURL(JFritz.PROGRAM_URL);
		} else if (e.getActionCommand().equals("export_csv")) //$NON-NLS-1$
			exportCallerListToCSV();
		else if (e.getActionCommand().equals("export_phonebook")) //$NON-NLS-1$
			exportPhoneBookToCSV();
		else if (e.getActionCommand().equals("print_callerlist")) //$NON-NLS-1$
			printCallerList();
		else if (e.getActionCommand().equals("import_outlook")) //$NON-NLS-1$
			importOutlook();
		else if (e.getActionCommand().equals("config")) //$NON-NLS-1$
			showConfigDialog();
		else if (e.getActionCommand().equals("callerlist")) //$NON-NLS-1$
			tabber.setSelectedComponent(callerListPanel);
		else if (e.getActionCommand().equals("phonebook")) //$NON-NLS-1$
			activatePhoneBook();
		else if (e.getActionCommand().equals("quickdial")) //$NON-NLS-1$
			tabber.setSelectedComponent(quickDialPanel);
		else if (e.getActionCommand().equals("stats")) //$NON-NLS-1$
			showStatsDialog();
		else if (e.getActionCommand().equals("fetchList")) //$NON-NLS-1$
			fetchList();
		else if (e.getActionCommand().equals("delete_fritzbox_callerlist")) //$NON-NLS-1$
			deleteFritzBoxCallerList();
		else if (e.getActionCommand().equals("backup")) //$NON-NLS-1$
			backupToChoosenDirectory();
		else if (e.getActionCommand().equals("fetchTask")) //$NON-NLS-1$
			fetchTask(((JToggleButton) e.getSource()).isSelected());
		else if (e.getActionCommand().equals("callMonitor")) { //$NON-NLS-1$
			boolean active = ((JToggleButton) e.getSource()).isSelected();
			if (active) {
				Debug.msg("Start callMonitor"); //$NON-NLS-1$
				startChosenCallMonitor();
			} else {
				Debug.msg("Stop callMonitor"); //$NON-NLS-1$
				jfritz.stopCallMonitor();
			}

		} else if (e.getActionCommand().equals("reverselookup")) //$NON-NLS-1$
			reverseLookup();
		else if (e.getActionCommand().equals("F5")) //$NON-NLS-1$
			fetchList();
		else if (e.getActionCommand().equals("import_callerlist_csv")) //$NON-NLS-1$
			importCallerlistCSV();
		else if (e.getActionCommand().equals("phonebook_import")) //$NON-NLS-1$
			phoneBookPanel.importFromXML();
		else if (e.getActionCommand().equals("import_contacts_thunderbird_csv")) //$NON-NLS-1$
			importContactsThunderbirdCSV();
		else if (e.getActionCommand().equals("showhide")) {
			setVisible(!isVisible());
		}
		else
			Debug.err("Unimplemented action: " + e.getActionCommand()); //$NON-NLS-1$
	}

	/**
	 * Exports caller list as CSV
	 */
	public void exportCallerListToCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("export_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, JFritz
						.getMessage("overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						 JFritz.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
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
				"options.exportXMLpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("dialog_title_export_callerlist_xml")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("xml_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportXMLpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, JFritz
						.getMessage("overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						 JFritz.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
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
	 *
	 * @author Bastian Schaefer
	 */
	public void exportPhoneBookToCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpathOfPhoneBook", null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("export_csv_phonebook")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.PHONEBOOK_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportCSVpathOfPhoneBook", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, JFritz
						.getMessage("overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						 JFritz.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					jfritz.getPhonebook().saveToCSVFile(file.getAbsolutePath(),
							false);
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
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
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
		Debug.msg("Starte Import von Outlook"); //$NON-NLS-1$
		Thread thread = new Thread(new ImportOutlookContacts(jfritz));
		thread.start();
	}

	public void startChosenCallMonitor() {
		switch (Integer.parseInt(JFritz.getProperty("option.callMonitorType", //$NON-NLS-1$
				"0"))) { //$NON-NLS-1$
			case 1 : {
				FritzBoxFirmware currentFirm;
				try {
					currentFirm = JFritzUtils.detectBoxType("", JFritz //$NON-NLS-1$
							.getProperty("box.address"), Encryption //$NON-NLS-1$
							.decrypt(JFritz.getProperty("box.password", ""))); //$NON-NLS-1$,  //$NON-NLS-2$
					if (currentFirm.getMajorFirmwareVersion() == 3
							&& currentFirm.getMinorFirmwareVersion() < 96) {
						Debug.errDlg(JFritz.getMessage("callmonitor_error_wrong_firmware")); //$NON-NLS-1$
						monitorButton.setSelected(false);
						this.setCallMonitorButtons(JFritz.CALLMONITOR_START);
					} else {
						if (currentFirm.getMajorFirmwareVersion() >= 4
								&& currentFirm.getMinorFirmwareVersion() >= 3) {
							jfritz.setCallMonitor(new FBoxListenerV3(jfritz));
						} else {
							jfritz.setCallMonitor(new FBoxListenerV1(jfritz));
						}
						this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
					}
				} catch (WrongPasswordException e) {
					jfritz.getJframe().setStatus(
							JFritz.getMessage("password_wrong")); //$NON-NLS-1$
					String password = jfritz.getJframe().showPasswordDialog(
							Encryption.decrypt(JFritz.getProperty(
									"box.password", ""))); //$NON-NLS-1$,  //$NON-NLS-2$
					if (password != null) { // Dialog not canceled
						JFritz.setProperty("box.password", Encryption //$NON-NLS-1$
								.encrypt(password));
					}
				} catch (IOException e) {
					Debug.err("Could not detect box."); //$NON-NLS-1$
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
						Integer.parseInt(JFritz.getProperty("option.yacport", //$NON-NLS-1$
								"10629")))); //$NON-NLS-1$
				this.setCallMonitorButtons(JFritz.CALLMONITOR_STOP);
				break;
			}
			case 5 : {
				jfritz.setCallMonitor(new CallmessageListener(jfritz, Integer
						.parseInt(JFritz.getProperty("option.callmessageport", //$NON-NLS-1$
								"23232")))); //$NON-NLS-1$
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
				.getMessage("delete_fritzbox_callerlist_confirm_msg"), JFritz //$NON-NLS-1$
				.getMessage("delete_fritzbox_callerlist"), //$NON-NLS-1$
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
			backup.copy(".", "xml", directory); //$NON-NLS-1$,  //$NON-NLS-2$
		} catch (NullPointerException e) {
			Debug.msg("No directory choosen for backup!"); //$NON-NLS-1$
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
	 * Provides easy creation of a KeyStroke object without a modifier and
	 * reaction onKeyReale
	 */
	private KeyStroke keyStroke(int vkey) {
		return KeyStroke.getKeyStroke(vkey, 0, false);
	}

	/**
	 * @author Brian Jensen
	 *
	 * opens the import csv dialog currently supported types: fritzbox native
	 * type and jfritz native type
	 *
	 * does a reverse lookup on return
	 */
	public void importCallerlistCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			JFritz.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(this, JFritz.getMessage("file_not_found"), //$NON-NLS-1$
						JFritz.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

			} else {
				jfritz.getCallerlist()
						.importFromCSVFile(file.getAbsolutePath());

				if (JFritz.getProperty("option.lookupAfterFetch", "false") //$NON-NLS-1$,  //$NON-NLS-2$
						.equals("true")) { //$NON-NLS-1$
					lookupButton.doClick();
				}
			}
		}
	}

	/**
	 * @author Brian Jensen
	 *
	 * opens the import thunderbird dialog selects a file then passes it on to
	 * PhoneBook.importFromThunderbirdCSVfile()
	 *
	 */
	public void importContactsThunderbirdCSV() {
		JFileChooser fc = new JFileChooser(JFritz.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("import_contacts_thunderbird_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			// options.import_contacts_thunderbird_CSVpath ???
			// JFritz.setProperty("options.exportCSVpath", path);
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(this, JFritz.getMessage("file_not_found"), //$NON-NLS-1$
						JFritz.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			} else {
				jfritz.getPhonebook().importFromThunderbirdCSVfile(
						file.getAbsolutePath());

				if (JFritz.getProperty("option.lookupAfterFetch", "false") //$NON-NLS-1$,  //$NON-NLS-2$
						.equals("true")) { //$NON-NLS-1$
					lookupButton.doClick();
				}
			}
		}
	}

	/**
	 * @author Brian Jensen This function changes the ResourceBundle in the
	 *         jfritz instance Then the jfritz object destroys the current
	 *         window and redraws a new one
	 *
	 * NOTE: This function is currently experimental
	 *
	 * @param locale
	 *            to switch the language to
	 */
	public void setLanguage(Locale locale) {
		jfritz.createNewWindow(locale);
		// current window will be destroyed and a new one created

		jfritz.refreshTrayMenu();
	}

	/**
	 * @author Bastian Schaefer
	 *
	 * The following 3 methods (getMaximizedBounds(), setMaximizedBounds()
	 * and setExtendedState()) are a workaround for the ensuing described bug:
	 *
	 *
	 * release: 5.0 hardware: x86 OSversion: win_xp priority: 4 synopsis:
	 * decorated and maximized JFrame hides taskbar description: FULL PRODUCT
	 * VERSION : java version "1.5.0_06" Java(TM) 2 Runtime Environment,
	 * Standard Edition (build 1.5 Java HotSpot(TM) Client VM (build
	 * 1.5.0_06-b05, mixed mode)
	 *
	 * ADDITIONAL OS VERSION INFORMATION : Microsoft Windows XP [Version
	 * 5.1.2600]
	 *
	 * A DESCRIPTION OF THE PROBLEM : When maximizing an default "look and feel"
	 * decorated frame, it covers the entire screen even though it should not
	 * cover the Windows taskbar.
	 *
	 * STEPS TO FOLLOW TO REPRODUCE THE PROBLEM : Run this program. Click the
	 * maximize button.
	 *
	 * EXPECTED VERSUS ACTUAL BEHAVIOR : EXPECTED - The frame should be
	 * maximized, without covering the taskbar. ACTUAL - The taskbar is covered.
	 *
	 * REPRODUCIBILITY : This bug can be reproduced always.
	 *
	 * ---------- BEGIN SOURCE ----------
	 * import javax.swing.*; public class
	 * MaxFrame extends JFrame { public static void main(String[] args) {
	 * JFrame.setDefaultLookAndFeelDecorated(true);
	 *
	 * JFrame f = new JFrame();
	 *
	 * f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.pack();
	 * f.setBounds(100, 100, 100, 100); f.setVisible(true); } }
	 * ---------- END SOURCE ----------
	 */

	public Rectangle getMaximizedBounds() {
		return (maxBounds);
	}

	public synchronized void setMaximizedBounds(Rectangle maxBounds) {
		this.maxBounds = maxBounds;
		super.setMaximizedBounds(maxBounds);
	}

	public synchronized void setExtendedState(int state) {
		if (maxBounds == null
				&& (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
			Insets screenInsets = getToolkit().getScreenInsets(
					getGraphicsConfiguration());
			Rectangle screenSize = getGraphicsConfiguration().getBounds();
			Rectangle maxBounds = new Rectangle(screenInsets.left
					+ screenSize.x, screenInsets.top + screenSize.y,
					screenSize.x + screenSize.width - screenInsets.right
							- screenInsets.left, screenSize.y
							+ screenSize.height - screenInsets.bottom
							- screenInsets.top);
			super.setMaximizedBounds(maxBounds);
		}

		super.setExtendedState(state);
	}

}