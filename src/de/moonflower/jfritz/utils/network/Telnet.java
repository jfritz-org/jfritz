/*
 * Created on 16.07.2005
 *
 */
package de.moonflower.jfritz.utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.StatusBarController;
import de.moonflower.jfritz.dialogs.config.TelnetConfigDialog;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;

import org.apache.commons.net.telnet.TelnetClient;

/**
 * Manages Telnetfunctions: Connect, Login, Disconnect, sendCommand
 *
 * @author rob
 *
 */
public class Telnet {

	private boolean connected = false;

	private TelnetClient telnet;

	private InputStream in;

	private PrintStream out;

	private final char prompt = '#';

	private static final int LOGIN_OK = 0;

	private static final int LOGIN_CANCELED = 1;

	private StatusBarController statusBarController = new StatusBarController();

	public Telnet() {
		telnet = new TelnetClient();
	}

	/**
	 * Connects to FritzBox-IP
	 *
	 * TODO: Einbau der Abfrage der IP, User, Passwort, wenn keine Verbindung
	 * aufgebaut werden kann.
	 * @throws IOException
	 * @throws InvalidFirmwareException
	 * @throws WrongPasswordException
	 */
	public void connect() throws WrongPasswordException, InvalidFirmwareException, IOException {
		boolean isdone = false;
		int connectionFailures = 0;
		while (!isdone) {
			String server = JFritz.getFritzBox().getAddress();

			String password;
			if (Main.getProperty("telnet.password").equals("")) { //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				// Noch kein Passwort gesetzt. Zeige Einstellungsdialog
				TelnetConfigDialog telnetConfigDialog = new TelnetConfigDialog(
						JFritz.getJframe());
				telnetConfigDialog.setModal(true);
				if (telnetConfigDialog.showConfigDialog() == TelnetConfigDialog.CANCEL_OPTION) {
					// ABBRUCH
					telnetConfigDialog.dispose();
					return;
				}
				telnetConfigDialog.dispose();
			}

			String user = Main.getProperty("telnet.user"); //$NON-NLS-1$,  //$NON-NLS-2$
			password = Encryption.decrypt(Main.getProperty("telnet.password")); //$NON-NLS-1$
			int port = 23;
			try {
				Debug.msg("Verbinde mit Telnet ..."); //$NON-NLS-1$
				statusBarController.fireStatusChanged(Main
						.getMessage("Verbinde mit Telnet ...")); //$NON-NLS-1$

				telnet.connect(server, port); // Connect to the specified
				// server
				in = telnet.getInputStream();
				out = new PrintStream(telnet.getOutputStream());
				if (login(user, password) == LOGIN_OK) {
					connected = true;
				}
				isdone = true;
				Debug.msg("Done"); //$NON-NLS-1$
			} catch (ConnectException e) { // Connection Timeout
				Debug.msg("Telnet connection timeout ..."); //$NON-NLS-1$
				// Warten, falls wir von einem Standby aufwachen,
				// oder das Netzwerk temporär nicht erreichbar ist.
				if (connectionFailures < 5) {
					Debug.msg("Waiting for FritzBox, retrying ..."); //$NON-NLS-1$
					connectionFailures++;
				} else {
					Debug.msg("FritzBox not found. Get new IP ..."); //$NON-NLS-1$
					statusBarController.fireStatusChanged(Main
							.getMessage("box.not_found")); //$NON-NLS-1$
					Debug.err("Address wrong!"); //$NON-NLS-1$
					JFritz.getJframe().setBusy(false);
					String box_address = JFritz.getJframe().showAddressDialog(
							JFritz.getFritzBox().getAddress()); //,
					if (box_address == null) {
						JFritz.stopCallMonitor();
						isdone = true;
					} else {
						Main.setProperty("box.address", box_address); //$NON-NLS-1$
						JFritz.getFritzBox().setAddress(box_address);
						JFritz.getFritzBox().detectFirmware();
					}
				}
			} catch (Exception e) {
				System.err.println("Error in Class Telnet"); //$NON-NLS-1$
				Debug.err(e.toString());
				return;
			}
		}
	}

	/**
	 * login on a telnet session with user and password
	 *
	 * @param user
	 * @param password
	 */

