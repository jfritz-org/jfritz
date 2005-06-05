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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Arno Willig
 *
 */
public class CallerListPanel extends JPanel implements ActionListener,
		CaretListener {
	private JFritz jfritz;

	private JFritzProperties properties;

	private CallerTable callerTable;

	private JToggleButton dateButton;

	private JTextField searchFilter;

	public CallerListPanel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		this.properties = jfritz.getProperties();

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
		tb.setToolTipText(jfritz.getMessages().getString("filter_callin"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callin", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("callinfailed_grey.png"), true);
		tb.setSelectedIcon(getImage("callinfailed.png"));
		tb.setActionCommand("filter_callinfailed");
		tb.addActionListener(this);
		tb
				.setToolTipText(jfritz.getMessages().getString(
						"filter_callinfailed"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callinfailed", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("callout_grey.png"), true);
		tb.setSelectedIcon(getImage("callout.png"));
		tb.setActionCommand("filter_callout");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_callout"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.callout", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("phone_grey.png"), true);
		tb.setSelectedIcon(getImage("phone.png"));
		tb.setActionCommand("filter_number");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_number"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.number", "false")));
		toolBar.add(tb);

		tb = new JToggleButton(getImage("handy_grey.png"), true);
		tb.setSelectedIcon(getImage("handy.png"));
		tb.setActionCommand("filter_handy");
		tb.addActionListener(this);
		tb.setToolTipText(jfritz.getMessages().getString("filter_handy"));
		tb.setSelected(!JFritzUtils.parseBoolean(properties.getProperty(
				"filter.handy", "false")));
		toolBar.add(tb);

		dateButton = new JToggleButton(getImage("calendar_grey.png"), true);
		dateButton.setSelectedIcon(getImage("calendar.png"));
		dateButton.setActionCommand("filter_date");
		dateButton.addActionListener(this);
		dateButton
				.setToolTipText(jfritz.getMessages().getString("filter_date"));
		dateButton.setSelected(!JFritzUtils.parseBoolean(properties
				.getProperty("filter.date", "false")));
		setDateFilterText();
		toolBar.add(dateButton);

		toolBar.addSeparator();

		toolBar
				.add(new JLabel(jfritz.getMessages().getString("search") + ": "));
		searchFilter = new JTextField(properties.getProperty("filter.search",
				""), 10);
		searchFilter.addCaretListener(this);
		searchFilter.addCaretListener(new CaretListener() {
			String filter = "";

			public void caretUpdate(CaretEvent e) {
				JTextField search = (JTextField) e.getSource();
				if (!filter.equals(search.getText())) {
					filter = search.getText();
					properties.setProperty("filter.search", filter);
					jfritz.getCallerlist().updateFilter();
					jfritz.getCallerlist().fireTableStructureChanged();
				}
			}

		});

		toolBar.add(searchFilter);
		JButton button = new JButton(jfritz.getMessages().getString("clear"));
		button.setActionCommand("clearSearchFilter");
		button.addActionListener(this);
		toolBar.add(button);
		return toolBar;

	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable(jfritz);

		callerTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					jfritz.getJframe().activatePhoneBook();
				}
			}

		});
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
		if (JFritzUtils.parseBoolean(properties.getProperty("filter.date"))) {
			if (properties.getProperty("filter.date_from").equals(
					properties.getProperty("filter.date_to"))) {
				dateButton.setText(properties.getProperty("filter.date_from"));
			} else {
				dateButton.setText(properties.getProperty("filter.date_from")
						+ " - " + properties.getProperty("filter.date_to"));
			}
		} else {
			dateButton.setText("");
		}
	}

	public void setDataFilterFromSelection() {
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

		properties.setProperty("filter.date_from", fromstr);
		properties.setProperty("filter.date_to", tostr);
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
			properties.setProperty("filter.callin", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();

		} else if (e.getActionCommand().equals("filter_callinfailed")) {
			properties.setProperty("filter.callinfailed", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand().equals("filter_callout")) {
			properties.setProperty("filter.callout", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_number") {
			properties.setProperty("filter.number", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_handy") {
			properties.setProperty("filter.handy", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "filter_date") {
			properties.setProperty("filter.date", Boolean
					.toString(!((JToggleButton) e.getSource()).isSelected()));
			setDataFilterFromSelection();
			jfritz.getCallerlist().fireTableStructureChanged();
		} else if (e.getActionCommand() == "clearSearchFilter") {
			setSearchFilter("");
			properties.setProperty("filter.search", "");
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();

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
			properties.setProperty("filter.search", filter);
			jfritz.getCallerlist().updateFilter();
			jfritz.getCallerlist().fireTableStructureChanged();
		}

	}
}
