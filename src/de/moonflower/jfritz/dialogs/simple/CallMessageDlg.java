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
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class for creating a popup dialog for incoming and outgoing calls. Hides after timeout
 * @author rob
 */

public class CallMessageDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1;
		public void showMessage (String caller, String called) {
			toFront();

			Timer timer = new Timer();
			HideTimer task = new HideTimer(this);

			// Timeout 10 sec
			timer.schedule(task, 10000);

			if (caller != null) {
				setTitle("JFritz - Ankommender Anruf");
			}
			else {
				setTitle("JFritz - Abgehender Anruf");
			}

			JButton closeButton = new JButton("OK");
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
							"/de/moonflower/jfritz/resources/images/callerlist.png")));
			JLabel label = new JLabel(boxicon);
			label.setIconTextGap(10);
			getContentPane().add(label, BorderLayout.WEST);

			JPanel mainPane = new JPanel();
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
			JLabel headerLabel;
			JLabel fromLabel;
			JLabel toLabel;
			if (caller != null) {
				headerLabel = new JLabel("Ankommender Anruf");
				if (caller.equals("")) {
					fromLabel = new JLabel();
				}
				else {
					fromLabel = new JLabel("von " + caller);
					toLabel = new JLabel("an " + called);
				}
				toLabel = new JLabel("an " + called);
			}
			else {
				headerLabel = new JLabel("Abgehender Anruf an");
				fromLabel = new JLabel(called);
				toLabel = new JLabel("von " + caller);
			}
			mainPane.add(headerLabel);
			mainPane.add(fromLabel);
			mainPane.add(toLabel);

			getContentPane().add(mainPane, BorderLayout.CENTER);
			System.out.println(caller + " " + called);

			setSize(300, 150);
			setVisible(true);
		}

		/**
		 * Hide dialog after OK-Button pressed
		 */
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
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
				msgDialog.setVisible(false);
			}
		}
}


