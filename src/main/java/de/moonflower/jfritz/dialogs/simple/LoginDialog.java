package de.moonflower.jfritz.dialogs.simple;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.config.ConfigPanelFritzBoxLogin;
import de.moonflower.jfritz.messages.MessageProvider;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;

public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 5673745860432052255L;
	
	protected MessageProvider messages = MessageProvider.getInstance();
	private boolean okPressed = false;
	private FritzBox fritzBox = null;
	private Exception exception = null;
	
	public LoginDialog(FritzBox fritzBox) {
		super();
		this.fritzBox = fritzBox;
		setModal(true);
		setTitle(messages.getMessage("config.login"));
	}
	
	public void setException(Exception e) {
		this.exception = e;
	}
	
	@Override
	public void setVisible(boolean visibility) {
		if (visibility) {
			final ConfigPanelFritzBoxLogin loginPane = new ConfigPanelFritzBoxLogin();
			loginPane.setFritzBox(fritzBox);
			loginPane.loadSettings();
			loginPane.updateGui();
			if (exception != null) {
				if (exception instanceof InvalidCredentialsException) {
					loginPane.handleInvalidCredentialsException((InvalidCredentialsException)exception);
				} else if (exception instanceof LoginBlockedException) {
					loginPane.handleLoginBlockedException((LoginBlockedException)exception);
				}
			}

			ActionListener actionListener = new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e != null)
					{
						if ("ok".equals(e.getActionCommand())) {
							okPressed = true;
							loginPane.saveSettings(false);
							dispose();
						} else if ("cancel".equals(e.getActionCommand())) {
							dispose();
						}
					}
				}
				
			};
			
			JPanel mainPanel = initGui(loginPane, actionListener);
			setContentPane(mainPanel);
			pack();
		}
		super.setVisible(visibility);
	}
	
	private JPanel initGui(JPanel loginPane, ActionListener actionListener) {
		int gridPosY = 0;
		JPanel cPane = new JPanel();

		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;

		c.gridy = gridPosY++;
		cPane.add(loginPane, c);
		
		c.insets.top = 10;
		c.insets.bottom = 10;
		c.insets.left = 25;
		c.insets.right = 25;
		
		c.gridy = gridPosY++;
		c.gridwidth = 1;
		
		JButton okButton = new JButton(messages.getMessage("okay"));
		okButton.setActionCommand("ok");
		okButton.addActionListener(actionListener);
		cPane.add(okButton, c);

		JButton cancelButton = new JButton(messages.getMessage("cancel"));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(actionListener);
		cPane.add(cancelButton, c);
		
		return cPane;
	}
	
	public boolean hasOkBeenPressed() {
		return okPressed;
	}
}
