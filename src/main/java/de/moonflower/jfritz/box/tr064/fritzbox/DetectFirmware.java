package de.moonflower.jfritz.box.tr064.fritzbox;

import de.bausdorf.avm.tr064.Action;
import de.bausdorf.avm.tr064.FritzConnection;
import de.bausdorf.avm.tr064.Response;
import de.bausdorf.avm.tr064.Service;

import java.io.IOException;

public class DetectFirmware {

    public Firmware detectFritzBoxFirmware(FritzConnection fc) {
        String fritzBoxName = "";
        String firmwareVersion = "";

        try {
            Service service = fc.getService("DeviceInfo:1");
            if (service != null) {
                Action action = service.getAction("GetInfo");
                if (action != null) {
                    Response response1 = action.execute();
                    if (response1 != null) {
                        try {
                            fritzBoxName = response1.getValueAsString("NewModelName");
                        } catch (NoSuchFieldException e) {
                            fritzBoxName = "Unsupported";
                        }
                        try {
                            firmwareVersion = response1.getValueAsString("NewSoftwareVersion");
                        } catch (NoSuchFieldException e) {
                            firmwareVersion = "0.0.0";
                        }
                    }
                }
            }
        } catch (IOException e) {
            fritzBoxName = "Communication error";
            firmwareVersion = "0.0.0";
        }

        return new Firmware(fritzBoxName, firmwareVersion);
    }
}
