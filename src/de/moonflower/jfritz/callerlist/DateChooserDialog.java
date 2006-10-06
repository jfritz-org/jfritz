package de.moonflower.jfritz.callerlist;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.toedter.calendar.JDateChooser;

//NOT USED
public class DateChooserDialog extends JDialog{
	private JDateChooser startChooser;
	private JDateChooser endChooser;
	private static final long serialVersionUID = 1L;


	public DateChooserDialog(JFrame parent){
		super(parent, true);
		startChooser = new JDateChooser();
		endChooser = new JDateChooser();
//		super.
	}


}
