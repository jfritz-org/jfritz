package de.moonflower.jfritz.network;

import java.io.Serializable;

import java.util.Vector;

/**
 * Class used to store data change operations for network usage
 * Currently supported are the operations ADD, REMOVE for
 * the destinations CallList and PhoneBook
 *
 * New: This object also supports sending call monitor information
 * but it will probably be removed in the next version for a better
 * model, i.e. this implementation is only temporary
 *
 * @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 */
public class DataChange<E> implements Serializable {

	//makes serialization go a bit faster
	public static final long serialVersionUID = 100;

	public enum Operation {ADD, UPDATE, REMOVE}

	public Operation operation;

	public enum Destination {CALLLIST, PHONEBOOK, CALLMONITOR}

	public Destination destination;

	public Vector<E> data;

	public E original, updated;

	public DataChange<E> clone() {
		DataChange<E> clone = new DataChange<E>();
		clone.operation = this.operation;
		clone.destination = this.destination;
		clone.data = this.data;
		clone.original = this.original;
		clone.updated = this.updated;

		return clone;
	}
}
