package de.moonflower.jfritz.backup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritzDataDirectory;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class JFritzBackup {
	private final static Logger log = Logger.getLogger(JFritzBackup.class);
    private final static SimpleDateFormat df = new SimpleDateFormat( "yyyy.MM.dd_HH.mm.ss" ); //$NON-NLS-1$

    private static JFritzBackup INSTANCE = new JFritzBackup();

    protected static PropertyProvider properties = PropertyProvider.getInstance();
	protected static MessageProvider messages = MessageProvider.getInstance();

	private JFritzBackup() {
		// Singleton, use getInstance()
	}

	public static JFritzBackup getInstance() {
		return INSTANCE;
	}

	/**
     * creates a directory-structure in the following format: backup/yyyy.MM.dd_HH.mm.ss
     *
     */
    private String createDirectory(final String targetDirectory) {
        boolean success = (new File(targetDirectory)).mkdirs();
        if (!success) {
            log.error("Directory creation failed"); //$NON-NLS-1$
        }
        return targetDirectory;
    }

	public void doBackup() {
    	Date date = Calendar.getInstance().getTime();
		doBackup(JFritzDataDirectory.getInstance().getDataDirectory(), createDirectory(JFritzDataDirectory.getInstance().getDataDirectory() + "backup"+File.separator + df.format( date )));
	}

	public void doBackup(final String sourceDirectory, final String targetDirectory) {
    	if (JFritzUtils.parseBoolean(properties.getProperty("option.keepImportantBackupsOnly")))
    	{
    		clearOldBackups();
    	}

    	String dest = targetDirectory;
    	if ((dest != null)
    		&& (!dest.endsWith(File.separator)))
    	{
    		dest += File.separator;
    	}
    	else return;

    	if ((sourceDirectory != null)
			&& (sourceDirectory.equals(dest)))
    	{
    		Debug.errDlg(log, messages.getMessage("backup_to_source_directory"));
    		return;
    	}

    	try {
			FileUtils.copyDirectory(new File(sourceDirectory), new File(dest), new MyFileFilter("xml"));
			log.info("Created backup successfully");
		} catch (IOException e) {
			Debug.errDlg(log, "Error while creating backup", e);
		}
	}

    /**
     * Deletes old backup files and directories.
     */
    public void clearOldBackups() {
        File dir = new File(JFritzDataDirectory.getInstance().getDataDirectory() + "backup");
        final Vector<Date> backupDates = new Vector<Date>(20);
        dir.listFiles(new FileFilter() {
            public boolean accept(File arg0) {
            	if (arg0.isDirectory())
            	{
	                try {
						Date date = (Date)df.parse(arg0.getName());
						backupDates.add(date);
						return true;
					} catch (ParseException e) {
						log.warn("Directory '"+arg0.getAbsolutePath()+"' is not a valid backup directory!");
					}
            	}
                return false;
            }
        });

        Collections.sort(backupDates); // sort backup directories in ascending order

        Vector<Date> deleteBackupList = new Vector<Date>(backupDates.size());
        Date currentDate = Calendar.getInstance().getTime();
        for (int i=0; i<backupDates.size(); i++)
    	{
        	int diffToToday = JFritzUtils.subtractDays(currentDate, backupDates.get(i));

        	boolean newerFound = false;
        	GregorianCalendar gc1 = new GregorianCalendar();
    		GregorianCalendar gc2 = new GregorianCalendar();
        	for (int j=i+1; j<backupDates.size() && !newerFound; j++) {
        		gc1.setTime(backupDates.get(i));
        		gc2.setTime(backupDates.get(j));

        		int year1 = gc1.get(Calendar.YEAR);
        		int year2 = gc2.get(Calendar.YEAR);

        		int month1 = gc1.get(Calendar.MONTH);
        		int month2 = gc2.get(Calendar.MONTH);

        		int week1 = gc1.get(Calendar.WEEK_OF_YEAR);
        		int week2 = gc2.get(Calendar.WEEK_OF_YEAR);

        		int day1 = gc1.get(Calendar.DAY_OF_MONTH);
        		int day2 = gc2.get(Calendar.DAY_OF_MONTH);

            	if (diffToToday > 93) {
                	// Older than 3 months, keep only one copy of this month
            		// delete it, if an newer entry of same year and month exists
            		if ((year1 == year2)
            			&& (month1 == month2))
            		{
                		deleteBackupList.add(backupDates.get(i));
                		newerFound = true;
            		}
            	} else if (diffToToday > 31)
        		{
                	// Older than 1 month, keep only one copy per week (latest one)
            		// delete it, if an newer entry of same year and weekofyear exists
            		if ((year1 == year2)
            				&& (week1 == week2))
            		{
                		deleteBackupList.add(backupDates.get(i));
            			newerFound = true;
            		}
        		} else if (diffToToday > 7) {
                	// Older than 1 week, keep only one copy per day (latest one)
            		// delete it, if an newer entry of same year, month and day exists
            		if ((year1 == year2)
            				&& (month1 == month2)
            				&& (day1 == day2))
            		{
                		deleteBackupList.add(backupDates.get(i));
            			newerFound = true;
            		}
        		}
        	}
    	}

        for (Date deleteDate:deleteBackupList) {
        	File deleteFile = new File(dir + File.separator + df.format(deleteDate));
        	deleteTree(deleteFile);
        }
    }

	private void deleteTree(File path) {
		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory())
					deleteTree(file);
				file.delete();
			}
			path.delete();
		}
	}

	private class MyFileFilter implements FileFilter {
		private final String fileEnding;

		public MyFileFilter(final String fileEnding) {
			this.fileEnding = fileEnding;
		}

		@Override
		public boolean accept(final File file) {
            if (file.isFile() && file.getName().endsWith(fileEnding))
                return true;
            return false;
		}

	}
}
