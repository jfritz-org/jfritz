package de.moonflower.jfritz.network;

import java.io.Serializable;

import java.util.Vector;

/**
 * Class used to store data change operations for network usage
 * Currently supported are the operations ADD, REMOVE for
 * the destinations CallList and PhoneBook
 *
 * Not this objects a new vector on construction and is intended
 * to be reused so that memory usage can be reduced
 *
 * @author brian
 *
 */
public class DataChange<E> implements Serializable {

	//makes serialization go a bit faster
	public static final long serialVersionUID = 100;

	public enum Operation {ADD, UPDATE, REMOVE}

	public Operation operation;

	public enum Destination {CALLLIST, PHONEBOOK}

	public Destination destination;

	public Vector<E> data;

	public E original, updated;

}
