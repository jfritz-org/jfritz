package de.moonflower.jfritz.simpletests;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.TimerTask;

public class WindowStateDemo extends JPanel
                             implements WindowListener,
                                        WindowFocusListener,
                                        WindowStateListener {
	private static final long serialVersionUID = 4552464584969809514L;
	final static String newline = "\n";
    final static String space = "    ";
    static JFrame frame;
    static ShowHideFrame sFrame;
    JTextArea display;
    static int oldState;
    static boolean minimizedByGui = false;

    class ShowHideFrame extends TimerTask
    {
    	JFrame frame;
    	public ShowHideFrame(JFrame frame)
    	{
    		this.frame = frame;
    	}

    	public void run()
    	{
	    	if (frame.isVisible())
	    	{
    			frame.setVisible(false); // hide frame
	    	}
	    	else
	    	{
	        	frame.setVisible(true);
	    	}
    	}
    }

    public WindowStateDemo() {
        super(new BorderLayout());
        display = new JTextArea();
        display.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(display);
        scrollPane.setPreferredSize(new Dimension(500, 450));
        add(scrollPane, BorderLayout.CENTER);

        frame.addWindowListener(this);
        frame.addWindowFocusListener(this);
        frame.addWindowStateListener(this);
        sFrame = new ShowHideFrame(frame);

        checkWM();

        // adding tray:
        final TrayIcon trayIcon;
        if (SystemTray.isSupported())
        {
        	SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("tray.gif");
            MouseListener mouseListener = new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                	showHide(true);
                }

                public void mouseEntered(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse entered!");
                }

                public void mouseExited(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse exited!");
                }

                public void mousePressed(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse pressed!");
                }

                public void mouseReleased(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse released!");
                }
            };

            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };
            PopupMenu popup = new PopupMenu();
            MenuItem menuItem = new MenuItem("Exit");
            menuItem.addActionListener(exitListener);
            popup.add(menuItem);

            trayIcon = new TrayIcon(image, "Tray Demo", popup);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                    trayIcon.displayMessage("Action Event",
//                        "An Action Event Has Been Performed!",
//                        TrayIcon.MessageType.INFO);
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        }
    }

    private static void hideFrame()
    {
		oldState = frame.getExtendedState();
		System.out.println("Old state: " + oldState);
		frame.setVisible(false);
		frame.setState(Frame.ICONIFIED);
		frame.setState(Frame.NORMAL);
		frame.setState(Frame.ICONIFIED);
    }

    private static void showFrame(boolean b)
    {
		System.out.println("Restoring state: " + oldState);
		frame.setState(oldState);
		frame.setVisible(true);
		frame.setState(Frame.ICONIFIED);
		frame.setVisible(false);
		frame.setState(oldState);
		frame.setVisible(true);
    }

    private void showHide(boolean tray)
    {
    	java.util.Timer timer = new java.util.Timer();
    	timer.schedule(new ShowHideFrame(frame), 50);
    }

    //Some window managers don't support all window states.
    //For example, dtwm doesn't support true maximization,
    //but mimics it by resizing the window to be the size
    //of the screen.  In this case the window does not fire
    //the MAXIMIZED_ constants on the window's state listener.
    //Microsoft Windows supports MAXIMIZED_BOTH, but not
    //MAXIMIZED_VERT or MAXIMIZED_HORIZ.
    public void checkWM() {
        Toolkit tk = frame.getToolkit();
        if (!(tk.isFrameStateSupported(Frame.ICONIFIED))) {
            displayMessage(
               "Your window manager doesn't support ICONIFIED.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_VERT))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_VERT.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_HORIZ))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_HORIZ.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_BOTH.");
        } else {
            displayMessage(
               "Your window manager supports MAXIMIZED_BOTH.");
        }
    }

    public void windowClosing(WindowEvent e) {
    	displayMessage("WindowListener method called: windowClosing.");

        //A pause so user can see the message before
        //the window actually closes.
        ActionListener task = new ActionListener() {
            boolean alreadyDisposed = false;
            public void actionPerformed(ActionEvent e) {
                if (!alreadyDisposed) {
                    alreadyDisposed = true;
                    frame.dispose();
                } else { //make sure the program exits
                    System.exit(0);
                }
            }
        };
        Timer timer = new Timer(500, task); //fire every half second
        timer.setInitialDelay(2000);        //first delay 2 seconds
        timer.start();
    }

    public void windowClosed(WindowEvent e) {
        //This will only be seen on standard output.
//        displayMessage("WindowListener method called: windowClosed.");
    }

    public void windowOpened(WindowEvent e) {
//        displayMessage("WindowListener method called: windowOpened.");
    }

    public void windowIconified(WindowEvent e) {
//    	frame.setVisible(false);
    	frame.setExtendedState(Frame.NORMAL);
    	showHide(false);
//        displayMessage("WindowListener method called: windowIconified.");
    }

    public void windowDeiconified(WindowEvent e) {
//        displayMessage("WindowListener method called: windowDeiconified.");
    }

    public void windowActivated(WindowEvent e) {
//        displayMessage("WindowListener method called: windowActivated.");
    }

    public void windowDeactivated(WindowEvent e) {
//        displayMessage("WindowListener method called: windowDeactivated.");
    }

    public void windowGainedFocus(WindowEvent e) {
//        displayMessage("WindowFocusListener method called: windowGainedFocus.");
    }

    public void windowLostFocus(WindowEvent e) {
//        displayMessage("WindowFocusListener method called: windowLostFocus.");
    }

    public void windowStateChanged(WindowEvent e) {
        displayStateMessage(
          "WindowStateListener method called: windowStateChanged.", e);
    }

    void displayMessage(String msg) {
        display.append(msg + newline);
        System.out.println(msg);
    }

    void displayStateMessage(String prefix, WindowEvent e) {
        int state = e.getNewState();
        int oldState = e.getOldState();
        String msg = prefix
                   + newline + space
                   + "New state: "
                   + convertStateToString(state)
                   + newline + space
                   + "Old state: "
                   + convertStateToString(oldState);
        display.append(msg + newline);
        System.out.println(msg);
    }

    String convertStateToString(int state) {
        if (state == Frame.NORMAL) {
            return "NORMAL";
        }
        if ((state & Frame.ICONIFIED) != 0) {
            return "ICONIFIED";
        }
        //MAXIMIZED_BOTH is a concatenation of two bits, so
        //we need to test for an exact match.
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            return "MAXIMIZED_BOTH";
        }
        if ((state & Frame.MAXIMIZED_VERT) != 0) {
            return "MAXIMIZED_VERT";
        }
        if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
            return "MAXIMIZED_HORIZ";
        }
        return "UNKNOWN";
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("WindowEventDemo");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new WindowStateDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}