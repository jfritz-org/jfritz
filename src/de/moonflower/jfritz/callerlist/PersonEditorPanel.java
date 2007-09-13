/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.phonebook.PersonDialog;
import de.moonflower.jfritz.struct.Person;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 *
 */
public class PersonEditorPanel extends JComponent {
	private static final long serialVersionUID = 1;

	private CallerList callerList;

	private Person person;

	private AbstractCellEditor cellEditor;

	private JLabel input;

	/**
	 *
	 */
	public PersonEditorPanel(AbstractCellEditor cellEditor, CallerList callerList) {
		super();
		this.callerList = callerList;
		this.cellEditor = cellEditor;
		drawPanel();
	}

	private void drawPanel() {
		setLayout(new BorderLayout());
		input = new JLabel("  ");  //$NON-NLS-1$
		input.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {

					callerList.getPhoneBook().addEntry(person);
					callerList.getPhoneBook().addFilterException(person);
					callerList.getPhoneBook().updateFilter();
					callerList.getPhoneBook().fireTableDataChanged();
					int index = callerList.getPhoneBook().indexOf(person);
					JFritz.getJframe().getPhoneBookPanel().getPhoneBookTable().getSelectionModel()
							.setSelectionInterval(index, index);


					JFritz.getJframe().activatePhoneBook();

					/* Old code, not compatible with the current interface
					PersonDialog p = new PersonDialog(callerList.getPhoneBook(), person);
					p.showDialog();
					cellEditor.stopCellEditing();
					callerList.update();
					p.dispose();
					*/
				}
			}
		});
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/person.png"))); //$NON-NLS-1$

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
