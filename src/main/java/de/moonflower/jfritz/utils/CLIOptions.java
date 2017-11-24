/*
 * Created on 25.06.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author Arno Willig
 *
 */
public class CLIOptions {
	private final static Logger log = Logger.getLogger(CLIOptions.class);

	Vector<CLIOption> CLIOptions;

	public CLIOptions() {
		CLIOptions = new Vector<CLIOption>();
	}

	/**
	 * Adds new Command Line Option
	 *
	 * @param shortOption
	 * @param longOption
	 * @param parameter
	 * @param description
	 */
	public void addOption(char shortOption, String longOption,
			String parameter, String description) {
		CLIOptions.add(new CLIOption(shortOption, longOption, parameter,
				description));
	}

	public void addOption(CLIOption opt) {
		CLIOptions.add(opt);
	}

	public boolean hasParameter(String optstr) {
		Enumeration<CLIOption> en = CLIOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = en.nextElement();
			if (("" + option.getShortOption()).equals(optstr) //$NON-NLS-1$
					|| option.getLongOption().equals(optstr)) {
				return option.hasParameter();
			}
		}
		return false;
	}

	public CLIOption findOption(String optstr, String parm) {
		Enumeration<CLIOption> en = CLIOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = en.nextElement();
			String shortOption = "" + option.getShortOption(); //$NON-NLS-1$
			if (shortOption.equals(optstr)
					|| option.getLongOption().equals(optstr)) {
				return new CLIOption(option.getShortOption(), option
						.getLongOption(), parm, option.getDescription());
			}
		}
		return null;
	}

	public void printOptions() {
		Enumeration<CLIOption> en = CLIOptions.elements();
		while (en.hasMoreElements()) {
			CLIOption option = en.nextElement();
			String line = "  -" + option.getShortOption(); //$NON-NLS-1$
			if (option.getLongOption().length() > 0)
				line += ", --" + option.getLongOption(); //$NON-NLS-1$
			if (option.getParameter() != null)
				line += "=[" + option.getParameter() + "]"; //$NON-NLS-1$,  //$NON-NLS-2$
			line = paddingString(line, 29, ' ', false); //$NON-NLS-1$
			line += option.getDescription();
			log.info(line);
			System.out.println(line);
		}
	}

	public Vector<CLIOption> parseOptions(String[] args) {
		Vector<CLIOption> foundOptions = new Vector<CLIOption>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--")) { //$NON-NLS-1$
				int pos = args[i].indexOf('='); //$NON-NLS-1$
				if (pos > 0) {
					String option = args[i].substring(2, pos);
					String parm = args[i].substring(pos + 1);
					foundOptions.add(findOption(option, parm));
				} else {
					String option = args[i].substring(2);
					String parm = ""; //$NON-NLS-1$
					foundOptions.add(findOption(option, parm));
				}
			} else if (args[i].startsWith("-")) { //$NON-NLS-1$
				int pos = 1;
				while (pos < args[i].length()) {
					String option = args[i].substring(pos, pos + 1);
					if (hasParameter(option)) {
						String parm = args[i].substring(pos + 1);
						foundOptions.add(findOption(option, parm));
						pos = args[i].length();
					} else {
						foundOptions.add(findOption(option, "")); //$NON-NLS-1$
						pos++;
					}
				}
			}
		}
		return foundOptions;
	}

	/**
	 * pad a string S with a size of N with char C on the left (True) or on the
	 * right(flase)
	 */
	public synchronized String paddingString(String s, int n, char c,
			boolean paddingLeft) {
		StringBuffer str = new StringBuffer(s);
		int strLength = str.length();
		if (n > 0 && n > strLength) {
			for (int i = 0; i <= n; i++) {
				if (paddingLeft) {
					if (i < n - strLength)
						str.insert(0, c);
				} else {
					if (i > strLength)
						str.append(c);
				}
			}
		}
		return str.toString();
	}

}
