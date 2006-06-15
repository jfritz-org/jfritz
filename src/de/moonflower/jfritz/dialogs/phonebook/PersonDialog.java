/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PersonDialog extends JDialog implements ActionListener,KeyListener  {
	private static final long serialVersionUID = 1;
	private JFritz jfritz;

	private Person person;

	private PersonPanel personPanel;

	JButton okButton, cancelButton;

	private boolean pressed_OK = false;

	/**
	 *
	 * @param jfritz JFritz object
	 * @param person Person object
	 * @throws HeadlessException
	 */
	public PersonDialog(JFritz jfritz, Person person) throws HeadlessException {
		super();
		this.jfritz = jfritz;
		this.person = new Person(person);
		if (this.person == null)
			person = new Person();
		this.setLocationRelativeTo(jfritz.getJframe());
		drawDialog();
	}

	private void drawDialog() {
		super.dialogInit();
		setTitle(JFritz.getMessage("dialog_title_phonebook_edit_person")); //$NON-NLS-1$
		setModal(true);
		getContentPane().setLayout(new BorderLayout());

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();

		// Top Pane
		JLabel label = new JLabel(JFritz.getMessage("dialog_title_phonebook_edit_person")); //$NON-NLS-1$
		topPane.add(label);

		// Main Pane
		personPanel = new PersonPanel(jfritz, person);

		// Bottom Pane
		okButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$
		okButton.setActionCommand("ok"); //$NON-NLS-1$
		okButton.addActionListener(this);

		cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
		cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(this);

		bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		//set default confirm button (Enter)
		getRootPane().setDefaultButton(okButton);

		//set default close button (ESC)
		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction()
		{
			private static final long serialVersionUID = 2L;

			public void actionPerformed(ActionEvent e)
			{
				 cancelButton.doClick();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);

		getContentPane().add(topPane, BorderLayout.NORTH);
		getContentPane().add(personPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);
		setSize(new Dimension(350, 400));

		//sets the center (x/y) of Edit dialog to center of JFritz frame
		this.setLocationRelativeTo(jfritz.getJframe());
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	public boolean showDialog() {
		setVisible(true);
		person.setPrivateEntry(personPanel.isPrivateEntry());
		person.setFirstName(personPanel.getFirstName());
		person.setCompany(personPanel.getCompany());
		person.setLastName(personPanel.getLastName());
		person.setCity(personPanel.getCity());
		person.setEmailAddress(personPanel.getEmail());
		person.setStreet(personPanel.getStreet());
		person.setPostalCode(personPanel.getPostalCode());
		return okPressed();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) { //$NON-NLS-1$
			pressed_OK = true;
			setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) { //$NON-NLS-1$
			pressed_OK = false;
			setVisible(false);
		}
	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	public void keyPressed( KeyEvent ke )
    {
    if( ke.getKeyCode() == KeyEvent.VK_ESCAPE )
       {
       this.dispose();
       }
    }
	public void keyReleased( KeyEvent ke )  { ; }
	public void keyTyped( KeyEvent ke ) { ; }

}
