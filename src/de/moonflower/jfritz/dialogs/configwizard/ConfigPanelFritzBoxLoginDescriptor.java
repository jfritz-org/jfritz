package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.WizardPanelDescriptor;

import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxLogin;

/**
 *  @author Robert Palmer
 *
 *  This is the descriptor for the box settings panel
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelFritzBoxLoginDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "FRITZBOX_LOGIN_PANEL";

	  public ConfigPanelFritzBoxLogin fritzBoxPanel;

	  public ConfigPanelFritzBoxLoginDescriptor(ConfigPanelFritzBoxIpDescriptor otherConfigPanel) {
			fritzBoxPanel = new ConfigPanelFritzBoxLogin();
			fritzBoxPanel.setFritzBoxPanelIp(otherConfigPanel.getFritzBoxPanel());
			fritzBoxPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(fritzBoxPanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelMessageDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanelFritzBoxIpDescriptor.IDENTIFIER;
	    }

	    public ConfigPanelFritzBoxLogin getFritzBoxPanel() {
	    	return fritzBoxPanel;
	    }
	    
	    @Override
	    public void aboutToDisplayPanel() {
	    	fritzBoxPanel.setWizardReference(this.getWizard());
	    }
}
