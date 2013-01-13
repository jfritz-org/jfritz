package de.moonflower.jfritz.exceptions;

public class FeatureNotSupportedByFirmware extends Exception {
	private static final long serialVersionUID = 1;

	private String feature = "";

    public FeatureNotSupportedByFirmware(final String feature, final String errorMessage) {
        super(errorMessage);
        this.feature = feature;
    }

    public String getFeature()
    {
    	return feature;
    }
}
