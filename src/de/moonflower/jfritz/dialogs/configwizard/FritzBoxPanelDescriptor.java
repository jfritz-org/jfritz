package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.FritzBoxPanel;

/**
 *  @author Brian Jensen
 *
 *  This is the descriptor for the box setings panel
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class FritzBoxPanelDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "FRITZBOX_PANEL";

	  public FritzBoxPanel fritzBoxPanel;

	  public FritzBoxPanelDescriptor() {
			fritzBoxPanel = new FritzBoxPanel();
			fritzBoxPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(fritzBoxPanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return MessagePanelDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return PhonePanelDescriptor.IDENTIFIER;
	    }
}
