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

import de.moonflower.jfritz.JFritz;
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
	private final ImageIcon imagePhone, imageHandy, imageHome, imageWorld,
			imageFreeCall, imageAT, imageBE, imageCH, imageCN, imageCZ, imageDE,
			imageDK, imageES, imageFI,  imageFR, imageGB, imageHU, imageIE,
			imageIT, imageJP, imageLU, imageNL, imageNO, imagePL, imagePT,
			imageRU, imageSE, imageSK,  imageTR, imageUA, imageUS;

	private final ImageIcon imageD1, imageD2, imageO2, imageEplus,
			imageSipgate;

	private final static boolean showHandyLogos = true;

	 static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$

	/**
	 * renders the number field in the CallerTable
	 */
	public NumberCellRenderer() {
		super();
		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png"))); //$NON-NLS-1$
		imageHandy = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/handy.png"))); //$NON-NLS-1$
		imageHome = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/home.png"))); //$NON-NLS-1$
		imageWorld = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png"))); //$NON-NLS-1$
		imageD1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/d1.png"))); //$NON-NLS-1$
		imageD2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/d2.png"))); //$NON-NLS-1$
		imageO2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/o2.png"))); //$NON-NLS-1$
		imageEplus = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/eplus.png"))); //$NON-NLS-1$
		imageSipgate = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/sipgate.png"))); //$NON-NLS-1$
		imageFreeCall = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/freecall.png"))); //$NON-NLS-1$

		String lang = JFritzUtils.getFullPath("lang");

		//Please keep these in alphabetical order

		imageAT = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "at.gif");

		imageBE = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "be.gif");

		imageCH = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "ch.gif");

		imageCN = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "cn.gif");

		imageCZ = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "cz.gif");

		imageDE = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "de.gif");

		imageDK = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "dk.gif");

		imageES = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "es.gif");

		imageFI = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "fi.gif");

		imageFR = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "fr.gif");

		imageGB = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "gb.gif");

		imageHU = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "hu.gif");

		imageIE = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "ie.gif");

		imageIT = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "it.gif");

		imageJP = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "jp.gif");

		imageLU = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "lu.gif");

		imageNL = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "nl.gif");

		imageNO = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "no.gif");

		imagePL = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "pl.gif");

		imagePT = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "pt.gif");

		imageRU = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "ru.gif");

		imageSE = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "se.gif");

		imageSK = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "sk.gif");

		imageTR = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "tr.gif");

		imageUA = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "ua.gif");

		imageUS = new ImageIcon(lang + FILESEP + "flags" + FILESEP + "us.gif");
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		String countryCode = JFritz.getProperty("country.code", "+49");

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			PhoneNumber number = (PhoneNumber) value;
			setToolTipText(number.toString());
			label.setText(number.getShortNumber());
			// Debug.msg("Number: "+number.getShortNumber());
			if (number.getIntNumber().length() > 6) {
				if (number.isMobile()) {
					String provider = number.getMobileProvider();
					if (provider.equals("")) //$NON-NLS-1$
						provider = JFritz.getMessage("unknown"); //$NON-NLS-1$

					setToolTipText(JFritz.getMessage("cellphone_network") //$NON-NLS-1$
							+ ": " + provider); //$NON-NLS-1$
					if (showHandyLogos) {
						if (provider.equals("D1")) { //$NON-NLS-1$
							label.setIcon(imageD1);
						} else if (provider.equals("D2")) { //$NON-NLS-1$
							label.setIcon(imageD2);
						} else if (provider.equals("O2")) { //$NON-NLS-1$
							label.setIcon(imageO2);
						} else if (provider.equals("E+")) { //$NON-NLS-1$
							label.setIcon(imageEplus);
						} else {
							label.setIcon(imageHandy);
						}
					} else {
						label.setIcon(imageHandy);
					}
				} else if ((number.getIntNumber().startsWith(JFritz
						.getProperty("area.prefix") //$NON-NLS-1$
						+ JFritz.getProperty("area.code") + "1988")) //$NON-NLS-1$,  //$NON-NLS-2$
						|| (number.getIntNumber().startsWith("01801777"))) { //$NON-NLS-1$
					label.setIcon(imageSipgate);
					setToolTipText(JFritz.getMessage("voip_call")); //$NON-NLS-1$
				} else if (number.isLocalCall()) {
					label.setIcon(imageHome);
					setToolTipText(JFritz.getMessage("local_call")); //$NON-NLS-1$
				} else if (number.getIntNumber().startsWith(countryCode)){
					label.setIcon(imagePhone);
					setToolTipText(JFritz.getMessage("fixed_network")); //$NON-NLS-1$

					//Please keep these in alphabetical order
				} else if(number.getIntNumber().startsWith(PhoneNumber.AUSTRIA_CODE)
						&& !countryCode.equals(PhoneNumber.AUSTRIA_CODE)){
					label.setIcon(imageAT);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.BELGIUM_CODE)
						&& !countryCode.equals(PhoneNumber.BELGIUM_CODE)){
					label.setIcon(imageBE);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.CHINA_CODE)
						&& !countryCode.equals(PhoneNumber.CHINA_CODE)){
					label.setIcon(imageCN);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.CZECH_CODE)
						&& !countryCode.equals(PhoneNumber.CZECH_CODE)){
					label.setIcon(imageCZ);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.DENMARK_CODE)
						&& !countryCode.equals(PhoneNumber.DENMARK_CODE)){
					label.setIcon(imageDK);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.FINLAND_CODE)
						&& !countryCode.equals(PhoneNumber.FINLAND_CODE)){
					label.setIcon(imageFI);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.FRANCE_CODE)
						&& !countryCode.equals(PhoneNumber.FRANCE_CODE)){
					label.setIcon(imageFR);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.GERMANY_CODE)
						&& !countryCode.equals(PhoneNumber.GERMANY_CODE)){
					label.setIcon(imageDE);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.GREATBRITAIN_CODE)
						&& !countryCode.equals(PhoneNumber.GREATBRITAIN_CODE)){
					label.setIcon(imageGB);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.HOLLAND_CODE)
						&& !countryCode.equals(PhoneNumber.HOLLAND_CODE)){
					label.setIcon(imageNL);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.HUNGARY_CODE)
						&& !countryCode.equals(PhoneNumber.HUNGARY_CODE)){
					label.setIcon(imageHU);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.IRELAND_CODE)
						&& !countryCode.equals(PhoneNumber.IRELAND_CODE)){
					label.setIcon(imageIE);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.ITALY_CODE)
						&& !countryCode.equals(PhoneNumber.ITALY_CODE)){
					label.setIcon(imageIT);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.JAPAN_CODE)
						&& !countryCode.equals(PhoneNumber.JAPAN_CODE)){
					label.setIcon(imageJP);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.LUXEMBOURG_CODE)
						&& !countryCode.equals(PhoneNumber.LUXEMBOURG_CODE)){
					label.setIcon(imageLU);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.NORWAY_CODE)
						&& !countryCode.equals(PhoneNumber.NORWAY_CODE)){
					label.setIcon(imageNO);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.POLAND_CODE)
						&& !countryCode.equals(PhoneNumber.POLAND_CODE)){
					label.setIcon(imagePL);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.PORTUGAL_CODE)
						&& !countryCode.equals(PhoneNumber.PORTUGAL_CODE)){
					label.setIcon(imagePT);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.RUSSIA_CODE)
						&& !countryCode.equals(PhoneNumber.RUSSIA_CODE)){
					label.setIcon(imageRU);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.SLOVAKIA_CODE)
						&& !countryCode.equals(PhoneNumber.SLOVAKIA_CODE)){
					label.setIcon(imageSK);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.SPAIN_CODE)
						&& !countryCode.equals(PhoneNumber.SPAIN_CODE)){
					label.setIcon(imageES);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.SWEDEN_CODE)
						&& !countryCode.equals(PhoneNumber.SWEDEN_CODE)){
					label.setIcon(imageSE);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.SWITZERLAND_CODE)
						&& !countryCode.equals(PhoneNumber.SWITZERLAND_CODE)){
					label.setIcon(imageCH);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.TURKEY_CODE)
						&& !countryCode.equals(PhoneNumber.TURKEY_CODE)){
					label.setIcon(imageTR);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.UKRAINE_CODE)
						&& !countryCode.equals(PhoneNumber.UKRAINE_CODE)){
					label.setIcon(imageUA);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				} else if(number.getIntNumber().startsWith(PhoneNumber.USA_CODE)
						&& !countryCode.equals(PhoneNumber.USA_CODE)){
					label.setIcon(imageUS);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$

				}else if (
						(number.getIntNumber().startsWith(JFritz.getProperty("country.prefix"))
							||
							(number.getIntNumber().startsWith("+"))
						&& !(number.getIntNumber().startsWith(countryCode)))) { //$NON-NLS-1$
					label.setIcon(imageWorld);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$
				} else if (number.isFreeCall()) {
					label.setIcon(imageFreeCall);
					setToolTipText(JFritz.getMessage("freecall")); //$NON-NLS-1$
				} else {
					label.setIcon(imagePhone);
					setToolTipText(JFritz.getMessage("fixed_network")); //$NON-NLS-1$
				}
			} else {
				label.setIcon(null);
			}
		} else {
			label.setIcon(null);
		}
		return label;
	}
}