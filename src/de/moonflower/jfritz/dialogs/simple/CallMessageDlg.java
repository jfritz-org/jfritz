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
 * Class for creating a popup dialog for incoming and outgoing calls. Hides after timeout
 * @author rob
 */

public class CallMessageDlg {}
/*extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1;
		public void showMessage (String caller, String called) {
			toFront();

			Timer timer = new Timer();
			HideTimer task = new HideTimer(this);

			// Timeout 10 sec
			timer.schedule(task, 10000);

			if (caller != null) {
				setTitle(Main.getMessage("dialog_title_callin")); //$NON-NLS-1$
			}
			else {
				setTitle(Main.getMessage("dialog_title_callout")); //$NON-NLS-1$
			}

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
			JLabel headerLabel;
			JLabel fromLabel;
			JLabel toLabel;
			if (caller != null) {
				headerLabel = new JLabel(Main.getMessage("dialog_title_callin"));	//$NON-NLS-1$
				if (caller.equals("")) { //$NON-NLS-1$
					fromLabel = new JLabel();
				}
				else {
					fromLabel = new JLabel(Main.getMessage("from") + caller);	//$NON-NLS-1$
//FIXME toLabel is overwritten in the next line
		//			toLabel = new JLabel(Main.getMessage("to") + called);	//$NON-NLS-1$
				}

				toLabel = new JLabel(Main.getMessage("through_provider") + called);	//$NON-NLS-1$
			}
			else {
				headerLabel = new JLabel(Main.getMessage("dialog_title_callout")	//$NON-NLS-1$
						+ " " + Main.getMessage("through_provider")); //$NON-NLS-1$, 	//$NON-NLS-2$
				fromLabel = new JLabel(called);
				//TODO caller == null maybe insert a message saying could not find caller or something
//				toLabel = new JLabel(Main.getMessage("from") + caller);	//$NON-NLS-1$
				toLabel = new JLabel(Main.getMessage("from")+Main.getMessage("unknown"));	//$NON-NLS-1$
			}
			mainPane.add(headerLabel);
			mainPane.add(fromLabel);
			mainPane.add(toLabel);

			getContentPane().add(mainPane, BorderLayout.CENTER);

			setSize(300, 150);
			setVisible(true);
		}

		/**
		 * Hide dialog after OK-Button pressed
		 */
/*	public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}

		/**
		 * Hide dialog after timeout
		 * @author rob
		 */
/*		private class HideTimer extends TimerTask {
			private CallMessageDlg msgDialog;

			public HideTimer(CallMessageDlg msgDialog) {
				this.msgDialog = msgDialog;
			}

			public void run() {
				msgDialog.setVisible(false);
				msgDialog.dispose();
			}
		}
}
*/

