package de.moonflower.jfritz.utils.callsimulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CallSimulator extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton initiateCall, establishCall, disconnectCall;

	private JTextField fromText, toText, lineText, portText, disconnectText;

	private JComboBox callType;

	private DataOutputStream outputStream;

	private Socket clientSocket;

	private WindowCloseAdapter closeAdapter;

	public CallSimulator() {
		closeAdapter = new WindowCloseAdapter();
		this.addWindowListener(closeAdapter);
	}

	private void createGui() {
		JLabel callLabel = new JLabel("Call type:");
		callType = new JComboBox();
		callType.addItem("Incoming");
		callType.addItem("Outgoing");

		JLabel fromLabel = new JLabel("From: ");
		fromText = new JTextField(10);

		JLabel toLabel = new JLabel("To: ");
		toText = new JTextField(10);

		JLabel lineLabel = new JLabel("Line: ");
		lineText = new JTextField(10);

		initiateCall = new JButton("Initiate call");

		establishCall = new JButton("Establish call");
		JLabel portLabel = new JLabel("Port: ");
		portText = new JTextField(3);

		disconnectCall = new JButton("Disconnect call");
		JLabel disconnectLabel = new JLabel("Disconnect after [s]: ");
		disconnectText = new JTextField(3);

		JLabel saveLabel = new JLabel("Saved sessions");
		JList savedSessions = new JList();
		JButton loadButton = new JButton("Load");
		JButton saveButton = new JButton("Save");
		JTextField saveText = new JTextField(10);

		JPanel initiatePanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		initiatePanel.setLayout(gbl);
		addComponent(initiatePanel, gbl, callLabel, 0, 0, 1, 1);
		addComponent(initiatePanel, gbl, callType, 1, 0, 1, 1);
		addComponent(initiatePanel, gbl, fromLabel, 0, 1, 1, 1);
		addComponent(initiatePanel, gbl, fromText, 1, 1, 1, 1);
		addComponent(initiatePanel, gbl, toLabel, 0, 2, 1, 1);
		addComponent(initiatePanel, gbl, toText, 1, 2, 1, 1);
		addComponent(initiatePanel, gbl, lineLabel, 0, 3, 1, 1);
		addComponent(initiatePanel, gbl, lineText, 1, 3, 1, 1);
		addComponent(initiatePanel, gbl, initiateCall, 0, 4, 2, 1);

		JPanel establishPanel = new JPanel();
		GridBagLayout gbl1 = new GridBagLayout();
		establishPanel.setLayout(gbl1);
		addComponent(establishPanel, gbl1, portLabel, 0, 0, 1, 1);
		addComponent(establishPanel, gbl1, portText, 1, 0, 1, 1);
		addComponent(establishPanel, gbl1, establishCall, 0, 1, 2, 1);

		JPanel disconnectPanel = new JPanel();
		GridBagLayout gbl2 = new GridBagLayout();
		disconnectPanel.setLayout(gbl2);
		addComponent(disconnectPanel, gbl2, disconnectLabel, 0, 0, 1, 1);
		addComponent(disconnectPanel, gbl2, disconnectText, 1, 0, 1, 1);
		addComponent(disconnectPanel, gbl2, disconnectCall, 0, 1, 2, 1);

		JPanel savePanel = new JPanel();
		GridBagLayout gbl3 = new GridBagLayout();
		savePanel.setLayout(gbl3);
		addComponent(savePanel, gbl3, saveLabel, 0, 0, 1, 1);
		addComponent(savePanel, gbl3, savedSessions, 0, 1, 1, 5);
		addComponent(savePanel, gbl3, loadButton, 0, 2, 1, 1);
		addComponent(savePanel, gbl3, saveButton, 0, 3, 1, 1);
		addComponent(savePanel, gbl3, saveText, 0, 4, 1, 1);

		JPanel centerLabel = new JPanel();
		centerLabel.setLayout(new BorderLayout());
		centerLabel.add(initiatePanel, BorderLayout.NORTH);
		centerLabel.add(establishPanel, BorderLayout.CENTER);
		centerLabel.add(disconnectPanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		getContentPane().add(centerLabel, BorderLayout.WEST);
		getContentPane().add(savePanel, BorderLayout.CENTER);

		this.setBounds(0, 0, 450, 350);
	}

	public void startServer() {
		try {
			int listenPort = 1012;
			System.out.println("Listening on port " + listenPort);
			ServerSocket listenSocket = new ServerSocket(listenPort);
			int i = 0;
			while (true) {
				i++;
				clientSocket = listenSocket.accept();
				enableCallButtons(true);
				closeAdapter.setSocket(clientSocket);
				System.out.println("Connected to client " + i);
				outputStream = new DataOutputStream(clientSocket
						.getOutputStream());
			}
		} catch (IOException ioe) {
			System.err.println("Listen socket: " + ioe.getMessage());
		}
	}

	static void addComponent(Container cont, GridBagLayout gbl, Component c,
			int x, int y, int width, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.insets = new Insets(5, 10, 5, 10);
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	public void enableCallButtons(boolean status) {
		initiateCall.setEnabled(status);
		establishCall.setEnabled(status);
		disconnectCall.setEnabled(status);
	}

	public void addListener() {
		initiateCall.addActionListener(this);
		initiateCall.setActionCommand("initiateCall");
		establishCall.addActionListener(this);
		establishCall.setActionCommand("establishCall");
		disconnectCall.addActionListener(this);
		disconnectCall.setActionCommand("disconnectCall");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CallSimulator callSim = new CallSimulator();
		callSim.createGui();
		callSim.enableCallButtons(false);
		callSim.addListener();
		callSim.setVisible(true);
		callSim.startServer();
	}

	public void actionPerformed(ActionEvent action) {
		Calendar rightNow = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		String currentDate = df.format(rightNow.getTime());
		try {
			if (action.getActionCommand().equals("initiateCall")) {
				if (callType.getItemAt(callType.getSelectedIndex()).equals(
						"Incoming")) {
					outputStream.writeBytes(currentDate + ";" + "RING" + ";"
							+ "1" + ";" + fromText.getText() + ";"
							+ toText.getText() + ";" + lineText.getText() + ";"
							+ "\n");
				} else {
					outputStream.writeBytes(currentDate + ";" + "CALL" + ";"
							+ "1" + ";" + portText.getText() + ";"
							+ fromText.getText() + ";" + toText.getText() + ";"
							+ lineText.getText() + ";" + "\n");
				}
			}
			if (action.getActionCommand().equals("establishCall")) {
				if (callType.getItemAt(callType.getSelectedIndex()).equals(
						"Incoming")) {
					outputStream.writeBytes(currentDate + ";" + "CONNECT" + ";"
							+ "1" + ";" + portText.getText() + ";"
							+ fromText.getText() + ";" + "\n");
				} else {
					outputStream.writeBytes(currentDate + ";" + "CONNECT" + ";"
							+ "1" + ";" + portText.getText() + ";"
							+ toText.getText() + ";" + "\n");
				}
			}
			if (action.getActionCommand().equals("disconnectCall")) {
				outputStream.writeBytes(currentDate + ";" + "DISCONNECT" + ";"
						+ "1" + ";" + disconnectText.getText() + ";" + "\n");
			}
		} catch (IOException ioe) {
			System.err.println("Could not send message: " + ioe.getMessage());
		} finally {
		}
	}
}

class WindowCloseAdapter extends WindowAdapter {
	private Socket clientSocket = null;

	private void closeSocket() {
		try {
			if (clientSocket != null)
			{
				clientSocket.close();
			}
		} catch (IOException ioe) {
			System.err.println("Could not close socket: " + ioe.getMessage());
		}
	}

	public void setSocket(Socket socket) {
		if (clientSocket != null) {
			closeSocket();
		}
		clientSocket = socket;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		closeSocket();
		System.exit(0);
	}
}

class Connection extends Thread {
	DataOutputStream out;
	Socket clientSocket;

	public Connection(Socket socket) {
		try {
			clientSocket = socket;
			out = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		} catch (IOException ioe) {
			System.err.println("Connection: " + ioe.getMessage());
		}
	}

	public void run() {
		try {
			System.out.println("Sending call ...");
			out.writeUTF("Test");
		} catch (IOException ioe) {
			System.err.println("Could not send message: " + ioe.getMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException ioe) {
				System.err.println("Could not close socket: "
						+ ioe.getMessage());
			}
		}
	}
}