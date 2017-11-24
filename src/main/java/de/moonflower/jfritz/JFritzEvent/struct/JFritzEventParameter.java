package de.moonflower.jfritz.JFritzEvent.struct;

public class JFritzEventParameter {

	private String parameterName;

	private String parameterPlaceHolder;

	private String value;

	public JFritzEventParameter(String parameterName, String parameterPlaceHolder) {
		this(parameterName, parameterPlaceHolder, "");
	}

	public JFritzEventParameter(String parameterName, String parameterPlaceHolder, String value) {
		this.parameterName = parameterName;
		this.parameterPlaceHolder = parameterPlaceHolder;
		this.value = value;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterPlaceHolder() {
		return parameterPlaceHolder;
	}

	public void setParameterPlaceHolder(String parameterPlaceHolder) {
		this.parameterPlaceHolder = parameterPlaceHolder;
	}

	public String toString() {
		return parameterName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
