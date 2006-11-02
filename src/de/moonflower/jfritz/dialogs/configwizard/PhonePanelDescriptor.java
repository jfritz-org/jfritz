package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.PhonePanel;

/**
 * @author Brian Jensen
 *
 * This is the descriptor for the phone settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class PhonePanelDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "PHONE_PANEL";

	  public PhonePanel phonePanel;

	   public PhonePanelDescriptor() {
			phonePanel = new PhonePanel();
			phonePanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(phonePanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return FritzBoxPanelDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanel1Descriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
