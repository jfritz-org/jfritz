package de.moonflower.jfritz.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CopyFile {

    private File[] entries;
    private FileInputStream[] in;
    private FileOutputStream[] out;
    private GregorianCalendar cal = new GregorianCalendar();
    private int numberOfFiles;
    private String directory, fileFormat, time, date, month, year;

    private void getFiles() {
        File dir = new File(directory);
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
        time = cal.get(Calendar.HOUR) + "." + cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.MINUTE) < 10) {
            time = cal.get(Calendar.HOUR) + ".0" + cal.get(Calendar.MINUTE);
        }
        if (cal.get(Calendar.AM_PM) == 0) {
            time = time + "_AM";
        } else {
            time = time + "_PM";
        }
        date = cal.get(Calendar.DATE) + ".";
        month = cal.get(Calendar.MONTH) + ".";
        year = cal.get(Calendar.YEAR) + "_";

        boolean success = (new File("backup"+File.separator+ date + month + year + time))
                .mkdirs();
        if (!success) {
            Debug.err("Directory creation failed");
        }
    }

    public void copy(String directory, String fileFormat) {
        this.directory = directory;
        this.fileFormat = fileFormat;
        getFiles();
        createDirectory();
        out = new FileOutputStream[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                Debug.msg("Found file to backup: " + entries[i].getName());
                out[i] = new FileOutputStream("backup" + File.separator + date + month + year
                        + time + File.separator + entries[i].getName());
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