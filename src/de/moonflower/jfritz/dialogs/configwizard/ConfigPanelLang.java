package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.config.languageComboBoxRenderer;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StartEndFilenameFilter;

/**
 * This class is responsible for creating a same pane to select the language
 * used in jfritz for the other wizard panels
 *
 * @author Brian Jensen
 *
 */
public class ConfigPanelLang extends JPanel{

    private static final long serialVersionUID = 1;

    public String[] localeList;

    public JComboBox languageCombo;

    static final String FILESEP = System.getProperty("file.separator");
    final String langID = FILESEP + "lang";

    public ConfigPanelLang(){

    	JPanel localePane = new JPanel();
		localePane.setLayout(new GridBagLayout());
		localePane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		JLabel label = new JLabel(""); //$NON-NLS-1$
		c.gridy = 2;
		label = new JLabel(JFritz.getMessage("language") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		localePane.add(label, c);



		File file = new File(JFritzUtils.getFullPath(langID));
		FilenameFilter props = new StartEndFilenameFilter("jfritz","properties");//$NON-NLS-1$,  //$NON-NLS-2$
		String[] list = file.list(props);
		localeList= new String[list.length];

		ImageIcon[]  images = new ImageIcon[list.length];

		for (int i = 0; i < list.length; i++) {
			localeList[i] = list[i].substring(list[i].indexOf("_") + 1,list[i].indexOf("."));//$NON-NLS-1$,  //$NON-NLS-2$
			images[i] = new ImageIcon("lang"+FILESEP+"flags"+FILESEP+localeList[i].substring(localeList[i].indexOf("_")+1, localeList[i].length()) + ".gif");//$NON-NLS-1$,  //$NON-NLS-2$ //$NON-NLS-3$,  //$NON-NLS-4$
			images[i].setDescription(JFritz.getLocaleMeaning(localeList[i]));
		}

		c.fill = GridBagConstraints.HORIZONTAL;
		languageCombo = new JComboBox(images);
		languageComboBoxRenderer renderer = new languageComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(180, 15));

		languageCombo.setRenderer(renderer);
		languageCombo.setActionCommand("languageCombo"); //$NON-NLS-1$
		languageCombo.setMaximumRowCount(8);

		localePane.add(languageCombo, c);

		add(localePane);

	    }

}
