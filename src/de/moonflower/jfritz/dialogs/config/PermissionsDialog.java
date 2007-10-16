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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.network.Login;

/**
 * This dialog is used for setting the permission of a client connections
 *
 * @author brian
 *
 */
public class PermissionsDialog extends JDialog {

	public static final long serialVersionUID = 100;

	private Login login;

	private JDialog parent;

	private int exitCode = 0;

    private JButton cancelButton, okButton;

    public static final int APPROVE_OPTION = 1;

    public static final int CANCEL_OPTION = 2;


	private JCheckBox allowCallList, allowCallListAdd, allowCallListUpdate, allowCallListRemove,
		allowPhoneBook, allowPhoneBookAdd, allowPhoneBookUpdate, allowPhoneBookRemove,
		allowCallMonitor, allowLookup, allowGetCallList;

	public PermissionsDialog(JDialog parent, Login login){
		super(parent, true);

		this.parent = parent;
		this.login = login;
		init();

		setLocationRelativeTo(parent);

	}

	public void init(){
		setTitle(Main.getMessage("set_client_permissions"));
	      setSize(320, 430);

	      drawDialog();
	      setProperties();
	}

	public void showConfigDialog() {
		super.setVisible(true);
	}

	public void drawDialog(){
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
				}else if(source == allowCallList && !allowCallList.isSelected()){
					allowCallListAdd.setSelected(false);
					allowCallListRemove.setSelected(false);
					allowCallListUpdate.setSelected(false);
				}else if(source == allowPhoneBook && !allowPhoneBook.isSelected()){
					allowPhoneBookAdd.setSelected(false);
					allowPhoneBookRemove.setSelected(false);
					allowPhoneBookUpdate.setSelected(false);
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
        allowCallList = new JCheckBox(Main.getMessage("allow_client_calllist"));
        allowCallList.addActionListener(actionListener);
        panel.add(allowCallList, c);

	    c.gridy = 1;
        allowCallListAdd = new JCheckBox(Main.getMessage("allow_client_add_calllist"));
        panel.add(allowCallListAdd, c);

        c.gridy = 2;
        allowCallListRemove = new JCheckBox(Main.getMessage("allow_client_remove_calllist"));
        panel.add(allowCallListRemove, c);

        c.gridy = 3;
        allowCallListUpdate = new JCheckBox(Main.getMessage("allow_client_update_calllist"));
        panel.add(allowCallListUpdate, c);

        c.gridy = 4;
        allowPhoneBook= new JCheckBox(Main.getMessage("allow_client_phonebook"));
        allowPhoneBook.addActionListener(actionListener);
        panel.add(allowPhoneBook, c);

        c.gridy = 5;
        allowPhoneBookAdd = new JCheckBox(Main.getMessage("allow_client_add_phonebook"));
        panel.add(allowPhoneBookAdd, c);

        c.gridy = 6;
        allowPhoneBookRemove = new JCheckBox(Main.getMessage("allow_client_remove_phonebook"));
        panel.add(allowPhoneBookRemove, c);

        c.gridy = 7;
        allowPhoneBookUpdate = new JCheckBox(Main.getMessage("allow_client_update_phoneBook"));
        panel.add(allowPhoneBookUpdate, c);

        c.gridy = 8;
        allowCallMonitor = new JCheckBox(Main.getMessage("allow_client_callmonitor"));
        panel.add(allowCallMonitor, c);

        c.gridy = 9;
        allowLookup = new JCheckBox(Main.getMessage("allow_client_lookup"));
        panel.add(allowLookup, c);

        c.gridy = 10;
        allowGetCallList = new JCheckBox(Main.getMessage("allow_client_getcalllist"));
        panel.add(allowGetCallList, c);

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

		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border compound = BorderFactory.createCompoundBorder(
				lowerEtched, new EmptyBorder(10,10,15,10));
		panel.setBorder(compound);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	}

	public void setProperties(){
		allowCallList.setSelected(login.allowCallList);
		allowCallListAdd.setSelected(login.allowAddList);
		allowCallListUpdate.setSelected(login.allowUpdateList);
		allowCallListRemove.setSelected(login.allowRemoveList);
		allowPhoneBook.setSelected(login.allowPhoneBook);
		allowPhoneBookAdd.setSelected(login.allowAddBook);
		allowPhoneBookUpdate.setSelected(login.allowUpdateBook);
		allowPhoneBookRemove.setSelected(login.allowRemoveBook);
		allowCallMonitor.setSelected(login.allowCallMonitor);
		allowLookup.setSelected(login.allowLookup);
		allowGetCallList.setSelected(login.allowGetList);
	}

	public void storeProperties(){
		login.allowCallList = allowCallList.isSelected();
		login.allowAddList = allowCallListAdd.isSelected();
		login.allowUpdateList = allowCallListUpdate.isSelected();
		login.allowRemoveList = allowCallListRemove.isSelected();
		login.allowPhoneBook = allowPhoneBook.isSelected();
		login.allowAddBook = allowPhoneBookAdd.isSelected();
		login.allowUpdateBook = allowPhoneBookUpdate.isSelected();
		login.allowRemoveBook = allowPhoneBookRemove.isSelected();
		login.allowCallMonitor = allowCallMonitor.isSelected();
		login.allowLookup = allowLookup.isSelected();
		login.allowGetList = allowGetCallList.isSelected();
	}

}
