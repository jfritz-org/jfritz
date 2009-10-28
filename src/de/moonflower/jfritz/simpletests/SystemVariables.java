package de.moonflower.jfritz.simpletests;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class SystemVariables {

	public static void main(String[] args)
	{
	    System.out.println("========= Environment variables ========");
	    Map<String, String> env = System.getenv();
	    for (String envName : env.keySet()) {
	        System.out.format("%s=%s%n", envName, env.get(envName));
	    }

	    System.out.println("");
	    System.out.println("========= System properties ========");
	    Properties props = System.getProperties();
	    Enumeration<Object> en = props.keys();
	    String currentKey = "";
	    while (en.hasMoreElements())
	    {
	    	currentKey = (String)en.nextElement();
	    	System.out.println(currentKey + ": " + System.getProperty(currentKey));
	    }
	}
}
