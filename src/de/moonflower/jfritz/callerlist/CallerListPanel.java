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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

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

import com.toedter.calendar.JDateChooser;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.filter.CallByCallFilter;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFailedFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFilter;
import de.moonflower.jfritz.callerlist.filter.CallOutFilter;
import de.moonflower.jfritz.callerlist.filter.CommentFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.callerlist.filter.FixedFilter;
import de.moonflower.jfritz.callerlist.filter.HandyFilter;
import de.moonflower.jfritz.callerlist.filter.NoCommentFilter;
import de.moonflower.jfritz.callerlist.filter.NoNumberFilter;
import de.moonflower.jfritz.callerlist.filter.SearchFilter;
import de.moonflower.jfritz.callerlist.filter.SipFilter;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzClipboard;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

/**
 * @author Arno Willig
 *
 */

//TODO write and read the Properties one time at creation and disposion
public class CallerListPanel extends JPanel implements ActionListener,
KeyListener, PropertyChangeListener {
	class PopupListener extends MouseAdapter {
		JPopupMenu popupMenu;

		PopupListener(JPopupMenu popupMenu) {
			super();
			this.popupMenu = popupMenu;
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() > 1
					&& e.getComponent().getClass() != JToggleButton.class) {
				JFritz.getJframe().activatePhoneBook();
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
	}

	private static final long serialVersionUID = 1;

	private CallerTable callerTable;

	private CallerList callerList;

	private JToggleButton dateFilterButton, callByCallFilterButton,
	callInFilterButton, calloutFilterButton, callInFailedFilterButton,
	numberFilterButton, fixedFilterButton, handyFilterButton,
	sipFilterButton, commentFilterButton, searchFilterButton;

	private JButton deleteEntriesButton;

	private JTextField searchFilterTextField;

	private CallByCallFilter callByCallFilter;

	private CallInFilter callInFilter;

	private CallInFailedFilter callInFailedFilter;

	private CallOutFilter callOutFilter;

	private CallFilter commentFilter;

	private NoNumberFilter noNumberFilter;

	private FixedFilter fixedFilter;

	private HandyFilter handyFilter;

	private SipFilter sipFilter;

	private SearchFilter searchFilter;

	private JDateChooser startDateChooser;

	private JDateChooser endDateChooser;

	private DateFilter dateFilter;

	// private FixedFilter fixedFilter;

	private JLabel searchLabel;

	private JButton applyFilterButton;

	public CallerListPanel(CallerList callerList) {
		super();
		this.callerList = callerList;
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		handleAction(e.getActionCommand());
		// callerList.updateFilter();
		callerList.update();
	}

	/*
	 * disable all filters and hide the search and date stuff
	 */
	private void clearAllFilter() {
		callInFilterButton.setSelected(true);
		calloutFilterButton.setSelected(true);
		callInFailedFilterButton.setSelected(true);
		numberFilterButton.setSelected(true);
		fixedFilterButton.setSelected(true);
		handyFilterButton.setSelected(true);
		dateFilterButton.setSelected(true);
		startDateChooser.setVisible(false);
		endDateChooser.setVisible(false);
		searchFilterTextField.setVisible(false);
		searchLabel.setVisible(false);
		searchFilterButton.setSelected(true);
		sipFilterButton.setSelected(true);
		callByCallFilterButton.setSelected(true);
		commentFilterButton.setSelected(true);
		callerList.removeAllFilter();
		callerList.update();
	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable();
		JPopupMenu callerlistPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(Main.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		menuItem = new JMenuItem(Main.getMessage("export_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("export_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("export_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("export_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("import_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("import_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		JMenu clipboardMenu = new JMenu(Main.getMessage("clipboard")); //$NON-NLS-1$
		clipboardMenu.setMnemonic(KeyEvent.VK_Z);

		JMenuItem item = new JMenuItem(Main.getMessage("number"), KeyEvent.VK_N); //$NON-NLS-1$
		item.setActionCommand("clipboard_number"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(Main.getMessage("address"), KeyEvent.VK_A); //$NON-NLS-1$
		item.setActionCommand("clipboard_adress"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		callerlistPopupMenu.add(clipboardMenu);

		MouseAdapter popupListener = new PopupListener(callerlistPopupMenu);

		callerTable.addMouseListener(popupListener);

		return new JScrollPane(callerTable);
	}

	public JPanel createToolBar() {
		JToolBar upperToolBar = new JToolBar();
		upperToolBar.setFloatable(true);
		JToolBar lowerToolBar = new JToolBar();
		lowerToolBar.setFloatable(true);

		JButton resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("export_csv"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImage("csv_export.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText(Main.getMessage("export_csv")); //$NON-NLS-1$
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("import_csv"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImage("csv_import.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("CSV-Datei importieren"); //$NON-NLS-1$
		resetFiltersButton.setEnabled(false);
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("export_xml"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImage("xml_export.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("XML-Datei exportieren"); //$NON-NLS-1$
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("import_xml"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImage("xml_import.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("XML-Datei importieren"); //$NON-NLS-1$
		resetFiltersButton.setEnabled(false);
		upperToolBar.add(resetFiltersButton);

		upperToolBar.addSeparator();

		callInFilterButton = new JToggleButton(getImage("callin_grey.png"), //$NON-NLS-1$
				true);
		callInFilterButton.setSelectedIcon(getImage("callin.png")); //$NON-NLS-1$
		callInFilterButton.setActionCommand("filter_callin"); //$NON-NLS-1$
		callInFilterButton.addActionListener(this);
		callInFilterButton.setToolTipText(Main.getMessage("filter_callin")); //$NON-NLS-1$
		callInFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.callin", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		callInFailedFilterButton = new JToggleButton(
				getImage("callinfailed_grey.png"), true); //$NON-NLS-1$
		callInFailedFilterButton.setSelectedIcon(getImage("callinfailed.png")); //$NON-NLS-1$
		callInFailedFilterButton.setActionCommand("filter_callinfailed"); //$NON-NLS-1$
		callInFailedFilterButton.addActionListener(this);
		callInFailedFilterButton.setToolTipText(Main
				.getMessage("filter_callinfailed")); //$NON-NLS-1$
		callInFailedFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.callinfailed", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		JPopupMenu missedPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(Main
				.getMessage("missed_calls_without_comments_last_week")); //$NON-NLS-1$
		menuItem
		.setActionCommand("filter_callinfailed_allWithoutCommentLastWeek"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main
				.getMessage("missed_calls_without_comments")); //$NON-NLS-1$
		menuItem.setActionCommand("filter_callinfailed_allWithoutComment"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		MouseAdapter popupListener = new PopupListener(missedPopupMenu);
		callInFailedFilterButton.addMouseListener(popupListener);

		calloutFilterButton = new JToggleButton(getImage("callout_grey.png"), //$NON-NLS-1$
				true);
		calloutFilterButton.setSelectedIcon(getImage("callout.png")); //$NON-NLS-1$
		calloutFilterButton.setActionCommand("filter_callout"); //$NON-NLS-1$
		calloutFilterButton.addActionListener(this);
		calloutFilterButton.setToolTipText(Main.getMessage("filter_callout")); //$NON-NLS-1$
		calloutFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.callout", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		numberFilterButton = new JToggleButton(
				getImage("phone_nonumber_grey.png"), true); //$NON-NLS-1$
		numberFilterButton.setSelectedIcon(getImage("phone_nonumber.png")); //$NON-NLS-1$
		numberFilterButton.setActionCommand("filter_number"); //$NON-NLS-1$
		numberFilterButton.addActionListener(this);
		numberFilterButton.setToolTipText(Main.getMessage("filter_number")); //$NON-NLS-1$
		numberFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.number", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		fixedFilterButton = new JToggleButton(getImage("phone_grey.png"), true); //$NON-NLS-1$
		fixedFilterButton.setSelectedIcon(getImage("phone.png")); //$NON-NLS-1$
		fixedFilterButton.setActionCommand("filter_fixed"); //$NON-NLS-1$
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(Main.getMessage("filter_fixed")); //$NON-NLS-1$
		fixedFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.fixed", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		handyFilterButton = new JToggleButton(getImage("handy_grey.png"), true); //$NON-NLS-1$
		handyFilterButton.setSelectedIcon(getImage("handy.png")); //$NON-NLS-1$
		handyFilterButton.setActionCommand("filter_handy"); //$NON-NLS-1$
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(Main.getMessage("filter_handy")); //$NON-NLS-1$
		handyFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.handy", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		dateFilterButton = new JToggleButton(getImage("calendar_grey.png"), //$NON-NLS-1$
				true);
		dateFilterButton.setSelectedIcon(getImage("calendar.png")); //$NON-NLS-1$
		dateFilterButton.setActionCommand("filter_date"); //$NON-NLS-1$
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(Main.getMessage("filter_date")); //$NON-NLS-1$
		dateFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.date", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		// callerList.getDateFilter().updateDateFilter()

		setDateFilterText();
		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem(Main.getMessage("date_filter_today")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_thisday"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage("date_filter_yesterday")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_yesterday"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage("date_filter_this_month")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_thismonth"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage("date_filter_last_month")); //$NON-NLS-1$
		menuItem.setActionCommand("setdatefilter_lastmonth"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		popupListener = new PopupListener(datePopupMenu);

		dateFilterButton.addMouseListener(popupListener);

		startDateChooser = new JDateChooser();
		startDateChooser.setDate(Calendar.getInstance().getTime());
		startDateChooser.setVisible(false);
		startDateChooser.addPropertyChangeListener("date", this);
		endDateChooser = new JDateChooser();
		endDateChooser.setDate(Calendar.getInstance().getTime());
		endDateChooser.setVisible(false);
		endDateChooser.addPropertyChangeListener("date", this);

		sipFilterButton = new JToggleButton(getImage("world_grey.png"), true); //$NON-NLS-1$
		sipFilterButton.setSelectedIcon(getImage("world.png")); //$NON-NLS-1$
		sipFilterButton.setActionCommand("filter_sip"); //$NON-NLS-1$
		sipFilterButton.addActionListener(this);
		sipFilterButton.setToolTipText(Main.getMessage("filter_sip")); //$NON-NLS-1$
		sipFilterButton.setSelected(!JFritzUtils.parseBoolean(Main.getProperty(
				"filter.sip", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		callByCallFilterButton = new JToggleButton(
				getImage("callbycall_grey.png"), true); //$NON-NLS-1$
		callByCallFilterButton.setSelectedIcon(getImage("callbycall.png")); //$NON-NLS-1$
		callByCallFilterButton.setActionCommand("filter_callbycall"); //$NON-NLS-1$
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(Main
				.getMessage("filter_callbycall")); //$NON-NLS-1$
		callByCallFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.callbycall", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		commentFilterButton = new JToggleButton(getImage("commentFilter.png"), //$NON-NLS-1$
				true);
		commentFilterButton.setSelectedIcon(getImage("commentFilter.png")); //$NON-NLS-1$
		commentFilterButton.setActionCommand("filter_comment"); //$NON-NLS-1$
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(Main.getMessage("filter_comment")); //$NON-NLS-1$
		commentFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.comment", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		searchFilterButton = new JToggleButton(getImage("searchfilter.png"), //$NON-NLS-1$
				true);
		searchFilterButton.setSelectedIcon(getImage("searchfilter.png")); //$NON-NLS-1$
		searchFilterButton.setActionCommand("filter_search"); //$NON-NLS-1$
		searchFilterButton.addActionListener(this);
		searchFilterButton.setToolTipText(Main.getMessage("filter_search")); //$NON-NLS-1$
		searchFilterButton.setSelected(!JFritzUtils.parseBoolean(Main
				.getProperty("filter.search", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(Main
				.getMessage("delete_entries").replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		deleteEntriesButton.setActionCommand("delete_entry"); //$NON-NLS-1$
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImage("delete.png")); //$NON-NLS-1$
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);

		applyFilterButton = new JButton();
		// FIXME make a message for this Tooltip
		// applyFilterButton.setToolTipText(Main.getMessage("apply_filters").replaceAll("%N",
		// "")); //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		applyFilterButton.setActionCommand("apply_filter"); //$NON-NLS-1$
		applyFilterButton.addActionListener(this);
		applyFilterButton.setIcon(getImage("apply_filter.png")); //$NON-NLS-1$
		applyFilterButton.setVisible(false);

		searchLabel = new JLabel(Main.getMessage("search") + ": ");//$NON-NLS-1$,  //$NON-NLS-2$
		searchLabel.setVisible(false);
		searchFilterTextField = new JTextField(Main.getProperty(
				"filter.search", ""), //$NON-NLS-1$,  //$NON-NLS-2$
				10);
		searchFilterTextField.addKeyListener(this);
		searchFilterTextField.setVisible(false);

		resetFiltersButton = new JButton(Main.getMessage("clear")); //$NON-NLS-1$
		resetFiltersButton.setActionCommand("clearFilter"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		// Icons sind noch zu groß, deshalb erst einmal auskommentiert
		// toolbarPanel.add(upperToolBar, BorderLayout.NORTH);

		/** **********add all Buttons and stuff to the lowerToolbar************** */
		lowerToolBar.add(callInFilterButton);
		lowerToolBar.add(callInFailedFilterButton);
		lowerToolBar.add(calloutFilterButton);
		lowerToolBar.add(numberFilterButton);
		lowerToolBar.add(fixedFilterButton);
		lowerToolBar.add(handyFilterButton);
		lowerToolBar.add(sipFilterButton);
		lowerToolBar.add(callByCallFilterButton);
		lowerToolBar.add(commentFilterButton);
		lowerToolBar.addSeparator();
		lowerToolBar.add(dateFilterButton);
		lowerToolBar.add(startDateChooser);
		lowerToolBar.add(endDateChooser);
		lowerToolBar.addSeparator();
		lowerToolBar.add(searchFilterButton);
		lowerToolBar.add(searchLabel);
		lowerToolBar.add(searchFilterTextField);
		lowerToolBar.addSeparator();
		lowerToolBar.add(applyFilterButton);
		lowerToolBar.add(resetFiltersButton);
		lowerToolBar.addSeparator();
		lowerToolBar.addSeparator();
		lowerToolBar.add(deleteEntriesButton);
		toolbarPanel.add(lowerToolBar, BorderLayout.SOUTH);

		return toolbarPanel;
	}

	public void disableDeleteEntriesButton() {
		deleteEntriesButton.setToolTipText(Main
				.getMessage("delete_entries").replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		deleteEntriesButton.setEnabled(false);
	}

	private void doReverseLookup() {
		int rows[] = callerTable.getSelectedRows();
		if (rows.length > 0) { // nur für markierte Einträge ReverseLookup
			// durchführen
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) callerList.getFilteredCallVector().get(
						rows[i]);
				Person newPerson = ReverseLookup.lookup(call.getPhoneNumber());
				if (newPerson != null) {
					JFritz.getPhonebook().addEntry(newPerson);
					JFritz.getPhonebook().fireTableDataChanged();
					callerList.fireTableDataChanged();
				}
			}
		} else { // Für alle Einträge ReverseLookup durchführen
			JFritz.getJframe().reverseLookup();
		}
	}

	public JToggleButton getCallByCallButton() {
		return callByCallFilterButton;
	}

	public CallerList getCallerList() {
		return callerList;
	}

	public CallerTable getCallerTable() {
		return callerTable;
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	// TODO make int constants instaed of strings and use switch
	private void handleAction(String command) {
		if (command.equals("filter_callin")) { //$NON-NLS-1$
			if (!callInFilterButton.isSelected()) {
				callInFilter = new CallInFilter();
				callerList.addFilter(callInFilter);
			} else
				callerList.removeFilter(callInFilter);
			return;
		}
		if (command.equals("filter_callinfailed")) { //$NON-NLS-1$
			if (!callInFailedFilterButton.isSelected()) {
				callInFailedFilter = new CallInFailedFilter();
				callerList.addFilter(callInFailedFilter);
			} else
				callerList.removeFilter(callInFailedFilter);

			return;
		}
		if (command.equals("filter_callout")) { //$NON-NLS-1$
			if (!calloutFilterButton.isSelected()) {
				callOutFilter = new CallOutFilter();
				callerList.addFilter(callOutFilter);
			} else
				callerList.removeFilter(callOutFilter);

			return;
		}
		if (command.equals("filter_comment")) { //$NON-NLS-1$
			if (!commentFilterButton.isSelected()) {
				commentFilter = new CommentFilter();
				callerList.addFilter(commentFilter);
			} else
				callerList.removeFilter(commentFilter);
			return;
		}
		if (command.equals("filter_callinfailed_allWithoutComment")) { //$NON-NLS-1$
			if (!callInFailedFilterButton.isSelected())
				callInFailedFilterButton.doClick();
			if (callInFilterButton.isSelected())
				callInFilterButton.doClick();
			if (calloutFilterButton.isSelected())
				calloutFilterButton.doClick();
			if (!sipFilterButton.isSelected())
				sipFilterButton.doClick();
			if (!handyFilterButton.isSelected())
				handyFilterButton.doClick();
			if (!dateFilterButton.isSelected())
				dateFilterButton.doClick();
			if (!searchFilterButton.isSelected())
				searchFilterButton.doClick();
			if (!callByCallFilterButton.isSelected())
				callByCallFilterButton.doClick();
			if (commentFilterButton.isSelected())
				commentFilterButton.doClick();
			if (!fixedFilterButton.isSelected())
				fixedFilterButton.doClick();
			if (!numberFilterButton.isSelected())
				numberFilterButton.doClick();
			callerList.removeFilter(commentFilter);
			commentFilter = new NoCommentFilter();
			callerList.addFilter(commentFilter);
			return;

		}
		if (command.equals("filter_callinfailed_allWithoutCommentLastWeek")) { //$NON-NLS-1$
			if (!callInFailedFilterButton.isSelected())
				callInFailedFilterButton.doClick();
			if (callInFilterButton.isSelected())
				callInFilterButton.doClick();
			if (calloutFilterButton.isSelected())
				calloutFilterButton.doClick();
			if (!sipFilterButton.isSelected())
				sipFilterButton.doClick();
			if (!handyFilterButton.isSelected())
				handyFilterButton.doClick();
			if (!dateFilterButton.isSelected())
				dateFilterButton.doClick();
			if (!searchFilterButton.isSelected())
				searchFilterButton.doClick();
			if (!callByCallFilterButton.isSelected())
				callByCallFilterButton.doClick();
			if (commentFilterButton.isSelected())
				commentFilterButton.doClick();
			if (!fixedFilterButton.isSelected())
				fixedFilterButton.doClick();
			if (!numberFilterButton.isSelected())
				numberFilterButton.doClick();
			callerList.removeFilter(commentFilter);
			commentFilter = new NoCommentFilter();
			callerList.addFilter(commentFilter);

			// dateFilter stuff for last week
			callerList.removeFilter(dateFilter);
			dateFilterButton.setSelected(false);
			Calendar cal = Calendar.getInstance();
			Date today = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
			Date yesterday = cal.getTime();
			dateFilter = new DateFilter(yesterday, today);
			callerList.addFilter(dateFilter);
			startDateChooser.setDate(yesterday);
			endDateChooser.setDate(today);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);

			return;
		}
		if (command.equals("filter_number")) { //$NON-NLS-1$
			if (!numberFilterButton.isSelected()) {
				noNumberFilter = new NoNumberFilter();
				callerList.addFilter(noNumberFilter);
			} else
				callerList.removeFilter(noNumberFilter);
			return;
		}
		if (command.equals("filter_fixed")) { //$NON-NLS-1$
			if (!fixedFilterButton.isSelected()) {
				fixedFilter = new FixedFilter();
				callerList.addFilter(fixedFilter);
			} else
				callerList.removeFilter(fixedFilter);
			return;
		}
		if (command.equals("filter_handy")) { //$NON-NLS-1$
			if (!handyFilterButton.isSelected()) {
				handyFilter = new HandyFilter();
				callerList.addFilter(handyFilter);
			} else
				callerList.removeFilter(handyFilter);
			return;
		}

		if (command.equals("filter_search")) {
			if (searchFilterButton.isSelected()) {
				searchFilterTextField.setVisible(false);
				searchLabel.setVisible(false);
			} else {
				searchFilterTextField.setVisible(true);
				searchLabel.setVisible(true);
			}
			return;
		}

		if (command.equals("filter_date")) { //$NON-NLS-1$
			if (!dateFilterButton.isSelected()) {
				// dateFilter = new DateFilter(startDateChooser.getDate(),
				// endDateChooser.getDate());
				// callerList.addFilter(dateFilter);
				startDateChooser.setVisible(true);
				endDateChooser.setVisible(true);

			} else {
				startDateChooser.setVisible(false);
				endDateChooser.setVisible(false);
				callerList.removeFilter(dateFilter);
			}
			return;
		}

		if (command.equals("setdatefilter_thisday")) { //$NON-NLS-1$
			callerList.removeFilter(dateFilter);
			dateFilterButton.setSelected(false);
			Date today = Calendar.getInstance().getTime();
			dateFilter = new DateFilter(today, today);
			callerList.addFilter(dateFilter);
			startDateChooser.setDate(today);
			endDateChooser.setDate(today);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		if (command.equals("setdatefilter_yesterday")) { //$NON-NLS-1$
			callerList.removeFilter(dateFilter);
			dateFilterButton.setSelected(false);
			Calendar cal = Calendar.getInstance();
			Date today = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
			Date yesterday = cal.getTime();
			dateFilter = new DateFilter(yesterday, today);
			callerList.addFilter(dateFilter);
			startDateChooser.setDate(yesterday);
			endDateChooser.setDate(today);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		if (command.equals("setdatefilter_thismonth")) { //$NON-NLS-1$
			callerList.removeFilter(dateFilter);
			dateFilterButton.setSelected(false);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			Date start = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal
					.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date end = cal.getTime();
			dateFilter = new DateFilter(start, end);
			callerList.addFilter(dateFilter);
			startDateChooser.setDate(start);
			endDateChooser.setDate(end);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		if (command.equals("setdatefilter_lastmonth")) { //$NON-NLS-1$
			callerList.removeFilter(dateFilter);
			dateFilterButton.setSelected(false);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
			cal.set(Calendar.DAY_OF_MONTH, 1);
			Date start = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal
					.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date end = cal.getTime();
			dateFilter = new DateFilter(start, end);
			callerList.addFilter(dateFilter);
			startDateChooser.setDate(start);
			endDateChooser.setDate(end);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		if (command.equals("filter_sip")) { //$NON-NLS-1$
			if (!sipFilterButton.isSelected()) {
				sipFilter = new SipFilter(callerList
						.getSelectedOrSipProviders());
				callerList.addFilter(sipFilter);
			} else
				callerList.removeFilter(sipFilter);
			return;
		}
		if (command.equals("filter_callbycall")) { //$NON-NLS-1$
			if (!callByCallFilterButton.isSelected()) {
				callByCallFilter = new CallByCallFilter(callerList
						.getSelectedCbCProviders());
				callerList.addFilter(callByCallFilter);
			} else {
				callerList.removeFilter(callByCallFilter);
			}
			return;
		}
		if (command.equals("clearFilter")) { //$NON-NLS-1$
			clearAllFilter();
			return;
		}
		if (command.equals("delete_entry")) { //$NON-NLS-1$
			callerList.removeEntries();
			return;
		}
		if (command.equals("reverselookup")) { //$NON-NLS-1$
			doReverseLookup();
			return;
		}
		if (command.equals("export_csv")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToCSV();
			return;
		}
		if (command.equals("export_xml")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToXML();
			return;
		}
		if (command.equals("import_callerlist_csv")) { //$NON-NLS-1$
			JFritz.getJframe().importCallerlistCSV();
			return;
		}
		if (command.equals("clipboard_number")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				PhoneNumber number = call.getPhoneNumber();
				if ((number != null) && (call != null))
					JFritzClipboard.copy(number.convertToNationalNumber());
			}
			// JFritz.getJframe().copyNumberToClipboard();
			return;
		}
		if (command.equals("clipboard_adress")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = call.getPerson();
				if (person != null)
					JFritzClipboard.copy(person.getAddress());
			}
			// JFritz.getJframe().copyAddressToClipboard();

		}
		if (command.equals("apply_filter")) {
			// TODO checken, ob sich der search filter geändert hat
			if (!dateFilterButton.isSelected()) {
				callerList.removeFilter(dateFilter);
				dateFilter = new DateFilter(startDateChooser.getDate(),
						endDateChooser.getDate());
				callerList.addFilter(dateFilter);
			}
			if (!searchFilterButton.isSelected()) {
				callerList.removeFilter(searchFilter);
				String str = searchFilterTextField.getText();
				Debug.msg(str);
				if (str.equals("")) {
					// add no filter
				} else {
					searchFilter = new SearchFilter(str);
					callerList.addFilter(searchFilter);
				}
			}
		}

	}

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			callerList.removeFilter(searchFilter);// remove old filter first
			String str = searchFilterTextField.getText();
			Debug.msg(str);
			if (str.equals("")) {
				// add no filter
			} else {
				searchFilter = new SearchFilter(str);
				callerList.addFilter(searchFilter);
			}
			callerList.update();
			return;

		}

	}

	public void keyReleased(KeyEvent arg0) {
		// unnötig

	}

	public void keyTyped(KeyEvent arg0) {
		// unnötig

	}

	public void setCallerList(CallerList callerList) {
		this.callerList = callerList;
	}

	public void setDateFilterText() {
		/*
		 * if (JFritzUtils.parseBoolean(JFritz.getProperty("filter.date"))) {
		 * //$NON-NLS-1$ dateFilterButton.setSelected(false);
		 *
		 * if (JFritz.getProperty("filter.date_from").equals( //$NON-NLS-1$
		 * JFritz.getProperty("filter.date_to"))) { //$NON-NLS-1$
		 * dateFilterButton .setText(JFritz.getProperty("filter.date_from"));
		 * //$NON-NLS-1$ } else {
		 * dateFilterButton.setText(JFritz.getProperty("filter.date_from")
		 * //$NON-NLS-1$ + " - " + JFritz.getProperty("filter.date_to"));
		 * //$NON-NLS-1$, //$NON-NLS-2$ } } else {
		 * dateFilterButton.setSelected(true); dateFilterButton.setText("");
		 * //$NON-NLS-1$ }
		 */
	}

	public void setDeleteEntriesButton(int rows) {
		deleteEntriesButton
		.setToolTipText(Main
				.getMessage("delete_entries").replaceAll("%N", Integer.toString(rows))); //$NON-NLS-1$,  //$NON-NLS-2$
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntryButton() {
		deleteEntriesButton.setToolTipText(Main.getMessage("delete_entry")); //$NON-NLS-1$
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteListButton() {
		deleteEntriesButton.setToolTipText(Main.getMessage("delete_list")); //$NON-NLS-1$
		// clearList-Icon to big, so use std. delete.png
		// deleteEntriesButton.setIcon(getImage("clearList.png"));
		deleteEntriesButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 *      invoced from the JDateChoose components
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		handleAction("apply_filter");
		callerList.update();
	}
}
