package de.moonflower.jfritz.utils;

////////////////////////////////////////////////////////
//Bare Bones Browser Launch                          //
//Version 1.1                                        //
//July 8, 2005                                       //
//Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//Example Usage:                                     //
// String url = "http://www.centerkey.com/";         //
// BrowserLaunch.openURL(url);                       //
//Public Domain Software -- Free to Use as You Like  //
////////////////////////////////////////////////////////

import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;

/**
 * Opens browser with an URL
 *
 */
public class BrowserLaunch {
	private final static Logger log = Logger.getLogger(BrowserLaunch.class);

	protected static MessageProvider messages = MessageProvider.getInstance();
	private static final String errMsg = messages.getMessage("error_browser_not_started"); //$NON-NLS-1$

	/**
	 * Open new browser with an url
	 * @param url
	 */
	public static void openURL(String url) {
	String osName = System.getProperty("os.name"); //$NON-NLS-1$
	   try {
	      if (osName.startsWith("Mac OS")) { //$NON-NLS-1$
	    	 Debug.debug(log, "openURL on Mac OS for URL: " + url);
	         Class<?> macUtils = Class.forName("com.apple.mrj.MRJFileUtils"); //$NON-NLS-1$
	         Method openURL = macUtils.getDeclaredMethod("openURL", //$NON-NLS-1$
	            new Class[] {String.class});
	         openURL.invoke(null, new Object[] {url});
	         }
	      else if (osName.startsWith("Windows")) { //$NON-NLS-1$
	     	 Debug.debug(log, "openURL on Windows for URL: " + url);
	         Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url); //$NON-NLS-1$
	      	 }
	      else { //assume Unix or Linux
	     	 Debug.debug(log, "openURL on Unix/Linux for URL: " + url);
	         String[] browsers = {
	            "firefox", //$NON-NLS-1$
	            "opera", //$NON-NLS-1$
	            "konqueror", //$NON-NLS-1$
	            "mozilla", //$NON-NLS-1$
	            "netscape" }; //$NON-NLS-1$
	         String browser = null;
	         for (int count = 0; count < browsers.length && browser == null; count++)
	            if (Runtime.getRuntime().exec(
	                  new String[] {"which", browsers[count]}).waitFor() == 0) //$NON-NLS-1$
	               browser = browsers[count];
	         if (browser == null){
	        	Debug.error(log, "No browser found!");
	            throw new Exception(messages.getMessage("error_browser_not_found")); //$NON-NLS-1$
	         }else
	        	Debug.debug(log, "Executing browser '" + browser + "'");
	            Runtime.getRuntime().exec(new String[] {browser, url});
	         }
	      }
	   catch (Exception e) {
	  	  Debug.error(log, "error on openURL: " + e.toString());
	      JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.toString()); //$NON-NLS-1$
	      }
	   }
}
