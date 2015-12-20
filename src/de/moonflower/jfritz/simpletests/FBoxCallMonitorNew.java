package de.moonflower.jfritz.simpletests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FBoxCallMonitorNew extends Thread {

	private Socket socket;

	private BufferedReader in;
	private BufferedWriter out;

	FBoxCallMonitorNew()
	{
	}

	/**
	 *
	 * @return current Time HH:mm:ss
	 */
	private static String getCurrentTime() {
		Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS"); //$NON-NLS-1$
		return df.format(now);
	}

	public void run()
	{
		boolean connected = false;
		while (true) // endless loop
		{
			try {
				if (!connected)
				{
					socket = new Socket();
					socket.bind(null);
					try {
						socket.connect(new InetSocketAddress("192.168.1.4", 1012), 500);
					} catch (IOException e)
					{
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							System.err.println("Thread interrupted while sleeping!");
							e1.printStackTrace();
						}
						System.err.println(getCurrentTime() + " Connection timed out. Set symbol to DISCONNECTED.");
					}
					socket.setSoTimeout(1000);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					connected = true;
					System.out.println(getCurrentTime() + " Connected. Set symbol to CONNECTED.");
				}
				else
				{
					String line = in.readLine();
					System.out.println(line);
				}
			} catch (IOException e) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					in.close();
					out.close();
					socket.close();

					//TODO: probiere, ob mit setReuseAddress() und einem korrektem Bind die zustzlich
					      //geffneten Ports unterdrckt werden knnen.

				} catch (IOException e1) {
					System.err.println("Could not close socket.");
					// TODO Automatisch erstellter Catch-Block
					e1.printStackTrace();
				}
				connected = false;
				System.out.println(getCurrentTime() + " Read timeout!");
				// Read timeout occoured try to reestablish new connection
			}
		}
	}

	public static void main(String[] args)
	{
		FBoxCallMonitorNew neu = new FBoxCallMonitorNew();
		neu.start();
	}

}
