package de.moonflower.jfritz.dialogs.config;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StartEndFilenameFilter;

/**
 * This class is responsible for creating a same pane to select the language
 * used in jfritz for the other wizard panels
 *
 * @author Brian Jensen
 *
 */
public class ConfigPanelLang extends JPanel implements ConfigPanel{

    private static final long serialVersionUID = 1;

    public String[] localeList;

    public JComboBox languageCombo;

    public ConfigPanelLang(){

		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		JLabel label;
		c.gridy = 2;
		label = new JLabel(Main.getMessage("language") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		add(label, c);

		String lang = JFritzUtils.getFullPath(JFritzUtils.langID);
		File file = new File(lang);
		FilenameFilter props = new StartEndFilenameFilter("jfritz_","properties");//$NON-NLS-1$,  //$NON-NLS-2$
		String[] list = file.list(props);
		localeList= new String[list.length];

		ImageIcon[]  images = new ImageIcon[list.length];

		for (int i = 0; i < list.length; i++) {
			localeList[i] = list[i].substring(list[i].indexOf("_") + 1,list[i].indexOf("."));//$NON-NLS-1$,  //$NON-NLS-2$
			String imagePath =
			     lang + JFritzUtils.FILESEP + "flags" + JFritzUtils.FILESEP +	//$NON-NLS-1$,  //$NON-NLS-2$
			     localeList[i].substring(localeList[i].indexOf("_")+1,
			         localeList[i].length()).toLowerCase() + ".gif";			//$NON-NLS-1$
			Debug.msg("Found resources for locale '" + localeList[i] +			//$NON-NLS-1$
			     "', loading flag image '" + imagePath + "'");					//$NON-NLS-1$,  //$NON-NLS-2$
			images[i] = new ImageIcon(imagePath);
			images[i].setDescription(Main.getLocaleMeaning(localeList[i]));
		}

		c.fill = GridBagConstraints.HORIZONTAL;

		languageCombo = new JComboBox(images);
		LanguageComboBoxRenderer renderer = new LanguageComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(180, 15));

		languageCombo.setRenderer(renderer);
		languageCombo.setActionCommand("languageCombo"); //$NON-NLS-1$
		languageCombo.setMaximumRowCount(8);

		add(languageCombo, c);
	}

	public void loadSettings() {
		int index = 0;
		String loc = Main.getProperty("locale", "en_US");
		for (int a = 0; a < localeList.length; a++) {
			if (localeList[a].equals(loc)) index = a;
		}
		languageCombo.setSelectedIndex(index);
	}

	public void saveSettings() {
		if (!Main.getProperty("locale", "en_US").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
			Main.setProperty(
					"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
			String loc = localeList[languageCombo.getSelectedIndex()];
			JFritz.getJframe().setLanguage(
					new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
		}
	}
}
