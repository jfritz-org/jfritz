package de.moonflower.jfritz.box.tr064.fritzbox;

import de.bausdorf.avm.tr064.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DetectFritzConnection {

    private static final int DEFAULT_HTTP_PORT = 49000;
    private static final int DEFAULT_HTTPS_PORT = 49443;
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    Map<Integer, FritzBoxTR064> detectedBoxesByPort;

    private String address;
    private String password;
    private String user;

    public static void main(String[] args) {
        String address = null;
        String password = null;
        String user = null;
        if (args.length < 1 || args.length > 3) {
            System.err.println("args: <ip> '<password>' '<user>' (password and user optional)");
            System.err.println("e.g.: fritz.box");
            System.err.println("  or: fritz.box 'myPassword'");
            System.err.println("  or: fritz.box 'myPassword' 'hans'");
            System.err.println("  or: 192.168.178.1");
            System.err.println("  or: 192.168.178.1 'myPassword'");
            System.err.println("  or: 192.168.178.1 'myPassword' 'hans'");
            System.exit(1);
        } else {
            address = args[0];
            if (args.length > 1) {
                password = args[1];
            }
            if (args.length > 2) {
                user = args[2];
            }
        }

        DetectFritzConnection dfc = new DetectFritzConnection(address, user, password);
        dfc.detect();

        if (dfc.getDetectedBoxes().size() == 0) {
            System.out.println("No FritzBox detected!");
            return;
        }

        for (FritzBoxTR064 box: dfc.getDetectedBoxes().values()) {
            System.out.println("Detected FritzBox at " + box.getUri());
        }

        try {
            FritzBoxTR064 box = dfc.getBox();
            System.out.println("");
            System.out.println("Using FritzBox at " + box.getUri());
            try {
                box.connect(dfc.user, dfc.password);
                System.out.println(box.getConnectionInfo());
                printLoginMethod(box.getLoginMethod());
                printFirmware(box.getFirmware());

                dfc.detectAnonymousLogin(box);
                dfc.detectTwoFactor(box);
                dfc.detectDialPort(box);
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException | ParseException e) {
                System.err.println("Could not establish connection to FritzBox");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Could not get a valid connection!");
        }
    }

    private static void printLoginMethod(LoginMethod loginMethod) {
        if (LoginMethod.UNKNOWN.equals(loginMethod)) {
            System.out.println("Unbekannte Login-Methode");
        } else if (LoginMethod.PASSWORDLESS_WITH_BUTTON.equals(loginMethod)) {
            System.out.println("Login method: Kennwortlose Anmeldung mit zusätzlicher Bestätigung per Knopf");
        } else if (LoginMethod.PASSWORDLESS.equals(loginMethod)) {
            System.out.println("Login method: Kennwortlose Anmeldung");
        } else if (LoginMethod.PASSWORD_ONLY.equals(loginMethod)) {
            System.out.println("Login method: Anmeldung mit dem FRITZ!Box-Kennwort");
        } else if (LoginMethod.USERNAME_PASSWORD.equals(loginMethod)) {
            System.out.println("Login method: Anmeldung mit FRITZ!Box-Benutzernamen und Kennwort");
        }
    }

    private static void printFirmware(Firmware firmware) {
        System.out.println("FritzBox: " + firmware.getName());
        System.out.println("Firmware version: " + firmware.getFirmwareVersion());
    }

    public DetectFritzConnection(final String address, final String user, final String password) {
        this.address = address;
        this.password = password == null ? "" : password;
        this.user = user == null ? this.password : user;

        detectedBoxesByPort = new LinkedHashMap<>();
    }

    public void detect() {
        detectedBoxesByPort.clear();

        if (testSchemeAndPort(SCHEME_HTTPS, DEFAULT_HTTPS_PORT)) {
            detectedBoxesByPort.put(DEFAULT_HTTPS_PORT, new FritzBoxTR064(SCHEME_HTTPS, address, DEFAULT_HTTPS_PORT));
        }

        if (testSchemeAndPort(SCHEME_HTTP, DEFAULT_HTTP_PORT)) {
            detectedBoxesByPort.put(DEFAULT_HTTP_PORT, new FritzBoxTR064(SCHEME_HTTP, address, DEFAULT_HTTP_PORT));

            try {
                int securityPort = getSecurityPort(SCHEME_HTTP, DEFAULT_HTTP_PORT);
                if (testSchemeAndPort(SCHEME_HTTPS, securityPort)) {
                    if (!detectedBoxesByPort.containsKey(securityPort)) {
                        detectedBoxesByPort.put(DEFAULT_HTTP_PORT, new FritzBoxTR064(SCHEME_HTTPS, address, securityPort));
                    }
                }
            } catch (IOException e) {
                // nothing to do
            }
        }
    }

    private int getSecurityPort(String scheme, int port) throws IOException {
        try {
            FritzConnection fc = new FritzConnection(scheme, address, port, user, password);
            fc.init(null);

            Service service = fc.getService("DeviceInfo:1");
            if (service != null) {
                Action action = service.getAction("GetSecurityPort");
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        return response1.getValueAsInteger("NewSecurityPort");
                    }
                }
            }
        } catch (UnsupportedOperationException | NoSuchFieldException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | ParseException e) {
            throw new IOException("Security port not detected due to an exception", e);
        }

        throw new IOException("Security port not detected");
    }

    public Map<Integer, FritzBoxTR064> getDetectedBoxes() {
        return detectedBoxesByPort;
    }

    public FritzBoxTR064 getBox() throws Exception {
        for (FritzBoxTR064 box: detectedBoxesByPort.values()) {
            if (SCHEME_HTTPS.equals(box.getScheme())) {
                return box;
            }
        }

        for (FritzBoxTR064 box: detectedBoxesByPort.values()) {
            if (SCHEME_HTTP.equals(box.getScheme())) {
                return box;
            }
        }

        throw new Exception("No box found");
    }

    private boolean testSchemeAndPort(String scheme, int port) {
        try {
            FritzConnection fc = new FritzConnection(scheme, address, port);
            InputStream xml = fc.getXMLIS("/tr64desc.xml");
            int available = xml.available();
            if (available > 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            return false;
        }
    }

    private void detectAnonymousLogin(FritzBoxTR064 box) {
        String currentUser = "";
        String currentUserRights = "";

        try {
            Service service = box.getService("LANConfigSecurity:1");
            if (service != null) {
                Action action = service.getAction("X_AVM-DE_GetCurrentUser");
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        try {
                            currentUser = response1.getValueAsString("NewX_AVM-DE_CurrentUsername");
                        } catch (NoSuchFieldException e) {
                            System.err.println("Could not detect current user");
                        }

                        try {
                            currentUserRights = response1.getValueAsString("NewX_AVM-DE_CurrentUserRights");
                        } catch (NoSuchFieldException e) {
                            System.err.println("Could not detect current user rights");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not detect anonymous login due to exception: " + e.getMessage());
        }

        System.out.println("Current user: '" + currentUser + "'");
        System.out.println("Current user rights: '" + currentUserRights + "'");
    }

    private void detectTwoFactor(FritzBoxTR064 box) {
        boolean twoFactor = false;
        try {
            Service service = box.getService("X_AVM-DE_Auth:1");
            if (service != null) {
                Action action = service.getAction("GetInfo");
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        try {
                            twoFactor = response1.getValueAsBoolean("NewEnabled");
                        } catch (NoSuchFieldException e) {
                            System.err.println("Could not detect two factor authentication");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not detect two factor due to exception: " + e.getMessage());
        }

        if (twoFactor) {
            System.out.println("Ausführung bestimmter Einstellungen und Funktionen zusätzlich bestätigen: AKTIVIERT");
        } else {
            System.out.println("Ausführung bestimmter Einstellungen und Funktionen zusätzlich bestätigen: NICHT AKTIVIERT");
        }
    }

    private void detectDialPort(FritzBoxTR064 box) {
        String dialPort = "";
        try {
            Service service = box.getService("X_VoIP:1");
            if (service != null) {
                Action action = service.getAction("X_AVM-DE_DialGetConfig");
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        try {
                            dialPort = response1.getValueAsString("NewX_AVM-DE_PhoneName");
                        } catch (NoSuchFieldException e) {
                            System.err.println("Could not detect dial port");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not detect dial port due to exception: " + e.getMessage());
        }

        System.out.println("Nebenstelle für Wählhilfe: '" + dialPort + "'");
    }
}
