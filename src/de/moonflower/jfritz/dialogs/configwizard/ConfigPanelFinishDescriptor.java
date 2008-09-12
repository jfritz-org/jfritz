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
public class ConfigPanelFinishDescriptor extends WizardPanelDescriptor {

	public static final String IDENTIFIER = "FINISH_PANEL";

	private ConfigPanelFinish configpanelFinish;

	public ConfigPanelFinishDescriptor() {
		configpanelFinish = new ConfigPanelFinish();
		setPanelDescriptorIdentifier(IDENTIFIER);
	    setPanelComponent(configpanelFinish);

	}

	public Object getNextPanelDescriptor() {
		return FINISH;
	}

	public Object getBackPanelDescriptor() {
		return ConfigPanelCallMonitorDescriptor.IDENTIFIER;
	}

	public void aboutToDisplayPanel() {

	}

}
