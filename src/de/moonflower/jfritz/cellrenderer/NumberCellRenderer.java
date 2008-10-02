/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.JFritzUtils;
/**
 * This is the renderer for the call type cell of the table, which shows a small
 * icon.
 *
 * @author Arno Willig
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	private final ImageIcon imagePhone, /*imageHandy,*/ imageHome, imageWorld,
			imageFreeCall;

	static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$

	private final String lang;

	/**
	 * renders the number field in the CallerTable
	 */
	public NumberCellRenderer() {
		super();

		lang = JFritzUtils.getFullPath(JFritzUtils.langID);

		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png"))); //$NON-NLS-1$
		//imageHandy = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
		//		getClass().getResource(
		//				"/de/moonflower/jfritz/resources/images/handy.png"))); //$NON-NLS-1$
		imageHome = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/home.png"))); //$NON-NLS-1$
		imageWorld = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png"))); //$NON-NLS-1$
		imageFreeCall = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/freecall.png"))); //$NON-NLS-1$
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		String countryCode = Main.getProperty("country.code");

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			PhoneNumber number = (PhoneNumber) value;
			setToolTipText(number.getDescription());
			label.setText(number.getShortNumber());

			if(number.getIntNumber().length() > 6){

				if(number.isLocalCall()){
					label.setIcon(imageHome);
					setToolTipText(Main.getMessage("local_call")); //$NON-NLS-1$
				}else if(number.isFreeCall()){
					label.setIcon(imageFreeCall);
					setToolTipText(Main.getMessage("freecall")); //$NON-NLS-1$
				}else if (!number.isMobile() && number.getIntNumber().startsWith(countryCode)){
					label.setIcon(imagePhone);
					setToolTipText(Main.getMessage("fixed_network")); //$NON-NLS-1$

				}else{
					if(!number.getFlagFileName().equals("")){
						label.setIcon(new ImageIcon(lang + FILESEP + "flags" + FILESEP + number.getFlagFileName()));
						setToolTipText(number.getDescription());
					}else{
						label.setIcon(imageWorld);
						setToolTipText(Main.getMessage("int_call")); //$NON-NLS-1$
					}
				}
			}

		} else {
			label.setIcon(null);
		}
		return label;
	}
}