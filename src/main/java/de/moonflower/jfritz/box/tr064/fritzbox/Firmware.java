package de.moonflower.jfritz.box.tr064.fritzbox;

public class Firmware {
    private String name;
    private String firmwareVersion;

    public Firmware(String name, String firmwareVersion) {
        this.name = name;
        this.firmwareVersion = firmwareVersion;
    }

    public String getName() {
        return name;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }
}
