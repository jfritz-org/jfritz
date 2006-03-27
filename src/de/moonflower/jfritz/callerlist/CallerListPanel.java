/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.JFritzClipboard;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.ReverseLookup;

/**
 * @author Arno Willig
 *
 */
public class CallerListPanel extends JPanel
		implements
			ActionListener,
			KeyListener {
	private static final long serialVersionUID = 1;

	private static final int DATEFILTER_SELECTION = 0;

	private static final int DATEFILTER_TODAY = 1;

	private static final int DATEFILTER_THIS_MONTH = 2;

	private static final int DATEFILTER_LAST_MONTH = 3;

	private static final int DATEFILTER_YESTERDAY = 4;

	private static final int MISSED_FILTER_WITHOUT_COMMENTS = 0;

	private static final int MISSED_FILTER_WITHOUT_COMMENTS_LAST_WEEK = 1;

	private JFritz jfritz;

	private CallerTable callerTable;

	private JToggleButton dateFilterButton, callByCallFilterButton,
			callinFilterButton, calloutFilterButton, callinfailedFilterButton,
			numberFilterButton, fixedFilterButton, handyFilterButton,
			sipFilterButton, commentFilterButton;

	private JButton deleteEntriesButton;

	private JTextField searchFilter;

	public CallerListPanel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;

		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
	}

	public JPanel createToolBar() {
		JToolBar upperToolBar = new JToolBar();
		upperToolBar.setFloatable(true);
		JToolBar lowerToolBar = new JToolBar();
		lowerToolBar.setFloatable(true);

		JButton button = new JButton();
		button.setActionCommand("export_csv");
		button.addActionListener(this);
		button.setIcon(getImage("csv_export.png"));
		button.setToolTipText(JFritz.getMessage("export_csv"));
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("import_csv");
		button.addActionListener(this);
		button.setIcon(getImage("csv_import.png"));
		button.setToolTipText("CSV-Datei importieren");
		button.setEnabled(false);
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("export_xml");
		button.addActionListener(this);
		button.setIcon(getImage("xml_export.png"));
		button.setToolTipText("XML-Datei exportieren");
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("import_xml");
		button.addActionListener(this);
		button.setIcon(getImage("xml_import.png"));
		button.setToolTipText("XML-Datei importieren");
		button.setEnabled(false);
		upperToolBar.add(button);

		upperToolBar.addSeparator();

		callinFilterButton = new JToggleButton(getImage("callin_grey.png"),
				true);
		callinFilterButton.setSelectedIcon(getImage("callin.png"));
		callinFilterButton.setActionCommand("filter_callin");
		callinFilterButton.addActionListener(this);
		callinFilterButton.setToolTipText(JFritz.getMessage("filter_callin"));
		callinFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callin", "false")));
		lowerToolBar.add(callinFilterButton);

		callinfailedFilterButton = new JToggleButton(
				getImage("callinfailed_grey.png"), true);
		callinfailedFilterButton.setSelectedIcon(getImage("callinfailed.png"));
		callinfailedFilterButton.setActionCommand("filter_callinfailed");
		callinfailedFilterButton.addActionListener(this);
		callinfailedFilterButton.setToolTipText(JFritz
				.getMessage("filter_callinfailed"));
		callinfailedFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callinfailed", "false")));

		JPopupMenu missedPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Entgangene ohne Kommentar (letzte Woche)");
		menuItem
				.setActionCommand("filter_callinfailed_allWithoutCommentLastWeek");
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		menuItem = new JMenuItem("Entgangene ohne Kommentar (alle)");
		menuItem.setActionCommand("filter_callinfailed_allWithoutComment");
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		MouseAdapter popupListener = new PopupListener(missedPopupMenu);
		callinfailedFilterButton.addMouseListener(popupListener);
		lowerToolBar.add(callinfailedFilterButton);

		calloutFilterButton = new JToggleButton(getImage("callout_grey.png"),
				true);
		calloutFilterButton.setSelectedIcon(getImage("callout.png"));
		calloutFilterButton.setActionCommand("filter_callout");
		calloutFilterButton.addActionListener(this);
		calloutFilterButton.setToolTipText(JFritz.getMessage("filter_callout"));
		calloutFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callout", "false")));
		lowerToolBar.add(calloutFilterButton);

		numberFilterButton = new JToggleButton(
				getImage("phone_nonumber_grey.png"), true);
		numberFilterButton.setSelectedIcon(getImage("phone_nonumber.png"));
		numberFilterButton.setActionCommand("filter_number");
		numberFilterButton.addActionListener(this);
		numberFilterButton.setToolTipText(JFritz.getMessage("filter_number"));
		numberFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.number", "false")));
		lowerToolBar.add(numberFilterButton);

		fixedFilterButton = new JToggleButton(getImage("phone_grey.png"), true);
		fixedFilterButton.setSelectedIcon(getImage("phone.png"));
		fixedFilterButton.setActionCommand("filter_fixed");
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(JFritz.getMessage("filter_fixed"));
		fixedFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.fixed", "false")));
		lowerToolBar.add(fixedFilterButton);

		handyFilterButton = new JToggleButton(getImage("handy_grey.png"), true);
		handyFilterButton.setSelectedIcon(getImage("handy.png"));
		handyFilterButton.setActionCommand("filter_handy");
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(JFritz.getMessage("filter_handy"));
		handyFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.handy", "false")));
		lowerToolBar.add(handyFilterButton);

		dateFilterButton = new JToggleButton(getImage("calendar_grey.png"),
				true);
		dateFilterButton.setSelectedIcon(getImage("calendar.png"));
		dateFilterButton.setActionCommand("filter_date");
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(JFritz.getMessage("filter_date"));
		dateFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.date", "false")));
		setDateFilterText();
		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem("Heutiger Tag");
		menuItem.setActionCommand("setdatefilter_thisday");
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem("Gestern");
		menuItem.setActionCommand("setdatefilter_yesterday");
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem("Dieser Monat");
		menuItem.setActionCommand("setdatefilter_thismonth");
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem("Letzter Monat");
		menuItem.setActionCommand("setdatefilter_lastmonth");
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		popupListener = new PopupListener(datePopupMenu);

		dateFilterButton.addMouseListener(popupListener);

		lowerToolBar.add(dateFilterButton);

		sipFilterButton = new JToggleButton(getImage("world_grey.png"), true);
		sipFilterButton.setSelectedIcon(getImage("world.png"));
		sipFilterButton.setActionCommand("filter_sip");
		sipFilterButton.addActionListener(this);
		sipFilterButton.setToolTipText(JFritz.getMessage("filter_sip"));
		sipFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.sip", "false")));
		lowerToolBar.add(sipFilterButton);

		callByCallFilterButton = new JToggleButton(
				getImage("callbycall_grey.png"), true);
		callByCallFilterButton.setSelectedIcon(getImage("callbycall.png"));
		callByCallFilterButton.setActionCommand("filter_callbycall");
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(JFritz
				.getMessage("filter_callbycall"));
		callByCallFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callbycall", "false")));
		lowerToolBar.add(callByCallFilterButton);

		commentFilterButton = new JToggleButton(getImage("commentFilter.png"),
				true);
		commentFilterButton.setSelectedIcon(getImage("commentFilter.png"));
		commentFilterButton.setActionCommand("filter_comment");
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(JFritz.getMessage("filter_comment"));
		commentFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.comment", "false")));
		lowerToolBar.add(commentFilterButton);

		lowerToolBar.addSeparator();
		lowerToolBar.addSeparator();

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries"));
		deleteEntriesButton.setActionCommand("delete_entry");
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImage("delete.png"));
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);
		lowerToolBar.add(deleteEntriesButton);

		lowerToolBar.addSeparator();

		lowerToolBar.add(new JLabel(JFritz.getMessage("search") + ": "));
		searchFilter = new JTextField(JFritz.getProperty("filter.search", ""),
				10);
		searchFilter.addKeyListener(this);
		// TODO

		lowerToolBar.add(searchFilter);
		button = new JButton(JFritz.getMessage("clear"));
		button.setActionCommand("clearFilter");
		button.addActionListener(this);
		lowerToolBar.add(button);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		// Icons sind noch zu gro?, deshalb erst einmal auskommentiert
		// toolbarPanel.add(upperToolBar, BorderLayout.NORTH);
		toolbarPanel.add(lowerToolBar, BorderLayout.SOUTH);

		return toolbarPanel;
	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable(jfritz);
		JPopupMenu callerlistPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(JFritz.getMessage("reverse_lookup"));
		menuItem.setActionCommand("reverselookup");
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		menuItem = new JMenuItem(JFritz.getMessage("export_csv"));
		menuItem.setActionCommand("export_csv");
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("import_callerlist_csv"));
		menuItem.setActionCommand("import_callerlist_csv");
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("export_xml"));
		menuItem.setActionCommand("export_xml");
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("import_xml"));
		menuItem.setActionCommand("import_xml");
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		JMenu clipboardMenu = new JMenu(JFritz.getMessage("clipboard"));
		clipboardMenu.setMnemonic(KeyEvent.VK_Z);

		JMenuItem item = new JMenuItem(JFritz.getMessage("number"), KeyEvent.VK_N);
		item.setActionCommand("clipboard_number");
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("address"), KeyEvent.VK_A);
		item.setActionCommand("clipboard_adress");
		item.addActionListener(this);
		clipboardMenu.add(item);

		callerlistPopupMenu.add(clipboardMenu);

		MouseAdapter popupListener = new PopupListener(callerlistPopupMenu);

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
			dateFilterButton.setSelected(false);
			if (JFritz.getProperty("filter.date_from").equals(
					JFritz.getProperty("filter.date_to"))) {
				dateFilterButton
						.setText(JFritz.getProperty("filter.date_from"));
			} else {
				dateFilterButton.setText(JFritz.getProperty("filter.date_from")
						+ " - " + JFritz.getProperty("filter.date_to"));
			}
		} else {
			dateFilterButton.setSelected(true);
			dateFilterButton.setText("");
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

	public void setCallByCallProviderFilterFromSelection() {
		Vector filteredProviders = new Vector();
		try {
			String provider = "";
			int rows[] = callerTable.getSelectedRows();
			if (rows.length != 0) { // Filter only selected rows
				for (int i = 0; i < rows.length; i++) {
					Call call = (Call) jfritz.getCallerlist()
							.getFilteredCallVector().get(rows[i]);
					if (call.getPhoneNumber() != null) {
						provider = call.getPhoneNumber().getCallByCall();
						if (provider.equals("")) {
							provider = "NONE";
						}
					} else {
						provider = "NONE";
					}
					if (!filteredProviders.contains(provider)) {
						filteredProviders.add(provider);
					}
				}
			} else { // filter only calls with callbycall predial
				for (int i = 0; i < jfritz.getCallerlist()
						.getFilteredCallVector().size(); i++) {
					Call call = (Call) jfritz.getCallerlist()
							.getFilteredCallVector().get(i);
					if (call.getPhoneNumber() != null) {
						provider = call.getPhoneNumber().getCallByCall();
					}
					if (!provider.equals("")) {
						if (!filteredProviders.contains(provider)) {
							filteredProviders.add(provider);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		JFritz.setProperty("filter.callbycallProvider", filteredProviders
				.toString());
		jfritz.getCallerlist().updateFilter();
	}

	public void setDateFilterFromSelection(int datefilter) {
		Date from = null;
		Date to = null;
		Calendar cal = Calendar.getInstance();
		switch (datefilter) {
			case DATEFILTER_TODAY :
				from = cal.getTime();
				to = from;
				break;
			case DATEFILTER_YESTERDAY :
				cal.set(Calendar.DAY_OF_MONTH,
						cal.get(Calendar.DAY_OF_MONTH) - 1);
				from = cal.getTime();
				to = from;
				break;
			case DATEFILTER_THIS_MONTH :
				from = cal.getTime();
				to = from;
				cal.set(Calendar.DAY_OF_MONTH, 1);
				from = cal.getTime();
				cal.set(Calendar.DAY_OF_MONTH, cal
						.getActualMaximum(Calendar.DAY_OF_MONTH));
				to = cal.getTime();
				break;
			case DATEFILTER_LAST_MONTH :
				from = cal.getTime();
				to = from;
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
				// month
				// 0=januar,
				// ...,
				// 11=dezember
				cal.set(Calendar.DAY_OF_MONTH, 1);
				from = cal.getTime();
				cal.set(Calendar.DAY_OF_MONTH, cal
						.getActualMaximum(Calendar.DAY_OF_MONTH));
				to = cal.getTime();
				break;
			case DATEFILTER_SELECTION : {
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
				break;
			}
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

	private void setMissedFilter(int filterType) {
		Date from = null;
		Date to = null;
		JFritz.setProperty("filter.callin", "true");
		callinFilterButton.setSelected(false);
		JFritz.setProperty("filter.callout", "true");
		calloutFilterButton.setSelected(false);
		JFritz.setProperty("filter.calloutmissed", "false");
		callinfailedFilterButton.setSelected(true);
		JFritz.setProperty("filter.comment", "true");
		JFritz.setProperty("filter.comment.text", "");
		commentFilterButton.setSelected(false);
		switch (filterType) {
			case MISSED_FILTER_WITHOUT_COMMENTS :
				break;
			case MISSED_FILTER_WITHOUT_COMMENTS_LAST_WEEK : {
				Calendar cal = Calendar.getInstance();
				to = cal.getTime();
				cal.set(Calendar.DAY_OF_MONTH,
						cal.get(Calendar.DAY_OF_MONTH) - 7);
				from = cal.getTime();
				String fromstr = new SimpleDateFormat("dd.MM.yy").format(from);
				String tostr = new SimpleDateFormat("dd.MM.yy").format(to);
				JFritz.setProperty("filter.date_from", fromstr);
				JFritz.setProperty("filter.date_to", tostr);
				JFritz.setProperty("filter.date", "true");
				setDateFilterText();
				break;
			}
		}
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
					.toString(!callinFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();

		} else if (e.getActionCommand().equals("filter_callinfailed")) {
			JFritz.setProperty("filter.callinfailed", Boolean
					.toString(!callinfailedFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_comment")) {
			JFritz.setProperty("filter.comment", Boolean
					.toString(!commentFilterButton.isSelected()));
			JFritz.setProperty("filter.comment.text", "");
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals(
				"filter_callinfailed_allWithoutComment")) {
			setMissedFilter(MISSED_FILTER_WITHOUT_COMMENTS);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals(
				"filter_callinfailed_allWithoutCommentLastWeek")) {
			setMissedFilter(MISSED_FILTER_WITHOUT_COMMENTS_LAST_WEEK);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_callout")) {
			JFritz.setProperty("filter.callout", Boolean
					.toString(!calloutFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_number") {
			JFritz.setProperty("filter.number", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_fixed") {
			JFritz.setProperty("filter.fixed", Boolean
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
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_SELECTION);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_thisday") {
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_TODAY);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_yesterday") {
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_YESTERDAY);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_thismonth") {
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_THIS_MONTH);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_lastmonth") {
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_LAST_MONTH);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_sip") {
			JFritz.setProperty("filter.sip", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setSipProviderFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_callbycall") {
			JFritz.setProperty("filter.callbycall", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setCallByCallProviderFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "clearFilter") {
			clearAllFilter();
		} else if (e.getActionCommand() == "delete_entry") {
			jfritz.getCallerlist().removeEntries();
		} else if (e.getActionCommand().equals("reverselookup")) {
			doReverseLookup();
		} else if (e.getActionCommand().equals("export_csv")) {
			jfritz.getJframe().exportCallerListToCSV();
		} else if (e.getActionCommand().equals("export_xml")) {
			jfritz.getJframe().exportCallerListToXML();
		} else if (e.getActionCommand().equals("import_callerlist_csv")) {
			jfritz.getJframe().importCallerlistCSV();
		} else if (e.getActionCommand().equals("clipboard_number")) {
        	Call call = jfritz.getCallerlist().getSelectedCall();
        	if (call!=null)
        	{
        		PhoneNumber number = call.getPhoneNumber();
        		if ((number!=null)&&(call!=null))
        			JFritzClipboard.copy(number.convertToNationalNumber());
        	}
			//jfritz.getJframe().copyNumberToClipboard();
		} else if (e.getActionCommand().equals("clipboard_adress")) {
        	Call call = jfritz.getCallerlist().getSelectedCall();
        	if(call!=null)
        	{
        		Person person = call.getPerson();
        		if(person!=null)
        			JFritzClipboard.copy(person.getAddress());
        	}
			//jfritz.getJframe().copyAddressToClipboard();

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
		if (rows.length > 0) { // nur f?r markierte Eintr?ge ReverseLookup
			// durchf?hren
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) jfritz.getCallerlist()
						.getFilteredCallVector().get(rows[i]);
				Person newPerson = ReverseLookup.lookup(call.getPhoneNumber());
				if (newPerson != null) {
					jfritz.getPhonebook().addEntry(newPerson);
					jfritz.getPhonebook().fireTableDataChanged();
					jfritz.getCallerlist().fireTableDataChanged();
				}
			}
		} else { // F?r alle Eintr?ge ReverseLookup durchf?hren
			jfritz.getJframe().reverseLookup();
		}
	}

	class PopupListener extends MouseAdapter {
		JPopupMenu popupMenu;

		PopupListener(JPopupMenu popupMenu) {
			super();
			this.popupMenu = popupMenu;
		}

		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() > 1
					&& e.getComponent().getClass() != JToggleButton.class) {
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
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public JToggleButton getCallByCallButton() {
		return callByCallFilterButton;
	}

	private void clearAllFilter() {
		setSearchFilter("");
		JFritz.setProperty("filter.search", "");
		JFritz.setProperty("filter.callin", "false");
		callinFilterButton.setSelected(true);
		JFritz.setProperty("filter.callout", "false");
		calloutFilterButton.setSelected(true);
		JFritz.setProperty("filter.callinfailed", "false");
		callinfailedFilterButton.setSelected(true);
		JFritz.setProperty("filter.number", "false");
		numberFilterButton.setSelected(true);
		JFritz.setProperty("filter.fixed", "false");
		fixedFilterButton.setSelected(true);
		JFritz.setProperty("filter.handy", "false");
		handyFilterButton.setSelected(true);
		JFritz.setProperty("filter.date", "false");
		setDateFilterText();
		dateFilterButton.setSelected(true);
		JFritz.setProperty("filter.sip", "false");
		sipFilterButton.setSelected(true);
		JFritz.setProperty("filter.callbycall", "false");
		callByCallFilterButton.setSelected(true);
		JFritz.setProperty("filter.comment", "false");
		commentFilterButton.setSelected(true);
		jfritz.getCallerlist().updateFilter();
		jfritz.getCallerlist().fireTableStructureChanged();
	}

	public void keyTyped(KeyEvent arg0) {
		// unn?tig

	}

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			String filter = "";
			JTextField search = (JTextField) arg0.getSource();
			if (!filter.equals(search.getText())) {
				filter = search.getText();
				JFritz.setProperty("filter.search", filter);
				jfritz.getCallerlist().updateFilter();
				jfritz.getCallerlist().fireTableStructureChanged();
			}
			if (search.getText().equals("")){
				filter = " ";
				JFritz.setProperty("filter.search", filter);
				jfritz.getCallerlist().updateFilter();
				jfritz.getCallerlist().fireTableStructureChanged();
			}
		}

	}

	public void keyReleased(KeyEvent arg0) {
//		 unn?tig

	}
}
