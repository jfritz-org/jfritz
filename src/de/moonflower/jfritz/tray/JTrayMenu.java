//package de.moonflower.jfritz.tray;
//
//import javax.swing.JMenu;
//import javax.swing.JMenuItem;
//import javax.swing.JPopupMenu;
//import javax.swing.JSeparator;
//
///**
// * Ein PopupMenu auf Swingbasis für das JTrayIcon.
// */
//public class JTrayMenu
//{
//    private JPopupMenu popupMenu;
//    private JMenu menu;
//    private JMenuItem menuItem;
//
//    /**
//     * Erstellt ein PopupMenu.
//     *
//     * @param uiListener UIListener
//     *
//     * @return popupMenu
//     */
//    public JPopupMenu getPopupMenu(UIListener uiListener)
//    {
//        popupMenu = new JPopupMenu();
//
//        //Import
//        menuItem = new JMenuItem("Messwerte importieren");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/import16.png"))); // NOI18N
//        menuItem.setActionCommand("import");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        //Separator
//        popupMenu.add(new JSeparator());
//
//        //Regelkarten
//        menu = new JMenu("Regelkarten");
//
//            menuItem = new JMenuItem("Mittelwertkarte (xQuer)");
//            menuItem.setActionCommand("xQuer");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            menuItem = new JMenuItem("Mediankarte (xTilde)");
//            menuItem.setActionCommand("xTilde");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            menuItem = new JMenuItem("Standardabweichungskarte (s)");
//            menuItem.setActionCommand("s");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            menuItem = new JMenuItem("Spannweitenkarte (R)");
//            menuItem.setActionCommand("R");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            menuItem = new JMenuItem("Urwertekarte (x)");
//            menuItem.setActionCommand("x");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            //Separator
//            menu.add(new JSeparator());
//
//            menuItem = new JMenuItem("xQuer/s-Regelkarte");
//            menuItem.setActionCommand("xQuers");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            menuItem = new JMenuItem("xTilde/R-Regelkarte");
//            menuItem.setActionCommand("xTildeR");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//            //Separator
//            menu.add(new JSeparator());
//
//            menuItem = new JMenuItem("Individuelle Kombination");
//            menuItem.setActionCommand("chartCombChoise");
//            menuItem.addActionListener(uiListener);
//            menu.add(menuItem);
//
//        popupMenu.add(menu);
//
//        //Eventliste
//        menuItem = new JMenuItem("Eventliste");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/eventlist16.png"))); // NOI18N
//        menuItem.setActionCommand("eventList");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        //Sichprobenauswertung
//        menuItem = new JMenuItem("Sichprobenauswertung");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/mathvalue16.png"))); // NOI18N
//        menuItem.setActionCommand("mathList");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        //Histogramm
//        menuItem = new JMenuItem("Histogramm");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/histogramm16.png"))); // NOI18N
//        menuItem.setActionCommand("histogramm");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        //Separator
//        popupMenu.add(new JSeparator());
//
//        //Sichprobenauswertung
//        menuItem = new JMenuItem("Einstellungen");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/config16.png"))); // NOI18N
//        menuItem.setActionCommand("config");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        //Separator
//        popupMenu.add(new JSeparator());
//
//        //Sichprobenauswertung
//        menuItem = new JMenuItem("QuaRK beenden");
//        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/quark/img/16/exit16.png"))); // NOI18N
//        menuItem.setActionCommand("exit");
//        menuItem.addActionListener(uiListener);
//        popupMenu.add(menuItem);
//
//        return popupMenu;
//    }
//}