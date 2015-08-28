/*
* Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import com.toedter.calendar.JDateChooser;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.StatusBarPanel;
import de.moonflower.jfritz.callerlist.filter.AnonymFilter;
import de.moonflower.jfritz.callerlist.filter.CallByCallFilter;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFailedFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFilter;
import de.moonflower.jfritz.callerlist.filter.CallOutFilter;
import de.moonflower.jfritz.callerlist.filter.CheckboxPopupMenu;
import de.moonflower.jfritz.callerlist.filter.CommentFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.callerlist.filter.FixedFilter;
import de.moonflower.jfritz.callerlist.filter.HandyFilter;
import de.moonflower.jfritz.callerlist.filter.PortFilter;
import de.moonflower.jfritz.callerlist.filter.SearchFilter;
import de.moonflower.jfritz.callerlist.filter.SipFilter;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.phonebook.PhoneBookPanel;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzClipboard;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StatusBarController;
import de.moonflower.jfritz.utils.threeStateButton.ThreeStateButton;

/**
 * @author marc
 */
public class CallerListPanel extends JPanel implements ActionListener,
		KeyListener, PropertyChangeListener, IProgressListener {

	class PopupListener extends MouseAdapter {
		JPopupMenu popupMenu;
		ActionListener listener;

		PopupListener(JPopupMenu popupMenu, ActionListener listener) {
			super();
			this.popupMenu = popupMenu;
			this.listener = listener;
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				googleLink = null;
				//only modify the popup menu if its for the call table
				if(listener != null){

					//get the selected objects that were right clicked
					JTable target = (JTable) e.getSource();
					int[] selectedRows = target.getSelectedRows();

						//if calls have different country codes, only offer a generic lookup
					if(selectedRows.length > 0){
						reverseMenu.setEnabled(true);
					}else{
						reverseMenu.setEnabled(false);
					}

					// adapt google link
					adaptGoogleLink();
				}

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseClicked(MouseEvent e) {
			// FIXME Listener in den table einbauen
			if ((e.getClickCount() > 1)
					&& (e.getComponent().getClass() == CallerTable.class)) {
				int numSelected = callerTable.getSelectedPersons().size();
				if (numSelected == 1)
				{
					phoneBookPanel.getPhoneBookTable().showAndSelectPerson(callerTable.getSelectedPersons().get(0), true);
					phoneBookPanel.showEditPerson();
				} else if (numSelected == 0 && callerTable.getSelectedRowCount() == 1) {
					Call selectedCall = callerList.getFilteredCall(callerTable.getSelectedRow());
					if (selectedCall.getPhoneNumber() != null) {
						// do not create a new entry without phone number
						Person newPerson = new Person();
						newPerson.addNumber(selectedCall.getPhoneNumber());
						Vector<Person> persons = new Vector<Person>();
						persons.add(newPerson);
						JFritz.getPhonebook().addEntries(persons);
						phoneBookPanel.getPhoneBookTable().showAndSelectPerson(newPerson, true);
						phoneBookPanel.updateUI();
						phoneBookPanel.showEditPerson();
					} else {
						phoneBookPanel.hideEditPerson();
						phoneBookPanel.getPhoneBookTable().clearPersonSelection();
					}
				}

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

	private final static Logger log = Logger.getLogger(CallerListPanel.class);

	private static final String DELETE_ENTRIES = "delete_entries";

	private static final String DELETE_ENTRY = "delete_entry";


	public static final long serialVersionUID = 1;

	private String dateSpecialSaveString;

	private CallerList callerList;

	private CallerTable callerTable;

	private CallFilter[] filter;

	private static final int CALL_BY_CALL = 0;

	private static final int CALL_IN_FAILED = 1;

	private static final int CALL_IN = 2;

	private static final int CALL_OUT = 3;

	private static final int COMMENT = 4;

	private static final int MOBILE = 5;

	private static final int FIXED = 6;

	private static final int SIP = 7;

	private static final int PORT = 8;

	private static final int ANONYMOUS = 9;

	private static final int DATE = 10;

	private static final int SEARCH = 11;

	private static final int FILTERCOUNT = 12;

	private ThreeStateButton dateFilterButton, callByCallFilterButton,
			callInFilterButton, callOutFilterButton, callInFailedFilterButton,
			anonymFilterButton, fixedFilterButton, handyFilterButton,
			commentFilterButton, searchFilterButton;

	private JToggleButton sipFilterButton, portFilterButton;

	private JButton deleteEntriesButton;

	private JDateChooser endDateChooser;

	private JTextField searchFilterTextField;

	private JLabel searchLabel;

	private JDateChooser startDateChooser;
	private PhoneBookPanel phoneBookPanel;
	private JFritzWindow jFrame;
	private StatusBarController statusBarController = new StatusBarController();

	private JMenuItem reverseMenu;
	private JMenuItem googleItem;
	private String googleLink = null;

	private StatusBarPanel callerListStatusBar;
	private JLabel callsLabel;
	private JLabel durationLabel;
	private int numSelectedCalls = 0;
	private double durationSelectedCalls = 0;
	private StatusBarPanel progressStatusBar;
	private JProgressBar progressBar;

	private CheckboxPopupMenu msnPopupMenu = null;
	private CheckboxPopupMenu portPopupMenu = null;

	private PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	private Component focusBeforeSearch = null;

	private StatusBarPanel reverseLookupStatusBar = null;
	private JProgressBar lookupProgressBar = null;
	private JLabel reverseLookupLabel = null;

	/**
	 * A callerListPanel is a view for a callerlist, it has its own
	 * resourceBundle to get the localized strings for its components. The
	 * components are a Toolbar and a table. th callerListPanel has a
	 * callerList, the model for this view
	 *
	 * @see CallerList
	 * @param callerList
	 *            the model
	 * @param parent
	 *            the parent frame to display some messages and set the status
	 *            bar
	 */

	public CallerListPanel(CallerList callerList, JFritzWindow parent) {
		super();
		jFrame = parent;
		this.callerList = callerList;
		createFilters(callerList);
		setLayout(new BorderLayout());
		registerStatusBar();
		add(createToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
	}

	public void setPhoneBookPanel(PhoneBookPanel phoneBookPanel){
		this.phoneBookPanel = phoneBookPanel;
		if(phoneBookPanel==null){
			callerTable.setPhoneBookTable(null);
		}else{
			callerTable.setPhoneBookTable(phoneBookPanel.getPhoneBookTable());
		}
	}

	/**
	 * creates all filters and stores them in the array
	 *
	 * @param callerList
	 *            the model
	 */
	private void createFilters(CallerList callerList) {
		filter = new CallFilter[FILTERCOUNT];
		filter[CALL_BY_CALL] = new CallByCallFilter(
				getSelectedCallByCallProvider(callerList));
		callerList.addFilter(filter[CALL_BY_CALL]);
		filter[CALL_IN_FAILED] = new CallInFailedFilter();
		callerList.addFilter(filter[CALL_IN_FAILED]);
		filter[CALL_IN] = new CallInFilter();
		callerList.addFilter(filter[CALL_IN]);
		filter[CALL_OUT] = new CallOutFilter();
		callerList.addFilter(filter[CALL_OUT]);
		filter[COMMENT] = new CommentFilter();
		callerList.addFilter(filter[COMMENT]);
		filter[ANONYMOUS] = new AnonymFilter();
		callerList.addFilter(filter[ANONYMOUS]);
		filter[FIXED] = new FixedFilter();
		callerList.addFilter(filter[FIXED]);
		filter[MOBILE] = new HandyFilter();
		callerList.addFilter(filter[MOBILE]);
		filter[PORT] = new PortFilter();
		callerList.addFilter(filter[PORT]);
		filter[SIP] = new SipFilter();
		callerList.addFilter(filter[SIP]);
		filter[DATE] = new DateFilter(new Date(), new Date());
		callerList.addFilter(filter[DATE]);
		filter[SEARCH] = new SearchFilter("");
		callerList.addFilter(filter[SEARCH]);
	}

	/**
	 * creates a CallByCallfilter using only the selected CallByCallProviders or
	 * all CallByCallProviders, if none is selected
	 *
	 * @param callerList
	 *            the callerlist to retrieve the CallByCallProviders
	 * @return the createdFilter
	 */

	private Vector<String> getSelectedCallByCallProvider(CallerList callerList) {
		Vector<String> provider;
		if ((callerTable != null) && (callerTable.getSelectedRowCount() != 0)) {
			provider = callerList
					.getCbCProviders(callerTable.getSelectedRows());
		} else {
			provider = callerList.getCbCProviders();
		}
		return provider;
	}


	/**
	 * disable all filters and hide the search and date stuff
	 */
	private void clearAllFilter() {
		callInFilterButton.setState(ThreeStateButton.NOTHING);
		callOutFilterButton.setState(ThreeStateButton.NOTHING);
		callInFailedFilterButton.setState(ThreeStateButton.NOTHING);
		anonymFilterButton.setState(ThreeStateButton.NOTHING);
		fixedFilterButton.setState(ThreeStateButton.NOTHING);
		handyFilterButton.setState(ThreeStateButton.NOTHING);
		dateFilterButton.setState(ThreeStateButton.NOTHING);
		searchFilterButton.setState(ThreeStateButton.NOTHING);
		searchFilterTextField.setVisible(false);
		startDateChooser.setVisible(false);
		endDateChooser.setVisible(false);
		searchLabel.setVisible(false);
		properties.setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, "$ALL$");
		properties.setStateProperty(CallFilter.FILTER_PORT_LIST, "$ALL$");
		callByCallFilterButton.setState(ThreeStateButton.NOTHING);
		commentFilterButton.setState(ThreeStateButton.NOTHING);
		dateSpecialSaveString = " ";
		syncAllFilters();
		callerList.update();
		//((JFritzWindow) parentFrame).setStatus();
	}

	/**
	 * create gui
	 *
	 * @return a scrollPane with the callerListTable
	 */
	private JScrollPane createCallerListTable() {
		callerTable = new CallerTable(this, callerList);
		if(phoneBookPanel==null){
			callerTable.setPhoneBookTable(null);
		}else{
			callerTable.setPhoneBookTable(phoneBookPanel.getPhoneBookTable());
		}
		JPopupMenu callerlistPopupMenu = new JPopupMenu();
		JMenuItem menuItem;

		reverseMenu = new JMenuItem(messages.getMessage("reverse_lookup"));
		reverseMenu.setActionCommand("lookup:");
		reverseMenu.addActionListener(this);

		callerlistPopupMenu.add(reverseMenu);

		googleItem = new JMenuItem(messages.getMessage("show_on_google_maps"));
		googleItem.setActionCommand("google");
		googleItem.addActionListener(this);
		callerlistPopupMenu.add(googleItem);

		menuItem = new JMenuItem(messages.getMessage("reverse_lookup_dummy")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup_dummy"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		menuItem = new JMenuItem(messages.getMessage("export_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("export_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(messages.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(messages.getMessage("export_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("export_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(messages.getMessage("import_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("import_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		JMenu clipboardMenu = new JMenu(messages.getMessage("clipboard")); //$NON-NLS-1$
		clipboardMenu.setMnemonic(KeyEvent.VK_Z);

		JMenuItem item = new JMenuItem(messages.getMessage("name"), KeyEvent.VK_N); //$NON-NLS-1$
		item.setActionCommand("clipboard_name"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("company")); //$NON-NLS-1$
		item.setActionCommand("clipboard_company"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("number"), KeyEvent.VK_U); //$NON-NLS-1$
		item.setActionCommand("clipboard_number"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("address"), KeyEvent.VK_A); //$NON-NLS-1$
		item.setActionCommand("clipboard_adress"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("street"));
		item.setActionCommand("clipboard_street"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("postalCode"));
		item.setActionCommand("clipboard_postalCode"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("city"));
		item.setActionCommand("clipboard_city"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("type_desc"));
		item.setActionCommand("clipboard_type"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("date_desc"));
		item.setActionCommand("clipboard_date"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("callbycall_desc"));
		item.setActionCommand("clipboard_callbycall"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(messages.getMessage("comment_desc"));
		item.setActionCommand("clipboard_comment"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		callerlistPopupMenu.add(clipboardMenu);

		MouseAdapter popupListener = new PopupListener(callerlistPopupMenu, this);
		callerTable.addMouseListener(popupListener);

		return new JScrollPane(callerTable);
	}

	/**
	 * create gui
	 *
	 * @return the toolbar
	 */
	private JPanel createToolBar() {
		JToolBar upperToolBar = new JToolBar();
		upperToolBar.setFloatable(true);
		JToolBar lowerToolbar = new JToolBar();
		lowerToolbar.setFloatable(true);

		JButton toolbarButton = new JButton();
		toolbarButton.setActionCommand("export_csv"); //$NON-NLS-1$
		toolbarButton.addActionListener(this);
		toolbarButton.setIcon(getImageIcon("csv_export.png")); //$NON-NLS-1$
		toolbarButton.setToolTipText(messages.getMessage("export_csv")); //$NON-NLS-1$
		upperToolBar.add(toolbarButton);

		toolbarButton = new JButton();
		toolbarButton.setActionCommand("import_csv"); //$NON-NLS-1$
		toolbarButton.addActionListener(this);
		toolbarButton.setIcon(getImageIcon("csv_import.png")); //$NON-NLS-1$
		toolbarButton.setToolTipText("CSV-Datei importieren"); //$NON-NLS-1$
		toolbarButton.setEnabled(false);
		upperToolBar.add(toolbarButton);

		toolbarButton = new JButton();
		toolbarButton.setActionCommand("export_xml"); //$NON-NLS-1$
		toolbarButton.addActionListener(this);
		toolbarButton.setIcon(getImageIcon("xml_export.png")); //$NON-NLS-1$
		toolbarButton.setToolTipText("XML-Datei exportieren"); //$NON-NLS-1$
		upperToolBar.add(toolbarButton);

		toolbarButton = new JButton();
		toolbarButton.setActionCommand("import_xml"); //$NON-NLS-1$
		toolbarButton.addActionListener(this);
		toolbarButton.setIcon(getImageIcon("xml_import.png")); //$NON-NLS-1$
		toolbarButton.setToolTipText("XML-Datei importieren"); //$NON-NLS-1$
		toolbarButton.setEnabled(false);
		upperToolBar.add(toolbarButton);

		upperToolBar.addSeparator();

		callInFilterButton = new ThreeStateButton(getImageIcon("callin.png"));
		callInFilterButton.setActionCommand(CallFilter.FILTER_CALLIN_NOTHING);
		callInFilterButton.addActionListener(this);
		callInFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_CALLIN_NOTHING));

		callInFailedFilterButton = new ThreeStateButton(
				getImageIcon("callinfailed.png")); //$NON-NLS-1$
		callInFailedFilterButton.setActionCommand(CallFilter.FILTER_CALLINFAILED);
		callInFailedFilterButton.addActionListener(this);
		callInFailedFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_CALLINFAILED));

		JPopupMenu missedPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(messages.getMessage("missed_calls_without_comments_last_week")); //$NON-NLS-1$
		menuItem
				.setActionCommand("filter_callinfailed_allWithoutCommentLastWeek"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage("missed_calls_without_comments")); //$NON-NLS-1$
		menuItem.setActionCommand("filter_callinfailed_allWithoutComment"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		MouseAdapter popupListener = new PopupListener(missedPopupMenu, null);
		callInFailedFilterButton.addMouseListener(popupListener);

		callOutFilterButton = new ThreeStateButton(getImageIcon("callout.png"));
		callOutFilterButton.setActionCommand(CallFilter.FILTER_CALLOUT);
		callOutFilterButton.addActionListener(this);
		callOutFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_CALLOUT));

		anonymFilterButton = new ThreeStateButton(getImageIcon("mask.gif")); //$NON-NLS-1$
		anonymFilterButton.setActionCommand(CallFilter.FILTER_ANONYM);
		anonymFilterButton.addActionListener(this);
		anonymFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_ANONYM));

		fixedFilterButton = new ThreeStateButton(getImageIcon("phone.png")); //$NON-NLS-1$
		fixedFilterButton.setActionCommand(CallFilter.FILTER_FIXED);
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_FIXED));

		handyFilterButton = new ThreeStateButton(getImageIcon("handy.png")); //$NON-NLS-1$
		handyFilterButton.setActionCommand(CallFilter.FILTER_HANDY);
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_HANDY));

		dateFilterButton = new ThreeStateButton(getImageIcon("calendar.png")); //$NON-NLS-1$

		dateFilterButton.setActionCommand(CallFilter.FILTER_DATE);
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(ThreeStateButton.NOTHING, messages.getMessage(CallFilter.FILTER_DATE));

		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem(messages.getMessage(CallFilter.THIS_DAY));
		menuItem.setActionCommand(CallFilter.THIS_DAY);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage(CallFilter.LAST_DAY));
		menuItem.setActionCommand(CallFilter.LAST_DAY);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage(CallFilter.THIS_WEEK));
		menuItem.setActionCommand(CallFilter.THIS_WEEK);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage(CallFilter.LAST_WEEK));
		menuItem.setActionCommand(CallFilter.LAST_WEEK);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);

		menuItem = new JMenuItem(messages.getMessage(CallFilter.THIS_MONTH));
		menuItem.setActionCommand(CallFilter.THIS_MONTH);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage(CallFilter.LAST_MONTH));
		menuItem.setActionCommand(CallFilter.LAST_MONTH);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		popupListener = new PopupListener(datePopupMenu, null);

		dateFilterButton.addMouseListener(popupListener);

		startDateChooser = new JDateChooser();
		startDateChooser.setVisible(false);
		startDateChooser.addPropertyChangeListener("date", this);
		endDateChooser = new JDateChooser();
		endDateChooser.setDate(Calendar.getInstance().getTime());
		endDateChooser.setVisible(false);
		endDateChooser.addPropertyChangeListener("date", this);

		sipFilterButton = new JToggleButton(getImageIcon("world.png")); //$NON-NLS-1$
		sipFilterButton.setActionCommand(CallFilter.FILTER_SIP);
		sipFilterButton.addActionListener(this);
		sipFilterButton.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (msnPopupMenu != null) {
						msnPopupMenu.setVisible(false);
						msnPopupMenu = null;
					}
					msnPopupMenu = new CheckboxPopupMenu(JFritz.getJframe(), CallFilter.FILTER_SIP_PROVIDERS);
					Vector<String> providers = callerList.getUsedProviderList();
					msnPopupMenu.setObjects(providers);
					Vector<String> filteredMSNs = new Vector<String>();
					JFritzUtils.fillVectorByString(filteredMSNs, CallFilter.FILTER_SIP_PROVIDERS, ";");
					for (int i=0; i<filteredMSNs.size(); i++)
					{
						msnPopupMenu.setSelected(filteredMSNs.get(i), true);
					}

					Point tmpPoint = sipFilterButton.getLocationOnScreen();
					tmpPoint.translate(sipFilterButton.getSize().width/2, sipFilterButton.getSize().height/2);
					msnPopupMenu.setLocation(tmpPoint.x, tmpPoint.y);
					msnPopupMenu.setVisible(true);
					if (msnPopupMenu.okPressed()) {
						if (msnPopupMenu.getSelectedItems().size() == providers.size())
						{
							((SipFilter) filter[SIP]).setAllSelected();
						}
						else
						{
							((SipFilter) filter[SIP]).setProvider(msnPopupMenu.getSelectedItems());
						}
						filter[SIP].setEnabled(true);
						filter[SIP].setInvert(false);
						update();
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

		});
		sipFilterButton.setToolTipText(messages.getMessage(CallFilter.FILTER_SIP));

		portFilterButton = new JToggleButton(getImageIcon("portfilter.png")); //$NON-NLS-1$
		portFilterButton.setActionCommand(CallFilter.FILTER_PORT);
		portFilterButton.addActionListener(this);
		portFilterButton.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (portPopupMenu != null) {
						portPopupMenu.setVisible(false);
						portPopupMenu = null;
					}
					portPopupMenu = new CheckboxPopupMenu(JFritz.getJframe(), CallFilter.FILTER_PORT_LIST);
					Vector<String> ports = callerList.getUsedPortsList();
					portPopupMenu.setObjects(ports);
					Vector<String> filteredPorts = new Vector<String>();
					JFritzUtils.fillVectorByString(filteredPorts, CallFilter.FILTER_PORT_LIST, ";");
					for (int i=0; i<filteredPorts.size(); i++)
					{
						portPopupMenu.setSelected(filteredPorts.get(i), true);
					}

					Point tmpPoint = portFilterButton.getLocationOnScreen();
					tmpPoint.translate(portFilterButton.getSize().width/2, portFilterButton.getSize().height/2);
					portPopupMenu.setLocation(tmpPoint.x, tmpPoint.y);
					portPopupMenu.setVisible(true);
					if (portPopupMenu.okPressed()) {
						if (portPopupMenu.getSelectedItems().size() == ports.size())
						{
							((PortFilter) filter[PORT]).setAllSelected();
						}
						else
						{
							((PortFilter) filter[PORT]).setPorts(portPopupMenu.getSelectedItems());
						}
						filter[PORT].setEnabled(true);
						filter[PORT].setInvert(false);
						update();
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

		});
		portFilterButton.setToolTipText(messages.getMessage(CallFilter.FILTER_PORT));

		callByCallFilterButton = new ThreeStateButton(
				getImageIcon("callbycall.png")); //$NON-NLS-1$
		callByCallFilterButton.setActionCommand(CallFilter.FILTER_CALLBYCALL);
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(ThreeStateButton.NOTHING,
				messages.getMessage(CallFilter.FILTER_CALLBYCALL));

		commentFilterButton = new ThreeStateButton(
				getImageIcon("commentFilter.png"));
		commentFilterButton.setActionCommand(CallFilter.FILTER_COMMENT);
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(ThreeStateButton.NOTHING,
				messages.getMessage(CallFilter.FILTER_COMMENT));

		searchFilterButton = new ThreeStateButton(
				getImageIcon("searchfilter.png"));
		searchFilterButton.setActionCommand(CallFilter.FILTER_SEARCH);
		searchFilterButton.addActionListener(this);
		searchFilterButton.setToolTipText(ThreeStateButton.NOTHING,
				messages.getMessage(CallFilter.FILTER_SEARCH));
		searchFilterTextField = new JTextField(10);

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(messages.getMessage(DELETE_ENTRIES)
				.replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,
		deleteEntriesButton.setActionCommand(DELETE_ENTRY);
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImageIcon("delete.png")); //$NON-NLS-1$
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);

		searchLabel = new JLabel(messages.getMessage("search") + ": ");//$NON-NLS-1$,  //$NON-NLS-2$
		searchLabel.setVisible(false);
		searchFilterTextField.addKeyListener(this);
		searchFilterTextField.setVisible(false);

		toolbarButton = new JButton(messages.getMessage("clear")); //$NON-NLS-1$
		toolbarButton.setActionCommand("clearFilter"); //$NON-NLS-1$
		toolbarButton.addActionListener(this);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		// Icons sind noch zu groß, deshalb erst einmal auskommentiert
		// toolbarPanel.add(upperToolBar, BorderLayout.NORTH);

		/** **********add all Buttons and stuff to the lowerToolbar************** */
		lowerToolbar.add(callInFilterButton);
		lowerToolbar.add(callInFailedFilterButton);
		lowerToolbar.add(callOutFilterButton);
		lowerToolbar.add(anonymFilterButton);
		lowerToolbar.add(fixedFilterButton);
		lowerToolbar.add(handyFilterButton);
		lowerToolbar.add(portFilterButton);
		lowerToolbar.add(sipFilterButton);
		lowerToolbar.add(callByCallFilterButton);
		lowerToolbar.add(commentFilterButton);
		lowerToolbar.addSeparator();
		lowerToolbar.add(dateFilterButton);
		lowerToolbar.add(startDateChooser);
		lowerToolbar.add(endDateChooser);
		lowerToolbar.addSeparator();
		lowerToolbar.add(searchFilterButton);
		lowerToolbar.add(searchLabel);
		lowerToolbar.add(searchFilterTextField);
		lowerToolbar.addSeparator();
		lowerToolbar.add(toolbarButton);
		lowerToolbar.addSeparator();
		lowerToolbar.addSeparator();
		lowerToolbar.add(deleteEntriesButton);
		toolbarPanel.add(lowerToolbar, BorderLayout.SOUTH);
		loadButtonStatus();
		return toolbarPanel;
	}


	public CallerList getCallerList() {
		return callerList;
	}

	public CallerTable getCallerTable() {
		return callerTable;
	}

	/**
	 * get a imageIcon from a given filename
	 *
	 * @param filename
	 *            the name of the file
	 * @return the imageIcon
	 */
	public ImageIcon getImageIcon(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	/**
	 *
	 * get a image from a given filename
	 *
	 * @param filename
	 *            the name of the file
	 * @return the image
	 */

	public Image getImage(String filename) {
		return Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename)); //$NON-NLS-1$
	}
	/**
	 * some buttons are clicked.
	 *
	 * @see ActionListener
	 */
	public void actionPerformed(ActionEvent e) {
		handleAction(e.getActionCommand());
		update();
	}

	private void update() {
		callerList.update();
		updateStatusBar(false);
		renderFilterButtons();
//		statusBarController.fireStatusChanged(callerList.getTotalDuration());
		saveButtonStatus();
	}

	private void renderFilterButtons() {
		sipFilterButton.setSelected(!((SipFilter)filter[SIP]).isAllSelected());
		portFilterButton.setSelected(!((PortFilter)filter[PORT]).isAllSelected());
	}

	/**
	 * the control this method is called, if the user pushed buttons or changes
	 * dates or the search filter for command use the constants
	 * <code> <br>FILTER_CALLIN, <br>FILTER_CALLOUT,<br>FILTER_COMMENT<br>...
	 * </code>
	 *
	 * @param command
	 */
	private void handleAction(String command) {
		if (callerTable != null) {
			CellEditor ce = callerTable.getCellEditor();
			if (ce != null) {
				ce.cancelCellEditing();
			}
		}
		if (command.equals(CallFilter.FILTER_CALLIN_NOTHING)) {
			syncFilterWithButton(filter[CALL_IN], callInFilterButton);
		} else if (command.equals(CallFilter.FILTER_CALLINFAILED)) {
			syncFilterWithButton(filter[CALL_IN_FAILED], callInFailedFilterButton);
		} else if (command.equals(CallFilter.FILTER_CALLOUT)) {
			syncFilterWithButton(filter[CALL_OUT], callOutFilterButton);
		} else if (command.equals(CallFilter.FILTER_COMMENT)) {
			syncFilterWithButton(filter[COMMENT], commentFilterButton);
		} else if (command.equals(CallFilter.FILTER_ANONYM)) {
			syncFilterWithButton(filter[ANONYMOUS], anonymFilterButton);
		} else if (command.equals(CallFilter.FILTER_FIXED)) {
			syncFilterWithButton(filter[FIXED], fixedFilterButton);
		} else if (command.equals(CallFilter.FILTER_HANDY)) {
			syncFilterWithButton(filter[MOBILE], handyFilterButton);
		} else if (command.equals(CallFilter.FILTER_SEARCH)) {
			if (searchFilterButton.getState() == ThreeStateButton.NOTHING) {
				searchFilterTextField.setVisible(false);
				searchLabel.setVisible(false);
				filter[SEARCH].setEnabled(false);
			} else {
				filter[SEARCH].setEnabled(true);
				searchFilterTextField.setVisible(true);
				searchLabel.setVisible(true);
				((SearchFilter) filter[SEARCH])
						.setSearchString(searchFilterTextField.getText());
				// do nothing
				if(searchFilterButton.getState()==ThreeStateButton.SELECTED){
					filter[SEARCH].setInvert(false);
				}
				if (searchFilterButton.getState() == ThreeStateButton.INVERTED) {
					filter[SEARCH].setInvert(true);
				}
				searchFilterTextField.requestFocus();
				searchFilterTextField.selectAll();
			}
		} else if (command.equals(CallFilter.FILTER_DATE)) {
			syncFilterWithButton(filter[DATE], dateFilterButton);
			if (dateFilterButton.getState() == ThreeStateButton.NOTHING) {
				startDateChooser.setVisible(false);
				endDateChooser.setVisible(false);
			} else { // selected or inverted check if some rows are selected
				if ((callerTable != null)
						&& (callerTable.getSelectedRowCount() != 0)) {
					// some rows are selected so the the date according to the
					// selected rows
					int[] rows = callerTable.getSelectedRows();
					// min und max bestimmen
					Date min = (callerList.getFilteredCall(rows[0])).getCalldate();
					Date max = (callerList.getFilteredCall(rows[0])).getCalldate();
					Date current;
					for (int i = 0; i < rows.length; i++) {
						current = (callerList.getFilteredCall(rows[i])).getCalldate();
						if (current.before(min)) {
							min = current;
						}
						if (current.after(max)) {
							max = current;
						}
					}
					startDateChooser.setDate(min);
					endDateChooser.setDate(max);
				} else {// no rows selected so we only have the days and set the
						// hours to min and max
					if (endDateChooser.getDate() != null)
					{
						endDateChooser.setDate(JFritzUtils
							.setEndOfDay(endDateChooser.getDate()));
					}
					else
					{
						endDateChooser.setDate(JFritzUtils
								.setEndOfDay(new Date()));
					}

					if (startDateChooser.getDate() != null)
					{
						startDateChooser.setDate(JFritzUtils
							.setStartOfDay(startDateChooser.getDate()));
					}
					else
					{   // start date invalid, set Date to begining of this year
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						cal.set(Calendar.MONTH, 0);
						cal.set(Calendar.DAY_OF_MONTH, 1);
						startDateChooser.setDate(JFritzUtils.setStartOfDay(cal.getTime()));
					}
				}
				if ((startDateChooser.getDate() != null)
						&& (endDateChooser.getDate() != null))
				{
					((DateFilter) filter[DATE]).setStartDate(startDateChooser
							.getDate());
					((DateFilter) filter[DATE])
							.setEndDate(endDateChooser.getDate());
					if (dateFilterButton.getState() == ThreeStateButton.INVERTED) {
						filter[DATE].setInvert(true);
					}
					startDateChooser.setVisible(true);
					endDateChooser.setVisible(true);
				}
			}
			dateSpecialSaveString = "";
		} else if (command.equals(CallFilter.THIS_DAY)) {
			setThisDayFilter();
		} else if (command.equals(CallFilter.LAST_DAY)) {
			setLastDayFilter();
		} else if (command.equals(CallFilter.THIS_WEEK)) {
			setThisWeekFilter();
		} else if (command.equals(CallFilter.LAST_WEEK)) {
			setLastWeekFilter();
		} else if (command.equals(CallFilter.THIS_MONTH)) {
			setThisMonthFilter();
		} else if (command.equals(CallFilter.LAST_MONTH)) {
			setLastMonthFilter();
		} else if (command.equals("filter_callinfailed_allWithoutComment")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton.SELECTED);
			commentFilterButton.setState(ThreeStateButton.INVERTED);
			syncAllFilters();
		} else if (command.equals("filter_callinfailed_allWithoutCommentLastWeek")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton.SELECTED);
			commentFilterButton.setState(ThreeStateButton.INVERTED);
			// dateFilter stuff for last week
			Calendar cal = Calendar.getInstance();
			Date start = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
			Date end = cal.getTime();
			JFritzUtils.setStartOfDay(start);
			JFritzUtils.setEndOfDay(end);

			((DateFilter) filter[DATE]).setStartDate(start);
			((DateFilter) filter[DATE]).setEndDate(end);

			startDateChooser.setDate(end);
			endDateChooser.setDate(start);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
			syncAllFilters();
		} else if (command.equals(CallFilter.FILTER_CALLBYCALL)) {
			((CallByCallFilter) filter[CALL_BY_CALL])
					.setCallbyCallProvider(getSelectedCallByCallProvider(callerList));
			syncFilterWithButton(filter[CALL_BY_CALL], callByCallFilterButton);
		} else if (command.equals("clearFilter")) { //$NON-NLS-1$
			clearAllFilter();
		} else if (command.equals(DELETE_ENTRY)) {
			if (JOptionPane.showConfirmDialog(jFrame, messages.getMessage("really_delete_entries"), //$NON-NLS-1$
					ProgramConstants.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				int rows[] = callerTable.getSelectedRows();
				log.info("Removing " + rows.length + " entries"); //$NON-NLS-1$
				callerList.removeEntries(rows);
			}
		} else if(command.startsWith("lookup:")){

			Call call;
			int[] selectedRows = callerTable.getSelectedRows();

			Vector<PhoneNumberOld> numbers = new Vector<PhoneNumberOld>();
			for (int i: selectedRows) {
				call = callerList.getCallAt(i);
				if ( call.getPhoneNumber() != null )
				{
					if (!numbers.contains(call.getPhoneNumber())) {
						numbers.add(call.getPhoneNumber());
					}
				}
			}
			callerList.reverseLookup(numbers);
		} else if (command.equals("reverselookup_dummy")) { //$NON-NLS-1$
			callerList.reverseLookup(true, true);
		} else if (command.equals("google")) {
			if (googleLink != null) {
				Debug.debug(log, googleLink);
				BrowserLaunch.openURL(googleLink);
			}
		} else if (command.equals("export_csv")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToCSV();
		} else if (command.equals("export_xml")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToXML();
		} else if (command.equals("import_callerlist_csv")) { //$NON-NLS-1$
			JFritz.getJframe().importCallerlistCSV();
		} else if (command.equals("clipboard_name")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getFullname());
				}
			}
		} else if (command.equals("clipboard_company")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getCompany());
				}
			}
		} else if (command.equals("clipboard_number")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				PhoneNumberOld number = call.getPhoneNumber();
				if ((number != null) && (call != null)) {
					JFritzClipboard.copy(number.convertToNationalNumber());
				}
			}
		} else if (command.equals("clipboard_adress")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getAddress());
				}
			}
		} else if (command.equals("clipboard_street")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getStreet());
				}
			}
		} else if (command.equals("clipboard_postalCode")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getPostalCode());
				}
			}
		} else if (command.equals("clipboard_city")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = JFritz.getPhonebook().findPerson(call);
				if (person != null) {
					JFritzClipboard.copy(person.getCity());
				}
			}
		} else if (command.equals("clipboard_type")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				JFritzClipboard.copy(call.getCalltype().toDescription());
			}
		} else if (command.equals("clipboard_date")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Date date = call.getCalldate();
				JFritzClipboard.copy(date.toString());
			}
		} else if (command.equals("clipboard_callbycall")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				if (call.getPhoneNumber() != null)
				{
					JFritzClipboard.copy(call.getPhoneNumber().getCallByCall());
				}
			}
		} else if (command.equals("clipboard_comment")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				JFritzClipboard.copy(call.getComment());
			}
		} else if (command.equals(CallFilter.FILTER_SIP)) {
			// do nothing
		} else if (command.equals(CallFilter.FILTER_PORT)) {
			// do nothing
		} else {
			log.warn("Unknown command: " + command);
		}
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastMonthFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.LAST_MONTH;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */

	private void setThisMonthFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.THIS_MONTH;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastDayFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		Date end = cal.getTime();
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.LAST_DAY;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setThisWeekFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
				- daysPastMonday);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.LAST_WEEK;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastWeekFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
				- daysPastMonday);
		Date end = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.LAST_WEEK;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setThisDayFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Date start = Calendar.getInstance().getTime();
		Date end = Calendar.getInstance().getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[DATE]).setStartDate(start);
		((DateFilter) filter[DATE]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = CallFilter.THIS_DAY;
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getSource() == searchFilterTextField
				&& arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			handleAction(CallFilter.FILTER_SEARCH);
			callerList.update();
			updateStatusBar(false);
//			statusBarController.fireStatusChanged(callerList.getTotalDuration());
			return;
		} else if (arg0.getSource() == searchFilterTextField
				&& arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			searchFilterButton.setState(ThreeStateButton.NOTHING);
			syncAllFilters();
			update();
			callerTable.requestFocus();
		}
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */

	public void keyReleased(KeyEvent arg0) {
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */
	public void keyTyped(KeyEvent arg0) {
	}

	/**
	 * to react on the change events of the Datechoosers PropertyChangedListener
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		handleAction(CallFilter.FILTER_DATE);
		callerList.update();
		updateStatusBar(false);
//		statusBarController.fireStatusChanged(callerList.getTotalDuration());
	}

	/**
	 * syncronises all Filters with the states represented by the Buttons
	 *
	 */
	private void syncAllFilters() {
		if (searchFilterButton.getState() == ThreeStateButton.NOTHING) {
			searchFilterTextField.setVisible(false);
			searchLabel.setVisible(false);
		} else {
			searchFilterTextField.setVisible(true);
			searchLabel.setVisible(true);
		}
		if (dateFilterButton.getState() == ThreeStateButton.NOTHING) {
			startDateChooser.setVisible(false);
			endDateChooser.setVisible(false);
		} else {
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		((DateFilter)filter[DATE]).setStartDate(startDateChooser.getDate());
		((DateFilter)filter[DATE]).setEndDate(endDateChooser.getDate());
		((SearchFilter)filter[SEARCH]).setSearchString(searchFilterTextField.getText());

		Vector<String> providers = new Vector<String>();
		JFritzUtils.fillVectorByString(providers, CallFilter.FILTER_SIP_PROVIDERS, ";");
		if ((providers.size() == 1)
				&& (providers.get(0).equals("$ALL$")))
		{
			((SipFilter)filter[SIP]).setAllSelected();
		}
		else
		{
			((SipFilter)filter[SIP]).setProvider(providers);
		}

		Vector<String> ports = new Vector<String>();
		JFritzUtils.fillVectorByString(ports, CallFilter.FILTER_PORT_LIST, ";");
		if ((ports.size() == 1)
				&& (ports.get(0).equals("$ALL$")))
		{
			((PortFilter)filter[PORT]).setAllSelected();
		}
		else
		{
			((PortFilter)filter[PORT]).setPorts(ports);
		}
		syncFilterWithButton(filter[CALL_IN], callInFilterButton);
		syncFilterWithButton(filter[CALL_IN_FAILED], callInFailedFilterButton);
		syncFilterWithButton(filter[CALL_OUT], callOutFilterButton);
		syncFilterWithButton(filter[COMMENT], commentFilterButton);
		syncFilterWithButton(filter[ANONYMOUS], anonymFilterButton);
		syncFilterWithButton(filter[FIXED], fixedFilterButton);
		syncFilterWithButton(filter[MOBILE], handyFilterButton);
		syncFilterWithButton(filter[DATE], dateFilterButton);
		syncFilterWithButton(filter[CALL_BY_CALL], callByCallFilterButton);
		syncFilterWithButton(filter[SEARCH], searchFilterButton);

	}

	public void setCallerList(CallerList callerList) {
		this.callerList = callerList;
	}

	/**
	 * enables/disables the delete button and changes the tooltip
	 *
	 * @param rows
	 *            the rows, wich are selected
	 */
	public void setDeleteEntriesButton(int rows) {
		if(rows==0){
			deleteEntriesButton.setToolTipText(messages.getMessage(DELETE_ENTRIES)
					.replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,
			deleteEntriesButton.setEnabled(false);
		}
		else if(rows ==1){
			deleteEntriesButton.setToolTipText(messages.getMessage(DELETE_ENTRY));
			deleteEntriesButton.setEnabled(true);
		}else{
			deleteEntriesButton.setToolTipText(messages.getMessage(DELETE_ENTRIES)
					.replaceAll("%N", Integer.toString(rows))); //$NON-NLS-1$,
			deleteEntriesButton.setEnabled(true);
		}
	}

	/**
	 * syncronises a Filter with the state represented by the Button
	 *
	 * @param filter
	 *            the filter his values will be changed
	 * @param button
	 *            the button his status will be read
	 */
	private void syncFilterWithButton(CallFilter filter,
			ThreeStateButton button) {
		if (button.getState() == ThreeStateButton.SELECTED) {
			filter.setEnabled(true);
			filter.setInvert(false);
		}
		if (button.getState() == ThreeStateButton.INVERTED) {
			filter.setEnabled(true);
			filter.setInvert(true);
		}
		if (button.getState() == ThreeStateButton.NOTHING) {
			filter.setEnabled(false);
		}
	}

	/**
	 * save the Button status to the Main Properties
	 *
	 */
	private void saveButtonStatus() {
		properties.setStateProperty(CallFilter.FILTER_SEARCH_TEXT, searchFilterTextField.getText());
		properties.setStateProperty(CallFilter.FILTER_SEARCH, "" + searchFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_COMMENT, "" + commentFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_DATE, "" + dateFilterButton.getState());
		DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
		Date start = startDateChooser.getDate();
		Date end = endDateChooser.getDate();

		if (start != null)
		{
			properties.setStateProperty(CallFilter.FILTER_DATE_START, df.format(start));
		}
		else
		{
			properties.setStateProperty(CallFilter.FILTER_DATE_START, "");
		}

		if (end != null)
		{
			properties.setStateProperty(CallFilter.FILTER_DATE_END, df.format(end));
		}
		else
		{
			properties.setStateProperty(CallFilter.FILTER_DATE_END, "");
		}

		properties.setStateProperty(CallFilter.FILTER_DATE_SPECIAL, dateSpecialSaveString);
		properties.setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, filter[SIP].toString());
		properties.setStateProperty(CallFilter.FILTER_PORT_LIST, filter[PORT].toString());
		properties.setStateProperty(CallFilter.FILTER_CALLBYCALL, ""
				+ callByCallFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_CALLOUT, "" + callOutFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_ANONYM, "" + anonymFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_FIXED, "" + fixedFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_HANDY, "" + handyFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_CALLIN_NOTHING, "" + callInFilterButton.getState());
		properties.setStateProperty(CallFilter.FILTER_CALLINFAILED, ""
				+ callInFailedFilterButton.getState());
	}

	/**
	 * load the status of the Buttons from the Main Properties
	 */
	private void loadButtonStatus() {
		int state;
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_COMMENT));
		commentFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_DATE));
		dateFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_CALLBYCALL));
		callByCallFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_CALLOUT));
		callOutFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_ANONYM));
		anonymFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_FIXED));
		fixedFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_HANDY));
		handyFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getStateProperty(CallFilter.FILTER_CALLIN_NOTHING));
		callInFilterButton.setState(state);
		state = JFritzUtils
				.parseInt(properties.getStateProperty(CallFilter.FILTER_CALLINFAILED));
		callInFailedFilterButton.setState(state);

		searchFilterTextField.setText("");
		searchFilterButton.setState(ThreeStateButton.NOTHING);
		dateSpecialSaveString = properties.getStateProperty(CallFilter.FILTER_DATE_SPECIAL);
		// Debug.msg(dateSpecialSaveString);
		if (dateSpecialSaveString.equals(CallFilter.THIS_DAY)) {
			setThisDayFilter();
		} else if (dateSpecialSaveString.equals(CallFilter.LAST_DAY)) {
			setLastDayFilter();
		} else if (dateSpecialSaveString.equals(CallFilter.THIS_WEEK)) {
			setThisWeekFilter();
		} else if (dateSpecialSaveString.equals(CallFilter.LAST_WEEK)) {
			setLastWeekFilter();
		} else if (dateSpecialSaveString.equals(CallFilter.THIS_MONTH)) {
			setThisMonthFilter();
		} else if (dateSpecialSaveString.equals(CallFilter.LAST_MONTH)) {
			setLastMonthFilter();
		} else { // read data
			DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
			Date start = new Date();
			Date end = new Date();
			try {
				start = df.parse(properties.getStateProperty(CallFilter.FILTER_DATE_START));
				end = df.parse(properties.getStateProperty(CallFilter.FILTER_DATE_END));
				startDateChooser.setDate(start); // durch setDate wird über
													// den
				endDateChooser.setDate(end); // PropertyListener update
												// aufgerufen
			} catch (ParseException e) {
				startDateChooser.setDate(Calendar.getInstance().getTime());
				endDateChooser.setDate(Calendar.getInstance().getTime());
				Debug.error(log, "Error parsing date while loading dates from main properties "
								+ e.toString());
			}
		}
		syncAllFilters();
		update();
	}


	/**
	 * Verschiebt und versteckt die Spalten je nach Einstellung des Benutzers
	 */
	public void reorderColumns()
	{
		callerTable.reorderColumns();
		callerList.fireTableStructureChanged();
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

	public void registerStatusBar()
	{
		callerListStatusBar = new StatusBarPanel(1);
		Border border = LineBorder.createGrayLineBorder();
		callsLabel = new JLabel("");
		callsLabel.setBorder(border);
		durationLabel = new JLabel();
		durationLabel.setBorder(border);
		callerListStatusBar.add(callsLabel);
		callerListStatusBar.add(durationLabel);
		jFrame.getStatusBar().registerFixStatusPanel(callerListStatusBar);

		progressStatusBar = new StatusBarPanel(2);
		progressBar = new JProgressBar();
		progressStatusBar.add(progressBar);
		progressBar.setToolTipText("Importing calls...");
		progressBar.setName("Importing calls ...");
		progressBar.setVisible(false);
		progressStatusBar.setVisible(false);
		jFrame.getStatusBar().registerDynamicStatusPanel(progressStatusBar);
	}

	public void setSelectedCallsInfo(int numSelectedCalls, double durationSelectedCalls)
	{
		this.numSelectedCalls = numSelectedCalls;
		this.durationSelectedCalls = durationSelectedCalls;
	}

	public void setStatus()
	{
		jFrame.setStatus("");
	}

	/**
	 *
	 * @param selectedEntries Specifies which type of status message will be displayed.
	 *        if true, only information about selected calls will be displayed.
	 *        if false, information about all calls will be displayed.
	 */
	public void updateStatusBar(boolean selectedEntries)
	{
		if ( selectedEntries )
		{
			if ( numSelectedCalls != 1 )
			{
				String calls = " " + messages.getMessage("entries").replaceAll( //$NON-NLS-1$, //$NON-NLS-2$
						"%N", Integer.toString(numSelectedCalls)) + " "; //$NON-NLS-1$, //$NON-NLS-2$
				String duration = " " + messages.getMessage("total_duration") + ": " + (durationSelectedCalls / 60) + " min "; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

				jFrame.setStatus(calls + " " + duration);
			} else {
				jFrame.setStatus("");
			}
		}
		else
		{
			if ( callsLabel != null )
			{
				callsLabel.setText(" " + messages.getMessage("telephone_entries").replaceAll("%N", Integer.toString(callerList.getRowCount())) + " "); //$NON-NLS-1$,  //$NON-NLS-2$
			}
			if ( durationLabel != null )
			{
				int duration = callerList.getTotalDuration();
				int hours = duration / 3600;
				int mins = duration % 3600 / 60;

				durationLabel.setText(" " + messages.getMessage("total_duration") + ": " + hours + "h " //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
						+ mins + " min " + " (" + duration / 60 + " min) "); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			}
		}
	}

	public void setMax(int max) {
		progressBar.setValue(0);
		progressBar.setMaximum(max);
		progressBar.setVisible(true);
		progressStatusBar.setVisible(true);
		jFrame.getStatusBar().refresh();
	}

	public void setMin(int min) {
		progressBar.setValue(0);
		progressBar.setMinimum(min);
		progressBar.setVisible(true);
		progressStatusBar.setVisible(true);
		jFrame.getStatusBar().refresh();
	}

	public void setProgress(int progress) {
		progressBar.setValue(progress);
		jFrame.getStatusBar().refresh();
	}

	public void finished(Vector<Call> newCalls) {
		progressBar.setVisible(false);
		progressStatusBar.setVisible(false);
		jFrame.getStatusBar().refresh();
	}

	public void adaptGoogleLink() {
		int[] rows = JFritz.getJframe().getCallerTable().getSelectedRows();
		if ((rows.length == 0) || (rows.length > 1)) {
			updateGoogleItem(false);
		} else {
			Call call = JFritz.getCallerList().getFilteredCall(rows[0]);
			Person person = JFritz.getPhonebook().findPerson(call);
			if (person != null) {
				updateGoogleItem(true);
				googleLink = person.getGoogleLink();
			} else {
				updateGoogleItem(false);
			}
		}
	}

	public void updateGoogleItem(boolean status) {
		googleItem.setEnabled(status);
		JFritz.getJframe().setGoogleItem(status);
	}

	public void toggleSearchFilter() {
		if (searchFilterButton.getState() != ThreeStateButton.NOTHING)
		{
			searchFilterTextField.setVisible(false);
			searchLabel.setVisible(false);
			searchFilterButton.setState(ThreeStateButton.NOTHING);
			this.update();
			if (focusBeforeSearch != null) {
				focusBeforeSearch.requestFocus();
			}
			filter[SEARCH].setEnabled(false);
		} else {
			focusBeforeSearch = JFritz.getJframe().getFocusOwner();
			searchFilterTextField.setVisible(true);
			searchLabel.setVisible(true);
			((SearchFilter) filter[SEARCH])
					.setSearchString(searchFilterTextField.getText());

			searchFilterButton.setState(ThreeStateButton.SELECTED);
			filter[SEARCH].setInvert(false);
			this.requestFocus();
			searchFilterTextField.requestFocus();
			searchFilterTextField.selectAll();
			filter[SEARCH].setEnabled(true);
		}
		update();
	}

	public void createReverselookupProgressBar()
	{
		if ( reverseLookupStatusBar == null )
		{
			reverseLookupStatusBar = new StatusBarPanel(2);
			reverseLookupLabel = new JLabel("Reverse lookup: ");
			lookupProgressBar = new JProgressBar();
			lookupProgressBar.setVisible(false);
			lookupProgressBar.setMinimum(0);
			lookupProgressBar.setMaximum(100);
			lookupProgressBar.setValue(0);
			lookupProgressBar.setStringPainted(true);

			reverseLookupStatusBar.add(reverseLookupLabel);
			reverseLookupStatusBar.add(lookupProgressBar);
			if (   (JFritz.getJframe() != null )
				&& (JFritz.getJframe().getStatusBar() != null ))
				{
					JFritz.getJframe().getStatusBar().registerDynamicStatusPanel(reverseLookupStatusBar);
				}
			updateReverselookupProgressBar(0);
		}
	}

	public void updateReverselookupProgressBar(int percent)
	{
		if ( percent == 100)
		{
			reverseLookupLabel.setVisible(false);
			lookupProgressBar.setVisible(false);
			lookupProgressBar.setValue(0);
			if (reverseLookupStatusBar != null) {
				reverseLookupStatusBar.setVisible(false);
				reverseLookupStatusBar = null;
			}
		}
		else
		{
			reverseLookupStatusBar.setVisible(true);
			reverseLookupLabel.setVisible(true);
			lookupProgressBar.setVisible(true);
			Dimension dim = new Dimension(100, 20);
			lookupProgressBar.setMinimumSize(dim);
			lookupProgressBar.setMaximumSize(dim);
			lookupProgressBar.setPreferredSize(dim);
			lookupProgressBar.setValue(percent);
			if ( percent == 0 )
			{
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(false);
			}
			else
			{
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(true);
			}
		}

		if (JFritz.getJframe() != null && JFritz.getJframe().getStatusBar() != null)
		{
			JFritz.getJframe().getStatusBar().refresh();
		}
	}

}