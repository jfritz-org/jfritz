package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelCallerList extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = 7267124419351267208L;

	private JCheckBox deleteAfterFetchButton, fetchAfterStartButton,
	notifyOnCallsButton,
	lookupAfterFetchButton,
	showCallByCallColumnButton,
	showCommentColumnButton, showPortColumnButton,
	showPictureColumnButton, fetchAfterStandby;

	public ConfigPanelCallerList() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fetchAfterStartButton = new JCheckBox(Main.getMessage("fetch_after_start")); //$NON-NLS-1$
		cPane.add(fetchAfterStartButton, c);

		c.gridy = 1;
		notifyOnCallsButton = new JCheckBox(Main.getMessage("notify_on_calls")); //$NON-NLS-1$
		cPane.add(notifyOnCallsButton, c);

		c.gridy = 2;
		deleteAfterFetchButton = new JCheckBox(Main.getMessage("delete_after_fetch")); //$NON-NLS-1$
		cPane.add(deleteAfterFetchButton, c);

		c.gridy = 3;
		lookupAfterFetchButton = new JCheckBox(Main.getMessage("lookup_after_fetch")); //$NON-NLS-1$
		cPane.add(lookupAfterFetchButton, c);

		c.gridy = 4;
		showCallByCallColumnButton = new JCheckBox(Main.getMessage("show_callbyball_column")); //$NON-NLS-1$
		cPane.add(showCallByCallColumnButton, c);

		c.gridy = 5;
		showPictureColumnButton = new JCheckBox(Main.getMessage("show_picture_column")); //$NON-NLS-1$
		cPane.add(showPictureColumnButton, c);

		c.gridy = 6;
		showCommentColumnButton = new JCheckBox(Main.getMessage("show_comment_column")); //$NON-NLS-1$
		cPane.add(showCommentColumnButton, c);

		c.gridy = 7;
		showPortColumnButton = new JCheckBox(Main.getMessage("show_port_column")); //$NON-NLS-1$
		cPane.add(showPortColumnButton, c);

		c.gridy = 8;
		fetchAfterStandby = new JCheckBox(Main.getMessage("fetch_after_standby")); //$NON-NLS-1$
		cPane.add(fetchAfterStandby, c);

		JScrollPane scrollPane = new JScrollPane(cPane);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void loadSettings() {
		notifyOnCallsButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.notifyOnCalls"))); //$NON-NLS-1$
		fetchAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.fetchAfterStart"))); //$NON-NLS-1$
		deleteAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.deleteAfterFetch"))); //$NON-NLS-1$

		lookupAfterFetchButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.lookupAfterFetch")));

		showCallByCallColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn."+CallerTable.COLUMN_CALL_BY_CALL))); //$NON-NLS-1$,  //$NON-NLS-2$

		showCommentColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn."+CallerTable.COLUMN_COMMENT))); //$NON-NLS-1$,  //$NON-NLS-2$

		showPortColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn."+CallerTable.COLUMN_PORT))); //$NON-NLS-1$,  //$NON-NLS-2$

		showPictureColumnButton.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn."+CallerTable.COLUMN_PICTURE))); //$NON-NLS-1$,  //$NON-NLS-2$

		fetchAfterStandby.setSelected(JFritzUtils.parseBoolean(Main.getProperty("option.watchdog.fetchAfterStandby"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if(Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList"))){

			Debug.netMsg("JFritz is running as a client and using call list from server, disabeling some options");
			deleteAfterFetchButton.setSelected(false);
			deleteAfterFetchButton.setEnabled(false);
		}
	}

	public void saveSettings() {
		Main.setProperty("option.notifyOnCalls", Boolean //$NON-NLS-1$
				.toString(notifyOnCallsButton.isSelected()));
		Main.setProperty("option.fetchAfterStart", Boolean //$NON-NLS-1$
				.toString(fetchAfterStartButton.isSelected()));
		Main.setProperty("option.deleteAfterFetch", Boolean //$NON-NLS-1$
				.toString(deleteAfterFetchButton.isSelected()));

		Main.setProperty("option.lookupAfterFetch", Boolean //$NON-NLS-1$
				.toString(lookupAfterFetchButton.isSelected()));

		Main.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_CALL_BY_CALL //$NON-NLS-1$
				, Boolean.toString(showCallByCallColumnButton.isSelected()));

		Main.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_COMMENT //$NON-NLS-1$
				, Boolean.toString(showCommentColumnButton.isSelected()));

		Main.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_PORT //$NON-NLS-1$
				, Boolean.toString(showPortColumnButton.isSelected()));

		Main.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_PICTURE //$NON-NLS-1$
				, Boolean.toString(showPictureColumnButton.isSelected()));

		Main.setProperty("option.watchdog.fetchAfterStandby", Boolean //$NON-NLS-1$
				.toString(fetchAfterStandby.isSelected()));
	}

	public String getPath()
	{
		return Main.getMessage("callerlist");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Anrufliste";
	}
}
