/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;

/**
 * @author Arno Willig
 *
 */
public class CallerListPanel extends JPanel implements ActionListener,
		CaretListener {
	private static final long serialVersionUID = 1;

	private JFritz jfritz;

	private CallerTable callerTable;

	private JToggleButton dateButton;

	private JButton deleteEntriesButton;

	private JTextField searchFilter;

	private JPopupMenu popupMenu;

	public CallerListPanel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;

		setLayout(new BorderLayout());
		add(createCallerListToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
	}

	public JToolBar createCallerListToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		JToggleButton tb = new JToggleButton(getImage("callin_grey.png"), true);
		tb.setSelectedIcon(getImage("callin.png"));
		tb.setActionCommand("filter_callin");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("filter_callin"));
		tb.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.callin", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("callinfailed_grey.png"), true);
		tb.setSelectedIcon(getImage("callinfailed.png"));
		tb.setActionCommand("filter_callinfailed");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("filter_callinfailed"));
		tb.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.callinfailed", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("callout_grey.png"), true);
		tb.setSelectedIcon(getImage("callout.png"));
		tb.setActionCommand("filter_callout");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("filter_callout"));
		tb.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.callout", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("phone_grey.png"), true);
		tb.setSelectedIcon(getImage("phone.png"));
		tb.setActionCommand("filter_number");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("filter_number"));
		tb.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.number", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("handy_grey.png"), true);
		tb.setSelectedIcon(getImage("handy.png"));
		tb.setActionCommand("filter_handy");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("filter_handy"));
		tb.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.handy", "false")));
		toolBar.add(tb);

		dateButton = new JToggleButton(getImage("calendar_grey.png"), true);
		dateButton.setSelectedIcon(getImage("calendar.png"));
		dateButton.setActionCommand("filter_date");
		dateButton.addActionListener(this);
		dateButton.setToolTipText(JFritz.getMessage("filter_date"));
		dateButton.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.date", "false")));
		setDateFilterText();
		toolBar.add(dateButton);

		JToggleButton sipButton = new JToggleButton(
				getImage("world_grey.png"), true);
		sipButton.setSelectedIcon(getImage("world.png"));
		sipButton.setActionCommand("filter_sip");
		sipButton.addActionListener(this);
		sipButton.setToolTipText("Anrufe nach SIP-Providern filtern");
		sipButton.setSelected(!JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter.sip", "false")));
		setDateFilterText();
		toolBar.add(sipButton);

		toolBar.addSeparator();

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries"));
		deleteEntriesButton.setActionCommand("delete_entry");
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImage("delete.png"));
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);
		toolBar.add(deleteEntriesButton);

		toolBar.addSeparator();

		toolBar.add(new JLabel(JFritz.getMessage("search") + ": "));
		searchFilter = new JTextField(JFritz.getProperty("filter.search", ""),
				10);
		searchFilter.addCaretListener(this);
		searchFilter.addCaretListener(new CaretListener() {
			String filter = "";

			public void caretUpdate(CaretEvent e) {
				JTextField search = (JTextField) e.getSource();
				if (!filter.equals(search.getText())) {
					filter = search.getText();
					JFritz.setProperty("filter.search", filter);
					jfritz.getCallerlist().updateFilter();
					jfritz.getCallerlist().fireTableStructureChanged();
				}
			}

		});

		toolBar.add(searchFilter);
		JButton button = new JButton(JFritz.getMessage("clear"));
		button.setActionCommand("clearSearchFilter");
		button.addActionListener(this);
		toolBar.add(button);
		return toolBar;

	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable(jfritz);
		popupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Reverselookup ...");
		menuItem.setActionCommand("reverselookup");
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);

		MouseAdapter popupListener = new PopupListener();


		callerTable.addMouseListener(popupListener);
		return new JScrollPane(callerTable);
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename)));
	}

	public void setSearchFilter(String text) {
		searchFilter.setText(text);
	}

	public void setDateFilterText() {
		if (JFritzUtils.parseBoolean(JFritz.getProperty("filter.date"))) {
			if (JFritz.getProperty("filter.date_from").equals(
					JFritz.getProperty("filter.date_to"))) {
				dateButton.setText(JFritz.getProperty("filter.date_from"));
			} else {
				dateButton.setText(JFritz.getProperty("filter.date_from")
						+ " - " + JFritz.getProperty("filter.date_to"));
			}
		} else {
			dateButton.setText("");
		}
	}

	public void setSipProviderFilterFromSelection() {
		Vector filteredProviders = new Vector();
		try {
			int rows[] = callerTable.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) jfritz.getCallerlist()
						.getFilteredCallVector().get(rows[i]);
				String route = call.getRoute();
				if (route.equals("")) {
					route = "FIXEDLINE";
				}
				if (!filteredProviders.contains(route)) {
						filteredProviders.add(route);
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		JFritz.setProperty("filter.sipProvider", filteredProviders.toString());
		jfritz.getCallerlist().updateFilter();
	}

	public void setDateFilterFromSelection() {
		Date from = null;
		Date to = null;
		try {
			int rows[] = callerTable.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) jfritz.getCallerlist()
						.getFilteredCallVector().get(rows[i]);

				if (to == null || call.getCalldate().after(to))
					to = call.getCalldate();

				if (from == null || call.getCalldate().before(from))
					from = call.getCalldate();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		if (to == null)
			to = new Date();
		if (from == null)
			from = new Date();
		String fromstr = new SimpleDateFormat("dd.MM.yy").format(from);
		String tostr = new SimpleDateFormat("dd.MM.yy").format(to);

		JFritz.setProperty("filter.date_from", fromstr);
		JFritz.setProperty("filter.date_to", tostr);
		setDateFilterText();
		jfritz.getCallerlist().updateFilter();
	}

	public CallerTable getCallerTable() {
		return callerTable;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("filter_callin")) {
			JFritz.setProperty("filter.callin", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();

		} else if (e.getActionCommand().equals("filter_callinfailed")) {
			JFritz.setProperty("filter.callinfailed", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_callout")) {
			JFritz.setProperty("filter.callout", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_number") {
			JFritz.setProperty("filter.number", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_handy") {
			JFritz.setProperty("filter.handy", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_date") {
			JFritz.setProperty("filter.date", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setDateFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_sip") {
			JFritz.setProperty("filter.sip", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setSipProviderFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "clearSearchFilter") {
			setSearchFilter("");
			JFritz.setProperty("filter.search", "");
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "delete_entry") {
			jfritz.getCallerlist().removeEntries();
		} else if (e.getActionCommand().equals("reverselookup")) {
			doReverseLookup();
		}
	}

	/**
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent e) {
		String filter = "";
		JTextField search = (JTextField) e.getSource();
		if (!filter.equals(search.getText())) {
			filter = search.getText();
			JFritz.setProperty("filter.search", filter);
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		}

	}

	public void setDeleteListButton() {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_list"));
		// clearList-Icon to big, so use std. delete.png
		// deleteEntriesButton.setIcon(getImage("clearList.png"));
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntriesButton(int rows) {
		deleteEntriesButton.setToolTipText(rows + " "
				+ JFritz.getMessage("delete_entries"));
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntryButton() {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entry"));
		deleteEntriesButton.setEnabled(true);
	}

	public void disableDeleteEntriesButton() {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries"));
		deleteEntriesButton.setEnabled(false);
	}

	private void doReverseLookup() {
		int rows[] = callerTable.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			Call call = (Call) jfritz.getCallerlist()
			.getFilteredCallVector().get(rows[i]);
			Person newPerson = ReverseLookup.lookup(call.getPhoneNumber());
			if (newPerson != null) {
				jfritz.getPhonebook().addEntry(newPerson);
				jfritz.getPhonebook().fireTableDataChanged();
				jfritz.getCallerlist()
						.fireTableDataChanged();
			}
		}
	}

	class PopupListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					jfritz.getJframe().activatePhoneBook();
				}
			}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e
						.getComponent(), e.getX(), e.getY());
			}
		}
	}


}
