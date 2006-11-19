package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBox;

/**
 *  @author Brian Jensen
 *
 *  This is the descriptor for the box setings panel
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelFritzBoxDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "FRITZBOX_PANEL";

	  public ConfigPanelFritzBox fritzBoxPanel;

	  public ConfigPanelFritzBoxDescriptor() {
			fritzBoxPanel = new ConfigPanelFritzBox();
			fritzBoxPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(fritzBoxPanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelMessageDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanelPhoneDescriptor.IDENTIFIER;
	    }

	    public ConfigPanelFritzBox getFritzBoxPanel() {
	    	return fritzBoxPanel;
	    }
}
