/*
 * Created on 19.07.2005
 */
package de.moonflower.jfritz.dialogs.simple;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JButton;
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
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupTurkey;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupUnitedStates;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for creating a popup dialog for incoming and outgoing calls. Hides after timeout
 * @author rob
 */

public class CallMessageDlg extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1;

	private Timer timer;
	private HideTimer task;
	private String os_file = "file:";
	private String langPath = "";
	private JEditorPane callInLabel;
    private URL imageWorldPath, imagePhonePath, imageHomePath, imageFreeCallPath, imageMobilePath, googlePath;
    private String template_incoming = "", template_outgoing = "";
    private int preferredImageWidth, preferredImageHeight;

	public CallMessageDlg()
	{
		super();
	    addWindowListener(new WindowCloseHandle(this));
		if (System.getProperty("os.name").equals("Linux"))
			os_file += "/";
		template_incoming = loadTemplate(JFritzUtils.getFullPath(JFritzUtils.rootID) + "styles/template-incoming.html");
		template_outgoing = loadTemplate(JFritzUtils.getFullPath(JFritzUtils.rootID) + "styles/template-outgoing.html");

		langPath = JFritzUtils.getFullPath(JFritzUtils.langID);
		imageWorldPath = getClass().getResource("/de/moonflower/jfritz/resources/images/world.png"); //$NON-NLS-1$
		imagePhonePath = getClass().getResource("/de/moonflower/jfritz/resources/images/phone.png"); //$NON-NLS-1$
		imageHomePath = getClass().getResource("/de/moonflower/jfritz/resources/images/home.png"); //$NON-NLS-1$
		imageFreeCallPath = getClass().getResource("/de/moonflower/jfritz/resources/images/freecall.png"); //$NON-NLS-1$
		imageMobilePath = getClass().getResource("/de/moonflower/jfritz/resources/images/handy.png"); //$NON-NLS-1$
		googlePath = getClass().getResource("/de/moonflower/jfritz/resources/images/google.gif"); //$NON-NLS-1$
	}

	public void showIncomingCall(Call call, String callerstr, String calledstr, Person person) {

		Debug.msg("Showing incoming call...");
		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(Main.getProperty(
				"option.popupDelay")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/de/moonflower/jfritz/resources/images/callin.png"))).getImage()); //$NON-NLS-1$)
		String titleStr = Main.getMessage("dialog_title_callin");
		titleStr = titleStr.replaceAll("%SOURCE%", callerstr);
		titleStr = titleStr.replaceAll("%DEST%", calledstr);
		setTitle(titleStr); //$NON-NLS-1$

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

		String message = template_incoming;
		if ( person != null )
		{
			message = message.replaceAll("%FIRSTNAME%", person.getFirstName());
			message = message.replaceAll("%LASTNAME%", person.getLastName());
			message = message.replaceAll("%COMPANY%", person.getCompany());
			message = message.replaceAll("%STREET%", person.getStreet());
			message = message.replaceAll("%POSTALCODE%", person.getPostalCode());
			message = message.replaceAll("%CITY%", person.getCity());
			if ( !person.getPictureUrl().equals("") )
			{
				URL imageURL;
				try {
					imageURL = new URL(os_file + person.getPictureUrl());
					ImageIcon image = new ImageIcon(imageURL);
					int iconHeight = image.getIconHeight();
					int iconWidth = image.getIconWidth();
					String imageHeight = Integer.toString(iconHeight);
					String imageWidth = Integer.toString(iconWidth);
					if ( (preferredImageHeight <= 0) && (preferredImageWidth > 0 ))
					{
						imageWidth = Integer.toString(preferredImageWidth);
						imageHeight = Integer.toString((iconHeight * preferredImageWidth) / iconWidth);
					}
					if ( (preferredImageHeight > 0) && (preferredImageWidth <= 0 ))
					{
						imageWidth = Integer.toString((iconWidth * preferredImageHeight) / iconHeight);
						imageHeight = Integer.toString(preferredImageHeight);
					}
					if ( (preferredImageHeight > 0) && (preferredImageWidth > 0 ))
					{
						imageWidth = Integer.toString(preferredImageWidth);
						imageHeight = Integer.toString(preferredImageHeight);
					}
					String htmlStr = "<img src=\"" + imageURL.toString() + "\" height=\"%HEIGHT%\" width=\"%WIDTH%\">";
					htmlStr = htmlStr.replaceAll("%HEIGHT%", imageHeight);
					htmlStr = htmlStr.replaceAll("%WIDTH%", imageWidth);
					message = message.replaceAll("%IMAGE%", htmlStr);
					} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			else
			{
				message = message.replaceAll("%IMAGE%", "");
			}
		}
		else
		{
			message = message.replaceAll("%FIRSTNAME%", "");
			message = message.replaceAll("%LASTNAME%", "");
			message = message.replaceAll("%COMPANY%", "");
			message = message.replaceAll("%STREET%", "");
			message = message.replaceAll("%POSTALCODE%", "");
			message = message.replaceAll("%CITY%", "");
			message = message.replaceAll("%IMAGE%", "");
		}

		if ( call != null )
		{
			message = message.replaceAll("%CALLED%", call.getRoute());
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy"); //$NON-NLS-1$
			message = message.replaceAll("%DATE%", df.format(call.getCalldate()));
			df = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
			message = message.replaceAll("%TIME%", df.format(call.getCalldate()));
			PhoneNumber number = call.getPhoneNumber();
			if ( call.getPhoneNumber() != null )
			{
				message = message.replaceAll("%COUNTRY%", number.getCountry());
				if (!number.getFlagFileName().equals(""))
				{
					try {
						URL flagURL = new URL(os_file + langPath + JFritzUtils.FILESEP + "flags" + JFritzUtils.FILESEP+ number.getFlagFileName());
						message = message.replaceAll("%FLAG%", "<img src=\"" + flagURL.toString() + "\">");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				else
				{
					message = message.replaceAll("%FLAG%", "<img src=\"" + imageWorldPath.toString() + "\">");
					message = message.replaceAll("%COUNTRY%", Main.getMessage("unknown_country"));
				}

				String countryCode = Main.getProperty("country.code");
				URL phoneIcon = imageHomePath;
				if(number.isLocalCall()){
					phoneIcon = imageHomePath;
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
				}else if(number.isFreeCall()){
					phoneIcon = imageFreeCallPath;
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
				}else if (number.getIntNumber().startsWith(countryCode)){
					if(number.isMobile()){
						phoneIcon = imageMobilePath;
						message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
					} else {
						phoneIcon = imagePhonePath;
						message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getFullNumber());
					}
				}else{
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getFullNumber());
				}
				message = message.replaceAll("%PHONEICON%", "<img src=\"" + phoneIcon.toString() + "\">");

				String loc = Main.getProperty("locale");
				String googleLink = "http://maps.google.com/maps?f=q&hl="+ loc.substring(0, 2) +"&q=";
				String city = "";
				if ( person != null )
				{
					googleLink += HTMLUtil.stripEntities(person.getStreet())+",+";
					city = person.getCity();

					if ( city.replaceAll(" ", "").equals(""))
					{
						if(number.getCountryCode().equals(ReverseLookup.GERMANY_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupGermany.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.AUSTRIA_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupAustria.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.USA_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupUnitedStates.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.TURKEY_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupTurkey.getCity(number.getAreaNumber()))+",+";
						}
					}
					googleLink += HTMLUtil.stripEntities(city) + ",+";
					googleLink += HTMLUtil.stripEntities(number.getCountry());
					message = message.replaceAll("%GOOGLE%", "<a border=\"0\" href='" + googleLink + "'><img src=\"" + googlePath + "\"></a>");
				}
				else
				{
					message = message.replaceAll("%GOOGLE%", "");
				}
			}
			else
			{
				message = message.replaceAll("%NUMBER%", "");
				message = message.replaceAll("%FLAG%", "");
				message = message.replaceAll("%COUNTRY%", "");
				message = message.replaceAll("%PHONEICON%", "");
				message = message.replaceAll("%GOOGLE%", "");
			}
		}
		else
		{
			message = message.replaceAll("%CALLED%", "");
			message = message.replaceAll("%NUMBER%", "");
			message = message.replaceAll("%DATE%", "");
			message = message.replaceAll("%TIME%", "");
			message = message.replaceAll("%FLAG%", "");
			message = message.replaceAll("%COUNTRY%", "");
			message = message.replaceAll("%PHONEICON%", "");
			message = message.replaceAll("%GOOGLE%", "");
		}

		message = message.replaceAll("<td></td>", "");
		Debug.msg(message);
		callInLabel.setText(message);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		Debug.msg("Display message...");
		pack();
		if ( this.getWidth() < 450 )
		{
			this.setSize(450, this.getHeight());
		}

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

	public void showOutgoingCall(Call call, String callerstr, String calledstr, Person person) {

		Debug.msg("Showing outgoing call...");
		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(Main.getProperty(
				"option.popupDelay")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/de/moonflower/jfritz/resources/images/callout.png"))).getImage()); //$NON-NLS-1$)
		String titleStr = Main.getMessage("dialog_title_callout");
		titleStr = titleStr.replaceAll("%SOURCE%", callerstr);
		titleStr = titleStr.replaceAll("%DEST%", calledstr);
		setTitle(titleStr); //$NON-NLS-1$

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

		String message = template_outgoing;
		if ( person != null )
		{
			message = message.replaceAll("%FIRSTNAME%", person.getFirstName());
			message = message.replaceAll("%LASTNAME%", person.getLastName());
			message = message.replaceAll("%COMPANY%", person.getCompany());
			message = message.replaceAll("%STREET%", person.getStreet());
			message = message.replaceAll("%POSTALCODE%", person.getPostalCode());
			message = message.replaceAll("%CITY%", person.getCity());
			if ( !person.getPictureUrl().equals("") )
			{
				URL imageURL;
				try {
					imageURL = new URL(os_file + person.getPictureUrl());
					ImageIcon image = new ImageIcon(imageURL);
					int iconHeight = image.getIconHeight();
					int iconWidth = image.getIconWidth();
					String imageHeight = Integer.toString(iconHeight);
					String imageWidth = Integer.toString(iconWidth);
					if ( (preferredImageHeight <= 0) && (preferredImageWidth > 0 ))
					{
						imageWidth = Integer.toString(preferredImageWidth);
						imageHeight = Integer.toString((iconHeight * preferredImageWidth) / iconWidth);
					}
					if ( (preferredImageHeight > 0) && (preferredImageWidth <= 0 ))
					{
						imageWidth = Integer.toString((iconWidth * preferredImageHeight) / iconHeight);
						imageHeight = Integer.toString(preferredImageHeight);
					}
					if ( (preferredImageHeight > 0) && (preferredImageWidth > 0 ))
					{
						imageWidth = Integer.toString(preferredImageWidth);
						imageHeight = Integer.toString(preferredImageHeight);
					}
					String htmlStr = "<img src=\"" + imageURL.toString() + "\" height=\"%HEIGHT%\" width=\"%WIDTH%\">";
					htmlStr = htmlStr.replaceAll("%HEIGHT%", imageHeight);
					htmlStr = htmlStr.replaceAll("%WIDTH%", imageWidth);
					message = message.replaceAll("%IMAGE%", htmlStr);
					} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			else
			{
				message = message.replaceAll("%IMAGE%", "");
			}
		}
		else
		{
			message = message.replaceAll("%FIRSTNAME%", "");
			message = message.replaceAll("%LASTNAME%", "");
			message = message.replaceAll("%COMPANY%", "");
			message = message.replaceAll("%STREET%", "");
			message = message.replaceAll("%POSTALCODE%", "");
			message = message.replaceAll("%CITY%", "");
			message = message.replaceAll("%IMAGE%", "");
		}

		if ( call != null )
		{
			message = message.replaceAll("%CALLED%", call.getRoute());
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy"); //$NON-NLS-1$
			message = message.replaceAll("%DATE%", df.format(call.getCalldate()));
			df = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
			message = message.replaceAll("%TIME%", df.format(call.getCalldate()));
			PhoneNumber number = call.getPhoneNumber();
			if ( call.getPhoneNumber() != null )
			{
				message = message.replaceAll("%COUNTRY%", number.getCountry());
				if (!number.getFlagFileName().equals(""))
				{
					try {
						URL flagURL = new URL(os_file + langPath + JFritzUtils.FILESEP + "flags" + JFritzUtils.FILESEP+ number.getFlagFileName());
						message = message.replaceAll("%FLAG%", "<img src=\"" + flagURL.toString() + "\">");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				else
				{
					message = message.replaceAll("%FLAG%", "<img src=\"" + imageWorldPath.toString() + "\">");
					message = message.replaceAll("%COUNTRY%", Main.getMessage("unknown_country"));
				}

				String countryCode = Main.getProperty("country.code");
				URL phoneIcon = imageHomePath;
				if(number.isLocalCall()){
					phoneIcon = imageHomePath;
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
				}else if(number.isFreeCall()){
					phoneIcon = imageFreeCallPath;
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
				}else if (number.getIntNumber().startsWith(countryCode)){
					if(number.isMobile()){
						phoneIcon = imageMobilePath;
						message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getAreaNumber());
					} else {
						phoneIcon = imagePhonePath;
						message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getFullNumber());
					}
				}else{
					message = message.replaceAll("%NUMBER%", call.getPhoneNumber().getFullNumber());
				}
				message = message.replaceAll("%PHONEICON%", "<img src=\"" + phoneIcon.toString() + "\">");

				String loc = Main.getProperty("locale");
				String googleLink = "http://maps.google.com/maps?f=q&hl="+ loc.substring(0, 2) +"&q=";
				String city = "";
				if ( person != null )
				{
					googleLink += HTMLUtil.stripEntities(person.getStreet())+",+";
					city = person.getCity();

					if ( city.replaceAll(" ", "").equals(""))
					{
						if(number.getCountryCode().equals(ReverseLookup.GERMANY_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupGermany.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.AUSTRIA_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupAustria.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.USA_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupUnitedStates.getCity(number.getAreaNumber()))+",+";
						}
						if(number.getCountryCode().equals(ReverseLookup.TURKEY_CODE))
						{
							googleLink += HTMLUtil.stripEntities(ReverseLookupTurkey.getCity(number.getAreaNumber()))+",+";
						}
					}
					googleLink += HTMLUtil.stripEntities(city) + ",+";
					googleLink += HTMLUtil.stripEntities(number.getCountry());
					message = message.replaceAll("%GOOGLE%", "<a border=\"0\" href='" + googleLink + "'><img src=\"" + googlePath + "\"></a>");
				}
				else
				{
					message = message.replaceAll("%GOOGLE%", "");
				}
			}
			else
			{
				message = message.replaceAll("%NUMBER%", "");
				message = message.replaceAll("%FLAG%", "");
				message = message.replaceAll("%COUNTRY%", "");
				message = message.replaceAll("%PHONEICON%", "");
				message = message.replaceAll("%GOOGLE%", "");
			}
		}
		else
		{
			message = message.replaceAll("%CALLED%", "");
			message = message.replaceAll("%NUMBER%", "");
			message = message.replaceAll("%DATE%", "");
			message = message.replaceAll("%TIME%", "");
			message = message.replaceAll("%FLAG%", "");
			message = message.replaceAll("%COUNTRY%", "");
			message = message.replaceAll("%PHONEICON%", "");
			message = message.replaceAll("%GOOGLE%", "");
		}

		message = message.replaceAll("<td></td>", "");
		Debug.msg(message);
		callInLabel.setText(message);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		Debug.msg("Display message...");
		pack();
		if ( this.getWidth() < 450 )
		{
			this.setSize(450, this.getHeight());
		}

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

	public String loadTemplate(String filename)
	{
		Debug.msg("Loading template " + filename + " ... ");
		preferredImageWidth = -1;
		preferredImageHeight = -1;
		String template = "";
		try {
			String line = "";
			BufferedReader test =
				 new BufferedReader(new FileReader(filename));
			while ( null != (line = test.readLine()))
			{
				if ( line.contains("#imagewidth"))
				{
					String[] linesplit = line.split(":");
					if ( linesplit.length == 2)
					{
						String command = linesplit[0];
						String pixel = linesplit[1];
						command = command.replaceAll(" ","");
						command = command.replaceAll("\t","");
						pixel = pixel.replaceAll(" ","");
						pixel = pixel.replaceAll("\t","");
						preferredImageWidth = Integer.parseInt(pixel);
					} else {
						Debug.msg("Error in parsing popup-template! Wrong syntax: Use #imagewidth:100 and/or #imageheight:150 to set width of the picture to 100 and height to 150!");
					}
				} else if ( line.contains("#imageheight"))
				{
					String[] linesplit = line.split(":");
					if ( linesplit.length == 2)
					{
						String command = linesplit[0];
						String pixel = linesplit[1];
						command = command.replaceAll(" ","");
						command = command.replaceAll("\t","");
						pixel = pixel.replaceAll(" ","");
						pixel = pixel.replaceAll("\t","");
						preferredImageHeight = Integer.parseInt(pixel);
					} else {
						Debug.msg("Error in parsing popup-template! Wrong syntax: Use #imagewidth:100 and #imageheight:150 to set width of the picture to 100 and height to 150!");
					}
				}
				template += line;
			}
			Debug.msg("Loding template done!");
		} catch (FileNotFoundException e) {
			//@todo error message or load default style
			Debug.err("Template not found! " + e.toString());
		} catch (IOException e) {
			//@todo error message or load default style
			Debug.err("IOException! " + e.toString());
		}
		return template;
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

