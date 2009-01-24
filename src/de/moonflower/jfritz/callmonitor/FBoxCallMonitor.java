package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public abstract class FBoxCallMonitor extends Thread implements CallMonitorInterface {

    // Liest den TCP-Strom ein
    protected BufferedReader in;

    protected Socket clientSocket;

    private boolean running;

    // wird benutzt, um X Sekunden lang zu warten
    protected Random zufallszahl;

    private boolean connected = false;

    private long exceptionTimestamp = 0;

    private JEditorPane errorPane;

    public FBoxCallMonitor() {
        super("FBoxThread");
        Debug.msg("Starting FBoxListener"); //$NON-NLS-1$
        this.setDaemon(true);
        start();
        zufallszahl = new Random();
    }

    public abstract void run();

    protected boolean connect() {
        try {
            Debug.msg("Trying to connect to " //$NON-NLS-1$
                    + JFritz.getFritzBox().getAddress() + ":1012"); //$NON-NLS-1$,  //$NON-NLS-2$
            running = true;
            clientSocket = new Socket(JFritz.getFritzBox().getAddress(), 1012); //$NON-NLS-1$
            clientSocket.setKeepAlive(true);
    		JFritz.getJframe().setCallMonitorConnectedStatus();
            connected = true;
            return true;
        } catch (UnknownHostException uhe) {
            Debug.msg("Unknown host exception: " + uhe.toString()); //$NON-NLS-1$
            closeConnection();
        	if ( exceptionTimestamp == 0)
        	{
        		exceptionTimestamp = System.currentTimeMillis();
        		JFritz.getJframe().setCallMonitorDisconnectedStatus();
        	}
        	if ( System.currentTimeMillis() > exceptionTimestamp + 180*1000)
        	{
	            Debug.errDlg(Main.getMessage("error_fritzbox_callmonitor_no_connection"). //$NON-NLS-1$
	            		replaceAll("%A", JFritz.getFritzBox().getAddress()).
	            		replaceAll("%LINK_TO_MANUAL%", "http://www.jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box-Anrufmonitor")); //$NON-NLS-1$,  //$NON-NLS-2$
	            exceptionTimestamp = 0;
        	}
        } catch (IOException ioe) {
            Debug.msg("IO exception: " + ioe.toString()); //$NON-NLS-1$
            closeConnection();
        	if ( exceptionTimestamp == 0)
        	{
        		exceptionTimestamp = System.currentTimeMillis();
        		JFritz.getJframe().setCallMonitorDisconnectedStatus();
        	}
        	if ( System.currentTimeMillis() > exceptionTimestamp + 180*1000)
        	{
        		String errStr = Main.getMessage("error_fritzbox_callmonitor_no_connection"). //$NON-NLS-1$
        							replaceAll("%A", JFritz.getFritzBox().getAddress()).
        							replaceAll("%LINK_TO_MANUAL%", "http://www.jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box-Anrufmonitor"); //$NON-NLS-1$,  //$NON-NLS-2$

        		Debug.err(errStr);

        		errorPane = new JEditorPane();
        		errorPane.setContentType("text/html");
        		errorPane.setEditable(false);
        		errorPane.setOpaque(false);
        		errorPane.addHyperlinkListener(new HyperlinkListener() {
        			private String tooltip;

        			public void hyperlinkUpdate(HyperlinkEvent hle) {
        				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
        					BrowserLaunch.openURL(hle.getURL().toString());
        				}
                        else if (hle.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                            tooltip = errorPane.getToolTipText();
                            errorPane.setToolTipText(Main.getMessage("show_on_google_maps"));
                        } else if (hle.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        	errorPane.setToolTipText(tooltip);
                        }
        			}
        		});
        		errorPane.setText("<html>" + errStr + "</html>");

//        		JOptionPane.showMessageDialog(null, errorPane,
//        				Main.getMessage("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

        		Debug.errDlg(Main.getMessage("error_fritzbox_callmonitor_no_connection"). //$NON-NLS-1$
	            		replaceAll("%A", JFritz.getFritzBox().getAddress()).
	            		replaceAll("%LINK_TO_MANUAL%", "http://www.jfritz.org/wiki/JFritz_Handbuch:Deutsch#FRITZ.21Box-Anrufmonitor")); //$NON-NLS-1$,  //$NON-NLS-2$
	            exceptionTimestamp = 0;
        	}
        }
        connected = false;
        return false;
    }

    protected void readOutput() {
    	int failedConnectionCounter = 0;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket
                    .getInputStream()));
            String currentLine;
            while (running) {
                // lese nächste Nachricht ein
            	while (!in.ready() && running)
            	{
					if ( running && !connected )
					{
						if (failedConnectionCounter==0)
						{
							Debug.msg("Detected connection interruption. Reconnecting...");
							JFritz.getJframe().setCallMonitorDisconnectedStatus();
						}
						failedConnectionCounter++;

            			if (failedConnectionCounter % 20 == 0)
            			{
							connected = connect();
		                    in = new BufferedReader(new InputStreamReader(clientSocket
		                            .getInputStream()));
		                    if (connected)
		                    {
		                    	failedConnectionCounter=0;
		                    }
            			}
					}
            		try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						connected = false;
			        	Thread.currentThread().interrupt();
					}
            	}

            	if (running && connected)
            	{
	                currentLine = in.readLine();
	                parseOutput(currentLine);
            	}
            }
        } catch (IOException ioe) {
            Debug.msg("IO exception: " + ioe.toString()); //$NON-NLS-1$
            connected = false;
        }
        Debug.msg("Callmonitor Thread stopped");
    }

    protected abstract void parseOutput(String line);

    public void stopCallMonitor() {
        Debug.msg("Stopping FBoxListener"); //$NON-NLS-1$
    	closeConnection();
        running = false;
    }

    public void closeConnection()
    {
        Debug.msg("Closing connection"); //$NON-NLS-1$
        try {
            if (clientSocket != null)
                clientSocket.close();
            connected = false;
//            this.interrupt();
        } catch (IOException e) {
            Debug.err(e.toString());
        }
    }

    public boolean isConnected()
    {
    	return connected;
    }

    public boolean isRunning()
    {
    	return running;
    }

    public boolean pingBox()
    {
    	boolean pingSucceeded = false;
	    try {
	    	if (clientSocket != null)
	    	{
	    		pingSucceeded = clientSocket.getInetAddress().isReachable(2000);
	    	}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return pingSucceeded;
    }
}