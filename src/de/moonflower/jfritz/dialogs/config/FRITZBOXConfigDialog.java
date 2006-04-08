/*
 * Created on 09.09.2005
 *
 */
package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Robert Palmer
 * This class is the config dialog for the call monitor and not for
 * Jfritz!
 *
 */
public class FRITZBOXConfigDialog extends JDialog implements
        CallMonitorConfigDialog {

    /**
     * This avoids compiler warnings I don't know what it's for yet
     */
    private static final long serialVersionUID = -8662130877265779872L;

    private int exitCode = 0;

    private JButton cancelButton, okButton;

    public static final int APPROVE_OPTION = 1;

    public static final int CANCEL_OPTION = 2;

    private JCheckBox monitorIncomingCalls, monitorOutgoingCalls,
                      fetchAfterDisconnect;

    private JTextField ignoreMSN;

    public FRITZBOXConfigDialog(JDialog parent, JFritz jfritz) {
        super(parent, true);
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
        // this.jfritz = jfritz;
        initDialog();
    }

    public void initDialog() {
        setTitle(JFritz.getMessage("monitor_settings"));
        setSize(270, 240);
        drawDialog();
        setProperties();
    }

    private void setProperties() {
        monitorIncomingCalls.setSelected(JFritzUtils
                .parseBoolean(JFritz.getProperty(
                        "option.callmonitor.monitorIncomingCalls", "true")));
        monitorOutgoingCalls.setSelected(JFritzUtils
                .parseBoolean(JFritz.getProperty(
                        "option.callmonitor.monitorOutgoingCalls", "false")));
        fetchAfterDisconnect.setSelected(JFritzUtils
                .parseBoolean(JFritz.getProperty(
                        "option.callmonitor.fetchAfterDisconnect", "false")));
        ignoreMSN.setText(JFritz.getProperty("option.callmonitor.ignoreMSN",""));
    }

    private void storeProperties() {
        JFritz.setProperty("option.callmonitor.monitorIncomingCalls", Boolean
                .toString(monitorIncomingCalls.isSelected()));
        JFritz.setProperty("option.callmonitor.monitorOutgoingCalls", Boolean
                .toString(monitorOutgoingCalls.isSelected()));
        JFritz.setProperty("option.callmonitor.fetchAfterDisconnect", Boolean.toString(fetchAfterDisconnect.isSelected()));
        JFritz.setProperty("option.callmonitor.ignoreMSN", ignoreMSN.getText());
    }

    public int showConfigDialog() {
        // super.show();
        super.setVisible(true);
        return exitCode;
    }

    private void drawDialog() {
        KeyListener keyListener = (new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // Cancel
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                        || (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    exitCode = CANCEL_OPTION;
                    setVisible(false);
                }
                // OK
                if (e.getSource() == okButton
                        && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    storeProperties();
                    exitCode = APPROVE_OPTION;
                    setVisible(false);
                }
            }
        });
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == okButton) {
                    // OK
                    exitCode = APPROVE_OPTION;
                    storeProperties();
                } else if (source == cancelButton) {
                    exitCode = CANCEL_OPTION;
                }
                // Close Window
                if (source == okButton || source == cancelButton) {
                    setVisible(false);
                }
            }
        };

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        c.anchor = GridBagConstraints.WEST;

        c.gridwidth = 1;
        c.gridy = 0;
        monitorIncomingCalls = new JCheckBox(
        		JFritz.getMessage("monitor_incoming_calls"));
        panel.add(monitorIncomingCalls, c);
        c.gridy = 1;
        monitorOutgoingCalls = new JCheckBox("" +
        		JFritz.getMessage("monitor_outgoing_calls"));
        panel.add(monitorOutgoingCalls, c);
        c.gridy = 2;
        fetchAfterDisconnect = new JCheckBox(
        		JFritz.getMessage("monitor_fetch_disconnect"));
        panel.add(fetchAfterDisconnect, c);
        c.gridy = 3;
        JLabel label = new JLabel(
        		JFritz.getMessage("monitor_ignore_msns"));
        panel.add(label, c);
        c.gridy = 4;
        ignoreMSN = new JTextField("", 20);
        panel.add(ignoreMSN, c);

        JPanel buttonPanel = new JPanel();
        okButton = new JButton(JFritz.getMessage("okay"));
        okButton.setActionCommand("ok_pressed");
        okButton.addActionListener(actionListener);
        okButton.addKeyListener(keyListener);

        cancelButton = new JButton(JFritz.getMessage("cancel"));
        cancelButton.setActionCommand("cancel_pressed");
        cancelButton.addActionListener(actionListener);
        cancelButton.addKeyListener(keyListener);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
}
