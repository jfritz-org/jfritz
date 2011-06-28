package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import say.swing.JFontChooser;
import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

public class ConfigPanelFont extends JPanel implements ConfigPanel{

	private static final long serialVersionUID = 1L;

	public JFontChooser fontChooser;

    protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

    public ConfigPanelFont(){
        setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		fontChooser = new JFontChooser();
		cPane.add(fontChooser, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void loadSettings() {
	}

	public void saveSettings() {
		JFritz.getJframe().setFont(fontChooser.getSelectedFont());
		JFritz.getJframe().repaint();
	}

	public String getPath()
	{
		return messages.getMessage("font");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Schriftart";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		// is already done on saving settings
		return true;
	}

	public boolean shouldRefreshTrayMenu() {
		// is already done on saving settings
		return true;
	}
}
