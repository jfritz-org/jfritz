/*
 * Created on 19.07.2005
 */
package de.moonflower.jfritz.dialogs.simple;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Person;

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

	public CallMessageDlg()
	{
		super();
	    addWindowListener(new WindowCloseHandle(this));
	}

	public void showIncomingCall(String callerstr, String calledstr, String name, String portstr, Person person) {

		String callInMsg = "<html>Name: %Name (%Company)<br>Nummer: %Number<br>Leitung: %Port</html>";

		toFront();

		timer = new Timer();
		task = new HideTimer(this);

		//if the delay is <=0 then dont close the dialog
		long delay = Long.parseLong(Main.getProperty(
				"option.popupDelay", "10")) * 1000;
		if(delay > 0)
			timer.schedule(task, delay);

		setTitle(Main.getMessage("dialog_title_callin")); //$NON-NLS-1$

		JButton closeButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		closeButton.addActionListener(this);
		getContentPane().setLayout(new BorderLayout(15, 15));
		getContentPane().add(closeButton, BorderLayout.SOUTH);
		getContentPane().getInsets().left = 15;
		Toolkit 	tk 	= getToolkit();
		Dimension dim 	= tk.getScreenSize();
		int 		x	= (dim.width / 2) - 200;
		int 		y	= (dim.height / 2) - 250;
		setLocation(x, y);
		ImageIcon boxicon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callerlist.png"))); //$NON-NLS-1$
		JLabel label = new JLabel(boxicon);
		label.setIconTextGap(10);
		getContentPane().add(label, BorderLayout.WEST);

		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		JLabel callInLabel = new JLabel("");

		callInMsg = callInMsg.replaceAll("%Number", callerstr); //$NON-NLS-1$
		callInMsg = callInMsg.replaceAll("%Name", name); //$NON-NLS-1$
		callInMsg = callInMsg.replaceAll("%Called", calledstr); //$NON-NLS-1$
		callInMsg = callInMsg.replaceAll("%Port", portstr); //$NON-NLS-1$
		String firstname = "";
		String surname = "";
		String company = "";
		if ( person != null )
		{
			firstname = person.getFirstName();
			surname = person.getLastName();
			company = person.getCompany();
		}
		callInMsg = callInMsg.replaceAll("%Firstname", firstname); //$NON-NLS-1$
		callInMsg = callInMsg.replaceAll("%Surname", surname); //$NON-NLS-1$
		callInMsg = callInMsg.replaceAll("%Company", company); //$NON-NLS-1$

		callInLabel.setText(callInMsg);
		mainPane.add(callInLabel);

		getContentPane().add(mainPane, BorderLayout.CENTER);

		setSize(300, 150);
		setVisible(true);
		toFront();
	}

	/**
	 * Hide dialog after OK-Button pressed
	 */
	public void actionPerformed(ActionEvent e) {
		timer.cancel();
		task.cancel();
		setVisible(false);
		dispose();
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
			timer.cancel();
			task.cancel();
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
			timer.cancel();
			task.cancel();
			msgDialog.dispose();
        }
	}

}


//setTitle(Main.getMessage("dialog_title_callout")); //$NON-NLS-1$
//headerLabel = new JLabel(Main.getMessage("dialog_title_callout")	//$NON-NLS-1$
//		+ " " + Main.getMessage("through_provider")); //$NON-NLS-1$, 	//$NON-NLS-2$
//fromLabel = new JLabel(called);
////TODO caller == null maybe insert a message saying could not find caller or something
////toLabel = new JLabel(Main.getMessage("from") + caller);	//$NON-NLS-1$
//toLabel = new JLabel(Main.getMessage("from")+Main.getMessage("unknown"));	//$NON-NLS-1$
