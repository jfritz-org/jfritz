/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import de.moonflower.jfritz.callerlist.PersonCellEditor;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 *
 */
public class PersonEditorPanel extends JComponent {
	private static final long serialVersionUID = 1;

	private PersonCellEditor editor;

	private Person person;

	private JLabel input;

	/**
	 *
	 */
	public PersonEditorPanel(PersonCellEditor editor) {
		super();
		this.editor = editor;
		drawPanel();
	}

	private void drawPanel() {
		setLayout(new BorderLayout());
		input = new JLabel("  ");
		input.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					PersonDialog p = new PersonDialog(editor.getJfritz(),
							person);
					if (p.showDialog()) {
						person = p.getPerson();
						input.setText(person.getFullname());
						editor.stopCellEditing();
						if (p.okPressed()) {
							editor.getJfritz().getPhonebook().saveToXMLFile(
									JFritz.PHONEBOOK_FILE);
							editor.getJfritz().getPhonebook().sortAllFilteredRows();
						}
					}
					p.dispose();
				}
			}
		});
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/person.png")));

		input.setFocusable(false);
		input.setForeground(new Color(127,127,255));
		input.setIcon(icon);
		add(input, BorderLayout.CENTER);
	}

	public void setText(String text) {
		input.setText(text);
	}

	public String getText() {
		return input.getText();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	/**
	 * @param person
	 *            The person to set.
	 */
	public final void setPerson(Person person) {
		this.person = person;
	}
}
