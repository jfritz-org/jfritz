package de.moonflower.jfritz.tray;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.utils.Debug;

/**
 * Ein Swing TrayIcon welches die regulären AWT-Komponenten überschreibt und
 * durch neue Swing-Komponenten ersetzt.
 *
 * QUELLE: http://www.java-forum.org/awt-swing-swt/82465-abstract-windowing-toolkit-trayicon-ubuntu.html
 */
public class SwingTray implements Tray {
	private final static Logger log = Logger.getLogger(SwingTray.class);

	private JPopupMenu popupMenu;
    private JDialog trayParent;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private ActionListener leftClickListener, leftDoubleClickListener, rightClickListener;

    /**
     * Konstruktor
     * Erzeugt eine Dialogbox für das PopupMenu, einPopupMenu, das TrayIcon
     * und erstellt das finale SystemTray.
     *
     * @see quark.ui.tray.JTrayMenu
     * @see quark.ui.tray.JTrayImage
     */
    public SwingTray()
    {
        try
        {
            trayIcon = new JTrayImage().setIcon();
        }
        catch (IOException ex)
        {
        	log.error(ex.toString());
        }

        //ClassCastException fix
        Toolkit.getDefaultToolkit().getSystemEventQueue().push( new PopupFixQueue(popupMenu) );
    }

    /**
     * Setzt ein PopupMenu in die Dialogbox und überschreibt die aktuellen
     * Listenerklassen.
     *
     * @param trayMenu JPopupMenu
     */
    private void setTrayPopUp(JPopupMenu trayMenu)
    {
        trayParent = new JDialog();
        trayParent.setSize(0, 0);
        trayParent.setUndecorated(true);
        trayParent.setAlwaysOnTop(true);
        trayParent.setVisible(false);

        this.popupMenu = trayMenu;

        popupMenu.addPopupMenuListener(new PopupMenuListener()
        {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                trayParent.setVisible(false);
            }

            public void popupMenuCanceled(PopupMenuEvent e)
            {
                trayParent.setVisible(false);
            }
        });
//        popupMenu.setVisible(true);
//        popupMenu.setVisible(false);

        //Listener registrieren
        trayIcon.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    if (e.getClickCount() == 1 && leftClickListener != null)
                        leftClickListener.actionPerformed(null);
                    else if (e.getClickCount() == 2 && leftDoubleClickListener != null)
                        leftDoubleClickListener.actionPerformed(null);
                }

                else if (SwingUtilities.isRightMouseButton(e))
                {
                    if (e.getClickCount() == 1 && rightClickListener != null)
                        rightClickListener.actionPerformed(null);
                    showPopup(e.getPoint());
                }
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    if (SwingUtilities.isRightMouseButton(e) && rightClickListener != null)
                        rightClickListener.actionPerformed(null);
                    showPopup(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    if (SwingUtilities.isRightMouseButton(e) && rightClickListener != null)
                        rightClickListener.actionPerformed(null);
                    showPopup(e.getPoint());
                }
            }
        });
    }

    /**
     * Methode zum zeigen des PopupMenus.
     *
     * @param p Position des Listeners
     */
    private void showPopup(final Point p)
    {
        trayParent.setVisible(true);
        trayParent.toFront();

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Point p2 = computeDisplayPoint(p.x, p.y, popupMenu.getPreferredSize());
                popupMenu.show(trayParent, p2.x - trayParent.getLocation().x, p2.y - trayParent.getLocation().y);
            };
        });
    }

    /**
     * Berechnet die optimale Position für die Anzeige des PopupMenus.
     */
    private Point computeDisplayPoint(int x, int y, Dimension dim)
    {
        if (x - dim.width > 0)
            x -= dim.width;
        if (y - dim.height > 0)
            y -= dim.height;
        return new Point(x, y);
    }

    //Sonstiges
    //==========================================================================

    /**
     * ClassCastException FIX
     */
    private class PopupFixQueue extends EventQueue
    {
        private JPopupMenu popup;

        public PopupFixQueue(JPopupMenu popup)
        {
            this.popup = popup;
        }

        protected void dispatchEvent(AWTEvent event)
        {
            try
            {
                super.dispatchEvent(event);
            }
            catch (Exception ex)
            {
                if (event.getSource() instanceof TrayIcon)
                {
                    popup.setVisible(false);
                }
            }
        }
    }

	public void add(ImageIcon i) {
        tray = SystemTray.getSystemTray();
        try
        {
            tray.add(trayIcon);
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }
	}

	public void addActionListener(ClickListener l) {
		if (l.getClickType() == ClickListener.CLICK_LEFT) {
			if (l.getClickCount() == ClickListener.CLICK_COUNT_SINGLE)
			{
				leftClickListener = l;
			} else if (l.getClickCount() == ClickListener.CLICK_COUNT_DOUBLE)
			{
				leftDoubleClickListener = l;
			}
		} else if (l.getClickType() == ClickListener.CLICK_RIGHT)
		{
			if (l.getClickCount() == ClickListener.CLICK_COUNT_SINGLE)
			{
				rightClickListener = l;
			}
		}
	}

	public void addMouseListener(MouseListener l) {
	}

	public void displayMessage(String caption, String message, int type) {
		TrayIcon.MessageType tp = TrayIcon.MessageType.NONE;
		switch (type)
		{
			case MESSAGE_TYPE_ERROR: tp=TrayIcon.MessageType.ERROR; break;
			case MESSAGE_TYPE_INFO: tp=TrayIcon.MessageType.INFO; break;
			case MESSAGE_TYPE_NONE: tp=TrayIcon.MessageType.NONE; break;
			case MESSAGE_TYPE_WARNING: tp=TrayIcon.MessageType.WARNING; break;

		}
		trayIcon.displayMessage(caption, message, tp);
	}

	public boolean isSupported() {
		return SystemTray.isSupported();
	}

	public void remove() {
		trayParent.dispose();
		trayParent = null;
		tray.remove(trayIcon);
	}

	public void setImage(ImageIcon i) {
	}

	public void setPopupMenu(TrayMenu m) {
        //PopupMenu erstellen
        popupMenu = m.getJPopupMenu();
        setTrayPopUp(popupMenu);
	}

	public void setTooltip(String s) {
		trayIcon.setToolTip(s);
	}

	public void clearActionListeners() {
		leftClickListener = null;
		leftDoubleClickListener = null;
	}
}
