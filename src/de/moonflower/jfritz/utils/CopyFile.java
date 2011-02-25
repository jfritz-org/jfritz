package de.moonflower.jfritz.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;

/**
 * This class allows to backup files to a
 * file structure consisting out of the current date.
 *
 *  @author Bastian Schaefer
 */
public class CopyFile {

    private File[] entries;
    private FileInputStream[] in;
    private FileOutputStream[] out;
    private int numberOfFiles;
    private String sourceDirectory, fileFormat;
    private Date date;
    SimpleDateFormat df = new SimpleDateFormat( "yyyy.MM.dd_HH.mm.ss" ); //$NON-NLS-1$
    protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

    /**
     * gets all files with the ending "fileFormat" in directory "sourceDirectory"
     *
     */
    private void getFiles() {
        File dir = new File(sourceDirectory);
        entries = dir.listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                if (arg0.getName().endsWith(fileFormat))
                    return true;
                return false;
            }
        });
        numberOfFiles = entries.length;

        in = new FileInputStream[numberOfFiles];

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                in[i] = new FileInputStream(Main.SAVE_DIR + entries[i].getName());
            } catch (IOException ex) {
                Debug.error(ex.toString());
            }
        }

    }

    /**
     * creates a directory-structure in the following format: backup/yyyy.MM.dd_HH.mm.ss
     *
     */
    private String createDirectory() {
    	String dir = Main.SAVE_DIR + "backup"+File.separator + df.format( date );
        boolean success = (new File(dir)) //$NON-NLS-1$
                .mkdirs();
        if (!success) {
            Debug.error("Directory creation failed"); //$NON-NLS-1$
        }
        return dir;
    }

    /**
     * Deletes old backup files and directories.
     */
    private void clearOldBackups(final String fileFormat) {
        File dir = new File(Main.SAVE_DIR + "backup");
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
						Debug.warning("Directory '"+arg0.getAbsolutePath()+"' is not a valid backup directory!");
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

    /**
     * copies all files, which were fetched with getFiles() to the just
     * created folder by method createDirectory()
     *
     */
    public void copy(String sourceDirectory, String fileFormat) {
        date = Calendar.getInstance().getTime();
        copy(sourceDirectory, fileFormat, createDirectory());
    }

    /**
     * copies all  files, which were fetched with getFiles() to the
     * parametric passed folder "targetDirectory"
     *
     */
    public void copy(String sourceDirectory, String fileFormat, String targetDirectory) {

    	if (JFritzUtils.parseBoolean(properties.getProperty("option.keepImportantBackupsOnly")))
    	{
    		clearOldBackups(fileFormat);
    	}

    	if ((targetDirectory != null)
    		&& (!targetDirectory.endsWith(File.separator)))
    	{
    		targetDirectory += File.separator;
    	}
    	else return;

    	if ((sourceDirectory != null)
			&& (sourceDirectory.equals(targetDirectory)))
    	{
    		Debug.errDlg(messages.getMessage("backup_to_source_directory"));
    		return;
    	}

    	// do this only if sourceDirectory != targetDirectory
        this.sourceDirectory = sourceDirectory;
        this.fileFormat = fileFormat;
        getFiles();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.info("Found file to backup: " + entries[i].getName()); //$NON-NLS-1$
                out[i] = new FileOutputStream( targetDirectory + File.separator + entries[i].getName());
                byte[] buf = new byte[4096];
                int len;
                while ((len = in[i].read(buf)) > 0) {
                    out[i].write(buf, 0, len);
                }
                in[i].close();
                out[i].close();
            } catch (IOException ex) {
                Debug.error(ex.toString());
            } catch (ArrayIndexOutOfBoundsException ex) {
                Debug.error("No files available"); //$NON-NLS-1$
            }finally{
            	try{
            		if(in[i]!=null)
            			in[i].close();
            	}catch(IOException e){
                    Debug.error("exception closing a stream"); //$NON-NLS-1$
            	}
            	try{
            		if(out[i]!=null)
            			out[i].close();
            	}catch(IOException e){
                    Debug.error("exception closing a stream"); //$NON-NLS-1$
            	}
            }
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

}