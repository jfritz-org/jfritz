/**
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.moonflower.jfritz.autoupdate.JFritzUpdate;
import de.moonflower.jfritz.autoupdate.Update;
import de.moonflower.jfritz.callerlist.CallDialog;
import de.moonflower.jfritz.callerlist.CallerListPanel;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.callerlist.FetchListTimer;

import de.moonflower.jfritz.dialogs.config.ConfigDialog;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.quickdial.QuickDialPanel;
import de.moonflower.jfritz.dialogs.simple.AddressPasswordDialog;
import de.moonflower.jfritz.dialogs.simple.CallMessageDlg;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.dialogs.stats.StatsDialog;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.monitoring.MonitoringPanel;
import de.moonflower.jfritz.network.NetworkStateListener;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.phonebook.PhoneBookPanel;

import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.DirectoryChooser;
import de.moonflower.jfritz.utils.ImportOutlookContactsDialog;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.PrintCallerList;
import de.moonflower.jfritz.utils.SwingWorker;

/**
 * This is main window class of JFritz, which creates the GUI.
 *
 * @author akw
 */
public class JFritzWindow extends JFrame implements Runnable, ActionListener,
		ItemListener, NetworkStateListener {

	private static final long serialVersionUID = 1;

	private FetchListTimer timer = null;

	private TimerTask timerTask = null;

	private JMenuBar menu;

	private JToolBar mBar;

	private JButton fetchButton, configButton, calldialogButton;

	private JToggleButton taskButton, monitorButton, lookupButton, networkButton;

	private JProgressBar progressbar;

	private StatusBarPanel mainStatusPanel, iconStatusPanel;

	private StatusBar statusBar;

	private JLabel mainStatusBar;

	private boolean isretrieving = false;

	private JTabbedPane tabber;

	private CallerListPanel callerListPanel;

	private PhoneBookPanel phoneBookPanel;

	private QuickDialPanel quickDialPanel;

	private MonitoringPanel monitoringPanel;

	private ConfigDialog configDialog;

	private Rectangle maxBounds;

	private JFritz jFritz;

	private ImageIcon connectIcon;

	private ImageIcon disconnectIcon;

	private ImageIcon callMonitorConnectIcon;

	private ImageIcon callMonitorDisconnectIcon;

	private JLabel callMonitorConnectButton;

	private JLabel connectButton;

	private JFritzWindow thisWindow;

	private boolean callMonitorStarted = false;

	public final String WINDOW_PROPERTIES_FILE = "jfritz.window.properties.xml"; //$NON-NLS-1$

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		this.jFritz = jfritz;
		Debug.msg("Create JFritz-GUI"); //$NON-NLS-1$
		maxBounds = null;
		createGUI();
    	CallMessageDlg callMsgDialog = new CallMessageDlg();
    	callMsgDialog.showIncomingCall(null, "", "", null);
    	callMsgDialog.close();
    	callMsgDialog.dispose();
		thisWindow = this;
		this.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
				Debug.msg("Window hidden");
			}

			public void componentMoved(ComponentEvent arg0) {
				Debug.msg("Window moved");
				Main.setStateProperty("position.left", Integer.toString(getLocation().x)); //$NON-NLS-1$
				Main.setStateProperty("position.top", Integer.toString(getLocation().y));//$NON-NLS-1$
				Main.setStateProperty("position.width", Integer.toString(thisWindow.getWidth()));//$NON-NLS-1$
				Main.setStateProperty("position.height", Integer.toString(thisWindow.getHeight()));//$NON-NLS-1$
			}

			public void componentResized(ComponentEvent arg0) {
				Debug.msg("Window resized");
				Main.setStateProperty("position.left", Integer.toString(getLocation().x)); //$NON-NLS-1$
				Main.setStateProperty("position.top", Integer.toString(getLocation().y));//$NON-NLS-1$
				Main.setStateProperty("position.width", Integer.toString(thisWindow.getWidth()));//$NON-NLS-1$
				Main.setStateProperty("position.height", Integer.toString(thisWindow.getHeight()));//$NON-NLS-1$
			}

			public void componentShown(ComponentEvent arg0) {
				Debug.msg("Window shown");
				Main.setStateProperty("window.state.old", Main.getStateProperty("window.state", Integer.toString(Frame.NORMAL)));
				Main.setStateProperty("window.state", Integer.toString(getExtendedState()));
			}
		});
		addWindowStateListener(new WindowStateListener() {

			public void windowStateChanged(WindowEvent arg0) {
				Debug.msg("Window state changed: " + Main.getStateProperty("window.state", Integer.toString(Frame.NORMAL)) + " -> " + Integer.toString(getExtendedState()));
				Main.setStateProperty("window.state.old", Main.getStateProperty("window.state", Integer.toString(Frame.NORMAL)));
				Main.setStateProperty("window.state", Integer.toString(getExtendedState()));
			}

		});
	}

	public void checkStartOptions() {
		Debug.msg("CHECKSTARTOPTIONS: ");
		if (!Main.getProperty("option.startMinimized", "false") //$NON-NLS-1$,  //$NON-NLS-2$,
				.equals("true")) { //$NON-NLS-1$
			setVisible(true);
			Debug.msg("CHECKSTARTOPTIONS: don't start minimized");
		}
//		} else {
//			if (!Main.SYSTRAY_SUPPORT) {
//				setVisible(true);
//			}
//			if ( getExtendedState() != Frame.ICONIFIED )
//			{
//				setExtendedState(Frame.ICONIFIED);
//			}
//		}
	}

	public void checkOptions() {
		if (Main.getProperty("option.timerAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			taskButton.doClick();
		}
		if (Main.getProperty("option.fetchAfterStart", "false") //$NON-NLS-1$,  //$NON-NLS-2$
				.equals("true")) { //$NON-NLS-1$
			fetchButton.doClick();
		}
		if (Main.getProperty("option.autostartcallmonitor", "false").equals( //$NON-NLS-1$,  //$NON-NLS-2$
				"true")) { //$NON-NLS-1$
			if (callMonitorStarted == false)
			{
				callMonitorStarted = true;
				jFritz.startChosenCallMonitor(true);
			}
		}
	}

	private void createGUI() {
		setTitle(Main.PROGRAM_NAME);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		statusBar = createStatusBar2();

		addKeyListener(KeyEvent.VK_F5, "F5"); //$NON-NLS-1$

		this
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								getClass()
										.getResource(
												"/de/moonflower/jfritz/resources/images/trayicon.png"))); //$NON-NLS-1$

		callerListPanel = new CallerListPanel(JFritz.getCallerList(), this);
		phoneBookPanel = new PhoneBookPanel(JFritz.getPhonebook(), this);
		phoneBookPanel.getStatusBarController().addStatusBarListener(jFritz);
		callerListPanel.setPhoneBookPanel(phoneBookPanel);
		callerListPanel.getStatusBarController().addStatusBarListener(jFritz);
		quickDialPanel = new QuickDialPanel(JFritz.getQuickDials());
		quickDialPanel.getStatusBarController().addStatusBarListener(jFritz);
		// New code here, remove if problematic
		monitoringPanel = new MonitoringPanel();
		monitoringPanel.getStatusBarController().addStatusBarListener(jFritz);

		tabber = new JTabbedPane(SwingConstants.BOTTOM);
		tabber.addTab(Main.getMessage("callerlist"), callerListPanel); //$NON-NLS-1$
		tabber.addTab(Main.getMessage("phonebook"), phoneBookPanel); //$NON-NLS-1$
		tabber.addTab(Main.getMessage("quickdials"), quickDialPanel); //$NON-NLS-1$
		tabber.addTab(Main.getMessage("monitoring"), monitoringPanel);
		tabber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("callerlist"))) { //$NON-NLS-1$
					callerListPanel.setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("phonebook"))) { //$NON-NLS-1$
					phoneBookPanel.setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("quickdials"))) { //$NON-NLS-1$
					quickDialPanel.setStatus();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("monitoring"))) {
					monitoringPanel.setStatus();
				}
			}
		});

		// Adding gui components
		setJMenuBar(createMenu());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createMainToolBar(), BorderLayout.NORTH);
		getContentPane().add(tabber, BorderLayout.CENTER);
		JPanel statusPanels = new JPanel();
		statusPanels.setLayout(new BorderLayout());
