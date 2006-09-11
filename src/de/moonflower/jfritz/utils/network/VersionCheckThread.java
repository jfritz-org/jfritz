package de.moonflower.jfritz.utils.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;

/**
 * This thread processes the current version listed on www.jfritz.org
 * If a newer version is listed on the site then the user is presented
 * with a download link for the newer version
 *
 *
 * @author Bastian Schaefer
 *
 */
public class VersionCheckThread implements Runnable {

	boolean informNoNewVersion;

	public VersionCheckThread(boolean informNoNewVersion){
		this.informNoNewVersion = informNoNewVersion;
	}

	public void run(){

		if(checkForNewVersion()){
			Object[] options = {JFritz.getMessage("yes"), JFritz.getMessage("no")};
			int ok = JOptionPane.showOptionDialog(JFritz.getJframe(),
					JFritz.getMessage("new_version_text"),
					JFritz.getMessage("new_version"), JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, // don't use a
														// custom Icon
					options, // the titles of buttons
				options[0]); // default button title
			if (ok == JOptionPane.YES_OPTION)
			BrowserLaunch.openURL(JFritz.PROGRAM_URL + "#download");
		}else if(informNoNewVersion == true){
			JOptionPane.showMessageDialog(JFritz.getJframe(),JFritz.getMessage("no_new_version_found"),"JFritz",JOptionPane.INFORMATION_MESSAGE);
		}

		Debug.msg("CheckVersionThread exiting..");
	}

	public static boolean checkForNewVersion() {
		URL url = null;
		String data = ""; //$NON-NLS-1$

		String urlstr = "http://www.jfritz.org/update/current.txt"; //$NON-NLS-1$

		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();
					BufferedReader d = new BufferedReader(
							new InputStreamReader(con.getInputStream()));
					int i = 0;
					String str = ""; //$NON-NLS-1$

					// Get response data
					while ((i < 700) && (null != ((str = d.readLine())))) {
						data += str;
						i++;
					}
					d.close();
					Debug.msg("Begin processing Version File");

					if (Integer.valueOf(data.replaceAll("\\.", "")).compareTo(
							Integer.valueOf(JFritz.PROGRAM_VERSION.replaceAll(
									"\\.", ""))) > 0) {
						return true;
					}

				} catch (IOException e1) {
					Debug
							.err("Error while retrieving " + urlstr + " (possibly no connection to the internet)"); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
		}
		return false;

	}

}
