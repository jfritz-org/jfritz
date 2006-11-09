/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.phonebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 * TODO: Update des Phonebooks und der CallerList nach dem OK
 */
public class PersonDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1;

	private PhoneBook phoneBook;

	private Person person;

	private PersonPanel personPanel;

	/**
	 *
	 * @param person
	 *            Person object
	 * @throws HeadlessException
	 */
	public PersonDialog(PhoneBook phoneBook, Person person) throws HeadlessException {
		super(JFritz.getJframe()); // parent needed for Dialog's icon
		this.person = person;
		this.phoneBook = phoneBook;
		drawDialog();
		// centers PersonDialog in JFritz application window
		// needs to be called after drawDialog (Dialog needs size)
		this.setLocationRelativeTo(JFritz.getJframe());
	}

	private void drawDialog() {
		super.dialogInit();
		setTitle(Main.getMessage("dialog_title_phonebook_edit_person")); //$NON-NLS-1$
		setModal(true);
		getContentPane().setLayout(new BorderLayout());

		JPanel topPane = new JPanel();

		// Top Pane
		JLabel label = new JLabel(Main
				.getMessage("dialog_title_phonebook_edit_person")); //$NON-NLS-1$
		topPane.add(label);

		// Main Pane
		personPanel = new PersonPanel(person, phoneBook);
		personPanel.addActionListener(this);

		// set default close button (ESC)
		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
				0, false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 2L;

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
		getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

		getContentPane().add(topPane, BorderLayout.NORTH);
		getContentPane().add(personPanel, BorderLayout.CENTER);
		setSize(new Dimension(350, 400));
	}

	public void showDialog() {
		setVisible(true);
	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("ok")
				|| arg0.getActionCommand().equals("cancel")) {
			this.setVisible(false);
		}
	}

}
