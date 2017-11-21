package de.moonflower.jfritz.tray;

import java.awt.Menu;
import java.awt.SystemTray;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.BoxCommunication;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

public class JFritzTray {
	private final static Logger log = Logger.getLogger(JFritzTray.class);

	private static Tray tray;
	private static MessageProvider messages = MessageProvider.getInstance();
	private static ImageIcon trayIcon;
	private static JFritzWindow jframe;
	private static BoxCommunication boxCommunication;

	public static void initTray(final JFritzWindow frame, final BoxCommunication boxComm) {
		jframe = frame;
		boxCommunication = boxComm;
		if (SystemTray.isSupported()) {
			log.info("Using AWTTray as SystemTray");
			tray = new AWTTray();
		} else {
			log.info("Using SwingTray as SystemTray");
			tray = new SwingTray();
		}

		initIcon();

		TrayMenu menu = createTrayMenu();
		tray.setPopupMenu(menu);

		System.setProperty("javax.swing.adjustPopupLocationToFit", "false"); //$NON-NLS-1$,  //$NON-NLS-2$

		tray.setTooltip(ProgramConstants.PROGRAM_NAME + " v"+ProgramConstants.PROGRAM_VERSION);

		refreshTrayActionListener();

	}

	private static void initIcon() {
		trayIcon = new ImageIcon(JFritz.class.getClassLoader().getResource("images/trayicon.png")); //$NON-NLS-1$

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
		if (boxCommunication != null && boxCommunication.getBoxCount() > 0) {
			for (int i = 0; i < boxCommunication.getBoxCount(); i++) {
				String boxName = boxCommunication.getBox(i).getName();
				BoxClass box = boxCommunication.getBox(boxName);
				if (box != null) {
					Menu boxItem = new Menu(boxName);
					menuItem = new TrayMenuItem("IP: " + box.getExternalIP());
					boxItem.add(menuItem.getMenuItem());
					boxItem.addSeparator();
					menuItem = new TrayMenuItem(messages.getMessage("fetchlist"));
					menuItem.setActionCommand("fetchList-" + boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getMenuItem());
					menuItem = new TrayMenuItem(messages.getMessage("renew_ip"));
					menuItem.setActionCommand("renewIP-" + boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getMenuItem());
					menuItem = new TrayMenuItem("Reboot");
					menuItem.setActionCommand("reboot-" + boxName);
					if (jframe != null) {
						menuItem.addActionListener(jframe);
					}
					boxItem.add(menuItem.getMenuItem());
					menu.add(boxItem);
				}
			}
			menu.addSeparator();
		}

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
	    tray.clearActionListeners();
        tray.addMouseListener(new MouseAdapter() {
            @Override()
            public void mouseClicked(MouseEvent e) {
                if (isOneClickConfigured() && isLeftMouseButtonPressedOnce(e)) {
                    showHideJfritzWindow();
                } else if (isTwoClickConfigured() && isLeftMouseButtonPressedTwice(e)) {
					showHideJfritzWindow();
                }
            }
        });
	}

	private static boolean isOneClickConfigured() {
        return "1".equals(PropertyProvider.getInstance().getProperty("tray.clickCount"));
    }

    private static boolean isTwoClickConfigured() {
        return "2".equals(PropertyProvider.getInstance().getProperty("tray.clickCount"));
    }

    private static boolean isLeftMouseButtonPressedOnce(MouseEvent e) {
        return e.getClickCount() == 1 && e.getButton() == (MouseEvent.BUTTON1 & MouseEvent.MOUSE_PRESSED);
    }

    private static boolean isLeftMouseButtonPressedTwice(MouseEvent e) {
        return e.getClickCount() == 2 && e.getButton() == (MouseEvent.BUTTON1 & MouseEvent.MOUSE_PRESSED);
    }

    private static void showHideJfritzWindow() {
        if (jframe != null) {
            jframe.hideShowJFritz(true);
        }
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
			log.info("Removing systray"); //$NON-NLS-1$
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
