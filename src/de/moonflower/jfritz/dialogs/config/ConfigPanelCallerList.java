package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelCallerList extends JPanel implements ConfigPanel {
	private final static Logger log = Logger.getLogger(ConfigPanelCallerList.class);

	private static final long serialVersionUID = 7267124419351267208L;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
	notifyOnCallsButton,
	lookupAfterFetchButton,
	fetchAfterStandby;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelCallerList() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fetchAfterStartButton = new JCheckBox(messages.getMessage("fetch_after_start")); //$NON-NLS-1$
		cPane.add(fetchAfterStartButton, c);

		c.gridy = c.gridy + 1;
		notifyOnCallsButton = new JCheckBox(messages.getMessage("notify_on_calls")); //$NON-NLS-1$
		cPane.add(notifyOnCallsButton, c);

		c.gridy = c.gridy + 1;
		deleteAfterFetchButton = new JCheckBox(messages.getMessage("delete_after_fetch")); //$NON-NLS-1$
		cPane.add(deleteAfterFetchButton, c);

		c.gridy = c.gridy + 1;
		lookupAfterFetchButton = new JCheckBox(messages.getMessage("lookup_after_fetch")); //$NON-NLS-1$
		cPane.add(lookupAfterFetchButton, c);

		c.gridy = c.gridy + 1;
		fetchAfterStandby = new JCheckBox(messages.getMessage("fetch_after_standby")); //$NON-NLS-1$
		cPane.add(fetchAfterStandby, c);

		JScrollPane scrollPane = new JScrollPane(cPane);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void loadSettings() {
		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(properties.getProperty("option.notifyOnCalls"))); //$NON-NLS-1$
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(properties.getProperty("option.fetchAfterStart"))); //$NON-NLS-1$
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(properties.getProperty("option.deleteAfterFetch"))); //$NON-NLS-1$

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(properties.getProperty("option.lookupAfterFetch")));

		fetchAfterStandby.setSelected(JFritzUtils.parseBoolean(properties.getProperty("option.watchdog.fetchAfterStandby"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if(properties.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(properties.getProperty("option.clientCallList"))){

			log.info("NETWORKING: JFritz is running as a client and using call list from server, disabeling some options");
			deleteAfterFetchButton.setSelected(false);
			deleteAfterFetchButton.setEnabled(false);
		}
	}

	public void saveSettings() {
		properties.setProperty("option.notifyOnCalls", Boolean //$NON-NLS-1$
				.toString(notifyOnCallsButton.isSelected()));
		properties.setProperty("option.fetchAfterStart", Boolean //$NON-NLS-1$
				.toString(fetchAfterStartButton.isSelected()));
		properties.setProperty("option.deleteAfterFetch", Boolean //$NON-NLS-1$
				.toString(deleteAfterFetchButton.isSelected()));
		properties.setProperty("option.lookupAfterFetch", Boolean //$NON-NLS-1$
				.toString(lookupAfterFetchButton.isSelected()));
		properties.setProperty("option.watchdog.fetchAfterStandby", Boolean //$NON-NLS-1$
				.toString(fetchAfterStandby.isSelected()));
	}

	public String getPath()
	{
		return messages.getMessage("callerlist");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Anrufliste";
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
