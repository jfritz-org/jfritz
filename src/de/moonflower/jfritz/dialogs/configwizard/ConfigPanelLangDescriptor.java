package de.moonflower.jfritz.dialogs.configwizard;

import com.nexes.wizard.WizardPanelDescriptor;

import de.moonflower.jfritz.dialogs.config.ConfigPanelLang;

/**
 * This is the descriptor class for ConfigPanelLang
 *
 * @author Brian Jensen
 *
 */
public class ConfigPanelLangDescriptor extends WizardPanelDescriptor{

public static final String IDENTIFIER = "LANG_PANEL";

	private ConfigPanelLang configpanellang;

	public ConfigPanelLangDescriptor() {
		configpanellang = new ConfigPanelLang();
		configpanellang.loadSettings();
		setPanelDescriptorIdentifier(IDENTIFIER);
	    setPanelComponent(configpanellang);

	}

	public Object getNextPanelDescriptor() {
		return FINISH;
	}

	public Object getBackPanelDescriptor() {
		return null;
	}

	public void aboutToDisplayPanel() {

	}


}
