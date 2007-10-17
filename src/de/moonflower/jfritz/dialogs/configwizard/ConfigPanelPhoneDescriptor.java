package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.ConfigPanelPhone;

/**
 * @author Brian Jensen
 *
 * This is the descriptor for the phone settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelPhoneDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "PHONE_PANEL";

	  public ConfigPanelPhone phonePanel;

	   public ConfigPanelPhoneDescriptor() {
			phonePanel = new ConfigPanelPhone();
			phonePanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(phonePanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelNetworkDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanel1Descriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
