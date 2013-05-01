/*
 *
 * Created on 10.04.2005
 */

package de.moonflower.jfritz.exceptions;

public class RedirectToLoginLuaException extends Exception {
	private static final long serialVersionUID = 1;

	private String affectedBox = "";

    public RedirectToLoginLuaException() {
        super();
    }

    public RedirectToLoginLuaException(final String affectedBox, final String param) {
        super(param);
        this.affectedBox = affectedBox;
    }

    public String getAffectedBox()
    {
    	return affectedBox;
    }
}
