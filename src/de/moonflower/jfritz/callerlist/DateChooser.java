package de.moonflower.jfritz.callerlist;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JFrame;

public class DateChooser {
	//NOT USED!
	public static final int APPROVE_OPTION = 1;
	public static final int CANCEL_OPTION = 2;
	private Date startDate;
	private Date endDate;
	private int returnValue;
	DateChooserDialog dialog;

	public int showDateChooserDialog(JFrame parent){
		dialog = new DateChooserDialog(parent);
		dialog.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			returnValue = CANCEL_OPTION;
		    }
		});
		dialog.setVisible(true);
		dialog.dispose();
		dialog = null;
		return DateChooser.APPROVE_OPTION;
	}

    public void approveSelection() {
    	returnValue = APPROVE_OPTION;
    	if(dialog != null) {
    	    dialog.setVisible(false);
    	}
    }

	public Date getEndDate() {
		return endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

}
