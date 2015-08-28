package de.moonflower.jfritz.JFritzEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.moonflower.jfritz.JFritzEvent.actions.JFritzAction;
import de.moonflower.jfritz.JFritzEvent.events.JFritzEvent;
import de.moonflower.jfritz.JFritzEvent.events.MessageEvent;
import de.moonflower.jfritz.JFritzEvent.struct.ConditionObject;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventCondition;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventParameter;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class JFritzEventDispatcher extends Thread {
	private final static Logger log = Logger.getLogger(JFritzEventDispatcher.class);

	private static String EVENT_MANAGMENT_FILE_NAME = "jfritz.events.xml";

	@SuppressWarnings("unused")
	private static PriorityQueue<JFritzEvent> eventQueue = new PriorityQueue<JFritzEvent>();

	private static LinkedList<JFritzEventAction> eventList = new LinkedList<JFritzEventAction>();

	private static LinkedList<JFritzEvent> registeredEvents = new LinkedList<JFritzEvent>();

	private static LinkedList<JFritzAction> registeredActions = new LinkedList<JFritzAction>();

	public static void createDummyData() {
		eventList.add(new JFritzEventAction("Test", new MessageEvent(
				"Testnachricht")));
	}

	public static void registerEventType(JFritzEvent c) {
		registeredEvents.add(c);
	}

	public static void registerActionType(JFritzAction c) {
		registeredActions.add(c);
	}

	public static LinkedList<JFritzAction> getRegisteredActionTypes() {
		return registeredActions;
	}

	public static void addEvent(JFritzEventAction event) {
		eventList.add(event);
	}

	public static void removeEvent(JFritzEventAction event) {
		eventList.remove(event);
	}

	public static void clearEventList() {
		for ( int i=0; i<eventList.size(); i++)
		{
			if ( eventList.get(i).isVisible() )
				eventList.remove(i);
		}
	}

	public static int getEventCount() {
		return eventList.size();
	}

	public static JFritzEventAction getEvent(int i) {
		return eventList.get(i);
	}

	public void run() {
		// TODO In Schleife EventQueue abarbeiten
	}

	public static JComboBox createEventComboBox() {
		JComboBox eventComboBox = new JComboBox();
		for (int i = 0; i < registeredEvents.size(); i++) {
			eventComboBox.addItem(registeredEvents.get(i));
		}
		return eventComboBox;
	}

	public static JComboBox createActionComboBox() {
		JComboBox actionComboBox = new JComboBox();
		for (int i = 0; i < registeredActions.size(); i++) {
			actionComboBox.addItem(registeredActions.get(i).clone());
		}
		log.debug(actionComboBox.toString());
		return actionComboBox;
	}

	public static JFritzEventAction createNewEventAction() {
		JFritzEvent event = registeredEvents.getFirst();
		JFritzEventAction newEventAction = new JFritzEventAction(
				"New event handler", event);
		newEventAction.addCondition(createNewCondition(event));
		newEventAction.addAction(createNewAction());
		return newEventAction;
	}

	public static JFritzEventCondition createNewCondition(JFritzEvent event) {
		return new JFritzEventCondition(event.getParameter((byte) 0));
	}

	public static JFritzAction createNewAction() {
		return registeredActions.getFirst().clone();
	}

	public static void loadFromXML() {
		String filename = EVENT_MANAGMENT_FILE_NAME;
	    try {
			SAXBuilder builder = new SAXBuilder();
	    	Document doc = builder.build( new File( filename ) );

	    	Element rootElement = doc.getRootElement();
	    	Element eventActionElement;
    		Element eventElement;
    		Element conditionElement;
    		Element actionElement;

	    	@SuppressWarnings("rawtypes")
			List eventActionElements = rootElement.getChildren("eventaction");
	    	@SuppressWarnings("rawtypes")
			List eventList;
	    	@SuppressWarnings("rawtypes")
			List conditionList;
	    	@SuppressWarnings("rawtypes")
			List actionList;

	    	for ( int i=0; i<eventActionElements.size(); i++)
	    	{
	    		eventActionElement = (Element)eventActionElements.get(i);

	    		// create and initialize JFritzEventAction
				JFritzEventAction eventAction = new JFritzEventAction(eventActionElement.getAttributeValue("description"));
				eventAction.setDescription(eventActionElement.getAttributeValue("description"));
				eventAction.setActive(JFritzUtils.parseBoolean(eventActionElement.getAttributeValue("active")));
				eventAction.setVisible((JFritzUtils.parseBoolean(eventActionElement.getAttributeValue("active"))));
				eventAction.setConditionConcatenation(Integer.parseInt(eventActionElement.getAttributeValue("conditionconcatenation")));

				// Iterate over all events
	    		eventList = eventActionElement.getChildren("event");
	    		for (int j=0; j<eventList.size(); j++)
	    		{
	    			eventElement = (Element)eventList.get(j);

	    			// create JFritzEvent
	    			@SuppressWarnings("rawtypes")
					Class eventClass = getEventFromEventName(eventElement.getAttributeValue("eventname")).getClass();
	    			JFritzEvent eventObject = (JFritzEvent) eventClass.newInstance();

					eventAction.setEvent(eventObject);

					// add all saved conditions
					conditionList = eventElement.getChildren("condition");
	    			for ( int k=0; k<conditionList.size(); k++)
	    			{
		    			conditionElement = (Element)conditionList.get(k);
	    		    	JFritzEventParameter eventParameter = null;
	    				for ( byte l=0; l<eventObject.getParameterCount(); l++)
	    				{
	    					if ( eventObject.getParameter(l).getParameterPlaceHolder().equals(conditionElement.getAttributeValue("parameter")))
	    					{
	    						eventParameter = eventObject.getParameter(l);
	    					}
	    				}
	    				ConditionObject condObj = new ConditionObject(Integer.parseInt(conditionElement.getAttributeValue("cond")));

	    				// create JFritzEventCondition
	    				JFritzEventCondition condition = new JFritzEventCondition(eventParameter, condObj , conditionElement.getAttributeValue("value"));
	    				eventAction.addCondition(condition);
	    			}

	    			// add all saved actions
	    			actionList = eventElement.getChildren("action");
	    			for ( int m=0; m<actionList.size(); m++)
	    			{
		    			actionElement = (Element)actionList.get(m);

		    			// create JFritzEvent
		    			@SuppressWarnings("rawtypes")
						Class actionClass = getActionFromActionName(actionElement.getAttributeValue("name")).getClass();
		    			JFritzAction actionObject = (JFritzAction) actionClass.newInstance();
		    			actionObject.setDescription(actionElement.getAttributeValue("description"));
		    			actionObject.loadSettings(actionElement);
		    			eventAction.addAction(actionObject);
	    			}
	    		}
		    	addEvent(eventAction);
	    	}
	    } catch (JDOMParseException jdomex ) {
	    	// FIXME: I18N
	        Debug.errDlg(log, "Error parsing "+EVENT_MANAGMENT_FILE_NAME +"\n"+ "Line: " + jdomex.getLineNumber() + " Column: " + jdomex.getColumnNumber(), jdomex);
	    } catch( Exception ex ) {
	        ex.printStackTrace();
	      }
	}

	public static void saveToXML() {
		String filename = EVENT_MANAGMENT_FILE_NAME;
		log.info("Saving events to file " + filename); //$NON-NLS-1$
		try {
			BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF8")); //$NON-NLS-1$
		    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		    XMLOutputter stdOutputter = new XMLOutputter(Format.getPrettyFormat());

			Document doc = new Document();
			Element rootElement = new Element("eventhandling");
			JFritzEventAction eventAction;
			JFritzEventCondition condition;
			JFritzAction action;

			for ( int i=0; i<eventList.size(); i++) {
				eventAction = eventList.get(i);
				Element eventElement = new Element("event");
				eventElement.setAttribute("eventname", eventAction.getEvent().getName());

				for ( int j=0; j<eventAction.getConditionListSize(); j++) {
					Element conditionElement = new Element("condition");
					condition = eventAction.getCondition(j);
					conditionElement.setAttribute("parameter",condition.getParameter().getParameterPlaceHolder());
					conditionElement.setAttribute("cond",Integer.toString(condition.getCondition().getConditionID()));
					conditionElement.setAttribute("value",condition.getValue());
					eventElement.addContent(conditionElement);
				}

				for ( int k=0; k<eventAction.getActionListSize(); k++) {
					Element actionElement = new Element("action");
					action = eventAction.getAction(k);
					actionElement.setAttribute("name", action.getName());
					actionElement.setAttribute("description",action.getDescription());
					actionElement.addContent(action.saveSettings());
					eventElement.addContent(actionElement);
				}

				Element eventActionElement = new Element("eventaction");
				eventActionElement.setAttribute("description",eventAction.getDescription());
				if ( eventAction.isActive() )
					eventActionElement.setAttribute("active", "true");
				else
					eventActionElement.setAttribute("active", "false");
				if ( eventAction.isVisible() )
					eventActionElement.setAttribute("visible", "true");
				else
					eventActionElement.setAttribute("visible", "false");
				eventActionElement.setAttribute("conditionconcatenation", Integer.toString(eventAction.getConditionConcatenation()));
				eventActionElement.addContent(eventElement);

				stdOutputter.output(eventActionElement, System.out);

				rootElement.addContent(eventActionElement);
			}

			doc.setRootElement(rootElement);
			outputter.output(doc, pw);
			pw.close();

		} catch (UnsupportedEncodingException e) {
			log.error("UTF-8 not supported"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			log.error("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		} catch (IOException e) {
			log.error("IOException " + filename); //$NON-NLS-1$
		}

	}

	private static JFritzEvent getEventFromEventName(String eventName)
	{
		for ( int i=0; i<registeredEvents.size(); i++)
		{
			if ( registeredEvents.get(i).getName().equals(eventName))
				return registeredEvents.get(i);
		}
		return null;
	}

	private static JFritzAction getActionFromActionName(String actionName)
	{
		for ( int i=0; i<registeredActions.size(); i++)
		{
			if ( registeredActions.get(i).getName().equals(actionName))
				return registeredActions.get(i);
		}
		return null;
	}
}
