package de.moonflower.jfritz.dialogs.configwizard;


import com.nexes.wizard.*;
import de.moonflower.jfritz.dialogs.config.ConfigPanelMessage;

/**
 * @author Brian Jensen
 *
 * This is the descriptor for the message settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelMessageDescriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MESSAGES_PANEL";

	  ConfigPanelMessage messagePanel;

	   public ConfigPanelMessageDescriptor() {
			messagePanel = new ConfigPanelMessage();
			messagePanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(messagePanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelCallMonitorDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	    	return ConfigPanelFritzBoxLoginDescriptor.IDENTIFIER;
	    }
}
