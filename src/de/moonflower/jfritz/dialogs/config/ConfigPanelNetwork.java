package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzDataDirectory;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.cellrenderer.ButtonCellEditor;
import de.moonflower.jfritz.cellrenderer.ButtonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PasswordCellRenderer;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.network.ClientLoginsTableModel;
import de.moonflower.jfritz.network.Login;
import de.moonflower.jfritz.network.NetworkStateListener;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelNetwork extends JPanel implements ConfigPanel, ActionListener,
					NetworkStateListener {
	private final static Logger log = Logger.getLogger(ConfigPanelNetwork.class);

	private static final long serialVersionUID = 100;

	private JDialog parent;

	private JComboBox<String> networkTypeCombo;

	private JCheckBox clientTelephoneBook, clientCallList, clientCallMonitor,
		clientStandAlone, connectOnStartup, listenOnStartup;

	private JTextField serverName, serverPort, serverLogin,
	 	clientsPort, maxConnections;

	private JPasswordField serverPassword;

	private JToggleButton startClientButton, startServerButton;

	private JTable logonsTable;

	private JPanel mainPanel;

	private JPanel serverPanel;

	//private JScrollPane clientPanel;

	private JPanel clientPanel;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelNetwork(JDialog parent) {
		this.parent = parent;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
		networkTypeCombo = new JComboBox<String>();
		networkTypeCombo.addItem(messages.getMessage("no_network_function")); //$NON-NLS-1$
		networkTypeCombo.addItem(messages.getMessage("network_server_function")); //$NON-NLS-1$
		networkTypeCombo.addItem(messages.getMessage("network_client_function")); //$NON-NLS-1$
		networkTypeCombo.addActionListener(this);

		mainPanel.add(networkTypeCombo, BorderLayout.NORTH);

		clientPanel = getClientPanel();
		serverPanel = getServerPanel();

		clientPanel.setVisible(false);
		serverPanel.setVisible(false);

		NetworkStateMonitor.addListener(this);

		add(new JScrollPane(mainPanel), BorderLayout.CENTER);
	}

	public void loadSettings() {
		String type = properties.getProperty("network.type");
		if(type.equals("0")){
			networkTypeCombo.setSelectedIndex(0);
		}else if(type.equals("1")){
			mainPanel.add(serverPanel, BorderLayout.SOUTH);
			networkTypeCombo.setSelectedIndex(1);
		}else{
			mainPanel.add(clientPanel, BorderLayout.SOUTH);
			networkTypeCombo.setSelectedIndex(2);
		}

		clientTelephoneBook.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.clientTelephoneBook"))); //$NON-NLS-1$,  //$NON-NLS-2$

		clientCallList.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.clientCallList"))); //$NON-NLS-1$,  //$NON-NLS-2$

		clientCallMonitor.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.clientCallMonitor"))); //$NON-NLS-1$,  //$NON-NLS-2$

		clientStandAlone.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.clientStandAlone"))); //$NON-NLS-1$,  //$NON-NLS-2$

		listenOnStartup.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.clientCallMonitor"))); //$NON-NLS-1$,  //$NON-NLS-2$

		connectOnStartup.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.connectOnStartup"))); //$NON-NLS-1$,  //$NON-NLS-2$

		listenOnStartup.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.listenOnStartup"))); //$NON-NLS-1$,  //$NON-NLS-2$

		serverName.setText(properties.getProperty("server.name"));
		serverPort.setText(properties.getProperty("server.port"));
		serverLogin.setText(properties.getProperty("server.login"));
		serverPassword.setText(Encryption.decrypt(properties.getProperty("server.password")));

		clientsPort.setText(properties.getProperty("clients.port"));
		maxConnections.setText(properties.getProperty("max.Connections"));

		if(NetworkStateMonitor.isListening()){
			startServerButton.setSelected(true);
			startServerButton.setText(messages.getMessage("server_is_listening"));
			startClientButton.setSelected(false);
			startClientButton.setText(messages.getMessage("connect_to_server"));
		}else if(NetworkStateMonitor.isConnectedToServer()){
			startClientButton.setSelected(true);
			startClientButton.setText(messages.getMessage("client_is_connected"));
			startServerButton.setSelected(false);
			startServerButton.setText(messages.getMessage("start_listening_clients"));
		}else{
			startClientButton.setSelected(false);
			startClientButton.setText(messages.getMessage("connect_to_server"));
			startServerButton.setSelected(false);
			startServerButton.setText(messages.getMessage("start_listening_clients"));
		}

	}

	public void saveSettings() {
		// save the various settings
		int selectedIndex = networkTypeCombo.getSelectedIndex();

		properties.setProperty("option.clientTelephoneBook", Boolean.toString(clientTelephoneBook //$NON-NLS-1$
				.isSelected()));
		properties.setProperty("option.clientCallList", Boolean //$NON-NLS-1$
				.toString(clientCallList.isSelected()));

		properties.setProperty("option.clientCallMonitor", Boolean
				.toString(clientCallMonitor.isSelected()));

		properties.setProperty("option.clientStandAlone", Boolean
				.toString(clientStandAlone.isSelected()));

		properties.setProperty("network.type", String //$NON-NLS-1$
				.valueOf(selectedIndex));
		properties.setProperty("option.connectOnStartup", Boolean //$NON-NLS-1$
				.toString(connectOnStartup.isSelected()));
		properties.setProperty("option.listenOnStartup", Boolean //$NON-NLS-1$
				.toString(listenOnStartup.isSelected()));

		properties.setProperty("server.name", serverName.getText());
		properties.setProperty("server.port", serverPort.getText());
		properties.setProperty("server.login", serverLogin.getText());
		String password = new String(serverPassword.getPassword());
		properties.setProperty("server.password", Encryption.encrypt(password));

		properties.setProperty("clients.port", clientsPort.getText());
		properties.setProperty("max.Connections", maxConnections.getText());

		NetworkStateMonitor.removeListener(this);

		//Clear the previous network connections that don't fit to the user selection
		if(selectedIndex == 0){
			if(NetworkStateMonitor.isListening())
				NetworkStateMonitor.stopServer();
			else if(NetworkStateMonitor.isConnectedToServer())
				NetworkStateMonitor.stopClient();
		}else if(selectedIndex == 1){
			if(NetworkStateMonitor.isConnectedToServer())
				NetworkStateMonitor.stopClient();

		}else if(selectedIndex == 2){
			if(NetworkStateMonitor.isListening())
				NetworkStateMonitor.stopServer();
		}

		JFritz.getJframe().setNetworkButton();
		ClientLoginsTableModel.saveToXMLFile(JFritzDataDirectory.getInstance().getDataDirectory() + JFritz.CLIENT_SETTINGS_FILE);

	}

	private JPanel getServerPanel(){
		JPanel panel = new JPanel();
		JPanel optionsPanel = new JPanel();

		panel.setLayout(new BorderLayout());

		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		optionsPanel.add(new JLabel(messages.getMessage("listen_on_startup")), c);
		listenOnStartup = new JCheckBox();
		optionsPanel.add(listenOnStartup, c);

		c.gridy = 1;
		optionsPanel.add(new JLabel(messages.getMessage("client_connect_port")), c);
		clientsPort = new JTextField("", 16);
		clientsPort.setMinimumSize(new Dimension(200, 20));
		optionsPanel.add(clientsPort, c);

		c.gridy = 2;
		optionsPanel.add(new JLabel(messages.getMessage("max_client_connections")), c);
		maxConnections = new JTextField("", 16);
		maxConnections.setMinimumSize(new Dimension(200, 20));
		optionsPanel.add(maxConnections, c);

		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel buttonsPanel = new JPanel();
		JButton addButton = new JButton(messages.getMessage("add"));
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
		buttonsPanel.add(addButton);

		JButton removeButton = new JButton(messages.getMessage("remove"));
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);
		buttonsPanel.add(removeButton);
		optionsPanel.add(buttonsPanel, c);


		panel.add(optionsPanel, BorderLayout.NORTH);

		logonsTable = new JTable(JFritz.getClientLogins()) {
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

		logonsTable.setRowHeight(24);
		logonsTable.setFocusable(false);
		logonsTable.setAutoCreateColumnsFromModel(true);
		logonsTable.setColumnSelectionAllowed(false);
		logonsTable.setCellSelectionEnabled(false);
		logonsTable.setRowSelectionAllowed(true);
		logonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		logonsTable.getColumnModel().getColumn(0).setMinWidth(50);
		logonsTable.getColumnModel().getColumn(0).setMaxWidth(120);
		logonsTable.getColumnModel().getColumn(1).setMinWidth(50);
		logonsTable.getColumnModel().getColumn(1).setMaxWidth(120);
		logonsTable.getColumnModel().getColumn(1).setCellRenderer(new PasswordCellRenderer());
		logonsTable.getColumnModel().getColumn(2).setMinWidth(100);
		logonsTable.getColumnModel().getColumn(2).setMaxWidth(100);
		logonsTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonCellRenderer());
		logonsTable.getColumnModel().getColumn(2).setCellEditor(new ButtonCellEditor(new JCheckBox(), parent));
		logonsTable.getColumnModel().getColumn(3).setMinWidth(100);
		logonsTable.getColumnModel().getColumn(3).setMaxWidth(100);
		logonsTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonCellRenderer());
		logonsTable.getColumnModel().getColumn(3).setCellEditor(new ButtonCellEditor(new JCheckBox(), parent));
		logonsTable.getColumnModel().getColumn(4).setMinWidth(100);
		logonsTable.getColumnModel().getColumn(4).setMaxWidth(100);
		logonsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonCellRenderer());
		logonsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonCellEditor(new JCheckBox(), parent));
		logonsTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
		JScrollPane jsPane = new JScrollPane(logonsTable);
		jsPane.setMaximumSize(new Dimension(200, 100));

		panel.add(jsPane, BorderLayout.CENTER);

		startServerButton = new JToggleButton();
		startServerButton.setMaximumSize(new Dimension(200, 20));
		startServerButton.addActionListener(this);
		startServerButton.setActionCommand("listen");
		panel.add(startServerButton, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel getClientPanel(){
		JPanel panel = new JPanel();
		JLabel label;

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.weightx = 0.9;
		c.gridy = 1;
		label = new JLabel(messages.getMessage("client_call_list"));
		label.setPreferredSize(new Dimension(200, 20));
		panel.add(label, c);
		clientCallList = new JCheckBox();
		c.weightx = 0.1;
		panel.add(clientCallList, c);

		c.gridy = 2;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("client_telephone_book"));
		label.setPreferredSize(new Dimension(200, 20));
		panel.add(label, c);
		clientTelephoneBook = new JCheckBox();
		c.weightx = 0.1;
		panel.add(clientTelephoneBook, c);

		c.gridy = 3;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("client_call_monitor"));
		label.setPreferredSize(new Dimension(200, 20));
		panel.add(label, c);
		clientCallMonitor = new JCheckBox();
		c.weightx = 0.1;
		panel.add(clientCallMonitor, c);

		c.gridy = 4;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("connect_on_startup"));
		label.setPreferredSize(new Dimension(200, 20));
		panel.add(label, c);
		connectOnStartup = new JCheckBox();
		c.weightx = 0.1;
		panel.add(connectOnStartup, c);

		c.gridy = 5;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("client_stand_alone"));
		label.setPreferredSize(new Dimension(200, 20));
		panel.add(label, c);
		clientStandAlone = new JCheckBox();
		c.weightx = 0.1;
		panel.add(clientStandAlone, c);

		c.gridy = 6;
		c.weightx = 0.9;
		c.gridx = 0;
		label = new JLabel(messages.getMessage("server_name"));
		label.setPreferredSize(new Dimension(100, 20));
		panel.add(label, c);
		c.gridx = 1;
		c.weightx = 0.1;
		serverName = new JTextField("", 16);
		serverName.setPreferredSize(new Dimension(100, 20));
		panel.add(serverName, c);

		c.weightx = 0.9;
		c.gridy = 7;
		c.gridx = 0;
		label = new JLabel(messages.getMessage("server_login"));
		label.setPreferredSize(new Dimension(100, 20));
		panel.add(label, c);
		c.gridx = 1;
		c.weightx = 0.1;
		serverLogin = new JTextField("", 16);
		serverLogin.setPreferredSize(new Dimension(100, 20));
		panel.add(serverLogin, c);

		c.gridy = 8;
		c.gridx = 0;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("server_password"));
		label.setPreferredSize(new Dimension(100, 20));
		panel.add(label, c);
		c.gridx = 1;
		c.weightx = 0.1;
		serverPassword = new JPasswordField("", 16);
		serverPassword.setPreferredSize(new Dimension(100, 20));
		panel.add(serverPassword, c);

		c.gridy = 9;
		c.gridx = 0;
		c.weightx = 0.9;
		label = new JLabel(messages.getMessage("server_port"));
		label.setPreferredSize(new Dimension(100, 20));
		panel.add(label, c);
		c.gridx = 1;
		c.weightx = 0.1;
		serverPort = new JTextField("", 16);
		serverPort.setPreferredSize(new Dimension(100, 20));
		panel.add(serverPort, c);

		c.gridy = 10;
		c.gridx = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		startClientButton = new JToggleButton();
		startClientButton.setMinimumSize(new Dimension(200, 20));
		startClientButton.setActionCommand("connect");
		startClientButton.addActionListener(this);
		panel.add(startClientButton, c);

		//sPanel = new JScrollPane(panel);

		//return sPanel;
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		if ("comboboxchanged".equalsIgnoreCase(e.getActionCommand())) { //$NON-NLS-1$
			// Zur Darstellung der gewünschten Einstellungspanels
			switch (networkTypeCombo.getSelectedIndex()) {
			case 0: {
				log.info("No network functionality chosen"); //$NON-NLS-1$
				mainPanel.removeAll();
				networkTypeCombo.setSelectedIndex(0);
				mainPanel.add(networkTypeCombo, BorderLayout.NORTH);
				clientPanel.setVisible(false);
				serverPanel.setVisible(false);
				mainPanel.updateUI();
				break;
			}
			case 1: {
				log.info("JFritz as a server chosen"); //$NON-NLS-1$
				mainPanel.removeAll();
				mainPanel.add(networkTypeCombo, BorderLayout.NORTH);
				networkTypeCombo.setSelectedIndex(1);
				mainPanel.add(serverPanel, BorderLayout.SOUTH);
				clientPanel.setVisible(false);
				serverPanel.setVisible(true);
				serverPanel.repaint();
				mainPanel.updateUI();
				break;
			}
			case 2: {
				log.info("JFritz as a client chosen"); //$NON-NLS-1$
				mainPanel.removeAll();
				mainPanel.add(networkTypeCombo, BorderLayout.NORTH);
				networkTypeCombo.setSelectedIndex(2);
				mainPanel.add(clientPanel, BorderLayout.SOUTH);
				clientPanel.setVisible(true);
				serverPanel.setVisible(false);
				clientPanel.repaint();
				mainPanel.updateUI();
				break;

			}

			}

		}else if(e.getActionCommand().equals("listen")){
			if(startServerButton.isSelected()){
				this.saveSettings();
				//saveSettings() removes this panel as a listener, add it again
				NetworkStateMonitor.addListener(this);
				NetworkStateMonitor.startServer();
			}else{
				NetworkStateMonitor.stopServer();
			}
		}else if(e.getActionCommand().equals("connect")){
			if(this.startClientButton.isSelected()){
				this.saveSettings();
				//re add this panel as a listener
				NetworkStateMonitor.addListener(this);
				NetworkStateMonitor.startClient();
			}else{
				NetworkStateMonitor.stopClient();
			}
		}else if(e.getActionCommand().equals("add")){
			ClientLoginsTableModel.addLogin(new Login("changeme", "", false, false,
					false, false, false, false, false, false, false, false, false,
					false, false, new Vector<CallFilter>(), ""));
			JFritz.getClientLogins().fireTableDataChanged();

		}else if(e.getActionCommand().equals("remove")){
			int loginIndex = logonsTable.getSelectedRow();
			ClientLoginsTableModel.removeLogin(loginIndex);
			JFritz.getClientLogins().fireTableDataChanged();
		}
	}

	public void clientStateChanged(){
		if(NetworkStateMonitor.isConnectedToServer()){
			startClientButton.setSelected(true);
			startClientButton.setText(messages.getMessage("client_is_connected"));
		}else{
			startClientButton.setSelected(false);
			startClientButton.setText(messages.getMessage("connect_to_server"));
		}
	}

	public void serverStateChanged(){
		if(NetworkStateMonitor.isListening()){
			startClientButton.setSelected(true);
			startClientButton.setText(messages.getMessage("server_is_listening"));
		}else{
			startClientButton.setSelected(false);
			startClientButton.setText(messages.getMessage("start_listening_clients"));
		}
	}

	public String getPath()
	{
		return messages.getMessage("network");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Netzwerk";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}
}
