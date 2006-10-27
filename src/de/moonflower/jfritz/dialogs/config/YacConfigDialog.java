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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.Main;

/**
 * @author Robert Palmer
 *
 */
public class YacConfigDialog extends CallMonitorConfigDialog {

	/**
	 * This avoids compiler warnings
	 * I don't know what it's for yet
	 */
	private static final long serialVersionUID = -1652211517806640671L;

	private int exitCode = 0;

    private JButton cancelButton, okButton;

    public static final int APPROVE_OPTION = 1;

    public static final int CANCEL_OPTION = 2;

    private JTextField yacPort;

    public YacConfigDialog(JDialog parent) {
        super(parent, true);
        initDialog();
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
    }

    public void initDialog() {
        setTitle(Main.getMessage("dialog_title_yac_options")); //$NON-NLS-1$
        setSize(270, 140);
        drawDialog();
        setProperties();
    }

    private void setProperties() {
        yacPort.setText(Main.getProperty("option.yacport", "10629")); //$NON-NLS-1$,  //$NON-NLS-2$
    }

    private void storeProperties() {
        Main.setProperty("option.yacport", yacPort.getText()); //$NON-NLS-1$
    }

    public int showConfigDialog() {
//        super.show();
        super.setVisible(true);
        return exitCode;
    }

    private void drawDialog() {
        this.setModal(true);
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
                if (source == yacPort || source == okButton) {
                    //OK
                    exitCode = APPROVE_OPTION;
                    storeProperties();
                } else if (source == cancelButton) {
                    exitCode = CANCEL_OPTION;
                }
                // Close Window
                if (source == yacPort || source == okButton
                        || source == cancelButton) {
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
        JLabel label = new JLabel(Main.getMessage("yac_port")+": "); //$NON-NLS-1$
        panel.add(label, c);
        yacPort = new JTextField("", 5); //$NON-NLS-1$
        panel.add(yacPort, c);

        JPanel buttonPanel = new JPanel();
        okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
        okButton.setActionCommand("ok_pressed"); //$NON-NLS-1$
        okButton.addActionListener(actionListener);
        okButton.addKeyListener(keyListener);

        cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
        cancelButton.setActionCommand("cancel_pressed"); //$NON-NLS-1$
        cancelButton.addActionListener(actionListener);
        cancelButton.addKeyListener(keyListener);

        //set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        //set default close button (ESC)
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = 3L;

            public void actionPerformed(ActionEvent e)
            {
                 cancelButton.doClick();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
}
