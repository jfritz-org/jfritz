package de.moonflower.jfritz.box.tr064.fritzbox;

import de.bausdorf.avm.tr064.FritzConnection;
import de.bausdorf.avm.tr064.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class FritzBoxTR064 {

    private String scheme;
    private String address;
    private int port;

    private String user = "";
    private String password = "";
    private LoginMethod loginMethod;
    private Firmware firmware;

    private FritzConnection fc;

    private DetectLoginMethod dlm;
    private DetectFirmware df;

    public FritzBoxTR064(String scheme, String address, int port) {
        this.scheme = scheme;
        this.address = address;
        this.port = port;
    }

    private void setUser(String user) {
        if (user == null) {
            this.user = "";
        } else {
            this.user = user;
        }
    }

    private void setPassword(String password) {
        if (password == null) {
            this.password = "";
        } else {
            this.password = password;
            if (user == null || user.isEmpty()) {
                user = "dummyUser";
            }
        }
    }

    public void connect(String user, String password) throws Exception {
        setUser(user);
        setPassword(password);

        try {
            fc = new FritzConnection(getScheme(), getAddress(), getPort(), this.user, this.password);
            fc.init(null);
            dlm = new DetectLoginMethod();
            df = new DetectFirmware();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            fc = null;
            dlm = null;
            df = null;
            throw new Exception("Could not connect to " + getUri());
        }
    }

    public void disconnect() {
        fc = null;
        dlm = null;
    }

    public String getScheme() {
        return scheme;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getUri() {
        return scheme + "://" + address + ":" + port;
    }

    public LoginMethod getLoginMethod() throws Exception {
        if (fc == null) {
            throw new Exception("Not connected. Please call connect() first!");
        }

        if (loginMethod == null || LoginMethod.UNKNOWN.equals(loginMethod)) {
            loginMethod = dlm.detectLoginMethod(fc);
        }
        return loginMethod;
    }

    public Firmware getFirmware() throws Exception {
        if (fc == null) {
            throw new Exception("Not connected. Please call connect() first!");
        }

        if (firmware == null) {
            firmware = df.detectFritzBoxFirmware(fc);
        }
        return firmware;
    }

    public Service getService(String service) {
        return fc.getService(service);
    }

    public String getConnectionInfo() {
        return String.format("%s://%s:%d", scheme, address, port);
    }
}
