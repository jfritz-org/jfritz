package de.moonflower.jfritz.dialogs.config;

import javax.swing.tree.DefaultMutableTreeNode;

public class ConfigTreeNode extends DefaultMutableTreeNode {

	private ConfigPanel panel = null;

	private String caption = "";

	private static final long serialVersionUID = 4774624880660041440L;

	public ConfigTreeNode(ConfigPanel panel, String caption)
	{
		this.caption = caption;
		this.panel = panel;
	}

	public ConfigTreeNode(String caption)
	{
		this.caption = caption;
	}

	public ConfigPanel getConfigPanel()
	{
		return panel;
	}

	public String toString()
	{
		return caption;
	}

	public ConfigPanel getPanel()
	{
		return panel;
	}

	public void setPanel(ConfigPanel panel)
	{
		this.panel = panel;
	}
}
