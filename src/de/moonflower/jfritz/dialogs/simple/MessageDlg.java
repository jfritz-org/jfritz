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

import java.util.Timer;
import java.util.TimerTask;


/**
 * Class for creating a popup dialog. Hides after timeout
 * @author rob
 */
public class MessageDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1;

		/**
		 * Show popup dialog with text: message
		 * @param message
		 */
		public void showMessage (String message, long delay) {
			toFront();
			Timer timer = new Timer();
			HideTimer task = new HideTimer(this);

			//if the delay is <=0 then dont close the dialog
			if(delay > 0)
				timer.schedule(task, delay);

			setTitle(Main.getMessage("dialog_title_popup_info")); //$NON-NLS-1$

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
							"/de/moonflower/jfritz/resources/images/info.png"))); //$NON-NLS-1$
			JLabel label = new JLabel(boxicon);
			label.setIconTextGap(10);
			getContentPane().add(label, BorderLayout.WEST);

			String[] splittedMessage = message.split("\\n"); //$NON-NLS-1$
			JPanel textPane = new JPanel();
			textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
			JLabel leerZeile = new JLabel(" "); //$NON-NLS-1$
			textPane.add(leerZeile);
			for (int i = 0; i< splittedMessage.length; i++ ) {
				JLabel tmpLabel = new JLabel(splittedMessage[i]);
				textPane.add(tmpLabel);
			}
			getContentPane().add(textPane, BorderLayout.CENTER);

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
			private MessageDlg msgDialog;

			public HideTimer(MessageDlg msgDialog) {
				this.msgDialog = msgDialog;
			}

			public void run() {
				msgDialog.setVisible(false);
				msgDialog.dispose();
			}
		}
}


