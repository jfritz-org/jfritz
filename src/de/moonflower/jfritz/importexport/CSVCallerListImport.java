package de.moonflower.jfritz.importexport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 *
 *
 * @author Robert
 *
 */
public class CSVCallerListImport extends CSVImport implements ICSVImport {
	private final static Logger log = Logger.getLogger(CSVCallerListImport.class);

	private Vector<String> availableColumns;

	private Vector<Call> importedCalls;

	protected PropertyProvider properties = PropertyProvider.getInstance();

	public CSVCallerListImport(final String fileName) {
		super(fileName);
		availableColumns = CallerTable.getCallerTableColumns();
		availableColumns.remove(CallerTable.COLUMN_CALL_BY_CALL);
		availableColumns.remove(CallerTable.COLUMN_PARTICIPANT);
		availableColumns.remove(CallerTable.COLUMN_PICTURE);
		availableColumns.remove(CallerTable.COLUMN_CITY);
		importedCalls = new Vector<Call>(20);
	}

	public Vector<String> getAvailableColumns() {
		return availableColumns;
	}

	public void csvImport() {
		this.setSeparaor(";");
		String line = "";
		importedCalls = new Vector<Call>(20);
		while ((line = readLine()) != null)
		{
			Vector<String> splitValues = this.split(line);
			Call entry = new Call(null, null, null, null, "", 0);
			boolean error = false;
			for (int i=0; i<splitValues.size() && !error; i++) {
				String columnName = this.getMappedColumn(i);
				if (columnName != null) {
					error = fillData(entry, columnName, splitValues.get(i));
				}
			}
			if (!error) {
				importedCalls.add(entry);
			}
		}
	}

