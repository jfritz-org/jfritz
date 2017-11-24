package de.moonflower.jfritz.network;

import java.io.Serializable;

import java.util.Date;
import java.util.Vector;

/**
 * This class represents client requests to the server.
 * Currently the fields data and timestamp are optional
 * and should be left null if not used to save space and bandwidth.
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 * @param <E>
 */
public class ClientDataRequest<E> implements Serializable {

	public static final long serialVersionUID = 100;

	public enum Operation{ ADD, GET, REMOVE, UPDATE}

	public Operation operation;

	public enum Destination{CALLLIST, PHONEBOOK}

	public Destination destination;

	public Vector<E> data;

	public Date timestamp;

	public E original, updated;

}
