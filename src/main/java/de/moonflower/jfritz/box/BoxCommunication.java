package de.moonflower.jfritz.box;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;
import org.jfritz.fboxlib.exceptions.PageNotFoundException;

public class BoxCommunication {
	private final Logger log;
	private static int maxCallsPerBox = 400;

	private int lastFetchedCallsCount = 0;

	Vector<BoxClass> registeredBoxes;

	Vector<CallMonitorStatusListener> callMonitorStatusListener;

	Vector<IProgressListener> callListProgressListener;

	public BoxCommunication(Logger log)
	{
		this.log = log;
		registeredBoxes = new Vector<BoxClass>(2);
		callMonitorStatusListener = new Vector<CallMonitorStatusListener>();
		callListProgressListener = new Vector<IProgressListener>();
	}

	public void addBox(BoxClass newBox)
	{
		registeredBoxes.add(newBox);
	}

	public void removeBox(BoxClass box)
	{
		registeredBoxes.remove(box);
	}

	public int getBoxCount()
	{
		return registeredBoxes.size();
	}

	public BoxClass getBox(int i)
	{
		return registeredBoxes.get(i);
	}

	public BoxClass getBox(String name)
	{
		for (int i=0; i<registeredBoxes.size(); i++) {
			BoxClass currBox = registeredBoxes.get(i);
			if (currBox.getName().equals(name)) {
				return currBox;
			}
		}
		return null;
	}

	public void refreshLogin(BoxClass box) {
		if (box != null) {
			box.refreshLogin();
		} else {
			for (BoxClass currentBox: registeredBoxes)
			{
				currentBox.refreshLogin();
			}
		}
	}
	
	public void startCallMonitor()
	{
		for (BoxClass box: registeredBoxes)
		{
			box.startCallMonitor(callMonitorStatusListener);
		}
	}

	public void stopCallMonitor()
	{
		for (BoxClass box: registeredBoxes)
		{
			box.stopCallMonitor(callMonitorStatusListener);
		}
	}

//	public Vector<Boolean> isCallMonitorRunning()
//	{
//		Vector<Boolean> isRunning = new Vector<Boolean>(registeredBoxes.size());
//
//		for (int i=0; i<registeredBoxes.size(); i++)
//		{
//			isRunning.add(registeredBoxes.get(i).isCallMonitorConnected());
//		}
//		return isRunning;
//	}

	public void getCallerList(BoxClass box)
	{
		Vector<Call> newCalls = new Vector<Call>(maxCallsPerBox * registeredBoxes.size());

		if (box != null) {
			try {
				Vector<Call> tmpCalls = box.getCallerList(callListProgressListener);
				newCalls.addAll(tmpCalls);
			} catch (FeatureNotSupportedByFirmware fns) {
				log.warn(fns.getMessage());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (LoginBlockedException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (InvalidCredentialsException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (PageNotFoundException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			}
		} else {
			for (BoxClass currentBox: registeredBoxes)
			{
				try {
					Vector<Call> tmpCalls = currentBox.getCallerList(callListProgressListener);
					newCalls.addAll(tmpCalls);
				} catch (FeatureNotSupportedByFirmware fns) {
					log.warn(fns.getMessage());
				} catch (MalformedURLException e) {
					e.printStackTrace();
					currentBox.setBoxDisconnected();
				} catch (IOException e) {
					e.printStackTrace();
					currentBox.setBoxDisconnected();
				} catch (LoginBlockedException e) {
					e.printStackTrace();
					currentBox.setBoxDisconnected();
				} catch (InvalidCredentialsException e) {
					e.printStackTrace();
					currentBox.setBoxDisconnected();
				} catch (PageNotFoundException e) {
					e.printStackTrace();
					currentBox.setBoxDisconnected();
				}
			}
		}

		for (IProgressListener listener:callListProgressListener)
		{
			listener.finished(newCalls);
		}

		lastFetchedCallsCount = newCalls.size();
	}

	public int getLastFetchedCallsCount()
	{
		return lastFetchedCallsCount;
	}

	public void clearCallerList()
	{
		for (BoxClass box:registeredBoxes)
		{
			try {
				box.clearCallerList();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (LoginBlockedException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (InvalidCredentialsException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			} catch (PageNotFoundException e) {
				e.printStackTrace();
				box.setBoxDisconnected();
			}
		}
	}

	public Vector<Port> getAvailablePorts()
	{
		Vector<Port> ports = new Vector<Port>();
		for (BoxClass box: registeredBoxes)
		{
			ports.addAll(box.getConfiguredPorts());
		}

		return ports;
	}

	public void registerCallMonitorStateListener(CallMonitorStatusListener listener)
	{
		if (!callMonitorStatusListener.contains(listener))
		{
			callMonitorStatusListener.add(listener);
		}
	}

	public void unregisterCallMonitorStateListener(CallMonitorStatusListener listener)
	{
		if (callMonitorStatusListener.contains(listener))
		{
			callMonitorStatusListener.remove(listener);
		}
	}

	public void registerCallListProgressListener(IProgressListener listener)
	{
		if (!callListProgressListener.contains(listener))
		{
			callListProgressListener.add(listener);
		}
	}

	public void unregisterCallListProgressListener(IProgressListener listener)
	{
		if (callListProgressListener.contains(listener))
		{
			callListProgressListener.remove(listener);
		}
	}

	public void registerBoxStatusListener(BoxStatusListener listener)
	{
		for (BoxClass box: registeredBoxes)
		{
			box.addBoxStatusListener(listener);
		}
	}

	public void registerBoxCallBackListener(BoxCallBackListener listener)
	{
		for (BoxClass box: registeredBoxes)
		{
			box.addBoxCallBackListener(listener);
		}
	}

	public void doCall(PhoneNumberOld number, Port port)
	{
		if (port != null)
		{
			if (port.getBox() != null)
			{
				port.getBox().doCall(number, port);
			}
		}
	}

	public void hangup(Port port)
	{
		if (port != null)
		{
			if (port.getBox() != null)
			{
				port.getBox().hangup(port);
			}
		}
	}

	public void renewIPAddress(BoxClass box) {
		if (box != null) {
			box.renewIPAddress();
		} else {
			for (BoxClass currentBox: registeredBoxes)
			{
				currentBox.renewIPAddress();
			}
		}
	}

	public void reboot(BoxClass box) throws WrongPasswordException {
		if (box != null) {
			box.reboot();
		} else {
			for (BoxClass currentBox: registeredBoxes) {
				currentBox.reboot();
			}
		}
	}
}
