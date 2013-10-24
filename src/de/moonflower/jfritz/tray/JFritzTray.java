package de.moonflower.jfritz.tray;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.BoxCommunication;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;

public class JFritzTray {
	private static Tray tray;
	private static MessageProvider messages = MessageProvider.getInstance();
	private static ImageIcon trayIcon;
	private static JFritzWindow jframe;
	private static BoxCommunication boxCommunication;

	public static void initTray(final JFritzWindow frame, final BoxCommunication boxComm) {
		jframe = frame;
		boxCommunication = boxComm;
		if(Integer.parseInt(System.getProperty("java.version").substring(2, 3)) < 6)
		{
			tray = new JDICTray();
		} else {
			tray = new SwingTray();
		}

		TrayMenu menu = createTrayMenu();
		tray.setPopupMenu(menu);

		initIcon();

		System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

		tray.setTooltip(ProgramConstants.PROGRAM_NAME + " v"+ProgramConstants.PROGRAM_VERSION);

		refreshTrayActionListener();

	}

	private static void initIcon() {
		trayIcon = new ImageIcon(
				JFritz.class
						.getResource("/de/moonflower/jfritz/resources/images/trayicon.png")); //$NON-NLS-1$

		tray.add(trayIcon);
	}

	/**
	 * Creates the tray icon menu
	 */
	private static TrayMenu createTrayMenu() {
		LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
		ButtonGroup lnfgroup = new ButtonGroup();

		JMenu lnfMenu = new JMenu(messages.getMessage("lnf_menu")); //$NON-NLS-1$
		// Add system dependent look and feels
		for (int i = 0; i < lnfs.length; i++) {
			JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i]
					.getName());
			lnfMenu.add(rbmi);
			rbmi.setSelected(UIManager.getLookAndFeel().getClass().getName()
					.equals(lnfs[i].getClassName()));
			rbmi.putClientProperty("lnf name", lnfs[i]); //$NON-NLS-1$
			if (jframe != null) {
				rbmi.addItemListener(jframe);
			}
			lnfgroup.add(rbmi);
		}

		// Add additional look and feels from looks-2.1.4.jar
		LookAndFeelInfo lnf = new LookAndFeelInfo("Plastic","com.jgoodies.looks.plastic.PlasticLookAndFeel");
		JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		if (jframe != null) {
			rb.addItemListener(jframe);
		}
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic 3D","com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		if (jframe != null) {
			rb.addItemListener(jframe);
		}
		lnfgroup.add(rb);

		lnf = new LookAndFeelInfo("Plastic XP","com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		rb = new JRadioButtonMenuItem(lnf.getName());
		lnfMenu.add(rb);
		rb.putClientProperty("lnf name", lnf);
		rb.setSelected(UIManager.getLookAndFeel().getClass().getName()
				.equals(lnf.getClassName()));
		if (jframe != null) {
			rb.addItemListener(jframe);
		}
		lnfgroup.add(rb);


		TrayMenu menu = new TrayMenu("JFritz Menu"); //$NON-NLS-1$
		TrayMenuItem menuItem = new TrayMenuItem(ProgramConstants.PROGRAM_NAME + " v" //$NON-NLS-1$
				+ ProgramConstants.PROGRAM_VERSION + " Rev: " + ProgramConstants.REVISION);
		menuItem.setActionCommand("showhide");
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menu.addSeparator();
		if (boxCommunication != null) {
			for (int i=0; i<boxCommunication.getBoxCount(); i++) {
				String boxName = boxCommunication.getBox(i).getName();
				BoxClass box = boxCommunication.getBox(boxName);
				if (box != null) {
					JMenu boxItem = new JMenu(boxName);
					menuItem = new TrayMenuItem("IP: " + box.getExternalIP());
					boxItem.add(menuItem.getJMenuItem());
					boxItem.addSeparator();
					menuItem = new TrayMenuItem(messages.getMessage("fetchlist"));
					menuItem.setActionCommand("fetchList-"+boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getJMenuItem());
					menuItem = new TrayMenuItem(messages.getMessage("renew_ip"));
					menuItem.setActionCommand("renewIP-"+boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getJMenuItem());
					menuItem = new TrayMenuItem("Reboot");
					menuItem.setActionCommand("reboot-"+boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getJMenuItem());
					menu.add(boxItem);
				}
			}
		}
		menu.addSeparator();
		menuItem = new TrayMenuItem(messages.getMessage("fetchlist")); //$NON-NLS-1$
		menuItem.setActionCommand("fetchList"); //$NON-NLS-1$
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("dial_assist")); //$NON-NLS-1$
		menuItem.setActionCommand("callDialog");
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menuItem = new TrayMenuItem(messages.getMessage("dial_assist") + "(" + messages.getMessage("clipboard") + ")"); //$NON-NLS-1$
		menuItem.setActionCommand("callDialogTray");
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menu.add(lnfMenu);
		menuItem = new TrayMenuItem(messages.getMessage("config")); //$NON-NLS-1$
		menuItem.setActionCommand("config"); //$NON-NLS-1$
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new TrayMenuItem(messages.getMessage("prog_exit")); //$NON-NLS-1$
		menuItem.setActionCommand("exit"); //$NON-NLS-1$
		if (jframe != null) {
			menuItem.addActionListener(jframe);
		}
		menu.add(menuItem);

		return menu;
	}

	private static void refreshTrayActionListener() {
		String trayClick = PropertyProvider.getInstance().getProperty("tray.clickCount");
		int clickCount = ClickListener.CLICK_COUNT_SINGLE;
		if ("2".equals(trayClick)) {
			clickCount = ClickListener.CLICK_COUNT_DOUBLE;
		}

		tray.clearActionListeners();
		tray.addActionListener(new ClickListener(ClickListener.CLICK_LEFT,
												 clickCount) {
			private long oldTimeStamp = 0;
			private void showHide() {
				if ( jframe != null )
				{
					jframe.hideShowJFritz(true);
				}
			}

			public void actionPerformed(ActionEvent e) {
				if (tray instanceof JDICTray) {
					// old JDICTray has no mouse listener,
					// get timestamp to simulate single/double-click
					if (this.getClickCount() == ClickListener.CLICK_COUNT_SINGLE) {
						long timeStamp = e.getWhen();
						if ( timeStamp-oldTimeStamp>600 ) {
							showHide();
							oldTimeStamp = timeStamp;
						}
					} else {
						long timeStamp = e.getWhen();
						if ( timeStamp-oldTimeStamp<600 ) {
							showHide();
						}
						oldTimeStamp = timeStamp;
					}
				} else if (tray instanceof SwingTray) {
					showHide();
				}
			}
		});
	}

	/**
	 * Deletes actual systemtray and creates a new one.
	 *
	 * @author Benjamin Schmitt
	 */
	public static void refreshTrayMenu() {
		if (tray != null && trayIcon != null) {
			tray.remove();
			initTray(jframe, boxCommunication);
		}
	}

	public static void removeTrayMenu() {
		if (tray != null) {
			Debug.info("Removing systray"); //$NON-NLS-1$
			tray.remove();
			tray = null;
		}
	}

	public static void displayMessage(final String caption, final String message, final int type) {
		if (tray != null) {
			tray.displayMessage(caption, message, type);
		}
	}

	public static boolean isSupported() {
		if (tray == null)
			return false;
		return tray.isSupported();
	}
}
