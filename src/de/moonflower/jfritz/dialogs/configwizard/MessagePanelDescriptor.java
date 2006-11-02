package de.moonflower.jfritz.dialogs.configwizard;


import com.nexes.wizard.*;
import de.moonflower.jfritz.dialogs.config.MessagePanel;

/**
 * @author Brian Jensen
 *
 * This is the descriptor for the message settings panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class MessagePanelDescriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MESSAGES_PANEL";

	  MessagePanel messagePanel;

	   public MessagePanelDescriptor() {
			messagePanel = new MessagePanel();
			messagePanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(messagePanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanel5Descriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return FritzBoxPanelDescriptor.IDENTIFIER;
	    }
}
