package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import de.moonflower.jfritz.JFritzEvent.JFritzEventDispatcher;
import de.moonflower.jfritz.JFritzEvent.events.JFritzEvent;
import de.moonflower.jfritz.JFritzEvent.struct.ConditionObject;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;
import de.moonflower.jfritz.callerlist.TextFieldCellEditor;
import de.moonflower.jfritz.messages.MessageProvider;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class JFritzEventActionGUI extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2068185369595737849L;

	private boolean okPressed = false;

	private JFritzEventAction eventAction;
	private JLabel actionLabel;
	private JTable actionTable;
	private JTable conditionTable;
	private JButton okButton;
	private JButton cancelButton;
	private JTextField nameTextField;
	private JRadioButton none_concatenation, and_concatenation, or_concatenation;
	private JComboBox eventComboBox;
	protected MessageProvider messages = MessageProvider.getInstance();

	public JFritzEventActionGUI(JDialog parent, JFritzEventAction eventAction) {
		super(parent,true);
		this.eventAction = eventAction;
		createGUI();
		setTitle(messages.getMessage("event_management"));
//		setSize(600,500);
		setValues();
		pack();
		if ( parent != null )
			setLocationRelativeTo(parent);
	}

	private void createButtonColumn(TableColumn column) {
		column.setPreferredWidth(5);
		column.setCellRenderer(new TableButtonRenderer());
		column.setCellEditor(new TableButtonEditor(new JCheckBox()));
	}

	private void createGUI() {
		BorderLayout outerLayout = new BorderLayout();
		getContentPane().setLayout(outerLayout);
		JPanel innerPanel = new JPanel();
		GridBagLayout innerLayout = new GridBagLayout();
		innerPanel.setLayout(innerLayout);
		innerLayout.rowHeights = new int[] {20, -1, 7, 20, 7, 13, 7, 7, 7, 20, 20, 20, 7, 20, 20};
		innerLayout.rowWeights = new double[] {0.1, 0.0, 0.1, 0.1, 0.1, 0.0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.0, 0.1, 0.1};
		innerLayout.columnWeights = new double[] {0.1, 0.0, 0.0, 0.0, 0.1, 0.1, 0.1};
		innerLayout.columnWidths = new int[] {20, 67, 46, 94, 20, 20, 20};

		// Filter name
		JLabel nameLabel = new JLabel("name"+": ");
		innerPanel.add(nameLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		nameTextField = new JTextField("", 20); //$NON-NLS-1$
		nameTextField.setMinimumSize(new Dimension(200, 20));
		innerPanel.add(nameTextField, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		JLabel eventLabel = new JLabel("event"+": ");
		innerPanel.add(eventLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		eventComboBox = JFritzEventDispatcher.createEventComboBox();
		innerPanel.add(eventComboBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		// condition concatenation
		JLabel conditionLabel = new JLabel("which_condition");
		conditionTable = new JTable(new ConditionTableModel(eventAction));
		innerPanel.add(conditionLabel, new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		ButtonGroup conditionGroup = new ButtonGroup();
		none_concatenation = new JRadioButton(messages.getMessage("none_condition"));
		none_concatenation.setActionCommand("none_condition");
		and_concatenation = new JRadioButton(messages.getMessage("and_condition"));
		and_concatenation.setActionCommand("and_condition");
		or_concatenation = new JRadioButton(messages.getMessage("or_condition"));
		or_concatenation.setActionCommand("or_condition");
		none_concatenation.setSelected(true);
		conditionGroup.add(none_concatenation);
		conditionGroup.add(and_concatenation);
		conditionGroup.add(or_concatenation);
		ConditionConcatenationListener conditionConcatenationListener = new ConditionConcatenationListener(conditionTable, eventAction);
		none_concatenation.addActionListener(conditionConcatenationListener);
		and_concatenation.addActionListener(conditionConcatenationListener);
		or_concatenation.addActionListener(conditionConcatenationListener);

		innerPanel.add(none_concatenation, new GridBagConstraints(1, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		innerPanel.add(and_concatenation, new GridBagConstraints(1, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		innerPanel.add(or_concatenation, new GridBagConstraints(1, 7, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		// condition table
		conditionTable.setPreferredScrollableViewportSize(new Dimension(550,80));
		conditionTable.setRowHeight(24);
		conditionTable.setFocusable(false);
		conditionTable.setRowSelectionAllowed(false);
		conditionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn parameterColumn = conditionTable.getColumnModel().getColumn(0);
		JComboBox parameterComboBox = new JComboBox();
		for ( byte i=0; i<eventAction.getEvent().getParameterCount(); i++) {
			parameterComboBox.addItem(eventAction.getEvent().getParameter(i));
		}
		parameterColumn.setCellEditor(new DefaultCellEditor(parameterComboBox));
		TableColumn conditionColumn = conditionTable.getColumnModel().getColumn(1);
		conditionColumn.setCellEditor(new DefaultCellEditor(ConditionObject.createComboBox()));
		TableColumn valueColumn = conditionTable.getColumnModel().getColumn(2);
		valueColumn.setCellEditor(new TextFieldCellEditor());

		createButtonColumn(conditionTable.getColumnModel().getColumn(3)); // add condition button
		createButtonColumn(conditionTable.getColumnModel().getColumn(4)); // remove condition button

		innerPanel.add(new JScrollPane(conditionTable), new GridBagConstraints(1, 8, 5, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		// action buttons
		actionLabel = new JLabel();
		innerPanel.add(actionLabel, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		actionLabel.setText("action");

		// action table
		actionTable = new JTable(new ActionTableModel(this, eventAction));
		actionTable.setPreferredScrollableViewportSize(new Dimension(550,80));
		actionTable.setRowHeight(24);
		actionTable.setFocusable(false);
		actionTable.setRowSelectionAllowed(false);
		actionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn actionNameColumn = actionTable.getColumnModel().getColumn(0);
		actionNameColumn.setCellEditor(new DefaultCellEditor(JFritzEventDispatcher.createActionComboBox()));

		TableColumn actionDescriptionColumn = actionTable.getColumnModel().getColumn(1);
		actionDescriptionColumn.setCellEditor(new TextFieldCellEditor());
		createButtonColumn(actionTable.getColumnModel().getColumn(2)); // edit action button
		createButtonColumn(actionTable.getColumnModel().getColumn(3)); // add action button
		createButtonColumn(actionTable.getColumnModel().getColumn(4)); // remove action button

		innerPanel.add(new JScrollPane(actionTable), new GridBagConstraints(1, 11, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));


		JPanel buttonPanel = new JPanel();
		FlowLayout buttonPanelLayout = new FlowLayout();
		buttonPanel.setLayout(buttonPanelLayout);

		{
			okButton = new JButton();
			buttonPanel.add(okButton);
			okButton.setText("ok");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);
		}
		{
			cancelButton = new JButton();
			buttonPanel.add(cancelButton);
			cancelButton.setText("cancel");
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);
		}

		getContentPane().add(innerPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	private void setValues() {
		nameTextField.setText(eventAction.getDescription());
		switch (eventAction.getConditionConcatenation()) {
		case JFritzEventAction.NONE:
			none_concatenation.setSelected(true);
			conditionTable.setEnabled(false);
			break;
		case JFritzEventAction.AND:
			and_concatenation.setSelected(true);
			break;
		case JFritzEventAction.OR:
			or_concatenation.setSelected(true);
			break;
		}

		// TODO condition-table
		// TODO action-table
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed;
	}

	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("ok")) {
			eventAction.setDescription(nameTextField.getText());
			eventAction.setEvent((JFritzEvent)eventComboBox.getSelectedItem());
			okPressed = true;
			setVisible(false);
		} else {
			setVisible(false);
		}

	}

}

