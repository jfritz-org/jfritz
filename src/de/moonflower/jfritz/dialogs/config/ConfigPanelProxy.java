package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

public class ConfigPanelProxy extends JPanel implements ConfigPanel, ActionListener {

	private static final long serialVersionUID = 504203751960755293L;

	private JTextField proxyHost, proxyPort, proxyUser, proxyPassword;

	private JCheckBox enableProxy, authRequired;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	private final int textFieldWidth = 10;

	public ConfigPanelProxy() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
        enableProxy = new JCheckBox(messages.getMessage("proxy_active")); //$NON-NLS-1$
        enableProxy.setActionCommand("enable");
        enableProxy.addActionListener(this);
		cPane.add(enableProxy, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.gridwidth = 1;
		JLabel label = new JLabel(messages.getMessage("proxy_http") + ": "); //$NON-NLS-1$
		cPane.add(label, c);
		c.gridx = 1;
		proxyHost = new JTextField("", textFieldWidth); //$NON-NLS-1$
		cPane.add(proxyHost, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		label = new JLabel(messages.getMessage("proxy_port") + ": "); //$NON-NLS-1$
		cPane.add(label, c);
		c.gridx = 1;
		proxyPort = new JTextField("", textFieldWidth); //$NON-NLS-1$
		cPane.add(proxyPort, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.gridwidth = 2;
        authRequired = new JCheckBox(messages.getMessage("proxy_auth")); //$NON-NLS-1$
        authRequired.setActionCommand("authRequired");
        authRequired.addActionListener(this);
		cPane.add(authRequired, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.gridwidth = 1;
		label = new JLabel(messages.getMessage("proxy_user") + ": "); //$NON-NLS-1$
		cPane.add(label, c);
		c.gridx = 1;
		proxyUser = new JTextField("", textFieldWidth); //$NON-NLS-1$
		cPane.add(proxyUser, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		label = new JLabel(messages.getMessage("proxy_password") + ": "); //$NON-NLS-1$
		cPane.add(label, c);
		c.gridx = 1;
		proxyPassword = new JTextField("", textFieldWidth); //$NON-NLS-1$
		cPane.add(proxyPassword, c);

        add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void loadSettings() {
		enableProxy.setSelected(Boolean.parseBoolean(properties.getProperty("option.proxy.active")));
		proxyHost.setText(properties.getProperty("option.proxy.host"));
		proxyPort.setText(properties.getProperty("option.proxy.port"));
		proxyUser.setText(properties.getProperty("option.proxy.user"));
		proxyPassword.setText(properties.getProperty("option.proxy.password"));
		authRequired.setSelected(Boolean.parseBoolean(properties.getProperty("option.proxy.authRequired")));

		refreshStatus();
	}

	public void saveSettings() {
		properties.setProperty("option.proxy.active", Boolean.toString(enableProxy.isSelected()));
		properties.setProperty("option.proxy.host", proxyHost.getText());
		properties.setProperty("option.proxy.port", proxyPort.getText());
		properties.setProperty("option.proxy.user", proxyUser.getText());
		properties.setProperty("option.proxy.password", proxyPassword.getText());
		properties.setProperty("option.proxy.authRequired", Boolean.toString(authRequired.isSelected()));
	}

	public String getPath()
	{
		return messages.getMessage("other")+"::"+messages.getMessage("proxy");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Proxy";
	}

	public void cancel() {
		// nothing to do here
	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}

	private void refreshStatus() {
		if (enableProxy.isSelected()) {
			proxyHost.setEnabled(true);
			proxyPort.setEnabled(true);
			authRequired.setEnabled(true);

			if (authRequired.isSelected()) {
				proxyUser.setEnabled(true);
				proxyPassword.setEnabled(true);
			} else {
				proxyUser.setEnabled(false);
				proxyPassword.setEnabled(false);
			}
		} else {
			proxyHost.setEnabled(false);
			proxyPort.setEnabled(false);
			proxyUser.setEnabled(false);
			proxyPassword.setEnabled(false);
			authRequired.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		refreshStatus();
	}
}
