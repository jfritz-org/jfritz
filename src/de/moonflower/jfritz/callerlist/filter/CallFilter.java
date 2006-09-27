/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.JFritzUtils;

public abstract class CallFilter {
	final String name;
    CallFilter(String name){this.name = name;}

    public final boolean passFilter(Call currentCall){
    	// if the filter is disabled just let everything pass otherwise block everything
    	if (filerIsDisabled())return true;
    	return passFilterIntern(currentCall);
    }

    abstract boolean passFilterIntern(Call currentCall);

    public boolean filerIsDisabled(){
    	return !JFritzUtils.parseBoolean(JFritz.getProperty(name));
    }
    public abstract void setFilter(int filter);
}
