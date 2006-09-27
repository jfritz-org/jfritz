package de.moonflower.jfritz.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
                in[i] = new FileInputStream(entries[i].getName());
            } catch (IOException ex) {
                Debug.err(ex.toString());
            }
        }

    }

    /**
     * creates a directory-structure in the following format: backup/yyyy.MM.dd_HH.mm.ss
     *
     */
    private void createDirectory() {
        date = Calendar.getInstance().getTime();

        boolean success = (new File("backup"+File.separator + df.format( date ))) //$NON-NLS-1$
                .mkdirs();
        if (!success) {
            Debug.err("Directory creation failed"); //$NON-NLS-1$
        }
    }

    /**
     * copies all files, which were fetched with getFiles() to the just
     * created folder by method createDirectory()
     *
     */
    public void copy(String sourceDirectory, String fileFormat) {
        this.sourceDirectory = sourceDirectory;
        this.fileFormat = fileFormat;
        getFiles();
        createDirectory();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.msg("Found file to backup: " + entries[i].getName()); //$NON-NLS-1$
                out[i] = new FileOutputStream("backup" //$NON-NLS-1$
                		+ File.separator + df.format( date ) + File.separator + entries[i].getName());
                byte[] buf = new byte[4096];
                int len;
                while ((len = in[i].read(buf)) > 0) {
                    out[i].write(buf, 0, len);
                }
                in[i].close();
                out[i].close();
            } catch (IOException ex) {
                Debug.err(ex.toString());
            } catch (ArrayIndexOutOfBoundsException ex) {
                Debug.err("No files available"); //$NON-NLS-1$
            }finally{
            	try{
            		if(in[i]!=null)
            			in[i].close();
            	}catch(IOException e){
                    Debug.err("exception closing a stream"); //$NON-NLS-1$
            	}
            	try{
            		if(out[i]!=null)
            			out[i].close();
            	}catch(IOException e){
                    Debug.err("exception closing a stream"); //$NON-NLS-1$
            	}
            }

        }
    }

    /**
     * copies all  files, which were fetched with getFiles() to the
     * parametric passed folder "targetDirectory"
     *
     */
    public void copy(String sourceDirectory, String fileFormat, String targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.fileFormat = fileFormat;
        getFiles();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.msg("Found file to backup: " + entries[i].getName()); //$NON-NLS-1$
                out[i] = new FileOutputStream( targetDirectory + File.separator + entries[i].getName());
                byte[] buf = new byte[4096];
                int len;
                while ((len = in[i].read(buf)) > 0) {
                    out[i].write(buf, 0, len);
                }
                in[i].close();
                out[i].close();
            } catch (IOException ex) {
                Debug.err(ex.toString());
            } catch (ArrayIndexOutOfBoundsException ex) {
                Debug.err("No files available"); //$NON-NLS-1$
            }finally{
            	try{
            		if(in[i]!=null)
            			in[i].close();
            	}catch(IOException e){
                    Debug.err("exception closing a stream"); //$NON-NLS-1$
            	}
            	try{
            		if(out[i]!=null)
            			out[i].close();
            	}catch(IOException e){
                    Debug.err("exception closing a stream"); //$NON-NLS-1$
            	}
            }
        }
    }
}