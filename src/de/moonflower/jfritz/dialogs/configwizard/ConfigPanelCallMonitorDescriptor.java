package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.WizardPanelDescriptor;

import de.moonflower.jfritz.dialogs.config.ConfigPanelCallMonitor;
import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxIP;

/**
 * @author Brian Jensen
 *
 * This is the  descriptor for the call monitor panel
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanelCallMonitorDescriptor extends WizardPanelDescriptor {

	  public static final String IDENTIFIER = "MONITOR_PANEL";

	  public ConfigPanelCallMonitor callMonitorPanel;

	   public ConfigPanelCallMonitorDescriptor(ConfigPanelFritzBoxIP fritzBoxPanel) {
			callMonitorPanel = new ConfigPanelCallMonitor(null, false, fritzBoxPanel, null); //TODO: nicht null, sondern ein richtiges Fenster
			callMonitorPanel.loadSettings();
			setPanelDescriptorIdentifier(IDENTIFIER);
		    setPanelComponent(callMonitorPanel);
	    }

	    public Object getNextPanelDescriptor() {
	        return ConfigPanelFinishDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	        return ConfigPanelMessageDescriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
