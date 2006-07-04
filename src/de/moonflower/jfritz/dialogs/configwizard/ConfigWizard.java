package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.Frame;
import javax.swing.*;
import java.util.Locale;

import com.nexes.wizard.*;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.JFritz;

/**
 *
 * @author Brian Jensen
 *
 * This class uses the java wizard framework for creating a configurations wizard
 * current config panels include: phone settings, box settings, message settings,
 * call monitor settings. This wizard is be used to configure jfritz for first time users
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 *
 */
public class ConfigWizard {

	private JFritz jfritz;

	private Wizard wizard;

	private WizardPanelDescriptor descriptor2, descriptor3, descriptor4, descriptor5;

	public ConfigWizard(JFritz jf, Frame parent){

		jfritz = jf;
		Debug.msg("asking the user for the language");
		askLanguage(parent);

		Debug.msg("Create JFritz config wizard");
		wizard = new Wizard(jfritz.getJframe());
        wizard.getDialog().setTitle(JFritz.getMessage("config_wizard"));
       	wizard.getDialog().setLocationRelativeTo(jfritz.getJframe());


        //initialize the wizard with the correct order of the panels
        WizardPanelDescriptor descriptor1 = new ConfigPanel1Descriptor();
        wizard.registerWizardPanel(ConfigPanel1Descriptor.IDENTIFIER, descriptor1);

        descriptor2 = new ConfigPanel2Descriptor();
        wizard.registerWizardPanel(ConfigPanel2Descriptor.IDENTIFIER, descriptor2);

        descriptor3 = new ConfigPanel3Descriptor(jfritz);
        wizard.registerWizardPanel(ConfigPanel3Descriptor.IDENTIFIER, descriptor3);

        descriptor4 = new ConfigPanel4Descriptor(jfritz);
        wizard.registerWizardPanel(ConfigPanel4Descriptor.IDENTIFIER, descriptor4);

        descriptor5 = new ConfigPanel5Descriptor(jfritz);
        wizard.registerWizardPanel(ConfigPanel5Descriptor.IDENTIFIER, descriptor5);

        //set the first panel to be displayed
        wizard.setCurrentPanel(ConfigPanel1Descriptor.IDENTIFIER);

	}
	/**
	 * This function shows the wizard then stores the values if finish is clicked
	 *
	 * @author Brian Jensen
	 *
	 */
	public void showWizard(){

		//possible return values: 0 finish clicked, 1 cancel clicked, 2 error...
       int ret = wizard.showModalDialog();

       switch (ret){

       		case 0:
       			Debug.msg("Finished clicked, saving settings");

       			//save the various settings
       			JFritz.setProperty("option.playSounds", Boolean.toString(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).soundButton //$NON-NLS-1$
       					.isSelected())));
       			JFritz.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
       					.toString(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).externProgramCheckBox.isSelected())));
       			JFritz.setProperty(
       					"option.externProgram", JFritzUtils.convertSpecialChars(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).externProgramTextField //$NON-NLS-1$
       					.getText())));
       			JFritz.setProperty("option.callMonitorType", String //$NON-NLS-1$
       					.valueOf((((ConfigPanel5)descriptor5.getPanelComponent()).callMonitorCombo
       					.getSelectedIndex())));

       			// Set Popup Messages Type
       			if ( ((ConfigPanel4)descriptor4.getPanelComponent()).popupNoButton.isSelected() ) {
       				JFritz.setProperty("option.popuptype", "0"); //$NON-NLS-1$, //$NON-NLS-2$
       			} else if (((ConfigPanel4)descriptor4.getPanelComponent()).popupDialogButton.isSelected()) {
       				JFritz.setProperty("option.popuptype", "1"); //$NON-NLS-1$, //$NON-NLS-2$
       			} else {
       				JFritz.setProperty("option.popuptype", "2"); //$NON-NLS-1$, //$NON-NLS-2$
       			}

       			JFritz.setProperty("box.password", Encryption.encrypt(
       					((ConfigPanel3)descriptor3.getPanelComponent()).password)); //$NON-NLS-1$

       			JFritz.setProperty("box.address",
       					((ConfigPanel3)descriptor3.getPanelComponent()).address.getText()); //$NON-NLS-1$
       			JFritz.setProperty("area.code",
       					((ConfigPanel2)descriptor2.getPanelComponent()).areaCode.getText()); //$NON-NLS-1$
       			JFritz.setProperty("country.code",
       					((ConfigPanel2)descriptor2.getPanelComponent()).countryCode.getText()); //$NON-NLS-1$
       			JFritz.setProperty("area.prefix",
       					((ConfigPanel2)descriptor2.getPanelComponent()).areaPrefix.getText()); //$NON-NLS-1$
       			JFritz.setProperty("country.prefix",
       					((ConfigPanel2)descriptor2.getPanelComponent()).countryPrefix.getText()); //$NON-NLS-1$

      			if (((ConfigPanel3)descriptor3.getPanelComponent()).firmware != null) {
       				JFritz.setProperty("box.firmware",
       						((ConfigPanel3)descriptor3.getPanelComponent()).firmware.getFirmwareVersion()); //$NON-NLS-1$
       			} else {
       				JFritz.removeProperty("box.firmware"); //$NON-NLS-1$
       			}

      			jfritz.getFritzBox().detectFirmware();
      			jfritz.saveProperties();

       			break;
       		case 1:
       			Debug.msg("Cancel clicked, not saving values");
       			break;
       		case 2:
       			Debug.msg("Error in the wizard, bailing out..");
       			break;


       }

	}

	/**
	 * This dialog changes the language used in jfritz
	 * It is called before the initial creation of the real wizard,
	 * so that the wizard will be displayed in the proper language
	 *
	 * @author Brian Jensen
	 *
	 * @param parent
	 */
	public void askLanguage(Frame parent){

		wizard = new Wizard();
	    wizard.getDialog().setTitle(JFritz.getMessage("config_wizard"));

	    if(parent != null)
	       	wizard.getDialog().setLocationRelativeTo(parent);

	    WizardPanelDescriptor descriptorLang = new ConfigPanelLangDescriptor();
        wizard.registerWizardPanel(ConfigPanelLangDescriptor.IDENTIFIER, descriptorLang);

        wizard.setCurrentPanel(ConfigPanelLangDescriptor.IDENTIFIER);

		int ret = wizard.showModalDialog();
		//only change the language if finish was selected
		if(ret == 0){

			//get the components from the wizardpanel
			JComboBox languageCombo = ((ConfigPanelLang)descriptorLang.getPanelComponent()).languageCombo;
			String[] localeList = ((ConfigPanelLang)descriptorLang.getPanelComponent()).localeList;

			//This code is real ugly, i should get around to cleaning it up!
			if (!JFritz.getProperty("locale", "de_DE").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
				JFritz.setProperty(
						"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
				String loc = localeList[languageCombo.getSelectedIndex()];
				jfritz.getJframe().setLanguage(
						new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
			}
		}

	}

}
