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
public class MessageDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1;
		protected void createGUI (String message) {
			setTitle("JFritz - Info");

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
							"/de/moonflower/jfritz/resources/images/info.png")));
			JLabel label = new JLabel(boxicon);
			label.setIconTextGap(10);
			getContentPane().add(label, BorderLayout.WEST);

			String[] splittedMessage = message.split("\\n");
			JPanel textPane = new JPanel();
			textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
			JLabel leerZeile = new JLabel(" ");
			textPane.add(leerZeile);
			for (int i = 0; i< splittedMessage.length; i++ ) {
				JLabel tmpLabel = new JLabel(splittedMessage[i]);
				textPane.add(tmpLabel);
			}
			getContentPane().add(textPane, BorderLayout.CENTER);

			setSize(300, 150);
			setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}

		public MessageDlg(String message) {
			super();

			createGUI(message);

			Timer timer = new Timer();
			HideTimer task = new HideTimer(this);
			timer.schedule(task, 10000);
		}

		private class HideTimer extends TimerTask {
			private MessageDlg msgDialog;

			public HideTimer(MessageDlg msgDialog) {
				this.msgDialog = msgDialog;
			}

			public void run() {
				msgDialog.setVisible(false);
			}
		}
}


