package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import javax.swing.JPanel;
/**
 * @author Brian Jensen
 *
 * This is the descriptor for the phone settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel2Descriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "PHONE_PANEL";

	  public JPanel configpanel2;

	   public ConfigPanel2Descriptor() {
			configpanel2 = new ConfigPanel2();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(configpanel2);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanel3Descriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanel1Descriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
