package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.JFritz;
/**
 *  @author Brian Jensen
 *
 *  This is the descriptor for the box setings panel
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel3Descriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "FRITZBOX_PANEL";

	   public ConfigPanel3Descriptor(JFritz jfritz) {
	        super(IDENTIFIER, new ConfigPanel3(jfritz));
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanel4Descriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanel2Descriptor.IDENTIFIER;
	    }
}
