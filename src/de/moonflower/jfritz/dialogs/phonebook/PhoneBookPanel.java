/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookPanel extends JPanel implements ListSelectionListener,
		PropertyChangeListener, ActionListener {

	private final int PERSONPANEL_WIDTH = 350;

	private JFritz jfritz;

	private PhoneBookTable phoneBookTable;

	private PersonPanel personPanel;

	private JSplitPane splitPane;

	private JButton saveButton, cancelButton;

	public PhoneBookPanel(JFritz jfritz) {
		this.jfritz = jfritz;
		setLayout(new BorderLayout());

		JPanel editPanel = createEditPanel();

		add(createPhoneBookToolBar(), BorderLayout.NORTH);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(editPanel);
		splitPane.setRightComponent(createPhoneBookTable());

		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(0);
	}

	/**
	 * @return editPanel
	 */
	private JPanel createEditPanel() {
		JPanel editPanel = new JPanel(new BorderLayout());
		JPanel editButtonPanel = new JPanel();
		saveButton = new JButton(JFritz.getMessage("save"));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		saveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png"))));
		cancelButton = new JButton(JFritz.getMessage("reset"));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		editButtonPanel.add(saveButton);
		editButtonPanel.add(cancelButton);

		personPanel = new PersonPanel(jfritz, new Person());
		personPanel.addPropertyChangeListener("hasChanged", this);
		editPanel.add(personPanel, BorderLayout.CENTER);
		editPanel.add(editButtonPanel, BorderLayout.SOUTH);
		return editPanel;
	}

	public JToolBar createPhoneBookToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);
		JButton addButton = new JButton(JFritz.getMessage("new_entry"));
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png"))));
		addButton.setActionCommand("addPerson");
		addButton.addActionListener(this);
		JButton delButton = new JButton(JFritz.getMessage("del_entry"));
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));
		delButton.setActionCommand("deletePerson");
		delButton.addActionListener(this);
		toolBar.add(addButton);
		toolBar.add(delButton);
		return toolBar;
	}

	public JScrollPane createPhoneBookTable() {
		phoneBookTable = new PhoneBookTable(jfritz);
		phoneBookTable.getSelectionModel().addListSelectionListener(this);
		phoneBookTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int loc = splitPane.getDividerLocation();
					if (loc < PERSONPANEL_WIDTH)
						splitPane.setDividerLocation(PERSONPANEL_WIDTH);
					else
						splitPane.setDividerLocation(0);
				}
			}

		});
		return new JScrollPane(phoneBookTable);
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = phoneBookTable.getSelectedRow();
			if (row > -1) {
				Person p = ((PhoneBook) phoneBookTable.getModel())
						.getPersonAt(row);
				personPanel.setPerson(p);
			}
		}
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		saveButton.setEnabled(personPanel.hasChanged());
		cancelButton.setEnabled(personPanel.hasChanged());
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel")) {
			personPanel.updateGUI();
		} else if (e.getActionCommand().equals("save")) {
			personPanel.updatePerson();
			jfritz.getPhonebook().fireTableDataChanged();
		} else if (e.getActionCommand().equals("addPerson")) {
			jfritz.getPhonebook().addEntry(new Person("", " NEU "));
			jfritz.getPhonebook().fireTableDataChanged();
		} else if (e.getActionCommand().equals("deletePerson")) {
			jfritz.getPhonebook().deleteEntry(personPanel.getPerson());
			jfritz.getPhonebook().fireTableDataChanged();
		}
		propertyChange(null);
	}

	/**
	 * @return Returns the phoneBookTable.
	 */
	public final PhoneBookTable getPhoneBookTable() {
		return phoneBookTable;
	}

	/**
	 * @return Returns the personPanel.
	 */
	public final PersonPanel getPersonPanel() {
		return personPanel;
	}

	public void showPersonPanel() {
		splitPane.setDividerLocation(PERSONPANEL_WIDTH);
	}
}