	private boolean fillData(Call entry, String columnName, String value) {
		if (columnName.equals(CallerTable.COLUMN_TYPE)) {
			if (value.equals(CallType.CALLIN)
				|| value.equals(CallType.CALLIN.toString())
				|| value.equals("1") // FritzBox CSV
				|| value.equals("Incoming") // JFritz CSV
				|| value.equals("accepted")) // jAnrufmonitor CSV
			{
				entry.setCallType(CallType.CALLIN);
			} else if (value.equals(CallType.CALLIN_FAILED)
				|| value.equals(CallType.CALLIN_FAILED.toString())
				|| value.equals("2") // FritzBox CSV
				|| value.equals("Missed") // JFritz CSV
				|| value.equals("away")) // jAnrufmonitor CSV
			{
				entry.setCallType(CallType.CALLIN_FAILED);
			} else if (value.equals(CallType.CALLIN_BLOCKED)
					|| value.equals(CallType.CALLIN_BLOCKED.toString())
					|| value.equals("3") // FritzBox CSV
					|| value.equals("Blocked") // JFritz CSV
					|| value.equals("blocked")) // jAnrufmonitor CSV
			{
				entry.setCallType(CallType.CALLIN_BLOCKED);
			} else if (value.equals(CallType.CALLOUT)
				|| value.equals(CallType.CALLOUT.toString())
				|| value.equals("3") // FritzBox CSV
				|| value.equals("4") // FritzBox CSV 05.50
				|| value.equals("Outgoing") // JFritz CSV
				|| value.equals("outgoing")) // jAnrufmonitor CSV
			{
				entry.setCallType(CallType.CALLOUT);
			} else {
				Debug.error(log, "Unknown call type: " + value);
				return true;
			}
		} else if (columnName.equals(CallerTable.COLUMN_DATE)) {
			Date callDate;
			GregorianCalendar calendar = new GregorianCalendar();
			SimpleDateFormat dateFormat;

			// check date and time
			dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss"); //$NON-NLS-1$
			try {
				callDate = dateFormat.parse(value);
				entry.setCalldate(callDate);
			} catch (ParseException pe) {
				dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
				try {
					callDate = dateFormat.parse(value);
					entry.setCalldate(callDate);
				} catch (ParseException e) {
					dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm"); //$NON-NLS-1$
					try {
						callDate = dateFormat.parse(value);
						entry.setCalldate(callDate);
					} catch (ParseException e0) {
						dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
						try {
							callDate = dateFormat.parse(value);
							entry.setCalldate(callDate);
						} catch (ParseException e1) {
							dateFormat = new SimpleDateFormat("dd.MM.yy");
							try {
								GregorianCalendar dateCal = new GregorianCalendar();
								dateCal.setTime(dateFormat.parse(value));
								if (entry.getCalldate() != null) {
									// extract time value
									calendar.setTime(entry.getCalldate());
									calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
									calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
									calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
									entry.setCalldate(calendar.getTime());
								} else {
									// no date set, just set callDate as date
									entry.setCalldate(dateCal.getTime());
								}
							} catch (ParseException pe3) {
								dateFormat = new SimpleDateFormat("dd.MM.yyyy");
								try {
									GregorianCalendar dateCal = new GregorianCalendar();
									dateCal.setTime(dateFormat.parse(value));

									if (entry.getCalldate() != null) {
										// extract time value
										calendar.setTime(entry.getCalldate());
										calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
										calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
										calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
										entry.setCalldate(calendar.getTime());
									} else {
										// no date set, just set callDate as date
										entry.setCalldate(dateCal.getTime());
									}
								} catch (ParseException pe77) {
									dateFormat = new SimpleDateFormat("HH:mm:ss");
									try {
										GregorianCalendar timeCal = new GregorianCalendar();
										timeCal.setTime(dateFormat.parse(value));

										if (entry.getCalldate() != null) {
											// extract time value
											calendar.setTime(entry.getCalldate());
											calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
											calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
											calendar.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
											entry.setCalldate(calendar.getTime());
										} else {
											// no date set, just set callDate as date
											entry.setCalldate(timeCal.getTime());
										}
									} catch (ParseException e4) {
										dateFormat = new SimpleDateFormat("HH:mm");
										try {
											GregorianCalendar timeCal = new GregorianCalendar();
											timeCal.setTime(dateFormat.parse(value));

											if (entry.getCalldate() != null) {
												// extract time value
												calendar.setTime(entry.getCalldate());
												calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
												calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
												entry.setCalldate(calendar.getTime());
											} else {
												// no date set, just set callDate as date
												entry.setCalldate(timeCal.getTime());
											}
										} catch (ParseException e3) {
											Debug.error(log, "Could not parse date: " + value);
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		} else if (columnName.equals(CallerTable.COLUMN_COMMENT)) {
			entry.setComment(value);
		} else if (columnName.equals(CallerTable.COLUMN_CALL_BY_CALL)) {
			if (entry.getPhoneNumber() != null) {
				entry.getPhoneNumber().setCallByCall(value);
			} else {
				PhoneNumberOld number = new PhoneNumberOld(this.properties, "", false);
				number.setCallByCall(value);
				entry.setPhoneNumber(number);
			}
		} else if (columnName.equals(CallerTable.COLUMN_NUMBER)) {
			boolean useDialPrefix = JFritzUtils.parseBoolean(properties.getProperty("option.activateDialPrefix"))
									&& (entry.getCalltype().toString().equals(CallType.CALLOUT.toString()));

			String callByCall = "";
			if (entry.getPhoneNumber() != null) {
				callByCall = entry.getPhoneNumber().getCallByCall();
				if (value.equals("unbekannt")) {
					entry.setPhoneNumber(new PhoneNumberOld(this.properties, "", false));
				} else {
					entry.setPhoneNumber(new PhoneNumberOld(this.properties, value, useDialPrefix));
				}
				entry.getPhoneNumber().setCallByCall(callByCall);
			} else {
				if (value.equals("unbekannt")) {
					entry.setPhoneNumber(new PhoneNumberOld(this.properties, "", false));
				} else {
					entry.setPhoneNumber(new PhoneNumberOld(this.properties, value, useDialPrefix));
				}
			}
		} else if (columnName.equals(CallerTable.COLUMN_PORT)) {
			entry.setPort(new Port(0, value, "-1", "-1"));
		} else if (columnName.equals(CallerTable.COLUMN_DURATION)) {
			try {
				if (value.equals(""))
				{
					entry.setDuration(0);
				}
				else if (value.endsWith(" min ")) {
					int min = Integer.parseInt(value.substring(0, value.length()-5));
					entry.setDuration(min * 60);
				} else if (value.endsWith(" min")) {
					int min = Integer.parseInt(value.substring(0, value.length()-4));
					entry.setDuration(min * 60);
				} else {
					int duration = Integer.parseInt(value);
					entry.setDuration(duration);
				}
			} catch (NumberFormatException nfe) {
				if (value.contains(":")) {
					String[] time = value.split(":");
					try {
						entry.setDuration((Integer.parseInt(time[0]) * 3600) + (Integer.parseInt(time[1])*60));
					} catch (NumberFormatException nfe2) {
						Debug.error(log, "Could not parse duration: " + value);
						return true;
					}
				}
			}
		} else if (columnName.equals(CallerTable.COLUMN_ROUTE)) {
			entry.setRoute(value);
		}

		return false;
	}

	public Vector<Call> getImportedCalls() {
		return importedCalls;
	}

	public static void main(String[] args) {
		CSVCallerListImport csvImport = new CSVCallerListImport("CSVTest.csv");
		csvImport.openFile();
		csvImport.mapColumn(0, CallerTable.COLUMN_TYPE);
		csvImport.mapColumn(1, CallerTable.COLUMN_DATE);
		csvImport.mapColumn(2, CallerTable.COLUMN_DATE);
		csvImport.mapColumn(3, CallerTable.COLUMN_NUMBER);
		csvImport.mapColumn(4, CallerTable.COLUMN_ROUTE);
		csvImport.mapColumn(5, CallerTable.COLUMN_PORT);
		csvImport.mapColumn(6, CallerTable.COLUMN_DURATION);
		csvImport.mapColumn(10, CallerTable.COLUMN_CALL_BY_CALL);
		csvImport.mapColumn(11, CallerTable.COLUMN_COMMENT);
		csvImport.csvImport();
		csvImport.closeFile();
	}
}
