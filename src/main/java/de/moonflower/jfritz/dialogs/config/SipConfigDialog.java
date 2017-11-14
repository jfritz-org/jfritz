/*
 * Created on 02.10.2005
 *
 */
package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.messages.MessageProvider;

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

    private JButton okButton, cancelButton;

    private boolean pressedOk = false;
	protected MessageProvider messages = MessageProvider.getInstance();

    public SipConfigDialog(JDialog parent, SipProvider sipProvider) {
        super(parent, true);
        setTitle("Edit Sip-Provider: " + sipProvider.toString());
        drawDialog();
        setLocationRelativeTo(parent);
    }

    public boolean showDialog() {
        pressedOk = false;
//        super.show();
        super.setVisible(true);
        if (pressedOk)
            return true;
        else
            return false;
    }

    protected void drawDialog() {

        // Create JTabbedPane
        JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

        tpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        okButton = new JButton(messages.getMessage("okay"));
        okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                getClass().getClassLoader().getResource("images/okay.png"))));
        cancelButton = new JButton(messages.getMessage("cancel"));

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == okButton) {
                    pressedOk = true;
                }
                if (source == cancelButton) {
                    pressedOk = false;
                }
                if ((source == okButton)
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

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tpane, BorderLayout.CENTER);
        getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
        c.fill = GridBagConstraints.HORIZONTAL;

        setSize(new Dimension(480, 350));
        setResizable(false);
        // pack();
    }
}
