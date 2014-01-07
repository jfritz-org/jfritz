package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxIP;

/**
 *  @author Brian Jensen
 *
 *  This is the descriptor for the box settings panel
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelFritzBoxIpDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "FRITZBOX_IP_PANEL";

	  public ConfigPanelFritzBoxIP fritzBoxPanel;

	  public ConfigPanelFritzBoxIpDescriptor() {
		  if (JFritz.getBoxCommunication() != null
				  && JFritz.getBoxCommunication().getBoxCount() > 0
				  && JFritz.getBoxCommunication().getBox(0) != null) {
				fritzBoxPanel = new ConfigPanelFritzBoxIP(
						(FritzBox) JFritz.getBoxCommunication().getBox(0));
		  } else {
			  fritzBoxPanel = new ConfigPanelFritzBoxIP(null);
		  }
			fritzBoxPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(fritzBoxPanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelFritzBoxLoginDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanelPhoneDescriptor.IDENTIFIER;
	    }

	    public ConfigPanelFritzBoxIP getFritzBoxPanel() {
	    	return fritzBoxPanel;
	    }
}
