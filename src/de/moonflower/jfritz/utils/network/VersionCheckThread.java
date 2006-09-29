package de.moonflower.jfritz.utils.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * This thread processes the current version listed on www.jfritz.org If a newer
 * version is listed on the site then the user is presented with a download link
 * for the newer version
 *
 *
 * @author Bastian Schaefer
 *
 */
public class VersionCheckThread implements Runnable {

    private String newJFritzVersion = "";

    private List filesToDownload = new LinkedList();

    private boolean informNoNewVersion;

    public VersionCheckThread(boolean informNoNewVersion) {
        this.informNoNewVersion = informNoNewVersion;
    }

    public void run() {
        if (checkForNewVersion()) {
            Object[] options = { JFritz.getMessage("yes"),
                    JFritz.getMessage("no") };
            int ok = JOptionPane.showOptionDialog(JFritz.getJframe(), JFritz
                    .getMessage("new_version_text"), JFritz
                    .getMessage("new_version"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, // don't use a
                    // custom Icon
                    options, // the titles of buttons
                    options[0]); // default button title
            if (ok == JOptionPane.YES_OPTION) {
                for ( int i=0; i < filesToDownload.size(); i++ )
                    downloadFile((String) filesToDownload.get(i));

                // Ask for restarting JFritz
                ok = JOptionPane.showOptionDialog(JFritz.getJframe(), JFritz
                        .getMessage("new_version_restart"), JFritz
                        .getMessage("new_version"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, // don't use a
                        // custom Icon
                        options, // the titles of buttons
                        options[0]); // default button title
                if (ok == JOptionPane.YES_OPTION) {
                	;
                }
            }
        } else if (informNoNewVersion == true) {
            JOptionPane.showMessageDialog(JFritz.getJframe(), JFritz
                    .getMessage("no_new_version_found"), "JFritz",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        Debug.msg("CheckVersionThread: CheckVersionThread done");
    }

    public boolean checkForNewVersion() {
        URL url = null;

        String urlstr = "http://www.jfritz.org/update/current.txt"; //$NON-NLS-1$

        boolean newVersion = false;

        try {
            url = new URL(urlstr);
            if (url != null) {

                URLConnection con;
                try {
                    con = url.openConnection();
                    BufferedReader d = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String str = ""; //$NON-NLS-1$

                    // Get new version
                    str = d.readLine();
                    Debug.msg("CheckVersionThread: Checking for new JFritz-Version...");
                    if (Integer.valueOf(str.replaceAll("\\.", "")).compareTo(
                            Integer.valueOf(JFritz.PROGRAM_VERSION.replaceAll(
                                    "\\.", ""))) > 0) {
                        newJFritzVersion = str;
                        newVersion = true;
                    }

                    // Füge Dateien zur filesToDownload-Liste hinzu
                    while (null != (str = d.readLine() )) {
                        filesToDownload.add(str);
                    }

                    d.close();

                } catch (IOException e1) {
                    Debug
                            .err("CheckVersionThread: Error while retrieving " + urlstr + " (possibly no connection to the internet)"); //$NON-NLS-1$
                }
            }
        } catch (MalformedURLException e) {
            Debug.err("CheckVersionThread: URL invalid: " + urlstr); //$NON-NLS-1$
        }
        return newVersion;

    }

    private void downloadFile(String fileName) {
        URL url = null;
        String jfritzHomedir = JFritzUtils.getFullPath(".update");
        String outputPath = jfritzHomedir.substring(0, jfritzHomedir.length()-7);

        String urlstr = "http://www.jfritz.org/update/" + newJFritzVersion + "/" + fileName;
            try
            {
                Debug.msg("CheckVersionThread: Download new file from " + urlstr);
                url = new URL(urlstr);
                URLConnection conn = url.openConnection();

                BufferedInputStream in = new BufferedInputStream(conn
                        .getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(outputPath + JFritzUtils.FILESEP + fileName));

                int i = in.read();
                while( i!=-1 )
                {
                    out.write(i);
                    i=in.read();
                }
                in.close();
                out.flush();
                out.close();
                Debug.msg("CheckVersionThread: Saved file " + fileName + " to " + outputPath + JFritzUtils.FILESEP + fileName);
            } catch (Exception e)
            {
                Debug.err("CheckVersionThread: Error ("+e.toString()+")");
            }
        }

}
