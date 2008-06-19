/*
 * Created on 19.07.2005
 */
package de.moonflower.jfritz.dialogs.simple;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HTMLUtil;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for creating a popup dialog for incoming and outgoing calls. Hides after timeout
 * @author rob
 */

public class CallMessageDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1;

	private Timer timer;
	private HideTimer task;
	private String os_file = "file:";
	private String HTML_HEAD = "";
	private String langPath = "";
	private JEditorPane callInLabel;
    private URL imageWorldPath, imagePhonePath, imageHomePath, imageFreeCallPath, imageMobilePath, googlePath;

	public CallMessageDlg()
	{
		super();
	    addWindowListener(new WindowCloseHandle(this));
		if (System.getProperty("os.name").equals("Linux"))
			os_file += "/";
		HTML_HEAD = "<head>" + "<link rel=STYLESHEET TYPE=\"text/css\" HREF=\""+ os_file +
		JFritzUtils.getFullPath(JFritzUtils.rootID) + "css/ru_blue.css"+ "\"></head>";
		langPath = JFritzUtils.getFullPath(JFritzUtils.langID);
		imageWorldPath = getClass().getResource("/de/moonflower/jfritz/resources/images/world.png"); //$NON-NLS-1$
		imagePhonePath = getClass().getResource("/de/moonflower/jfritz/resources/images/phone.png"); //$NON-NLS-1$
		imageHomePath = getClass().getResource("/de/moonflower/jfritz/resources/images/home.png"); //$NON-NLS-1$
		imageFreeCallPath = getClass().getResource("/de/moonflower/jfritz/resources/images/freecall.png"); //$NON-NLS-1$
		imageMobilePath = getClass().getResource("/de/moonflower/jfritz/resources/images/handy.png"); //$NON-NLS-1$
		googlePath = getClass().getResource("/de/moonflower/jfritz/resources/images/google.gif"); //$NON-NLS-1$
	}

	public void showIncomingCall(Call call, String callerstr, String calledstr, String name, String portstr, Person person) {

		Debug.msg("Showing incoming call...");
		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(Main.getProperty(
				"option.popupDelay", "10")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/de/moonflower/jfritz/resources/images/callin.png"))).getImage()); //$NON-NLS-1$)
		setTitle(Main.getMessage("dialog_title_callin") + ": " + calledstr); //$NON-NLS-1$

		Debug.msg("Creating call message gui...");
		JButton closeButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		closeButton.addActionListener(this);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(closeButton, BorderLayout.SOUTH);
		getContentPane().getInsets().set(0, 0, 0, 0);
		Toolkit 	tk 	= getToolkit();
		Dimension dim 	= tk.getScreenSize();
		int 		x	= (dim.width / 2) - 200;
		int 		y	= (dim.height / 2) - 250;
		setLocation(x, y);

		JPanel mainPane = new JPanel();
		BoxLayout mainPaneLayout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
		mainPane.setLayout(mainPaneLayout);
		mainPane.getInsets().set(0, 0, 0, 0);

		callInLabel = new JEditorPane();
		callInLabel.setContentType("text/html");
		callInLabel.setEditable(false);
		callInLabel.setOpaque(false);
		callInLabel.addHyperlinkListener(new HyperlinkListener() {
			private String tooltip;

			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					BrowserLaunch.openURL(hle.getURL().toString());
				}
                else if (hle.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    tooltip = callInLabel.getToolTipText();
                    callInLabel.setToolTipText(Main.getMessage("show_on_google_maps"));
                } else if (hle.getEventType() == HyperlinkEvent.EventType.EXITED) {
                	callInLabel.setToolTipText(tooltip);
                }
			}
		});

		Debug.msg("Determine name, street and city...");
		String nameStr = "",
			   streetStr = "",
			   cityStr = "",
			   flagPath = "",
			   country = "",
			   countryStr = "",
			   google = "",
			   dateStr = "",
			   timeStr = "";
		URL phoneIcon;
		if ( person != null )
		{
			nameStr =  "<td id=\"name\" height=\"" + "10" + "\">" + person.getFullname() + "</td>";
			streetStr = "<td id=\"inv_strasse\" class=\"inv\">" + person.getStreet() + "</td></tr><tr>";
			cityStr = "<td id=\"inv_ort\" class=\"inv\">" + person.getPostalCode() + " " + person.getCity() + "</td></tr><tr>";
		}

		Debug.msg("Determine country and flag...");
		phoneIcon = imageHomePath;
		if ((call != null) && (call.getPhoneNumber() != null) && (call.getPhoneNumber().getIntNumber().length() > 6))
		{
			PhoneNumber number = call.getPhoneNumber();
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy"); //$NON-NLS-1$
			dateStr = df.format(call.getCalldate());
			df = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
			timeStr = df.format(call.getCalldate());
			country = number.getCountry();
			if (!number.getFlagFileName().equals(""))
			{
				flagPath = os_file + langPath + JFritzUtils.FILESEP + "flags" + JFritzUtils.FILESEP+ number.getFlagFileName();
			}
			else
			{
				flagPath = "";
				country = "Unknown country";
			}

			String countryCode = Main.getProperty("country.code", "+49");
			if(number.isLocalCall()){
				phoneIcon = imageHomePath;
			}else if(number.isFreeCall()){
				phoneIcon = imageFreeCallPath;
			}else if (number.getIntNumber().startsWith(countryCode)){
				if(number.isMobile()){
					phoneIcon = imageMobilePath;
				} else {
					phoneIcon = imagePhonePath;
				}
			}

			String loc = Main.getProperty("locale", "en_US");
			String googleLink = "http://maps.google.com/maps?f=q&hl="+ loc.substring(0, 2) +"&q=";
			String city = "";
			if ( person != null )
			{
				googleLink += HTMLUtil.stripEntities(person.getStreet())+",+";
				city = person.getCity();

				if ( city.replaceAll(" ", "").equals(""))
				{
					if(number.getCountryCode().equals("+49"))
					{
						googleLink += HTMLUtil.stripEntities(ReverseLookupGermany.getCity(number.getAreaNumber()))+",+";
					}
					if(number.getCountryCode().equals("+43"))
					{
						googleLink += HTMLUtil.stripEntities(ReverseLookupAustria.getCity(number.getAreaNumber()))+",+";
					}
				}
				googleLink += HTMLUtil.stripEntities(city) + ",+";
				googleLink += HTMLUtil.stripEntities(number.getCountry());
				google = "<tr><td id=\"google\"><a border=\"0\" href='" + googleLink + "'><img src=\"" + googlePath + "\"></a></td></tr>";
			}
		}

		if ( country.equals("Unknown country"))
		{
			countryStr = "<td id=\"inv_land\" class=\"inv\"><img src=\""+ imageWorldPath +"\">&nbsp;&nbsp;" + country + "</td></tr>";
			google = "";
		} else {
			countryStr = "<td id=\"inv_land\" class=\"inv\"><img src=\""+ flagPath +"\">&nbsp;&nbsp;" + country + "</td></tr>";
		}

		Debug.msg("Setting text message...");
		String textToDisplay = "<html>" + HTML_HEAD + "<body><div id=\"divU\" " +
