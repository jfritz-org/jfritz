package de.moonflower.jfritz.box;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;

import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;
import org.jfritz.fboxlib.exceptions.PageNotFoundException;

public interface BoxCallListInterface {

	public Vector<Call> getCallerList(Vector<IProgressListener> progressListener) throws FeatureNotSupportedByFirmware, ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException;
	public void clearCallerList() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException;

}
