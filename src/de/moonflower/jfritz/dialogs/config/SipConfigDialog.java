/*
 * Created on 02.10.2005
 *
 */
package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Robert Palmer
 *
 */
public class SipConfigDialog extends JDialog {

	/**
	 * This avoids compiler warnings
	 * I don't know what it's for yet
	 */
	private static final long serialVersionUID = 523526251509572465L;

	private JComboBox startDatum;

    private JCheckBox warnFreiminutenCheckBox;

    private JTextField festnetzTakt1, festnetzTakt2, festnetzKosten,
            festnetzFreiminuten, mobilTakt1, mobilTakt2, mobilKosten,
            mobilFreiminuten, warnFreiminutenTextField;

    private JButton okButton, cancelButton;

    private boolean ok_pressed = false;

    private SipProvider sipProvider;

    public SipConfigDialog(JDialog parent, SipProvider sipProvider) {
        super(parent, true);
        setTitle("Edit Sip-Provider: " + sipProvider.toString());
        this.sipProvider = sipProvider;
        drawDialog();
        setValues();
        setLocationRelativeTo(parent);
    }

    public boolean checkValues() {
        try {
            Integer.parseInt(festnetzTakt1.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Der Festnetztakt für die erste Minute wurde falsch eingegeben.");
            return false;
        }
        try {
            Integer.parseInt(festnetzTakt2.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Der Festnetztakt ab der zweiten Minute wurde falsch eingegeben.");
            return false;
        }
        try {
            festnetzKosten.setText(festnetzKosten.getText()
                    .replaceAll(",", "."));
            Double.parseDouble(festnetzKosten.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Die Festnetzkosten wurden falsch eingegeben.\nBitte in der Art 1.5 eingeben.");
            return false;
        }
        try {
            Integer.parseInt(festnetzFreiminuten.getText());
        } catch (NumberFormatException nfe) {
            Debug.errDlg("Die Festnetzfreiminuten wurden falsch eingegeben.");
            return false;
        }
        if ((Integer.parseInt(festnetzTakt1.getText()) % 60 != 0)
                || (Integer.parseInt(festnetzTakt2.getText()) % 60 != 0)) {
            Debug
                    .errDlg("Die FritzBox rechnet nur Minutengenau ab,\ndeshalb wird der von Ihnen eingegebene Takt auf die nächst höhere Minute gerundet.");
            if (Integer.parseInt(festnetzTakt1.getText()) % 60 != 0)
                festnetzTakt1.setText(Integer.toString(((Integer
                        .parseInt(festnetzTakt1.getText()) / 60) + 1) * 60));
            if (Integer.parseInt(festnetzTakt2.getText()) % 60 != 0)
                festnetzTakt2.setText(Integer.toString(((Integer
                        .parseInt(festnetzTakt2.getText()) / 60) + 1) * 60));
            return false;
        }
        try {
            Integer.parseInt(mobilTakt1.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Der Mobilfunktakt für die erste Minute wurde falsch eingegeben.");
            return false;
        }
        try {
            Integer.parseInt(mobilTakt2.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Der Mobilfunktakt ab der zweiten Minute wurde falsch eingegeben.");
            return false;
        }
        try {
            mobilKosten.setText(mobilKosten.getText().replaceAll(",", "."));
            Double.parseDouble(mobilKosten.getText());
        } catch (NumberFormatException nfe) {
            Debug
                    .errDlg("Die Mobilfunkkosten wurden falsch eingegeben.\nBitte in der Art 1.5 eingeben.");
            return false;
        }
        try {
            Integer.parseInt(mobilFreiminuten.getText());
        } catch (NumberFormatException nfe) {
            Debug.errDlg("Die Mobilfunkfreiminuten wurden falsch eingegeben.");
            return false;
        }
        if ((Integer.parseInt(mobilTakt1.getText()) % 60 != 0)
                || (Integer.parseInt(mobilTakt2.getText()) % 60 != 0)) {
            Debug
                    .errDlg("Die FritzBox rechnet nur Minutengenau ab,\ndeshalb wird der von Ihnen eingegebene Takt auf die nächst höhere Minute gerundet.");
            if (Integer.parseInt(mobilTakt1.getText()) % 60 != 0)
                mobilTakt1.setText(Integer.toString(((Integer
                        .parseInt(mobilTakt1.getText()) / 60) + 1) * 60));
            if (Integer.parseInt(mobilTakt2.getText()) % 60 != 0)
                mobilTakt2.setText(Integer.toString(((Integer
                        .parseInt(mobilTakt2.getText()) / 60) + 1) * 60));
            return false;
        }
        return true;
    }

    /**
     * Set config
     *
     */
    public void setValues() {
        startDatum.setSelectedIndex(sipProvider.getStartDate() - 1);
        festnetzTakt1.setText(Integer.toString(sipProvider.getFestnetzTakt1()));
        festnetzTakt2.setText(Integer.toString(sipProvider.getFestnetzTakt2()));
        festnetzKosten
                .setText(Double.toString(sipProvider.getFestnetzKosten()));
        festnetzFreiminuten.setText(Integer.toString(sipProvider
                .getFestnetzFreiminuten()));
        mobilTakt1.setText(Integer.toString(sipProvider.getMobileTakt1()));
        mobilTakt2.setText(Integer.toString(sipProvider.getMobileTakt2()));
        mobilKosten.setText(Double.toString(sipProvider.getMobileKosten()));
        mobilFreiminuten.setText(Integer.toString(sipProvider
                .getMobileFreiminuten()));
        if (sipProvider.getWarnFreiminuten() < 0) {
            warnFreiminutenCheckBox.setSelected(false);
            warnFreiminutenTextField.setText("0");
        } else {
            warnFreiminutenCheckBox.setSelected(true);
            warnFreiminutenTextField.setText(Integer.toString(sipProvider
                    .getWarnFreiminuten()));
        }
    }

    /**
     * Save config
     *
     */
    public void storeValues() {
        sipProvider.setStartDate(startDatum.getSelectedIndex() + 1);
        sipProvider.setFestnetzTakt1(Integer.parseInt(festnetzTakt1.getText()));
        sipProvider.setFestnetzTakt2(Integer.parseInt(festnetzTakt2.getText()));
        sipProvider.setFestnetzKosten(Double.parseDouble(festnetzKosten
                .getText()));
        sipProvider.setFestnetzFreiminuten(Integer.parseInt(festnetzFreiminuten
                .getText()));
        sipProvider.setMobileTakt1(Integer.parseInt(mobilTakt1.getText()));
        sipProvider.setMobileTakt2(Integer.parseInt(mobilTakt2.getText()));
        sipProvider.setMobileKosten(Double.parseDouble(mobilKosten.getText()));
        sipProvider.setMobileFreiminuten(Integer.parseInt(mobilFreiminuten
                .getText()));
        if (warnFreiminutenCheckBox.isSelected()) {
            sipProvider.setWarnFreiminuten(Integer
                    .parseInt(warnFreiminutenTextField.getText()));
        } else { // Keine Warnung
            sipProvider.setWarnFreiminuten(-1);
        }

    }

    public boolean showDialog() {
        ok_pressed = false;
//        super.show();
        super.setVisible(true);
        if (ok_pressed)
            return true;
        else
            return false;
    }

    protected void drawDialog() {

        // Create JTabbedPane
        JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

        tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        okButton = new JButton(JFritz.getMessage("okay"));
        okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(
                        "/de/moonflower/jfritz/resources/images/okay.png"))));
        cancelButton = new JButton(JFritz.getMessage("cancel"));

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == okButton) {
                    ok_pressed = true;
                }
                if (source == cancelButton) {
                    ok_pressed = false;
                }
                if ((source == okButton && checkValues())
                        || source == cancelButton) {
                    SipConfigDialog.this.setVisible(false);
                }
            }
        };

        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        JPanel okcancelpanel = new JPanel();
        okButton.addActionListener(actionListener);
        okcancelpanel.add(okButton, c);
        cancelButton.addActionListener(actionListener);
        cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
        okcancelpanel.add(cancelButton);

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

