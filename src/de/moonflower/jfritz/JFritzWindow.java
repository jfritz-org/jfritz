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
import java.net.URL;
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
import javax.swing.filechooser.FileFilter;

import org.jdesktop.jdic.desktop.Desktop;

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
import de.moonflower.jfritz.struct.VCardList;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;
import de.moonflower.jfritz.utils.SwingWorker;

/**
 * This is main window class of JFritz, which creates the GUI.
 *
 * @author akw
 */
public class JFritzWindow extends JFrame implements Runnable, ActionListener,
		ItemListener {

	JFritz jfritz;

	JFritzProperties properties, participants;

	Timer timer;

	JMenuBar menu;

	JToolBar mBar;

	private JButton fetchButton, lookupButton, configButton, vcardButton;

	JToggleButton taskButton;

	JProgressBar progressbar;

	boolean isretrieving = false;

	private JTabbedPane tabber;

	private CallerListPanel callerListPanel;

	private PhoneBookPanel phoneBookPanel;

	private QuickDialPanel quickDialPanel;

	/**
	 * Constructs JFritzWindow
	 *
	 * @param jfritz
	 */
	public JFritzWindow(JFritz jfritz) {
		this.jfritz = jfritz;
		this.properties = jfritz.getProperties();
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

		callerListPanel = new CallerListPanel(jfritz);
		phoneBookPanel = new PhoneBookPanel(jfritz);
		quickDialPanel = new QuickDialPanel(jfritz);

		tabber = new JTabbedPane(JTabbedPane.BOTTOM);
		tabber.addTab(jfritz.getMessages().getString("callerlist"),
				callerListPanel);
		tabber.addTab(jfritz.getMessages().getString("phonebook"),
				phoneBookPanel);
		tabber.addTab(jfritz.getMessages().getString("quickdials"),
				quickDialPanel);

		// Adding gui components
		setJMenuBar(createMenu());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createMainToolBar(), BorderLayout.NORTH);
		getContentPane().add(tabber, BorderLayout.CENTER);
		getContentPane().add(createStatusBar(), BorderLayout.SOUTH);

		jfritz.getCallerlist().fireTableStructureChanged();
	}

	/**
	 * Sets default Look'n'Feel
	 */
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
		mBar.add(button);

		button = new JButton();
		button.setActionCommand("quickdial");
		button.addActionListener(this);
		button.setIcon(getImage("quickdial.png"));
		button.setToolTipText(jfritz.getMessages().getString("quickdials"));
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
		return mBar;
	}

	/**
	 * Creates the menu bar
	 */
	public JMenuBar createMenu() {
		JMenu jfritzMenu = new JMenu(JFritz.PROGRAM_NAME);
		JMenu editMenu = new JMenu(jfritz.getMessages().getString("edit_menu"));
		JMenu optionsMenu = new JMenu(jfritz.getMessages().getString(
				"options_menu"));
		JMenu helpMenu = new JMenu(jfritz.getMessages().getString("help_menu"));
		JMenu lnfMenu = new JMenu(jfritz.getMessages().getString("lnf_menu"));
		JMenu exportMenu = new JMenu(jfritz.getMessages().getString(
				"export_menu"));
		JMenu viewMenu = new JMenu(jfritz.getMessages().getString("view_menu"));

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

		item = new JMenuItem(jfritz.getMessages().getString("view_menu"), 'l');
		item.setActionCommand("view");
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
			tabber.setSelectedComponent(callerListPanel);
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
			tabber.setSelectedComponent(callerListPanel);
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
							PhoneNumber number = call.getPhoneNumber();
							if (number != null && (call.getPerson() == null)) {
								setStatus(jfritz.getMessages().getString(
										"reverse_lookup_for")
										+ " " + number.getNumber() + " ...");
								Debug.msg("Reverse lookup for "
										+ number.getNumber());

								Person newPerson = ReverseLookup.lookup(number
										.getNumber());
								if (newPerson != null) {
									jfritz.getPhonebook().addEntry(newPerson);
									// jfritz.getCallerlist().setPerson(newPerson,i);
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
		boolean exit = true;

		if (properties.getProperty("option.confirmOnExit", "true") == "true")
			exit = JOptionPane.showConfirmDialog(this, jfritz.getMessages()
					.getString("really_quit"), JFritz.PROGRAM_NAME,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

		if (exit) {
			quickDialPanel.getDataModel().saveToXMLFile(JFritz.QUICKDIALS_FILE);

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
		if (fetchButton != null) {
			fetchButton.setEnabled(!busy);
			lookupButton.setEnabled(!busy);
			configButton.setEnabled(!busy);
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
		} else if (e.getActionCommand() == "export_csv")
			expportCSV();
		else if (e.getActionCommand() == "export_vcard")
			exportVCard();
		else if (e.getActionCommand() == "config")
			showConfigDialog();
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
		else if (e.getActionCommand() == "reverselookup")
			reverseLookup();
		else
			Debug.err("Unimplemented action: " + e.getActionCommand());

	}

	/**
	 * Exports caller list as CSV
	 */
	private void expportCSV() {
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
		int rows[] = callerListPanel.getCallerTable().getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			Person person = (Person) callerListPanel.getCallerTable()
					.getModel().getValueAt(rows[i], 3);
			if (person != null && person.getFullname() != "") {
				list.addVCard(person);
			}
		}
		if (list.getCount() > 0) {
			if (list.getCount() == 1) {
				fc.setSelectedFile(new File(list.getPerson(0)
						.getStandardTelephoneNumber()
						+ ".vcf"));
			} else if (list.getCount() > 1) {
				fc.setSelectedFile(new File("jfritz.vcf"));
			}
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				list.saveToFile(file);
			}
		} else {
			jfritz.errorMsg("Keine einzige sinnvolle Zeile selektiert!\n\n"
					+ "Bitte eine oder mehrere Zeilen auswählen,\n"
					+ "um die Daten als VCard zu exportieren!");
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
}