	private int login(String user, String password) {
		try {
			Debug.msg("Login to Telnet"); //$NON-NLS-1$
			String login = "ogin: "; //$NON-NLS-1$
			String passwd = "assword: "; //$NON-NLS-1$
			boolean firstLogin = true;
			boolean firstPassword = true;
			char lastCharLogin = login.charAt(login.length() - 1);
			char lastCharPasswd = passwd.charAt(passwd.length() - 1);
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch); // FIXME This can be done better!!!
				if ((ch == lastCharLogin) || (ch == lastCharPasswd) || (ch == prompt)) {
					if (sb.toString().endsWith(login)) {
						// wenn Fehlgeschlagen, dann
						// mehrmaliges Login mit falschem
						// Username verhindern
						if (firstLogin) {
							Debug.msg("Writing Telnet User: " + user); //$NON-NLS-1$
							write(user);
							firstLogin = false;
						} else {
							TelnetConfigDialog telnetConfigDialog = new TelnetConfigDialog(
									JFritz.getJframe());
							telnetConfigDialog.setModal(true);
							if (telnetConfigDialog.showConfigDialog() == TelnetConfigDialog.CANCEL_OPTION) {
								// ABBRUCH
								JFritz.stopCallMonitor();
								telnetConfigDialog.dispose();
								return LOGIN_CANCELED;
							}
							telnetConfigDialog.dispose();
						}
					}
					if (sb.toString().endsWith(passwd)) {
						// schauen, ob WebPasswort abgefragt wird
						if (sb.toString().endsWith("web password: ")) { //$NON-NLS-1$
							password = JFritz.getFritzBox().getPassword();

							while (true) { // test WebPassword
								try {
									FritzBoxFirmware.detectFirmwareVersion(
											JFritz.getFritzBox().getAddress(),
											JFritz.getFritzBox().getPassword(),
											JFritz.getFritzBox().getPort());
									password = JFritz.getFritzBox()
											.getPassword();
									break; // go on with telnet login
								} catch (WrongPasswordException e1) {
									Debug.err(Main.getMessage("box.wrong_password")); //$NON-NLS-1$

									statusBarController.fireStatusChanged(Main
											.getMessage("box.wrong_password")); //$NON-NLS-1$
									JFritz.getJframe().setBusy(false);

									String newPassword = JFritz
											.showPasswordDialog(
													JFritz.getFritzBox()
															.getPassword());
									Debug.msg("OLD PASS: " //$NON-NLS-1$
											+ JFritz.getFritzBox()
													.getPassword());
									if (newPassword == null) { // Dialog
										// aborted
										JFritz.stopCallMonitor();
										return LOGIN_CANCELED;
									} else {
										Main
												.setProperty(
														"box.password", //$NON-NLS-1$
														Encryption
																.encrypt(newPassword));
										JFritz.getFritzBox().setPassword(
												newPassword);
										JFritz.getFritzBox().detectFirmware();
									}
								}
							}
						}
						// wenn Fehlgeschlagen, dann
						// mehrmaliges Login mit falschem
						// Passwort verhindern
						if (firstPassword) {
							Debug.msg("Writing Telnet Password: " + password); //$NON-NLS-1$
							write(password);
							firstPassword = false;
						} else {
							TelnetConfigDialog telnetConfigDialog = new TelnetConfigDialog(
									JFritz.getJframe());
							telnetConfigDialog.setModal(true);
							if (telnetConfigDialog.showConfigDialog() == TelnetConfigDialog.CANCEL_OPTION) {
								// ABBRUCH
								telnetConfigDialog.dispose();
								return LOGIN_CANCELED;
							}
							telnetConfigDialog.dispose();
						}
					}
					if (ch == prompt) {
						System.err.println(sb.toString());
						break;
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			Debug.err(e.getMessage());
		}
		Debug.msg("Logged into Telnet connection."); //$NON-NLS-1$
		return LOGIN_OK;
	}

	/**
	 * gets all Data until a pattern is reached
	 *
	 * @param pattern
	 * @return data read until pattern
	 */
	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch); // FIXME This can be done better!!!
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			Debug.err(e.getMessage());
		}
		return null;
	}

	/**
	 * Write value to telnet-session
	 *
	 * @param value
	 */
	public void write(String value) {
		try {
			out.println(value);
			out.flush();
		} catch (Exception e) {
			Debug.err(e.toString());
		}
	}

	/**
	 * writes command on telnet session and waits till prompt
	 *
	 * @param command
	 * @return data read until prompt
	 */
	public String sendCommand(String command) {
		try {
			write(command);
			String data = readUntil(prompt + " "); //$NON-NLS-1$
			return data;
		} catch (Exception e) {
			Debug.err(e.toString());
		}
		return null;
	}

	/**
	 *
	 * @return telnetIsConnected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Disconnects Telnet from FritzBox
	 *
	 */
	public void disconnect() {
		Debug.msg("Disconnect Telnet connection."); //$NON-NLS-1$
		try {
			telnet.disconnect();
			connected = false;
		} catch (Exception e) {
		}
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

}
