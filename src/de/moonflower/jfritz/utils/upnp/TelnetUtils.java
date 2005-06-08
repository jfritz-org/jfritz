/*
 * Created on 07.06.2005
 *
 */
package de.moonflower.jfritz.utils.upnp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Arno Willig
 *
 */
public class TelnetUtils {

	/**
	 *
	 */
	public TelnetUtils() {
		System.out.println("Socket-test");
		Socket t;
		try {
			t = new Socket("192.168.178.1", 23);
			BufferedReader in = new BufferedReader(new InputStreamReader(t
					.getInputStream()));
			PrintStream os = new PrintStream(t.getOutputStream());
			//			String test = "Superkalifragilistischexpialigetisch";
			//			os.println(test);
			char[] c = new char[1024];
			in.read(c);
			String s = new String(c);
			//			String s = in.readLine();
			for (int i=0;i<c.length;i++) {
			System.out.println((int) c[i]);
			}
			t.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TelnetUtils();
	}
}
