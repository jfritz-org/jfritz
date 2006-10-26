package de.moonflower.jfritz.testsuites;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author marc
 *
 * Also wenn der JDialog ein parent hat ist es gar kein Problem, sobal beim
 * parent dispose aufgerufen wird wird der dialog geschossen Wenn man mit
 * JOptionPane.showOptionDialog(null, also ohne ein parent einen Dialog erzeugt
 * scheint es auch zu klappen evtl ruft der dialog selbst dispose auf aber bei
 * einem normalen JDialog bla = JDialog(); muss man von hand dispose aufrufen
 */
public class DialogDisposeTest implements ActionListener {

	public static void main(String[] args) {
		new DialogDisposeTest();
	}

	private JDialog testDialog;

	private JFrame testFrame;

	private JButton disposeButton;

	private JButton dialogButton;

	private JButton hideButton;

	private JButton dialogNullButton;

	private JButton optionDialogNullButton;

	private JButton disposeTestDialogButton;

	DialogDisposeTest() {
		testFrame = new JFrame();
		JPanel p = new JPanel();

		dialogButton = new JButton("dialog");
		dialogButton.addActionListener(this);
		dialogButton.setActionCommand("dialog");

		dialogNullButton = new JButton("dialog_null");
		dialogNullButton.addActionListener(this);
		dialogNullButton.setActionCommand("dialog_null");

		optionDialogNullButton = new JButton("option_dialog_null");
		optionDialogNullButton.addActionListener(this);
		optionDialogNullButton.setActionCommand("option_dialog_null");

		disposeButton = new JButton("dispose");
		disposeButton.addActionListener(this);
		disposeButton.setActionCommand("dispose");

		disposeTestDialogButton = new JButton("disposeTestDialog");
		disposeTestDialogButton.addActionListener(this);
		disposeTestDialogButton.setActionCommand("disposeTestDialog");

		hideButton = new JButton("hide");
		hideButton.addActionListener(this);
		hideButton.setActionCommand("hide");

		p.add(dialogButton);
		p.add(hideButton);
		p.add(dialogNullButton);
		p.add(optionDialogNullButton);
		p.add(disposeTestDialogButton);
		p.add(disposeButton);

		testFrame.getContentPane().add(p);
		testFrame.pack();
		testFrame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("dialog")) {
			testDialog = new JDialog(testFrame);
			testDialog.setVisible(true);
		}
		if (e.getActionCommand().equals("dialog_null")) {
			testDialog = new JDialog();
			testDialog.setVisible(true);
		}

		if (e.getActionCommand().equals("dispose")) {
			System.out.println("disposing");
			testFrame.dispose();
		}
		if (e.getActionCommand().equals("hide")) {
			System.out.println("hiding");
			testFrame.setVisible(false);
		}
		if (e.getActionCommand().equals("disposeTestDialog")) {
			testDialog.dispose();
		}
		if (e.getActionCommand().equals("option_dialog_null")) {
			// Custom button text
			Object[] options = { "Yes, please", "No, thanks",
					"No eggs, no ham!" };
			JOptionPane.showOptionDialog(null,
					"Would you like some green eggs to go " + "with that ham?",
					"A Silly Question", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		}
	}

}
