package de.moonflower.jfritz.JFritzEvent.struct;

import java.util.Iterator;
import java.util.LinkedList;

import de.moonflower.jfritz.JFritzEvent.actions.JFritzAction;
import de.moonflower.jfritz.JFritzEvent.events.JFritzEvent;

public class JFritzEventAction implements Cloneable {

	public static final int NONE = 0;
	public static final int AND = 1;
	public static final int OR = 2;

	private String description = "";

	private JFritzEvent event;

	private boolean active = true;

	private boolean visible = true;

	private LinkedList<JFritzAction> actionList;

	private LinkedList<JFritzEventCondition> eventConditionList;

	private int conditionConcatenation;

	public JFritzEventAction(String description) {
		this.description = description;
		this.event = null;
		actionList = new LinkedList<JFritzAction>();
		eventConditionList = new LinkedList<JFritzEventCondition>();
	}

	public JFritzEventAction(String description, JFritzEvent event) {
		this.description = description;
		this.event = event;
		actionList = new LinkedList<JFritzAction>();
		eventConditionList = new LinkedList<JFritzEventCondition>();
	}

	public void setEvent(JFritzEvent event) {
		this.event = event;
	}

	public JFritzEvent getEvent() {
		return event;
	}

	public void setDescription(String name) {
		this.description = name;
	}

	public String getDescription() {
		return description;
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
	}

	public boolean isActive() {
		return active;
	}

	public void setAction(int i, JFritzAction action) {
		actionList.set(i, action);
	}

	public void addAction(int i, JFritzAction action) {
		actionList.add(i, action);
	}

	public void addAction(JFritzAction action) {
		actionList.add(action);
	}

	public void removeAction(int i) {
		actionList.remove(i);
	}

	public void removeAction(JFritzAction action) {
		actionList.remove(action);
	}

	public JFritzAction getAction(int i) {
		return actionList.get(i);
	}

	public int getActionListSize() {
		return actionList.size();
	}

	public void addCondition(int i, JFritzEventCondition cond) {
		eventConditionList.add(i, cond);
	}

	public void addCondition(JFritzEventCondition cond) {
		eventConditionList.add(cond);
	}

	public void removeCondition(int i) {
		eventConditionList.remove(i);
	}

	public void removeCondition(JFritzEventCondition cond) {
		eventConditionList.remove(cond);
	}

	public JFritzEventCondition getCondition(int id) {
		return eventConditionList.get(id);
	}

	public int getConditionListSize() {
		return eventConditionList.size();
	}

	public void throwEvent() {
		Iterator<JFritzEventCondition> eventIterator = eventConditionList.iterator();
		boolean conditionSatisfied = false;
		while ( !conditionSatisfied && eventIterator.hasNext() ) {
			JFritzEventCondition condition = eventIterator.next();
			JFritzEventParameter parameter = condition.getParameter();
			boolean passCondition = condition.passCondition(parameter.getValue());

			switch (conditionConcatenation) {
				case NONE: conditionSatisfied = true;
				case AND: conditionSatisfied = conditionSatisfied && passCondition; break;
				case OR: conditionSatisfied = conditionSatisfied && passCondition; break;
			}
		}
		if ( conditionSatisfied ) {
			Iterator<JFritzAction> actionIterator = actionList.iterator();
			while ( actionIterator.hasNext() ) {
				JFritzAction action = actionIterator.next();
				action.run();
			}
		}
	}

	public void setConditionConcatenation(int conditionConcatenation) {
		this.conditionConcatenation = conditionConcatenation;
	}

	public int getConditionConcatenation() {
		return conditionConcatenation;
	}

    public JFritzEventAction clone() {
        try {
            return (JFritzEventAction) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();

            return null;
        }
    }

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}