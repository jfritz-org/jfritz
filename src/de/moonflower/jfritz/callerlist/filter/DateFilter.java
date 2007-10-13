/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Date;
import com.toedter.calendar.JDateChooser;
import de.moonflower.jfritz.struct.Call;

/**
 * Date filter for call list
 *
 * @author Robert Palmer
 */
public class DateFilter extends CallFilter {
	public JDateChooser d;

	private Date startDate;

	private Date endDate;

	private static final String type = FILTER_DATE;

	public String specialType = " ";

	public DateFilter(Date from, Date to) {
		// make sure from is not after to
		if (from.after(to)) {
			Date temp = from;
			from = to;
			to = temp;
		}
		//        	Debug.msg(from.toLocaleString());
		//        	Debug.msg(to.toLocaleString());
		//TODO status updaten
		startDate = from;
		endDate = to;

	}

	public boolean passInternFilter(Call currentCall) {
		Date currentDate = currentCall.getCalldate();
		if (currentDate.after(startDate) && currentDate.before(endDate)) {
			return true;
		}
		if (currentDate.equals(startDate)) {
			return true;
		}
		if (currentDate.equals(endDate)) {
			return true;
		}
		return false;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getType(){
		return type;
	}

	public DateFilter clone(){
		DateFilter df = new DateFilter(startDate, endDate);
		df.specialType = this.specialType;
		df.setEnabled(this.isEnabled());
		df.setInvert(this.isInvert());
		return df;
	}

}