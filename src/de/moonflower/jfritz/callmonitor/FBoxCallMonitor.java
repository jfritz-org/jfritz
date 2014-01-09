package de.moonflower.jfritz.callmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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

	private static final int READ_TIMEOUT = 5000;

	private static final int WAIT_UNTIL_RECONNECT = 0; //15000;

    // Liest den TCP-Strom ein
    protected BufferedReader in;

    protected Socket clientSocket;

    private boolean running;

    private int connectionCount = 0;

    private boolean connected = false;

    protected FritzBox fritzBox;

    private Vector<CallMonitorStatusListener> stateListener;

    public FBoxCallMonitor(FritzBox fritzBox,
    		Vector<CallMonitorStatusListener> stateListener, boolean shouldConnect) {
        super("FBoxThread");
        //log.setLevel(Level.DEBUG);
        this.stateListener = stateListener;
        this.fritzBox = fritzBox;
	    this.setDaemon(true);
        if (shouldConnect) {
		    log.info("(CM) Starting FBoxListener"); //$NON-NLS-1$
		    running = true;
		    start();
		    log.info("(CM) Trying to connect to " //$NON-NLS-1$
		    		+ fritzBox.getAddress() + ":1012"); //$NON-NLS-1$,  //$NON-NLS-2$
        }
    }

    public abstract void run();

    protected boolean connect() {
    	try {
    		log.info("(CM) [" + ++connectionCount + "] Connecting call monitor ... ");
	        clientSocket = new Socket();
	        clientSocket.connect(new InetSocketAddress(fritzBox.getAddress(), 1012), CONNECTION_TIMEOUT);
			connected = true;
			clientSocket.setSoTimeout(READ_TIMEOUT);
			try {
				clientSocket.setKeepAlive(true);
			} catch (Exception e) {
		        log.error("(CM) [" + connectionCount + "] Could not set keep-alive: " + e.toString()); //$NON-NLS-1$
			}
            in = new BufferedReader(new InputStreamReader(clientSocket
                    .getInputStream()));
			this.setConnectedStatus();
    		log.info("(CM) [" + connectionCount + "] Connection to call monitor established!");
			return true;
    	} catch (SocketTimeoutException stoe) {
	        log.error("(CM) [" + connectionCount + "] Socket connect timeout: " + stoe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} catch (NoRouteToHostException nrthe) {
	        log.error("(CM) [" + connectionCount + "] No route to host exception: " + nrthe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (UnknownHostException uhe) {
	        log.error("(CM) [" + connectionCount + "] Unknown host exception: " + uhe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (ConnectException ce) {
	        log.error("(CM) [" + connectionCount + "] Connect exception: " + ce.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (IOException ioe) {
	        log.error("(CM) [" + connectionCount + "] IO exception: " + ioe.toString()); //$NON-NLS-1$
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
    	try {
        	log.debug("(CM) [" + connectionCount + "] Reading call monitor input ... ");
            String currentLine;
            // lese nächste Nachricht ein
        	if (running && connected)
        	{
                currentLine = in.readLine();
                if (currentLine != null)
                {
                    parseOutput(currentLine);
                }
                else
                {
                	log.debug("(CM) [" + connectionCount + "] Connection lost! ");
                	connected = false;
                	this.setDisconnectedStatus();
                	closeAm();
                }
        	}
    	} catch (SocketTimeoutException ste) {
        	log.debug("(CM) [" + connectionCount + "] Read timeout, just proceed");
        	try {
				clientSocket.sendUrgentData(0);
			} catch (Exception e) {
				log.warn("(CM) [" + connectionCount + "] Exception when sending urgent data: " + e.getMessage());
			}
        } catch (IOException ioe) {
        	log.warn("(CM) [" + connectionCount + "] IOException: " + ioe.getMessage());
            connected = false;
            this.setDisconnectedStatus();
        	closeAm();
        }
    }
    
    private void closeAm() {
        try {
        	if (clientSocket != null) {
        		clientSocket.close();
        	}
        	if (in != null) {
        		in.close();
        	}
        	log.info("(CM) [" + connectionCount + "] Closed input stream of call monitor! ");
        }
        catch (IOException ioe)
        {
        	log.error("(CM) [" + connectionCount + "] IOException while closing call monitor input!");
        }
    }

    protected abstract void parseOutput(String line);

    public void stopCallMonitor() {
        log.info("(CM) [" + connectionCount + "] Stopping FBoxListener"); //$NON-NLS-1$
    	closeConnection();
        running = false;
    }

    public void closeConnection()
    {
        log.info("(CM) [" + connectionCount + "] Closing connection "); //$NON-NLS-1$
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