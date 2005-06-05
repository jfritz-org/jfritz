/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookPanel extends JPanel {
	private JFritz jfritz;

	public PhoneBookPanel(JFritz jfritz) {
		this.jfritz = jfritz;
		setLayout(new BorderLayout());

		JPanel editPanel = createEditPanel();

		add(createPhoneBookToolBar(), BorderLayout.NORTH);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(editPanel);
		splitPane.setRightComponent(createPhoneBookTable());

		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		add(splitPane, BorderLayout.CENTER);
		//		add(createPhoneBookTable(), BorderLayout.CENTER);
	}

	/**
	 * @return editPanel
	 */
	private JPanel createEditPanel() {
		JPanel editPanel = new JPanel(new BorderLayout());

		JPanel editButtonPanel = new JPanel();
		JButton saveButton = new JButton("save");
		JButton cancelButton = new JButton("cancel");
		editButtonPanel.add(saveButton);
		editButtonPanel.add(cancelButton);

		PersonPanel pp = new PersonPanel(jfritz, new Person());
		editPanel.add(pp, BorderLayout.CENTER);
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
		JTable quickdialtable = new PhoneBookTable(jfritz);
		return new JScrollPane(quickdialtable);
	}
}
