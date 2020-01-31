package de.moonflower.jfritz.box.tr064.fritzbox;

import de.bausdorf.avm.tr064.Action;
import de.bausdorf.avm.tr064.FritzConnection;
import de.bausdorf.avm.tr064.Response;
import de.bausdorf.avm.tr064.Service;

import java.io.IOException;

public class DetectLoginMethod {

    public static final String SERVICE_LAN_CONFIG_SECURITY = "LANConfigSecurity:1";
    public static final String ACTION_GET_ANONYMOUS_LOGIN = "X_AVM-DE_GetAnonymousLogin";
    public static final String PROPERTY_ANONYMOUS_LOGIN_ENABLED = "NewX_AVM-DE_AnonymousLoginEnabled";
    public static final String PROPERTY_BUTTON_LOGIN_ENABLED = "NewX_AVM-DE_ButtonLoginEnabled";
    public static final String ACTION_GET_CURRENT_USER = "X_AVM-DE_GetCurrentUser";
    public static final String PROPERTY_CURRENT_USER_NAME = "NewX_AVM-DE_CurrentUsername";

    public LoginMethod detectLoginMethod(FritzConnection fc) {
        boolean anonymousLogin = false;
        boolean buttonLogin = false;
        String user = "";

        try {
            Service service = fc.getService(SERVICE_LAN_CONFIG_SECURITY);
            if (service != null) {
                Action action = service.getAction(ACTION_GET_ANONYMOUS_LOGIN);
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        try {
                            anonymousLogin = response1.getValueAsBoolean(PROPERTY_ANONYMOUS_LOGIN_ENABLED);
                        } catch (NoSuchFieldException e) {
                            return LoginMethod.UNKNOWN;
                        }
                        try {
                            buttonLogin = response1.getValueAsBoolean(PROPERTY_BUTTON_LOGIN_ENABLED);
                        } catch (NoSuchFieldException e) {
                            return LoginMethod.UNKNOWN;
                        }

                    }
                }

                if (!buttonLogin) {
                    action = service.getAction(ACTION_GET_CURRENT_USER);
                    if (action != null) {
                        Response response1 = action.execute();
                        if (response1 != null) {
                            try {
                                user = response1.getValueAsString(PROPERTY_CURRENT_USER_NAME);
                            } catch (NoSuchFieldException e) {
                                return LoginMethod.UNKNOWN;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            return LoginMethod.UNKNOWN;
        }

        if (anonymousLogin && buttonLogin) {
            return LoginMethod.PASSWORDLESS_WITH_BUTTON;
        } else if (anonymousLogin && user.isEmpty()) {
            return LoginMethod.PASSWORDLESS;
        } else if (anonymousLogin && !user.isEmpty()) {
            return LoginMethod.PASSWORD_ONLY;
        } else {
            return LoginMethod.USERNAME_PASSWORD;
        }
    }
}
