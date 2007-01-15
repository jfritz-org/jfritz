/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * JDialog for JFritz configuration.
 *
 * @author Arno Willig
 *
 */
public class ConfigDialog extends JDialog {

	public static boolean refreshWindow;

	private static final long serialVersionUID = 1;

	private JButton okButton, cancelButton;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
			notifyOnCallsButton,
			lookupAfterFetchButton,
			showCallByCallColumnButton,
			showCommentColumnButton, showPortColumnButton,
			fetchAfterStandby;

	private ConfigPanelPhone phonePanel;
	private ConfigPanelFritzBox fritzBoxPanel;
	private ConfigPanelMessage messagePanel;
	private ConfigPanelCallMonitor callMonitorPanel;
	private ConfigPanelLang languagePanel;
	private ConfigPanelOther otherPanel;

	private boolean pressed_OK = false;

    static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
	final String langID = FILESEP + "lang";										//$NON-NLS-1$

	public ConfigDialog(Frame parent) {
		super(parent, true);
		setTitle(Main.getMessage("config")); //$NON-NLS-1$

		phonePanel = new ConfigPanelPhone();
		fritzBoxPanel = new ConfigPanelFritzBox();
		messagePanel = new ConfigPanelMessage();
		callMonitorPanel = new ConfigPanelCallMonitor(this, true, fritzBoxPanel);
		languagePanel = new ConfigPanelLang();
		otherPanel = new ConfigPanelOther(fritzBoxPanel);

		drawDialog();
		setValues();
		if (parent != null) {
			setLocationRelativeTo(parent);
		}

	}

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * Sets properties to dialog components
	 */
	public void setValues() {
		fritzBoxPanel.loadSettings();
		phonePanel.loadSettings();
		messagePanel.loadSettings();
		callMonitorPanel.loadSettings();
		languagePanel.loadSettings();
		otherPanel.loadSettings();

		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.notifyOnCalls"))); //$NON-NLS-1$
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.fetchAfterStart"))); //$NON-NLS-1$
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.deleteAfterFetch"))); //$NON-NLS-1$

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.lookupAfterFetch", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCallByCallColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallByCallColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCommentColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCommentColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		showPortColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showPortColumn", "true"))); //$NON-NLS-1$,  //$NON-NLS-2$

		fetchAfterStandby.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.watchdog.fetchAfterStandby", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		//Buggy code
		/*if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				SSDPPacket p = (SSDPPacket) devices.get(i);
				if (p.getIP().getHostAddress().equals(address.getText())) {
					addressCombo.setSelectedIndex(i);
				}
			}
		}*/
	}

	/**
	 * Stores values in dialog components to programm properties
	 */
	public void storeValues() {
		fritzBoxPanel.saveSettings();
		phonePanel.saveSettings();
		messagePanel.saveSettings();
		callMonitorPanel.saveSettings();
		languagePanel.saveSettings();
		otherPanel.saveSettings();

		Main.setProperty("option.notifyOnCalls", Boolean //$NON-NLS-1$
				.toString(notifyOnCallsButton.isSelected()));
		Main.setProperty("option.fetchAfterStart", Boolean //$NON-NLS-1$
				.toString(fetchAfterStartButton.isSelected()));
		Main.setProperty("option.deleteAfterFetch", Boolean //$NON-NLS-1$
				.toString(deleteAfterFetchButton.isSelected()));

		Main.setProperty("option.lookupAfterFetch", Boolean //$NON-NLS-1$
				.toString(lookupAfterFetchButton.isSelected()));

		Main.setProperty("option.showCallByCallColumn", Boolean //$NON-NLS-1$
				.toString(showCallByCallColumnButton.isSelected()));

		Main.setProperty("option.showCommentColumn", Boolean //$NON-NLS-1$
				.toString(showCommentColumnButton.isSelected()));

		Main.setProperty("option.showPortColumn", Boolean //$NON-NLS-1$
				.toString(showPortColumnButton.isSelected()));

		Main.setProperty("option.watchdog.fetchAfterStandby", Boolean //$NON-NLS-1$
				.toString(fetchAfterStandby.isSelected()));

		JFritz.getFritzBox().setAddress(fritzBoxPanel.getAddress());
		JFritz.getFritzBox().setPassword(fritzBoxPanel.getPassword());
		JFritz.getFritzBox().setPort(fritzBoxPanel.getPort());
		JFritz.getFritzBox().detectFirmware();

		Debug.msg("Saved config"); //$NON-NLS-1$
		JFritz.getSIPProviderTableModel()
				.saveToXMLFile(Main.SAVE_DIR + JFritz.SIPPROVIDER_FILE);
		JFritz.getCallerList().saveToXMLFile(Main.SAVE_DIR+JFritz.CALLS_FILE, true);
		JFritz.getPhonebook().saveToXMLFile(Main.SAVE_DIR+JFritz.PHONEBOOK_FILE);
        Main.saveUpdateProperties();
	}

	protected JPanel createSipPane(ActionListener actionListener) {
		JPanel sippane = new JPanel();
		sippane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel sipButtonPane = new JPanel();
		final JTable siptable = new JTable(JFritz.getSIPProviderTableModel()) {
			private static final long serialVersionUID = 1;

			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(new Color(255, 255, 200));
				} else if (!isCellSelected(rowIndex, vColIndex)) {
					// If not shaded, match the table's background
					c.setBackground(getBackground());
				} else {
					c.setBackground(new Color(204, 204, 255));
				}
				return c;
			}
		};
		siptable.setRowHeight(24);
		siptable.setFocusable(false);
		siptable.setAutoCreateColumnsFromModel(false);
		siptable.setColumnSelectionAllowed(false);
		siptable.setCellSelectionEnabled(false);
		siptable.setRowSelectionAllowed(true);
		siptable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		siptable.getColumnModel().getColumn(0).setMinWidth(20);
		siptable.getColumnModel().getColumn(0).setMaxWidth(20);
		siptable.getColumnModel().getColumn(1).setMinWidth(40);
		siptable.getColumnModel().getColumn(1).setMaxWidth(40);
		siptable.setSize(200, 200);
		JButton b1 = new JButton(Main.getMessage("get_sip_provider_from_box")); //$NON-NLS-1$
		b1.setActionCommand("fetchSIP"); //$NON-NLS-1$
		b1.addActionListener(actionListener);
		JButton b2 = new JButton(Main.getMessage("save_sip_provider_on_box")); //$NON-NLS-1$
		b2.setEnabled(false);
		sipButtonPane.add(b1);
		sipButtonPane.add(b2);

		sippane.setLayout(new BorderLayout());
		sippane.add(sipButtonPane, BorderLayout.NORTH);
		sippane.add(new JScrollPane(siptable), BorderLayout.CENTER);
		return sippane;
	}

	protected JPanel createCallerListPane() {
		JPanel cPanel = new JPanel();

		cPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fetchAfterStartButton = new JCheckBox(Main.getMessage("fetch_after_start")); //$NON-NLS-1$
		cPanel.add(fetchAfterStartButton, c);

		c.gridy = 1;
		notifyOnCallsButton = new JCheckBox(Main.getMessage("notify_on_calls")); //$NON-NLS-1$
		cPanel.add(notifyOnCallsButton, c);

		c.gridy = 2;
		deleteAfterFetchButton = new JCheckBox(Main.getMessage("delete_after_fetch")); //$NON-NLS-1$
		cPanel.add(deleteAfterFetchButton, c);

		c.gridy = 3;
		lookupAfterFetchButton = new JCheckBox(Main.getMessage("lookup_after_fetch")); //$NON-NLS-1$
		cPanel.add(lookupAfterFetchButton, c);

		c.gridy = 4;
		showCallByCallColumnButton = new JCheckBox(Main.getMessage("show_callbyball_column")); //$NON-NLS-1$
		cPanel.add(showCallByCallColumnButton, c);

		c.gridy = 5;
		showCommentColumnButton = new JCheckBox(Main.getMessage("show_comment_column")); //$NON-NLS-1$
		cPanel.add(showCommentColumnButton, c);

		c.gridy = 6;
		showPortColumnButton = new JCheckBox(Main.getMessage("show_port_column")); //$NON-NLS-1$
		cPanel.add(showPortColumnButton, c);

		c.gridy = 7;
		fetchAfterStandby = new JCheckBox(Main.getMessage("fetch_after_standby")); //$NON-NLS-1$
		cPanel.add(fetchAfterStandby, c);

		return cPanel;
	}

	protected void drawDialog() {

		// Create JTabbedPane
		JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

		tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					ConfigDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					ConfigDialog.this.setVisible(false);
				}
			}
		});

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == okButton);
				if (source == okButton
						|| source == cancelButton) {
					ConfigDialog.this.setVisible(false);
				} else if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
					try {
						JFritz.getFritzBox().setAddress(fritzBoxPanel.getAddress());
						JFritz.getFritzBox().setPassword(fritzBoxPanel.getPassword());
						JFritz.getFritzBox().setPort(fritzBoxPanel.getPort());
						JFritz.getFritzBox().detectFirmware();
						Vector<SipProvider> data = JFritz.getFritzBox().retrieveSipProvider();
						JFritz.getSIPProviderTableModel().updateProviderList(
								data);
						JFritz.getSIPProviderTableModel()
								.fireTableDataChanged();
						JFritz.getCallerList().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						JFritz.errorMsg(Main.getMessage("box.address_wrong")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.address_wrong")); //$NON-NLS-1$
					} catch (InvalidFirmwareException e1) {
						JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
				}
			}
		};

		// Create OK/Cancel Panel
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel okcancelpanel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton, c);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);

        //set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        //set default close button (ESC)
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = 3L;

            public void actionPerformed(ActionEvent e)
            {
                 cancelButton.doClick();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		tpane.addTab(
				Main.getMessage("FRITZ!Box"), fritzBoxPanel); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("telephone"), phonePanel); //$NON-NLS-1$
		tpane
				.addTab(
						Main.getMessage("sip_numbers"), createSipPane(actionListener)); //$NON-NLS-1$
		JScrollPane callerListPaneScrollable = new JScrollPane(
				createCallerListPane());
		tpane.addTab(Main.getMessage("callerlist"), callerListPaneScrollable); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("callmonitor"), callMonitorPanel); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("messages"), messagePanel); //$NON-NLS-1$
		JScrollPane otherPaneScrollable = new JScrollPane(otherPanel); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("other"), otherPaneScrollable); //$NON-NLS-1$
		tpane.addTab(Main.getMessage("language"),languagePanel);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tpane, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
		c.fill = GridBagConstraints.HORIZONTAL;

		addKeyListener(keyListener);

		setSize(new Dimension(510, 360));
		setResizable(false);
		// pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}
}