package de.moonflower.jfritz.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CopyFile {

    private File[] entries;
    private FileInputStream[] in;
    private FileOutputStream[] out;
    private int numberOfFiles;
    private String sourceDirectory, fileFormat;
    private Date date;
    SimpleDateFormat df = new SimpleDateFormat( "yyyy.MM.dd_HH.mm.ss" );

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

    private void createDirectory() {
        date = Calendar.getInstance().getTime();

        boolean success = (new File("backup"+File.separator + df.format( date )))
                .mkdirs();
        if (!success) {
            Debug.err("Directory creation failed");
        }
    }

    public void copy(String sourceDirectory, String fileFormat) {
        this.sourceDirectory = sourceDirectory;
        this.fileFormat = fileFormat;
        getFiles();
        createDirectory();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.msg("Found file to backup: " + entries[i].getName());
                out[i] = new FileOutputStream("backup" + File.separator + df.format( date ) + File.separator + entries[i].getName());
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
                Debug.err("No files available");
            }
        }
    }

    public void copy(String sourceDirectory, String fileFormat, String targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.fileFormat = fileFormat;
        getFiles();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.msg("Found file to backup: " + entries[i].getName());
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
                Debug.err("No files available");
            }
        }
    }
}