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

import sun.misc.Cleaner;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.autoupdate.Logger;

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
        File[] directories = dir.listFiles(new FileFilter() {
            public boolean accept(File arg0) {
            	if (arg0.isDirectory())
            	{
	                try {
						Date date = (Date)df.parse(arg0.getName());
						backupDates.add(date);
						return true;
					} catch (ParseException e) {
						Debug.warning("Directory '"+arg0.getAbsolutePath()+"' is not a backup directory!");
					}
            	}
                return false;
            }
        });

        Collections.sort(backupDates);

        Vector<Date> deleteBackupList = new Vector<Date>(20);
        Date currentDate = Calendar.getInstance().getTime();
        for (int i=0; i<backupDates.size(); i++)
    	{
//        	Debug.debug("Found backup directory: " + backupDates.get(i).toString());
        	int diffToToday = subtractDays(currentDate, backupDates.get(i));
//        	Debug.debug("Days old: " + diffToToday);

        	for (int j=i+1; j<backupDates.size(); j++) {
//        		Debug.debug("Comparing directory " + backupDates.get(i) + " with " + backupDates.get(j));

        		int diffCompareToToday = subtractDays(currentDate, backupDates.get(j));
            	int daysDiffToOtherBackups = subtractDays(backupDates.get(j), backupDates.get(i));

//            	Debug.debug("Diff " + daysDiffToOtherBackups);
            	if (diffToToday > 93 && diffCompareToToday > 93) {
                	// Older than 3 months, keep only one copy of this month
                	if (daysDiffToOtherBackups < 31) {
                		deleteBackupList.add(backupDates.get(i));
//                		Debug.debug("Should delete old backup : " + backupDates.get(i));
                		break;
                	}
            	} else if (diffToToday > 31 && diffCompareToToday > 31)
        		{
                	// Older than 1 month, keep only one copy per week (latest one)
            		if (daysDiffToOtherBackups < 7) {
                		deleteBackupList.add(backupDates.get(i));
//            			Debug.debug("Should delete old backup2 : " + backupDates.get(i));
            			break;
            		}
        		}
        	}
    	}

        for (Date deleteDate:deleteBackupList) {
        	Debug.debug("Deleting old backup directory: " + df.format(deleteDate));
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

    	if (JFritzUtils.parseBoolean(Main.getProperty("option.keepImportantBackupsOnly")))
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
    		Debug.errDlg(Main.getMessage("backup_to_source_directory"));
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

    private static int subtractDays(Date date1, Date date2)
    {
      GregorianCalendar gc1 = new GregorianCalendar();  gc1.setTime(date1);
      GregorianCalendar gc2 = new GregorianCalendar();  gc2.setTime(date2);

      int days1 = 0;
      int days2 = 0;
      int maxYear = Math.max(gc1.get(Calendar.YEAR), gc2.get(Calendar.YEAR));

      GregorianCalendar gctmp = (GregorianCalendar) gc1.clone();
      for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++)
        {days1 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);}

      gctmp = (GregorianCalendar) gc2.clone();
      for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++)
        {days2 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);}

      days1 += gc1.get(Calendar.DAY_OF_YEAR) - 1;
      days2 += gc2.get(Calendar.DAY_OF_YEAR) - 1;

      return (days1 - days2);
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