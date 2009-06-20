package de.moonflower.jfritz.JFritzEvent.struct;

import javax.swing.JComboBox;

import de.moonflower.jfritz.Main;

public class ConditionObject {
	public static final int EQUAL = 0;
	public static final int NOT_EQUAL = 1;
	public static final int CONTAINS = 2;
	public static final int CONTAINS_NOT = 3;
	public static final int STARTS_WITH = 4;
	public static final int STARTS_NOT_WITH = 5;
	public static final int ENDS_WITH = 6;
	public static final int ENDS_NOT_WITH = 7;
	public static final int GREATER_THEN = 8;
	public static final int NOT_GREATER_THEN = 9;
	public static final int LESS_THEN = 10;
	public static final int NOT_LESS_THEN = 11;

	private static String[] conditionNames = {
				"equal","not_equal","contains","contains_not",
				"starts_with","starts_not_with","ends_with",
				"ends_not_with","greater_then","not_greater_then",
				"less_then","not_less_then" };

	private String conditionName;
	private int conditionID;

	public ConditionObject() {
		this.conditionID = 0;
		this.conditionName = conditionNames[0];
	}

	public ConditionObject(int id) {
		this.conditionID = id;
		this.conditionName = conditionNames[id];
	}

	public String getConditionName() {
		return Main.getMessage(conditionName);
	}

	public String getConditionPlaceHolder() {
		return conditionName;
	}

	public int getConditionID() {
		return conditionID;
	}

	public void setConditionID(int id) {
		conditionID = id;
		conditionName = conditionNames[id];
	}

	public static int getRegisteredConditionSize() {
		return conditionNames.length;
	}

	public void updateCondition(String newCondition) {

	}

	public String toString() {
		return Main.getMessage(conditionName);
	}

	public static JComboBox createComboBox() {
		JComboBox combo = new JComboBox();
		for ( int i=0; i<conditionNames.length; i++) {
			combo.addItem(new ConditionObject(i));
		}
		return combo;
	}

}