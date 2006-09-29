/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import de.moonflower.jfritz.struct.Call;

public abstract class CallFilter {
    public abstract boolean passFilter(Call currentCall);
}