        tpane.addTab("Kosten", createKostenPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tpane, BorderLayout.CENTER);
        getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
        c.fill = GridBagConstraints.HORIZONTAL;

        setSize(new Dimension(480, 350));
        setResizable(false);
        // pack();
    }

    public JPanel createKostenPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        c.insets.left = 10;
        c.insets.right = 10;
        c.anchor = GridBagConstraints.WEST;

        JLabel label;

        JPanel abrechnungsPanel = new JPanel();
        label = new JLabel("Start des Abrechnungszeitraums: ");
        abrechnungsPanel.add(label);
        startDatum = new JComboBox();
        for (int i = 1; i < 32; i++) {
            startDatum.addItem(Integer.toString(i));
        }
        abrechnungsPanel.add(startDatum);

        c.gridy = 1;
        c.gridwidth = 5;
        panel.add(abrechnungsPanel, c);

        c.gridy = 2;
        c.gridwidth = 1;
        label = new JLabel(" ");
        panel.add(label, c);

        c.gridy = 3;
        c.gridx = 2;
        label = new JLabel("Taktung");
        panel.add(label, c);
        c.gridx = 3;
        label = new JLabel("Taktung");
        panel.add(label, c);
        c.gridx = 4;
        label = new JLabel("Kosten");
        panel.add(label, c);

        c.gridy = 4;
        c.gridx = 1;
        label = new JLabel(" ");
        panel.add(label, c);
        c.gridx = 2;
        label = new JLabel("1. Min");
        panel.add(label, c);
        c.gridx = 3;
        label = new JLabel("ab 2. Min");
        panel.add(label, c);
        c.gridx = 4;
        label = new JLabel("in Cent / min");
        panel.add(label, c);
        c.gridx = 5;
        label = new JLabel("Freiminuten");
        panel.add(label, c);

        c.gridy = 5;
        c.gridx = 1;
        label = new JLabel(JFritz.getMessage("fixed_network")+": ");
        panel.add(label, c);
        c.gridx = 2;
        festnetzTakt1 = new JTextField("60", 4);
        panel.add(festnetzTakt1, c);
        c.gridx = 3;
        festnetzTakt2 = new JTextField("30", 4);
        panel.add(festnetzTakt2, c);
        c.gridx = 4;
        festnetzKosten = new JTextField("1.5", 4);
        panel.add(festnetzKosten, c);
        c.gridx = 5;
        festnetzFreiminuten = new JTextField("0", 4);
        panel.add(festnetzFreiminuten, c);

        c.gridy = 6;
        c.gridx = 1;
        label = new JLabel("Mobilfunk: ");
        panel.add(label, c);
        c.gridx = 2;
        mobilTakt1 = new JTextField("60", 4);
        panel.add(mobilTakt1, c);
        c.gridx = 3;
        mobilTakt2 = new JTextField("60", 4);
        panel.add(mobilTakt2, c);
        c.gridx = 4;
        mobilKosten = new JTextField("20.5", 4);
        panel.add(mobilKosten, c);
        c.gridx = 5;
        mobilFreiminuten = new JTextField("0", 4);
        panel.add(mobilFreiminuten, c);

        JPanel warnPanel = new JPanel();

        warnFreiminutenCheckBox = new JCheckBox("Warnen bei nur noch");
        warnPanel.add(warnFreiminutenCheckBox);
        warnFreiminutenTextField = new JTextField("0", 4);
        warnPanel.add(warnFreiminutenTextField, c);
        label = new JLabel("Freiminuten");
        warnPanel.add(label, c);

        c.gridy = 7;
        c.gridx = 1;
        c.gridwidth = 5;
        panel.add(warnPanel, c);

        return panel;
    }
}
