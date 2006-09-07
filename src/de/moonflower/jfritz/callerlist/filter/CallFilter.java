/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;

public abstract class CallFilter {

    JFritz jfritz;

    public CallFilter(JFritz jfritz) {
        this.jfritz = jfritz;
    }

    public abstract boolean filterPassed(Call currentCall);

    public abstract void setFilter(int filter);
}
