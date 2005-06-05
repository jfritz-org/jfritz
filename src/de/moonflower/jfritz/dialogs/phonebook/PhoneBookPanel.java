/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
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
	private JFritz jfritz;

	private JTable phoneBookTable;

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
		saveButton = new JButton(jfritz.getMessages().getString("save"));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		cancelButton = new JButton(jfritz.getMessages().getString("reset"));
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
		toolBar.add(new JLabel("Phonebook-ToolBar"));
		return toolBar;
	}

	public JScrollPane createPhoneBookTable() {
		phoneBookTable = new PhoneBookTable(jfritz);
		phoneBookTable.getSelectionModel().addListSelectionListener(this);
		phoneBookTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int loc = splitPane.getDividerLocation();
					if (loc < 350)
						splitPane.setDividerLocation(350);
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
		}
		propertyChange(null);
	}

}
