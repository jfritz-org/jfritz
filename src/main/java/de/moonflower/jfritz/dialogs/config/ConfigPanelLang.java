package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
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
	private final static Logger log = Logger.getLogger(ConfigPanelLang.class);

    private static final long serialVersionUID = 1;

    public String[] localeList;

    public JComboBox languageCombo;

    protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

    public ConfigPanelLang(){
        setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		JLabel label;
		c.gridy = 2;
		label = new JLabel(messages.getMessage("language") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		cPane.add(label, c);

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
			log.info("Found resources for locale '" + localeList[i] +			//$NON-NLS-1$
			     "', loading flag image '" + imagePath + "'");					//$NON-NLS-1$,  //$NON-NLS-2$
			images[i] = new ImageIcon(imagePath);
			images[i].setDescription(Main.getLocaleMeaning(localeList[i]));
		}

		c.fill = GridBagConstraints.HORIZONTAL;

		languageCombo = new JComboBox(images);
		LanguageComboBoxRenderer renderer = new LanguageComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(180, 25));

		languageCombo.setRenderer(renderer);
		languageCombo.setActionCommand("languageCombo"); //$NON-NLS-1$
		languageCombo.setMaximumRowCount(8);

		cPane.add(languageCombo, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void loadSettings() {
		int index = 0;
		String loc = properties.getProperty("locale");
		for (int a = 0; a < localeList.length; a++) {
			if (localeList[a].equals(loc)) index = a;
		}
		languageCombo.setSelectedIndex(index);
	}

	public void saveSettings() {
		if (!properties.getProperty("locale").equals(localeList[languageCombo.getSelectedIndex()])) { //$NON-NLS-1$ //$NON-NLS-2$
			properties.setProperty(
					"locale", localeList[languageCombo.getSelectedIndex()]); //$NON-NLS-1$
			String loc = localeList[languageCombo.getSelectedIndex()];
			JFritz.getJframe().setLanguage(
					new Locale(loc.substring(0, loc.indexOf("_")), loc.substring(loc.indexOf("_")+1, loc.length())));
		}
	}

	public String getPath()
	{
		return messages.getMessage("language");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "https://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Sprache";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		// is already done on saving settings
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		// is already done on saving settings
		return false;
	}
}
