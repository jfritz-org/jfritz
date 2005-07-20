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
 * @author rob
 */
public class CallMessageDlg extends JDialog implements ActionListener{

		protected void createGUI (String caller, String called) {
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

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}

		public CallMessageDlg(String caller, String called) {
			super();

			createGUI(caller, called);

			toFront();

			Timer timer = new Timer();
			HideTimer task = new HideTimer(this);
			timer.schedule(task, 10000);
		}

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


