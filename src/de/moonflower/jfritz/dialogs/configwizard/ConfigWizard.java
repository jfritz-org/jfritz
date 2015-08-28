package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.SplashScreen;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.dialogs.config.ConfigPanelCallMonitor;
import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxIP;
import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxLogin;
import de.moonflower.jfritz.dialogs.config.ConfigPanelLang;
import de.moonflower.jfritz.dialogs.config.ConfigPanelMessage;
import de.moonflower.jfritz.dialogs.config.ConfigPanelPhone;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.messages.UpdateMessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;

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
	private final static Logger log = Logger.getLogger(ConfigWizard.class);

	private Wizard wizard;
	private Image icon;

	private ConfigPanelFritzBoxIpDescriptor descriptor3;
	private ConfigPanelFritzBoxLoginDescriptor descriptor4;

	private WizardPanelDescriptor descriptor2, descriptor5, descriptor6;

	private boolean languageCanceled = false;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigWizard(Frame parent){
		icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource(
				"/de/moonflower/jfritz/resources/images/tray16.png")); //$NON-NLS-1$

		Debug.info(log, "asking the user for the language");

		Wizard.setBackText(messages.getMessage("back"));
		Wizard.setNextText(messages.getMessage("next"));
		Wizard.setFinishText(messages.getMessage("finish"));
		Wizard.setCancelText(messages.getMessage("cancel"));

		//if user clicked cancel on the language dialog, return back to jfritz
		if (!askLanguage(parent)) {

			Debug.info(log, "Create JFritz config wizard");
			wizard = new Wizard(JFritz.getJframe());
			wizard.getDialog().setIconImage(icon);

	        wizard.getDialog().setTitle(ProgramConstants.PROGRAM_NAME + " " + messages.getMessage("config_wizard"));

	        //initialize the wizard with the correct order of the panels
	        WizardPanelDescriptor descriptor1 = new ConfigPanel1Descriptor();
	        wizard.registerWizardPanel(ConfigPanel1Descriptor.IDENTIFIER, descriptor1);

	        descriptor2 = new ConfigPanelPhoneDescriptor();
	        wizard.registerWizardPanel(ConfigPanelPhoneDescriptor.IDENTIFIER, descriptor2);

	        descriptor3 = new ConfigPanelFritzBoxIpDescriptor();
	        wizard.registerWizardPanel(ConfigPanelFritzBoxIpDescriptor.IDENTIFIER, descriptor3);

	        descriptor4 = new ConfigPanelFritzBoxLoginDescriptor(descriptor3);
	        wizard.registerWizardPanel(ConfigPanelFritzBoxLoginDescriptor.IDENTIFIER, descriptor4);

	        descriptor5 = new ConfigPanelMessageDescriptor();
	        wizard.registerWizardPanel(ConfigPanelMessageDescriptor.IDENTIFIER, descriptor5);

	        descriptor6 = new ConfigPanelCallMonitorDescriptor(descriptor3.getFritzBoxPanel());
	        wizard.registerWizardPanel(ConfigPanelCallMonitorDescriptor.IDENTIFIER, descriptor6);

	        WizardPanelDescriptor finishDescriptor= new ConfigPanelFinishDescriptor();
	        wizard.registerWizardPanel(ConfigPanelFinishDescriptor.IDENTIFIER, finishDescriptor);

	        //set the first panel to be displayed
	        wizard.setCurrentPanel(ConfigPanel1Descriptor.IDENTIFIER);
	       	wizard.setLocationRelativeToParent(true);
		}
	}
	/**
	 * This function shows the wizard then stores the values if finish is clicked
	 *
	 * @author Brian Jensen
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 *
	 */
	public boolean showWizard(final SplashScreen splash) throws WrongPasswordException, InvalidFirmwareException, IOException{
		if(languageCanceled)
			return true;

		//possible return values: 0 finish clicked, 1 cancel clicked, 2 error...
       int ret = wizard.showModalDialog();

       switch (ret){

       		case 0:
       			Debug.info(log, "Finished clicked, saving settings");
       			if (splash != null) {
       				splash.setVisible(true);
       			}

       			((ConfigPanelPhone)descriptor2.getPanelComponent()).saveSettings();
       			
       			// save settings of FritzBoxLogin before FritzBoxIp, because FritzBoxIp needs username and password
       			// to get further data in saveSettings()
       			((ConfigPanelFritzBoxLogin)descriptor4.getPanelComponent()).saveSettings(false);
       			
       			try {
           			((ConfigPanelFritzBoxIP)descriptor3.getPanelComponent()).saveSettings(false);
       			}
   				catch (InvalidFirmwareException ife)
   				{
   					Debug.error(log, "Invalid firmware");
   				}

       			((ConfigPanelMessage)descriptor5.getPanelComponent()).saveSettings();
       			((ConfigPanelCallMonitor)descriptor6.getPanelComponent()).saveSettings();

      			properties.saveConfigProperties();

				return false;
       		case 1:
       			Debug.info(log, "Cancel clicked, not saving values");
       			return true;
       		case 2:
       			Debug.info(log, "Error in the wizard, bailing out..");
       			return true;
       		default:
       			return true;
       }
	}
	
	public void setNextFinishButtonEnabled(boolean newValue) {
		wizard.setNextFinishButtonEnabled(newValue);
	}


	/**
	 * This dialog changes the language used in jfritz
	 * It is called before the initial creation of the real wizard,
	 * so that the wizard will be displayed in the proper language
	 *
	 * @author Brian Jensen
	 *
	 * @param parent
	 * @return true if language selection has been canceled
	 */
	public boolean askLanguage(Frame parent){
		wizard = new Wizard();
		wizard.getDialog().setIconImage(icon);

	    wizard.getDialog().setTitle(ProgramConstants.PROGRAM_NAME + " " + messages.getMessage("config_wizard"));
       	wizard.setLocationRelativeToParent(true);

	    if(parent != null)
	       	wizard.getDialog().setLocationRelativeTo(parent);

	    WizardPanelDescriptor descriptorLang = new ConfigPanelLangDescriptor();
        wizard.registerWizardPanel(ConfigPanelLangDescriptor.IDENTIFIER, descriptorLang);

        wizard.setCurrentPanel(ConfigPanelLangDescriptor.IDENTIFIER);
        wizard.getDialog().setLocationRelativeTo(parent);
		int ret = wizard.showModalDialog();
		//only change the language if finish was selected
		if(ret == 0){

			//get the components from the wizardpanel
			JComboBox languageCombo = ((ConfigPanelLang)descriptorLang.getPanelComponent()).languageCombo;
			String[] localeList = ((ConfigPanelLang)descriptorLang.getPanelComponent()).localeList;

			//This code is real ugly, i should get around to cleaning it up!
			if (!properties.getProperty("locale").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
				properties.setProperty(
						"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
				String loc = localeList[languageCombo.getSelectedIndex()];
				Locale locale = new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length()));
				messages.loadMessages(locale);
				UpdateMessageProvider.getInstance().loadMessages(locale);
				if (JFritz.getJframe() != null) {
					JFritz.getJframe().setLanguage(locale);
				}
				Wizard.setBackText(messages.getMessage("back"));
				Wizard.setNextText(messages.getMessage("next"));
				Wizard.setFinishText(messages.getMessage("finish"));
				Wizard.setCancelText(messages.getMessage("cancel"));
			}

		} else {
			languageCanceled =  true;
		}

		return languageCanceled;
	}

}
