package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.Frame;
import javax.swing.*;
import java.util.Locale;

import com.nexes.wizard.*;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;

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

	private Wizard wizard;

	private WizardPanelDescriptor descriptor2, descriptor3, descriptor4, descriptor5;

	private boolean canceled = false;

	public ConfigWizard(Frame parent){

		Debug.msg("asking the user for the language");

		//if user clicked cancel on the language dialog, return back to jfritz
		askLanguage(parent);

		Debug.msg("Create JFritz config wizard");
		wizard = new Wizard(JFritz.getJframe());
        wizard.getDialog().setTitle(Main.getMessage("config_wizard"));
       	wizard.getDialog().setLocationRelativeTo(JFritz.getJframe());


        //initialize the wizard with the correct order of the panels
        WizardPanelDescriptor descriptor1 = new ConfigPanel1Descriptor();
        wizard.registerWizardPanel(ConfigPanel1Descriptor.IDENTIFIER, descriptor1);

        descriptor2 = new ConfigPanel2Descriptor();
        wizard.registerWizardPanel(ConfigPanel2Descriptor.IDENTIFIER, descriptor2);

        descriptor3 = new ConfigPanel3Descriptor();
        wizard.registerWizardPanel(ConfigPanel3Descriptor.IDENTIFIER, descriptor3);

        descriptor4 = new ConfigPanel4Descriptor();
        wizard.registerWizardPanel(ConfigPanel4Descriptor.IDENTIFIER, descriptor4);

        descriptor5 = new ConfigPanel5Descriptor();
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

		if(canceled)
			return;

		//possible return values: 0 finish clicked, 1 cancel clicked, 2 error...
       int ret = wizard.showModalDialog();

       switch (ret){

       		case 0:
       			Debug.msg("Finished clicked, saving settings");

       			//save the various settings
       			Main.setProperty("option.playSounds", Boolean.toString(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).soundButton //$NON-NLS-1$
       					.isSelected())));
       			Main.setProperty("option.startExternProgram", Boolean //$NON-NLS-1$
       					.toString(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).externProgramCheckBox.isSelected())));
       			Main.setProperty(
       					"option.externProgram", JFritzUtils.convertSpecialChars(
       					(((ConfigPanel5)descriptor5.getPanelComponent()).externProgramTextField //$NON-NLS-1$
       					.getText())));
       			Main.setProperty("option.callMonitorType", String //$NON-NLS-1$
       					.valueOf((((ConfigPanel5)descriptor5.getPanelComponent()).callMonitorCombo
       					.getSelectedIndex())));

       			// Set Popup Messages Type
       			if ( ((ConfigPanel4)descriptor4.getPanelComponent()).popupNoButton.isSelected() ) {
       				Main.setProperty("option.popuptype", "0"); //$NON-NLS-1$, //$NON-NLS-2$
       			} else if (((ConfigPanel4)descriptor4.getPanelComponent()).popupDialogButton.isSelected()) {
       				Main.setProperty("option.popuptype", "1"); //$NON-NLS-1$, //$NON-NLS-2$
       			} else {
       				Main.setProperty("option.popuptype", "2"); //$NON-NLS-1$, //$NON-NLS-2$
       			}

       			Main.setProperty("box.password", Encryption.encrypt(
       					((ConfigPanel3)descriptor3.getPanelComponent()).password)); //$NON-NLS-1$

       			Main.setProperty("box.address",
       					((ConfigPanel3)descriptor3.getPanelComponent()).address.getText()); //$NON-NLS-1$
       			Main.setProperty("area.code",
       					((ConfigPanel2)descriptor2.getPanelComponent()).areaCode.getText()); //$NON-NLS-1$
       			Main.setProperty("country.code",
       					((ConfigPanel2)descriptor2.getPanelComponent()).countryCode.getText()); //$NON-NLS-1$
       			Main.setProperty("area.prefix",
       					((ConfigPanel2)descriptor2.getPanelComponent()).areaPrefix.getText()); //$NON-NLS-1$
       			Main.setProperty("country.prefix",
       					((ConfigPanel2)descriptor2.getPanelComponent()).countryPrefix.getText()); //$NON-NLS-1$

      			if (((ConfigPanel3)descriptor3.getPanelComponent()).firmware != null) {
       				Main.setProperty("box.firmware",
       						((ConfigPanel3)descriptor3.getPanelComponent()).firmware.getFirmwareVersion()); //$NON-NLS-1$
       			} else {
       				Main.removeProperty("box.firmware"); //$NON-NLS-1$
       			}

       			Main.setProperty("dial.prefix",
       					((ConfigPanel2)descriptor2.getPanelComponent()).dialPrefix.getText()); //$NON-NLS-1$
       			Main.setProperty(
       	                "option.activateDialPrefix", Boolean.toString(
       	                		((ConfigPanel2)descriptor2.getPanelComponent()).activateDialPrefix.isSelected())); //$NON-NLS-1$

      			JFritz.getFritzBox().detectFirmware();
      			Main.saveProperties();

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
	    wizard.getDialog().setTitle(Main.getMessage("config_wizard"));

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
			if (!Main.getProperty("locale", "de_DE").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
				Main.setProperty(
						"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
				String loc = localeList[languageCombo.getSelectedIndex()];
				JFritz.getJframe().setLanguage(
						new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
			}

		}else
			canceled =  true;

	}

}
