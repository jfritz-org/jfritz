package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.CallMonitorPanel;

/**
 * @author Brian Jensen
 *
 * This is the  descriptor for the call monitor panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class CallMonitorPanelDescriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MONITOR_PANEL";

	  public CallMonitorPanel callMonitorPanel;

	   public CallMonitorPanelDescriptor() {
			callMonitorPanel = new CallMonitorPanel(null, false); //TODO: nicht null, sondern ein richtiges Fenster
			callMonitorPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(callMonitorPanel);
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
