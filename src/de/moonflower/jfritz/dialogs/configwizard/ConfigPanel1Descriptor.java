package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.*;
/**
 *  @author Brian Jensen
 *
 *  This is the first panel descriptor
 *
 *  @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel1Descriptor extends WizardPanelDescriptor {

	public static final String IDENTIFIER = "INTRO_PANEL";

	private ConfigPanel1 configpanel1;

	public ConfigPanel1Descriptor() {
		configpanel1 = new ConfigPanel1();
		setPanelDescriptorIdentifier(IDENTIFIER);
	    setPanelComponent(configpanel1);

	}

	public Object getNextPanelDescriptor() {
		return PhonePanelDescriptor.IDENTIFIER;
	}

	public Object getBackPanelDescriptor() {
		return null;
	}

	public void aboutToDisplayPanel() {

	}

}
