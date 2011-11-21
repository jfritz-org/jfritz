package de.moonflower.jfritz.JFritzEvent.struct;

public class JFritzEventCondition {

	private JFritzEventParameter parameter;
	private ConditionObject condition;
	private String value;

	public JFritzEventCondition(JFritzEventParameter parameter) {
		this.parameter = parameter;
		condition = new ConditionObject();
		condition.setConditionID(ConditionObject.EQUAL);
		value = "";
	}

	public JFritzEventCondition(JFritzEventParameter parameter, ConditionObject condition, String value) {
		this.parameter = parameter;
		this.condition = condition;
		this.value = value;
	}

	public ConditionObject getCondition() {
		return condition;
	}

	public void setCondition(int conditionID) {
		this.condition.setConditionID(conditionID);
	}

	public JFritzEventParameter getParameter() {
		return parameter;
	}

	public void setParameter(JFritzEventParameter parameter) {
		this.parameter = parameter;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean passCondition(String checkValue) {
		switch (condition.getConditionID()) {
		case ConditionObject.EQUAL:
			if ( checkValue.equals(value) )
				return true;
			break;
		case ConditionObject.NOT_EQUAL:
			if ( !checkValue.equals(value) )
				return true;
			break;
		case ConditionObject.CONTAINS:
			if ( checkValue.contains(value))
				return true;
			break;
		case ConditionObject.CONTAINS_NOT:
			if ( checkValue.contains(value))
				return true;
			break;
		case ConditionObject.STARTS_WITH:
			if ( checkValue.startsWith(value))
				return true;
			break;
		case ConditionObject.STARTS_NOT_WITH:
			if ( !checkValue.startsWith(value))
				return true;
			break;
		case ConditionObject.ENDS_WITH:
			if ( checkValue.endsWith(value))
				return true;
			break;
		case ConditionObject.ENDS_NOT_WITH:
			if ( !checkValue.endsWith(value))
				return true;
			break;
		case ConditionObject.GREATER_THAN:
			try {
				int valueInt = Integer.parseInt(value);
				int checkValueInt = Integer.parseInt(checkValue);
				if ( checkValueInt > valueInt)
					return true;
			} catch (NumberFormatException nfe) {
				// not a number
				if ( checkValue.length() > value.length() )
					return true;
			}
			break;
		case ConditionObject.NOT_GREATER_THAN:
			try {
				int valueInt = Integer.parseInt(value);
				int checkValueInt = Integer.parseInt(checkValue);
				if ( checkValueInt <= valueInt)
					return true;
			} catch (NumberFormatException nfe) {
				// not a number
				if ( checkValue.length() <= value.length() )
					return true;
			}
			break;
		case ConditionObject.LESS_THAN:
			try {
				int valueInt = Integer.parseInt(value);
				int checkValueInt = Integer.parseInt(checkValue);
				if ( checkValueInt < valueInt)
					return true;
			} catch (NumberFormatException nfe) {
				// not a number
				if ( checkValue.length() < value.length() )
					return true;
			}
			break;
		case ConditionObject.NOT_LESS_THAN:
			try {
				int valueInt = Integer.parseInt(value);
				int checkValueInt = Integer.parseInt(checkValue);
				if ( checkValueInt >= valueInt)
					return true;
			} catch (NumberFormatException nfe) {
				// not a number
				if ( checkValue.length() >= value.length() )
					return true;
			}
			break;
		default: return false;
		}
		return false;
	}

}