package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JTable;
/**
 * This is used to make the editable buttons in the table
 *
 * @author brian
 *
 */
public class ButtonCellEditor extends DefaultCellEditor {

	public static final long serialVersionUID = 100;

	protected JButton button;
	private String label;
	private boolean isPushed;
	private JTable table;
	private int column, row;
	private JDialog parent;

	public ButtonCellEditor(JCheckBox checkBox, JDialog parent){
		super(checkBox);
		this.parent = parent;

		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fireEditingStopped();
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column){

		if(isSelected){
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		}else{
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		this.column = column;
		this.row = row;
		this.table = table;
		label = (value == null) ? "" : value.toString();
		button.setText(label);
		isPushed = true;
		return button;
	}

	public Object getCellEditorValue(){
		if(isPushed){
			table.setValueAt(parent, row, column);
		}
		isPushed = false;
		return label;
	}

	public boolean stopCellEditing(){
		isPushed = false;
		return super.stopCellEditing();
	}

	public void fireEditingStopped(){
		super.fireEditingStopped();
	}
}
