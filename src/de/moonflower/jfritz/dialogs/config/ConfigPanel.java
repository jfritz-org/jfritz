package de.moonflower.jfritz.dialogs.config;

import java.io.IOException;
import javax.swing.JPanel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;

public interface ConfigPanel {
	public abstract void loadSettings();
	public abstract void saveSettings() throws WrongPasswordException, InvalidFirmwareException, IOException;
	public abstract void cancel();
	public abstract String getPath();
	public abstract JPanel getPanel();
	public abstract String getHelpUrl();
}
