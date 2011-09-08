package de.moonflower.jfritz.callmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.utils.Debug;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public abstract class FBoxCallMonitor extends Thread implements CallMonitorInterface {
	private static final Logger log = Logger.getLogger(FBoxCallMonitor.class);

	private static final int CONNECTION_TIMEOUT = 1000;

	private static final int READ_TIMEOUT = 15000; //15000;

	private static final int WAIT_UNTIL_RECONNECT = 10000; //15000;

    // Liest den TCP-Strom ein
    protected BufferedReader in;

    protected Socket clientSocket;

    private boolean running;

    private int connectionCount = 0;

    // wird benutzt, um X Sekunden lang zu warten
    protected Random zufallszahl;

    private boolean connected = false;

    protected FritzBox fritzBox;

    private Vector<CallMonitorStatusListener> stateListener;

    public FBoxCallMonitor(FritzBox fritzBox,
    		Vector<CallMonitorStatusListener> stateListener, boolean shouldConnect) {
        super("FBoxThread");
        this.stateListener = stateListener;
        this.fritzBox = fritzBox;
	    zufallszahl = new Random();
	    this.setDaemon(true);
        if (shouldConnect) {
		    log.info("Starting FBoxListener"); //$NON-NLS-1$
		    running = true;
		    start();
		    log.info("Trying to connect to " //$NON-NLS-1$
		    		+ fritzBox.getAddress() + ":1012"); //$NON-NLS-1$,  //$NON-NLS-2$
        }
    }

    public abstract void run();

    protected boolean connect() {
    	try {
    		log.debug("Connecting call monitor ... " + ++connectionCount);
	        clientSocket = new Socket(fritzBox.getAddress(), 1012); //$NON-NLS-1$
			clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
			connected = true;
			clientSocket.setSoTimeout(READ_TIMEOUT);
            in = new BufferedReader(new InputStreamReader(clientSocket
                    .getInputStream()));
			this.setConnectedStatus();
			return true;
    	} catch (SocketTimeoutException stoe) {
	        log.error("Socket connect timeout: " + stoe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} catch (NoRouteToHostException nrthe) {
	        log.error("No route to host exception: " + nrthe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (UnknownHostException uhe) {
	        log.error("Unknown host exception: " + uhe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (ConnectException ce) {
	        log.error("Connect exception: " + ce.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (IOException ioe) {
			try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e1) {
				System.err.println("Thread interrupted while sleeping!");
				e1.printStackTrace();
			}
	    }
	    return false;
    }

    protected void readOutput() {
    	boolean timeout = true;
    	try {
        	log.debug("Reading call monitor input ... ");
            String currentLine;
            // lese nächste Nachricht ein
        	if (running && connected)
        	{
                currentLine = in.readLine();
                if (currentLine != null)
                {
                	timeout = false;
                    parseOutput(currentLine);
                }
                else
                {
                	connected = false;
                	this.setDisconnectedStatus();
                	in.close();
                }
        	}
        } catch (IOException ioe) {
            connected = false;
            this.setDisconnectedStatus();
        }
        if (timeout)
        {
	        try {
	        	if (clientSocket != null) {
	        		clientSocket.close();
	        	}
	        	if (in != null) {
	        		in.close();
	        	}
	        	log.debug("Closed input stream of call monitor! " + connectionCount);
	        }
	        catch (IOException ioe)
	        {
	        	log.error("IOException while closing call monitor input!");
	        }
        }
    }

    protected abstract void parseOutput(String line);

    public void stopCallMonitor() {
        log.info("Stopping FBoxListener"); //$NON-NLS-1$
    	closeConnection();
        running = false;
    }

    public void closeConnection()
    {
        log.debug("Closing connection " + connectionCount); //$NON-NLS-1$
        try {
            connected = false;
            if (clientSocket != null)
            {
                clientSocket.close();
            }
//            this.interrupt();
        } catch (IOException e) {
            Debug.error(e.toString());
        }
        try {
        	if (in != null)
        	{
        		in.close();
        	}
	    } catch (IOException e) {
	        Debug.error(e.toString());
	    }
	    this.setDisconnectedStatus();
    }

    public boolean isConnected()
    {
    	return connected;
    }

    public boolean isRunning()
    {
    	return running;
    }

	private void setConnectedStatus()
	{
		if (stateListener != null)
		{
			for (int i=0; i<stateListener.size(); i++)
			{
				stateListener.get(i).setConnectedStatus(fritzBox.getName());
			}
		}
	}

	private void setDisconnectedStatus()
	{
		if (stateListener != null)
		{
			for (int i=0; i<stateListener.size(); i++)
			{
				stateListener.get(i).setDisconnectedStatus(fritzBox.getName());
			}
		}
	}
}