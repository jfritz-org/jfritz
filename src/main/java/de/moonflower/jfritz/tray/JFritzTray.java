package de.moonflower.jfritz.tray;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.BoxCommunication;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

        tray.setTooltip(ProgramConstants.PROGRAM_NAME + " v" + ProgramConstants.PROGRAM_VERSION);

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
        TrayMenu menu = new TrayMenu("JFritz Menu"); //$NON-NLS-1$
        String programNameAndVersion = ProgramConstants.PROGRAM_NAME
                + " v" + ProgramConstants.PROGRAM_VERSION //$NON-NLS-1$
                + " Rev: " + ProgramConstants.REVISION; //$NON-NLS-1$

        menu.add(createMenuItem(programNameAndVersion, "showhide"));
        menu.addSeparator();

        createMenuItemsForAllBoxes(menu);

        menu.add(createMenuItemTranslated("fetchlist", "fetchList"));  //$NON-NLS-1$,  //$NON-NLS-2$
        menu.add(createMenuItemTranslated("reverse_lookup", "reverselookup"));  //$NON-NLS-1$,  //$NON-NLS-2$
        menu.add(createMenuItemTranslated("dial_assist", "callDialog"));  //$NON-NLS-1$,  //$NON-NLS-2$
        menu.add(createMenuItemTranslated("dial_assist_clipboard", "callDialogTray"));  //$NON-NLS-1$,  //$NON-NLS-2$
        menu.add(createMenuItemTranslated("config", "config"));  //$NON-NLS-1$,  //$NON-NLS-2$
        menu.addSeparator();

        menu.add(createMenuItemTranslated("prog_exit", "exit"));  //$NON-NLS-1$,  //$NON-NLS-2$

        return menu;
    }

    private static TrayMenuItem createMenuItem(String message, String command) {
        TrayMenuItem menuItem = new TrayMenuItem(message);
        menuItem.setActionCommand(command);
        if (jframe != null) {
            menuItem.addActionListener(jframe);
        }
        return menuItem;
    }

    private static TrayMenuItem createMenuItemTranslated(String messageKey, String command) {
        return createMenuItem(messages.getMessage(messageKey), command);
    }

    private static void createMenuItemsForAllBoxes(TrayMenu menu) {
        if (boxCommunication != null && boxCommunication.getBoxCount() > 0) {
            for (int i = 0; i < boxCommunication.getBoxCount(); i++) {
                String boxName = boxCommunication.getBox(i).getName();
                menu.add(createBoxMenu(boxName));
            }
            menu.addSeparator();
        }
    }

    private static Menu createBoxMenu(String boxName) {
        Menu boxItem = null;

        BoxClass box = boxCommunication.getBox(boxName);
        if (box != null) {
            boxItem = new Menu(boxName);

            boxItem.add(new TrayMenuItem("IP: " + box.getExternalIP()).getMenuItem());
            boxItem.addSeparator();

            boxItem.add(createMenuItemTranslated("fetchlist", "fetchList-" + boxName).getMenuItem());
            boxItem.add(createMenuItemTranslated("renew_ip", "renewIP-" + boxName).getMenuItem());
            boxItem.add(createMenuItemTranslated("reboot", "reboot-" + boxName).getMenuItem());
        }
        return boxItem;
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
        return tray != null && tray.isSupported();
    }
}
