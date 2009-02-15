package de.moonflower.jfritz.phonebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

public class NumberPersonMultiHashMap {

	private Map<String, List<Person>> hashMap;

	public NumberPersonMultiHashMap()
	{
		hashMap = new HashMap<String, List<Person>>();
	}

	public void addPerson(PhoneNumber number, Person person)
	{
		List<Person> l = hashMap.get(number.getIntNumber());
		if (l == null)
		{
			hashMap.put(number.getIntNumber(), l=new ArrayList<Person>());
		}
		if (!l.contains(person))
		{
			l.add(person);
		}
	}

	public List<Person> getPerson(PhoneNumber number)
	{
		return hashMap.get(number.getIntNumber());
	}

	public void deletePerson(PhoneNumber number, Person person)
	{
		List<Person> l = hashMap.get(number.getIntNumber());
		l.remove(person);
		hashMap.remove(number.getIntNumber());
		hashMap.put(number.getIntNumber(), l);
	}

	public List<Person> getAllPerson()
	{
		List<Person> result = new ArrayList<Person>();
		Iterator<List<Person>> it = hashMap.values().iterator();
		while (it.hasNext())
		{
			List<Person> list = it.next();
			for (Person p:list)
			{
				if (!result.contains(p))
				{
					result.add(p);
				}
			}
		}
		return result;
	}
}
