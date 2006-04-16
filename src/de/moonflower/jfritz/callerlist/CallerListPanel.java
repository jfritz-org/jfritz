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
		button.setActionCommand("export_csv"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("csv_export.png")); //$NON-NLS-1$
		button.setToolTipText(JFritz.getMessage("export_csv")); //$NON-NLS-1$
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("import_csv"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("csv_import.png")); //$NON-NLS-1$
		button.setToolTipText("CSV-Datei importieren"); //$NON-NLS-1$
		button.setEnabled(false);
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("export_xml"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("xml_export.png")); //$NON-NLS-1$
		button.setToolTipText("XML-Datei exportieren"); //$NON-NLS-1$
		upperToolBar.add(button);

		button = new JButton();
		button.setActionCommand("import_xml"); //$NON-NLS-1$
		button.addActionListener(this);
		button.setIcon(getImage("xml_import.png")); //$NON-NLS-1$
		button.setToolTipText("XML-Datei importieren"); //$NON-NLS-1$
		button.setEnabled(false);
		upperToolBar.add(button);

		upperToolBar.addSeparator();

		callinFilterButton = new JToggleButton(getImage("callin_grey.png"), //$NON-NLS-1$
				true);
		callinFilterButton.setSelectedIcon(getImage("callin.png")); //$NON-NLS-1$
		callinFilterButton.setActionCommand("filter_callin"); //$NON-NLS-1$
		callinFilterButton.addActionListener(this);
		callinFilterButton.setToolTipText(JFritz.getMessage("filter_callin")); //$NON-NLS-1$
		callinFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callin", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(callinFilterButton);

		callinfailedFilterButton = new JToggleButton(
				getImage("callinfailed_grey.png"), true); //$NON-NLS-1$
		callinfailedFilterButton.setSelectedIcon(getImage("callinfailed.png")); //$NON-NLS-1$
		callinfailedFilterButton.setActionCommand("filter_callinfailed"); //$NON-NLS-1$
		callinfailedFilterButton.addActionListener(this);
		callinfailedFilterButton.setToolTipText(JFritz
				.getMessage("filter_callinfailed")); //$NON-NLS-1$
		callinfailedFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callinfailed", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		JPopupMenu missedPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(JFritz.getMessage("missed_calls_without_comments_last_week")); //$NON-NLS-1$
		menuItem
				.setActionCommand("filter_callinfailed_allWithoutCommentLastWeek"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("missed_calls_without_comments")); //$NON-NLS-1$
		menuItem.setActionCommand("filter_callinfailed_allWithoutComment"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		MouseAdapter popupListener = new PopupListener(missedPopupMenu);
		callinfailedFilterButton.addMouseListener(popupListener);
		lowerToolBar.add(callinfailedFilterButton);

		calloutFilterButton = new JToggleButton(getImage("callout_grey.png"), //$NON-NLS-1$
				true);
		calloutFilterButton.setSelectedIcon(getImage("callout.png")); //$NON-NLS-1$
		calloutFilterButton.setActionCommand("filter_callout"); //$NON-NLS-1$
		calloutFilterButton.addActionListener(this);
		calloutFilterButton.setToolTipText(JFritz.getMessage("filter_callout")); //$NON-NLS-1$
		calloutFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callout", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(calloutFilterButton);

		numberFilterButton = new JToggleButton(
				getImage("phone_nonumber_grey.png"), true); //$NON-NLS-1$
		numberFilterButton.setSelectedIcon(getImage("phone_nonumber.png")); //$NON-NLS-1$
		numberFilterButton.setActionCommand("filter_number"); //$NON-NLS-1$
		numberFilterButton.addActionListener(this);
		numberFilterButton.setToolTipText(JFritz.getMessage("filter_number")); //$NON-NLS-1$
		numberFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.number", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(numberFilterButton);

		fixedFilterButton = new JToggleButton(getImage("phone_grey.png"), true); //$NON-NLS-1$
		fixedFilterButton.setSelectedIcon(getImage("phone.png")); //$NON-NLS-1$
		fixedFilterButton.setActionCommand("filter_fixed"); //$NON-NLS-1$
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(JFritz.getMessage("filter_fixed")); //$NON-NLS-1$
		fixedFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.fixed", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(fixedFilterButton);

		handyFilterButton = new JToggleButton(getImage("handy_grey.png"), true); //$NON-NLS-1$
		handyFilterButton.setSelectedIcon(getImage("handy.png")); //$NON-NLS-1$
		handyFilterButton.setActionCommand("filter_handy"); //$NON-NLS-1$
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(JFritz.getMessage("filter_handy")); //$NON-NLS-1$
		handyFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.handy", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(handyFilterButton);

		dateFilterButton = new JToggleButton(getImage("calendar_grey.png"), //$NON-NLS-1$
				true);
		dateFilterButton.setSelectedIcon(getImage("calendar.png")); //$NON-NLS-1$
		dateFilterButton.setActionCommand("filter_date"); //$NON-NLS-1$
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(JFritz.getMessage("filter_date")); //$NON-NLS-1$
		dateFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.date", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		setDateFilterText();
		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem(JFritz.getMessage("date_filter_today")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_thisday"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("date_filter_yesterday")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_yesterday"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("date_filter_this_month")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_thismonth"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("date_filter_last_month")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_lastmonth"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		popupListener = new PopupListener(datePopupMenu);

		dateFilterButton.addMouseListener(popupListener);

		lowerToolBar.add(dateFilterButton);

		sipFilterButton = new JToggleButton(getImage("world_grey.png"), true); //$NON-NLS-1$
		sipFilterButton.setSelectedIcon(getImage("world.png")); //$NON-NLS-1$
		sipFilterButton.setActionCommand("filter_sip"); //$NON-NLS-1$
		sipFilterButton.addActionListener(this);
		sipFilterButton.setToolTipText(JFritz.getMessage("filter_sip")); //$NON-NLS-1$
		sipFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.sip", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(sipFilterButton);

		callByCallFilterButton = new JToggleButton(
				getImage("callbycall_grey.png"), true); //$NON-NLS-1$
		callByCallFilterButton.setSelectedIcon(getImage("callbycall.png")); //$NON-NLS-1$
		callByCallFilterButton.setActionCommand("filter_callbycall"); //$NON-NLS-1$
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(JFritz
				.getMessage("filter_callbycall")); //$NON-NLS-1$
		callByCallFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.callbycall", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(callByCallFilterButton);

		commentFilterButton = new JToggleButton(getImage("commentFilter.png"), //$NON-NLS-1$
				true);
		commentFilterButton.setSelectedIcon(getImage("commentFilter.png")); //$NON-NLS-1$
		commentFilterButton.setActionCommand("filter_comment"); //$NON-NLS-1$
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(JFritz.getMessage("filter_comment")); //$NON-NLS-1$
		commentFilterButton.setSelected(!JFritzUtils.parseBoolean(JFritz
				.getProperty("filter.comment", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		lowerToolBar.add(commentFilterButton);

		lowerToolBar.addSeparator();
		lowerToolBar.addSeparator();

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries").replaceAll("%N","")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		deleteEntriesButton.setActionCommand("delete_entry"); //$NON-NLS-1$
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImage("delete.png")); //$NON-NLS-1$
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);
		lowerToolBar.add(deleteEntriesButton);

		lowerToolBar.addSeparator();

		lowerToolBar.add(new JLabel(JFritz.getMessage("search") + ": ")); //$NON-NLS-1$,  //$NON-NLS-2$
		searchFilter = new JTextField(JFritz.getProperty("filter.search", ""), //$NON-NLS-1$,  //$NON-NLS-2$
				10);
		searchFilter.addKeyListener(this);

		lowerToolBar.add(searchFilter);
		button = new JButton(JFritz.getMessage("clear")); //$NON-NLS-1$
		button.setActionCommand("clearFilter"); //$NON-NLS-1$
		button.addActionListener(this);
		lowerToolBar.add(button);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		// Icons sind noch zu groß, deshalb erst einmal auskommentiert
		// toolbarPanel.add(upperToolBar, BorderLayout.NORTH);
		toolbarPanel.add(lowerToolBar, BorderLayout.SOUTH);

		return toolbarPanel;
	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable(jfritz);
		JPopupMenu callerlistPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(JFritz.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		menuItem = new JMenuItem(JFritz.getMessage("export_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("export_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("export_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("export_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(JFritz.getMessage("import_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("import_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		JMenu clipboardMenu = new JMenu(JFritz.getMessage("clipboard")); //$NON-NLS-1$
		clipboardMenu.setMnemonic(KeyEvent.VK_Z);

		JMenuItem item = new JMenuItem(JFritz.getMessage("number"), KeyEvent.VK_N); //$NON-NLS-1$
		item.setActionCommand("clipboard_number"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(JFritz.getMessage("address"), KeyEvent.VK_A); //$NON-NLS-1$
		item.setActionCommand("clipboard_adress"); //$NON-NLS-1$
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
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	public void setSearchFilter(String text) {
		searchFilter.setText(text);
	}

	public void setDateFilterText() {
		if (JFritzUtils.parseBoolean(JFritz.getProperty("filter.date"))) { //$NON-NLS-1$
			dateFilterButton.setSelected(false);
			if (JFritz.getProperty("filter.date_from").equals( //$NON-NLS-1$
					JFritz.getProperty("filter.date_to"))) { //$NON-NLS-1$
				dateFilterButton
						.setText(JFritz.getProperty("filter.date_from")); //$NON-NLS-1$
			} else {
				dateFilterButton.setText(JFritz.getProperty("filter.date_from") //$NON-NLS-1$
						+ " - " + JFritz.getProperty("filter.date_to")); //$NON-NLS-1$,  //$NON-NLS-2$
			}
		} else {
			dateFilterButton.setSelected(true);
			dateFilterButton.setText(""); //$NON-NLS-1$
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
				if (route.equals("")) { //$NON-NLS-1$
					route = "FIXEDLINE"; //$NON-NLS-1$
				}
				if (!filteredProviders.contains(route)) {
					filteredProviders.add(route);
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		JFritz.setProperty("filter.sipProvider", filteredProviders.toString()); //$NON-NLS-1$
		jfritz.getCallerlist().updateFilter();
	}

	public void setCallByCallProviderFilterFromSelection() {
		Vector filteredProviders = new Vector();
		try {
			String provider = ""; //$NON-NLS-1$
			int rows[] = callerTable.getSelectedRows();
			if (rows.length != 0) { // Filter only selected rows
				for (int i = 0; i < rows.length; i++) {
					Call call = (Call) jfritz.getCallerlist()
							.getFilteredCallVector().get(rows[i]);
					if (call.getPhoneNumber() != null) {
						provider = call.getPhoneNumber().getCallByCall();
						if (provider.equals("")) { //$NON-NLS-1$
							provider = "NONE"; //$NON-NLS-1$
						}
					} else {
						provider = "NONE"; //$NON-NLS-1$
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
					if (!provider.equals("")) { //$NON-NLS-1$
						if (!filteredProviders.contains(provider)) {
							filteredProviders.add(provider);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		JFritz.setProperty("filter.callbycallProvider", filteredProviders //$NON-NLS-1$
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
		String fromstr = new SimpleDateFormat("dd.MM.yy").format(from); //$NON-NLS-1$
		String tostr = new SimpleDateFormat("dd.MM.yy").format(to); //$NON-NLS-1$

		JFritz.setProperty("filter.date_from", fromstr); //$NON-NLS-1$
		JFritz.setProperty("filter.date_to", tostr); //$NON-NLS-1$
		setDateFilterText();
		jfritz.getCallerlist().updateFilter();
	}

	private void setMissedFilter(int filterType) {
		Date from = null;
		Date to = null;
		JFritz.setProperty("filter.callin", "true"); //$NON-NLS-1$,  //$NON-NLS-2$
		callinFilterButton.setSelected(false);
		JFritz.setProperty("filter.callout", "true"); //$NON-NLS-1$,  //$NON-NLS-2$
		calloutFilterButton.setSelected(false);
		JFritz.setProperty("filter.calloutmissed", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		callinfailedFilterButton.setSelected(true);
		JFritz.setProperty("filter.comment", "true"); //$NON-NLS-1$,  //$NON-NLS-2$
		JFritz.setProperty("filter.comment.text", ""); //$NON-NLS-1$,  //$NON-NLS-2$
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
				String fromstr = new SimpleDateFormat("dd.MM.yy").format(from); //$NON-NLS-1$
				String tostr = new SimpleDateFormat("dd.MM.yy").format(to); //$NON-NLS-1$
				JFritz.setProperty("filter.date_from", fromstr); //$NON-NLS-1$
				JFritz.setProperty("filter.date_to", tostr); //$NON-NLS-1$
				JFritz.setProperty("filter.date", "true"); //$NON-NLS-1$,  //$NON-NLS-2$
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
		if (e.getActionCommand().equals("filter_callin")) { //$NON-NLS-1$
			JFritz.setProperty("filter.callin", Boolean //$NON-NLS-1$
					.toString(!callinFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();

		} else if (e.getActionCommand().equals("filter_callinfailed")) { //$NON-NLS-1$
			JFritz.setProperty("filter.callinfailed", Boolean //$NON-NLS-1$
					.toString(!callinfailedFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_comment")) { //$NON-NLS-1$
			JFritz.setProperty("filter.comment", Boolean //$NON-NLS-1$
					.toString(!commentFilterButton.isSelected()));
			JFritz.setProperty("filter.comment.text", ""); //$NON-NLS-1$,  //$NON-NLS-2$
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals(
				"filter_callinfailed_allWithoutComment")) { //$NON-NLS-1$
			setMissedFilter(MISSED_FILTER_WITHOUT_COMMENTS);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals(
				"filter_callinfailed_allWithoutCommentLastWeek")) { //$NON-NLS-1$
			setMissedFilter(MISSED_FILTER_WITHOUT_COMMENTS_LAST_WEEK);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_callout")) { //$NON-NLS-1$
			JFritz.setProperty("filter.callout", Boolean //$NON-NLS-1$
					.toString(!calloutFilterButton.isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_number") { //$NON-NLS-1$
			JFritz.setProperty("filter.number", Boolean //$NON-NLS-1$
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_fixed") { //$NON-NLS-1$
			JFritz.setProperty("filter.fixed", Boolean //$NON-NLS-1$
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_handy") { //$NON-NLS-1$
			JFritz.setProperty("filter.handy", Boolean //$NON-NLS-1$
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_date") { //$NON-NLS-1$
			JFritz.setProperty("filter.date", Boolean //$NON-NLS-1$
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_SELECTION);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_thisday") { //$NON-NLS-1$
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean //$NON-NLS-1$
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_TODAY);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_yesterday") { //$NON-NLS-1$
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean //$NON-NLS-1$
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_YESTERDAY);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_thismonth") { //$NON-NLS-1$
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean //$NON-NLS-1$
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_THIS_MONTH);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "setdatefilter_lastmonth") { //$NON-NLS-1$
			dateFilterButton.setSelected(false);
			JFritz.setProperty("filter.date", Boolean //$NON-NLS-1$
					.toString(!dateFilterButton.isSelected()));
			setDateFilterFromSelection(DATEFILTER_LAST_MONTH);
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_sip") { //$NON-NLS-1$
			JFritz.setProperty("filter.sip", Boolean //$NON-NLS-1$
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setSipProviderFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_callbycall") { //$NON-NLS-1$
			JFritz.setProperty("filter.callbycall", Boolean //$NON-NLS-1$
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setCallByCallProviderFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "clearFilter") { //$NON-NLS-1$
			clearAllFilter();
		} else if (e.getActionCommand() == "delete_entry") { //$NON-NLS-1$
			jfritz.getCallerlist().removeEntries();
		} else if (e.getActionCommand().equals("reverselookup")) { //$NON-NLS-1$
			doReverseLookup();
		} else if (e.getActionCommand().equals("export_csv")) { //$NON-NLS-1$
			jfritz.getJframe().exportCallerListToCSV();
		} else if (e.getActionCommand().equals("export_xml")) { //$NON-NLS-1$
			jfritz.getJframe().exportCallerListToXML();
		} else if (e.getActionCommand().equals("import_callerlist_csv")) { //$NON-NLS-1$
			jfritz.getJframe().importCallerlistCSV();
		} else if (e.getActionCommand().equals("clipboard_number")) { //$NON-NLS-1$
        	Call call = jfritz.getCallerlist().getSelectedCall();
        	if (call!=null)
        	{
        		PhoneNumber number = call.getPhoneNumber();
        		if ((number!=null)&&(call!=null))
        			JFritzClipboard.copy(number.convertToNationalNumber());
        	}
			//jfritz.getJframe().copyNumberToClipboard();
		} else if (e.getActionCommand().equals("clipboard_adress")) { //$NON-NLS-1$
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
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_list")); //$NON-NLS-1$
		// clearList-Icon to big, so use std. delete.png
		// deleteEntriesButton.setIcon(getImage("clearList.png"));
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntriesButton(int rows) {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries").replaceAll("%N", Integer.toString(rows))); //$NON-NLS-1$,  //$NON-NLS-2$
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntryButton() {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entry")); //$NON-NLS-1$
		deleteEntriesButton.setEnabled(true);
	}

	public void disableDeleteEntriesButton() {
		deleteEntriesButton.setToolTipText(JFritz.getMessage("delete_entries").replaceAll("%N","")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		deleteEntriesButton.setEnabled(false);
	}

	private void doReverseLookup() {
		int rows[] = callerTable.getSelectedRows();
		if (rows.length > 0) { // nur für markierte Einträge ReverseLookup
			// durchführen
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
		} else { // Für alle Einträge ReverseLookup durchführen
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
		setSearchFilter(""); //$NON-NLS-1$
		JFritz.setProperty("filter.search", ""); //$NON-NLS-1$,  //$NON-NLS-2$
		JFritz.setProperty("filter.callin", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		callinFilterButton.setSelected(true);
		JFritz.setProperty("filter.callout", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		calloutFilterButton.setSelected(true);
		JFritz.setProperty("filter.callinfailed", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		callinfailedFilterButton.setSelected(true);
		JFritz.setProperty("filter.number", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		numberFilterButton.setSelected(true);
		JFritz.setProperty("filter.fixed", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		fixedFilterButton.setSelected(true);
		JFritz.setProperty("filter.handy", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		handyFilterButton.setSelected(true);
		JFritz.setProperty("filter.date", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		setDateFilterText();
		dateFilterButton.setSelected(true);
		JFritz.setProperty("filter.sip", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		sipFilterButton.setSelected(true);
		JFritz.setProperty("filter.callbycall", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		callByCallFilterButton.setSelected(true);
		JFritz.setProperty("filter.comment", "false"); //$NON-NLS-1$,  //$NON-NLS-2$
		commentFilterButton.setSelected(true);
		jfritz.getCallerlist().updateFilter();
		jfritz.getCallerlist().fireTableStructureChanged();
	}

	public void keyTyped(KeyEvent arg0) {
		// unnötig

	}

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			String filter = ""; //$NON-NLS-1$
			JTextField search = (JTextField) arg0.getSource();
			if (!filter.equals(search.getText())) {
				filter = search.getText();
				JFritz.setProperty("filter.search", filter); //$NON-NLS-1$
				jfritz.getCallerlist().updateFilter();
				jfritz.getCallerlist().fireTableStructureChanged();
			}
			if (search.getText().equals("")){ //$NON-NLS-1$
				filter = " "; //$NON-NLS-1$
				JFritz.setProperty("filter.search", filter); //$NON-NLS-1$
				jfritz.getCallerlist().updateFilter();
				jfritz.getCallerlist().fireTableStructureChanged();
			}
		}

	}

	public void keyReleased(KeyEvent arg0) {
//		 unnötig

	}
}
