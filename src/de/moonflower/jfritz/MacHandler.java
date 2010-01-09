package de.moonflower.jfritz;

import java.lang.reflect.*;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;



/**
 * @author rob
 *
 * Handles events thrown by MacOSX's Application Menubar (Quit, About, Prefs)
 */
public class MacHandler {

	private JFritz jfritz;

	/**
	 * Register quit-, about-, prefsHandler
	 *
	 *            An instance of JFritz
	 */
	public MacHandler(JFritz jfritz) {
		this.jfritz = jfritz;
		try {

			System.setProperty("apple.laf.useScreenMenuBar", "true");  //$NON-NLS-1$,  //$NON-NLS-2$
			System
					.setProperty(
							"com.apple.mrj.application.apple.menu.about.name", //$NON-NLS-1$
							"JFritz"); //$NON-NLS-1$
			System.setProperty("com.apple.mrj.application.growbox.intrudes", //$NON-NLS-1$
					"false"); //$NON-NLS-1$

			Class<?> quitHandler = Class.forName("com.apple.mrj.MRJQuitHandler"); //$NON-NLS-1$
			Class<?> aboutHandler = Class.forName("com.apple.mrj.MRJAboutHandler"); //$NON-NLS-1$
			Class<?> prefsHandler = Class.forName("com.apple.mrj.MRJPrefsHandler"); //$NON-NLS-1$
			Class<?> MRJApplicationUtils = Class
					.forName("com.apple.mrj.MRJApplicationUtils"); //$NON-NLS-1$

			MyInvocationHandler invocationHandler = new MyInvocationHandler();

			// Array of arguments for calling method
			Object argslist[] = new Object[1];
			// Array of argument types
			Class<?>[] types = new Class[1];

			Debug.info("MAC: Register quitHandler"); //$NON-NLS-1$
			Proxy proxy = (Proxy) Proxy.newProxyInstance(quitHandler
					.getClassLoader(), new Class[] { quitHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = quitHandler;
			Method registerQuitHandler = MRJApplicationUtils.getMethod(
					"registerQuitHandler", types); //$NON-NLS-1$
			registerQuitHandler.invoke(proxy, argslist);

			Debug.info("MAC: Register aboutHandler"); //$NON-NLS-1$
			proxy = (Proxy) Proxy.newProxyInstance(aboutHandler
					.getClassLoader(), new Class[] { aboutHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = aboutHandler;
			Method registerAboutHandler = MRJApplicationUtils.getMethod(
					"registerAboutHandler", types); //$NON-NLS-1$
			registerAboutHandler.invoke(proxy, argslist);

			Debug.info("MAC: Register prefsHandler"); //$NON-NLS-1$
			proxy = (Proxy) Proxy.newProxyInstance(prefsHandler
					.getClassLoader(), new Class[] { prefsHandler },
					invocationHandler);

			argslist[0] = proxy;
			types[0] = prefsHandler;
			Method registerPrefsHandler = MRJApplicationUtils.getMethod(
					"registerPrefsHandler", types); //$NON-NLS-1$
			registerPrefsHandler.invoke(proxy, argslist);

		} catch (Throwable e) {
			Debug.error(e.toString());
		}
	}

	/**
	 * Handles thrown Events (handleQuit, handleAbout, handlePrefs)
	 *
	 * @author rob
	 */
	public class MyInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			if (method.getName().equalsIgnoreCase("handleQuit")) { //$NON-NLS-1$

				//new attempt at preventing jfritz from stopping a shutdown
				//JFritz either has to call system.exit before this method closes
				//or JFritz has to throw an illegalStateException to prevent
				//the close event from continuing, if the user click cancel
				Debug.info("Mac Quit handler was called");
				if(jfritz.maybeExit(0, true)){
					System.exit(0);
				} else{
					throw new IllegalStateException("User chose not to quit JFritz!"); //$NON-NLS-1$
				}

				Debug.info("Mac Quit handler is exiting");



	//This is an old workaround for 10.2 code, no longer need and preventing proper shutdowns
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						Debug.msg("MAC Application Menu: Show Exit Dialog"); //$NON-NLS-1$
//						jfritz.maybeExit(0);
//					}
//				});

			}

			else if (method.getName().equalsIgnoreCase("handleAbout")) { //$NON-NLS-1$
				Debug.info("MAC Application Menu: Show About Dialog"); //$NON-NLS-1$
                JFritz.getJframe().showAboutDialog();
			} else if (method.getName().equalsIgnoreCase("handlePrefs")) { //$NON-NLS-1$
				Debug.info("MAC Application Menu: Show Prefs Dialog"); //$NON-NLS-1$
                JFritz.getJframe().showConfigDialog();
			}
			return null;
		}
	}

}
