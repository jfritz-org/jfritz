/**
 */

package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Container;
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
import java.awt.event.WindowListener;
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

import jd.nutils.OSDetector;

import de.moonflower.jfritz.autoupdate.JFritzUpdate;
import de.moonflower.jfritz.autoupdate.Update;
import de.moonflower.jfritz.box.BoxStatusListener;
import de.moonflower.jfritz.callerlist.CallDialog;
import de.moonflower.jfritz.callerlist.CallerListPanel;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.callerlist.FetchListTimer;
import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;

import de.moonflower.jfritz.dialogs.config.ConfigDialog;
import de.moonflower.jfritz.dialogs.configwizard.ConfigWizard;
import de.moonflower.jfritz.dialogs.quickdial.QuickDialPanel;
import de.moonflower.jfritz.dialogs.simple.CallMessageDlg;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.monitoring.MonitoringPanel;
import de.moonflower.jfritz.network.NetworkStateListener;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.phonebook.PhoneBookPanel;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
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
		ItemListener, NetworkStateListener, CallMonitorStatusListener,
		BoxStatusListener, WindowListener {

	private static final long serialVersionUID = 7856291642743441767L;

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

	private long lastDeIconifiedEvent = System.currentTimeMillis() - 1000;

	public final String WINDOW_PROPERTIES_FILE = "jfritz.window.properties.xml"; //$NON-NLS-1$

	private JMenuItem googleItem;

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		super();
		this.jFritz = jfritz;
		Debug.info("Create JFritz-GUI"); //$NON-NLS-1$
		maxBounds = null;
		createGUI();
    	CallMessageDlg callMsgDialog = new CallMessageDlg();
    	callMsgDialog.showIncomingCall(null, "", "", null);
    	callMsgDialog.close();
    	callMsgDialog.dispose();
		thisWindow = this;
		this.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
				Debug.debug("Window hidden");
			}

			public void componentMoved(ComponentEvent arg0) {
//				Debug.debug("Window moved");
				Main.setStateProperty("position.left", Integer.toString(getLocation().x)); //$NON-NLS-1$
				Main.setStateProperty("position.top", Integer.toString(getLocation().y));//$NON-NLS-1$
				Main.setStateProperty("position.width", Integer.toString(thisWindow.getWidth()));//$NON-NLS-1$
				Main.setStateProperty("position.height", Integer.toString(thisWindow.getHeight()));//$NON-NLS-1$
			}

			public void componentResized(ComponentEvent arg0) {
				if (getExtendedState() != Frame.MAXIMIZED_BOTH)
				{
//					Debug.debug("Window resized");
					Main.setStateProperty("position.left", Integer.toString(getLocation().x)); //$NON-NLS-1$
					Main.setStateProperty("position.top", Integer.toString(getLocation().y));//$NON-NLS-1$
					Main.setStateProperty("position.width", Integer.toString(thisWindow.getWidth()));//$NON-NLS-1$
					Main.setStateProperty("position.height", Integer.toString(thisWindow.getHeight()));//$NON-NLS-1$
				}
			}

			public void componentShown(ComponentEvent arg0) {
				Debug.debug("Window shown");
			}
		});
		addWindowStateListener(new WindowStateListener() {

			public void windowStateChanged(WindowEvent arg0) {
				Main.setStateProperty("window.state.old", Main.getStateProperty("window.state"));
				Main.setStateProperty("window.state", Integer.toString(getExtendedState()));
				Debug.debug("Window state changed: " + Main.getStateProperty("window.state.old") + " -> " + Main.getStateProperty("window.state"));
			}

		});
	}

	public void checkStartOptions() {
		Debug.debug("CHECKSTARTOPTIONS: ");
		if (!Main.getProperty("option.startMinimized") //$NON-NLS-1$,  //$NON-NLS-2$,
				.equals("true")) { //$NON-NLS-1$
			setVisible(true);
			Debug.debug("CHECKSTARTOPTIONS: don't start minimized");
		} else {
			if (!Main.systraySupport) {
				setVisible(true);
			}
			if ( getExtendedState() != Frame.ICONIFIED )
			{
				setExtendedState(Frame.ICONIFIED);
			}
		}
	}

	public void checkOptions() {
		if (!JFritz.isShutdownInvoked())
		{
			if (Main.getProperty("option.fetchAfterStart") //$NON-NLS-1$,  //$NON-NLS-2$
					.equals("true")) { //$NON-NLS-1$
				fetchButton.doClick();
			}
			if (Main.getProperty("option.autostartcallmonitor").equals( //$NON-NLS-1$,  //$NON-NLS-2$
					"true")) { //$NON-NLS-1$
				JFritz.getBoxCommunication().startCallMonitor();
			}
			if (Main.getProperty("option.timerAfterStart") //$NON-NLS-1$,  //$NON-NLS-2$
					.equals("true")) { //$NON-NLS-1$
				taskButton.doClick();
			}
		}
	}

	private void createGUI() {
		setTitle(Main.PROGRAM_NAME);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		statusBar = createStatusBar2();

		addKeyListener(this, KeyEvent.VK_F5, "fetchList", 0); //$NON-NLS-1$

		this.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								getClass()
										.getResource(
												"/de/moonflower/jfritz/resources/images/tray16.png"))); //$NON-NLS-1$

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
					googleItem.removeActionListener(callerListPanel);
					googleItem.removeActionListener(phoneBookPanel);
					googleItem.addActionListener(callerListPanel);
					callerListPanel.adaptGoogleLink();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("phonebook"))) { //$NON-NLS-1$
					phoneBookPanel.setStatus();
					googleItem.removeActionListener(callerListPanel);
					googleItem.removeActionListener(phoneBookPanel);
					googleItem.addActionListener(phoneBookPanel);
					phoneBookPanel.adaptGoogleLink();
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("quickdials"))) { //$NON-NLS-1$
					quickDialPanel.setStatus();
					googleItem.setEnabled(false);
				} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
						Main.getMessage("monitoring"))) {
					monitoringPanel.setStatus();
					googleItem.setEnabled(false);
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
				"position.left")); //$NON-NLS-1$,  //$NON-NLS-2$
		int y = Integer.parseInt(Main.getStateProperty(
				"position.top")); //$NON-NLS-1$,  //$NON-NLS-2$
		int w = Integer.parseInt(Main.getStateProperty(
				"position.width")); //$NON-NLS-1$,  //$NON-NLS-2$
		int h = Integer.parseInt(Main.getStateProperty(
				"position.height")); //$NON-NLS-1$,  //$NON-NLS-2$

		int windowState = Frame.NORMAL;

		Debug.debug("CREATE GUI: ");
		if ((!Main.getProperty("option.startMinimized").equals("true")) &&
			(Frame.ICONIFIED == Integer.parseInt(Main.getStateProperty("window.state"))))
		{ // Old state was iconified and we don't want to startup iconified
		  // Set previous old state to prevent bug in showing menu bar
			windowState = Integer.parseInt(Main.getStateProperty("window.state.old"));
			Debug.debug("CREATE GUI: restore old window state " + Integer.toString(windowState));
		} else
		{
			windowState = Integer.parseInt(Main.getStateProperty("window.state"));
			Debug.debug("CREATE GUI: restore window state " + Integer.toString(windowState));
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
		setBoxDisconnected("");
		iconStatusPanel.add(connectButton);
		iconStatusPanel.add(callMonitorConnectButton);
		statusBar.registerStatusIcon(iconStatusPanel);

		return statusBar;
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
		calldialogButton.setToolTipText(Main.getMessage("dial_assist"));
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
		button.setIcon(getImage("DeleteList.png")); //$NON-NLS-1$
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
		button.setActionCommand("help"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("help.png")); //$NON-NLS-1$
		button.setToolTipText(Main.getMessage("help_menu")); //$NON-NLS-1$
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

		String networkType = Main.getProperty("network.type");

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
		if(Main.getProperty("network.type").equals("0")){
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

		googleItem = new JMenuItem(Main.getMessage("show_on_google_maps"));
		googleItem.setActionCommand("google");
		googleItem.addActionListener(callerListPanel);
		googleItem.setEnabled(false);
		googleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
		jfritzMenu.add(googleItem);

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
			int interval = Integer.parseInt(Main.getProperty("fetch.timer")) * 60000; //$NON-NLS-1$
			timerTask = new TimerTask() {

				public void run() {
					if (!JFritz.isShutdownInvoked())
					{
						Debug.info("Running FetchListTask after Timer ..."); //$NON-NLS-1$
						fetchList(false);
					} else {
						this.cancel();
					}
				}
			};
			timer = new FetchListTimer("FetchList-Timer", true);
			timer.schedule(timerTask, interval, interval); //$NON-NLS-1$
			Debug.always("Timer enabled"); //$NON-NLS-1$
		} else {
			timer.cancel();
			Debug.always("Timer disabled"); //$NON-NLS-1$
		}
	}

	private void restartFetchListTimer()
	{
		if (timer != null)
		{
			if (timer.getState() == FetchListTimer.STATE_SCHEDULED)
			{
				// restart timer
				timer.cancel();
				timer = null;
				int interval = Integer.parseInt(Main.getProperty("fetch.timer")) * 60000; //$NON-NLS-1$
				timerTask.cancel();
				timerTask = new TimerTask() {

					public void run() {
						Debug.info("Running FetchListTask after timer ..."); //$NON-NLS-1$
						fetchList(false);
					}
				};
				timer = new FetchListTimer("FetchList-Timer2", true);
				timer.schedule(timerTask, interval, interval); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Fetches list from box
	 */
	public void fetchList(final boolean deleteFritzBoxCallerList) {
		Debug.info("Reset timer ...");
		restartFetchListTimer();
		Debug.info("Fetching list ...");
		//only send request to the server if we are connected
		if(Main.getProperty("option.clientCallList").equals("true")
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
			isretrieving = true;
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean isdone = false;
					while (!isdone) {
						setBusy(true);
						setStatus(Main.getMessage("fetchdata")); //$NON-NLS-1$
						setBoxConnected("");
						JFritz.getBoxCommunication().getCallerList();
						isdone = true;
					}
					return null;
				}

				public void finished() {
					setBusy(false);
					JFritz.getCallerList().fireTableStructureChanged();
					isretrieving = false;
					if (!JFritz.isShutdownInvoked())
					{
						if ((JFritz.getBoxCommunication().getLastFetchedCallsCount()>0) &&
								((Main.getProperty("option.deleteAfterFetch").equals("true")))
								|| (deleteFritzBoxCallerList)) {
							JFritz.getBoxCommunication().clearCallerList();
						}
					}

					if (!JFritz.isShutdownInvoked())
					{
						if (Main.getProperty("option.lookupAfterFetch") //$NON-NLS-1$,  //$NON-NLS-2$
								.equals("true")) { //$NON-NLS-1$

							if(Main.getProperty("option.clientTelephoneBook").equals("true"))
								lookupButton.doClick();
							else
								JFritz.getCallerList().reverseLookup(false, false);
						}
						setStatus("");
	//					interrupt();
					}
				}
			};
			worker.start();
		}
	}

	/**
	 * Shows the configuration dialog
	 */
	public void showConfigDialog() {
	    Container c = this.getContentPane(); // get the window's content pane
	    c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Vector<CallMonitorStatusListener> stateListener = new Vector<CallMonitorStatusListener>();
		stateListener.add(this);
		configDialog = new ConfigDialog(this, stateListener);
		configDialog.setLocationRelativeTo(this);
		if (configDialog.showDialog()) {
			this.setStatus(Main.getMessage("save_settings"));
			configDialog.storeValues();

			Main.saveConfigProperties();
			setBoxConnected("");
			monitorButton.setEnabled((Integer.parseInt(Main.getProperty(
					"option.callMonitorType")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$

			callerListPanel.reorderColumns();
		}
		configDialog.dispose();
		this.setStatus("");
	    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Shows the about dialog
	 */
	public void showAboutDialog() {
		JOptionPane.showMessageDialog(this, Main.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ Main.PROGRAM_VERSION + "\n" //$NON-NLS-1$
				+ JFritzUtils.getVersionFromCVSTag(Main.CVS_TAG) + "\n" //$NON-NLS-1$
				+ "(c) 2005-2009 by " + Main.JFRITZ_PROJECT + "\n" //$NON-NLS-1$,  //$NON-NLS-2$
				+ Main.PROGRAM_URL + "\n\n" 							//$NON-NLS-1$
				+ "Project-Admin: " + Main.PROJECT_ADMIN + "\n"		//$NON-NLS-1$
				+ "Project-Initiator: " + "Arno Willig <akw@thinkwiki.org>" //$NON-NLS-1$
				+ "\n\n"
				+ "Active Developers:\n"
				+ "Robert Palmer <robotniko@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Brian Jensen <capncrunch@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Rainer Ullrich <jfritz@rainerullrich.de>\n" 			//$NON-NLS-1$
				+ "\n"													//$NON-NLS-1$
				+ "Former Developers:\n" 								//$NON-NLS-1$
				+ "Arno Willig <akw@thinkwiki.org>\n"					//$NON-NLS-1$
				+ "Christian Klein <kleinch@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Benjamin Schmitt <little_ben@users.sourceforge.net>\n" //$NON-NLS-1$
				+ "Bastian Schaefer <baefer@users.sourceforge.net>\n" 	//$NON-NLS-1$
				+ "Marc Waldenberger <MarcWaldenberger@gmx.net>\n"		//$NON-NLS-1$
				+ "Simeon Faensen (Klingeling-Idee)\n"					//$NON-NLS-1$
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
			if (JFritzUtils.parseBoolean(Main.getProperty("option.minimize"))) //$NON-NLS-1$
			{
				Debug.debug("PROCESS WINDOW EVENT: minimize statt close");
				setExtendedState(Frame.ICONIFIED);
			} else {
				jFritz.maybeExit(0);
			}
		} else if (e.getID() == WindowEvent.WINDOW_ICONIFIED) {
			Debug.debug("PROCESS WINDOW EVENT: minimize");
			hideShowJFritz();
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
				JFritz.getBoxCommunication().stopCallMonitor();
				jFritz.refreshWindow();
			} catch (Exception e) {
				Debug.error("Unable to set UI " + e.getMessage()); //$NON-NLS-1$
			}
		}
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
			callerListPanel.updateStatusBar(false);
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
							"option.callMonitorType")) > 0)); //$NON-NLS-1$,  //$NON-NLS-2$
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
		Debug.debug("Action " + e.getActionCommand()); //$NON-NLS-1$
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
			Debug.setSaveButtonText(Main.getMessage("save"));
			Debug.setRefreshButtonText(Main.getMessage("refresh"));
			Debug.setCloseButtonText(Main.getMessage("close"));
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
		} else if (e.getActionCommand().equals("fetchList")) {
			fetchList(false);
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
			if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
					Main.getMessage("callerlist"))) { //$NON-NLS-1$
				PhoneNumber number = null;
				if (this.getCallerTable().getSelectedRowCount() == 1)
				{
					int index = this.getCallerTable().getSelectedRow();
					Call call = this.getCallerListPanel().getCallerList().getFilteredCallVector().get(index);
					number = call.getPhoneNumber();
				}
				else {
					number = new PhoneNumber("0", false, false);
				}
				CallDialog p = new CallDialog(number);
				p.setVisible(true);
				p.dispose();
			} else if (tabber.getTitleAt(tabber.getSelectedIndex()).equals(
					Main.getMessage("phonebook"))) { //$NON-NLS-1$
				CallDialog dialog = null;
				if (this.getPhoneBookPanel().getPhoneBookTable().getSelectedRowCount() == 1)
				{
					int index = this.getPhoneBookPanel().getPhoneBookTable().getSelectedRow();
					Person person = JFritz.getPhonebook().getFilteredPersons().get(index);
					dialog = new CallDialog(person.getNumbers(), person.getStandardTelephoneNumber());
				} else {
					dialog = new CallDialog(new PhoneNumber("0", false, false));
				}
				dialog.setVisible(true);
				dialog.dispose();
			}
		} else if (e.getActionCommand().equals("callMonitor")) { //$NON-NLS-1$
			boolean active = ((JToggleButton) e.getSource()).isSelected();
			if (active) {
				Debug.info("Start callMonitor"); //$NON-NLS-1$
				JFritz.getBoxCommunication().startCallMonitor();
			} else {
				Debug.info("Stop callMonitor"); //$NON-NLS-1$
				JFritz.getBoxCommunication().stopCallMonitor();
			}

		} else if (e.getActionCommand().equals("reverselookup")) {
			//reverseLookup();
			Object o = e.getSource();
			if (lookupButton.isSelected() || (o instanceof JMenuItem  && !lookupButton.isSelected())) {
				if(Main.getProperty("option.clientTelephoneBook").equals("true") &&
						NetworkStateMonitor.isConnectedToServer()){
					//if connected to server make server to the lookup
					Debug.netMsg("requesting reverse lookup from server");
					NetworkStateMonitor.requestLookupFromServer();
					lookupButton.setSelected(false);
				}else{
					Debug.info("Start reverselookup"); //$NON-NLS-1$
					JFritz.getCallerList().reverseLookup(true, false);
				}
			} else {
				Debug.info("Stopping reverse lookup"); //$NON-NLS-1$
				JFritz.getCallerList().stopLookup();
			}
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

			if(Main.getProperty("network.type").equals("2")){
				if(networkButton.isSelected())
					NetworkStateMonitor.startClient();
				else
					NetworkStateMonitor.stopClient();

			}else if(Main.getProperty("network.type").equals("1")){
				if(networkButton.isSelected())
					NetworkStateMonitor.startServer();
				else
					NetworkStateMonitor.stopServer();
			}
		}else {
			Debug.warning("Unimplemented action: " + e.getActionCommand()); //$NON-NLS-1$
		}
	}

	public void hideShowJFritz() {
		hideShowJFritz(false);
	}

	public void hideShowJFritz(boolean saveState) {
		if (System.currentTimeMillis() > this.lastDeIconifiedEvent + 250) {
			this.lastDeIconifiedEvent  = System.currentTimeMillis();
			if (isVisible()) {
				Debug.debug("Hide JFritz-Window"); //$NON-NLS-1$
				if (Main.systraySupport)
				{
					Debug.debug("Setting to invisible!");
					this.setVisible(false);
				}
				if (saveState) {
					Main.setStateProperty("window.state.old", Main.getStateProperty("window.state"));
					Main.setStateProperty("window.state", Integer.toString(getExtendedState()));
					Debug.debug("Saving new state: " + Main.getStateProperty("window.state.old")
							+ " -> " + Main.getStateProperty("window.state"));
				}
			} else while ( !isVisible() ){
				Debug.debug("Show JFritz-Window"); //$NON-NLS-1$
				int windowState = 0;
				windowState = Integer.parseInt(Main.getStateProperty("window.state.old"));

				Debug.debug("Window state old: " + Integer.toString(windowState));
				Debug.debug("Windows state:    " + Main.getStateProperty("window.state"));

				if ((windowState != Frame.MAXIMIZED_BOTH) && (windowState != Frame.ICONIFIED))
				{
					windowState = Frame.NORMAL;
				}

				if (OSDetector.isGnome())
				{
					Debug.debug("Current state1: "
							+ Main.getStateProperty("window.state.old")
							+ "/"+Main.getStateProperty("window.state"));
					Debug.debug("Maximize gnome style");
		            setExtendedState(windowState);
		            setVisible(true);
		            setExtendedState(Frame.ICONIFIED);
		            setVisible(false);
		            setExtendedState(windowState);
		            setVisible(true);
		            String tmp = Main.getStateProperty("window.state");
		            Main.setStateProperty("window.state", Main.getStateProperty("window.state.old"));
		            Main.setStateProperty("window.state.old", tmp);
					Debug.debug("Current state2: "
							+ Main.getStateProperty("window.state.old")
							+ "/"+Main.getStateProperty("window.state"));
				}
				else
				{
					// use this at windows and other systems
					Debug.debug("Maximize windows style");
					setVisible(true);
					setExtendedState(windowState);
				}

				// use this on any system
				toFront();
	            repaint();
			}
		}
	}


	/**
	 * @author Brian Jensen This creates and then display the config wizard
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 *
	 */
	public boolean showConfigWizard() {
		ConfigWizard wizard = new ConfigWizard(this);
		boolean wizardCanceled = false;
		try {
			wizardCanceled = wizard.showWizard();
			setBoxConnected("");
		} catch (WrongPasswordException e) {
			setBoxDisconnected("");
			e.printStackTrace();
			return true;
		} catch (InvalidFirmwareException e) {
			setBoxDisconnected("");
			e.printStackTrace();
			return true;
		} catch (IOException e) {
			setBoxDisconnected("");
			e.printStackTrace();
			return true;
		}
		return wizardCanceled;
	}

	/**
	 * Exports caller list as CSV
	 */
	public void exportCallerListToCSV() {
		JFileChooser fc = new JFileChooser(Main.getProperty(
				"options.exportCSVpath")); //$NON-NLS-1$
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
				"options.exportXMLpath")); //$NON-NLS-1$
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
				"options.exportCSVpathOfPhoneBook")); //$NON-NLS-1$
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

	private void importOutlook() {
		Debug.info("Starte Import von Outlook"); //$NON-NLS-1$
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
			Debug.debug("Fetching data before deleting list on box!");
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
			Debug.error("No directory choosen for backup!"); //$NON-NLS-1$
		}
	}

	/**
	 * Provides easy implementation of a KeyListener. Will add the KeyListener
	 * to the main Jframe and react without having the Focus.
	 */
	public void addKeyListener(ActionListener listener, int vkey, String listenerString, int mask) {

		this.getRootPane().registerKeyboardAction(listener, listenerString,
				keyStroke(vkey, mask), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	/**
	 * Provides easy creation of a KeyStroke object without a modifier and
	 * reaction onKeyReale
	 */
	private KeyStroke keyStroke(int vkey, int mask) {
		return KeyStroke.getKeyStroke(vkey, mask, false);
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
				"options.exportCSVpath")); //$NON-NLS-1$
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

					if (Main.getProperty("option.lookupAfterFetch")
							.equals("true")) {
						JFritz.getCallerList().reverseLookup(false, false);
					}

				} catch (FileNotFoundException e) {
					Debug.error("File not found!");
				} catch (IOException e) {
					Debug.error("IO Excetion reading file!");
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
				"options.exportCSVpath")); //$NON-NLS-1$
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

				if (Main.getProperty("option.lookupAfterFetch") //$NON-NLS-1$,  //$NON-NLS-2$
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
		JFritz.getBoxCommunication().stopCallMonitor();
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
		Debug.info("prepareShutdown in JFritzWindow.java");
		if ( timer != null )
			timer.cancel();

		monitoringPanel.prepareShutdown();

		// TODO: möglicherweise speichern der Einstellungen für
		// phonebookPanel
		// quickDialPanel
		// monitoringPanel
		Debug.info("prepareShutdown in JFritzWindow.java done");
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
			if(Main.getProperty("option.clientCallMonitor").equals("true")){
				this.monitorButton.setSelected(true);
				this.monitorButton.setEnabled(false);
			}
		}else{
			networkButton.setSelected(false);
			networkButton.setToolTipText(Main.getMessage("connect_to_server"));

			//also deactivate the call monitor if one was active
			if(Main.getProperty("option.clientCallMonitor").equals("true")){
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

		String networkType = Main.getProperty("network.type");

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
		return monitorButton.isSelected();
	}

	public void setConnectedStatus(String boxName) {
		//@TODO: if multiple boxes have callmonitors support it here
		callMonitorConnectButton.setIcon(callMonitorConnectIcon);
		callMonitorConnectButton.setToolTipText(Main.getMessage("connected_callmonitor"));
		callMonitorConnectButton.setVisible(true);
		if (statusBar != null)
		{
			statusBar.refresh();
		}

		if (monitorButton != null)
		{
			monitorButton.setSelected(true);
		}
	}

	public void setDisconnectedStatus(String boxName) {
		//@TODO: if multiple boxes have callmonitors support it here
		callMonitorConnectButton.setIcon(callMonitorDisconnectIcon);
		callMonitorConnectButton.setToolTipText(Main.getMessage("disconnected_callmonitor"));
		callMonitorConnectButton.setVisible(true);
		if (statusBar != null)
		{
			statusBar.refresh();
		}

		if (monitorButton != null)
		{
			monitorButton.setSelected(false);
		}
	}

	public void finished(Vector<Call> newCalls) {
		// TODO Auto-generated method stub

	}

	public void setMax(int max) {
		// TODO Auto-generated method stub

	}

	public void setMin(int min) {
		// TODO Auto-generated method stub

	}

	public void setProgress(int progress) {
		// TODO Auto-generated method stub

	}

	public void setBoxConnected(String boxName) {
		connectButton.setIcon(connectIcon);
		connectButton.setToolTipText(Main.getMessage("connected_fritz"));
		statusBar.refresh();
	}

	public void setBoxDisconnected(String boxName) {
		connectButton.setIcon(disconnectIcon);
		connectButton.setToolTipText(Main.getMessage("disconnected_fritz"));
		this.setDisconnectedStatus(""); // set call monitor to disconnected status
		statusBar.refresh();
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent e) {
		hideShowJFritz();
	}

	public void windowIconified(WindowEvent e) {
		hideShowJFritz();
	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void setGoogleItem(boolean status) {
		googleItem.setEnabled(status);
	}
}