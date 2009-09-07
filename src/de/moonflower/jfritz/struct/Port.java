package de.moonflower.jfritz.struct;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.moonflower.jfritz.box.BoxClass;

public class Port implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3535276686594592176L;

	private static HashMap<Integer, Port> allPorts = null;

	private int id = -1;

	private String name = "";

	private String dialPort = "";

	private String internalNumber = "";

	private BoxClass box = null;

	public Port(int id, String name, String dialPort, String internalNumber)
	{
		this.id = id;
		this.name = name;
		this.dialPort = dialPort;
		this.internalNumber = internalNumber;
		if (allPorts == null)
		{
			addStaticPorts();
		}
	}

	public String toString()
	{
		if (box != null)
		{
			return box.getName() + ": " + name;
		}
		else
		{
			return name;
		}
	}

	public String getName()
	{
		return name;
	}

	public String getDialPort()
	{
		return dialPort;
	}

	public String getInternalNumber()
	{
		return internalNumber;
	}

	public int getId()
	{
		return id;
	}

	private void addStaticPorts()
	{
		allPorts = new HashMap<Integer, Port>();

		// add static analog ports
		addPort(new Port(0, "Fon 1", "1", "1"));
		addPort(new Port(1, "Fon 2", "2", "2"));
		addPort(new Port(2, "Fon 3", "3", "3"));

		// add static isdn ports
		addPort(new Port(50, "Alle ISDN/DECT Telefone", "50", "50"));
		addPort(new Port(51, "ISDN 1", "51", "51"));
		addPort(new Port(52, "ISDN 2", "52", "52"));
		addPort(new Port(53, "ISDN 3", "53", "53"));
		addPort(new Port(54, "ISDN 4", "54", "54"));
		addPort(new Port(55, "ISDN 5", "55", "55"));
		addPort(new Port(56, "ISDN 6", "56", "56"));
		addPort(new Port(57, "ISDN 7", "57", "57"));
		addPort(new Port(58, "ISDN 8", "58", "58"));
		addPort(new Port(59, "ISDN 9", "59", "59"));

		// add static dect ports
		addPort(new Port(10, "DECT 1", "60", "610"));
		addPort(new Port(11, "DECT 2", "61", "611"));
		addPort(new Port(12, "DECT 3", "62", "612"));
		addPort(new Port(13, "DECT 4", "63", "613"));
		addPort(new Port(14, "DECT 5", "64", "614"));
		addPort(new Port(15, "DECT 6", "65", "615"));

		// add static VoIP-Extension ports
		addPort(new Port(20, "VoIP-Extension 1", "20", "620"));
		addPort(new Port(21, "VoIP-Extension 2", "21", "621"));
		addPort(new Port(22, "VoIP-Extension 3", "22", "622"));
		addPort(new Port(23, "VoIP-Extension 4", "23", "623"));
		addPort(new Port(24, "VoIP-Extension 5", "24", "624"));
		addPort(new Port(25, "VoIP-Extension 6", "25", "625"));
		addPort(new Port(26, "VoIP-Extension 7", "26", "626"));
		addPort(new Port(27, "VoIP-Extension 8", "27", "627"));
		addPort(new Port(28, "VoIP-Extension 9", "28", "628"));
		addPort(new Port(29, "VoIP-Extension 10", "29", "629"));

		// add other static ports
		addPort(new Port(3, "Durchwahl", "-1", "-1"));
		addPort(new Port(4, "ISDN", "-1", "-1"));
		addPort(new Port(5, "FAX/FON", "-1", "-1"));
		addPort(new Port(6, "Anrufbeantworter", "-1", "-1"));
		addPort(new Port(32, "DATA Fon 1", "-1", "-1"));
		addPort(new Port(33, "DATA Fon 2", "-1", "-1"));
		addPort(new Port(34, "DATA Fon 3", "-1", "-1"));
		addPort(new Port(36, "DATA Fon S0/ISDN", "-1", "-1"));
	}

	private void addPort(Port port)
	{
		allPorts.put(port.getId(), port);
	}

	public static Port getPort(int id)
	{
		if (allPorts.get(id) != null)
		{
			return allPorts.get(id);
		}
		else
		{
			return null;
		}
	}

	public void setBox(BoxClass box)
	{
		this.box = box;
	}

	public BoxClass getBox()
	{
		return box;
	}

	public boolean equals(Object obj) {
		Port port;
		if (!(obj instanceof Port)) {
			return false;
		}
		port = (Port) obj;

		if (this.getName().equals(port.getName()))
		{
			return true;
		}

		if (this.getName().equals("ISDN Geraet")
				&& (port.getName().equals("ISDN Gerät")))
		{
			return true;
		}

		if (this.getName().equals("ISDN Gerät")
				&& (port.getName().equals("ISDN Geraet")))
		{
			return true;
		}

		if (this.getName().equals("ISDN Geraet")
			&& (port.getName().equals("ISDN")))
		{
			return true;
		}

		if (!"".equals(this.getName()))
		{
			try {
				int portNr = Integer.parseInt(this.getName());
				if (portNr == port.getId())
				{
					return true;
				}
			} catch (NumberFormatException nfe)
			{
				Collection<Port> portList = allPorts.values();
				Iterator<Port> portIterator = portList.iterator();
				while (portIterator.hasNext())
				{
					Port current = portIterator.next();
					if (current.getName().equals(this.getName()))
					{
						if (port.getId() == current.getId())
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
