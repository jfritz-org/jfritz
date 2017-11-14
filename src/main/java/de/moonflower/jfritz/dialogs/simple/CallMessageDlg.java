package de.moonflower.jfritz.dialogs.simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Class for creating a popup dialog for incoming and outgoing calls. Hides after timeout
 * @author rob
 */

public class CallMessageDlg extends JFrame implements ActionListener {
	private final static Logger log = Logger.getLogger(CallMessageDlg.class);
	private static final long serialVersionUID = 1;

	private Timer timer;
	private HideTimer task;
	private String os_file = "file:";
	private String langPath = "";
	private JEditorPane callInLabel;
    private URL imageWorldPath, imagePhonePath, imageHomePath, imageFreeCallPath, imageMobilePath, googlePath;
    private String template_incoming = "", template_outgoing = "";
    private int preferredImageWidth, preferredImageHeight;
    protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public CallMessageDlg()
	{
		super();
	    addWindowListener(new WindowCloseHandle(this));
		if (System.getProperty("os.name").equals("Linux"))
			os_file += "//";
		template_incoming = loadTemplate(JFritzUtils.getFullPath(JFritzUtils.rootID + "styles") + JFritzUtils.FILESEP + "template-incoming.html");
		template_outgoing = loadTemplate(JFritzUtils.getFullPath(JFritzUtils.rootID + "styles") + JFritzUtils.FILESEP + "template-outgoing.html");

		langPath = JFritzUtils.getFullPath(JFritzUtils.langID);
		imageWorldPath = loadResource("images/world.png"); //$NON-NLS-1$
		imagePhonePath = loadResource("images/phone.png"); //$NON-NLS-1$
		imageHomePath = loadResource("images/home.png"); //$NON-NLS-1$
		imageFreeCallPath = loadResource("images/freecall.png"); //$NON-NLS-1$
		imageMobilePath = loadResource("images/handy.png"); //$NON-NLS-1$
		googlePath = loadResource("images/google.gif"); //$NON-NLS-1$
	}

	public void showIncomingCall(Call call, String callerstr, String calledstr, Person person) {

		log.info("Showing incoming call...");
		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(properties.getProperty(
				"option.popupDelay")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getClassLoader().getResource("images/callin.png"))).getImage()); //$NON-NLS-1$)
		String titleStr = messages.getMessage("dialog_title_callin");
		titleStr = titleStr.replaceAll("%SOURCE%", callerstr);
		titleStr = titleStr.replaceAll("%DEST%", calledstr);
		setTitle(titleStr); //$NON-NLS-1$

		log.info("Creating call message gui...");
		JButton closeButton = new JButton(messages.getMessage("okay")); //$NON-NLS-1$
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
                    callInLabel.setToolTipText(messages.getMessage("show_on_google_maps"));
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
			PhoneNumberOld number = call.getPhoneNumber();
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
					message = message.replaceAll("%COUNTRY%", messages.getMessage("unknown_country"));
				}

				String countryCode = properties.getProperty("country.code");
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

				if ( person != null )
				{
					String googleLink = person.getGoogleLink();
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
		log.debug(message);
		callInLabel.setText(message);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		log.info("Display message...");
		pack();
		if ( this.getWidth() < 450 )
		{
			this.setSize(450, this.getHeight());
		}

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		int posXDefault = (screenDim.width / 2) - (this.getWidth() / 2);
		int posYDefault = (screenDim.height / 2) - (this.getHeight() / 2);
		int xPos = Integer.parseInt(properties.getStateProperty("callmessenger.popup.xpos", Integer.toString(posXDefault)));
		int yPos = Integer.parseInt(properties.getStateProperty("callmessenger.popup.ypos", Integer.toString(posYDefault)));
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
		log.debug("Should be displayed...");
	}

	public void showOutgoingCall(Call call, String callerstr, String calledstr, Person person) {

		log.info("Showing outgoing call...");
		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(properties.getProperty(
				"option.popupDelay")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		this.setIconImage(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getClassLoader().getResource("images/callout.png"))).getImage()); //$NON-NLS-1$)
		String titleStr = messages.getMessage("dialog_title_callout");
		titleStr = titleStr.replaceAll("%SOURCE%", callerstr);
		titleStr = titleStr.replaceAll("%DEST%", calledstr);
		setTitle(titleStr); //$NON-NLS-1$

		log.info("Creating call message gui...");
		JButton closeButton = new JButton(messages.getMessage("okay")); //$NON-NLS-1$
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
                    callInLabel.setToolTipText(messages.getMessage("show_on_google_maps"));
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
			PhoneNumberOld number = call.getPhoneNumber();
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
					message = message.replaceAll("%COUNTRY%", messages.getMessage("unknown_country"));
				}

				String countryCode = properties.getProperty("country.code");
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

				if ( person != null )
				{
					String googleLink = person.getGoogleLink();
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
		log.debug(message);
		callInLabel.setText(message);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		log.info("Display message...");
		pack();
		if ( this.getWidth() < 450 )
		{
			this.setSize(450, this.getHeight());
		}

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		int posXDefault = (screenDim.width / 2) - (this.getWidth() / 2);
		int posYDefault = (screenDim.height / 2) - (this.getHeight() / 2);
		int xPos = Integer.parseInt(properties.getStateProperty("callmessenger.popup.xpos", Integer.toString(posXDefault)));
		int yPos = Integer.parseInt(properties.getStateProperty("callmessenger.popup.ypos", Integer.toString(posYDefault)));
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
		log.debug("Should be displayed...");
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
		properties.setStateProperty("callmessenger.popup.xpos", currentXPos);
		properties.setStateProperty("callmessenger.popup.ypos", currentYPos);
	}

	public String loadTemplate(String filename)
	{
		log.info("Loading template " + filename + " ... ");
		preferredImageWidth = -1;
		preferredImageHeight = -1;
		String template = "";
		try {
			String line = "";
			@SuppressWarnings("resource")
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
						log.error("Error in parsing popup-template! Wrong syntax: Use #imagewidth:100 and/or #imageheight:150 to set width of the picture to 100 and height to 150!");
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
						log.error("Error in parsing popup-template! Wrong syntax: Use #imagewidth:100 and #imageheight:150 to set width of the picture to 100 and height to 150!");
					}
				}
				template += line;
			}
			log.info("Loding template done!");
		} catch (FileNotFoundException e) {
			//@todo error message or load default style
			log.error("Template not found! " + e.toString());
		} catch (IOException e) {
			//@todo error message or load default style
			log.error("IOException! " + e.toString());
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

	private URL loadResource(String resourcePath) {
		return getClass().getClassLoader().getResource(resourcePath);
	}
}

