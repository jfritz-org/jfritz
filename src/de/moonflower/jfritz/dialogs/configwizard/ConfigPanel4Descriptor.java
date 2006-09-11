package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

/**
 * @author Brian Jensen
 *
 * This is the descriptor for the message settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel4Descriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MESSAGES_PANEL";

	   public ConfigPanel4Descriptor() {
	        super(IDENTIFIER, new ConfigPanel4());
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanel5Descriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanel3Descriptor.IDENTIFIER;
	    }
}