//		statusPanels.add(createStatusBar(), BorderLayout.CENTER);
		statusPanels.add(statusBar, BorderLayout.SOUTH);
		getContentPane().add(statusPanels, BorderLayout.SOUTH);
		JFritz.getCallerList().fireTableDataChanged();
		JFritz.getCallerList().fireTableStructureChanged();

		// Setting size and position
		int x = Integer.parseInt(Main.getStateProperty(
				"position.left", "10")); //$NON-NLS-1$,  //$NON-NLS-2$
		int y = Integer.parseInt(Main.getStateProperty(
				"position.top", "10")); //$NON-NLS-1$,  //$NON-NLS-2$
		int w = Integer.parseInt(Main.getStateProperty(
				"position.width", "640")); //$NON-NLS-1$,  //$NON-NLS-2$
		int h = Integer.parseInt(Main.getStateProperty(
				"position.height", "400")); //$NON-NLS-1$,  //$NON-NLS-2$

		int windowState = Frame.NORMAL;

		Debug.msg("CREATE GUI: ");
		if ((!Main.getProperty("option.startMinimized", "false").equals("true")) &&
			(Frame.ICONIFIED == Integer.parseInt(Main.getStateProperty("window.state", Integer.toString(Frame.NORMAL)))))
		{ // Old state was iconified and we don't want to startup iconified
		  // Set previous old state to prevent bug in showing menu bar
			windowState = Integer.parseInt(Main.getStateProperty(
					"window.state.old", Integer.toString(Frame.NORMAL)));
			Debug.msg("CREATE GUI: restore old window state " + Integer.toString(windowState));
		} else
		{
			windowState = Integer.parseInt(Main.getStateProperty(
					"window.state", Integer.toString(Frame.NORMAL)));
			Debug.msg("CREATE GUI: restore window state " + Integer.toString(windowState));
		}
		setLocation(x, y);
		setSize(w, h);
		setExtendedState(windowState);
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
	 * Create the StatusBar
	 */
	public StatusBar createStatusBar2() {
		statusBar = new StatusBar();
		mainStatusPanel = new StatusBarPanel(1);
		mainStatusBar = new JLabel("");
		mainStatusPanel.add(mainStatusBar);
		mainStatusPanel.setVisible(false);
		statusBar.registerDynamicStatusPanel(mainStatusPanel);

		iconStatusPanel = new StatusBarPanel(1);
		iconStatusPanel.setVisible(true);
		connectIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/connect.png")); //$NON-NLS-1$
		disconnectIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/disconnect.png")); //$NON-NLS-1$
		callMonitorConnectIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/monitor-up.png")); //$NON-NLS-1$
		callMonitorDisconnectIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/monitor-down.png")); //$NON-NLS-1$
		connectButton = new JLabel("");
		callMonitorConnectButton = new JLabel("");
		setDisconnectedStatus();
		iconStatusPanel.add(connectButton);
		iconStatusPanel.add(callMonitorConnectButton);
		statusBar.registerStatusIcon(iconStatusPanel);

		return statusBar;
	}

	public void setDisconnectedStatus()
	{
		connectButton.setIcon(disconnectIcon);
		connectButton.setToolTipText(Main.getMessage("disconnected_fritz"));
		setCallMonitorButtonPushed(false);

	}

	public void setConnectedStatus()
	{
			connectButton.setIcon(connectIcon);
			connectButton.setToolTipText(Main.getMessage("connected_fritz"));
	}

	public void setCallMonitorConnectedStatus()
	{
		if (JFritz.getCallMonitor() != null)
		{
			callMonitorConnectButton.setIcon(callMonitorConnectIcon);
			callMonitorConnectButton.setToolTipText(Main.getMessage("connected_callmonitor"));
			callMonitorConnectButton.setVisible(true);
		}
		else
		{
			callMonitorConnectButton.setVisible(false);
		}
	}

	public void setCallMonitorDisconnectedStatus()
	{
		if (JFritz.getCallMonitor() != null)
		{
			callMonitorConnectButton.setIcon(callMonitorDisconnectIcon);
			callMonitorConnectButton.setToolTipText(Main.getMessage("disconnected_callmonitor"));
			callMonitorConnectButton.setVisible(true);
		}
		else
		{
			callMonitorConnectButton.setVisible(false);
		}
	}

	/**
	 * Creates the main ToolBar
	 */
	public JToolBar createMainToolBar() {
		mBar = new JToolBar();
		mBar.setFloatable(true);

		fetchButton = new JButton();
		fetchButton.setToolTipText(Main.getMessage("fetchlist")); //$NON-NLS-1$
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
		// button.setToolTipText(Main.getMessage("call"));
		// mBar.add(button);

		taskButton = new JToggleButton();
		taskButton.setToolTipText(Main.getMessage("fetchtask")); //$NON-NLS-1$
		taskButton.setActionCommand("fetchTask"); //$NON-NLS-1$
		taskButton.addActionListener(this);
		taskButton.setIcon(getImage("clock.png")); //$NON-NLS-1$
		mBar.add(taskButton);

		monitorButton = new JToggleButton();
		monitorButton.setToolTipText(Main.getMessage("callmonitor")); //$NON-NLS-1$
		monitorButton.setActionCommand("callMonitor"); //$NON-NLS-1$
		monitorButton.addActionListener(this);
		monitorButton.setIcon(getImage("monitor.png")); //$NON-NLS-1$
		mBar.add(monitorButton);

		calldialogButton = new JButton();
		calldialogButton.setToolTipText(Main.getMessage("call"));
		calldialogButton.setActionCommand("callDialog");
		calldialogButton.addActionListener(this);
		calldialogButton.setIcon(getImage("PhoneBig.png"));
		mBar.add(calldialogButton);

		lookupButton = new JToggleButton();
		lookupButton.setToolTipText(Main.getMessage("reverse_lookup")); //$NON-NLS-1$
		lookupButton.setActionCommand("reverselookup"); //$NON-NLS-1$
		lookupButton.addActionListener(this);
		lookupButton.setIcon(getImage("reverselookup.png")); //$NON-NLS-1$
		mBar.add(lookupButton);

		button = new JButton();
		button.setActionCommand("phonebook"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("phonebook.png")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("phonebook")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("quickdials")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("delete_fritzbox_callerlist"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("DeleteList.gif")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("delete_fritzbox_callerlist")); //$NON-NLS-1$
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("backup"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("Backup.gif")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("backup")); //$NON-NLS-1$
		mBar.add(button);

		mBar.addSeparator();

		button = new JButton();
		button.setActionCommand("stats"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("stats.png")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("stats")); //$NON-NLS-1$
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("help"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("help.png")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("help_menu")); //$NON-NLS-1$
		// button.setEnabled(JFritz.DEVEL_VERSION);
		mBar.add(button);

		mBar.addSeparator();

		configButton = new JButton();
		configButton.setActionCommand("config"); //$NON-NLS-1$
		configButton.addActionListener(this);
		configButton.setIcon(getImage("config.png")); //$NON-NLS-1$
		configButton.setToolTipText(Main.getMessage("config")); //$NON-NLS-1$
		mBar.add(configButton);

		networkButton = new JToggleButton();
		networkButton.setActionCommand("network");
		networkButton.addActionListener(this);

		String networkType = Main.getProperty("network.type", "0");

		if(networkType.equals("1")){
			networkButton.setIcon(getImage("server.png"));
			networkButton.setToolTipText(Main.getMessage("start_listening_clients"));
		}else if(networkType.equals("2")){
			networkButton.setIcon(getImage("client.png"));
			networkButton.setToolTipText(Main.getMessage("connect_to_server"));
		}else{
			networkButton.setIcon(getImage("no_network.png"));
			networkButton.setEnabled(false);
		}

		networkButton.setPreferredSize(new Dimension(32, 32));

		//disable icon if jfritz network functionality not wanted
		if(Main.getProperty("network.type", "0").equals("0")){
			networkButton.setEnabled(false);
		}

		NetworkStateMonitor.addListener(this);

		mBar.add(networkButton);

		mBar.addSeparator();
		return mBar;
	}

	/**
	 * Creates the menu bar
	 */
	public JMenuBar createMenu() {
		String menu_text = Main.PROGRAM_NAME;
		if (JFritz.runsOn().equals("Mac")) {
			menu_text = "File"; //$NON-NLS-1$
		}

		JMenu jfritzMenu = new JMenu(menu_text);
		// JMenu editMenu = new JMenu(Main.getMessage("edit_menu"));
		JMenu optionsMenu = new JMenu(Main.getMessage("options_menu")); //$NON-NLS-1$
		JMenu helpMenu = new JMenu(Main.getMessage("help_menu")); //$NON-NLS-1$
		JMenu lnfMenu = new JMenu(Main.getMessage("lnf_menu")); //$NON-NLS-1$
		JMenu importMenu = new JMenu(Main.getMessage("import_menu")); //$NON-NLS-1$
		JMenu exportMenu = new JMenu(Main.getMessage("export_menu")); //$NON-NLS-1$
		JMenu viewMenu = new JMenu(Main.getMessage("view_menu")); //$NON-NLS-1$

		// File menu
		JMenuItem item = new JMenuItem(Main.getMessage("fetchlist"), 'a'); //$NON-NLS-1$,
		item.setActionCommand("fetchList"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);
		item = new JMenuItem(Main.getMessage("reverse_lookup"), 'l'); //$NON-NLS-1$,
		item.setActionCommand("reverselookup"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(Main.getMessage("delete_fritzbox_callerlist")); //$NON-NLS-1$
		item.setActionCommand("delete_fritzbox_callerlist"); //$NON-NLS-1$
		item.setMnemonic(KeyEvent.VK_F);
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(Main
				.getMessage("delete_duplicate_phonebook_entries")); //$NON-NLS-1$
		item.setActionCommand("delete_duplicate_phonebook_entries"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(Main.getMessage("backup")); //$NON-NLS-1$
		item.setActionCommand("backup"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		item = new JMenuItem(Main.getMessage("print_callerlist")); //$NON-NLS-1$
		item.setActionCommand("print_callerlist"); //$NON-NLS-1$
		item.addActionListener(this);
		jfritzMenu.add(item);

		// export submenu
		item = new JMenuItem(Main.getMessage("export_csv"), 'c'); //$NON-NLS-1$,
		item.setActionCommand("export_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		exportMenu.add(item);

		item = new JMenuItem(Main.getMessage("export_csv_phonebook")); //$NON-NLS-1$
		item.setActionCommand("export_phonebook"); //$NON-NLS-1$
		item.addActionListener(this);
		exportMenu.add(item);

		jfritzMenu.add(exportMenu);

		// import submenu

		item = new JMenuItem(Main.getMessage("import_callerlist_csv"), 'i'); //$NON-NLS-1$,
		item.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		item = new JMenuItem(Main.getMessage("phonebook_import")); //$NON-NLS-1$
		item.setActionCommand("phonebook_import"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		item = new JMenuItem(Main.getMessage("import_contacts_thunderbird_csv")); //$NON-NLS-1$
		item.setActionCommand("import_contacts_thunderbird_csv"); //$NON-NLS-1$
		item.addActionListener(this);
		importMenu.add(item);

		if (JFritz.runsOn().startsWith("Windows")) { //$NON-NLS-1$
			item = new JMenuItem(Main.getMessage("import_contacts_outlook")); //$NON-NLS-1$
			item.setActionCommand("import_outlook"); //$NON-NLS-1$
			item.addActionListener(this);
			importMenu.add(item);
		}

		jfritzMenu.add(importMenu);

		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			jfritzMenu.add(new JSeparator());
			item = new JMenuItem(Main.getMessage("prog_exit"), 'x'); //$NON-NLS-1$,
			// item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			// ActionEvent.ALT_MASK));
			item.setActionCommand("exit"); //$NON-NLS-1$
			item.addActionListener(this);
			jfritzMenu.add(item);
		}

		// options menu
		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();

		// Add system dependent look and feels
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

		// Add additional look and feels from looks-2.1.4.jar
		LookAndFeelInfo lnf = new LookAndFeelInfo("Plastic","com.jgoodies.looks.plastic.PlasticLookAndFeel");
		JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic 3D","com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic XP","com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		rb.addItemListener(this);
		lnfgroup.add(rb);

		optionsMenu.add(lnfMenu);

		item = new JMenuItem(Main.getMessage("config_wizard"), null); //$NON-NLS-1$
		item.setActionCommand("configwizard"); //$NON-NLS-1$
		item.addActionListener(this);
		optionsMenu.add(item);

		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			item = new JMenuItem(Main.getMessage("config"), 'e'); //$NON-NLS-1$,
			item.setActionCommand("config"); //$NON-NLS-1$
			item.addActionListener(this);
			optionsMenu.add(item);
		}

		// view menu
		item = new JMenuItem(Main.getMessage("callerlist"), null); //$NON-NLS-1$
		item.setActionCommand("callerlist"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(Main.getMessage("phonebook"), null); //$NON-NLS-1$
		item.setActionCommand("phonebook"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);

		item = new JMenuItem(Main.getMessage("quickdials"), null); //$NON-NLS-1$
		item.setActionCommand("quickdial"); //$NON-NLS-1$
		item.addActionListener(this);
		viewMenu.add(item);


		item = new JMenuItem(Main.getMessage("monitoring"), null);
		item.setActionCommand("monitoring");
		item.addActionListener(this);
		viewMenu.add(item);

		// help menu
		item = new JMenuItem(Main.getMessage("help_content"), 'h'); //$NON-NLS-1$,
		item.setActionCommand("help"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);

		item = new JMenuItem(Main.getMessage("debug_window"), 'h'); //$NON-NLS-1$,
		item.setActionCommand("debug_window"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);

		item = new JMenuItem(Main.getMessage("jfritz_website"), 'w'); //$NON-NLS-1$,
		item.setActionCommand("website"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);
		item = new JMenuItem(Main.getMessage("update_JFritz"), 'w'); //$NON-NLS-1$,
		item.setActionCommand("update"); //$NON-NLS-1$
		item.addActionListener(this);
		helpMenu.add(item);

		if (!JFritz.runsOn().equals("Mac")) { //$NON-NLS-1$
			helpMenu.add(new JSeparator());
			item = new JMenuItem(Main.getMessage("prog_info"), 'i'); //$NON-NLS-1$,
			item.setActionCommand("about"); //$NON-NLS-1$
			item.addActionListener(this);
			helpMenu.add(item);
		}

		menu = new JMenuBar();
		menu.add(jfritzMenu);
		menu.add(optionsMenu);
		menu.add(viewMenu);
		menu.add(helpMenu);
		menu.setVisible(true);
		return menu;
	}

	/**
	 * start/stop timer for cyclic caller list fetching
	 *
	 * @param enabled
	 */
	private void fetchTask(boolean enabled) {
		if (enabled) {
			int interval = Integer.parseInt(Main.getProperty("fetch.timer", //$NON-NLS-1$
					"3")) * 60000;
			timerTask = new TimerTask() {

				public void run() {
					Debug.msg("Running FetchListTask.."); //$NON-NLS-1$
					fetchList();
				}
			};
			timer = new FetchListTimer("FetchList-Timer", true);
			timer.schedule(timerTask, interval, interval); //$NON-NLS-1$
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
		if (timer != null)
		{
			if (timer.getState() == FetchListTimer.STATE_SCHEDULED)
			{
				// restart timer
				timer.cancel();
				timer = null;
				int interval = Integer.parseInt(Main.getProperty("fetch.timer", //$NON-NLS-1$
				"3")) * 60000;
				timerTask.cancel();
				timerTask = new TimerTask() {

					public void run() {
						Debug.msg("Running FetchListTask.."); //$NON-NLS-1$
						fetchList();
					}
				};
				timer = new FetchListTimer("FetchList-Timer2", true);
				timer.schedule(timerTask, interval, interval); //$NON-NLS-1$
			}
		}
		// fetch the call list
		fetchList(false);
	}

	/**
	 * Fetches list from box
	 */
	public void fetchList(final boolean deleteFritzBoxCallerList) {
		Debug.msg("Fetching list ...");
		//only send request to the server if we are connected
		if(Main.getProperty("option.clientCallList", "false").equals("true")
				&& NetworkStateMonitor.isConnectedToServer()){

				//pass on the request to delete the list from the box
			if(deleteFritzBoxCallerList){
				Debug.netMsg("Requesting server to delete the list from the box");
				NetworkStateMonitor.requestDeleteList();
			}else{
				Debug.netMsg("requesting get call list from box from the server");
				NetworkStateMonitor.requestGetCallListFromServer();
			}

			//otherwise act as a standalone instance
		} else if (!isretrieving) { // Prevent multiple clicking
			Debug.msg("1");
			isretrieving = true;
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					Debug.msg("2");
					boolean isdone = false;
					while (!isdone) {
						try {
							Debug.msg("3");
							setBusy(true);
							setStatus(Main.getMessage("fetchdata")); //$NON-NLS-1$
							setConnectedStatus();
							Debug.msg("4");
							JFritz.getCallerList().getNewCalls(
									deleteFritzBoxCallerList);
							Debug.msg("5");
							isdone = true;
						} catch (WrongPasswordException e) {
							setBusy(false);
							isdone = true;
							setDisconnectedStatus();
						} catch (IOException e) {
							setDisconnectedStatus();
							isdone = true;
						} catch (InvalidFirmwareException e) {
							setDisconnectedStatus();
							isdone = true;
							e.printStackTrace();
						}
					}
					return null;
				}

				public void finished() {
					Debug.msg("6");
					setBusy(false);
					JFritz.getCallerList().fireTableStructureChanged();
					Debug.msg("7");
					isretrieving = false;
					if (Main.getProperty("option.lookupAfterFetch", "false") //$NON-NLS-1$,  //$NON-NLS-2$
							.equals("true")) { //$NON-NLS-1$

						if(Main.getProperty("option.clientTelephoneBook", "false").equals("true"))
							lookupButton.doClick();
						else
							JFritz.getCallerList().reverseLookup(false, false);
					}
					Debug.msg("8");
					setStatus("");
//					interrupt();
				}
			};
			Debug.msg("1.1");
			worker.start();
			Debug.msg("1.2");
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
			try {
				configDialog.storeValues();
				Main.saveConfigProperties();
				if (JFritz.getSIPProviderTableModel().getProviderList().size() == 0) { // Noch
					// keine
					// SipProvider
					// eingelesen.
						setConnectedStatus();
						Vector<SipProvider> data = JFritz.getFritzBox()
								.retrieveSipProvider();
						JFritz.getSIPProviderTableModel().updateProviderList(data);
						JFritz.getSIPProviderTableModel().fireTableDataChanged();
						JFritz.getSIPProviderTableModel().saveToXMLFile(
								Main.SAVE_DIR + JFritz.SIPPROVIDER_FILE);
						JFritz.getCallerList().fireTableDataChanged();
				}
			} catch (WrongPasswordException e1) {
				setDisconnectedStatus();
			} catch (IOException e1) {
				setDisconnectedStatus();
			} catch (InvalidFirmwareException e1) {
				setDisconnectedStatus();
			}
			monitorButton.setEnabled((Integer.parseInt(Main.getProperty(
					"option.callMonitorType", "0")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$

			callerListPanel.showHideColumns();
		}
		configDialog.dispose();
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
		JOptionPane.showMessageDialog(this, Main.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ Main.PROGRAM_VERSION + "\n" //$NON-NLS-1$
				+ JFritzUtils.getVersionFromCVSTag(Main.CVS_TAG) + "\n" //$NON-NLS-1$
				+ "(c) 2005-2008 by " + Main.JFRITZ_PROJECT + "\n" //$NON-NLS-1$,  //$NON-NLS-2$
				+ Main.PROGRAM_URL + "\n\n" 							//$NON-NLS-1$
				+ "Project-Admin: " + Main.PROJECT_ADMIN + "\n"		//$NON-NLS-1$
				+ "Project-Initiator: " + "Arno Willig <akw@thinkwiki.org>" //$NON-NLS-1$
				+ "\n\n"
				+ "Active Developers:\n"
				+ "Robert Palmer <robotniko@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Brian Jensen <capncrunch@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "\n"													//$NON-NLS-1$
				+ "Former Developers:\n" 								//$NON-NLS-1$
				+ "Arno Willig <akw@thinkwiki.org>\n"					//$NON-NLS-1$
				+ "Christian Klein <kleinch@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Benjamin Schmitt <little_ben@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Bastian Schaefer <baefer@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Marc Waldenberger <MarcWaldenberger@gmx.net>\n"		//$NON-NLS-1$
				+ "\n\n"												//$NON-NLS-1$
				+ "This tool is developed and released under\n" 		//$NON-NLS-1$
				+ "the terms of the GNU General Public License\n",		//$NON-NLS-1$
				"About", JOptionPane.INFORMATION_MESSAGE); 	//$NON-NLS-1$
	}

	/**
	 * Listener for window events
	 */
	protected void processWindowEvent(WindowEvent e) {

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (JFritzUtils.parseBoolean(Main.getProperty("option.minimize", //$NON-NLS-1$
					"false"))) { //$NON-NLS-1$
				setExtendedState(Frame.ICONIFIED);
				Debug.msg("PROCESS WINDOW EVENT: minimize statt close");
			} else {
				jFritz.maybeExit(0);
			}
		} else if (e.getID() == WindowEvent.WINDOW_ICONIFIED) {
			Debug.msg("PROCESS WINDOW EVENT: minimize");
			setExtendedState(Frame.ICONIFIED);
			if (Main.SYSTRAY_SUPPORT) {
				setVisible(false);
			}
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
//				UIManager.setLookAndFeel(info.getClassName());
				Main.setStateProperty("lookandfeel", info.getClassName()); //$NON-NLS-1$
				jFritz.refreshWindow();
			} catch (Exception e) {
				Debug.err("Unable to set UI " + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @deprecated
	 * Sets standard info into the status bar
	 *
	 */
	public void setStatus() {
		callerListPanel.updateStatusBar(false);
	}


	/**
	 * Sets text in the status bar
	 * @param status
	 */
	public void setStatus(String status) {
		if (status.equals("")) {
			mainStatusBar.setText("");
			mainStatusPanel.setVisible(false);
			callerListPanel.updateStatusBar(false);
			statusBar.refresh();
			setStatus();
		} else {
			mainStatusPanel.setVisible(true);
			mainStatusBar.setText(status);
			statusBar.refresh();
			if (progressbar != null) {
				progressbar.setString(status);
			}
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
					&& (Integer.parseInt(Main.getProperty(
							"option.callMonitorType", "0")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$
		}
		menu.setEnabled(!busy);
		if ( progressbar != null )
		{
			progressbar.setIndeterminate(busy);
		}
		if (busy) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/**
	 * Used to set the progressbar as busy
	 * function does not block any other functions
	 *
	 * @param busy
	 */
	public void setLookupBusy(boolean busy){
		if ( progressbar != null )
		{
			progressbar.setIndeterminate(busy);
		}
	}

	/**
	 * Action Listener for menu and toolbar
	 */
	public void actionPerformed(ActionEvent e) {
		Debug.msg("Action " + e.getActionCommand()); //$NON-NLS-1$
		if (e.getActionCommand().equals("exit")) { //$NON-NLS-1$
			jFritz.maybeExit(0);
		} else if (e.getActionCommand().equals("about")) {
			showAboutDialog();
		} else if (e.getActionCommand().equals("help")) { //$NON-NLS-1$
			BrowserLaunch.openURL(JFritz.DOCUMENTATION_URL);
		} else if (e.getActionCommand().equals("debug_window")) { //$NON-NLS-1$
			JFrame debug_frame = new JFrame();
			debug_frame.add(Debug.getPanel());
			debug_frame.setTitle(Main.getMessage("debug_window"));
			Debug.SetSaveButtonText(Main.getMessage("save"));
			Debug.SetClearButtonText(Main.getMessage("clear"));
			Debug.SetCloseButtonText(Main.getMessage("close"));
			Debug.setFrame(debug_frame);
			debug_frame.pack();
			debug_frame.setVisible(true);
		} else if (e.getActionCommand().equals("website")) { //$NON-NLS-1$
			BrowserLaunch.openURL(Main.PROGRAM_URL);
		} else if (e.getActionCommand().equals("export_csv")) {
			exportCallerListToCSV();
		} else if (e.getActionCommand().equals("update")) { //$NON-NLS-1$
			JFritzUpdate jfritzUpdate = new JFritzUpdate(true);
			Update update = new Update(jfritzUpdate.getPropertiesDirectory());
			update.loadSettings();
			update.setProgramVersion(Main.PROGRAM_VERSION);
			jfritzUpdate.downloadNewFiles(update);
			update.saveSettings();
		} else if (e.getActionCommand().equals("export_phonebook")) {
			exportPhoneBookToCSV();
		} else if (e.getActionCommand().equals("print_callerlist")) {
			printCallerList();
		} else if (e.getActionCommand().equals("import_outlook")) {
			importOutlook();
		} else if (e.getActionCommand().equals("config")) {
			showConfigDialog();
		} else if (e.getActionCommand().equals("callerlist")) {
			tabber.setSelectedComponent(callerListPanel);
		} else if (e.getActionCommand().equals("phonebook")) {
			activatePhoneBook();
		} else if (e.getActionCommand().equals("quickdial")) {
			tabber.setSelectedComponent(quickDialPanel);
		} else if (e.getActionCommand().equals("monitoring")) {
			tabber.setSelectedComponent(monitoringPanel);
		} else if (e.getActionCommand().equals("stats")) {
			showStatsDialog();
		} else if (e.getActionCommand().equals("fetchList")) {
			fetchList();
		} else if (e.getActionCommand().equals("delete_fritzbox_callerlist")) {
			deleteFritzBoxCallerList();
		} else if (e.getActionCommand().equals(
				"delete_duplicate_phonebook_entries")) {
			deleteDuplicatePhoneBookEntries();
		} else if (e.getActionCommand().equals("backup")) {
			backupToChoosenDirectory();
		} else if (e.getActionCommand().equals("fetchTask")) {
			fetchTask(((JToggleButton) e.getSource()).isSelected());
		} else if (e.getActionCommand().equals("callDialog")) {
			CallDialog p = new CallDialog(new PhoneNumber("0", false));
			p.setVisible(true);
			p.dispose();
		} else if (e.getActionCommand().equals("callMonitor")) { //$NON-NLS-1$
			boolean active = ((JToggleButton) e.getSource()).isSelected();
			if (active) {
				Debug.msg("Start callMonitor"); //$NON-NLS-1$
				callMonitorStarted = true;
				jFritz.startChosenCallMonitor(true);
			} else {
				Debug.msg("Stop callMonitor"); //$NON-NLS-1$
				callMonitorStarted = false;
				JFritz.stopCallMonitor();
			}

		} else if (e.getActionCommand().equals("reverselookup")) {
			//reverseLookup();
			Object o = e.getSource();
			if (lookupButton.isSelected() || (o instanceof JMenuItem  && !lookupButton.isSelected())) {
				if(Main.getProperty("option.clientTelephoneBook").equals("true") &&
						NetworkStateMonitor.isConnectedToServer()){
					//if connected to server make server to the lookup
					Debug.msg("requesting reverse lookup from server");
					NetworkStateMonitor.requestLookupFromServer();
					lookupButton.setSelected(false);
				}else{
					Debug.msg("Start reverselookup"); //$NON-NLS-1$
					JFritz.getCallerList().reverseLookup(true, false);
				}
			} else {
				Debug.msg("Stopping reverse lookup"); //$NON-NLS-1$
				JFritz.getCallerList().stopLookup();
			}

		} else if (e.getActionCommand().equals("F5")) {
			fetchList();
		} else if (e.getActionCommand().equals("import_callerlist_csv")) {
			importCallerlistCSV();
		} else if (e.getActionCommand().equals("phonebook_import")) {
			phoneBookPanel.importFromXML();
		} else if (e.getActionCommand().equals(
				"import_contacts_thunderbird_csv")) {
			importContactsThunderbirdCSV();
		} else if (e.getActionCommand().equals("showhide")) {
			hideShowJFritz();
		} else if (e.getActionCommand().equals("configwizard")) {
			showConfigWizard();
		} else if(e.getActionCommand().equals("network")){

			if(Main.getProperty("network.type", "0").equals("2")){
				if(networkButton.isSelected())
					NetworkStateMonitor.startClient();
				else
					NetworkStateMonitor.stopClient();

			}else if(Main.getProperty("network.type", "0").equals("1")){
				if(networkButton.isSelected())
					NetworkStateMonitor.startServer();
				else
					NetworkStateMonitor.stopServer();
			}
		}else {
			Debug.err("Unimplemented action: " + e.getActionCommand()); //$NON-NLS-1$
		}
	}

	public void hideShowJFritz() {
		if (isVisible()) {
			Debug.msg("Hide JFritz-Window"); //$NON-NLS-1$
			Debug.msg(Main.getStateProperty("window.property.old",
					Integer.toString(Frame.NORMAL)));
			setExtendedState(JFrame.ICONIFIED);
		} else while ( !isVisible() ){
			Debug.msg("Show JFritz-Window"); //$NON-NLS-1$
			int windowState = 0;
			windowState = Integer.parseInt(Main.getStateProperty("window.property.old", Integer.toString(Frame.NORMAL)));

			Debug.msg(Integer.toString(windowState));

			setExtendedState(windowState);
			setVisible(true);

			toFront();
			repaint();
		}
	}


	/**
	 * @author Brian Jensen This creates and then display the config wizard
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 *
	 */
	public void showConfigWizard() {
		ConfigWizard wizard = new ConfigWizard(this);
		try {
			setConnectedStatus();
			wizard.showWizard();
		} catch (WrongPasswordException e) {
			setDisconnectedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFirmwareException e) {
			setDisconnectedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			setDisconnectedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Exports caller list as CSV
	 */
	public void exportCallerListToCSV() {
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(Main.getMessage("export_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Main.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			Main.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, Main.getMessage(
						"overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						Main.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					JFritz.getCallerList().saveToCSVFile(
							file.getAbsolutePath(), false);
				}
			} else {
				JFritz.getCallerList().saveToCSVFile(file.getAbsolutePath(),
						false);
			}
		}
	}

	/**
	 * Exports caller list as XML
	 */
	public void exportCallerListToXML() {
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportXMLpath", null)); //$NON-NLS-1$
		fc
				.setDialogTitle(Main
						.getMessage("dialog_title_export_callerlist_xml")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Main.getMessage("xml_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			Main.setProperty("options.exportXMLpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, Main.getMessage(
						"overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						Main.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					JFritz.getCallerList().saveToXMLFile(
							file.getAbsolutePath(), false);
				}
			} else {
				JFritz.getCallerList().saveToXMLFile(file.getAbsolutePath(),
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
		// FIXME selbst wenn die callerlist aktiviert ist können im
		// phonebook einträge ausgewählt sein, dann werden nur diese
		// exportiert, das kann für verwirrung sorgen.
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportCSVpathOfPhoneBook", null)); //$NON-NLS-1$
		fc.setDialogTitle(Main.getMessage("export_csv_phonebook")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setSelectedFile(new File(JFritz.PHONEBOOK_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Main.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			Main.setProperty("options.exportCSVpathOfPhoneBook", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(this, Main.getMessage(
						"overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$, //$NON-NLS-2$
						Main.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					JFritz.getPhonebook().saveToCSVFile(
							file.getAbsolutePath(),
							phoneBookPanel.getPhoneBookTable()
									.getSelectedRows(), ';');
				}
			} else {
				JFritz.getPhonebook().saveToCSVFile(file.getAbsolutePath(),
						phoneBookPanel.getPhoneBookTable().getSelectedRows(),
						';');
			}
		}
	}

	//
	public void printCallerList() {
		PrintCallerList printCallerList = new PrintCallerList();
		printCallerList.print();
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
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

	public void interrupt()
	{
		Thread.currentThread().interrupt();
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
		Debug.msg("Rectangle: "+rect.toString());
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

	public void switchMonitorButton() {
		if (!monitorButton.isEnabled()) {
			monitorButton.setEnabled(true);
		}
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

	public void setCallMonitorButtonPushed(boolean isPushed) {
		Main.setProperty("option.callmonitorStarted", Boolean
				.toString(isPushed));
		if (monitorButton != null)
			monitorButton.setSelected(isPushed);

		/**
		 * switch (option) { case JFritz.CALLMONITOR_START: {
		 * monitorButton.setSelected(false); break; } case
		 * JFritz.CALLMONITOR_STOP: { monitorButton.setSelected(true); } }
		 */
	}

	private void importOutlook() {
		Debug.msg("Starte Import von Outlook"); //$NON-NLS-1$
		Thread thread = new Thread(new ImportOutlookContactsDialog(this));
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
//		thread.interrupt();
		thread = null;
	}

	/**
	 * Deletes the caller list in Fritz!Box after having actualized it with the
	 * JFritz-CallerList. Method uses JFritzWindow.fetchList(true) to delete the
	 * caller list.
	 *
	 * @author Benjamin Schmitt
	 */
	public void deleteFritzBoxCallerList() {
		// options-object needed to set focus to no-button
		Object[] options = { Main.getMessage("yes"), //$NON-NLS-1$
				Main.getMessage("no") }; //$NON-NLS-1$
		int answer = JOptionPane.showOptionDialog(this, Main
				.getMessage("delete_fritzbox_callerlist_confirm_msg"), //$NON-NLS-1$
				Main.getMessage("delete_fritzbox_callerlist"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[1]);

		if (answer == JOptionPane.YES_OPTION) {
			fetchList(true); // param true indicates that FritzBox-CallerList
			// is to be deleted
		}
	}

	/**
	 * Removes redundant entries from the phonebook.
	 *
	 * @see de.moonflower.jfritz.phonebook.PhoneBook#deleteDuplicateEntries()
	 */
	private void deleteDuplicatePhoneBookEntries() {
		// TODO:Set focus to Cancel-Button
		int answer = JOptionPane
				.showConfirmDialog(
						this,
						Main
								.getMessage("delete_duplicate_phonebook_entries_confirm_msg"), Main //$NON-NLS-1$
								.getMessage("delete_duplicate_phonebook_entries"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION);

		if (answer == JOptionPane.YES_OPTION) {
			int removedEntries = JFritz.getPhonebook().deleteDuplicateEntries();
			JOptionPane.showMessageDialog(this, Main.getMessage(
					"delete_duplicate_phonebook_entries_inform_msg") //$NON-NLS-1$
					.replaceAll("%N", Integer.toString(removedEntries)), Main //$NON-NLS-1$
					.getMessage("delete_duplicate_phonebook_entries"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Creates a backup to a user selected directory
	 *
	 * @author Bastian Schaefer
	 */
	public void backupToChoosenDirectory() {
		CopyFile backup = new CopyFile();
		try {
			String directory = new DirectoryChooser().getDirectory(this)
					.toString();
			backup.copy(Main.SAVE_DIR, "xml", directory); //$NON-NLS-1$,  //$NON-NLS-2$
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
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(Main.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setSelectedFile(new File(JFritz.CALLS_CSV_FILE));
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Main.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			path = path.substring(0, path.length()
					- fc.getSelectedFile().getName().length());
			Main.setProperty("options.exportCSVpath", path); //$NON-NLS-1$
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane
						.showMessageDialog(
								this,
								Main.getMessage("file_not_found"), //$NON-NLS-1$
								Main.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

			} else {

				try {
					FileReader fr = new FileReader(file.getAbsolutePath());
					BufferedReader br = new BufferedReader(fr);
					JFritz.getCallerList().importFromCSVFile(br);
					br.close();

					if (Main.getProperty("option.lookupAfterFetch", "false")
							.equals("true")) {
						JFritz.getCallerList().reverseLookup(false, false);
					}

				} catch (FileNotFoundException e) {
					Debug.err("File not found!");
				} catch (IOException e) {
					Debug.err("IO Excetion reading file!");
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
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportCSVpath", null)); //$NON-NLS-1$
		fc.setDialogTitle(Main.getMessage("import_contacts_thunderbird_csv")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".csv"); //$NON-NLS-1$
			}

			public String getDescription() {
				return Main.getMessage("csv_files"); //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// FIXME
			// String path = fc.getSelectedFile().getPath();
			// path = path.substring(0, path.length()
			// - fc.getSelectedFile().getName().length());
			// options.import_contacts_thunderbird_CSVpath ???
			// Main.setProperty("options.exportCSVpath", path);
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane
						.showMessageDialog(
								this,
								Main.getMessage("file_not_found"), //$NON-NLS-1$
								Main.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			} else {
				String msg = JFritz.getPhonebook()
						.importFromThunderbirdCSVfile(file.getAbsolutePath());
				JFritz.infoMsg(msg);

				if (Main.getProperty("option.lookupAfterFetch", "false") //$NON-NLS-1$,  //$NON-NLS-2$
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
		jFritz.createNewWindow(locale);
		// current window will be destroyed and a new one created

		jFritz.refreshTrayMenu();
	}

	/**
	 * @author Bastian Schaefer
	 *
	 * The following 3 methods (getMaximizedBounds(), setMaximizedBounds() and
	 * setExtendedState()) are a workaround for the ensuing described bug:
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
	 * ---------- BEGIN SOURCE ---------- import javax.swing.*; public class
	 * MaxFrame extends JFrame { public static void main(String[] args) {
	 * JFrame.setDefaultLookAndFeelDecorated(true);
	 *
	 * JFrame f = new JFrame();
	 *
	 * f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.pack();
	 * f.setBounds(100, 100, 100, 100); f.setVisible(true); } } ---------- END
	 * SOURCE ----------
	 */

	public Rectangle getMaximizedBounds() {
		return (maxBounds);
	}

	public synchronized void setMaximizedBounds(Rectangle maxBounds) {
		this.maxBounds = maxBounds;
		super.setMaximizedBounds(maxBounds);
	}

	public synchronized void setExtendedState(int state) {
		if ((maxBounds == null)
				&& ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)) {
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

	public JProgressBar getProgressbar() {
		return progressbar;
	}

	public void prepareShutdown() {
		Debug.msg("prepareShutdown in JFritzWindow.java");
		if ( timer != null )
			timer.cancel();

		if(MonitoringPanel.timer != null)
			MonitoringPanel.timer.cancel();

		// TODO: möglicherweise speichern der Einstellungen für
		// phonebookPanel
		// quickDialPanel
		// monitoringPanel
		Debug.msg("prepareShutdown in JFritzWindow.java done");
	}

	public void selectLookupButton(boolean select){
		lookupButton.setSelected(select);
	}

	public void clientStateChanged(){
		networkButton.setEnabled(true);
		if(NetworkStateMonitor.isConnectedToServer()){
			networkButton.setSelected(true);
			networkButton.setToolTipText(Main.getMessage("client_is_connected"));

			//also activate the call monitor if one is wished
			if(Main.getProperty("option.clientCallMonitor", "false").equals("true")){
				callMonitorStarted = true;
				this.monitorButton.setSelected(true);
				this.monitorButton.setEnabled(false);
			}
		}else{
			networkButton.setSelected(false);
			networkButton.setToolTipText(Main.getMessage("connect_to_server"));

			//also deactivate the call monitor if one was active
			if(Main.getProperty("option.clientCallMonitor", "false").equals("true")){

				callMonitorStarted = false;
				this.monitorButton.setEnabled(true);
				this.monitorButton.setSelected(false);
			}


		}
	}

	public void serverStateChanged(){
		networkButton.setEnabled(true);
		if(NetworkStateMonitor.isListening()){
			networkButton.setSelected(true);
			networkButton.setToolTipText(Main.getMessage("server_is_listening"));
		}else{
			networkButton.setSelected(false);
			networkButton.setToolTipText(Main.getMessage("start_listening_clients"));
		}
	}

	/**
	 * This function is called by the network code
	 * when a client requests a reverse lookup
	 *
	 */
	public void doLookupButtonClick(){
		if(!lookupButton.isSelected())
			lookupButton.doClick();
	}

	public void doFetchButtonClick(){
		if(fetchButton.isEnabled())
			fetchButton.doClick();
	}

	/**
	 * This function sets the icon for the network button
	 * and enables or disables the toggle button.
	 * function called after saving the settings in the dialog
	 *
	 *
	 */
	public void setNetworkButton(){

		String networkType = Main.getProperty("network.type", "0");

		if(networkType.equals("1")){
			networkButton.setIcon(getImage("server.png"));
			serverStateChanged();
		}else if(networkType.equals("2")){
			networkButton.setIcon(getImage("client.png"));
			clientStateChanged();
		}else{
			networkButton.setIcon(getImage("no_network.png"));
			networkButton.setEnabled(false);
			networkButton.setToolTipText("");
		}
	}

	public StatusBar getStatusBar()
	{
		return statusBar;
	}

	public boolean isCallMonitorStarted()
	{
		return callMonitorStarted;
	}
}