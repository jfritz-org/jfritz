/*
 * Created on 29.07.2005
 *
 */
package de.moonflower.jfritz;

import javax.swing.SwingUtilities;

import java.lang.reflect.*;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 *
 * Handles events thrown by MacOSX's Application Menubar (Quit, About, Prefs)
 */
public class MacHandler {
	JFritz jfritz;

	/**
	 * Register quit-, about-, prefsHandler
	 *
	 * @param jfritz
	 *            An instance of JFritz
	 */
	public MacHandler(JFritz jfritz) {
		try {
			this.jfritz = jfritz;

			// @Jochen: Vielleicht kannst du hier noch Code einfügen, so dass
			// JFritz unter dem MAC hübscher aussieht, auch wenn man nicht die
			// extra MAC-Version nimmt. Ich habe da schon mal drei Beispiele
			// reingemacht

			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System
					.setProperty(
							"com.apple.mrj.application.apple.menu.about.name",
							"JFritz");
			System.setProperty("com.apple.mrj.application.growbox.intrudes",
					"false");

			Class quitHandler = Class.forName("com.apple.mrj.MRJQuitHandler");
			Class aboutHandler = Class.forName("com.apple.mrj.MRJAboutHandler");
			Class prefsHandler = Class.forName("com.apple.mrj.MRJPrefsHandler");
			Class MRJApplicationUtils = Class
					.forName("com.apple.mrj.MRJApplicationUtils");

			myInvocationHandler invocationHandler = new myInvocationHandler();

			// Array of arguments for calling method
			Object argslist[] = new Object[1];
			// Array of argument types
			Class[] types = new Class[1];

			Debug.msg("MAC: Register quitHandler");
			Proxy proxy = (Proxy) Proxy.newProxyInstance(quitHandler
					.getClassLoader(), new Class[] { quitHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = quitHandler;
			Method registerQuitHandler = MRJApplicationUtils.getMethod(
					"registerQuitHandler", types);
			registerQuitHandler.invoke(proxy, argslist);

			Debug.msg("MAC: Register aboutHandler");
			proxy = (Proxy) Proxy.newProxyInstance(aboutHandler
					.getClassLoader(), new Class[] { aboutHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = aboutHandler;
			Method registerAboutHandler = MRJApplicationUtils.getMethod(
					"registerAboutHandler", types);
			registerAboutHandler.invoke(proxy, argslist);

			Debug.msg("MAC: Register prefsHandler");
			proxy = (Proxy) Proxy.newProxyInstance(prefsHandler
					.getClassLoader(), new Class[] { prefsHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = prefsHandler;
			Method registerPrefsHandler = MRJApplicationUtils.getMethod(
					"registerPrefsHandler", types);
			registerPrefsHandler.invoke(proxy, argslist);

		} catch (Throwable e) {
			System.err.println(e);
		}
	}

	/**
	 * Handles thrown Events (handleQuit, handleAbout, handlePrefs)
	 *
	 * @author rob
	 */
	public class myInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			if (method.getName().equalsIgnoreCase("handleQuit")) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Debug.msg("MAC Application Menu: Show Exit Dialog");
						jfritz.getJframe().showExitDialog();
					}
				});
				throw new IllegalStateException("Let the quit handler do it");
			}

			else if (method.getName().equalsIgnoreCase("handleAbout")) {
				Debug.msg("MAC Application Menu: Show About Dialog");
				jfritz.getJframe().showAboutDialog();
			} else if (method.getName().equalsIgnoreCase("handlePrefs")) {
				Debug.msg("MAC Application Menu: Show Prefs Dialog");
				jfritz.getJframe().showConfigDialog();
			}
			return null;
		}
	}

}
