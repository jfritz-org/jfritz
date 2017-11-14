package de.moonflower.jfritz.simpletests;

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class SocketTest {

	private static final String ip = "192.168.1.4";
	private static final int port = 1012;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector<Socket> sockets = new Vector<Socket>();
		try {
			for (int i=0; i<60; i++)
			{
				sockets.add(new Socket(ip, port));
			}

			Thread.sleep(30000);

			for (Socket socket:sockets)
			{
				socket.close();
			}
		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
