package de.moonflower.jfritz.tray;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

/**
 * Ermittel anhand der zur Verf�gung stehenden Gr��e in der Statusleiste des
 * Betriebssystems die optimale Gr��e und erstellt ein TrayIcon.
 */
public class JTrayImage
{
	private final static Logger log = Logger.getLogger(JTrayImage.class);

    private TrayIcon trayIcon;

    /**
     * Erstellt ein TrayIcon.
     *
     * @return TrayIcon
     * @throws java.io.IOException
     */
    public TrayIcon setIcon() throws IOException
    {
        try
        {
            Dimension trayIconSize = getTrayIconSize();
            BufferedImage trayIconImage = null;

            //Sonderregel für Linux mit Gnome (braucht nicht konvertiert werden)
            if(trayIconSize.width == 24 & trayIconSize.height <= 24)
            {
                trayIconImage = ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray24.png"));
            }
            else if(trayIconSize.height > 16 && trayIconSize.height <= 22)
            {
                trayIconImage = scaleIconToBufferedImage(ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray22.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
            }
            else if(trayIconSize.height > 22 && trayIconSize.height <= 32)
            {
                trayIconImage = scaleIconToBufferedImage(ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray32.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
            }
            else if(trayIconSize.height > 32 && trayIconSize.height <= 64)
            {
                trayIconImage = scaleIconToBufferedImage(ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray64.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
            }
            else if(trayIconSize.height > 64 && trayIconSize.height <= 128)
            {
                trayIconImage = scaleIconToBufferedImage(ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray128.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
            }
            else
            {
                trayIconImage = ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray16.png"));
            }

            trayIcon = new TrayIcon(trayIconImage);
        }

        catch(Exception exp)
        {
            log.error("Fehlerhaftes TrayIcon");
            trayIcon = new TrayIcon(ImageIO.read(getClass().getResource("/de/moonflower/jfritz/resources/images/tray16.png")));
        }

        return trayIcon;
    }

    /**
     * Holt sich über die SystemTray Klasse die aktuell zur Verfügung stehende
     * Größe für das TrayIcon.
     *
     * @return Dimension Die zur verfügung stehende Größe
     */
    public Dimension getTrayIconSize()
    {
        try
        {
            Class<?> clazz = Class.forName("java.awt.SystemTray");

            if(clazz != null)
            {
                Object o = clazz.getMethod("getSystemTray",new Class[] {}).invoke(clazz,new Object[] {});
                return (Dimension)clazz.getMethod("getTrayIconSize", new Class[] {}).invoke(o, new Object[] {});
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Skalliert ein Image auf eine fest definierte Größe.
     * Scales an image to a specific size and returns an BufferedImage
     *
     * @param img Ursprungsbild
     * @param x maximale Breite
     * @param y maximale Höhe
     *
     * @return skalliertes BufferedImage
     */
    public static BufferedImage scaleIconToBufferedImage(BufferedImage img, int x, int y)
    {
        return scaleIconToBufferedImage(img, x, y, img.getType());
    }

    /**
   * Scales an image to a specific size and returns an BufferedImage
   *
   * @param img
   *          Scale this IMage
   * @param x
   *          new X-Value
   * @param y
   *          new Y-Value
   * @param type The type of the image.
   * @return Scaled BufferedImage
   *
   * @since 2.7
   */
    public static BufferedImage scaleIconToBufferedImage(BufferedImage img, int x, int y, int type)
    {
        Image image = img.getScaledInstance(x, y, Image.SCALE_SMOOTH);

        BufferedImage im = new BufferedImage(x, y, type);

        Graphics2D g2 = im.createGraphics();
        g2.drawImage(image, null, null);
        g2.dispose();

        im.flush();
        return im;
    }
}