package de.moonflower.jfritz.callmonitor;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Thread. Connects to FritzBox Port 1012. Captures Callermessages.
 *
 * @author Robert Palmer
 *
 */

public abstract class FBoxCallMonitor extends Thread implements CallMonitorInterface {

	private static final int CONNECTION_TIMEOUT = 1000; //15000

	private static final int READ_TIMEOUT = 15000; //15000;

	private static final int WAIT_UNTIL_RECONNECT = 10000; //15000;

    // Liest den TCP-Strom ein
    protected BufferedReader in;

    protected Socket clientSocket;

    private boolean running;

    // wird benutzt, um X Sekunden lang zu warten
    protected Random zufallszahl;

    private boolean connected = false;

    public FBoxCallMonitor() {
        super("FBoxThread");
        Debug.msg("Starting FBoxListener"); //$NON-NLS-1$
        this.setDaemon(true);
        running = true;
        start();
        zufallszahl = new Random();
        Debug.msg("Trying to connect to " //$NON-NLS-1$
                + JFritz.getFritzBox().getAddress() + ":1012"); //$NON-NLS-1$,  //$NON-NLS-2$
    }

    public abstract void run();

    protected boolean connect() {
    	try {
//    		Debug.msg("Connecting call monitor ... ");
	        clientSocket = new Socket(JFritz.getFritzBox().getAddress(), 1012); //$NON-NLS-1$
	        clientSocket.setKeepAlive(true);
			JFritz.getJframe().setCallMonitorConnectedStatus();
			clientSocket.setSoTimeout(READ_TIMEOUT);
			connected = true;
			return true;
    	} catch (SocketTimeoutException stoe) {
	        Debug.msg("Socket connect timeout: " + stoe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} catch (NoRouteToHostException nrthe) {
	        Debug.msg("No route to host exception: " + nrthe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (UnknownHostException uhe) {
	        Debug.msg("Unknown host exception: " + uhe.toString()); //$NON-NLS-1$
	        closeConnection();
	        try {
				Thread.sleep(WAIT_UNTIL_RECONNECT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } catch (ConnectException ce) {
	        Debug.msg("Connect exception: " + ce.toString()); //$NON-NLS-1$
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
        try {
//        	Debug.msg("Reading call monitor input ... ");
            in = new BufferedReader(new InputStreamReader(clientSocket
                    .getInputStream()));
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
                	connected = false;
                	in.close();
                }
        	}
        } catch (IOException ioe) {
            connected = false;
        }
        try {
        	clientSocket.close();
        	in.close();
//        	Debug.msg("Closed input stream of call monitor!");
        }
        catch (IOException ioe)
        {
        	Debug.err("IOException while closing call monitor input!");
        }
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
            connected = false;
            if (clientSocket != null)
            {
                clientSocket.close();
            }
//            this.interrupt();
        } catch (IOException e) {
            Debug.err(e.toString());
        }
        try {
        	in.close();
	    } catch (IOException e) {
	        Debug.err(e.toString());
	    }
		JFritz.getJframe().setCallMonitorDisconnectedStatus();
    }

    public boolean isConnected()
    {
    	return connected;
    }

    public boolean isRunning()
    {
    	return running;
    }
}