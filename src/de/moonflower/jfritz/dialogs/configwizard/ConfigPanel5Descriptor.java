package de.moonflower.jfritz.dialogs.configwizard;

import javax.swing.JPanel;

import com.nexes.wizard.*;

/**
 * @author Brian Jensen
 *
 * This is the  descriptor for the call monitor panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel5Descriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MONITOR_PANEL";

	  public JPanel configpanel5;

	   public ConfigPanel5Descriptor() {
			configpanel5 = new ConfigPanel5();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(configpanel5);
	    }

	    public Object getNextPanelDescriptor() {
	        return FINISH;
	    }

	    public Object getBackPanelDescriptor() {
	        return MessagePanelDescriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
