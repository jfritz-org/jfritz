package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;

import de.moonflower.jfritz.dialogs.config.ConfigPanelNetwork;

public class ConfigPanelNetworkDescriptor extends WizardPanelDescriptor{

	  public static final String IDENTIFIER = "NETWORK_PANEL";

	  public static ConfigPanelNetwork networkPanel;

	  public ConfigPanelNetworkDescriptor(){
		  networkPanel = new ConfigPanelNetwork(null);
		  networkPanel.loadSettings();
		  setPanelDescriptorIdentifier(IDENTIFIER);
		  setPanelComponent(networkPanel);
	  }

	    public Object getNextPanelDescriptor() {
	        // if we are client using call list from server, dont show fritzbox panel
	    	if(networkPanel.getNetworkType() == 2
	    			&& networkPanel.useCallListFromServer()){
		    	return ConfigPanelMessageDescriptor.IDENTIFIER;
	        }
	    	return ConfigPanelFritzBoxDescriptor.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor() {
	    	return ConfigPanelPhoneDescriptor.IDENTIFIER;
	    }

	    public void aboutToDisplayPanel() {

	    }

}
