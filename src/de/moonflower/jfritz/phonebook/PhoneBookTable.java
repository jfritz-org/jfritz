/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.phonebook;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.callerlist.CallCellEditor;
import de.moonflower.jfritz.cellrenderer.CallTypeDateCellRenderer;
import de.moonflower.jfritz.cellrenderer.MultiLineCellRenderer;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookTable extends JTable implements KeyListener{
	private static final long serialVersionUID = 1;

	/**
	 *
	 */
	private PhoneBook phonebook;

	final private PhoneBookPanel parentPanel;
	public PhoneBookTable(PhoneBookPanel parentPanel,PhoneBook book) {
		this.phonebook = book;
		this.parentPanel = parentPanel;
		setModel(phonebook);
		setRowHeight(50);
		setFocusable(false);
		setAutoCreateColumnsFromModel(true);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addKeyListener(this);

		// setDefaultRenderer(Call.class, new CallTypeDateCellRenderer());
		getColumnModel().getColumn(4).setCellRenderer(
				new MultiLineCellRenderer());
		getColumnModel().getColumn(5).setCellRenderer(
				new CallTypeDateCellRenderer());
		getColumnModel().getColumn(0).setMinWidth(50);
		getColumnModel().getColumn(0).setMaxWidth(50);
		getColumnModel().getColumn(1).setMinWidth(60);
		getColumnModel().getColumn(1).setMaxWidth(60);

		getColumnModel().getColumn(3).setCellEditor(new CallCellEditor());

		getTableHeader().addMouseListener(new ColumnHeaderListener(getModel()));

	}

	// TODO: Select row
	public void showAndSelectRow(int row){

		getSelectionModel().clearSelection();
		getSelectionModel().setSelectionInterval(row, row);
		parentPanel.showPersonPanel();
		// assuring that the newly selected row in the phonebook is visible
		Rectangle rect = getCellRect(row, 0, true);
		scrollRectToVisible(rect);

	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(255, 255, 200));
		} else if (!isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(getBackground());
		} else {
			c.setBackground(new Color(204, 204, 255));
		}
		return c;
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// remove selection
			clearSelection();
			this.parentPanel.setStatus();
		}
		else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			// Delete selected entries
			JFritz.getJframe().getPhoneBookPanel().removeSelectedPersons();
		}
	}

	public void keyReleased(KeyEvent e) {
		//do nothing
	}

	public void keyTyped(KeyEvent e) {
		//do nothing
	}

	public void showAndSelectPersonByCall(Call call)
	{
		Person p = JFritz.getPhonebook().findPerson(call);
		if ( p == null && call.getPhoneNumber() != null)
		{
			Person person = new Person();
			person.addNumber(call.getPhoneNumber());
			Vector<Person> persons = new Vector<Person>();
			persons.add(person);
			phonebook.addEntries(persons);
			int index = phonebook.getFilteredPersons().indexOf(person);
			showAndSelectRow(index);
		} else {
			int index = phonebook.getFilteredPersons().indexOf(p);
			showAndSelectRow(index);
		}
	}

	public void showAndSelectPerson(Person person, boolean resetFilter) {
		String filterText = parentPanel.searchFilter.getText();
		if (resetFilter)
		{
			parentPanel.resetButton.doClick();
		}
		phonebook.updateFilter();
		int index = phonebook.getFilteredPersons().indexOf(person);
		showAndSelectRow(index);
		parentPanel.searchFilter.setText(filterText);
	}
}