//				"height=\"" + "300" + "\" width=\"" +"300" +
				"><table id=\"tabU\">" +
				"<tr><td id=\"datetime\" height=\"10\">" + dateStr + " " + timeStr + "</td></tr>" +
				"<tr>" + "<td id=\"called\" height=\"" + "10" + "\">" + calledstr + "</td></tr>" +
				"<tr>" + nameStr + "</tr>" +
				"<tr>" + "<td id=\"number\" height=\"" + "10" + "\"><img src=\""+ phoneIcon+"\">&nbsp;&nbsp;" + callerstr+ "</td></tr>" +
				"<tr><td id=\"inv_name\" class=\"inv\">" + name + "</td></tr><tr>" +
				streetStr +
				cityStr +
				countryStr +
				google +
				"</table></div>" +
				"</body></html>";
		Debug.msg(textToDisplay);
//		BrowserLaunch.openURL(JFritz.DOCUMENTATION_URL);

		callInLabel.setText(textToDisplay);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		Debug.msg("Display message...");
		pack();
		Debug.msg("Showing icon...");
		if ( person != null )
		{
			if ( !person.getPictureUrl().equals("") )
			{
				ImageIcon boxicon = new ImageIcon(person.getPictureUrl());
				Image scaledImage = boxicon.getImage().getScaledInstance(-1, mainPane.getHeight(), Image.SCALE_SMOOTH);
				JLabel label = new JLabel(new ImageIcon(scaledImage));
				label.setIconTextGap(0);
				getContentPane().add(label, BorderLayout.WEST);
			}
		}
		pack();
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		int posXDefault = (screenDim.width / 2) - (this.getWidth() / 2);
		int posYDefault = (screenDim.height / 2) - (this.getHeight() / 2);
		int xPos = Integer.parseInt(Main.getStateProperty("callmessenger.popup.xpos", Integer.toString(posXDefault)));
		int yPos = Integer.parseInt(Main.getStateProperty("callmessenger.popup.ypos", Integer.toString(posYDefault)));
		if (   (xPos - 40 > screenDim.width)
			|| (yPos - 40 > screenDim.height)
			|| (xPos + this.getWidth() + 40 < 0)
			|| (yPos + this.getHeight() + 40 < 0)
		   )
		{
			xPos = posXDefault;
			yPos = posXDefault;
		}
		this.setLocation(xPos, yPos);
		setVisible(true);
		toFront();
		Debug.msg("Should be displayed...");
	}

	/**
	 * Hide dialog after OK-Button pressed
	 */
	public void actionPerformed(ActionEvent e) {
		close();
		setVisible(false);
		dispose();
	}

	public void close()
	{
		timer.cancel();
		task.cancel();
		String currentXPos = Integer.toString(getLocation().x);
		String currentYPos = Integer.toString(getLocation().y);
		Main.setStateProperty("callmessenger.popup.xpos", currentXPos);
		Main.setStateProperty("callmessenger.popup.ypos", currentYPos);
	}

	/**
	 * Hide dialog after timeout
	 * @author rob
	 */
	private class HideTimer extends TimerTask {
		private CallMessageDlg msgDialog;

		public HideTimer(CallMessageDlg msgDialog) {
			this.msgDialog = msgDialog;
		}

		public void run() {
			cancel();
			msgDialog.close();
			msgDialog.setVisible(false);
			msgDialog.dispose();
		}
	}

	private class WindowCloseHandle extends java.awt.event.WindowAdapter
	{
		private CallMessageDlg msgDialog;
		public WindowCloseHandle(CallMessageDlg msgDialog)
		{
			this.msgDialog = msgDialog;
		}

		public void windowClosing(java.awt.event.WindowEvent evt)
        {
			msgDialog.close();
			msgDialog.dispose();
        }
	}
}

